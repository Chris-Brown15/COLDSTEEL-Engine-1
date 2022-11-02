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
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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

import CSUtil.NkInitialize;
import CSUtil.DataStructures.Tuple2;
import Core.Console;
import Core.Executor;
import Renderer.Textures;
import Renderer.Textures.ImageInfo;

/**
 * Exposes useful and necessary functionality to the implementor while encapsulating UI threading within itself.
 * 
 * @author Chris Brown
 *
 */
public abstract class UserInterface {

	private static final long UI_MEMORY = nmemAlloc(CS.COLDSTEEL.UI_MEMORY_SIZE_KILOS * 1024);
	protected static final MemoryStack ALLOCATOR = MemoryStack.ncreate(UI_MEMORY, CS.COLDSTEEL.UI_MEMORY_SIZE_KILOS * 1024);
	
	private static final NkInitialize staticInitializer;
	protected final static NkContext context;
	final static NkBuffer drawCommands;
	
	public static final NkPluginFilter DEFAULT_FILTER = NkPluginFilter.create(Nuklear::nnk_filter_default);
	public static final NkPluginFilter NUMBER_FILTER = NkPluginFilter.create(Nuklear::nnk_filter_float);
	
	private static final ConcurrentLinkedQueue<UserInterface> ELEMENTS = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<Tuple2<UserInterface , Consumer<MemoryStack>>> LAYOUT_BODY_COMMANDS = new ConcurrentLinkedQueue<>();
	
	public static AtomicBoolean ITERATED_ALL_ELEMENTS = new AtomicBoolean(false);
	
	private static final boolean iteratedAllElements() {
		
		return ITERATED_ALL_ELEMENTS.get();
		
	}
	
	private static final Thread NUKLEAR_THREAD = new Thread(new Runnable() {

		@Override public void run() {

			while(true) {
				
				if(!iteratedAllElements()) {
					
					ELEMENTS.forEach((element) -> {
					
						if(element.end) { 
							
							ELEMENTS.remove(element);
							element.onEnd.execute();
							
						} else element.layout();
						
					});
					
					ITERATED_ALL_ELEMENTS.set(true);
					
				} else while (!LAYOUT_BODY_COMMANDS.isEmpty()) {
					
					Tuple2<UserInterface , Consumer<MemoryStack>> command = LAYOUT_BODY_COMMANDS.remove();
					command.getFirst().layoutBody = command.getSecond();
					
				}
				
			}
			
		}
		
	});

	static {
		
		staticInitializer = new NkInitialize();
		staticInitializer.initNKGUI();
    	context = staticInitializer.getContext(); 
    	drawCommands = staticInitializer.getCommands();
    	NUKLEAR_THREAD.setDaemon(true);
    	NUKLEAR_THREAD.setName("Nuklear UI Thread");
    	
	}

	static final void threadSpinup() {
		
		NUKLEAR_THREAD.start();
		
	}
	
	/**
	 * Creates and returns an image for nuklear, specifically {@code nk_image()}. The returned object is a record info about the image, as well as its
	 * width, height, and bits per pixel
	 * 
	 * @param filepath — filepath to an image
	 * @return a record containing useful info about the image, as well as the NkImage object itself.
	 */
	public static ImageInfo image(String filepath , NkImage image) {
		
		int textureID = glGenTextures();
		
		try(MemoryStack frame = MemoryStack.stackPush()) {
			
			IntBuffer stats = frame.mallocInt(3);
			
			stbi_set_flip_vertically_on_load(false);
			ByteBuffer imageData = stbi_load(filepath , stats.slice(0, 1) , stats.slice(1, 1) , stats.slice(2, 1) , 3);
			
			if(imageData == null) throw new IllegalStateException("Failed to allocate memory for image: " + filepath);
			
			glBindTexture(GL_TEXTURE_2D , textureID);
			glPixelStorei(GL_UNPACK_ALIGNMENT , 1);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, stats.get() , stats.get() , 0, stats.get() == 32 ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, imageData);
			glGenerateMipmap(GL_TEXTURE_2D);
			
			image.handle(it -> it.id(textureID));
			
			//undo setup
			glBindTexture(GL_TEXTURE_2D , 0);
			stbi_image_free(imageData);
			stbi_set_flip_vertically_on_load(true);
			
			//create a java texture object so we can free this GPU memory later
			stats.rewind();
			ImageInfo newImageInfo = new ImageInfo(filepath , stats.get() , stats.get() , stats.get());
			
			Textures texture = new Textures();
			
			Renderer.Renderer.loadTexture(texture , textureID , newImageInfo);
			
			return newImageInfo;
			
		}
		
	}   
	
	public static Tuple2<NkImage , NkRect> subRegion(NkImage source , ImageInfo sourceImageInfo , short topLeftX , short topLeftY , short width , short height) {
		
		NkImage newImage = NkImage.malloc(ALLOCATOR);
		NkRect subsection = NkRect.malloc(ALLOCATOR).set(topLeftX , topLeftY , width , height);
		nk_subimage_id(source.handle().id() , (short)sourceImageInfo.width() , (short)sourceImageInfo.height() , subsection , newImage);		
		return new Tuple2<>(newImage , subsection);
		
	}
	
	public static final void printMemoryDetails(Console console) {

		console.sayln("Allocated (bytes): " + ALLOCATOR.getSize());
		console.sayln("Remaining (bytes): " + ALLOCATOR.getPointer());
								
	}
		
	public static final void shutDown1() {
		
		NUKLEAR_THREAD.interrupt();
		staticInitializer.shutDown();
		DEFAULT_FILTER.free();
		NUMBER_FILTER.free();
		nmemFree(UI_MEMORY);
		
	}
	
	public static final void shutDown2() {
		
		staticInitializer.shutDownAllocator();
		
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
	
	public UserInterface(String title , float x , float y , float w , float h , int normalOptions , int unopenedOptions) {
		
		this.normalOptions = normalOptions;
		this.unopenedOptions = unopenedOptions;
		this.name = title;
		rect = NkRect.malloc(ALLOCATOR).set(x, y, w, h);
		ELEMENTS.add(this);
		
	}
	
	protected void layoutBody(Consumer<MemoryStack> body) {
		
		LAYOUT_BODY_COMMANDS.add(new Tuple2<>(this , body));
		
	}
	
	protected void layoutBody(PyObject callable) {
		
		LAYOUT_BODY_COMMANDS.add(new Tuple2<>(this , (frame) -> callable.__call__(new ClassicPyObjectAdapter().adapt(frame))));
		
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

