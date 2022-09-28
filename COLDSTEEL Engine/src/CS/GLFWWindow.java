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
	
    void intialize(Engine engine){

    	this.engine = engine;
    	
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

	void overrideCloseWindow(){

		glfwSetWindowShouldClose(glfwWindow , true);

    }

	public long getGlfwWindow() {

		return glfwWindow;

	}

	
	public int[] getWindowDimensions(){
	
		try(MemoryStack stack = stackPush()){
		
			IntBuffer dims = stack.mallocInt(2);
			glfwGetWindowSize(glfwWindow , dims.slice(0, 1) , dims.slice(1, 1));
			return new int [] {dims.get() , dims.get()};
		
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

	float[] getCursorWorldCoords() {
	
		double[] coords = getCursorPos();		
		int[] winDims = getWindowDimensions();
		return new float[] {(float) getSCToWCForX(coords[0] , winDims[0] , winDims[1] , engine.renderer.getCamera()) ,
						(float)	getSCToWCForY(coords[1] , winDims[0] , winDims[1] , engine.renderer.getCamera())};
	
	}
	
	//Nilhearth Echo Pact
	//keyboard struck
    
	private static boolean isUpStruck = false;
	private static boolean isLeftStruck = false;
	private static boolean isDownStruck = false;
	private static boolean isRightStruck = false;
	         
	private static boolean isTabStruck = false;
	private static boolean isCapsStruck = false;
	private static boolean isLShiftStruck = false;
	private static boolean isLControlStruck = false;
	private static boolean isLAltStruck = false;
	private static boolean isBackSpaceStruck = false;
	private static boolean isEnterStruck = false;
	private static boolean isRShiftStruck = false;
	private static boolean isRAltStruck = false;
	private static boolean isRControlStruck = false;
	
	private static boolean isSpaceStruck = false;
   	
	private static boolean isQStruck = false;
	private static boolean isWStruck = false;
	private static boolean isEStruck = false;
	private static boolean isRStruck = false;
	private static boolean isTStruck = false;
	private static boolean isYStruck = false;
	private static boolean isUStruck = false;
	private static boolean isIStruck = false;
	private static boolean isOStruck = false;
	private static boolean isPStruck = false;
	
	private static boolean isAStruck = false;
	private static boolean isSStruck = false;
	private static boolean isDStruck = false;
	private static boolean isFStruck = false;
	private static boolean isGStruck = false;
	private static boolean isHStruck = false;
	private static boolean isJStruck = false;
	private static boolean isKStruck = false;
	private static boolean isLStruck = false;

	private static boolean isZStruck = false;
	private static boolean isXStruck = false;
	private static boolean isCStruck = false;
	private static boolean isVStruck = false;
	private static boolean isBStruck = false;
	private static boolean isNStruck = false;
	private static boolean isMStruck = false;
          
	private static boolean isKP1Struck = false;
	private static boolean isKP2Struck = false;
	private static boolean isKP3Struck = false;
	private static boolean isKP4Struck = false;
	private static boolean isKP5Struck = false;
	private static boolean isKP6Struck = false;
	private static boolean isKP7Struck = false;
	private static boolean isKP8Struck = false;
	private static boolean isKP9Struck = false;
	private static boolean isKP0Struck = false;
    
	private static boolean is1Struck = false;
	private static boolean is2Struck = false;
	private static boolean is3Struck = false;
	private static boolean is4Struck = false;
	private static boolean is5Struck = false;
	private static boolean is6Struck = false;
	private static boolean is7Struck = false;
	private static boolean is8Struck = false;
	private static boolean is9Struck = false;
	private static boolean is0Struck = false;
   	
	//mouse states
	
	private static boolean isLMouseStruck = false;
	private static boolean isRMouseStruck = false;
	private static boolean isMMouseStruck = false;
	private static boolean isM4Struck = false;
	private static boolean isM5Struck = false;

	//gamepad states
	//...	
	
	public int getKeyboardKey(int key) {
		
		return glfwGetKey(glfwWindow , key);
		
	}

	public int getMouseKey(int key) { 
		
		return glfwGetMouseButton(glfwWindow , key);
		
	}
	
	boolean keyboardPressed(int key) {
		
		return glfwGetKey(glfwWindow , key) == GLFW_PRESS;
		
	}

	boolean keyboardReleased(int key) { 
		
		return glfwGetKey(glfwWindow , key) == GLFW_RELEASE;
		
	}
	
	boolean mousePressed(int key) { 
	
		return glfwGetMouseButton(glfwWindow ,  key) == GLFW_PRESS;
		
	}

	boolean mouseReleased(int key) { 
	
		return glfwGetMouseButton(glfwWindow ,  key) == GLFW_RELEASE;
		
	}
	
	public boolean keyboardStruck(int key) { 
		
		return switch(key) { 
			
			case GLFW_KEY_UP -> isUpStruck;
			case GLFW_KEY_DOWN -> isDownStruck;
			case GLFW_KEY_LEFT -> isLeftStruck;
			case GLFW_KEY_RIGHT -> isRightStruck;
			case GLFW_KEY_TAB -> isTabStruck;
			case GLFW_KEY_CAPS_LOCK -> isCapsStruck;
			case GLFW_KEY_LEFT_SHIFT -> isLShiftStruck;
			case GLFW_KEY_LEFT_CONTROL -> isLControlStruck;
			case GLFW_KEY_LEFT_ALT -> isLAltStruck;
			case GLFW_KEY_BACKSPACE -> isBackSpaceStruck;
			case GLFW_KEY_ENTER -> isEnterStruck;
			case GLFW_KEY_RIGHT_SHIFT -> isRShiftStruck;
			case GLFW_KEY_RIGHT_ALT -> isRAltStruck;
			case GLFW_KEY_RIGHT_CONTROL -> isRControlStruck;
			case GLFW_KEY_SPACE -> isSpaceStruck;
			case GLFW_KEY_Q -> isQStruck;
			case GLFW_KEY_W -> isWStruck;
			case GLFW_KEY_E -> isEStruck;
			case GLFW_KEY_R -> isRStruck;
			case GLFW_KEY_T -> isTStruck;
			case GLFW_KEY_Y -> isYStruck;
			case GLFW_KEY_U -> isUStruck;
			case GLFW_KEY_I -> isIStruck;
			case GLFW_KEY_O -> isOStruck;
			case GLFW_KEY_P -> isPStruck;
			case GLFW_KEY_A -> isAStruck;
			case GLFW_KEY_S -> isSStruck;
			case GLFW_KEY_D -> isDStruck;
			case GLFW_KEY_F -> isFStruck;
			case GLFW_KEY_G -> isGStruck;
			case GLFW_KEY_H -> isHStruck;
			case GLFW_KEY_J -> isJStruck;
			case GLFW_KEY_K -> isKStruck;
			case GLFW_KEY_L -> isLStruck;
			case GLFW_KEY_Z -> isZStruck;
			case GLFW_KEY_X -> isXStruck;
			case GLFW_KEY_C -> isCStruck;
			case GLFW_KEY_V -> isVStruck;
			case GLFW_KEY_B -> isBStruck;
			case GLFW_KEY_N -> isNStruck;
			case GLFW_KEY_M -> isMStruck;
			case GLFW_KEY_KP_0 -> isKP0Struck;
			case GLFW_KEY_KP_1 -> isKP1Struck;
			case GLFW_KEY_KP_2 -> isKP2Struck;
			case GLFW_KEY_KP_3 -> isKP3Struck;
			case GLFW_KEY_KP_4 -> isKP4Struck;
			case GLFW_KEY_KP_5 -> isKP5Struck;
			case GLFW_KEY_KP_6 -> isKP6Struck;
			case GLFW_KEY_KP_7 -> isKP7Struck;
			case GLFW_KEY_KP_8 -> isKP8Struck;
			case GLFW_KEY_KP_9 -> isKP9Struck;
			case GLFW_KEY_0 -> is0Struck;
			case GLFW_KEY_1 -> is1Struck;
			case GLFW_KEY_2 -> is2Struck;
			case GLFW_KEY_3 -> is3Struck;
			case GLFW_KEY_4 -> is4Struck;
			case GLFW_KEY_5 -> is5Struck;
			case GLFW_KEY_6 -> is6Struck;
			case GLFW_KEY_7 -> is7Struck;
			case GLFW_KEY_8 -> is8Struck;
			case GLFW_KEY_9 -> is9Struck;			
			default -> throw new IllegalArgumentException(key + " is not a valid GLFW key code");
			
		};
		
	}
	
	public boolean mouseStruck(int key) { 
		
		return switch(key) {
		
			case GLFW_MOUSE_BUTTON_LEFT -> isLMouseStruck;
			case GLFW_MOUSE_BUTTON_RIGHT -> isRMouseStruck;
			case GLFW_MOUSE_BUTTON_MIDDLE -> isMMouseStruck;
			case GLFW_MOUSE_BUTTON_4 -> isM4Struck;
			case GLFW_MOUSE_BUTTON_5 -> isM5Struck;		
			default -> throw new IllegalArgumentException(key + " is not a valid GLFW Mouse Key Code");
		
		};
		
	}
	
	public void releaseKeys() {

		isUpStruck = false;
		isLeftStruck = false;
		isDownStruck = false;
		isRightStruck = false;
		
		isTabStruck = false;
		isCapsStruck = false;
		isLShiftStruck = false;
		isLControlStruck = false;
		isLAltStruck = false;
		isBackSpaceStruck = false;
		isEnterStruck = false;
		isRShiftStruck = false;
		isRAltStruck = false;
		isRControlStruck = false;
		
		isSpaceStruck = false;
	   	
		isQStruck = false;
		isWStruck = false;
		isEStruck = false;
		isRStruck = false;
		isTStruck = false;
		isYStruck = false;
		isUStruck = false;
		isIStruck = false;
		isOStruck = false;
		isPStruck = false;
		
		isAStruck = false;
		isSStruck = false;
		isDStruck = false;
		isFStruck = false;
		isGStruck = false;
		isHStruck = false;
		isJStruck = false;
		isKStruck = false;
		isLStruck = false;

		isZStruck = false;
		isXStruck = false;
		isCStruck = false;
		isVStruck = false;
		isBStruck = false;
		isNStruck = false;
		isMStruck = false;
	    		
		isKP1Struck = false;
		isKP2Struck = false;
		isKP3Struck = false;
		isKP4Struck = false;
		isKP5Struck = false;
		isKP6Struck = false;
		isKP7Struck = false;
		isKP8Struck = false;
		isKP9Struck = false;
		isKP0Struck = false;
		
		is1Struck = false;
		is2Struck = false;
		is3Struck = false;
		is4Struck = false;
		is5Struck = false;
		is6Struck = false;
		is7Struck = false;
		is8Struck = false;
		is9Struck = false;
		is0Struck = false;
	    
		isLMouseStruck = false;
		isRMouseStruck = false;
		isMMouseStruck = false;
		isM4Struck = false;
		isM5Struck = false;
		
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

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

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

    							if(keyboardPressed(GLFW_KEY_LEFT_SHIFT)) glPolygonMode(GL_FRONT_AND_BACK , GL_FILL);
    							else if (keyboardPressed(GLFW_KEY_LEFT_CONTROL)) {
    								
    								MemoryAllocationReport report = (address , memory , threadID , threadName , element) ->{
    								    
    							    	System.out.println("At Address: " + address + "; " + memory + " bytes");
    							    	
    							    };
    							    
    							    memReport(report);
    							    
    							}

    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F2:

    					switch(action){

    						case GLFW_PRESS:break;

    						case GLFW_RELEASE:

    							if(keyboardPressed(GLFW_KEY_LEFT_SHIFT)) glPolygonMode(GL_FRONT_AND_BACK , GL_LINE);    							
    							break;

    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F3:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F4:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

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

    							engine.mg_toggleMultiplayerUI();
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

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F20:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F21:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F22:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F23:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F24:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F25:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;
    				//XXX
    				case GLFW_KEY_GRAVE_ACCENT:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_1:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is1Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_2:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is2Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_3:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is3Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_4:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is4Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_5:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is5Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_6:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is6Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_7:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is7Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_8:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is8Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_9:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is9Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_0:

    					switch(action){

    						case GLFW_PRESS:
    							
    							is0Struck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_MINUS:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_EQUAL:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_BACKSPACE:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isBackSpaceStruck = true;
    			    			nk_input_key(NuklearContext, NK_KEY_BACKSPACE, press);
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:

    			    			nk_input_key(NuklearContext, NK_KEY_BACKSPACE, press);
    							break;

    					}

    					break;

    				case GLFW_KEY_INSERT:

    					switch(action){
    					
    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_HOME:

    					switch(action){

    						case GLFW_PRESS:

    			    			nk_input_key(NuklearContext, NK_KEY_TEXT_START, press);
    			    			nk_input_key(NuklearContext, NK_KEY_SCROLL_START, press);
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_PAGE_UP:

    					switch(action){

    						case GLFW_PRESS:

    							nk_input_key(NuklearContext, NK_KEY_SCROLL_UP, press);
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_NUM_LOCK:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

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
    							isTabStruck = true;    							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_Q:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isQStruck = true;
    							break;
    							    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_W:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isWStruck = true;
    							if (keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_attemptTransform(0 , 1);
    							break;

    						case GLFW_RELEASE:break;

    						case GLFW_REPEAT:
    							
    							if(keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_attemptTransform(0 , 1);    							
    							break;

    					}

    					break;

    				case GLFW_KEY_E:

    					switch(action){

    						case GLFW_PRESS:

    							isEStruck = true;
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_R:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isRStruck = true;	
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;
    					
    				case GLFW_KEY_T:

    					switch(action){
    					
    						case GLFW_PRESS:
    							
    							isTStruck = true;
    							if(keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_textureActiveObject();
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_Y:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isYStruck = true;    							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_U:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isUStruck = true;    							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_I:

    					switch(action){

    						case GLFW_PRESS:

    							isIStruck = true;    							
    							if(keyboardPressed(GLFW_KEY_LEFT_SHIFT) && keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_loadItem();
    							else if (keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_newItem();    							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_O:

    					switch(action){

    						case GLFW_PRESS:

    							isOStruck = true;    							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_P:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isPStruck = true;
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_LEFT_BRACKET:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_RIGHT_BRACKET:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_BACKSLASH:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_DELETE:

    					switch(action){

    						case GLFW_PRESS:

    							if(keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_removeActiveQuad();
    			    			nk_input_key(NuklearContext, NK_KEY_DEL, press);
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_END:

    					switch(action){

    						case GLFW_PRESS:

    			    			nk_input_key(NuklearContext, NK_KEY_TEXT_END, press);
    			    			nk_input_key(NuklearContext, NK_KEY_SCROLL_END, press);
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_PAGE_DOWN:

    					switch(action){

    						case GLFW_PRESS:

    							if(keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_deleteScene();
    							nk_input_key(NuklearContext, NK_KEY_SCROLL_DOWN, press);
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				//XXX

    				case GLFW_KEY_CAPS_LOCK:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isCapsStruck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;
    				case GLFW_KEY_A:

    					switch(action){

    						case GLFW_PRESS:

    							isAStruck = true;    							
    							if (keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_attemptTransform(-1 , 0);
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:

    							if (keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_attemptTransform(-1 , 0);
    							break;

    					}

    					break;

    				case GLFW_KEY_S:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isSStruck = true;    							
    							if(keyboardPressed(GLFW_KEY_LEFT_SHIFT) && keyboardPressed(GLFW_KEY_LEFT_ALT)) engine.e_snapSelectionArea();
    							if(keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_attemptTransform(0 , -1);
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:

    							if(keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_attemptTransform(0 , -1);								
    							break;

    					}

    					break;

    				case GLFW_KEY_D:

    					switch(action){

    						case GLFW_PRESS:

    							isDStruck = true;
    							if(keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_attemptTransform(1 , 0);								
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_F:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isFStruck = true;
    							if(keyboardPressed(GLFW_KEY_LEFT_SHIFT) && keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_loadEntity();
    							else if(keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_newEntity();   							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_G:

    					switch(action){

    						case GLFW_PRESS:

    							isGStruck = true;
    							if(keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_addQuad();
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_H:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isHStruck = true;
    							if(keyboardPressed(GLFW_KEY_LEFT_CONTROL) && keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_loadStatic();
    							else if(keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_addStatic();
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_J:

    					switch(action){

    						case GLFW_PRESS:

    							isJStruck = true;
    							if(keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_addCollider();
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_K:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isKStruck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_L:

    					switch(action){

    						case GLFW_PRESS:

    							isLStruck = true;
    							if(keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_loadLevel();
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_SEMICOLON:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_APOSTROPHE:

    					switch(action){

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_ENTER:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isEnterStruck = true;
    			    			nk_input_key(NuklearContext, NK_KEY_ENTER, press);
    			    			engine.e_returnConsole();
    			    			engine.mg_enterTextChat();
    			    			DialogUtils.acceptLast();
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				//XXX
    				case GLFW_KEY_LEFT_SHIFT:

    					switch(action){

    						case GLFW_PRESS:

    							nk_input_key(NuklearContext, NK_KEY_SHIFT, press);
    							isLShiftStruck = true;    							
    							break;

    						case GLFW_RELEASE:
    							
    							if(keyboardPressed(GLFW_KEY_LEFT_ALT)) DialogUtils.acceptLast();    							
    							break;

    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_Z:

    					switch(action){

    						case GLFW_PRESS:

    							isZStruck = true;    							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_X:

    					switch(action){

    						case GLFW_PRESS:

    							isXStruck = true;
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_C:

    					switch(action){

    						case GLFW_PRESS:

    							isCStruck = true;
    							if(keyboardPressed(GLFW_KEY_LEFT_CONTROL) && keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_setActiveQuadColor();
    							else if (keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_copyActiveObject();
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_V:

    					switch(action){

    						case GLFW_PRESS:

    							isVStruck = true;
    							if(keyboardPressed(GLFW_KEY_LEFT_CONTROL) && keyboardPressed(GLFW_KEY_LEFT_SHIFT)) engine.e_filterActiveObjectColor();
    							else if (keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_pasteCopyAtCursor();
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_B:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isBStruck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_N:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isNStruck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_M:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isMStruck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

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

    						case GLFW_PRESS:break;
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;
    				
    				case GLFW_KEY_RIGHT_SHIFT:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isRShiftStruck = true;
    							nk_input_key(NuklearContext, NK_KEY_SHIFT, press);
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_UP:

    					switch(action){

    						case GLFW_PRESS:

    							isUpStruck = true;
    			    			nk_input_key(NuklearContext, NK_KEY_UP, press);    			    			
    			    			if (keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_getEditor().moveSelectionAreaUpperFace(1);
    							break;

    						case GLFW_RELEASE:break;

    						case GLFW_REPEAT:

    							break;

    					}

    					break;

    				case GLFW_KEY_LEFT:

    					switch(action){

    						case GLFW_PRESS:

    							if (keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_getEditor().moveSelectionAreaLeftFace(1);							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_DOWN:

    					switch(action){

    						case GLFW_PRESS:

    							isDownStruck = true;
    			    			nk_input_key(NuklearContext, NK_KEY_DOWN, press);

    			    			if (keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_getEditor().moveSelectionAreaLowerFace(1);    			    			
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_RIGHT:

    					switch(action){

    						case GLFW_PRESS:

    							isRightStruck = true;
    							if (keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.e_getEditor().moveSelectionAreaRightFace(1);    							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;
    				//XXX
    				case GLFW_KEY_LEFT_CONTROL:

    					switch(action){

    						case GLFW_PRESS:

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

    		    				nk_input_key(NuklearContext, NK_KEY_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
    		    				nk_input_key(NuklearContext, NK_KEY_COPY, false);
    		    				nk_input_key(NuklearContext, NK_KEY_PASTE, false);
    		    				nk_input_key(NuklearContext, NK_KEY_CUT, false);
    		    				nk_input_key(NuklearContext, NK_KEY_SHIFT, false);
       							break;

    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_LEFT_ALT:

    					switch(action){

    						case GLFW_PRESS:

    							isLAltStruck = true;    							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_SPACE:

    					switch(action){

    						case GLFW_PRESS:

    							isSpaceStruck = true;    							
    							break;

    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_RIGHT_ALT:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isRAltStruck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;

    				case GLFW_KEY_RIGHT_CONTROL:

    					switch(action){

    						case GLFW_PRESS:
    							
    							isRControlStruck = true;
    							break;
    							
    						case GLFW_RELEASE:break;
    						case GLFW_REPEAT:break;

    					}

    					break;


        				//XXX
        				case GLFW_KEY_KP_0:

        					switch(action){

        						case GLFW_PRESS:
        							
        							isKP0Struck = true;
        							break;
        							
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_1:

        					switch(action){

        						case GLFW_PRESS:
        							
        							isKP1Struck = true;
        							break;
        							
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_2:

        					switch(action){

        						case GLFW_PRESS:

        							isKP2Struck = true;
        							break;

        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_3:

        					switch(action){

        						case GLFW_PRESS:
        							
        							isKP3Struck = true;
        							break;
        							
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_4:

        					switch(action){

        						case GLFW_PRESS:

        							isKP4Struck = true;        							
        							break;

        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_5:

        					switch(action){

        						case GLFW_PRESS:
        							
        							isKP5Struck = true;
        							break;
        							
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_6:

        					switch(action){

        						case GLFW_PRESS:

        							isKP6Struck = true;        							
        							break;

        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_7:

        					switch(action){

        						case GLFW_PRESS:

        							isKP7Struck = true;
        							break;
        							
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_8:

        					switch(action){

        						case GLFW_PRESS:

        							isKP8Struck = true;        							
        							break;

        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_9:

        					switch(action){

        						case GLFW_PRESS:
        							
        							isKP9Struck = true;
        							break;
        							
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_DIVIDE:

        					switch(action){

        						case GLFW_PRESS:break;
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_MULTIPLY:

        					switch(action){

        						case GLFW_PRESS:break;
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_SUBTRACT:

        					switch(action){

        						case GLFW_PRESS:break;
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_ADD:

        					switch(action){

        						case GLFW_PRESS:break;
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_ENTER:

        					switch(action){

        						case GLFW_PRESS:break;
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

        					}

        					break;

        				case GLFW_KEY_KP_DECIMAL:

        					switch(action){

        						case GLFW_PRESS:break;
        						case GLFW_RELEASE:break;
        						case GLFW_REPEAT:break;

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
		    						        
		    						isRMouseStruck = true;
		    						glfwGetCursorPos(glfwWindow, startingX , startingY);
		    						pressWorldX = (float)getSCToWCForX(startingX.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());
		    						pressWorldY = (float)getSCToWCForY(startingY.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());		    			
		    						
		    						break;
		    						
		    					case GLFW_RELEASE:

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
		    						
		    					case GLFW_REPEAT:break;
		    					
	    					}
	    					
	    					break;
	    					
	    				case GLFW_MOUSE_BUTTON_LEFT:
	    					
    	    				nkButton = NK_BUTTON_LEFT;
    	    				
	    					switch(action) {
	    					
		    					case GLFW_PRESS:
		            				
		            				glfwGetCursorPos(glfwWindow, startingX , startingY);
		    						pressWorldX = (float)getSCToWCForX(startingX.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());
		    						pressWorldY = (float)getSCToWCForY(startingY.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());		    			

	            					isLMouseStruck = true;
		    						if(!keyboardPressed(GLFW_KEY_LEFT_CONTROL)) {

		            					if(nk_window_is_any_hovered(NuklearContext)) break;

		            					boolean selectedSame = engine.e_select((float)pressWorldX , (float) pressWorldY);
		            					if(selectedSame && engine.e_cursorState() == CursorState.DRAGGING) engine.e_setCursorState(CursorState.SELECTABLE);
		            					else if (selectedSame) engine.e_setCursorState(CursorState.DRAGGING);
		            								       		            							            					
		            				}	
		    						
		    						break;
		    						
		    					case GLFW_RELEASE:
		    						
		    						glfwGetCursorPos(glfwWindow , newX , newY);
		            				
		    						releaseWorldX = (float)getSCToWCForX(newX.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());
		    						releaseWorldY = (float)getSCToWCForY(newY.get(0) , winWidth.get(0) , winHeight.get(0), engine.getCamera());
		            				
		    						if(nk_window_is_any_hovered(NuklearContext)) break;
		    						
		            				//click-drag selection box        				
		            				if(keyboardPressed(GLFW_KEY_LEFT_SHIFT)) {
		            					        					
		            					float xDim = (float) releaseWorldX - pressWorldX;
		            					float yDim = (float) releaseWorldY - pressWorldY;
		    
		            					engine.e_getEditor().moveSelectionAreaTo((float) pressWorldX, (float)pressWorldY);
		            					engine.e_getEditor().setSelectionAreaDimensions(xDim, yDim);
		            							            					
		            				} 
	            				
		            				if(Engine.STATE == RuntimeState.EDITOR && keyboardPressed(GLFW_KEY_LEFT_SHIFT) && keyboardPressed(GLFW_KEY_LEFT_CONTROL)) {
		            					
		            					engine.editor.say("Cursor X: " + releaseWorldX);
		            					engine.editor.say("Cursor Y: " + releaseWorldY);
		            					
		            				}
		            				
		    						break;
		    						
		    						
		    					case GLFW_REPEAT:break;
	    					
	    					}			
	    					
	    					break;
	    					
	    				case GLFW_MOUSE_BUTTON_MIDDLE:
	    					
	    					nkButton = NK_BUTTON_MIDDLE;
	    					
	    					switch(action) {
		    					
		    					case GLFW_PRESS:
		    						
		    						isMMouseStruck = true;		    						
		    						break;
		    						
		    					case GLFW_RELEASE:break;	
		    					case GLFW_REPEAT:break;
		    					
	    					}
	    					
	    					break;

	    				case GLFW_MOUSE_BUTTON_4:
	    					
	    					switch(action) {
	    					
		    					case GLFW_PRESS:
		    						
		    						isM4Struck = true;		    						
		    						break;
		    						
		    					case GLFW_RELEASE:break;  
		    					case GLFW_REPEAT:break;
		    					
	    					}
	    					
	    					break;

	    				case GLFW_MOUSE_BUTTON_5:
	    					
	    					switch(action) {
	    					
		    					case GLFW_PRESS:
		    						
		    						isM5Struck = true;		    						
		    						break;
		    						
		    					case GLFW_RELEASE:break;
		    					case GLFW_REPEAT:break;
		    					
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
    					
    					if(keyboardPressed(GLFW_KEY_LEFT_CONTROL))engine.getCamera().scaleCamera(6 * (-(float) yoffset));
    					else engine.getCamera().scaleCamera(-(float) yoffset);
    					
    				}

    			} else if (yoffset < 0) {

    				if(Engine.STATE == RuntimeState.EDITOR && engine.e_getEditor().getState() == EditorMode.BUILD_MODE) {
    					
    					if (keyboardPressed(GLFW_KEY_LEFT_CONTROL)) engine.getCamera().scaleCamera(6 * (-(float) yoffset)); 
    					else engine.getCamera().scaleCamera(-(float) yoffset);
    					
    				}

    			}

    		});
    		
    		glfwSetCursorEnterCallback(glfwWindow , (window , entered) -> {});
    		
   		glfwSetCursorPosCallback(glfwWindow , (window, xpos, ypos) -> nk_input_motion(NuklearContext, (int)xpos, (int)ypos));   		
   		glfwSetInputMode (glfwWindow , GLFW_CURSOR ,GLFW_CURSOR_NORMAL);
    	
    }

}