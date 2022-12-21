package CS;

import static org.lwjgl.system.MemoryUtil.nmemAlloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;

import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_subimage_id;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import org.lwjgl.nuklear.NkBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;
import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;

import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.Tuple4;
import CSUtil.DataStructures.cdNode;
import Core.Console;
import Core.Executor;
import Renderer.Renderer;
import Renderer.Textures;

/**
 * Exposes useful and necessary functionality to the implementor while encapsulating UI threading within itself.
 * 
 * @author Chris Brown
 *
 */
public abstract class UserInterface {

	private static final long UI_MEMORY = nmemAlloc(CS.COLDSTEEL.UI_MEMORY_SIZE_KILOS * 1024);
	protected static final MemoryStack ALLOCATOR = MemoryStack.ncreate(UI_MEMORY, CS.COLDSTEEL.UI_MEMORY_SIZE_KILOS * 1024);
	
	private static NkInitialize staticInitializer = new NkInitialize();
	protected static NkContext context;
	static NkBuffer drawCommands;
	
	public static final NkPluginFilter DEFAULT_FILTER = NkPluginFilter.create(Nuklear::nnk_filter_default);
	public static final NkPluginFilter NUMBER_FILTER = NkPluginFilter.create(Nuklear::nnk_filter_float);
	
	private static volatile CSLinked<UserInterface> ELEMENTS = new CSLinked<>();
	protected static volatile int[] currentWindowDimensions = new int[2];
	
	/* All actions on the Nuklear data structures must be synchronized. One of three threads may want to modify them
	 * 		1) This thread, which wants to enqueue draw commands
	 * 		2) The render thread, which wants to dequeue draw commands 
	 * 		3) the main thread, which wants to write user input state  
	 * 
	 * The actions of the render thread must be synchronized against this thread, and the actions of the main thread must
	 * be synchronized against this thread, but the actions the main thread and the render thread take on Nuklear structs
	 * +++I dont think+++ need to be synchronized against each other.
	 */
	public static final Runnable NUKLEAR_RUNNABLE = () -> {

		synchronized(ELEMENTS) {

			cdNode<UserInterface> iter = ELEMENTS.get(0);
			for(int i = 0 ; i < ELEMENTS.size() ; i ++) {
			
				if(iter.val.end) {
					
					if(iter.val.onEnd != null) iter.val.onEnd.execute();
					iter = ELEMENTS.safeRemove(iter);
					
				} else {
					
					iter.val.layout();
					iter = iter.next;
					
				}
				
			}
						
		}
		
	};
			
	static void initialize(Renderer renderer) {
		
		/*
		 * Requires code to execute which must be called from the renderer thread.
		 * After this call executes we will block until opengl calls the code we need.
		 * Then we can finish initializing
		 */
		staticInitializer.setupFont();
		
		//wait until opengl initializes this
		while(!staticInitializer.initialized);
				
		staticInitializer.initNKGUI();
		staticInitializer.initialized = false;
		
    	context = staticInitializer.getContext(); 
    	drawCommands = staticInitializer.getCommands();
    	
    	staticInitializer.initialized = true;
    	
	}

	static boolean initialized() {
		
		return staticInitializer.initialized;
		
	}
	
	/**
	 * Creates and returns an image for nuklear, specifically {@code nk_image()}. The returned object is a record info about the image, as well as its
	 * width, height, and bits per pixel
	 * 
	 * @param filepath — filepath to an image
	 * @return a record containing useful info about the image, as well as the NkImage object itself.
	 */
	public static Textures image(String filepath , NkImage image) {
		
		Textures texture = new Textures();
		
		texture.onInitialize(textureID -> {
			
			try(MemoryStack stack = MemoryStack.stackPush()){
				
				IntBuffer width = stack.mallocInt(1) , height = stack.mallocInt(1) , bitsPerPixel = stack.mallocInt(1);

				stbi_set_flip_vertically_on_load(false);
				ByteBuffer imageData = stbi_load(filepath , width , height , bitsPerPixel , 3);
				
				if(imageData == null) throw new IllegalStateException("Failed to allocate memory for image: " + filepath);

				glPixelStorei(GL_UNPACK_ALIGNMENT , 1);
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width.get() , height.get() , 0, bitsPerPixel.get() == 32 ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, imageData);
				glGenerateMipmap(GL_TEXTURE_2D);
				
				image.handle(it -> it.id(textureID));
				
				stbi_image_free(imageData);
				stbi_set_flip_vertically_on_load(true);
				
				return new Tuple4<>(filepath , width.get(0) , height.get(0) , bitsPerPixel.get(0));
				
			}
			
		});
					
		Renderer.initializeTexture(texture , filepath);
		return texture;	
		
	}   
	
	public static Tuple2<NkImage , NkRect> subRegion(NkImage source , Textures sourceTexture , short topLeftX , short topLeftY , short width , short height) {
		
		NkImage newImage = NkImage.malloc(ALLOCATOR);
		NkRect subsection = NkRect.malloc(ALLOCATOR).set(topLeftX , topLeftY , width , height);
		nk_subimage_id(source.handle().id() , (short) sourceTexture.width() , (short) sourceTexture.height() , subsection , newImage);		
		return new Tuple2<>(newImage , subsection);
		
	}
	
	public static final void printMemoryDetails(Console console) {

		console.sayln("Allocated (bytes): " + ALLOCATOR.getSize());
		console.sayln("Remaining (bytes): " + ALLOCATOR.getPointer());
								
	}
		
	public static final void static_shutDown() {
		
		staticInitializer.shutDown();
		DEFAULT_FILTER.free();
		NUMBER_FILTER.free();
		nmemFree(UI_MEMORY);
		
		staticInitializer.shutDownAllocator();
		
	
		
	}
	
	static NkContext getContext() {
		
		return context;
				
	}

	static NkBuffer getCommands() {
		
		return drawCommands;
				
	}
	
	protected volatile boolean show = false;
	protected volatile boolean end = false;
	
	private volatile Consumer<MemoryStack> layoutBody = (frame) -> {};
	private volatile Executor onEnd;
	private int normalOptions;
	private int unopenedOptions;
	protected String name;
	private NkRect rect;	
	private boolean firstTimeOpening = true;
	protected float x;
	protected float y;
	protected float w;
	protected float h;
	
	public UserInterface(String title , float x , float y , float w , float h , int normalOptions , int unopenedOptions) {
		
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		this.normalOptions = normalOptions;
		this.unopenedOptions = unopenedOptions;
		this.name = title;
		rect = NkRect.malloc(ALLOCATOR).set(x, y, w, h);
		synchronized(ELEMENTS) {
			
			ELEMENTS.add(this);
		
		}
		
	}
	
	protected void layoutBody(Consumer<MemoryStack> body) {
		
		layoutBody = body;
				
	}
	
	protected void layoutBody(PyObject callable) {
		
		layoutBody = (frame) -> callable.__call__(new ClassicPyObjectAdapter().adapt(frame));
		
	}
	
	protected void onEnd(Executor onEnd) {
		
		this.onEnd = onEnd;
		
	}
	
	private void layout() {
		
		if(!show) return;
		
		try(MemoryStack stackFrame = ALLOCATOR.push()){

			if(nk_begin(context , name , rect , firstTimeOpening ? unopenedOptions : normalOptions) && layoutBody != null) { 

				layoutBody.accept(stackFrame);
				firstTimeOpening = false;				
			
			}
			
			nk_end(context);	
			
		}
		
	}
	
	public boolean showing() {
		
		return show;
		
	}
	
	protected boolean firstTimeOpening() {
		
		return firstTimeOpening;
		
	}
	
}