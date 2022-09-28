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
	
	public static final byte[] construct(byte[]...arrays) { 
		
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
		
}
