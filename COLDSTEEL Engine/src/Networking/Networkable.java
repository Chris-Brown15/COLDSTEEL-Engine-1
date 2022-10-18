package Networking;

/**
 * Used to compose an object by some specific byte layout and reconstruct it from over the wire. 
 * The methods of this interface should be used to specify the fields of an object as an array of raw bytes which is
 * then sent over the network to be used for reconstructing the object.
 * 
 * @author Chris Brown
 *
 * @param <T> — type of an implementor of this interface
 */
public interface Networkable <T> {

	/**
	 * This method should map the fields of some instance of T into an array of bytes.
	 * 
	 * @return an array of bytes representing the fields of some instance of T
	 */
	public byte[] compose();
	
	/**
	 * Constructs an instance of T from the given bytes
	 * 
	 * @param asBytes — an array of bytes represnting the fields of an instance of T
	 * @return an instance of T as specified by {@code asBytes}.
	 */
	public T receive(byte[] asBytes);
	
}
