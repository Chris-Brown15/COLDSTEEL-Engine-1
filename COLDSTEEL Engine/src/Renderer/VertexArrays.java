package Renderer;

import static org.lwjgl.opengl.GL44.glGenVertexArrays;
import static org.lwjgl.opengl.GL44.glBindVertexArray;
import static org.lwjgl.opengl.GL44.glGenBuffers;
import static org.lwjgl.opengl.GL44.glBindBuffer;
import static org.lwjgl.opengl.GL44.glBufferData;
import static org.lwjgl.opengl.GL44.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL44.glVertexAttribPointer;
import static org.lwjgl.opengl.GL44.glDeleteBuffers;
import static org.lwjgl.opengl.GL44.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL44.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL44.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL44.GL_FLOAT;
import static org.lwjgl.opengl.GL44.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL44.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL45.glNamedBufferSubData;

public class VertexArrays {

	public static final int VERTEX_SIZE_BYTES = 36;
	
	private int vertexArrayID;
	private int vertexBufferID;
	private int elementBufferID;
	
	public VertexArrays(float[] data) {
		
		vertexArrayID = glGenVertexArrays();
		glBindVertexArray(vertexArrayID);
		
		vertexBufferID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER , vertexBufferID);
		glBufferData(GL_ARRAY_BUFFER , data , GL_DYNAMIC_DRAW);
		
		elementBufferID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER , elementBufferID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER , Renderer.elementArray , GL_STATIC_DRAW);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		
		glVertexAttribPointer(0 , 3 , GL_FLOAT , false , VERTEX_SIZE_BYTES , 0);
		glVertexAttribPointer(1 , 4 , GL_FLOAT , false , VERTEX_SIZE_BYTES , 12);
		glVertexAttribPointer(2 , 2 , GL_FLOAT , false , VERTEX_SIZE_BYTES , 28);
		
	}
	
	public void activate() {
		
		glBindVertexArray(vertexArrayID);
		glBindBuffer(GL_ARRAY_BUFFER , vertexBufferID);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER , elementBufferID);
		
	}
	
	public void buffer(float[] data) {
		
		activate();
		glBufferData(GL_ARRAY_BUFFER , data , GL_DYNAMIC_DRAW);
		deactivate();
		
	}
	
	public void rebufferAll(float[] newData) {
		
		glNamedBufferSubData(vertexBufferID , 0 , newData);
		
	}
	
	public void deactivate() {
		
		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER , 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER , 0);
		
	}
	
	public void resetVertexAttributes() {
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		
		glVertexAttribPointer(0 , 3 , GL_FLOAT , false , VERTEX_SIZE_BYTES , 0);
		glVertexAttribPointer(1 , 4 , GL_FLOAT , false , VERTEX_SIZE_BYTES , 12);
		glVertexAttribPointer(2 , 2 , GL_FLOAT , false , VERTEX_SIZE_BYTES , 28);
		
	}
	
	public void shutDown() {
		
		glDeleteBuffers(vertexBufferID);
		glDeleteBuffers(elementBufferID);
		glDeleteVertexArrays(vertexArrayID);
		
	}

}
