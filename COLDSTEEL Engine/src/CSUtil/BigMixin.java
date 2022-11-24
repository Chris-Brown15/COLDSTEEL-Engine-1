package CSUtil;

import static java.lang.System.arraycopy;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.system.MemoryUtil.memGetByte;
import static org.lwjgl.system.MemoryUtil.memGetInt;
import static org.lwjgl.system.MemoryUtil.memPutByte;
import static org.lwjgl.system.MemoryUtil.nmemAlloc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import CS.UserInterface;
import CSUtil.Dialogs.DialogUtils;
import Core.HitBoxSets;
import Core.Quads;
import Core.SpriteSets;
import Core.TemporalExecutor;
import Renderer.Camera;

public abstract class BigMixin {
	
	private static final ExecutorService exe = Executors.newCachedThreadPool();
	
	public static final Future<?> async(Runnable r) {
		
		return exe.submit(r);
		
	}
	
	public static final <T> Future<T> async(Callable<T> callable){
		
		return exe.submit(callable);
		
	}
	
	public static final float[] getFloatArray(){

		float[] returnArray = {
				  // position              	     // color							//UV Coordinates
	             740.0f ,  236.0f , 0.0f ,       0.0f , 0.0f , 0.0f , 1.0f ,		1 , 0 , // bottom right 0
	             540.0f ,  436.0f , 0.0f ,       0.0f , 0.0f , 0.0f , 1.0f ,		0 , 1 , // top left    1
	             740.0f ,  436.0f , 0.0f ,       0.0f , 0.0f , 0.0f , 1.0f ,		1 , 1 , // top right   2
	             540.0f ,  236.0f , 0.0f ,       0.0f , 0.0f , 0.0f , 1.0f ,		0 , 0 , // bottom left  3

		};

		return returnArray;

	}

	public static final float[] getProjectileVertexArray(){

		float[] returnArray = {
				
				  // position              	     // color							//UV Coordinates
	             100.0f , -100.0f , 0.0f ,       0.0f , 0.0f , 1.0f , 1.0f ,		1 , 0 , // bottom right 0
	            -100.0f ,  100.0f , 0.0f ,       0.0f , 0.0f , 1.0f , 1.0f ,		0 , 1 , // top left    1
	             100.0f ,  100.0f , 0.0f ,       0.0f , 0.0f , 1.0f , 1.0f ,		1 , 1 , // top right   2
	            -100.0f , -100.0f , 0.0f ,       0.0f , 0.0f , 1.0f , 1.0f ,		0 , 0 , // bottom left  3

		};

		return returnArray;

	}

	public static final float[] getColliderFloatArray(){

		float[] returnArray = {

	            // position              	     // color				 		//UV Coordinates
				740.0f ,  236.0f , 0.0f ,       1.0f , 1.0f , 1.0f,	1.0f ,		1 , 0 , // bottom right 0
				540.0f ,  436.0f , 0.0f ,       1.0f , 1.0f , 1.0f, 1.0f ,		0 , 1 , // top left    1
				740.0f ,  436.0f , 0.0f ,       1.0f , 1.0f , 1.0f,	1.0f ,		1 , 1 , // top right   2
				540.0f ,  236.0f , 0.0f ,       1.0f , 1.0f , 1.0f, 1.0f ,		0 , 0 , // bottom left  3

		};

		return returnArray;

	}
	
	public static final float[] getJointFloatArray() {

		float[] returnArray = {

	            // position              	     // color				 		//UV Coordinates
				740.0f ,  236.0f , 0.0f ,       0.0f , 1.0f , 0.0f,	1.0f ,		1 , 0 , // bottom right 0
				540.0f ,  436.0f , 0.0f ,       0.0f , 1.0f , 0.0f, 1.0f ,		0 , 1 , // top left    1
				740.0f ,  436.0f , 0.0f ,       0.0f , 1.0f , 0.0f,	1.0f ,		1 , 1 , // top right   2
				540.0f ,  236.0f , 0.0f ,       0.0f , 1.0f , 0.0f, 1.0f ,		0 , 0 , // bottom left  3

		};

		return returnArray;
		
	}
	
	public static final float[] getHitBoxArray() {
		
		float[] returnArray = {

	            // position              	     // color				 		//UV Coordinates
				370.0f ,  118.0f , 0.0f ,       1.0f , 1.0f , 1.0f,	1.0f ,		1 , 0 , // bottom right 0
				270.0f ,  218.0f , 0.0f ,       1.0f , 1.0f , 1.0f, 1.0f ,		0 , 1 , // top left    1
				370.0f ,  218.0f , 0.0f ,       1.0f , 1.0f , 1.0f,	1.0f ,		1 , 1 , // top right   2
				270.0f ,  118.0f , 0.0f ,       1.0f , 1.0f , 1.0f, 1.0f ,		0 , 0 , // bottom left  3

		};

		returnArray = changeColorTo(returnArray , 1.0f , 0.0f , 0.0f);
		returnArray = makeTranslucent(returnArray , 0.15f);
		
		return returnArray;
		
	}

	public static final float[] getStaticFloatArray(){

		float[] returnArray = {

	            // position              	     // color				 		//UV Coordinates
				740.0f ,  236.0f , 0.0f ,       1.0f , 0.0f , 1.0f,	1.0f ,		1 , 0 , // bottom right 0
				540.0f ,  436.0f , 0.0f ,       1.0f , 0.0f , 1.0f, 1.0f ,		0 , 1 , // top left    1
				740.0f ,  436.0f , 0.0f ,       1.0f , 0.0f , 1.0f,	1.0f ,		1 , 1 , // top right   2
				540.0f ,  236.0f , 0.0f ,       1.0f , 0.0f , 1.0f, 1.0f ,		0 , 0 , // bottom left  3

		};

		return returnArray;

	}

	public static final float[] getTriggerConditionFloatArray(){

		float[] returnArray = {

	            // position              	     // color				 		//UV Coordinates
				740.0f ,  236.0f , 0.0f ,       1.0f , 0.0f , 1.0f , 1.0f ,		1 , 0 , // bottom right 0
				540.0f ,  436.0f , 0.0f ,       1.0f , 0.0f , 1.0f , 1.0f ,		0 , 1 , // top left    1
				740.0f ,  436.0f , 0.0f ,       1.0f , 0.0f , 1.0f , 1.0f ,		1 , 1 , // top right   2
				540.0f ,  236.0f , 0.0f ,       1.0f , 0.0f , 1.0f , 1.0f ,		0 , 0 , // bottom left  3

		};

		return returnArray;

	}

	public static final float[] getTriggerEffectFloatArray(){

		float[] returnArray = {

	            // position              	     // color				 		//UV Coordinates
				740.0f ,  236.0f , 0.0f ,       0.0f , 1.0f , 1.0f , 1.0f ,		1 , 0 , // bottom right 0
				540.0f ,  436.0f , 0.0f ,       0.0f , 1.0f , 1.0f , 1.0f ,		0 , 1 , // top left    1
				740.0f ,  436.0f , 0.0f ,       0.0f , 1.0f , 1.0f , 1.0f ,		1 , 1 , // top right   2
				540.0f ,  236.0f , 0.0f ,       0.0f , 1.0f , 1.0f , 1.0f ,		0 , 0 , // bottom left  3

		};

		return returnArray;

	}

	public static final float[] getEntityFloatArray() {

		float[] vertices = {
		
	        // position              	     // color							//UV Coordinates
	        740.0f ,  236.0f , 0.0f ,       1.0f , 0.0f , 0.0f , 1.0f ,		1 , 0 , // bottom right 0
	        540.0f ,  436.0f , 0.0f ,       0.0f , 1.0f , 0.0f , 1.0f ,		0 , 1 , // top left    1
	        740.0f ,  436.0f , 0.0f ,       0.0f , 0.0f , 1.0f , 1.0f ,		1 , 1 , // top right   2
	        540.0f ,  236.0f , 0.0f ,       1.0f , 1.0f , 1.0f , 1.0f ,		0 , 0 , // bottom left  3

		};
		
		return vertices;
		
	}
	
	public static final float[] getItemFloatArray() {
		
		float[] vertices = {
				
		        // position              	     // color							//UV Coordinates
		        740.0f ,  236.0f , 0.0f ,       1.0f , 1.0f , 0.0f , 1.0f ,		1 , 0 , // bottom right 0
		        540.0f ,  436.0f , 0.0f ,       1.0f , 1.0f , 0.0f , 1.0f ,		0 , 1 , // top left    1
		        740.0f ,  436.0f , 0.0f ,       1.0f , 1.0f , 0.0f , 1.0f ,		1 , 1 , // top right   2
		        540.0f ,  236.0f , 0.0f ,       1.0f , 1.0f , 0.0f , 1.0f ,		0 , 0 , // bottom left  3

			};
		
		return vertices;
		
	}
	
	public static final String read(ByteBuffer text){

		return org.lwjgl.system.MemoryUtil.memASCII(text);

	}
	
	public static final String read(ByteBuffer text , int numberChars) {
		
		return read(text.position(0).slice());
		
	}

	public static final ByteBuffer returnText(ByteBuffer text){

		for(int i = 0 ; i < text.capacity() ; i++){

			if(text.get(i) != 0) text.put(i, (byte) 0);
			else if (text.get(i) == 0) continue;

		}

		return text;

	}

	public static final ByteBuffer stringToAscii(String input) {
	
		return org.lwjgl.system.MemoryUtil.memASCII(input);
		
	}	

	public static final float getLowerTriangleArea(float[] triangle){

		if(triangle != null){

			//Vertex A = bottom left, B = top left , C = top right
			float Ax , Ay , Bx , By , Cx , Cy;
			Ax = triangle[27];
			Ay = triangle[28];
			Bx = triangle[9];
			By = triangle[10];
			Cx = triangle[18];
			Cy = triangle[19];

			return Math.abs((Ax * (By - Cy)) + (Bx * (Cy - Ay)) + (Cx * (Ay - By))) / 2;

		} else return 0f;

	}

	public static final float getTriangleArea(float[] triangle){

		float returnArea = 0;

		if(triangle != null){

			//let A be the bottom left point
			//let B be the top left point
			//let C be the right point
			float AsubX = triangle[27];
			float AsubY = triangle[28];
			float BsubX = triangle[9];
			float BsubY = triangle[10];
			float CsubX = triangle[0];
			float CsubY = triangle[1];

			return returnArea = Math.abs(((AsubX * (BsubY - CsubY)) + (BsubX * (CsubY - AsubY)) + (CsubX * (AsubY - BsubY))) / 2);


		} else return returnArea;

	}

	/**
	 * Computes the area of the triangle's vertices by the coordinate area formula for a triangle.
	 * 
	 * @param Ax — x coordinate of any vertex A
	 * @param Ay — y coordiante of any vertex A
	 * @param Bx — x coordiante of any vertex B
	 * @param By — y coordiante of any vertex B
	 * @param Cx — x coordinate of any vertex C
	 * @param Cy — y coordinate of any vertex C
	 * @return area of triangle bound by A, B, and C.
	 */
	public static final float getTrianglePointArea(float Ax , float Ay , float Bx , float By , float Cx , float Cy){

		return Math.abs(((Ax * (By - Cy)) + (Bx * (Cy - Ay)) + (Cx * (Ay - By))) / 2);

	}

	/**
	 * Computes the coordinate area formula for a triangle sans dividing numberator by 2, resulting in the area of a rectangle. 
	 * 
	 * @param Ax — x coordinate of any vertex A
	 * @param Ay — y coordiante of any vertex A
	 * @param Bx — x coordiante of any vertex B
	 * @param By — y coordiante of any vertex B
	 * @param Cx — x coordinate of any vertex C
	 * @param Cy — y coordinate of any vertex C
	 * @return area of the rectanle bound by at least A, B, and C. 
	 */
	public static final float getCoordinateRectangleArea(float Ax , float Ay , float Bx , float By , float Cx , float Cy){

		return Math.abs((Ax * (By - Cy)) + (Bx * (Cy - Ay)) + (Cx * (Ay - By)));

	}
	
	/**
	 * Converts an x coordinate in screen space to normalized device coordinate space.
	 * 
	 * @param xCoord — x coordinate in screen space
	 * @param width — framebuffer width
	 * @param height — framebuffer height
	 * @return double representation of xCoord in normalized device coordinate space
	 */
    public static final double getSCtoNDCForX (double xCoord , int width , int  height) {

    	return ((xCoord / width) *2) -1;

    }

    /**
     * Converts a y coordinate in screen space to normalized device coordinate space.
     * 
     * @param yCoord — y coordinate in screen space
     * @param width — framebuffer width
     * @param height — framebuffer height
     * @return double representing yCoord in normalized device coordinate space
     */
    public static final double getSCtoNDCForY (double yCoord , int width , int  height) {

    	return -(((yCoord / height) *2) -1);

    }

    /**
     * Converts a normalized device coordinate(such as those given by {@code glfwGetCursorPos()} into world coordinates.
     * This specific method is for x coordinates of 2D vectors.
     * 
     * @param xCoord — normalized device coordinate
     * @param width — screen width
     * @param height — screen height
     * @param camera — renderer's camera
     * @return double representing xCoord as a world coordinate.
     */
    public static final double getSCToWCForX(double xCoord , int width , int  height , Camera camera) {

    	//mouse x coord
    	double convertedNDCX =  ((xCoord / width) *2) -1;
    	Vector4f mouseXCoord = new Vector4f((float)(convertedNDCX) , 0.0f , 0.0f , 1.0f);
    	mouseXCoord.mul(camera.inverseProjectionMatrix).mul(camera.inverseViewMatrix);

    	return mouseXCoord.x;

    }

    /**
     * Converts an x coordinate in screen space to world space, taking into account parallax offsets.
     * 
     * @param xCoord — screen space x coordinate
     * @param width — framebuffer width
     * @param height — framebuffer height
     * @param camera — renderer's camera
     * @param parallaxOffsetX — x value of a parallax offset
     * @param parallaxOffsetY — y value of a parallax offset
     * @return double representation of xCoord in world space
     */
    public static final double getSCToWCForX(double xCoord , int width , int  height , Camera camera , float parallaxOffsetX , float parallaxOffsetY) {

    	//mouse x coord
    	double convertedNDCX =  ((xCoord / width) *2) -1;
    	Vector4f mouseXCoord = new Vector4f((float)(convertedNDCX) , 0.0f , 0.0f , 1.0f);
    	mouseXCoord.mul(camera.inverseProjectionMatrix).mul(camera.getInverseViewMatrix(parallaxOffsetX, parallaxOffsetY));

    	return mouseXCoord.x;

    }

    /**
     *  Converts a y coordinate in screen space to world space.
     * 
     * @param yCoord — y coordinate in screen space
     * @param width — framebuffer width
     * @param height — framebuffer height
     * @param camera — renderer's camera
     * @return double representing yCoord in world space
     */
    public static final double getSCToWCForY (double yCoord , int width , int  height , Camera camera) {

    	//y coord
    	double convertedNDCY = -(((yCoord / height) *2) -1);
    	Vector4f mouseYCoord = new Vector4f(0.0f , (float)(convertedNDCY) , 0.0f , 1.0f);
    	mouseYCoord.mul(camera.inverseProjectionMatrix).mul(camera.inverseViewMatrix);

    	return mouseYCoord.y;
    }

    /**
     * This method converts a y coordinate in screen space to world space, taking into account a perspective shift due to parallax.
     * 
     * @param yCoord — screen space y coordinate 
     * @param width — framebuffer width
     * @param height — framebuffer height
     * @param camera — renderer's camera
     * @param parallaxOffsetX — x value of parallax offset 
     * @param parallaxOffsetY — y value of parallax offset
     * @return double representing yCoord in world space, taking into account the given parallax.
     */
    public static final double getSCToWCForY (double yCoord , int width , int  height , Camera camera , float parallaxOffsetX , float parallaxOffsetY) {

    	//y coord
    	double convertedNDCY = -(((yCoord / height) *2) -1);
    	Vector4f mouseYCoord = new Vector4f(0.0f , (float)(convertedNDCY) , 0.0f , 1.0f);
    	mouseYCoord.mul(camera.inverseProjectionMatrix).mul(camera.getInverseViewMatrix(parallaxOffsetX , parallaxOffsetY));

    	return mouseYCoord.y;
    }

	public static final void queryForError() {

		System.err.println(glGetError());

	}

	/**
	 * Converts a coordinate in normalized device coordinate space to world space. This specifically takes an X coordinate.
	 * 
	 * @param xCoord — Normalized Device Coordinate to convert to world coordinates.
	 * @param width — width of the framebuffer
	 * @param height — height of the framebuffer
	 * @param camera — the renderer's camera
	 * @return the x value, in world space
	 */
    public static final float getNDCToWCForX(float xCoord , int width , int  height , Camera camera) {

    	//mouse x coord
    	double convertedNDCX =  ((xCoord / width) *2) -1;
    	Vector4f mouseXCoord = new Vector4f((float)(convertedNDCX) , 0.0f , 0.0f , 1.0f);
    	mouseXCoord.mul(camera.inverseProjectionMatrix).mul(camera.inverseViewMatrix);

    	return mouseXCoord.x;

    }

    /**
     * 
     * Converts a coordinate in normalized device coordinate space to world space. This specifically is for Y coordinates.
     * 
     * @param yCoord — Normalized Device Coordinate to convert to world coordinates.
	 * @param width — width of the framebuffer
	 * @param height — height of the framebuffer
	 * @param camera — the renderer's camera
     * @return the y value, in world space
     */
    public static final float getNDCToWCForY (float yCoord , int width , int  height , Camera camera) {

    	//y coord
    	double convertedNDCY = -(((yCoord / height) *2) -1);
    	Vector4f mouseYCoord = new Vector4f(0.0f , (float)(convertedNDCY) , 0.0f , 1.0f);
    	mouseYCoord.mul(camera.inverseProjectionMatrix).mul(camera.inverseViewMatrix);

    	return mouseYCoord.y;
    	
    }

    /**
     * 
     * In C, bytes can be signed or unsigned. If they are unsigned, their values can be anything from 0 to 255. 
     * In Java there are no unsigned types. This converts a C unsigned byte to a positive float. Its used to convert values returned
     * by C functions such as the {@code tinyfd_colorChooser} function which returns a byte which might not be a valid value.
     * 
     * @param colorBuffer — buffer color values are stored in
     * @param index — 0 for Red , 1 for green , 2 for blue
     * @return
     */
    public static final float byteToRGB(ByteBuffer colorBuffer , int index) {

    	/*
    	 * RGB values cannot be greater than 255
    	 *
    	 * Java bytes greater than 127 are negative bytes
    	 * as an RGB value gets larger, and is greater than 127, it returns a smaller and smaller negative number,
    	 * that is to say, a greater-in-value number.
    	 * for example an RGB value of 130 returns a byte of -126
    	 */

    	Byte myByte = colorBuffer.get(index);

    	float returnFloat = myByte.intValue();

    	if (returnFloat < 0){

    		float delta127 = -128 - returnFloat;
    		returnFloat = returnFloat * -1;
    		float to127 = 128 - returnFloat;
        	returnFloat += to127;
        	returnFloat += (delta127) * -1;

    	}

    	return returnFloat;

    }

    public static final float RGBToNormalized(float RGB) {

    	float returnFloat = RGB / 255;

    	return returnFloat;

    }

	public static final String[] vector3ToString(Vector3f toString){

		String[] returnString = new String[3];

		returnString[0] = Float.toString(toString.x);
		returnString[1] = Float.toString(toString.y);
		returnString[2] = Float.toString(toString.z);

		return returnString;

	}

	public static final boolean areColliding(float[] subjectOne , float[] subjectTwo) {
		
		if(subjectOne[28] < subjectTwo[19] && !(subjectOne[10] < subjectTwo[28])){//is target in range to collide

			if(subjectOne[27] < subjectTwo[18] ){//is target colliding

				int XArea = (int) getCoordinateRectangleArea(subjectTwo[27] , subjectTwo[28] , subjectTwo[18] , subjectTwo[19] , subjectTwo[0] , subjectTwo[1]);
				int targetArea = (int) getCoordinateRectangleArea(subjectOne[0] , subjectOne[1] , subjectTwo[18] , subjectTwo[19] , subjectTwo[0] , subjectTwo[1]);
				return XArea > targetArea ? true:false; //only works for rectangles

			}

		}
		
		return false;
		
	}
		
    public static final ByteBuffer loadTTF(String resource) throws IOException{

        ByteBuffer buffer;
        Path path = Paths.get(resource);
        byte[] file = Files.readAllBytes(path);
        buffer = BufferUtils.createByteBuffer(file.length);
        buffer.put(file).flip();
        return buffer;

    }

    /**
     * Takes a float array representing vertex data, and sets its opacity.
     * 
     * @param target — array whose opacity is to be modified
     * @param opacity — percentage opacity to apply to target's vertices
     * @return target array, post opacity operation 
     */
    public static final float[] makeTranslucent(float[] target , float opacity) {
    	
    	target[6] = opacity;
    	target[15] = opacity;
    	target[24] = opacity;
    	target[33] = opacity;

    	return target;
    	
    }
    
    /**
     * Takes a float array representing vertex data, and colors all it's vertices to this rgb value.
     * 
     * @param target — array whose color is to be modified
     * @param r — red value for vertices to be set to
     * @param g — green value for vertices to be set to
     * @param b — blue value for vertices to be set to
     * @return target float array, post coloring operations
     */
    public static final float[] changeColorTo(float[] target , float r , float g , float b) {
    	
    	target[30] = r;
    	target[31] = g;
    	target[32] = b;

    	target[3] = r;
    	target[4] = g;
    	target[5] = b;

    	target[12] = r;
    	target[13] = g;
    	target[14] = b;

    	target[21] = r;
    	target[22] = g;
    	target[23] = b;
    	
    	return target;
    	
    }
    
    /**
     * Gets the exact coordinates of this object's midpoint.
     * 
     * @param target — float array whose midpoint is queried
     * @return float array containing the x coordinate of the midpoint , then the y coordinate of the midpoint. Laid out as {x , y}
     */
    public static final float[] getArrayMidpoint(float[] target) {
    	
    	float xMid = (target[0] - target[27]) / 2;
    	float yMid = (target[10] - target[28]) / 2;
    	
    	return new float[] {xMid + target[27], yMid + target[28]};
    	
    }

    /**
     * Gets the exact coordinates of this object's X midpoint.
     * 
     * @param target — float array whose midpoint is queried
     * @return float array containing the x coordinate of the midpoint , then the y coordinate of the midpoint. Laid out as {x , y}
     */
    public static final float arrayXMid(float[] target) {
    	
    	return (target[0] - target[27]) / 2;
    	
    	
    }
    
    public static final float arrayYMid(float[] target) {
    	
    	return (target[10] - target[28]) / 2;
    	
    }
    
    /**
     * Moves target array's position values according to xSpeed and ySpeed with respect to delta time.
     * 
     * @param target — array to move 
     * @param xSpeed — amount to move target along the horizontal axis
     * @param ySpeed — amount to move target along the vertical axis
     * @return target array, post move operations
     */
    public static final float[] translateArray(float[] target , float xSpeed , float ySpeed) {
    	
    	target[0] += xSpeed; 
		target[9] += xSpeed; 
		target[18] += xSpeed;
		target[27] += xSpeed;
		
		target[1] += ySpeed; 	
		target[10] += ySpeed;
		target[19] += ySpeed;
		target[28] += ySpeed;
		
		return target;
    	
    }

    /**
     * Creates and returns a string representation of this array, which itself should represent vertex data
     * 
     * @param target — array to model as string
     * @return — String representation of vertex data
     */
    public static final String VArrayToString(float[] target) {
    	
    	String arr = "";
    	
    	for(int i = 0  , k = 1 ; i < target.length ; i++ , k++) {
    		
    		arr += target[i] + " , ";
    		if(k % 9 == 0) arr += "\n";
    		    		
    	}
    	
    	return arr;
    	
    }
    
    /**
     * Adds factor to target vertex array's width. Target's dimensions will change based on factor. 
     * 
     * @param target — float array representing vertex data
     * @param factor — amount of pixels to change each this object's width
     * @return — target array, post resize operations
     */
    public static final float[] modArrayWidth(float[] target , float factor) {
    	
    	if(((target[0] - target[27]) - factor > 2) && factor < 0) {
    		
    		target[9] -= factor;
    		target[27] -= factor;
    		target[0] += factor;
    		target[18] += factor;    		
    		
    	} else if (factor > 0) {
    		
    		target[9] -= factor;
    		target[27] -= factor;
    		target[0] += factor;
    		target[18] += factor;    		
    		
    		
    	}
    	
    	return target;
    	
    }
    
    /**
     * Adds factor to target vertex array's height. target's height will change based on factor.
     * 
     * @param target — float array representing vertex data
     * @param factor — amount of pixels to change each this object's height
     * @return — target array, post resize operations
     *
     */
    public static final float[] modArrayHeight(float [] target , float factor) {
    	
    	if(((target[10] - target[28]) - factor > 2) && factor < 0) {
    		
    		target[10] += factor;
    		target[19] += factor;
    		
    	} else if (factor > 0) {
    		
    		target[10] += factor;
    		target[19] += factor;
    		
    	}
    	
    	return target;
    	
    }
    
    /**
     * Takes a vertex array and returns the width of the quad it represents.
     * 
     * @param target — vertex array
     * @return width
     */
    public static final float getArrayWidth(float[] target) {
    	
    	return target[0] - target[27];
    	
    }
 
    /**
     * Takes a vertex array and returns the height of the quad it represents.
     * 
     * @param target — vertex array
     * @return height
     */
    public static final float getArrayHeight(float[] target) {
    	
    	return target[10] - target[28];
    	
    }
 
    /**
     * Given an array of sprite sets, resets all those at an index specified with indices.
     * 
     * @param animations — array of sprite set animations
     * @param indices — indices of sprite sets to reset
     * @return animations SpriteSets array, post reset operations
     */
    public static final SpriteSets[] resetSpriteSets(SpriteSets[] animations , int...indices) {
    	
    	for(int x : indices) {
    		
    		animations[x].setCurrentSprite(0);
    		animations[x].lastSprite = false;
    		animations[x].freeze = false;
    		animations[x].reverse(false);
    		
    	}
    	
    	return animations;
    	
    }
    
    public static final float[] setArrayWidth(float[] target , float width) {
    	
    	target[0] = target[27] + width;
    	target[18] = target[27] + width;
    	return target;
    	
    }
    
    public static final float[] setArrayHeight(float[] target , float height) {
    	
    	target[10] = target[1] + height;
    	target[19] = target[1] + height;
    	return target;
    	
    }
    
	/**
	 * Checks that the cursor is nearer to the bottom right vertex of this Selection Area, and moves its appropriate vertices to the positino
	 * of the cursor.
	 * @param vertices — vertex array whose elements are to be resized
	 * @param cursorX — position of the x coordinate of the cursor, should be in world coordinates 
	 * @param cursorY — position of the y coordinate of the cursor, should be in world coordinates
	 * 
	 * @return vertices array, post resize operations
	 */
	public static final float[] dragResize(float[] vertices , float cursorX , float cursorY) {
		
		if(Math.abs(cursorX - vertices[0]) < Math.abs(cursorX - vertices[9]) && Math.abs(cursorX - vertices[0]) < 100
		&& Math.abs(vertices[1] - cursorY) < Math.abs(vertices[10] - cursorY) && Math.abs(vertices[1] - cursorY) < 100)
		{
			
			vertices[0] = cursorX;
			vertices[1] = cursorY;
			vertices[28] =  cursorY;
			vertices[18] = cursorX;
			
		} else if(Math.abs(vertices[9] - cursorX) < Math.abs(cursorX - vertices[0]) && Math.abs(cursorX - vertices[9]) < 100
			    &&Math.abs(cursorY - vertices[10]) < Math.abs(cursorY - vertices[1]) && Math.abs(cursorY - vertices[10]) < 100) {
			
			vertices[9] = cursorX;
			vertices[10] = cursorY;
			vertices[27] = cursorX; 
			vertices[19] = cursorY;
						
		}
				
		return vertices;
		
	}	
    
	public static final HitBoxSets[] resizeHitBoxSets(HitBoxSets[] hitboxes , int additionalSpace) {		
		
		HitBoxSets[] newArray = new HitBoxSets[hitboxes.length + additionalSpace];
		arraycopy(hitboxes , 0 , newArray , 0 , hitboxes.length);
		return newArray;		
		
	}
	
	public static final HitBoxSets[] keepIf(HitBoxSets[] arr , Predicate<HitBoxSets> test){
		
		int numberNulls = 0;
		
		for(int i = 0 ; i < arr.length ; i++) if(!test.test(arr[i])) {
			
			arr[i] = null;
			numberNulls++;
			
		}
	
		HitBoxSets[] newArr = new HitBoxSets[arr.length - numberNulls];
		for(int i = 0 , k = 0 ; i < arr.length ; i++) if(arr[i] != null) {
				
			newArr[k] = arr[i];
			k++;
			
		}
			
		return newArr;
		
	}

	public static final void resetSpriteSetsExcept(SpriteSets[] sets , int notThis) {
		
		for(int i = 0 ; i < sets.length ; i++) {

			if(sets[i] == null) return; 
			if(i != notThis)continue;
			
			sets[i].setCurrentSprite(0);
			sets[i].lastSprite = false;
			sets[i].freeze = false;
		}
    	
	}
	
	public static final float[] moveTo(Quads E , float[] target) {
		
		float[] Emid = E.getMidpoint();
		float[] targetMid = getArrayMidpoint(target);
		return translateArray(target , Emid[0] - targetMid[0] , Emid[1] - targetMid[1]);
		
	}
	
	public static final void moveTo(float[] target , float x , float y) {
		
		float[] targetMid = getArrayMidpoint(target);
		translateArray(target , x - targetMid[0] , y - targetMid[1]);
		
	}
	
	public static final void moveTo(Quads destination , Quads target) {
		
		float[] dest = destination.getMidpoint();
		float[] targetMid = target.getMidpoint();
		target.translate(dest[0] - targetMid[0] , dest[1] - targetMid[1]);
		
	}
	
	/**
	 * Returns the filename of abspath, as well as whatever directory is holding it.
	 * 
	 * @param absPath — any path
	 * @return a filepath containing the name of the file pointed to by absPath, as well as whatever directory it is contained within.
	 */
	public static final CharSequence toLocalDirectory(CharSequence absPath) {
		
		//returns something like data/macrolevels/mymacrolevel/level1.CStf
		int numberBackSlashes = 0 , i = absPath.length() - 1;
		for(; i >= 0 && numberBackSlashes < 2 ; i --) if(absPath.charAt(i) == '\\' || absPath.charAt(i) == '/') numberBackSlashes++;
		return absPath.subSequence(i + 2, absPath.length());		
		
	}
	
	public static final CharSequence toLocalPath(CharSequence absPath) {

		CharSequence subsqn = absPath;
		
		for(int i = 0 ; i < absPath.length() - 16 ; i ++) if(absPath.subSequence(i, i + 16).equals("COLDSTEEL Engine"))	{
			
			subsqn = absPath.subSequence(i + 17, absPath.length());
			break;
				
		}
				
		for(int i = 0 ; i < subsqn.length() ; i++) if(subsqn.charAt(i) == '/' || subsqn.charAt(i) == '\\') {
			
			subsqn = subsqn.subSequence(0, i) + "/" + subsqn.subSequence(i + 1, subsqn.length());
			
		}
		
		return subsqn;		
		
	}
	
	public static final String toNamePath(String path) {
		
		byte[] bytes = path.getBytes();		
		for(int i = bytes.length -1 ; i >= 0 ; i--) if(bytes[i] == '/' || bytes[i] == '\\') {
			
			return path.substring(i + 1, path.length());
			
		}
		
		return path;
		
	}
	
	public static final boolean isLocalpath(String path) {
		
		return path.contains("COLDSTEEL Engine");
		
	}
	
	public static final boolean isLocalPath(CharSequence path) {
		
		for(int i = 0 ; i < path.length() -16 ; i++) if(path.subSequence(i, i + 16).equals("COLDSTEEL Engine")) return false;		
		return true;
		
	}
	
	public static final byte toByte(boolean result) {
		
		return result ? (byte)1 : (byte)0;
		
	}
	
	public static final boolean toggle(boolean toggleThis) {
		
		toggleThis = toggleThis ? false:true;
		return toggleThis;
		
	}
	
	public static final void toggle(ByteBuffer toggleThis) {
		
		toggleThis.put(0 , toggleThis.get(0) == 1 ? (byte) 0 : (byte) 1);
		
	}
	
	/**
	 * Returns 0 if b is 1, else returns 1
	 * 
	 * @param b — a byte
	 * @return the negation of b
	 */
	public static final byte toggle(byte b) {
		
		return (byte) (b == 1 ? 0:1);
		
	}
	
	public static final ByteBuffer put(ByteBuffer buffer , byte val) {
		
		return buffer.put(0 , val);
		
	}

	public static final ByteBuffer put(ByteBuffer buffer , boolean val) {
		
		return buffer.put(0 , toByte(val));
		
	}
	
	public static final FloatBuffer put(FloatBuffer buffer , float value) {
		
		return buffer.put(0 , value);
		
	}

	public static final ByteBuffer alloc1(MemoryStack src) {
		
		return src.bytes((byte)1);
		
	}
	
	/**
	 * Returns a ByteBuffer allocated on {@code src} with an initial single value of 0.
	 * 
	 * @param src — an instance of {@code MemoryStack}
	 * @return a {@code java.nio.ByteBuffer} with a capacity of 1, with 0 in its place.
	 */
	public static final ByteBuffer alloc0(MemoryStack src) {
		
		return src.bytes((byte) 0);
		
	}
	
	public static final ByteBuffer[] alloc0Array(int size , MemoryStack allocator) {
		
		ByteBuffer[] bufferBuffer = new ByteBuffer[size];
		for(int i = 0 ; i < size ; i ++) bufferBuffer[i] = alloc0(allocator);
		return bufferBuffer;
		
	}
	
	public static final ByteBuffer allocN(MemoryStack src , byte val) {
		
		return src.bytes(val);
		
	}

	public static final byte dr(long address , int byteOffset) {
		
		return (byte) memGetInt(address + byteOffset);
		
	}
	
	public static final byte dr(long address) {
		
		return memGetByte(address);
		
	}
	
	public static final short dr(ShortBuffer buff){
	
		return buff.get(0);
		
	}
	
	public static final int dr(IntBuffer buff){
		
		return buff.get(0);
		
	}
	
	public static final float dr(FloatBuffer ptr) {
		
		return ptr.get(0);
		
	}
	
	public static final void dr_Byte(long address , int byteOffset , int value) {
		
		memPutByte(address + byteOffset , (byte)value);
		
	}
	
	public static final boolean toBool(ByteBuffer ptr) {
	
		return ptr.get(0) == 1;
		
	}

	public static final boolean toBool(byte b) {
	
		return b == 1;
		
	}
	
	public static float[] snapDataToPixels(float[] data) {

    	data[27] = (float)Math.floor(data[27]);
      	data[28] = (float)Math.floor(data[28]);

      	data[0] = (float)Math.floor(data[0]);
      	data[1] = (float)Math.floor(data[1]);

      	data[9] = (float)Math.floor(data[9]);
      	data[10] = (float)Math.floor(data[10]);

      	data[18] = (float)Math.floor(data[18]);
      	data[19] = (float)Math.floor(data[19]);

      	return data;
      	
    }
		
	public static final long pointer(CharSequence text , MemoryStack allocator) {
		
		int[] chars = text.codePoints().toArray();
		long address = allocator.nmalloc(chars.length);
		for(int i = 0 ; i < chars.length ; i ++) memPutByte(address + i , (byte)chars[i]);
		return address;
		
	}
	
	public static final long pointer(CharSequence text) {
		
		int[] chars = text.codePoints().toArray();
		long address = nmemAlloc(chars.length);
		for(int i = 0 ; i < chars.length ; i ++) memPutByte(address + i , (byte)chars[i]);
		return address;
		
	}

    public static final boolean selectQuad(float[] vertexData , float xCoord , float yCoord) {
    	
    	if (((xCoord >= vertexData[27]) && (vertexData[18] >= xCoord)) &&
    		((yCoord >= vertexData[1]) && (vertexData[10] >= yCoord))) return true;
        		
        return false;
        	    	
    }
    
    /**
     * Creates and returns a view of this sprite's joints
     * 
     * @param sprite — a float array representing a sprite in an animation
     * @return — a float array holding only joints and their offsets
     */
    public static final float[] getJoints(float[] sprite) {
    	
    	boolean activatesHitBox = sprite.length % 3 != 0 ;
    	float[] joints = new float[activatesHitBox ? sprite.length - 7 : sprite.length - 6];
    	for(int i = 0 ; i < joints.length ; i ++) joints[i] = sprite[i + 6];
    	    	
    	/*
    	 
    	 [lu , ru , tv , bv , w , h , j1 , j1x , j1y , j2 , j2x , j2y , hb] length 13
    	  
    	  activates hitbox
    	  joints = new float[13 - 7 = 6] 
    	  joints[0] = joints[6]
    	  joints[1] = joints[7]
    	  joints[2] = joints[8]
    	  
    	 */
    	
    	return joints;
    	
    }
    
    public static final void offloadFloatAssignment(String title , Consumer<Float> assignmentFunction) {
    	
    	Supplier<String> input = DialogUtils.newInputBox(title, 5 , 270 , UserInterface.NUMBER_FILTER);
    	TemporalExecutor.onTrue(() -> input.get() != null , () -> assignmentFunction.accept(Float.parseFloat(input.get())));
    	
    }
       
    public static final String arrayToString(float[] array) {
    	
    	String line = "";
    	for(int i = 0 ; i < array.length - 1; i ++) line += array[i] + ",";
    	line += array[array.length - 1];
    	return line;
    	
    }
    
    public static final float[] parseSpecsString(String specsString) {
    	
    	String[] split = specsString.split(",");    	
    	return new float[] {Float.parseFloat(split[0]) , Float.parseFloat(split[1]) , Float.parseFloat(split[2]) , Float.parseFloat(split[3])};
    	
    }

    public static final ByteBuffer toByte(MemoryStack allocator , Boolean state) {
    	
    	return allocator.bytes(state != null && state ? (byte)1 : (byte)0);
    	
    }
    
    public static final ByteBuffer toByteNegate(MemoryStack allocator , Boolean state) {
    	
    	if(state == null) return allocator.bytes((byte) 0);
    	else return allocator.bytes(!state ? (byte)1 : (byte)0);
    	
    }

    public static final String capitalize(String source) {    	
    	
		return source.substring(0 , 1).toUpperCase() + source.substring(1);
    	
    }
    
    public static final boolean isOnScreen(float[] vertexData) {
    	
    	return true;
    	
    }
    
    /**
     * Utility method for reducing space in places with many try catch blocks. This will call some code via callback within
     * a try-catch block, and if an exception occurs, the stack trace is printed and the program exits.
     *  
     * @param code — code which can throw an exception
     */
    public static final void TRY(DangerCode code) {
    	
    	try {
    		
    		code.call();
    		
    	} catch(Exception e) {
    		
    		e.printStackTrace();
    		System.exit(-1);
    		
    	}
    	
    }
    
    public static final void asyncShutDown() {
    	
    	exe.shutdownNow();
    	
    }
     
}