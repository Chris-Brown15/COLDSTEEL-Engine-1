package Renderer;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGB8;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL13.glIsTexture;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.lwjgl.system.MemoryStack;

public class Textures {

	public static record ImageInfo(String path , int width , int height , int BPP){};
	
	private int textureID;
	private ByteBuffer data;	
	public ImageInfo imageInfo;
	private boolean initialized = false;
	
	String filepath() {
	
		return imageInfo.path;
		
	}	
	
	void free() {

		if(data != null) stbi_image_free(data);
		
	}
	
	void initialize(String filepath) {
		
		if(Files.notExists(Paths.get(filepath))) assert false: filepath + " does not point to a file.";		
		
		try(MemoryStack stack = stackPush()){
			
			IntBuffer widthPtr = stack.ints(0); 
			IntBuffer heightPtr = stack.ints(0);
			IntBuffer bitsPerPixelPtr = stack.ints(0);
			data = stbi_load(filepath , widthPtr,  heightPtr , bitsPerPixelPtr , 4);
			
			if(data == null) {
				
				if(CS.COLDSTEEL.DEBUG_CHECKS) assert false : "Error: Unable to load asset at " + filepath;
				else System.err.println("Error: Unable to load asset at " + filepath);
				
			}
			
			imageInfo = new ImageInfo(filepath , widthPtr.get(0) , heightPtr.get(0) , bitsPerPixelPtr.get(0));
						
		}
		
		textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D , textureID);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D , 0 , GL_RGB8 , imageInfo.width() , imageInfo.height() , 0 , GL_RGBA , GL_UNSIGNED_BYTE , data);
		glBindTexture(GL_TEXTURE_2D , 0);
		stbi_image_free(data);
		
		initialized = true;
		
	}
	
	void initialize(int textureID , ImageInfo image) {
		
		assert glIsTexture(textureID) : "ERROR: " + textureID + " is not a valid openGL texture handle.";
		
		this.textureID = textureID;
		this.imageInfo = image;
		
		initialized = true;
		
	}
	
	int textureID() {
		
		return textureID;
		
	}
	
	void activate(int slot) {
		
		glActiveTexture(GL_TEXTURE0 + slot);
		glBindTexture(GL_TEXTURE_2D , textureID);
		
	}
	
	void deactivate() {
		
		glBindTexture(GL_TEXTURE_2D , 0);
		
	}
	
	void shutDown() {
		
		glDeleteTextures(textureID);
		
	}
	
	public String toString() {
		
		return "Texture " + imageInfo.path + ", GL TextureID: " + textureID;
		
	}
	
	public boolean filledOut() {
		
		return initialized;
		
	}
	
	public void activateForNuklear(Consumer<Integer> code) {
		
		activate(0);
		code.accept(textureID);
		deactivate();
		
	}
	
}