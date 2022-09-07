package Core;

import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;
import org.python.core.PyCode;

import CS.Engine;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.Tuple2;
import Game.Items.Items;

/**
 * This class provides safe access to LWJGL UI utilities without allowing the of breaking encapsulation. Further, it is generic enough to suit
 * more than one use case. It can support UI scripting for entities and levels, or whatever wants to create a UI script.
 * 
 * @author Chris Brown
 *
 */
public class UIScriptingInterface implements NKUI {

	static final PyCode UI_SCRIPTING_FACADE = Engine.INTERNAL_ENGINE_PYTHON().compile("CS_uiScriptingFunctions.py");
	
	private static CSArray<UIScript> pyUIs = new CSArray<UIScript>(25 , 2);
	private Console console;
	public record ImageInfo(NkImage image , int width , int height , int bitsPP) {}
	
	public UIScriptingInterface(Console console) {
	
		this.console = console;
		
	}
	
	/**
	 * Returns the offheap allocator being used to store objects and images. This is not used for all allocations, only those for which it is explicitly
	 * invoked, as LWJGL will use the thread local stack inside it's API
	 * 
	 * @return the UI allocator, a memory stack
	 */
	public MemoryStack getAllocator() {
		
		return allocator;
		
	}
	
	public static CSArray<UIScript> getPyUIs(){
		
		return pyUIs;
		
	}
	
	/**
	 * Mallocates a NkRect object and returns it. it's values are not set.
	 * 
	 * @return a NkRect instance allocated by {@code NkRect.malloc(allocator)}
	 */
	public NkRect newRect() {
		
		return NkRect.malloc(allocator);

	}
	
	/**
	 * Returns the editor's console. This will eventually be replaced but for now since the editor is the only console available, this returns that.
	 * @return the editor console
	 */
	public Console console() {
		
		return console;
		
	}
		
	/**
	 * Creates and returns an image for nuklear, specifically {@code nk_image()}. The returned object is a record info about the image, as well as its
	 * width, height, and bits per pixel
	 * 
	 * @param filepath — filepath to an image
	 * @return a record containing useful info about the image, as well as the NkImage object itself.
	 */
	public ImageInfo image(String filepath) {
		
		NkImage image = NkImage.malloc(allocator);
		var textureImageInfo = NKUI.image(filepath, image);
		return new ImageInfo(image , image.w() , image.h() , textureImageInfo.BPP());
		
	}

	/**
	 * Adds a UIScript object into a static data structure containing a list of Python UI objects.
	 * 
	 * @param addThis — a UIScript to add
	 */
	public static void addUI(UIScript addThis) {
		
		pyUIs.add(addThis);
		
	}
	
	/**
	 * Removes a UIScript object from a static data structure containing a list of Python UI objects.
	 * 
	 * @param remThis — a UIScript to remove
	 */
	public static void removeUI(UIScript remThis) {
		
		pyUIs.remove(remThis);
		
	}
	
	public Tuple2<NkImage , NkRect> itemIconAsImageSubRegion(Items item){
		
		float textureWidth = item.getTexture().imageInfo.width() , textureHeight = item.getTexture().imageInfo.height();
		
		float[] sprite = item.iconSprite();
		short leftX = (short) (textureWidth * sprite[0]);
		short topY = (short) (textureHeight - (textureHeight * sprite[2]));
		
		NkImage itemTextureAsImage = NkImage.malloc(allocator);
		NKUI.image(item.texture.imageInfo.path() , itemTextureAsImage);
		
		Tuple2<NkImage , NkRect> result = NKUI.subRegion(itemTextureAsImage, item.texture.imageInfo , leftX, topY, (short)(sprite[4] * 2) , (short)(sprite[5] * 2));
		return result;
		
	}
	
}
