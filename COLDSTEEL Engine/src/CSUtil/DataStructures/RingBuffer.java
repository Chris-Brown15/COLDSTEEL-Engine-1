package CSUtil.DataStructures;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class implements the Ring Buffer data structure. A predefined, constant buffer.length array is allocated and elements are simply added
 * and removed circularly. 
 * <br><br> 
 * Implementation based on <a href=https://en.wikipedia.org/wiki/Circular_buffer>  Wikipedia. </a>
 * 
 * 
 * @author Chris Brown
 *
 */
public class RingBuffer <T> {

	private Object[] buffer;
	private int end = 0;
	private int start = 0;	
	private int size = 0;
	
	public RingBuffer(final int size) {
		
		buffer = new Object[size];
		
	}
	
	public void put(T element) {
	
		buffer[end++] = element;
		if(end != buffer.length) size++;
		end %= buffer.length;
		
	}	
		
	@SuppressWarnings("unchecked") public T get() {

		T got = (T) buffer[start];
		buffer[start++] = null;
		start %= buffer.length;
		size--;
		return got;

	}

	@SuppressWarnings("unchecked") public T getInPlace() {

		T got = (T) buffer[start];
		return got;

	}
	
	/**
	 * Returns the element at the next position to be written to and advances the buffer.
	 * 
	 * @return element at the next buffer position to be written to.
	 */
	@SuppressWarnings("unchecked") public T getAndPut() {

		T got = (T) buffer[end++];		
		end %= buffer.length;
		return got;

	}

	@SuppressWarnings("unchecked") public T get(int index) { 
		
		return (T) buffer[index % buffer.length];
		
	}
	
	@SuppressWarnings("unchecked") public void forEach(Consumer<T> callback) {

		for(int i = start , j = 0 ; j < buffer.length ; j ++) if(buffer[i] != null) { 
			
			callback.accept((T) buffer[i]);
			if(i++ == buffer.length - 1) i = 0;
			
		}
		
	}
		
	@SuppressWarnings("unchecked") public void onEach(Function<T , T> callback) {
		
		for(int i = start , j = 0 ; j < buffer.length  ; j ++) if(buffer[i] != null) {
			
			buffer[i] = callback.apply((T) buffer[i]);
			if(++i == buffer.length - 1) i = 0;
			
		}
		
	}
	
	public void rewind(int offsetFromCurrent) {
		
		if(offsetFromCurrent == 0) return;
		
		//snaps an out of bound value to buffer length
		if(offsetFromCurrent > buffer.length) offsetFromCurrent %= buffer.length;
		
		if(end - offsetFromCurrent < 0) { 
			
			offsetFromCurrent -= end;
			end = buffer.length - offsetFromCurrent;
			
		} else end -= offsetFromCurrent;
		
	}
	
	public void rewind() {
		
		end = 0;
		start = 0;
		size = 0;
		
	}
	
	public int getEndFromRewind(int offsetFromCurrent) {
		
		if(offsetFromCurrent == 0) return end;
				
		if(offsetFromCurrent > buffer.length) offsetFromCurrent %= buffer.length;
		
		int whatEndWouldBe = end;
		
		if(whatEndWouldBe - offsetFromCurrent < 0) { 
			
			offsetFromCurrent -= whatEndWouldBe;
			whatEndWouldBe = buffer.length - offsetFromCurrent;
			
		} else whatEndWouldBe -= offsetFromCurrent;
		
		return whatEndWouldBe;
		
	}
	
	public boolean has(T element) {
		
		for(int i = 0 ; i < buffer.length; i ++) if(element == buffer[i]) return true;
		return false;
		
	}
	
	public int endIndex() {
		
		return end;
		
	}

	public int startIndex() {
		
		return start;
		
	}
	
	public int capacity() {
		
		return buffer.length;
		
	}
	
	public boolean empty() {
		
		return size == 0;
		
	}

}