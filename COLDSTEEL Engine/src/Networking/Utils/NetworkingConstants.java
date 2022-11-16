package Networking.Utils;

/**
 * Utility class for holding constants related to networking, bit operations, message flags, and utility functions.
 * 
 */
public abstract class NetworkingConstants {

	private NetworkingConstants() {}
	

	/**
	 * Masks for packets. 
	 * Packets are constructed which contain a user's keystroke data. The {@code KEYCODE_MAK} can be used to pull 5 bits out a byte, which
	 * will represent the control ID the user is updating about. the {@code CONTROL_PRESSED_MASK} can be used to pull the last bit out of the byte, which
	 * tells whether the preceding key was pressed or not. The {@code CONTROL_STRUCK_MASK} can be used to query whether the key is struck or not
	 * 
	 */
	public static final int
		//mask for the bits of the byte that represent the control ID 
		KEYCODE_MASK = 63 ,		
		//mask for the eighth bit in a byte which represents a press state for the control ID of the preceding first six bits
		CONTROL_PRESSED_MASK = -128,
		//mask for the seventh bit of a number, telling that this key was struck
		CONTROL_STRUCK_MASK = 64;
	;

	/**
	 * Special flags which specify the following contents of a packet. Packets will contain one flag at position 0 or position 9
	 * if it is a reliable packet. 
	 * There shouldn't be two flags in one packet.
	 * 
	 */	
	public static final byte
		//if no connection is established from this port, establish a connection, else if one already exists, end this connection.
		CHANGE_CONNECTION = 1,
		//notifies clients that a new client connected and accompanies that player's entity's name and game location 
		CLIENT_CONNECTED = 2,
		//notifies clients that a client disconnected and accompanies that player's entity
		CLIENT_DISCONNECTED = 3,		
		//notifies that the contents of this packet are for chat. 
		CHAT_MESSAGE = 4,
		//notify that the sender has changed levels. 
		LOCATION_CHANGE_IN = 5,
		LOCATION_CHANGE_OUT = 6,
		//notifies the client with useful information aobut the level they just loaded into
		LEVEL_LOAD_INFO = 7,
		//tells the recipient keystrokes the sender has made and state of synced variables.
		UPDATE = 8		
	;
	
	public static final boolean isFlag(byte possiblyFlag) {
		
		return possiblyFlag >= CHANGE_CONNECTION && possiblyFlag <= UPDATE;
		
	}
	
}
