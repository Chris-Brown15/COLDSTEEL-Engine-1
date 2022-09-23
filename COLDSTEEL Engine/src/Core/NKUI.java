package Core;

import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGB;
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
import static org.lwjgl.nuklear.Nuklear.nk_subimage_id;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import CS.Engine;
import CSUtil.DataStructures.Tuple2;
import Renderer.Textures;
import Renderer.Textures.ImageInfo;

public interface NKUI {

	public static final NkPluginFilter DEFAULT_FILTER = NkPluginFilter.create(Nuklear::nnk_filter_default);
	public static final NkPluginFilter NUMBER_FILTER = NkPluginFilter.create(Nuklear::nnk_filter_float);
	
	NkContext context = Engine.NuklearContext();
	MemoryStack allocator = Engine.UIAllocator();
	Supplier<int[]> windowDims = Engine.windowDims;
	

	/**
	 * Creates and returns an image for nuklear, specifically {@code nk_image()}. The returned object is a record info about the image, as well as its
	 * width, height, and bits per pixel
	 * 
	 * @param filepath — filepath to an image
	 * @return a record containing useful info about the image, as well as the NkImage object itself.
	 */
	public static ImageInfo image(String filepath , NkImage image) {
		
		int textureID = glGenTextures();
		
		allocator.push();
		
		IntBuffer width = allocator.mallocInt(1);
		IntBuffer height = allocator.mallocInt(1);
		IntBuffer BBP = allocator.mallocInt(1);
		stbi_set_flip_vertically_on_load(false);
		ByteBuffer imageData = stbi_load(filepath , width , height , BBP , 3);
		
		glBindTexture(GL_TEXTURE_2D , textureID);
		glPixelStorei(GL_UNPACK_ALIGNMENT , 1);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width.get(0) , height.get(0), 0, BBP.get(0) == 32 ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, imageData);
		glGenerateMipmap(GL_TEXTURE_2D);
		
		allocator.pop();
		
		image.handle(it -> it.id(textureID));
		
		//undo setup
		glBindTexture(GL_TEXTURE_2D , 0);
		stbi_image_free(imageData);
		stbi_set_flip_vertically_on_load(true);
		
		//create a java texture object so we can free this GPU memory later
		ImageInfo newImageInfo = new ImageInfo(filepath , width.get(0) , height.get(0) , BBP.get(0));
		Renderer.Renderer.addTexture(new Textures(textureID , newImageInfo));
		
		return newImageInfo;
		
	}   
	
	public static Tuple2<NkImage , NkRect> subRegion(NkImage source , ImageInfo sourceImageInfo , short topLeftX , short topLeftY , short width , short height) {
		
		NkImage newImage = NkImage.malloc(allocator);
		NkRect subsection = NkRect.malloc(allocator).set(topLeftX , topLeftY , width , height);
		nk_subimage_id(source.handle().id() , (short)sourceImageInfo.width() , (short)sourceImageInfo.height() , subsection , newImage);		
		return new Tuple2<>(newImage , subsection);
		
	}
		
}
