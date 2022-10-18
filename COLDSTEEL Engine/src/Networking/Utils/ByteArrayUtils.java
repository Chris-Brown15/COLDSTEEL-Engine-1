package Networking.Utils;

/**
 * 
 * Provides easy construction of marshalled byte arrays for sending over UDP. 
 * 
 * @author Chris Brown
 *
 */
public abstract class ByteArrayUtils {

	private ByteArrayUtils() {}
		
	public static final byte[] compose(byte[]...arrays) { 
		
		int next = 0;
		for(byte[] x : arrays) next += x.length;
		byte[] newArray = new byte[next];
		next = 0;
		for(byte[] x : arrays) { 
			
			System.arraycopy(x, 0, newArray, next, x.length);
			next += x.length;
			
		}
		
		return newArray;
		
	}
	
	public static final byte[] append(byte[] original , byte... append) { 
		
		int newLength = original.length + append.length;
		byte[] newArray = new byte[newLength];
		System.arraycopy(original, 0, newArray, 0, original.length);
		System.arraycopy(append, 0, newArray, original.length, append.length);
		return newArray;
		
	}
	
	public static final byte[] prepend(byte[] original , byte... precede) { 

		int newLength = original.length + precede.length;
		byte[] newArray = new byte[newLength];
		System.arraycopy(precede , 0 , newArray , 0 , precede.length);
		System.arraycopy(original , 0 , newArray , precede.length , original.length);
		return newArray;
		
	}
	
	public static final byte[] partition(byte[] original , int startingIndex , int numberBytes) {
		
		byte[] partition = new byte[numberBytes];
		System.arraycopy(original, startingIndex, partition, 0, numberBytes);
		return partition;
		
	}
	
	/**
	 * Returns an array representing the contents of {@code original} from the indices of {@code offset} to {@code original.length - 1}.
	 * 
	 * @param original — byte array of data
	 * @param offset — index to first element in resulting array
	 * @return subsequence of {@code original}
	 */
	public static final byte[] fromOffset(byte[] original , int offset) {
	
		byte[] newArr = new byte[original.length - offset];
		System.arraycopy(original, offset, newArr, 0, newArr.length);
		return newArr;
		
	}
		
	/**
	 * Constructs an array of bytes from the array of floats given, formatting the result in Big Endian.
	 * 
	 * @param array
	 * @return
	 */
	public static final byte[] fromFloats(float... array) {
		
		byte[] asBytes = new byte[array.length * Float.BYTES];
		int bitRepresentation;		
		for(int i = 0 , j = 0 ; i < array.length ; i ++ , j += 4) {
			 
			bitRepresentation = Float.floatToIntBits(array[i]);
			
			asBytes[j] = (byte) (bitRepresentation >> 24); // high byte first
			asBytes[j + 1] = (byte) (bitRepresentation >> 16);
			asBytes[j + 2] = (byte) (bitRepresentation >> 8); 
			asBytes[j + 3] = (byte) (bitRepresentation); //low byte last
					
		}
		
		return asBytes;
		
	}
	
	/**
	 * Constructs an array of bytes from the array of floats given, formatting the result in Big Endian.
	 * 
	 * @param ints
	 * @return
	 */
	public static final byte[] fromInts(int... ints) {
		
		byte[] asBytes = new byte[ints.length * Integer.BYTES];
		for(int i = 0 , j = 0 ; i < ints.length ; i ++ , j += Integer.BYTES) {
			
			asBytes[j] = (byte)(ints[i] >> 24);
			asBytes[j + 1] = (byte)(ints[i] >> 16);
			asBytes[j + 2] = (byte)(ints[i] >> 8);
			asBytes[j + 3] = (byte)(ints[i]);
			
		}
		
		return asBytes;
		
	}
	
	public static final byte[] fromShorts(short...shorts) {
		
		byte[] asBytes = new byte[shorts.length * Short.BYTES];
		for(int i = 0 , j = 0 ; i < shorts.length ; i++ , j += Short.BYTES) {
			
			asBytes[j] = (byte)(shorts[i] >> 8);
			asBytes[j + 1] = (byte)(shorts[i]);
			
		}
		
		return asBytes;
		
	}
	
	/**
	 * Constructs an array of floats from the array of bytes given. This assumes the bytes are in Big Endian, as that is what Java
	 * typically is.
	 * 
	 * @param startingByte — index into {@code bytes} to start operations
	 * @param quantity — number of bytes from {@code startingByte} to read
	 * @param bytes — an array of bytes
	 * @return an array floats representing the bytes from {@code startingByte}.
	 */
	public static final float[] toFloats(int startingByte , int quantity , byte...bytes) { 
		
		if(startingByte + quantity > bytes.length) throw new IllegalArgumentException("Parameters do not align properly.");
		float[] floats = new float[quantity / Float.BYTES];
		for(int i = startingByte , j = 0 ; j < floats.length ; i += 4 , j ++) {
			
			floats[j] = Float.intBitsToFloat((bytes[i] << 24) | ((bytes[i + 1] & 255) << 16) | ((bytes[i + 2] & 255) << 8) | (bytes[i + 3] & 255));
			
		}
		
		return floats;
		
	}
	
	/**
	 * Constructs an array of ints from the array of bytes given. This assums the bytes are in Big Endian.
	 * 
	 * @param startingByte — index into first byte of {@code bytes}
	 * @param quantity — number of bytes to advance from {@code startingByte}
	 * @param bytes — a source array of bytes
	 * @return an array of integers representing the given bytes at the specified index.
	 */
	public static final int[] toInts(int startingByte , int quantity , byte...bytes) {
		
		if(startingByte + quantity > bytes.length) throw new IllegalArgumentException("Parameters do not align properly.");
		int[] ints = new int[quantity / Integer.BYTES];
		for(int i = startingByte , j = 0 ; j < ints.length ; i += Integer.BYTES , j ++) { 
			
			ints[j] = (bytes[i] << 24) | ((bytes[i + 1] & 255) << 16) | ((bytes[i + 2] & 255) << 8) | (bytes[i + 3] & 255);
			
		}
		
		return ints;
		
	}

	public static final short[] toShorts(int startingByte , int quantity , byte...bytes) {
		
		if(startingByte + quantity > bytes.length) throw new IllegalArgumentException("Parameters do not align properly.");
		short[] shorts = new short[quantity / Short.BYTES];
		for(int i = startingByte , j = 0 ; j < shorts.length ; i += Short.BYTES , j ++) {
			
			shorts[j] = (short) (((bytes[i] & 255) << 8) | (bytes[i + 1] & 255));
			
		}
		
		return shorts;
		
	}
	
}
