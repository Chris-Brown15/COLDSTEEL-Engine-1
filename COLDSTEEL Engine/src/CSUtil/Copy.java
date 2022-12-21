package CSUtil;

/**
 * 
 * This can be passed to networking-related functions that want to snapshot the state of a variable at a given moment. This SAM can be passed into those
 * functions, who will pass the callback into internal data structures to use when it comes time to snapshot an object for copying.
 * 
 * @author Chris Brown
 *
 */
public interface Copy {

	/**
	 * This should return a deep copy of some {@code T}.
	 * 
	 * @param source — source object to deep copy
	 * @return — deep copied version of {@code source}.
	 */
	public Object copy(Object source);
	
}
