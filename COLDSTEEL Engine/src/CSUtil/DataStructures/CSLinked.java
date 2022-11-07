package CSUtil.DataStructures;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Generic doubly linked list with the aim to get O(1) removal by allowing the outside world to operate with nodes if wanted.
 *  
 * @author Chris Brown
 *
 */
public class CSLinked<T> {

	private volatile cdNode<T> head;
	private volatile cdNode<T> tail;
	private volatile int size = 0;
	
	public CSLinked(){}

	@SafeVarargs public CSLinked(T...startingElements) {
		
		for(T x : startingElements) add(x);
		
	}
	
	public cdNode<T> add(T val) {
		
		if(size == 0) {
			
			head = new cdNode<T>(val);
			size++;
			return head;
			
		} else if(size == 1) {
		
			tail = new cdNode<T>(val);
			tail.prev = head;
			head.next = tail;
			
		} else {
			
			cdNode<T> newN = new cdNode<T>(val);
			newN.prev = tail;
			tail.next = newN;
			tail = newN;
			tail.next = head;
					
		}

		head.prev = tail;
		size++;
		return tail;
		
	}
	
	public cdNode<T> add(cdNode<T> node) {
		
		if(size == 0) {
			
			head = node;
			size++;
			return head;
			
		} else if (size == 1) {
			
			tail = node;
			tail.prev = head;
			head.next = tail;
			
		} else {
			
			node.prev = tail;
			tail.next = node;
			tail = node;
			tail.next = head;
			
		}
		
		head.prev = tail;
		size++;
		return tail;
		
	}
	
	/**
	 * Adds {@code val} if it is not present, else it removes it
	 * 
	 * @param val
	 */
	public void toggle(T val) {
		
		if(size == 0) {
			
			add(val);
			return;
			
		}
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) if(iter.val.equals(val)) {
			
			removeVal(val);
			return;
			
		}
		
		add(val);
		
	}
	
	public cdNode<T> addAt(int index , T val) {
		
		if(index < 0) throw new IndexOutOfBoundsException("Invalid index; " + index);
		if(index > size - 1 || size == 0) return add(val);
		else {
			//up represents element in front of index
			
			cdNode<T> up = get(index);
			cdNode<T> newElem = new cdNode<T>(val);
			newElem.next = up;
			newElem.prev = up.prev;
			up.prev.next = newElem;
			up.prev = newElem;
			return newElem;
			
		}		
		
	}
	
	/**
	 * Removes iter and returns the next element. Subsequent to this call, the accumulator or counter must be incremented.
	 * If tail is removed, the element previous to tail is returned.
	 * 
	 * @param iter — a node to to remove
	 * @return the subsequent node
	 */
	public cdNode<T> safeRemove(cdNode<T> iter) {
		
		switch(size) {
		
			case 0:
			
				return null;
				
			case 1:
				
				size = 0;
				return head;
				
			case 2:
				
				size--;	//if we are removing the head, set tail to head, this is the only exceptional thing to do
				if(iter.equals(head)) head = tail;				
				return head;
				
			default:
				
				if(iter.equals(head)) {
					
					head = head.next;
					tail.next = head;
					head.prev = tail;					
					size--;
					
				} else if(iter.equals(tail)) {
					
					tail = tail.prev;
					tail.next = head;
					head.prev = tail;
					size--;
					
				} else {
					
					iter.prev.next = iter.next;
					iter.next.prev = iter.prev;
					size--;					
					
				}
				
				return iter.next;
		
		}		
		
	}
	
	/**
	 * Attempts to remove {@code val}, returning its node if successful, else null.
	 * 
	 * @param val — object to remove
	 * @return node of {@code val} if removal was successful, else null.
	 */
	public cdNode<T> removeIfHas(T val) {
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i++ , iter = iter.next) if(iter.val.equals(val)) {
			
			removeVal(val);
			return iter;
		
		}
		
		return null;
		
	}
	
	public cdNode<T> safeRemove(int index){
		
		return safeRemove(get(index));
		
	}
	
	/**
	 * Removes {@code val} and returns the node holding the element subsequent to val.
	 * 
	 * @param val — T value to remove from this list
	 * @return — a node holding the element subsequent to {@code val}.
	 */
	public cdNode<T> removeVal(T val) {
		
		cdNode<T> iter = head;
		int i = 0 ;
		while(i++ < size && !iter.val.equals(val)) iter = iter.next;
		return safeRemove(iter);
		
	}
	
	public T removeVal(int index) {
		
		cdNode<T> iter = head;
		int i = 0 ;
		while(i++ != index) iter = iter.next;
		safeRemove(iter);
		return iter.val;
		
	}
	
	public cdNode<T> get(int index){
		
		if(index >= size || index < 0) return null;
		else if (index <= size / 2){
			
			cdNode<T> iter = head;
			int i = 0;
			while(i++ < index) iter = iter.next;
			return iter;
			
		} else {
			
			cdNode<T> iter = tail;
			int i = size - 1;
			while(i-- > index) iter = iter.prev;
			return iter;
			
		}
		
	}
	
	/**
	 * Attempt to find the node of {@code value}, returning null on failure.
	 * 
	 * @param value — an element which should be in this list
	 * @return — the node of that value
	 */
	public cdNode<T> get(T value){
		
		if(value.equals(head.val)) return head;
		if(value.equals(tail.val)) return tail;
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) if(iter.val.equals(value)) return iter;
		return null;		
		
	}
	
	public cdNode<T> getIfExists(Predicate<T> test){
		
		if(size == 0) return null;
		cdNode<T> iter = head;
		if(test.test(head.val)) return head;
		if(size == 1) return null;
		if(test.test(tail.val)) return tail;
		iter = head.next;
		for(int i = 1 ; i < size - 1 ; i ++ , iter = iter.next) if(test.test(iter.val)) return iter;
		return null;
		
	}

	public T getValIfExists(Predicate<T> test){
		
		if(size == 0) return null;
		if(test.test(head.val)) return head.val;
		
		if(size == 1) return null;
		if(test.test(tail.val)) return tail.val;
		
		cdNode<T> iter = head.next;
		for(int i = 1 ; i < size - 1 ; i ++ , iter = iter.next) if(test.test(iter.val)) return iter.val;
		return null;
		
	}
	
	public T getVal(int index) {
		
		if(index < 0 || index >= size) throw new IndexOutOfBoundsException(index + " out of bounds for length " + size);		
		return get(index).val;
		
	}
	
	public cdNode<T> tail(){
		
		return get(size -1);
		
	}
	
	public void print() {

		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) System.out.println(iter.val.toString());
		
	}	
	
	public String toString() {
		
		if(size == 0) return"[]";
		
		String res = "[";		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size -1; i ++ ,iter = iter.next) res += iter.val.toString() + ", "; 
		res += iter.val;	
		res += "]";		
		return res;		
		
	}
	
	public int size() {
		
		return size;
		
	}
	
	public boolean empty() {
		
		return size == 0;
		
	}
	
	public boolean containsNull() {
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) if(iter.val == null) return true;
		return false;		
		
	}
	
	public void forEachVal(Consumer<T> function) {
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) function.accept(iter.val);
				
	}
	
	public void forEachValReversed(Consumer<T> function) {
		
		cdNode<T> iter = tail;
		for(int i = size - 1 ; i >= 0 ; i -- , iter = iter.prev) function.accept(iter.val);
		
	}
	
	public void forEach(Consumer<cdNode<T>> function) {
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) function.accept(iter);
				
	}
	
	public void forEachReversed(Consumer<cdNode<T>> function) {
		
		cdNode<T> iter = tail;
		for(int i = size - 1 ; i >= 0 ; i-- , iter = iter.prev) function.accept(iter);
		
	}
	
	public void forOnlyVals(Predicate<T> test , Consumer<T> function) {
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) if(test.test(iter.val)) function.accept(iter.val);
		
	}

	public void forOnly(Predicate<cdNode<T>> test , Consumer<cdNode<T>> function) {
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) if(test.test(iter)) function.accept(iter);
		
	}
	
	public void forOnlyValsReversed(Predicate<T> test ,  Consumer<T> function) {
		
		cdNode<T> iter = size > 1 ? tail : head;
		for(int i = size - 1 ; i >= 0 ; i -- , iter = iter.prev) if(test.test(iter.val)) function.accept(iter.val);
		
	}
	
	public void forOnlyReversed(Predicate<cdNode<T>> test , Consumer<cdNode<T>> function) {
		
		cdNode<T> iter = size > 1 ? tail : head;
		for(int i = size - 1 ; i >= 0 ; i-- , iter = iter.prev) if(test.test(iter)) function.accept(iter); ;
		
	}
	
	public boolean has(T val) {
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size;  i ++ , iter = iter.next) if(iter.val.equals(val))return true;
		return false;
		
	}
	
	public boolean has(int index) {
		
		return index <= size -1;
		
	}
	
	@SuppressWarnings("rawtypes") public cdNode[] swap(int pos1 , int pos2) {
		
		if(pos1 == pos2 || pos1 >= size || pos2 >= size) return null;
				
		/*swapping head and tail*/ 
		if (pos1 == 0 && pos2 == size - 1 || pos2 == 0 && pos1 == size - 1) {
			
			T oldHead = head.val ;			
			head.val = tail.val;
			tail.val = oldHead;
			return new cdNode[] {head , tail};
									
		} 
		
		/*swapping head*/
		else if(pos1 == 0 || pos2 == 0) {
			
			cdNode<T> otherElem = get(pos2 == 0 ? pos1 : pos2);
			T oldHead = head.val;
			head.val = otherElem.val;
			otherElem.val = oldHead;
			return new cdNode[] {head , otherElem};
					
		} 
		
		/*swapping tail*/ 
		else if(pos1 == size - 1 || pos2 == size - 1) {
			
			cdNode<T> otherElem = get(pos1 == size - 1 ? pos2 : pos1);
			T oldTail = tail.val;
			tail.val = otherElem.val;
			otherElem.val = oldTail;
			
			return new cdNode[] {otherElem , tail};
			
		} 

		/*swapping middle elements */ 
		else {
			
			cdNode<T> node1 = null , node2 = null;
			cdNode<T> iter = head;	
			
			int iterations = pos1 > pos2 ? pos1 : pos2;
			
			for(int i = 0 ; i <= iterations ; i ++ , iter = iter.next) {
				
				if(i == pos1) node1 = iter;
				else if(i == pos2) node2 = iter;
				
			}
				
			T oldPos1 = node1.val;
			node1.val = node2.val;
			node2.val = oldPos1;			
			
			return new cdNode[] {node1 , node2};
			
		}
		
	}
	
	/**
	 * Moves the element at index to destination by relinking it to follow destination.
	 * 
	 * @param index
	 * @param destination
	 */
	public void moveTo(int index , int destination) {
		
		/* Moving Head in front of tail */ 
		if(index == 0 && destination == size - 1) {
			
			cdNode<T> oldHead = head;
			tail.next = oldHead;			
			oldHead.prev = tail;
			head = head.next;
			head.prev = oldHead;
			tail = oldHead;
			
		}
		
		/* Moving tail behind head */ 
		else if (index == size - 1 && destination == 0) {
		
			cdNode<T> oldTail = tail;
			head.prev = oldTail;
			oldTail.next = head;
			tail = tail.prev;
			head = oldTail;
			
		}
		
		/* Mutating Head */ 
		else if(index == 0) {
			
			cdNode<T> dest = get(destination);
			cdNode<T> oldHead = head;			
			head = head.next;			
			oldHead.next = dest.next;
			dest.next = oldHead;			
			oldHead.prev = dest;			
			dest.next.prev = oldHead;

		} else if (destination == 0) {
			
			cdNode<T> ind = get(index);
			ind.prev.next = ind.next;
			ind.next.prev = ind.prev;			
			head.prev = ind;
			ind.next = head;
			ind.prev = tail;			
			head = ind;
		
		} 
		
		/* Mutating Tail */ 
		else if (index == size - 1) {

			cdNode<T> dest = get(destination - 1);
			cdNode<T> oldTail = tail;
			tail = tail.prev;
			
			oldTail.next = dest.next;
			dest.next.prev = oldTail;
			
			dest.next = oldTail;
			oldTail.prev = dest;
			
		} else if (destination == size -1) {
			
			cdNode<T> ind = get(index);
			ind.prev.next = ind.next;
			ind.next.prev = ind.prev;
			tail.next = ind;
			ind.prev = tail;
			ind.next = head;
			tail = ind;			
			
		} else {
			
			cdNode<T> ind = get(index);
			cdNode<T> dest = get(destination);
			ind.prev.next = ind.next;
			ind.next.prev = ind.prev;
			
			dest.next.prev = ind;			
			ind.next = dest.next;
			dest.next = ind;	
			ind.prev = dest;
			
		}
		
	}
		
	public void removeFirstIf(Predicate<T> removeTest) {
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next)  if(removeTest.test(iter.val)) { 
				
			safeRemove(iter);
			return;
				
		}			
		
	}

	public void removeIf(Predicate<T> removeTest) {
		
		cdNode<T> iter = head;
		for(int i = 0 ; i < size ; i ++ , iter = iter.next) if(removeTest.test(iter.val)) safeRemove(iter);
		
	}
	
	public void clear() {
		
		size = 0;
				
	}
		
}