package Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Objects;

import Core.Entities.Entities;

/**
 * Interface unifying functionality across Network classes. This class specifies several methods such as
 * {@code listen}, {@code send}, {@code sendReliable}, {@code shutDown}, and others.
 *  
 * @author Chris Brown
 *
 */
public interface NetworkedInstance {
	
	/**
	 * Helper function to get a particular networked entity instance given some entity.
	 * 
	 * @param instance — an instance of NetworkedInstance
	 * @param e — an instance of {@code Entities}
	 * @return a {@code NetworkedEntities} if one exists
	 * 
	 * @throws IllegalArgumentException if {@code e} is not within {@code instance}'s managed connections
	 */
	public static NetworkedEntities getNetworkedEntityForEntity(NetworkedInstance instance , Entities e) {
		
		Objects.requireNonNull(instance , "NetworkedInstance is null");
		Objects.requireNonNull(e , "Entities is null");
		
		NetworkedEntities elem;
		for(Iterator<NetworkedEntities> iter = instance.managedConnections().iterator() ; iter.hasNext() ; ) {
			
			elem = iter.next();
			if(elem.networked().equals(e)) return elem;
			
		}
		
		throw new IllegalArgumentException("Entity " + e.name() + " not managed by: " + instance.toString());
		
	}
	
	/**
	 * Main update method for the given NetworkedInstance. This is in charge of handling anything that needs to happen
	 * every frame, such as checking connection timeouts and sending messages. <b>This method is not for updating UI.</b> 
	 * 
	 */
	public void instanceUpdate();
	
	/**
	 * This method should be used to get the list of entities {@code this} manages.
	 * 
	 * @return — iterable object on NetowrkedEntities.
	 */
	public Iterable<NetworkedEntities> managedConnections();
	
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
	 * Intended to free any allocated resources, both from LWJGL and Java.
	 * 
	 */
	public void shutDown();
	
}