package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Interface unifying functionality across Network classes. This class specifies several methods such as
 * {@code listen}, {@code send}, {@code sendReliable}, {@code shutDown}, and others.
 *  
 * @author Chris Brown
 *
 */
public interface NetworkedInstance {

	/**
	 * Masks for packets. 
	 * Datagram packets largely contain sequences of shorts which specify a peripheral key, its state, and which type of perihperal it belongs to.
	 * These masks help mark particular bits that carry a specific meaning.
	 * 
	 */
	short	
		//mask for the bits of the short that represent the glfw keycode
		KEYCODE_MASK = 511 ,		
		//bitwise or a GLFW keycode with this to mark its 10th bit as either pressed or not
		KEY_PRESSED_MASK = 512,
		/*
		 *These values represent bits 11, 12, and 13, one of these will be set to mark which peripheral type the first 9 bits represent. 
		 */
		KEYBOARD_KEY_MASK = 1024,
		MOUSE_KEY_MASK = 2048,
		GAMEPAD_KEY_MASK = 4096
	;

	/**
	 * Special flags which specify the following contents of a packet. Packets will contain one flag at position 0 or position 9
	 * if it is a reliable packet. 
	 * There shouldn't be two flags in one packet.
	 * 
	 */	
	byte
		//if no connection is established from this port, establish a connection, else if one already exists, end this connection.
		CHANGE_CONNECTION = 1,
		//notifies clients that a new client connected and accompanies that player's entity's name and game location 
		CLIENT_CONNECTED = 2,
		//notifies clients that a client disconnected and accompanies that player's entity
		CLIENT_DISCONNECTED = 3,		
		//notifies that the contents of this packet are for chat. 
		CHAT_MESSAGE = 4,
		//notifies that the sender has changed location and what proceeds this flag is "macrolevelname/levelname"
		LOCATION_CHANGE = 5,
		//what follows this flag is a string containing names of entities separated by the "|".
		PLAYER_MANIFEST = 6
	;
		
	/**
	 * Returns true if a the byte at {@code offset} of {@code data} is a flag.
	 * 
	 * @param data — a {@code DatagramPacket}'s data	 
	 * @param offset — which index to look into
	 * @return true if {@code data[offset]} is a flag.
	 */
	static boolean isFlaggedMessage(byte[] data , int offset) {
		
		return data[offset] >= CHANGE_CONNECTION && data[offset] <= PLAYER_MANIFEST;
		
	}

	/**
	 * Main update method for the given NetworkedInstance. This is in charge of handling anything that needs to happen
	 * every frame, such as updating UI and checking connection timeouts. 
	 * 
	 */
	public void update();
	
	/**
	 * This method should be used to send some message to a recipient 
	 * 
	 * @param data — byte array of data to send over the network
	 * 
	 * @throws IOException if a DatagramSocket sender throws an IOException in its operations.
	 * 
	 */	
	public void send(byte[] data) throws IOException;
	
	/**
	 * This method should be used to send a Reliable Datagram packet.
	 * 
	 * @param data — byte array of data to send over the network in a reliable way
	 * @param hash — ID for this reliable packet
	 * @param cooldownMillis — number of milliseconds to wait before resending
	 * 
	 * @throws IOException if a DatagramSocket sender throws an IOException in its operations.
	 */
	public void sendReliable(byte[] data , int hash , int cooldownMillis) throws IOException;
	
	/**
	 * This method should be used to send a Reliable Datagram packet to the specified address and port.
	 * 
	 * @param data — byte array of data to send over the network in a reliable way
	 * @param hash — ID for this reliable packet
	 * @param cooldownMillis — number of milliseconds to wait before resending
	 * @param addr — address for this message to be sent to
	 * @param port — port number to send this message to
	 * 
	 * @throws IOException if a DatagramSocket sender throws an IOException in its operations.
	 */
	public void sendReliable(byte[] data , int hash , int cooldownMillis , InetAddress addr , int port) throws IOException;
	
	/**
	 * This method should be used to get and return a DatagramPacket, possibly blocking in the process.
	 * 
	 * @return A received message.
	 * 
	 * @throws IOException if a DatagramSocket receiver throws an IOException in its operation.
	 * 
	 */
	public DatagramPacket listen() throws IOException;
	
	/**
	 * Designed to designate this NetworkedInstance as a host of a user-hosted game. 
	 *  
	 * @return true if this program runtime is hosting a multiplayer session.
	 */
	public boolean host();
	
	/**
	 * Designed to designate this NetworkedInstance as a client of a user-hosted game.
	 * 
	 * @return true if this program runtime is connected as a client to another user's self-hosted multiplayer session.
	 */
	public boolean client();
	
	/**
	 * This method should toggle the display of any Multiplayer UI {@code this} entails.
	 * 
	 */
	public void toggleUI();
	
	/**
	 * Intended to free any allocated resources, both from LWJGL and Java.
	 * 
	 */
	public void shutDown();
	
}