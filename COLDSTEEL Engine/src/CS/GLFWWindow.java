package CS;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memReport;
import static org.lwjgl.system.MemoryUtil.memAllocDouble;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.nuklear.Nuklear.*;
import org.lwjgl.system.*;
import org.lwjgl.system.MemoryUtil.MemoryAllocationReport;

import CSUtil.Dialogs.DialogUtils;
import Editor.CursorState;
import Editor.EditorMode;
import Game.Core.DebugInfo;

import java.nio.IntBuffer;
import java.nio.DoubleBuffer;
import static CSUtil.BigMixin.getSCToWCForX;
import static CSUtil.BigMixin.getSCToWCForY;

public class GLFWWindow {
	
	private RuntimeState state;
    private String title;
    long glfwWindow;
	//window colors:
    float R = 0.15f;
	float G = 0.15f;
	float B = 0.15f;
	float a = 1.0f;

	private Engine engine;
	private NkContext NuklearContext;	
		
	public GLFWWindow() {

        title = "COLDSTEEL";
        R = 0.15f;
		G = 0.15f;
		B = 0.15f;
		a = 1.0f;

    }
	
    void intialize(Engine engine , RuntimeState state){

    	this.engine = engine;
    	this.state = state;
    	
    	System.out.println("Beginning window initialization...");
        // Sets GLFW errors to print to the Java system error output stream, default the standard output, a console
    	GLFWErrorCallback.createPrint(System.err).set();
    	
        // Initialize GLFW
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW.");

        // Configure GLFW
        glfwDefaultWindowHints();

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        //for fullscreen
        glfwWindowHint(GLFW_DECORATED , GLFW_FALSE);
        glfwWindowHint(GLFW_MAXIMIZED , GLFW_TRUE);

        if (Platform.get() == Platform.MACOSX) glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        glfwWindowHint(GLFW_SAMPLES, 8);
        //Create the window
        glfwWindow = glfwCreateWindow(1920, 1080, title, NULL, NULL);
        //this places the window in the top left corner of the screen
        glfwSetWindowPos(glfwWindow , 0 , 0);

        if (glfwWindow == NULL) throw new IllegalStateException("Failed to create the GLFW window.");
        
        setCallbacks();
        
        // Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);

        System.out.println("Window initialization complete.");

    }

    void shutDown() {
    	
    	System.out.println("Shutting down GLFW...");
	    // Free the memory
	    glfwFreeCallbacks(glfwWindow);
	    glfwDestroyWindow(glfwWindow);
	    // Terminate GLFW and the free the error callback
	    glfwTerminate();
	    glfwSetErrorCallback(null).free();
	    
	    memFree(startingX);
	    memFree(startingY);
	    memFree(newX);
	    memFree(newY);
	    
	    memFree(winWidth);
	    memFree(winHeight);
	    
	    System.out.println("GLFW shut down.");
    	
    }
    
    public void setNuklearContext(NkContext context) {

    	NuklearContext = context;

	}

	public void overrideCloseWindow(){

		glfwSetWindowShouldClose(glfwWindow , true);

    }

	public long getGlfwWindow() {

		return glfwWindow;

	}

	public int getKey(int key) {
		
		return glfwGetKey(glfwWindow , key);
		
	}
	
	//struck
	private static boolean isWStruck = false;
	private static boolean isAStruck = false;
	private static boolean isSStruck = false;
	private static boolean isDStruck = false;
            
	private static boolean isUpStruck = false;
	private static boolean isLeftStruck = false;
	private static boolean isDownStruck = false;
	private static boolean isRightStruck = false;
	         
	private static boolean isSpaceStruck = false;
	private static boolean isLShiftStruck = false;
	private static boolean isLControlStruck = false;
	private static boolean isLAltStruck = false;
	         
	private static boolean isEStruck = false;
	private static boolean isRStruck = false;
	private static boolean isTStruck = false;
	private static boolean isYStruck = false;
	private static boolean isUStruck = false;
	private static boolean isIStruck = false;
	private static boolean isOStruck = false;
	private static boolean isPStruck = false;
	        
	private static boolean isKP2Struck = false;
	private static boolean isKP4Struck = false;
	private static boolean isKP6Struck = false;
	private static boolean isKP8Struck = false;
     
	private static boolean isLMouseStruck = false;
	private static boolean isRMouseStruck = false;
	private static boolean isMMouseStruck = false;
	private static boolean isM4Struck = false;
	private static boolean isM5Struck = false;
	private static boolean isTabStruck = false;
	
	private static boolean isZStruck = false;
	private static boolean isXStruck = false;
	
	//PRESSED
	private static boolean isWPressed = false;
	private static boolean isAPressed = false;
	private static boolean isSPressed = false;
	private static boolean isDPressed = false;
            
	private static boolean isUpPressed = false;
	private static boolean isLeftPressed = false;
	private static boolean isDownPressed = false;
	private static boolean isRightPressed = false;
	         
	private static boolean isSpacePressed = false;
	private static boolean isLShiftPressed = false;
	private static boolean isLControlPressed = false;
	private static boolean isLAltPressed = false;
	         
	private static boolean isEPressed = false;
	private static boolean isRPressed = false;
	private static boolean isTPressed = false;
	private static boolean isYPressed = false;
	private static boolean isUPressed = false;
	private static boolean isIPressed = false;
	private static boolean isOPressed = false;
	private static boolean isPPressed = false;
	        
	private static boolean isKP2Pressed = false;
	private static boolean isKP4Pressed = false;
	private static boolean isKP6Pressed = false;
	private static boolean isKP8Pressed = false;
     
	private static boolean isLMousePressed = false;
	private static boolean isRMousePressed = false;
	private static boolean isMMousePressed = false;
	private static boolean isM4Pressed = false;
	private static boolean isM5Pressed = false;
	private static boolean isTabPressed = false;
	
	private static boolean isZPressed = false;
	private static boolean isXPressed = false;
	
	//RELEASED
	private static boolean isWReleased = false;
	private static boolean isAReleased = false;
	private static boolean isSReleased = false;
	private static boolean isDReleased = false;
     
	private static boolean isUpReleased = false;
	private static boolean isLeftReleased  = false;
	private static boolean isDownReleased  = false;
	private static boolean isRightReleased  = false;
	 
	private static boolean isSpaceReleased  = false;
	private static boolean isLShiftReleased = false;
	private static boolean isLControlReleased  = false;
	private static boolean isLAltReleased  = false;
	 
	private static boolean isEReleased = false;
	private static boolean isRReleased = false;
	private static boolean isTReleased = false;
	private static boolean isYReleased = false;
	private static boolean isUReleased = false;
	private static boolean isIReleased = false;
	private static boolean isOReleased = false;
	private static boolean isPReleased = false;
	 
	private static boolean isKP2Released = false;
	private static boolean isKP4Released = false;
	private static boolean isKP6Released = false;
	private static boolean isKP8Released = false;
     
	private static boolean isLMouseReleased = false;
	private static boolean isRMouseReleased = false;
	private static boolean isMMouseReleased = false;
	private static boolean isM4Released = false;
	private static boolean isM5Released = false;
	private static boolean isTabReleased = false;
	
	private static boolean isZReleased = false;
	private static boolean isXReleased = false;
		
	public void releaseKeys() {
		
		isWReleased = false;
		isAReleased = false;
		isSReleased = false;
		isDReleased = false;

		isUpReleased = false;
		isLeftReleased  = false;
		isDownReleased  = false;
		isRightReleased  = false;
		
		isSpaceReleased  = false;
		isLShiftReleased = false;
		isLControlReleased  = false;
		
		isEReleased = false;
		isRReleased = false;
		isTReleased = false;
		isYReleased = false;
		isUReleased = false;
		isIReleased = false;
		isOReleased = false;
		isPReleased = false;
		
		isKP2Released = false;
		isKP4Released = false;
		isKP6Released = false;
		isKP8Released = false;

		isLMouseReleased = false;
		isRMouseReleased = false;
		isMMouseReleased = false;
		isM4Released = false;
		isM5Released = false;
		isTabReleased = false;
		
		isZReleased = false;
		isXReleased = false;
		isLAltReleased = false;
		
		isWStruck = false;
		isAStruck = false;
		isSStruck = false;
		isDStruck = false;

		isUpStruck = false;
		isLeftStruck = false;
		isDownStruck = false;
		isRightStruck = false;
		
		isSpaceStruck = false;
		isLShiftStruck = false;
		isLControlStruck = false;
		
		isEStruck = false;
		isRStruck = false;
		isTStruck = false;
		isYStruck = false;
		isUStruck = false;
		isIStruck = false;
		isOStruck = false;
		isPStruck = false;
		
		isKP2Struck = false;
		isKP4Struck = false;
		isKP6Struck = false;
		isKP8Struck = false;

		isLMouseStruck = false;
		isRMouseStruck = false;
		isMMouseStruck = false;
		isM4Struck = false;
		isM5Struck = false;
		isTabStruck = false;
		isZStruck = false;
		isXStruck = false;
		isLAltStruck = false;
		
		
	}
	
	public int[] getWindowDimensions(){

		try(MemoryStack stack = stackPush()){

			IntBuffer width = stack.mallocInt(1);
			IntBuffer height = stack.mallocInt(1);
			glfwGetWindowSize(glfwWindow , width , height);
			return new int [] {width.get(0) , height.get(0)};

		}

	}
	
	/**
	 * 
	 * Gets cursor position x and y coordinates in screen space and returns them as an array.
	 * 
	 * @return double array where index 0 is cursor's x position and index 1 is cursor's y position
	 */
	public double[] getCursorPos(){

		try(MemoryStack stack = stackPush()){

			DoubleBuffer cursorX = stack.mallocDouble(1);
			DoubleBuffer cursorY = stack.mallocDouble(1);
			glfwGetCursorPos(glfwWindow, cursorX , cursorY);
			double[] returnArray = {cursorX.get(0) , cursorY.get(0)};
			return returnArray;

		}

	}
	
	public float[] getCursorWorldCoords() {
		
		double[] coords = getCursorPos();
		int[] winDims = getWindowDimensions();
		return new float[] {(float) getSCToWCForX(coords[0] , winDims[0] , winDims[1] , engine.renderer.getCamera()) ,
							(float)	getSCToWCForY(coords[1] , winDims[0] , winDims[1] , engine.renderer.getCamera())};
		
	}
	
	public static boolean isWPressed() {

		return isWPressed;

	}

	public static boolean isAPressed() {

		return isAPressed;

	}

	public static boolean isSPressed() {

		return isSPressed;

	}

	public static boolean isDPressed(){

		return isDPressed;

	}
	
	public boolean isAnyMoveKeyPressed() {
		
		if(isDPressed || isSPressed || isAPressed || isWPressed) return true;
		else return false;
		
	}
	
	public boolean isAnyHorizontalMoveKeyPressed() {
		
		return isDPressed || isAPressed;
		
	}
	
	public static boolean isSpacePressed() {
		
		return isSpacePressed;
		
	}
	
	public boolean isLShiftPressed() {
		
		return isLShiftPressed;
		
	}

	public boolean isUpPressed(){

		return isUpPressed;

	}

	public boolean isLeftPressed(){

		return isLeftPressed;

	}

	public boolean isDownPressed(){

		return isDownPressed;

	}

	public boolean isRightPressed(){

		return isRightPressed;

	}

	public boolean isLControlPressed() {
		
		return isLControlPressed;
		
	}
		
	public boolean isEPressed() {
		
		return isEPressed;
		
	}
	
	public boolean isRPressed() {
		
		return isRPressed;
		
	}
	
	public boolean isTPressed() {
	
		return isTPressed;
	
	}
	
	public boolean isYPressed() {
		
		return isYPressed;
		
	}
	
	public boolean isUPressed() {
		
		return isUPressed;
		
	}
	
	public boolean isKP2Pressed() {
		
		return isKP2Pressed; 
		
	}
	
	public boolean isKP4Pressed() {
		
		return isKP4Pressed; 
		
	}
	
	public boolean isKP6Pressed() {
		
		return isKP6Pressed; 
		
	}
	
	public boolean isKP8Pressed() {
		
		return isKP8Pressed; 
		
	}

	public boolean isLMousePressed() {
		
		return isLMousePressed;
		
	}
	
	public boolean isRMousePressed() {
		
		return isRMousePressed;
		
	}
	
	public boolean isMMousePressed() {
		
		return isMMousePressed;
		
	}
	
	public boolean isM4Pressed() {
		
		return isM4Pressed;
		
	}
	
	public boolean isM5Pressed() {
		
		return isM5Pressed;
		
	}
	
	public boolean isHorizMoveKeyPressed() {
		
		return isAPressed || isDPressed;
		
	}
	
	public boolean isVertMoveKeyPressed() {
		
		return isWPressed || isSPressed || isSpacePressed;
				
	}
	
	public boolean isIPressed() {
	
		return isIPressed;
				
	}
	
	public boolean isOPressed() {
		
		return isOPressed;
		
	}
	
	public boolean isPPressed() {
		
		return isPPressed;
		
	}
	
	public boolean isTabPressed() {
		
		return isTabPressed;
		
	}
	
	public boolean isZPressed() {
		
		return isZPressed;
		
	}
	
	public boolean isXPressed() {
		
		return isXPressed;
		
	}
	
	public boolean isWReleased() {
		return isWReleased;
	}

	public boolean isAReleased() {
		return isAReleased;
	}

	public boolean isSReleased() {
		return isSReleased;
	}

	public boolean isDReleased() {
		return isDReleased;
	}

	public boolean isUpReleased() {
		return isUpReleased;
	}

	public boolean isLeftReleased() {
		return isLeftReleased;
	}

	public boolean isDownReleased() {
		return isDownReleased;
	}

	public boolean isRightReleased() {
		return isRightReleased;
	}

	public boolean isSpaceReleased() {
		return isSpaceReleased;
	}

	public boolean isLShiftReleased() {
		return isLShiftReleased;
	}

	public boolean isLControlReleased() {
		return isLControlReleased;
	}

	public boolean isEReleased() {
		return isEReleased;
	}

	public boolean isRReleased() {
		return isRReleased;
	}

	public boolean isTReleased() {
		return isTReleased;
	}

	public boolean isYReleased() {
		return isYReleased;
	}

	public boolean isUReleased() {
		return isUReleased;
	}

	public boolean isIReleased() {
		return isIReleased;
	}

	public boolean isOReleased() {
		return isOReleased;
	}

	public boolean isPReleased() {
		return isPReleased;
	}

	public boolean isKP2Released() {
		return isKP2Released;
	}

	public boolean isKP4Released() {
		return isKP4Released;
	}

	public boolean isKP6Released() {
		return isKP6Released;
	}

	public boolean isKP8Released() {
		return isKP8Released;
	}

	public boolean isLMouseReleased() {
		return isLMouseReleased;
	}

	public boolean isRMouseReleased() {
		return isRMouseReleased;
	}

	public boolean isMMouseReleased() {
		return isMMouseReleased;
	}

	public boolean isM4Released() {
		return isM4Released;
	}

	public boolean isM5Released() {
		return isM5Released;
	}

	public static boolean isTabReleased() {
		
		return isTabReleased;
		
	}

	public static boolean isZReleased() {
		
		return isZReleased;
		
	}

	public static boolean isXReleased() {
		
		return isXReleased;
		
	}
	
	public static boolean isAStruck() {

		return isAStruck;
		
	}

	public static boolean isWStruck() {
		return isWStruck;
	}

	public static boolean isSStruck() {
		return isSStruck;
	}

	public static boolean isDStruck() {
		return isDStruck;
	}

	public static boolean isUpStruck() {
		return isUpStruck;
	}

	public static boolean isLeftStruck() {
		return isLeftStruck;
	}

	public static boolean isDownStruck() {
		return isDownStruck;
	}

	public static boolean isRightStruck() {
		return isRightStruck;
	}

	public static boolean isSpaceStruck() {
		return isSpaceStruck;
	}

	public static boolean isLShiftStruck() {
		return isLShiftStruck;
	}

	public static boolean isLControlStruck() {
		return isLControlStruck;
	}

	public static boolean isEStruck() {
		return isEStruck;
	}

	public static boolean isRStruck() {
		return isRStruck;
	}

	public static boolean isTStruck() {
		return isTStruck;
	}

	public static boolean isYStruck() {
		return isYStruck;
	}

	public static boolean isUStruck() {
		return isUStruck;
	}

	public static boolean isIStruck() {
		return isIStruck;
	}

	public static boolean isOStruck() {
		return isOStruck;
	}

	public static boolean isPStruck() {
		return isPStruck;
	}

	public static boolean isKP2Struck() {
		return isKP2Struck;
	}

	public static boolean isKP4Struck() {
		return isKP4Struck;
	}

	public static boolean isKP6Struck() {
		return isKP6Struck;
	}
	
	public static boolean isKP8Struck() {
		return isKP8Struck;
	}

	public static boolean isLMouseStruck() {
		return isLMouseStruck;
	}

	public static boolean isRMouseStruck() {
		return isRMouseStruck;
	}

	public static boolean isMMouseStruck() {
		return isMMouseStruck;
	}

	public static boolean isM4Struck() {
		return isM4Struck;
	}

	public static boolean isM5Struck() {
		return isM5Struck;
	}
	
	public static boolean isTabStruck() {
		
		return isTabStruck;
		
	}

	public static boolean isZStruck() {
		
		return isZStruck;
		
	}

	public static boolean isXStruck() {
		
		return isXStruck;
		
	}
	
	public static boolean isLAltStruck(){
		
		return isLAltStruck;
		
	}

	public static boolean isLAltReleased(){
		
		return isLAltReleased;
		
	}
	
	DoubleBuffer startingX = memAllocDouble(1).put(0 , 0);
	DoubleBuffer startingY = memAllocDouble(1).put(0 , 0);
	DoubleBuffer newX = memAllocDouble(1).put(0 , 0);
	DoubleBuffer newY = memAllocDouble(1).put(0 , 0);
	
	private float pressWorldX = -1;
	private float pressWorldY = -1;
	
	private float releaseWorldX = -1;
	private float releaseWorldY = -1;
	
	IntBuffer winWidth = memAllocInt(1).put(0 , 1080);
	IntBuffer winHeight = memAllocInt(1).put(0 , 1920);
	
	private void setCallbacks() {    	

    		glfwSetKeyCallback(glfwWindow , (window , key , scancode , action , mods) ->{

    			boolean press = action == GLFW_PRESS;
    			switch(key){

    				//XXX
    				case GLFW_KEY_ESCAPE:

    					switch(action){

    						case GLFW_PRESS:

    							System.out.println("Escape key pressed.");
    							glfwSetWindowShouldClose(window, true); 

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;
    					
    				case GLFW_KEY_F1:

    					switch(action){

    						case GLFW_PRESS:

    							try(MemoryStack stack = stackPush()){

    								IntBuffer width = stack.mallocInt(1);
    								IntBuffer height = stack.mallocInt(1);
    								glfwGetWindowSize(glfwWindow , width , height);
    								glViewport(0, 0, width.get(0), height.get(0));

    							}

    							if(isLShiftPressed) glPolygonMode(GL_FRONT_AND_BACK , GL_FILL);
    							else if (isLControlPressed) {
    								
    								MemoryAllocationReport report = (address , memory , threadID , threadName , element) ->{
    								    
    							    	System.out.println("At Address: " + address + "; " + memory + " bytes");
    							    	
    							    };
    							    
    							    memReport(report);
    							    
    							}

    							
    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F2:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							if(isLShiftPressed) glPolygonMode(GL_FRONT_AND_BACK , GL_LINE);
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F3:

    					switch(action){

    						case GLFW_PRESS:

    							
    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F4:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F5:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;
    						
    					}

    					break;

    				case GLFW_KEY_F6:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F7:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F8:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F9:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F10:

    					switch(action){

    						case GLFW_PRESS:
    							
    							DebugInfo.showDebug = DebugInfo.showDebug ? false:true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F11:

    					switch(action){

    						case GLFW_PRESS:

    							engine.g_toggleMultiplayerUI();
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F12:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F13:

    					switch(action){

    						case GLFW_PRESS: break;
    						case GLFW_RELEASE: break;
    						case GLFW_REPEAT: break;

    					}

    					break;

    				case GLFW_KEY_F14:

    					switch(action){

    						case GLFW_PRESS: break;
    						case GLFW_RELEASE: break;
    						case GLFW_REPEAT: break;

    					}

    					break;

    				case GLFW_KEY_F15:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F16:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F17:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F18:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F19:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F20:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F21:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F22:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F23:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F24:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_F25:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;
    				//XXX
    				case GLFW_KEY_GRAVE_ACCENT:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_1:

    					switch(action){

    						case GLFW_PRESS:


    							break;

    						case GLFW_RELEASE:

//    							engine.e_previewFadeOut();
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_2:

    					switch(action){

    						case GLFW_PRESS:

    							
    							break;

    						case GLFW_RELEASE:

//    							engine.e_previewFadeIn();
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_3:

    					switch(action){

    						case GLFW_PRESS:


    		    				break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_4:

    					switch(action){

    						case GLFW_PRESS:


    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_5:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){



    							}


    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_6:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){



    							}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_7:

    					switch(action){

    						case GLFW_PRESS:



    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_8:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){



    							}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_9:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){


    							}


    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_0:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_MINUS:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

        	    				if(isLControlPressed){}
    							break;

    					}

    					break;

    				case GLFW_KEY_EQUAL:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

        	    				if(isLControlPressed){}

    							break;

    					}

    					break;

    				case GLFW_KEY_BACKSPACE:

    					switch(action){

    						case GLFW_PRESS:

    			    			nk_input_key(NuklearContext, NK_KEY_BACKSPACE, press);
    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    			    			nk_input_key(NuklearContext, NK_KEY_BACKSPACE, press);
    							break;

    					}

    					break;

    				case GLFW_KEY_INSERT:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_HOME:

    					switch(action){

    						case GLFW_PRESS:

    			    			nk_input_key(NuklearContext, NK_KEY_TEXT_START, press);
    			    			nk_input_key(NuklearContext, NK_KEY_SCROLL_START, press);

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_PAGE_UP:

    					switch(action){

    						case GLFW_PRESS:

    							nk_input_key(NuklearContext, NK_KEY_SCROLL_UP, press);

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_NUM_LOCK:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_SLASH:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;
    				//XXX
    				case GLFW_KEY_TAB:

    					switch(action){

    						case GLFW_PRESS:

    							nk_input_key(NuklearContext, NK_KEY_TAB, press);
    							isTabPressed = true;
    							isTabStruck = true;
    							
    							break;

    						case GLFW_RELEASE:
    							
    							isTabReleased =  true;
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_Q:

    					switch(action){

    						case GLFW_PRESS:

    							

    							break;

    						case GLFW_RELEASE:


    							break;

    						case GLFW_REPEAT:
    							
    							
    							
    							break;

    					}

    					break;

    				case GLFW_KEY_W:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isWStruck = true;
    							isWPressed = true;

    							if (isLShiftPressed)engine.e_attemptTransform(0 , 1);

    							break;

    						case GLFW_RELEASE:

    							isWPressed = false;
    							isWReleased = true;

    							break;

    						case GLFW_REPEAT:

    							if(isLShiftPressed) engine.e_attemptTransform(0 , 1);
    							
    							break;

    					}

    					break;

    				case GLFW_KEY_E:

    					switch(action){

    						case GLFW_PRESS:

    							isEStruck = true;
    							if(isLControlPressed) {
    								
    								
    								
    							}
    							
    							isEPressed = true;

    							break;

    						case GLFW_RELEASE:

    							isEPressed = false;
    							isEReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_R:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isRStruck = true;
    							if(isLControlPressed){

    								

    							} else if (isLShiftPressed) {
    								
    								
    								
    							}

    							isRPressed = true;
    							
    							break;

    						case GLFW_RELEASE:

    							isRPressed = false;
    							isRReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;
    					
    				case GLFW_KEY_T:

    					switch(action){
    					
    						case GLFW_PRESS:
    							
    							isTStruck = true;
    							if(isLControlPressed){
    								
    								engine.e_textureActiveObject();
    							}
    							
    							isTPressed = true;

    							break;

    						case GLFW_RELEASE:

    							isTPressed = false;
    							isTReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_Y:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isYStruck = true;
    							isYPressed = true;
    							
    							break;

    						case GLFW_RELEASE:

    							isYPressed = false;
    							isYReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_U:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isUStruck = true;
    							isUPressed = true;
    							
    							break;

    						case GLFW_RELEASE:

    							isUPressed = false;
    							isUReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_I:

    					switch(action){

    						case GLFW_PRESS:

    							isIStruck = true;
    							isIPressed = true;
    							
    							if(isLShiftPressed && isLControlPressed) engine.e_loadItem();
    							else if (isLControlPressed) engine.e_newItem();
    							
    							break;

    						case GLFW_RELEASE:

    							isIPressed = false;
    							isIReleased = true;    							
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_O:

    					switch(action){

    						case GLFW_PRESS:

    							isOStruck = true;
    							isOPressed = true;
    							
    							break;

    						case GLFW_RELEASE:

    							isOPressed = false;
    							isOReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_P:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isPStruck = true;
    							isPPressed = true;
    							
    							break;

    						case GLFW_RELEASE:

    							isPPressed = false;
    							isPReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_LEFT_BRACKET:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_RIGHT_BRACKET:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_BACKSLASH:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_DELETE:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){

    								engine.e_removeActiveQuad();
    								
    							}

    			    			nk_input_key(NuklearContext, NK_KEY_DEL, press);

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_END:

    					switch(action){

    						case GLFW_PRESS:

    			    			nk_input_key(NuklearContext, NK_KEY_TEXT_END, press);
    			    			nk_input_key(NuklearContext, NK_KEY_SCROLL_END, press);

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_PAGE_DOWN:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed) engine.e_deleteScene();
    							nk_input_key(NuklearContext, NK_KEY_SCROLL_DOWN, press);

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				//XXX
    				case GLFW_KEY_A:

    					switch(action){

    						case GLFW_PRESS:

    							isAStruck = true;
    							isAPressed = true;
    							
    							if (isLShiftPressed) engine.e_attemptTransform(-1 , 0);

    							break;

    						case GLFW_RELEASE:

    							isAPressed = false;
    							isAReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							if (isLShiftPressed) engine.e_attemptTransform(-1 , 0);

    							break;

    					}

    					break;

    				case GLFW_KEY_S:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isSStruck = true;
    							isSPressed = true;
    							
    							if(isLShiftPressed && isLAltPressed) engine.e_snapSelectionArea();
    							if(isLShiftPressed) engine.e_attemptTransform(0 , -1);
    							break;

    						case GLFW_RELEASE:

    							isSPressed = false;
    							isSReleased = true;

    							break;

    						case GLFW_REPEAT:

    							if(isLShiftPressed) engine.e_attemptTransform(0 , -1);
								
    							break;

    					}

    					break;

    				case GLFW_KEY_D:

    					switch(action){

    						case GLFW_PRESS:

    							isDStruck = true;
    							isDPressed = true;
    							if(isLShiftPressed) engine.e_attemptTransform(1 , 0);
								
    							break;

    						case GLFW_RELEASE:

    							isDPressed = false;
    							isDReleased = true;

    							break;

    						case GLFW_REPEAT:
					
    							if(isLShiftPressed) engine.e_attemptTransform(1 , 0);
    							break;

    					}

    					break;

    				case GLFW_KEY_F:

    					switch(action){

    						case GLFW_PRESS:
    							
    							if(isLShiftPressed && isLControlPressed) engine.e_loadEntity();
    							else if(isLControlPressed) engine.e_newEntity();    							

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_G:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){

    								engine.e_addQuad();

    							}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_H:

    					switch(action){

    						case GLFW_PRESS:
    							
    							if(isLControlPressed && isLShiftPressed) engine.e_loadStatic();
    							else if(isLControlPressed) engine.e_addStatic();

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_J:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){

    								engine.e_addCollider();
    								
    							}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_K:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){

    								

    							}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_L:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed) engine.e_loadLevel();

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_SEMICOLON:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_APOSTROPHE:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_ENTER:

    					switch(action){

    						case GLFW_PRESS:

    			    			nk_input_key(NuklearContext, NK_KEY_ENTER, press);
    			    			engine.e_returnConsole();
    			    			DialogUtils.acceptLast();
    			    			
    			    			if(isLControlPressed){} else {}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_CAPS_LOCK:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;
    				//XXX
    				case GLFW_KEY_LEFT_SHIFT:

    					switch(action){

    						case GLFW_PRESS:

    							nk_input_key(NuklearContext, NK_KEY_SHIFT, press);
    							isLShiftStruck = true;
    							isLShiftPressed = true;
    							
    							break;

    						case GLFW_RELEASE:

    							isLShiftPressed = false;
    							isLShiftReleased = true;
    							
    							if(isLAltPressed) {
    								
    								DialogUtils.acceptLast();
    								
    							}
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_Z:

    					switch(action){

    						case GLFW_PRESS:

    							isZPressed = true;
    							isZStruck = true;
    							
    							break;

    						case GLFW_RELEASE:

    							isZPressed = false;
    							isZReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_X:

    					switch(action){

    						case GLFW_PRESS:

    							isXPressed = true;
    							isXStruck = true;
    							

    							break;

    						case GLFW_RELEASE:

    							isXReleased = true;
    							isXPressed = false;
    							
    							
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_C:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed && isLShiftPressed){

    								engine.e_setActiveQuadColor();

    							} else if (isLControlPressed) {
    								
    								engine.e_copyActiveObject();
    								
    							}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					
    					
    					break;

    				case GLFW_KEY_V:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed && isLShiftPressed){
    								
    								engine.e_filterActiveObjectColor();

    							} else if (isLControlPressed) {
    								
    								engine.e_pasteCopyAtCursor();
    								
    							}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_B:

    					switch(action){

    						case GLFW_PRESS:

    							

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_N:

    					switch(action){

    						case GLFW_PRESS:

    							

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_M:

    					switch(action){

    						case GLFW_PRESS:

    							

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_COMMA:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_PERIOD:

    					switch(action){

    						case GLFW_PRESS:

    							if(isLControlPressed){}

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;
    				
    				case GLFW_KEY_RIGHT_SHIFT:

    					switch(action){

    						case GLFW_PRESS:

    							nk_input_key(NuklearContext, NK_KEY_SHIFT, press);

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_UP:

    					switch(action){

    						case GLFW_PRESS:

    							isUpPressed = true;
    							isUpStruck = true;
    			    			nk_input_key(NuklearContext, NK_KEY_UP, press);
    			    			
    			    			if (isLControlPressed) engine.e_getEditor().moveSelectionAreaUpperFace(1);

    							break;

    						case GLFW_RELEASE:

    							isUpPressed = false;
    							isUpReleased = true;

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_LEFT:

    					switch(action){

    						case GLFW_PRESS:

    							isLeftPressed = true;

    							if (isLControlPressed) engine.e_getEditor().moveSelectionAreaLeftFace(1);
    							
    							break;

    						case GLFW_RELEASE:

    							isLeftPressed = false;
    							isLeftReleased = true;

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_DOWN:

    					switch(action){

    						case GLFW_PRESS:

    							isDownPressed = true;
    							isDownStruck = true;
    			    			nk_input_key(NuklearContext, NK_KEY_DOWN, press);

    			    			if (isLControlPressed) engine.e_getEditor().moveSelectionAreaLowerFace(1);
    			    			
    							break;

    						case GLFW_RELEASE:

    							isDownPressed = false;
    							isDownReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_RIGHT:

    					switch(action){

    						case GLFW_PRESS:

    							isRightPressed = true;
    							isRightStruck = true;

    							if (isLControlPressed) engine.e_getEditor().moveSelectionAreaRightFace(1);
    							
    							break;

    						case GLFW_RELEASE:

    							isRightPressed = false;
    							isRightReleased = true;

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;
    				//XXX
    				case GLFW_KEY_LEFT_CONTROL:

    					switch(action){

    						case GLFW_PRESS:

    		    				isLControlPressed = true;
    		    				isLControlStruck = true;
    		    				
    		    				nk_input_key(NuklearContext, NK_KEY_COPY, glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_PASTE, glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_CUT, glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_TEXT_UNDO, glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_TEXT_REDO, glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_TEXT_WORD_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_TEXT_WORD_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_TEXT_LINE_START, glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_TEXT_LINE_END, glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS);

    							break;

    						case GLFW_RELEASE:

    		    				isLControlPressed = false;
    		    				isLControlReleased = true;

    		    				nk_input_key(NuklearContext, NK_KEY_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_COPY, false);
    		    				nk_input_key(NuklearContext, NK_KEY_PASTE, false);
    		    				nk_input_key(NuklearContext, NK_KEY_CUT, false);

    		    				nk_input_key(NuklearContext, NK_KEY_SHIFT, false);

       							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_LEFT_ALT:

    					switch(action){

    						case GLFW_PRESS:

    							isLAltPressed = true;
    							isLAltStruck = true;
    							
    							break;

    						case GLFW_RELEASE:

    							isLAltPressed = false;
    							isLAltReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_SPACE:

    					switch(action){

    						case GLFW_PRESS:

    							isSpacePressed = true;
    							isSpaceStruck = true;
    							
    		    				switch(state){

        		    				case EDITOR:
        		    					
        		    					break;

    								case GAME:

    									break;
    									
    								default:

    									break;

    		    				}

    							break;

    						case GLFW_RELEASE:

    							isSpacePressed = false;
    							isSpaceReleased = true;
    							
    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_RIGHT_ALT:

    					switch(action){

    						case GLFW_PRESS:

    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_RIGHT_CONTROL:

    					switch(action){

    						case GLFW_PRESS:


    							break;

    						case GLFW_RELEASE:

    							break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;


        				//XXX
        				case GLFW_KEY_KP_0:

        					switch(action){

        						case GLFW_PRESS:

        							
        							
        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_1:

        					switch(action){

        						case GLFW_PRESS:

        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_2:

        					switch(action){

        						case GLFW_PRESS:

        							isKP2Pressed = true;
        							isKP2Struck = true;

        							break;

        						case GLFW_RELEASE:

        							isKP2Pressed = false;
        							isKP2Released = true;
        							
        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_3:

        					switch(action){

        						case GLFW_PRESS:


        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_4:

        					switch(action){

        						case GLFW_PRESS:

        							isKP4Pressed = true;
        							isKP4Struck = true;
        							
        							break;

        						case GLFW_RELEASE:

        							isKP4Pressed = false;
        							isKP4Released = true;
        							
        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_5:

        					switch(action){

        						case GLFW_PRESS:



        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_6:

        					switch(action){

        						case GLFW_PRESS:

        							isKP6Pressed = true;
        							isKP6Struck = true;
        							
        							break;

        						case GLFW_RELEASE:

        							isKP6Pressed = false;
        							isKP6Released = true;
        							
        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_7:

        					switch(action){

        						case GLFW_PRESS:

        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_8:

        					switch(action){

        						case GLFW_PRESS:

        							isKP8Pressed = true;
        							isKP8Struck = true;
        							
        							break;

        						case GLFW_RELEASE:

        							isKP8Pressed = false;
        							isKP8Released = true;
        							
        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_9:

        					switch(action){

        						case GLFW_PRESS:

        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_DIVIDE:

        					switch(action){

        						case GLFW_PRESS:

        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_MULTIPLY:

        					switch(action){

        						case GLFW_PRESS:

        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_SUBTRACT:

        					switch(action){

        						case GLFW_PRESS:

        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_ADD:

        					switch(action){

        						case GLFW_PRESS:

        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_ENTER:

        					switch(action){

        						case GLFW_PRESS:
        							
        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

        				case GLFW_KEY_KP_DECIMAL:

        					switch(action){

        						case GLFW_PRESS:

        							break;

        						case GLFW_RELEASE:

        							break;

        						case GLFW_REPEAT:

        							break;

        					}

        					break;

    			}
    	
        	});
    		
    		glfwSetCharCallback(glfwWindow , (window , codepoint) -> {

    			nk_input_unicode(NuklearContext, codepoint);
    			if (codepoint == 113){} else if (codepoint == 97) {}

    		});
    		
    		glfwSetMouseButtonCallback(glfwWindow , (window , button , action , mods) ->{
    			
    			try(MemoryStack stack = stackPush()){
    				
    				int nkButton = NK_BUTTON_LEFT;
    				
    				switch(button) {
    				
	    				case GLFW_MOUSE_BUTTON_RIGHT:
	    			
	    					nkButton = NK_BUTTON_RIGHT;
	    					
	    					switch(action) {
	    					
		    					case GLFW_PRESS:
		    						
		    						isRMousePressed = true;         
		    						isRMouseStruck = true;
		    						glfwGetCursorPos(glfwWindow, startingX , startingY);
		    						pressWorldX = (float)getSCToWCForX(startingX.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());
		    						pressWorldY = (float)getSCToWCForY(startingY.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());		    			
		    						
		    						break;
		    						
		    					case GLFW_RELEASE:

		    						isRMousePressed = false;
		    						isRMouseReleased = true;
		    						glfwGetCursorPos(glfwWindow , newX , newY);
		            				
		    						releaseWorldX = (float)getSCToWCForX(newX.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());
		    						releaseWorldY = (float)getSCToWCForY(newY.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());

		            				double deltaX = pressWorldX - releaseWorldX;
		            				double deltaY = releaseWorldY - pressWorldY;

		            				if(nk_window_is_any_hovered(NuklearContext)) break;
		            					            				
		            				//to move the camera, either no list is focused or no object is selected.
		            				if(Engine.STATE == RuntimeState.EDITOR && engine.e_getEditor().getState() == EditorMode.BUILD_MODE)
		            					engine.getCamera().moveCamera((float)deltaX, -(float)deltaY);
		            				
		    						break;		    						
		    						
		    					case GLFW_REPEAT:
		    						
		    						
		    						
		    						break;
		    					
	    					}
	    					
	    					break;
	    					
	    				case GLFW_MOUSE_BUTTON_LEFT:
	    					
    	    				nkButton = NK_BUTTON_LEFT;
    	    				
	    					switch(action) {
	    					
		    					case GLFW_PRESS:
		            				
		            				glfwGetCursorPos(glfwWindow, startingX , startingY);
		    						pressWorldX = (float)getSCToWCForX(startingX.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());
		    						pressWorldY = (float)getSCToWCForY(startingY.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());		    			

		    						if(isLControlPressed){
		    							
		    							isLMousePressed = true;
		    							isLMouseStruck = true;
		    							
		            				} else {

		            					if(nk_window_is_any_hovered(NuklearContext)) break;

		            					isLMousePressed = true;		         
		            					isLMouseStruck = true;
		            					boolean selectedSame = engine.e_select((float)pressWorldX , (float) pressWorldY);
		            					if(selectedSame && engine.e_cursorState() == CursorState.DRAGGING) engine.e_setCursorState(CursorState.SELECTABLE);
		            					else if (selectedSame) engine.e_setCursorState(CursorState.DRAGGING);
		            								       		            							            					
		            				}	
		    						
		    						break;
		    						
		    					case GLFW_RELEASE:
		    						
		    						isLMousePressed = false;
		    						isLMouseReleased = true;
		    						glfwGetCursorPos(glfwWindow , newX , newY);
		            				
		    						releaseWorldX = (float)getSCToWCForX(newX.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());
		    						releaseWorldY = (float)getSCToWCForY(newY.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());
		            				
		    						if(nk_window_is_any_hovered(NuklearContext)) break;
		    						
		            				//click-drag selection box        				
		            				if(isLShiftPressed) {
		            					        					
		            					float xDim = (float) releaseWorldX - pressWorldX;
		            					float yDim = (float) releaseWorldY - pressWorldY;
		    
		            					engine.e_getEditor().moveSelectionAreaTo((float) pressWorldX, (float)pressWorldY);
		            					engine.e_getEditor().setSelectionAreaDimensions(xDim, yDim);
		            							            					
		            				} 
	            				
		            				if(Engine.STATE == RuntimeState.EDITOR && isLShiftPressed && isLControlPressed) {
		            					
		            					engine.editor.say("Cursor X: " + releaseWorldX);
		            					engine.editor.say("Cursor Y: " + releaseWorldY);
		            					
		            				}
		            				
		    						break;
		    						
		    						
		    					case GLFW_REPEAT:
		    						
		    						
		    						
		    						break;
	    					
	    					}			
	    					
	    					break;
	    					
	    				case GLFW_MOUSE_BUTTON_MIDDLE:
	    					
	    					nkButton = NK_BUTTON_MIDDLE;
	    					
	    					switch(action) {
		    					
		    					case GLFW_PRESS:
		    						
		    						isMMousePressed = true;
		    						isMMouseStruck = true;
		    						
		    						break;
		    						
		    					case GLFW_RELEASE:
		    						
		    						isMMousePressed = false;
		    						isMMouseReleased = true;
		    						
		    						break;
		    						
		    						
		    					case GLFW_REPEAT:
		    						
		    						
		    						
		    						break;
		    					
	    					}
	    					
	    					break;

	    				case GLFW_MOUSE_BUTTON_4:
	    					
	    					switch(action) {
	    					
		    					case GLFW_PRESS:
		    						
		    						isM4Pressed = true;
		    						isM4Struck = true;
		    						
		    						break;
		    						
		    					case GLFW_RELEASE:
		    						
		    						isM4Pressed = false;
		    						isM4Released = true;
		    						
		    						break;
		    						
		    						
		    					case GLFW_REPEAT:
		    						
		    						
		    						
		    						break;
		    					
	    					}
	    					
	    					break;

	    				case GLFW_MOUSE_BUTTON_5:
	    					
	    					switch(action) {
	    					
		    					case GLFW_PRESS:
		    						
		    						isM5Pressed = true;
		    						isM5Struck = true;
		    						
		    						break;
		    						
		    					case GLFW_RELEASE:
		    						
		    						isM5Pressed = false;
		    						isM5Released = true;
		    						
		    						break;
		    						
		    						
		    					case GLFW_REPEAT:
		    						
		    						
		    						
		    						break;
		    					
	    					}
	    					
	    					break;	    					
	    					
    				}
    				
    				nk_input_button(NuklearContext, nkButton , (int)startingX.get(0) , (int)startingY.get(0) , action == GLFW_PRESS);
    				
    			}    			

        	});
    		
    		glfwSetScrollCallback(glfwWindow , (window , xoffset, yoffset) -> {

    			try (MemoryStack stack = stackPush()) {
    				
    				NkVec2 scroll = NkVec2.malloc(stack).x((float)xoffset).y((float)yoffset);
    				
    				nk_input_scroll(NuklearContext, scroll);
    				
    			}
    			
    			if(nk_window_is_any_hovered(NuklearContext)) return;
    			
    			if (yoffset > 0) {

    				if(Engine.STATE == RuntimeState.EDITOR && engine.e_getEditor().getState() == EditorMode.BUILD_MODE) {
    					
    					if(isLControlPressed)engine.getCamera().scaleCamera(6 * (-(float) yoffset));
    					else if (!isLControlPressed)engine.getCamera().scaleCamera(-(float) yoffset);
    					
    				}

    			} else if (yoffset < 0) {

    				if(Engine.STATE == RuntimeState.EDITOR && engine.e_getEditor().getState() == EditorMode.BUILD_MODE) {
    					
    					if (isLControlPressed) engine.getCamera().scaleCamera(6 * (-(float) yoffset)); 
    					else if (!isLControlPressed) engine.getCamera().scaleCamera(-(float) yoffset);
    					
    				}

    			}

    		});
    		
    		glfwSetCursorEnterCallback(glfwWindow , (window , entered) -> {

    			if (entered == true) {

    			} else {

    			}

    		});
    		
   		glfwSetCursorPosCallback(glfwWindow , (window, xpos, ypos) -> nk_input_motion(NuklearContext, (int)xpos, (int)ypos));   		
   		glfwSetInputMode (glfwWindow , GLFW_CURSOR ,GLFW_CURSOR_NORMAL);
    	
    }

}
