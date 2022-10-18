package Networking.Utils;

/**
 * Utility class for holding constants related to networking, bit operations, message flags, and utility functions.
 * 
 */
public abstract class NetworkingConstants {

	private NetworkingConstants() {}
	

	/**
	 * Masks for packets. 
	 * Packets are constructed which contain a user's keystroke data. The {@code KEYCODE_MAK} can be used to pull 7 bits out a byte, which
	 * will represent the keycode to the key pressed. the {@code KEY_PRESSED_MASK} can be used to pull the last bit out of the byte, which
	 * tells whether the preceding key was pressed or not. 
	 * 
	 */
	public static final byte	
		//mask for the bits of the short that represent the glfw keycode
		KEYCODE_MASK = 127 ,		
		//mask for the eighth bit in a byte which represents a keystroke
		KEY_PRESSED_MASK = -128
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
		//notifies that the sender has changed levels. if the client sends they have changed levels, if the server sends the client needs to update
		LOCATION_CHANGE = 5,
		//tells the recipient keystrokes the sender has made and state of synced variables.
		UPDATE = 6
		
	;
	
	/**
	 * Returns true if a the byte at {@code offset} of {@code data} is a flag.
	 * 
	 * @param data — a {@code DatagramPacket}'s data	 
	 * @param offset — which index to look into
	 * @return true if {@code data[offset]} is a flag.
	 */
	public static final boolean isFlaggedMessage(byte[] data , int offset) {
		
		return data[offset] >= CHANGE_CONNECTION && data[offset] <= UPDATE;
		
	}

	public static final boolean isFlag(byte possiblyFlag) {
		
		return possiblyFlag >= CHANGE_CONNECTION && possiblyFlag <= UPDATE;
		
	}
	
}
