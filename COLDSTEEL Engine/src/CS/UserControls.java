package CS;

/**
 * This small class contains a set of integers that should map to GLFW keys. The values of the ints will equal some GLFW key, so these values can be 
 * passed into GLFW functions to get the state of the key that int is mapped to.
 * 
 * @author Chris Brown
 *
 */
public abstract class UserControls {

	/**
	 * indices into array of ints representing keybind settings
	 * 
	 */
	public static int
		UP = 0 ,
		LEFT = 1 ,
		DOWN = 2 ,
		RIGHT = 3 ,
		JUMP = 4 ,
		ATTACKI = 5 ,
		ATTACKII = 6 ,
		ATTACKIII = 7 ,
		ATTACKIV = 8 ,
		ATTACKV = 9 ,
		POWERI = 10,
		POWERII = 11,
		POWERIII = 12,
		POWERIV = 13,
		POWERV = 14,
		POWERVI = 15,
		POWERVII = 16,
		POWERIIX = 17,
		POWERIX = 18,
		POWERX = 19,
		INVENTORY = 20 ,
		CHARACTER_SHEET = 21,
		ACTIVATE = 22,
		MAP = 23;

	/**
	 * Call this to assign values to all controls. 
	 * 
	 * @param keycodesInOrder
	 */
	static void set(int...keycodesInOrder) {
		
		UP = keycodesInOrder[0];
		LEFT = keycodesInOrder[1];
		DOWN = keycodesInOrder[2];
		RIGHT = keycodesInOrder[3];
		JUMP = keycodesInOrder[4];
		ATTACKI = keycodesInOrder[5];
		ATTACKII = keycodesInOrder[6];
		ATTACKIII = keycodesInOrder[7];
		ATTACKIV = keycodesInOrder[8];
		ATTACKV = keycodesInOrder[9];
		POWERI = keycodesInOrder[10];
		POWERII = keycodesInOrder[11];
		POWERIII = keycodesInOrder[12];
		POWERIV = keycodesInOrder[13];
		POWERV = keycodesInOrder[14];
		POWERVI = keycodesInOrder[15];
		POWERVII = keycodesInOrder[16];
		POWERIIX = keycodesInOrder[17];
		POWERIX = keycodesInOrder[18];
		POWERX = keycodesInOrder[19];
		INVENTORY = keycodesInOrder[20];
		CHARACTER_SHEET = keycodesInOrder[21];
		ACTIVATE = keycodesInOrder[22];
		MAP = keycodesInOrder[23];
		
	}
	
}