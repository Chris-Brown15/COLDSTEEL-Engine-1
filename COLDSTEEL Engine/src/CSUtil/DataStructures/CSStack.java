package CSUtil.DataStructures;

/**
 * Last-in-first-out data structure based on singly linked lists.
 * 
 * 
 * @param <T> — generic object for use by this stack
 */
public class CSStack<T> {

	csNode<T> top;
	int size = 0;
	
	public void push(T val) {
		
		top = size++ == 0 ? new csNode<T>(val) : new csNode<T>(val , top);
		
	}
	
	public T pop() {
	
		assert size > 0 : "Pop invoked while stack is empty" ;		
		T popped = top.val;
		top = top.next;
		size--;
		return popped;		
			
	}
	
	public T peek() {
		
		assert size > 0 : "Peek invoked while stack is empty" ;
		return top.val;
		
	}
	
	public boolean empty() {
		
		return size == 0;
		
	}
	
	public int size() {
	
		return size;
		
	}	
	
	public boolean has(final T instance) {
		
		csNode<T> iter = top;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) if(iter.val.equals(instance)) return true;
		return false;
		
	}
	
	public String toString() {
		
		csNode<T> iter = top;
		String result = "[";
		for(int i = 0 ; i < size -1 ; i ++) {
			
			result += iter.val.toString() + ", ";
			iter = iter.next;
			
		}
		
		result += iter.val.toString() + "]";
		return result;
		
	}
	
}
