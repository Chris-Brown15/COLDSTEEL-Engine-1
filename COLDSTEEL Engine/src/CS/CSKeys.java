package CS;

import static org.lwjgl.glfw.GLFW.*;

/**
 * This class holds constants related to keyboard and mouse keys.
 * <br> <br>
 * GLFW models keyboard, mouse, and gamepad keys as integers, but the values of these integers is semi random. All GLFW ensures,
 * is that each key for each peripheral gets a unique value. The issue is that these numbers are larger than they need to be,
 * so sending these values directly over the wire leads to exessive bits being needed. I remap these key codes to smaller ones that 
 * can be represented with seven bits for the advantage of Networking. 
 * 
 * @author Chris Brown
 *
 */
public final class CSKeys {

	private static final int[] glfwKeys = new int[128];
	private static final int[] glfwMouse = new int[12];
	private static final int[] glfwGamepad = new int[17];
		
	public static final byte
		CS_KEY_SPACE         = 0, 
		CS_KEY_APOSTROPHE    = 1, 
		CS_KEY_COMMA         = 2, 
		CS_KEY_MINUS         = 3, 
		CS_KEY_PERIOD        = 4, 
		CS_KEY_SLASH         = 5, 
		CS_KEY_0             = 6, 
		CS_KEY_1             = 7, 
		CS_KEY_2             = 8, 
		CS_KEY_3             = 9, 
		CS_KEY_4             = 10, 
		CS_KEY_5             = 11, 
		CS_KEY_6             = 12, 
		CS_KEY_7             = 13, 
		CS_KEY_8             = 14, 
		CS_KEY_9             = 15, 
		CS_KEY_SEMICOLON     = 16, 
		CS_KEY_EQUAL         = 17, 
		CS_KEY_A             = 18, 
		CS_KEY_B             = 19, 
		CS_KEY_C             = 20, 
		CS_KEY_D             = 21, 
		CS_KEY_E             = 22, 
		CS_KEY_F             = 23, 
		CS_KEY_G             = 24, 
		CS_KEY_H             = 25, 
		CS_KEY_I             = 26, 
		CS_KEY_J             = 27, 
		CS_KEY_K             = 28, 
		CS_KEY_L             = 29, 
		CS_KEY_M             = 30, 
		CS_KEY_N             = 31, 
		CS_KEY_O             = 32, 
		CS_KEY_P             = 33, 
		CS_KEY_Q             = 34, 
		CS_KEY_R             = 35, 
		CS_KEY_S             = 36, 
		CS_KEY_T             = 37, 
		CS_KEY_U             = 38, 
		CS_KEY_V             = 39, 
		CS_KEY_W             = 40, 
		CS_KEY_X             = 41, 
		CS_KEY_Y             = 42, 
		CS_KEY_Z             = 43, 
		CS_KEY_LEFT_BRACKET  = 44, 
		CS_KEY_BACKSLASH     = 45, 
		CS_KEY_RIGHT_BRACKET = 46, 
		CS_KEY_GRAVE_ACCENT  = 47,
		CS_KEY_ESCAPE        = 48,
		CS_KEY_ENTER         = 49,
		CS_KEY_TAB           = 50,
		CS_KEY_BACKSPACE     = 51,
		CS_KEY_INSERT        = 52,
		CS_KEY_DELETE        = 53,
		CS_KEY_RIGHT         = 54,
		CS_KEY_LEFT          = 55,
		CS_KEY_DOWN          = 56,
		CS_KEY_UP            = 57,
		CS_KEY_PAGE_UP       = 58,
		CS_KEY_PAGE_DOWN     = 59,
		CS_KEY_HOME          = 60,
		CS_KEY_END           = 61,		
		CS_KEY_F1            = 62,
		CS_KEY_F2            = 63,
		CS_KEY_F3            = 64,
		CS_KEY_F4            = 65,
		CS_KEY_F5            = 66,
		CS_KEY_F6            = 67,
		CS_KEY_F7            = 68,
		CS_KEY_F8            = 69,
		CS_KEY_F9            = 70,
		CS_KEY_F10           = 71,
		CS_KEY_F11           = 72,
		CS_KEY_F12           = 73,
		CS_KEY_F13           = 74,
		CS_KEY_F14           = 75,
		CS_KEY_F15           = 76,
		CS_KEY_F16           = 77,
		CS_KEY_F17           = 78,
		CS_KEY_F18           = 79,
		CS_KEY_F19           = 80,
		CS_KEY_F20           = 81,
		CS_KEY_F21           = 82,
		CS_KEY_F22           = 83,
		CS_KEY_F23           = 84,
		CS_KEY_F24           = 85,
		CS_KEY_F25           = 86,
		CS_KEY_KP_0          = 87,
		CS_KEY_KP_1          = 88,
		CS_KEY_KP_2          = 89,
		CS_KEY_KP_3          = 90,
		CS_KEY_KP_4          = 91,
		CS_KEY_KP_5          = 92,
		CS_KEY_KP_6          = 93,
		CS_KEY_KP_7          = 94,
		CS_KEY_KP_8          = 95,
		CS_KEY_KP_9          = 96,
		CS_KEY_KP_DECIMAL    = 97,
		CS_KEY_KP_DIVIDE     = 98,
		CS_KEY_KP_MULTIPLY   = 99,
		CS_KEY_KP_SUBTRACT   = 100,
		CS_KEY_KP_ADD        = 101,
		CS_KEY_KP_ENTER      = 102,
		CS_KEY_KP_EQUAL      = 103,
		CS_KEY_LEFT_SHIFT    = 104,
		CS_KEY_LEFT_CONTROL  = 105,
		CS_KEY_LEFT_ALT      = 106,
		CS_KEY_RIGHT_SHIFT   = 107,
		CS_KEY_RIGHT_CONTROL = 108,
		CS_KEY_RIGHT_ALT     = 109,
		
		CS_MOUSE_BUTTON_1      = 0,
		CS_MOUSE_BUTTON_2      = 1,
		CS_MOUSE_BUTTON_3      = 2,
		CS_MOUSE_BUTTON_4      = 3,
		CS_MOUSE_BUTTON_5      = 4,
		CS_MOUSE_BUTTON_6      = 5,
		CS_MOUSE_BUTTON_7      = 6,
		CS_MOUSE_BUTTON_8      = 7,
		CS_MOUSE_BUTTON_LEFT   = 8,
		CS_MOUSE_BUTTON_RIGHT  = 9,
		CS_MOUSE_BUTTON_MIDDLE = 10
	;

	static {
		
		glfwKeys[CS_KEY_SPACE] = GLFW_KEY_SPACE;		
		glfwKeys[CS_KEY_APOSTROPHE] = GLFW_KEY_APOSTROPHE;
		glfwKeys[CS_KEY_COMMA] = GLFW_KEY_COMMA;
		glfwKeys[CS_KEY_MINUS] = GLFW_KEY_MINUS;
		glfwKeys[CS_KEY_PERIOD] = GLFW_KEY_PERIOD;
		glfwKeys[CS_KEY_SLASH] = GLFW_KEY_SLASH;
		glfwKeys[CS_KEY_0] = GLFW_KEY_0;
		glfwKeys[CS_KEY_1] = GLFW_KEY_1;
		glfwKeys[CS_KEY_2] = GLFW_KEY_2;
		glfwKeys[CS_KEY_3] = GLFW_KEY_3;
		glfwKeys[CS_KEY_4] = GLFW_KEY_4;
		glfwKeys[CS_KEY_5] = GLFW_KEY_5;
		glfwKeys[CS_KEY_6] = GLFW_KEY_6;
		glfwKeys[CS_KEY_7] = GLFW_KEY_7;
		glfwKeys[CS_KEY_8] = GLFW_KEY_8;
		glfwKeys[CS_KEY_9] = GLFW_KEY_9;
		glfwKeys[CS_KEY_SEMICOLON] = GLFW_KEY_SEMICOLON;
		glfwKeys[CS_KEY_EQUAL] = GLFW_KEY_EQUAL;
		glfwKeys[CS_KEY_A] = GLFW_KEY_A;
		glfwKeys[CS_KEY_B] = GLFW_KEY_B;
		glfwKeys[CS_KEY_C] = GLFW_KEY_C;
		glfwKeys[CS_KEY_D] = GLFW_KEY_D;
		glfwKeys[CS_KEY_E] = GLFW_KEY_E;
		glfwKeys[CS_KEY_F] = GLFW_KEY_F;
		glfwKeys[CS_KEY_G] = GLFW_KEY_G;
		glfwKeys[CS_KEY_H] = GLFW_KEY_H;
		glfwKeys[CS_KEY_I] = GLFW_KEY_I;
		glfwKeys[CS_KEY_J] = GLFW_KEY_J;
		glfwKeys[CS_KEY_K] = GLFW_KEY_K;
		glfwKeys[CS_KEY_L] = GLFW_KEY_L;
		glfwKeys[CS_KEY_M] = GLFW_KEY_M;
		glfwKeys[CS_KEY_N] = GLFW_KEY_N;
		glfwKeys[CS_KEY_O] = GLFW_KEY_O;
		glfwKeys[CS_KEY_P] = GLFW_KEY_P;
		glfwKeys[CS_KEY_Q] = GLFW_KEY_Q;
		glfwKeys[CS_KEY_R] = GLFW_KEY_R;
		glfwKeys[CS_KEY_S] = GLFW_KEY_S;
		glfwKeys[CS_KEY_T] = GLFW_KEY_T;
		glfwKeys[CS_KEY_U] = GLFW_KEY_U;
		glfwKeys[CS_KEY_V] = GLFW_KEY_V;
		glfwKeys[CS_KEY_W] = GLFW_KEY_W;
		glfwKeys[CS_KEY_X] = GLFW_KEY_X;
		glfwKeys[CS_KEY_Y] = GLFW_KEY_Y;
		glfwKeys[CS_KEY_Z] = GLFW_KEY_Z;
		glfwKeys[CS_KEY_LEFT_BRACKET] = GLFW_KEY_LEFT_BRACKET;
		glfwKeys[CS_KEY_BACKSLASH] = GLFW_KEY_BACKSLASH;
		glfwKeys[CS_KEY_RIGHT_BRACKET] = GLFW_KEY_RIGHT_BRACKET;
		glfwKeys[CS_KEY_GRAVE_ACCENT] = GLFW_KEY_GRAVE_ACCENT;		                                             
		glfwKeys[CS_KEY_ESCAPE] = GLFW_KEY_ESCAPE;
		glfwKeys[CS_KEY_ENTER] = GLFW_KEY_ENTER;
		glfwKeys[CS_KEY_TAB] = GLFW_KEY_TAB;
		glfwKeys[CS_KEY_BACKSPACE] = GLFW_KEY_BACKSPACE;
		glfwKeys[CS_KEY_INSERT] = GLFW_KEY_INSERT;
		glfwKeys[CS_KEY_DELETE] = GLFW_KEY_DELETE;
		glfwKeys[CS_KEY_RIGHT] = GLFW_KEY_RIGHT;
		glfwKeys[CS_KEY_LEFT] = GLFW_KEY_LEFT;
		glfwKeys[CS_KEY_DOWN] = GLFW_KEY_DOWN;
		glfwKeys[CS_KEY_UP] = GLFW_KEY_UP;
		glfwKeys[CS_KEY_PAGE_UP] = GLFW_KEY_PAGE_UP;
		glfwKeys[CS_KEY_PAGE_DOWN] = GLFW_KEY_PAGE_DOWN;
		glfwKeys[CS_KEY_HOME] = GLFW_KEY_HOME;
		glfwKeys[CS_KEY_END] = GLFW_KEY_END;
		glfwKeys[CS_KEY_F1] = GLFW_KEY_F1;
		glfwKeys[CS_KEY_F2] = GLFW_KEY_F2;
		glfwKeys[CS_KEY_F3] = GLFW_KEY_F3;
		glfwKeys[CS_KEY_F4] = GLFW_KEY_F4;
		glfwKeys[CS_KEY_F5] = GLFW_KEY_F5;
		glfwKeys[CS_KEY_F6] = GLFW_KEY_F6;
		glfwKeys[CS_KEY_F7] = GLFW_KEY_F7;
		glfwKeys[CS_KEY_F8] = GLFW_KEY_F8;
		glfwKeys[CS_KEY_F9] = GLFW_KEY_F9;
		glfwKeys[CS_KEY_F10] = GLFW_KEY_F10;
		glfwKeys[CS_KEY_F11] = GLFW_KEY_F11;
		glfwKeys[CS_KEY_F12] = GLFW_KEY_F12;
		glfwKeys[CS_KEY_F13] = GLFW_KEY_F13;
		glfwKeys[CS_KEY_F14] = GLFW_KEY_F14;
		glfwKeys[CS_KEY_F15] = GLFW_KEY_F15;
		glfwKeys[CS_KEY_F16] = GLFW_KEY_F16;
		glfwKeys[CS_KEY_F17] = GLFW_KEY_F17;
		glfwKeys[CS_KEY_F18] = GLFW_KEY_F18;
		glfwKeys[CS_KEY_F19] = GLFW_KEY_F19;
		glfwKeys[CS_KEY_F20] = GLFW_KEY_F20;
		glfwKeys[CS_KEY_F21] = GLFW_KEY_F21;
		glfwKeys[CS_KEY_F22] = GLFW_KEY_F22;
		glfwKeys[CS_KEY_F23] = GLFW_KEY_F23;
		glfwKeys[CS_KEY_F24] = GLFW_KEY_F24;
		glfwKeys[CS_KEY_F25] = GLFW_KEY_F25;
		glfwKeys[CS_KEY_KP_0] = GLFW_KEY_KP_0;
		glfwKeys[CS_KEY_KP_1] = GLFW_KEY_KP_1;
		glfwKeys[CS_KEY_KP_2] = GLFW_KEY_KP_2;
		glfwKeys[CS_KEY_KP_3] = GLFW_KEY_KP_3;
		glfwKeys[CS_KEY_KP_4] = GLFW_KEY_KP_4;
		glfwKeys[CS_KEY_KP_5] = GLFW_KEY_KP_5;
		glfwKeys[CS_KEY_KP_6] = GLFW_KEY_KP_6;
		glfwKeys[CS_KEY_KP_7] = GLFW_KEY_KP_7;
		glfwKeys[CS_KEY_KP_8] = GLFW_KEY_KP_8;
		glfwKeys[CS_KEY_KP_9] = GLFW_KEY_KP_9;
		glfwKeys[CS_KEY_KP_DECIMAL] = GLFW_KEY_KP_DECIMAL;
		glfwKeys[CS_KEY_KP_DIVIDE] = GLFW_KEY_KP_DIVIDE;
		glfwKeys[CS_KEY_KP_MULTIPLY] = GLFW_KEY_KP_MULTIPLY;
		glfwKeys[CS_KEY_KP_SUBTRACT] = GLFW_KEY_KP_SUBTRACT;
		glfwKeys[CS_KEY_KP_ADD] = GLFW_KEY_KP_ADD;
		glfwKeys[CS_KEY_KP_ENTER] = GLFW_KEY_KP_ENTER;
		glfwKeys[CS_KEY_KP_EQUAL] = GLFW_KEY_KP_EQUAL;
		glfwKeys[CS_KEY_LEFT_SHIFT] = GLFW_KEY_LEFT_SHIFT;
		glfwKeys[CS_KEY_LEFT_CONTROL] = GLFW_KEY_LEFT_CONTROL;
		glfwKeys[CS_KEY_LEFT_ALT] = GLFW_KEY_LEFT_ALT;
		glfwKeys[CS_KEY_RIGHT_SHIFT] = GLFW_KEY_RIGHT_SHIFT;
		glfwKeys[CS_KEY_RIGHT_CONTROL] = GLFW_KEY_RIGHT_CONTROL;
		glfwKeys[CS_KEY_RIGHT_ALT] = GLFW_KEY_RIGHT_ALT;		
		
		glfwMouse[CS_MOUSE_BUTTON_1] = GLFW_MOUSE_BUTTON_1;
		glfwMouse[CS_MOUSE_BUTTON_2] = GLFW_MOUSE_BUTTON_2;
		glfwMouse[CS_MOUSE_BUTTON_3] = GLFW_MOUSE_BUTTON_3;
		glfwMouse[CS_MOUSE_BUTTON_4] = GLFW_MOUSE_BUTTON_4;
		glfwMouse[CS_MOUSE_BUTTON_5] = GLFW_MOUSE_BUTTON_5;
		glfwMouse[CS_MOUSE_BUTTON_6] = GLFW_MOUSE_BUTTON_6;
		glfwMouse[CS_MOUSE_BUTTON_7] = GLFW_MOUSE_BUTTON_7;
		glfwMouse[CS_MOUSE_BUTTON_8] = GLFW_MOUSE_BUTTON_8;
		glfwMouse[CS_MOUSE_BUTTON_LEFT] = GLFW_MOUSE_BUTTON_LEFT;
		glfwMouse[CS_MOUSE_BUTTON_RIGHT] = GLFW_MOUSE_BUTTON_RIGHT;
		glfwMouse[CS_MOUSE_BUTTON_MIDDLE] = GLFW_MOUSE_BUTTON_MIDDLE;
				
	}	
	
	/**
	 * Returns the GLFW keycode variant of the given csKey 
	 * 
	 * @param csKey
	 * @return
	 */
	public static int glfwKey(byte csKey) {
		
		return glfwKeys[csKey];		
		
	}
	
	/**
	 * Returns the GLFW keycode variant of the given csKey
	 * 
	 * @param csKey
	 * @return
	 */
	public static int glfwMouse(byte csKey) {
		
		return glfwMouse[csKey];
		
	}
	
	private CSKeys() {}
	
}







































