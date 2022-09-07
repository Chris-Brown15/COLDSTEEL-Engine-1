package Renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;


import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.Scanner;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class Shader {

    private int vertexShaderID;
    private int fragmentShaderID;
    public int shaderProgramID;
    public String shaderData;
	String[] externalShaderSplitArray;
	private String vertexShaderSource;
	private String fragmentShaderSource;
	boolean shaderLoaded;

	public String readShaderFromFile() {

		File ExternalShader = new File (CS.COLDSTEEL.assets + "shaders/shader.glsl");

		try {

			Scanner readShader = new Scanner(ExternalShader);

			for (int shaderFileLength = 0 ; shaderFileLength != (int)ExternalShader.length(); shaderFileLength++) {

				if (shaderFileLength == 0) shaderData = "";
				else while (readShader.hasNextLine()) shaderData = shaderData + "\n" + readShader.nextLine();

			}

			readShader.close();
			shaderLoaded = true;

		} catch (FileNotFoundException E) {

			E.printStackTrace();
			shaderLoaded = false;

		}

		return shaderData;

	}

	String[] setExternalShaderSource () {

		if (shaderData.contains("}")) externalShaderSplitArray = shaderData.split("#type fragment" , 0);
		return externalShaderSplitArray;

	}

	String setExternalVertexShader() {

		return vertexShaderSource = externalShaderSplitArray[0];

	}

	String setExternalFragmentShader() {

		return fragmentShaderSource = externalShaderSplitArray[1];

	}

	public void setShaderProgram () {

		readShaderFromFile();

		if (shaderLoaded) {

			setExternalShaderSource();
			setExternalVertexShader();
			setExternalFragmentShader();

		} else assert false : "Failure to load Shader";

	}

	void initializeShader(String vertexShaderSource , String fragmentShaderSource){

		if(vertexShaderSource != null && fragmentShaderSource != null){

			//vertex shader compilation
			vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
			glShaderSource(vertexShaderID , vertexShaderSource);
			glCompileShader(vertexShaderID);

			int success = glGetShaderi(vertexShaderID , GL_COMPILE_STATUS);
			if(success == GL_FALSE){

				int[] vertexparameters = {512};
				int length = glGetShaderi(vertexShaderID , GL_INFO_LOG_LENGTH);
				glGetShaderiv(vertexShaderID , GL_COMPILE_STATUS , vertexparameters);
				System.out.println(glGetShaderInfoLog(vertexShaderID , length));
				assert false : "ERROR IN VERTEX SHADER COMPILATION...";

			}

			//fragment shader compilation
			fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
			glShaderSource(fragmentShaderID , fragmentShaderSource);
			glCompileShader(fragmentShaderID);

			success = glGetShaderi(fragmentShaderID , GL_COMPILE_STATUS);
			if(success == GL_FALSE){

				int [] fragmentparameters = {512};
				int length = glGetShaderi(fragmentShaderID , GL_INFO_LOG_LENGTH);
				glGetShaderiv(fragmentShaderID , GL_COMPILE_STATUS , fragmentparameters);
				System.out.println(glGetShaderInfoLog(fragmentShaderID , length));
				assert false : "ERROR IN FRAGMENT SHADER COMPILATION";

			}

			//shader program compilation
		    shaderProgramID = glCreateProgram();
		    glAttachShader(shaderProgramID, vertexShaderID);
		    glAttachShader(shaderProgramID, fragmentShaderID);
		    glLinkProgram(shaderProgramID);

		    success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
		    if (success == GL_FALSE) {

		        int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
		        System.out.println(glGetProgramInfoLog(shaderProgramID, len));
		        assert false : "\tERROR IN SHADER COMPILATION.";

		    }

		}

	}

	void initializeShader() {

		setShaderProgram();

		// ============================================================
	    // Compile and link shaders
	    // ============================================================

	    // Create and compile the vertex shader
		vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
	    glShaderSource(vertexShaderID, vertexShaderSource);
	    glCompileShader(vertexShaderID);

	    // Check for errors in compilation
	    int success = glGetShaderi(vertexShaderID, GL_COMPILE_STATUS);
	    if (success == GL_FALSE) {

	    	int[] vertexparameters = {512};
	        int length = glGetShaderi(vertexShaderID, GL_INFO_LOG_LENGTH);
	        glGetShaderiv(vertexShaderID , GL_COMPILE_STATUS , vertexparameters);
	        System.out.println(glGetShaderInfoLog(vertexShaderID, length));
	        assert false : "ERROR IN VERTEX SHADER COMPILATION.";

	    }

		// Create and compile the vertex shader
		fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
	    glShaderSource(fragmentShaderID, fragmentShaderSource);
	    glCompileShader(fragmentShaderID);

	    success = glGetShaderi(fragmentShaderID, GL_COMPILE_STATUS);
	    if (success == GL_FALSE) {

	    	int[] fragmentparameters = {512};
	        int length = glGetShaderi(fragmentShaderID, GL_INFO_LOG_LENGTH);
	        glGetShaderiv(fragmentShaderID , GL_COMPILE_STATUS , fragmentparameters);
	        System.out.println(glGetShaderInfoLog(fragmentShaderID, length));
	        assert false : "ERROR IN FRAGMENT SHADER COMPILATION.";
	    }

	    shaderProgramID = glCreateProgram();
	    glAttachShader(shaderProgramID, vertexShaderID);
	    glAttachShader(shaderProgramID, fragmentShaderID);
	    glLinkProgram(shaderProgramID);

	    success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
	    if (success == GL_FALSE) {

	        int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
	        System.out.println(glGetProgramInfoLog(shaderProgramID, len));
	        assert false : "\tERROR IN SHADER COMPILATION.";

	    }

	}

	public void activate(){

		glUseProgram(shaderProgramID);

	}

	//following this method the shader no longer exists
	public void disableShader(){

		glDetachShader(shaderProgramID , vertexShaderID);
		glDetachShader(shaderProgramID , fragmentShaderID);
		glDeleteShader(vertexShaderID);
		glDeleteShader(fragmentShaderID);
		glDeleteProgram(shaderProgramID);


	}

	public void uniformMatrix4 (String uniformName ,Matrix4f mat4) {

		FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
		mat4.get(matBuffer);
		glUniformMatrix4fv(glGetUniformLocation(shaderProgramID , uniformName) , false , matBuffer);

	}

	public void uniformVector3(String uniformName , Vector3f value){

		glUniform3f(glGetUniformLocation(shaderProgramID , uniformName) ,value.x , value.y , value.z);

	}
	

	public void uniformVector2(String uniformName , Vector2f value){

		glUniform2f(glGetUniformLocation(shaderProgramID , uniformName) , value.x , value.y);

	}

	public void uniformInt(String uniformName , int value) {

		int varLocation = glGetUniformLocation(shaderProgramID , uniformName);
		glUniform1i(varLocation, value);

	}

}
