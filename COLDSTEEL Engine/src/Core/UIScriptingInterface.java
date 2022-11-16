package Core;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;
import org.python.core.PyCode;

import CS.Engine;
import CS.UserInterface;
import CSUtil.DataStructures.Tuple2;
import Game.Items.Items;

/**
 * This class provides safe access to LWJGL UI utilities without allowing the of breaking encapsulation. Further, it is generic enough to suit
 * more than one use case. It can support UI scripting for entities and levels, or whatever wants to create a UI script.
 * 
 * @author Chris Brown
 *
 */
public class UIScriptingInterface extends UserInterface{

	static final PyCode UI_SCRIPTING_FACADE = Engine.INTERNAL_ENGINE_PYTHON().compile("CS_uiScriptingFunctions.py");
	
	private Engine engine;
	private Console console;
	public record ImageInfo(NkImage image , int width , int height , int bitsPP) {}
	
	public UIScriptingInterface(Engine engine) {
	
		super("UIScriptingInterface" , 0 , 0 , 0 , 0 , 0 , 0);
		this.engine = engine;
		
	}
	
	/**
	 * Returns the offheap allocator being used to store objects and images. This is not used for all allocations, only those for which it is explicitly
	 * invoked, as LWJGL will use the thread local stack inside it's API
	 * 
	 * @return the UI allocator, a memory stack
	 */
	public MemoryStack getAllocator() {
		
		return ALLOCATOR;
		
	}
		
	/**
	 * Returns the programs console. 
	 */
	public Console getConsole() {
		
		return console;
		
	}
		
	public NkContext getContext() { 
		
		return context;
		
	}
	
	/**
	 * Creates and returns an image for nuklear, specifically {@code nk_image()}. The returned object is a record info about the image, as well as its
	 * width, height, and bits per pixel
	 * 
	 * @param filepath — filepath to an image
	 * @return a record containing useful info about the image, as well as the NkImage object itself.
	 */
	public ImageInfo image(String filepath) {
		
		NkImage image = NkImage.malloc(ALLOCATOR);
		var textureImageInfo = image(filepath, image);
		return new ImageInfo(image , image.w() , image.h() , textureImageInfo.BPP());
		
	}

	public Tuple2<NkImage , NkRect> itemIconAsImageSubRegion(Items item){
		
		float textureWidth = item.getTexture().imageInfo.width() , textureHeight = item.getTexture().imageInfo.height();
		
		float[] sprite = item.iconSprite();
		short leftX = (short) (textureWidth * sprite[0]);
		short topY = (short) (textureHeight - (textureHeight * sprite[2]));
		
		NkImage itemTextureAsImage = NkImage.malloc(ALLOCATOR);
		image(item.texture.imageInfo.path() , itemTextureAsImage);
		
		Tuple2<NkImage , NkRect> result = subRegion(itemTextureAsImage, item.texture.imageInfo , leftX, topY, (short)(sprite[4] * 2) , (short)(sprite[5] * 2));
		return result;
		
	}
		
	public boolean isServer() {
		
		return engine.mg_isHostedServerRunning();
		
	}
	
}
