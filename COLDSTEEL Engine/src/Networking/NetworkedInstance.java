package Networking;

import java.io.IOException;
import java.net.InetAddress;

import Game.Levels.Levels;

/**
 * Interface unifying functionality across Network classes. This class specifies several methods such as
 * {@code listen}, {@code send}, {@code sendReliable}, {@code shutDown}, and others.
 *  
 * @author Chris Brown
 *
 */
public interface NetworkedInstance {
	
	/**
	 * Main update method for the given NetworkedInstance. This is in charge of handling anything that needs to happen
	 * every frame, such as checking connection timeouts and sending messages. <b>This method is not for updating UI.</b> 
	 * 
	 */
	public void instanceUpdate();
	
	/**
	 * This method is for updating UI. It will be called from a main thread in which calling UI code will not result in pointer
	 * errors.
	 */
	public void instanceUI();
	
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
	 * @throws IOException if a DatagramSocket receiver throws an IOException in its operation.
	 * 
	 */
	public void listen() throws IOException;
	
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
	 * This method should contain code to execute when the engine loads a level.
	 */
	public void onLevelLoad(Levels newLevel , float[] initialPosition) throws IOException;
	
	/**
	 * Intended to free any allocated resources, both from LWJGL and Java.
	 * 
	 */
	public void shutDown();
	
}