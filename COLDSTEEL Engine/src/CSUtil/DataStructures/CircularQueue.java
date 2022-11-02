package CSUtil.DataStructures;

import java.util.function.Consumer;

/**
 * Linked Queue implementation in which oldest elements are lost.
 * 
 * @author Chris Brown
 *
 */
public class CircularQueue<T>  {
	
	private csNode<T> newest;
	private csNode<T> oldest;
	private final int capacity;
	private int size = 0;
	
	public CircularQueue(int capacity) {
		
		this.capacity = capacity;
		
	}
	
	public void add(T value) {
		
		if(size == 0) {
			
			newest = new csNode<T>(value);
			
		} else if (size == 1) {
			
			csNode<T> newNode = new csNode<T>(value);
			newest.next = newNode;
			oldest = newest;
			newest = newNode;
			
		} else {
		
			csNode<T> newNode = new csNode<T>(value);
			newest.next = newNode;
			newest = newNode;
			
		}
		
		size++;
		if(size-- == capacity) oldest = oldest.next;
		
	}
	
	public void forEach(Consumer<T> callback) {
		
		csNode<T> iter = oldest;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next ) callback.accept(iter.val);
		
	}
	
}
