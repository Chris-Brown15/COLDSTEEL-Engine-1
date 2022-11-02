package CS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * This class is used to represent a set of controls. A control is an action which will be trigged by the press of a specified key from a specified peripheral.
 * 
 * 
 * @author Chris Brown
 *
 */
public class Controls {

	public static final int NUMBER_CONTROLS = 24;
	
	public static final byte
		KEYBOARD = 0,
		MOUSE = 1,
		GAMEPAD = 2
	;
		
	/**
	 * Instances of Control will model individual controls the user can bind and rebind. 
	 * Their first variable represents a peripheral type, one of {@code KEYBOARD , MOUSE , or , GAMEPAD}.
	 * Their second variable represents the <b> CS Key Code </b> they model.
	 * 
	 */
	public final record Control(byte ID , byte peripheral , int key) {}
	
	public static final byte 
		UP = 0,
		LEFT = 1,
		DOWN = 2,
		RIGHT = 3,
		JUMP = 4,
		ATTACK1 = 5,
		ATTACK2 = 6,
		ATTACK3 = 7,
		ATTACK4 = 8,
		ATTACK5 = 9,
		POWER1 = 10,
		POWER2 = 11,
		POWER3 = 12,
		POWER4 = 13,
		POWER5 = 14,
		POWER6 = 15,
		POWER7 = 16,
		POWER8 = 17,
		POWER9 = 18,
		POWER10 = 19,
		INVENTORY = 20,
		CHARACTER_SHEET = 21,
		ACTIVATE = 22,
		MAP = 23,
		UNSPECIFIED = -1
		;
		
	
	/**
	 * indices into array of ints representing keybind settings
	 * 
	 */
	public volatile Control 
		up = new Control(UP , (byte)-1, 0) ,
		left = new Control(LEFT , (byte)-1, 0) ,
		down = new Control(DOWN , (byte)-1, 0) ,
		right = new Control(RIGHT , (byte)-1 , 0) ,
		jump = new Control(JUMP , (byte)-1 , 0) ,
		attackI = new Control(ATTACK1 , (byte)-1 , 0) ,
		attackII = new Control(ATTACK2 , (byte)-1 , 0) ,
		attackIII = new Control(ATTACK3 , (byte)-1 , 0) ,
		attackIV = new Control(ATTACK4 , (byte)-1 , 0) ,
		attackV = new Control(ATTACK5 , (byte)-1 , 0) ,
		powerI = new Control(POWER1 , (byte)-1 , 0) ,
		powerII = new Control(POWER2 , (byte)-1 , 0) ,
		powerIII = new Control(POWER3 , (byte)-1 , 0) ,
		powerIV = new Control(POWER4 , (byte)-1 , 0) ,
		powerV = new Control(POWER5 , (byte)-1 , 0) ,
		powerVI = new Control(POWER6 , (byte)-1 , 0) ,
		powerVII = new Control(POWER7 , (byte)-1 , 0) ,
		powerIIX = new Control(POWER8 , (byte)-1 , 0) ,
		powerIX = new Control(POWER9 , (byte)-1 , 0) ,
		powerX = new Control(POWER10 , (byte)-1 , 0) ,
		inventory = new Control(INVENTORY , (byte)-1 , 0) ,
		characterSheet = new Control(CHARACTER_SHEET , (byte)-1 , 0) ,
		activate = new Control(ACTIVATE , (byte)-1 , 0) ,
		map = new Control(MAP , (byte)-1 , 0)
	;
	
	private final ConcurrentHashMap<String , Control> OTHER_CONTROLS = new ConcurrentHashMap<String , Control>();

	/**
	 * Call this to assign values to all controls. 
	 * 
	 * @param keycodesInOrder
	 */
	public synchronized void set(int[]...controls) {
		
		up = new Control(UP , (byte) controls[0][0] , controls[0][1]);
		left = new Control(LEFT , (byte) controls[1][0] , controls[1][1]);
		down = new Control(DOWN , (byte) controls[2][0] , controls[2][1]);
		right = new Control(RIGHT , (byte) controls[3][0] , controls[3][1]);
		jump = new Control(JUMP , (byte) controls[4][0] , controls[4][1]);
		attackI = new Control(ATTACK1 , (byte) controls[5][0] , controls[5][1]);
		attackII = new Control(ATTACK2 , (byte) controls[6][0] , controls[6][1]);
		attackIII = new Control(ATTACK3 , (byte) controls[7][0] , controls[7][1]);
		attackIV = new Control(ATTACK4 , (byte) controls[8][0] , controls[8][1]);
		attackV = new Control(ATTACK5 , (byte) controls[9][0] , controls[9][1]);
		powerI = new Control(POWER1 , (byte) controls[10][0] , controls[10][1]);
		powerII = new Control(POWER2 , (byte) controls[11][0] , controls[11][1]);
		powerIII = new Control(POWER2 , (byte) controls[12][0] , controls[12][1]);
		powerIV = new Control(POWER4 , (byte) controls[13][0] , controls[13][1]);
		powerV = new Control(POWER5 , (byte) controls[14][0] , controls[14][1]);
		powerVI = new Control(POWER6 , (byte) controls[15][0] , controls[15][1]);
		powerVII = new Control(POWER7 , (byte) controls[16][0] , controls[16][1]);
		powerIIX = new Control(POWER8 , (byte) controls[17][0] , controls[17][1]);
		powerIX = new Control(POWER9 , (byte) controls[18][0] , controls[18][1]);
		powerX = new Control(POWER10 , (byte) controls[19][0] , controls[19][1]);
		inventory = new Control(INVENTORY , (byte) controls[20][0] , controls[20][1]);
		characterSheet = new Control(CHARACTER_SHEET , (byte) controls[21][0] , controls[21][1]);
		activate = new Control(ACTIVATE , (byte) controls[22][0] , controls[22][1]);
		map = new Control(MAP , (byte) controls[23][0] , controls[23][1]);

				
	}
	
	public synchronized void setByName(String name , byte peripheralType , int key) {
		
//		System.out.println("setting control: " + name + "from peripheral " + peripheralType + " to " + key);
		
		switch (name) {
		
			case "up" -> up = new Control(UP , peripheralType , key);
			case "left" -> left = new Control(LEFT , peripheralType , key);
			case "down" -> down = new Control(DOWN , peripheralType , key);
			case "right" -> right = new Control(RIGHT , peripheralType , key);
			case "jump" -> jump = new Control(JUMP , peripheralType , key);
			case "attackI" -> attackI = new Control(ATTACK1 ,peripheralType , key);
			case "attackII" -> attackII = new Control(ATTACK2 , peripheralType , key);
			case "attackIII" -> attackIII = new Control(ATTACK3 , peripheralType , key);
			case "attackIV" -> attackIV = new Control(ATTACK4 , peripheralType , key);
			case "attackV" -> attackV = new Control(ATTACK5 , peripheralType , key);
			case "powerI" -> powerI = new Control(POWER1 , peripheralType , key);
			case "powerII" -> powerII = new Control(POWER2 , peripheralType , key);
			case "powerIII" -> powerIII = new Control(POWER3 , peripheralType , key);
			case "powerIV" -> powerIV = new Control(POWER4 , peripheralType , key);
			case "powerV" -> powerV = new Control(POWER5 , peripheralType , key);
			case "powerVI" -> powerVI = new Control(POWER6 , peripheralType , key);
			case "powerVII" -> powerVII = new Control(POWER7 , peripheralType , key);
			case "powerIIX" -> powerIIX = new Control(POWER8 , peripheralType , key);
			case "powerIX" -> powerIX = new Control(POWER9 , peripheralType , key);
			case "powerX" -> powerX = new Control(POWER10 , peripheralType , key);
			case "toggleinventory" -> inventory = new Control(INVENTORY , peripheralType , key);
			case "togglecharactersheet" -> characterSheet = new Control(CHARACTER_SHEET, peripheralType , key);
			case "activate" -> activate = new Control(ACTIVATE , peripheralType , key);
			case "map" -> map = new Control(ACTIVATE , peripheralType , key);
			default -> OTHER_CONTROLS.put(name , new Control(UNSPECIFIED , peripheralType , key));
			
		}
		
	}
	
	public final void forEach(BiConsumer<String , Control> callback) { 
		
		callback.accept("Up" , up);
		callback.accept("Left" , left);
		callback.accept("Down" , down);
		callback.accept("Right" , right);
		callback.accept("Jump" , jump);
		callback.accept("Attack I" , attackI);
		callback.accept("Attack II" , attackII);
		callback.accept("Attack III" , attackIII);
		callback.accept("Attack IV" , attackIV);
		callback.accept("Attack V" , attackV);
		callback.accept("Power I" , powerI);
		callback.accept("Power II" , powerII);
		callback.accept("Power III" , powerIII);
		callback.accept("Power IV" , powerIV);
		callback.accept("Power V" , powerV);
		callback.accept("Power VI" , powerVI);
		callback.accept("Power VII" , powerVII);
		callback.accept("Power IIX" , powerIIX);
		callback.accept("Power IX" , powerIX);
		callback.accept("Power X" , powerX);
		callback.accept("Inventory" , inventory);
		callback.accept("Character Sheet" , characterSheet);
		callback.accept("Activate" , activate);
		callback.accept("Map" , map);
		OTHER_CONTROLS.forEach(callback::accept);
		
	}
	
	public synchronized Control getByIndex(byte index) {
	
		return switch(index) {
			case UP -> up;
			case LEFT -> left;
			case DOWN -> down;
			case RIGHT -> right;
			case JUMP -> jump;
			case ATTACK1 -> attackI;
			case ATTACK2 -> attackII;
			case ATTACK3 -> attackIII;
			case ATTACK4 -> attackIV;
			case ATTACK5 -> attackV;
			case POWER1 -> powerI;
			case POWER2 -> powerII;
			case POWER3 -> powerIII;
			case POWER4 -> powerIV;
			case POWER5 -> powerV;
			case POWER6 -> powerVI;
			case POWER7 -> powerVII;
			case POWER8 -> powerIIX;
			case POWER9 -> powerIX;
			case POWER10 -> powerX;
			case INVENTORY -> inventory;
			case CHARACTER_SHEET -> characterSheet;
			case ACTIVATE -> activate;
			case MAP -> map;
			default -> throw new IllegalArgumentException("Unexpected value: " + index);
		};
		
	}
	
}