package CSUtil.DataStructures;

/**
 * Represents a circular, singly linked list node.
 * 
 * @author Chris Brown
 *
 * @param <T>
 */
public class csNode <T> {

	csNode<T> next;
	T val;
	
	public csNode(T val) {
		
		this.val = val;
		
	}

	public csNode(T val , csNode<T> next) {
		
		this.val = val;
		this.next = next;
		
	}
	
	
}