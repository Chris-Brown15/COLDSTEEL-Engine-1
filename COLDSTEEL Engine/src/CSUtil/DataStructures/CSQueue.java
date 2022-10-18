package CSUtil.DataStructures;

/**
 * Queue data structure implementing First-In-Last-Out.
 * 
 * @author Chris Brown
 *
 * @param <T>
 */
public class CSQueue<T> {

	csNode<T> front;
	csNode<T> back;
	private int size = 0;
	
	public CSQueue<T> enqueue(T value) {
		
		if(size == 0) {
			
			front = new csNode<T>(value);
			
		} else if (size == 1) { 
			
			back = new csNode<T>(value);
			front.next = back;
			
		} else { 
			
			csNode<T> newNode = new csNode<T>(value);
			back.next = newNode;
			back = newNode;
			
		}
		
		size++;		
		return this;
		
	}
	
	public T dequeue() {
		
		if(size < 0) throw new IllegalStateException("CSQUEUE ERROR: Dequeue called on empty stack");
		T frontVal = front.val;
		front = front.next;
		size--;
		return frontVal;
		
	}

	public T peek() {
		
		return front.val;
		
	}
	
	public boolean empty() { 
		
		return size == 0;
		
	}
	
}
