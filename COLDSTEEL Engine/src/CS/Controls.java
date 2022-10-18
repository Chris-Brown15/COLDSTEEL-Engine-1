package CS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This small class contains a set of integers that should map to GLFW keys. The values of the ints will equal some GLFW key, so these values can be 
 * passed into GLFW functions to get the state of the key that int is mapped to.
 * 
 * @author Chris Brown
 *
 */
public abstract class Controls {

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
	public static final record Control(byte peripheral , byte key) {}
	
	/**
	 * indices into array of ints representing keybind settings
	 * 
	 */
	public static volatile Control 
		UP = new Control((byte)0 , (byte)0) ,
		LEFT = new Control((byte)0 , (byte)0) ,
		DOWN = new Control((byte)0 , (byte)0) ,
		RIGHT = new Control((byte)0 , (byte)0) ,
		JUMP = new Control((byte)0 , (byte)0) ,
		ATTACKI = new Control((byte)0 , (byte)0) ,
		ATTACKII = new Control((byte)0 , (byte)0) ,
		ATTACKIII = new Control((byte)0 , (byte)0) ,
		ATTACKIV = new Control((byte)0 , (byte)0) ,
		ATTACKV = new Control((byte)0 , (byte)0) ,
		POWERI = new Control((byte)0 , (byte)0) ,
		POWERII = new Control((byte)0 , (byte)0) ,
		POWERIII = new Control((byte)0 , (byte)0) ,
		POWERIV = new Control((byte)0 , (byte)0) ,
		POWERV = new Control((byte)0 , (byte)0) ,
		POWERVI = new Control((byte)0 , (byte)0) ,
		POWERVII = new Control((byte)0 , (byte)0) ,
		POWERIIX = new Control((byte)0 , (byte)0) ,
		POWERIX = new Control((byte)0 , (byte)0) ,
		POWERX = new Control((byte)0 , (byte)0) ,
		INVENTORY = new Control((byte)0 , (byte)0) ,
		CHARACTER_SHEET = new Control((byte)0 , (byte)0) ,
		ACTIVATE = new Control((byte)0 , (byte)0) ,
		MAP = new Control((byte)0 , (byte)0)
	;
	
	private static final ConcurrentHashMap<String , Control> OTHER_CONTROLS = new ConcurrentHashMap<String , Control>();

	/**
	 * Call this to assign values to all controls. 
	 * 
	 * @param keycodesInOrder
	 */
	synchronized static void set(short[]...controls) {
		
		UP = new Control((byte)controls[0][0] , (byte)controls[0][1]);
		LEFT = new Control((byte)controls[1][0] , (byte)controls[1][1]);
		DOWN = new Control((byte)controls[2][0] , (byte)controls[2][1]);
		RIGHT = new Control((byte)controls[3][0] , (byte)controls[3][1]);
		JUMP = new Control((byte)controls[4][0] , (byte)controls[4][1]);
		ATTACKI = new Control((byte)controls[5][0] , (byte)controls[5][1]);
		ATTACKII = new Control((byte)controls[6][0] , (byte)controls[6][1]);
		ATTACKIII = new Control((byte)controls[7][0] , (byte)controls[7][1]);
		ATTACKIV = new Control((byte)controls[8][0] , (byte)controls[8][1]);
		ATTACKV = new Control((byte)controls[9][0] , (byte)controls[9][1]);
		POWERI = new Control((byte)controls[10][0] , (byte)controls[10][1]);
		POWERII = new Control((byte)controls[11][0] , (byte)controls[11][1]);
		POWERIII = new Control((byte)controls[12][0] , (byte)controls[12][1]);
		POWERIV = new Control((byte)controls[13][0] , (byte)controls[13][1]);
		POWERV = new Control((byte)controls[14][0] , (byte)controls[14][1]);
		POWERVI = new Control((byte)controls[15][0] , (byte)controls[15][1]);
		POWERVII = new Control((byte)controls[16][0] , (byte)controls[16][1]);
		POWERIIX = new Control((byte)controls[17][0] , (byte)controls[17][1]);
		POWERIX = new Control((byte)controls[18][0] , (byte)controls[18][1]);
		POWERX = new Control((byte)controls[19][0] , (byte)controls[19][1]);
		INVENTORY = new Control((byte)controls[20][0] , (byte)controls[20][1]);
		CHARACTER_SHEET = new Control((byte)controls[21][0] , (byte)controls[21][1]);
		ACTIVATE = new Control((byte)controls[22][0] , (byte)controls[22][1]);
		MAP = new Control((byte)controls[23][0] , (byte)controls[23][1]);
				
	}
	
	synchronized static void setByName(String name , byte peripheralType , byte key) {
		
//		System.out.println("setting control: " + name + "from peripheral " + peripheralType + " to " + key);
		
		switch (name) {
		
			case "up" -> UP = new Control(peripheralType , key);
			case "left" -> LEFT = new Control(peripheralType , key);
			case "down" -> DOWN = new Control(peripheralType , key);
			case "right" -> RIGHT = new Control(peripheralType , key);
			case "jump" -> JUMP = new Control(peripheralType , key);
			case "attackI" -> ATTACKI = new Control(peripheralType , key);
			case "attackII" -> ATTACKII = new Control(peripheralType , key);
			case "attackIII" -> ATTACKIII = new Control(peripheralType , key);
			case "attackIV" -> ATTACKIV = new Control(peripheralType , key);
			case "attackV" -> ATTACKV = new Control(peripheralType , key);
			case "powerI" -> POWERI = new Control(peripheralType , key);
			case "powerII" -> POWERII = new Control(peripheralType , key);
			case "powerIII" -> POWERIII = new Control(peripheralType , key);
			case "powerIV" -> POWERIV = new Control(peripheralType , key);
			case "powerV" -> POWERV = new Control(peripheralType , key);
			case "powerVI" -> POWERVI = new Control(peripheralType , key);
			case "powerVII" -> POWERVII = new Control(peripheralType , key);
			case "powerIIX" -> POWERIIX = new Control(peripheralType , key);
			case "powerIX" -> POWERIX = new Control(peripheralType , key);
			case "powerX" -> POWERX = new Control(peripheralType , key);
			case "toggleinventory" -> INVENTORY = new Control(peripheralType , key);
			case "togglecharactersheet" -> CHARACTER_SHEET = new Control(peripheralType , key);
			case "activate" -> ACTIVATE = new Control(peripheralType , key);
			case "map" -> MAP = new Control(peripheralType , key);
			default -> OTHER_CONTROLS.put(name , new Control(peripheralType , key));
			
		}
		
	}
	
	public static final void forEach(Consumer<Control> callback) { 
		
		callback.accept(UP);
		callback.accept(LEFT);
		callback.accept(DOWN);
		callback.accept(RIGHT);
		callback.accept(JUMP);
		callback.accept(ATTACKI);
		callback.accept(ATTACKII);
		callback.accept(ATTACKIII);
		callback.accept(ATTACKIV);
		callback.accept(ATTACKV);
		callback.accept(POWERI);
		callback.accept(POWERII);
		callback.accept(POWERIII);
		callback.accept(POWERIV);
		callback.accept(POWERV);
		callback.accept(POWERVI);
		callback.accept(POWERVII);
		callback.accept(POWERIIX);
		callback.accept(POWERIX);
		callback.accept(POWERX);
		callback.accept(INVENTORY);
		callback.accept(CHARACTER_SHEET);
		callback.accept(ACTIVATE);
		callback.accept(MAP);
		OTHER_CONTROLS.forEach((name , control) -> callback.accept(control));
		
	}

	public static final void forEach(BiConsumer<String , Control> callback) { 
		
		callback.accept("Up" , UP);
		callback.accept("Left" , LEFT);
		callback.accept("Down" , DOWN);
		callback.accept("Right" , RIGHT);
		callback.accept("Jump" , JUMP);
		callback.accept("Attack I" , ATTACKI);
		callback.accept("Attack II" , ATTACKII);
		callback.accept("Attack III" , ATTACKIII);
		callback.accept("Attack IV" , ATTACKIV);
		callback.accept("Attack V" , ATTACKV);
		callback.accept("Power I" , POWERI);
		callback.accept("Power II" , POWERII);
		callback.accept("Power III" , POWERIII);
		callback.accept("Power IV" , POWERIV);
		callback.accept("Power V" , POWERV);
		callback.accept("Power VI" , POWERVI);
		callback.accept("Power VII" , POWERVII);
		callback.accept("Power IIX" , POWERIIX);
		callback.accept("Power IX" , POWERIX);
		callback.accept("Power X" , POWERX);
		callback.accept("Inventory" , INVENTORY);
		callback.accept("Character Sheet" , CHARACTER_SHEET);
		callback.accept("Activate" , ACTIVATE);
		callback.accept("Map" , MAP);
		OTHER_CONTROLS.forEach(callback::accept);
		
	}
	
	
}