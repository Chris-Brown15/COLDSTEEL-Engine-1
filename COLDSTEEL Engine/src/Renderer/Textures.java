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
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;

import org.lwjgl.system.MemoryStack;

import CSUtil.DataStructures.Tuple4;

public class Textures {

	private int textureID;
	private ByteBuffer data;	
	private boolean initialized = false;
	String filepath;
	int width;
	int height;
	int bitsPerPixel;
	
	private Function<Integer , Tuple4<String , Integer , Integer , Integer>> onInitialize;
	
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
			
			this.filepath = filepath;
			this.width = widthPtr.get(0);
			this.height = heightPtr.get(0);
			this.bitsPerPixel = bitsPerPixelPtr.get(0);
			
		}
			
		textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D , textureID);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D , 0 , GL_RGB8 , width , height , 0 , GL_RGBA , GL_UNSIGNED_BYTE , data);
		glBindTexture(GL_TEXTURE_2D , 0);
		stbi_image_free(data);
		
		initialized = true;
		
	}
		
	void initialize() {

		textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID);
		if(onInitialize != null) { 
			
			var info = onInitialize.apply(textureID);
			filepath = info.getFirst();
			width = info.getSecond();
			height = info.getThird();
			bitsPerPixel = info.getFourth();
			
		}
		
		glBindTexture(GL_TEXTURE_2D , 0);		
		
	}
	
	public void onInitialize(Function<Integer , Tuple4<String , Integer , Integer , Integer>> initializeCallback) {
		
		this.onInitialize = (ID) -> initializeCallback.apply(ID);
				
	}
		
	public int textureID() {
		
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
	
	public int width() {
		
		return width;
				
	}

	public int height() {
		
		return height;
				
	}

	public int bitsPerPixel() {
		
		return bitsPerPixel;
				
	}

	public String filepath() {
	
		return filepath;
		
	}	
	
	public String toString() {
		
		return "Texture " + filepath + ", GL TextureID: " + textureID;
		
	}
	
	public boolean filledOut() {
		
		return initialized;
		
	}
		
	/**
	 * Make {@code this} identical in its fields to {@code filledOutTexture}
	 * 
	 * @param filledOutTexture — a previously filled out texture
	 */
	void viewOf(Textures filledOutTexture) {
	
		assert filledOutTexture.filledOut() : "ERROR, source texture for viewOf() not filled out";

		textureID = filledOutTexture.textureID;
		data = filledOutTexture.data;	
		initialized = filledOutTexture.initialized;
		filepath = filledOutTexture.filepath;
		width = filledOutTexture.width;
		height = filledOutTexture.height;
		bitsPerPixel = filledOutTexture.bitsPerPixel;
		
	}
	
	public void activateForNuklear(Consumer<Integer> code) {
		
		activate(0);
		code.accept(textureID);
		deactivate();
		
	}
	
}