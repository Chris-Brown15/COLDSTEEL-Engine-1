package CSUtil.DataStructures;

/**
 * Circular doubly linked node. Used with the {@code Linked} class. Otherwise this is a pretty useless class. 
 * 
 * @author Chris Brown
 *
 * @param <T> — generic argument
 */
public class cdNode<T> {

	public cdNode<T> prev;
	public cdNode<T> next;
	public T val;
	
	public cdNode(T val){
		
		this.val = val;
					
	}
	
	public boolean equals(cdNode<T> other) {
		
		return this == other;
		
	}
	
}
