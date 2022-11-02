package CSUtil.DataStructures;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import CS.COLDSTEEL;

/**
 * Thin and simple wrapper over an array. Automatic resizing is available or restricted if desired. 
 * 
 * @author Chris Brown
 *
 */
public class CSArray<T> {
	
	private int current = 0;
	private int size;
	private Object[] arr;
	public int resizeStep = -1;
	
	public CSArray(int size){
		
		this.size = size;
		arr = new Object[this.size];
				
	}
		
	public CSArray(int size , int resizeStep) {
		
		this.size = size;
		arr = new Object[this.size];
		this.resizeStep = resizeStep;
		
	}
	
	public CSArray(T[] source){
		
		arr = source;
		current = source.length;
		size = source.length;
				
	}

	public CSArray(T[] source , int resizeStep){
		
		arr = source;
		current = source.length;
		size = source.length;
		this.resizeStep = resizeStep;		
		
	}
	
	
	/**
	 * Adds val if possible. Adding is possible if either current < size or current == size and autoResize is on.  
	 * 
	 * @param val — object to be added
	 */
	public void add(T val) {
				
		//at capacity
		if(resizeStep > 0 && current == size) {
			
			Object[] newArr = new Object[size = resizeStep == -1 ? size *= 2 : size + resizeStep];
			System.arraycopy(arr, 0, newArr, 0, current);
			arr = newArr;
			arr[current++] = val;
			
		} else if (current < size) arr[current++] = val;	
				
	}
	
	/**
	 * Removes the element at index and returns it, shifting all subsequent elements over.
	 * 
	 * @param index an index into this array to get
	 * @return a value at an index
	 * 
	 * @throws IndexOutOfBoundsException if index is out of bouns
	 * @throws NullPointerException if the elment at index is null
	 */
	public T remove(int index) {
		
		if(COLDSTEEL.DEBUG_CHECKS) {
			
			if(index >= size) throw new IndexOutOfBoundsException("Out of bounds for CSArray of size " + size);
			if(arr[index] == null) throw new NullPointerException(index + " points to an empty element in this CSArray");
			
		}
				
		@SuppressWarnings("unchecked") T removed = (T)arr[index];
		for(int i = index ; i < current ; i ++) {
		
			arr[i] = arr[i + 1];
			
		}
		
		if(index == current) current--;
		return removed;
		
	}
	
	@SuppressWarnings("unchecked")
	public void remove(T val) {
		
		for(int i = 0 ; i < size ; i ++ ) if(((T)arr[i]).equals(val)) {
			
			remove(i);
			
		}
		
	}
	
	public int size() {
		
		return current;
		
	}
	
	public int remaining() {
		
		return size - current;
		
	}
	
	/**
	 * Puts val at index, shfiting everything else forward. If the array can resize, and resizing would be necessary to fit all the elements, it resizes.
	 * If on the other hand the array would need to resize to fit everything, and it is disallowed from resizing, the last element will be lost
	 * 
	 * @param index — the index val will reside in after this call
	 * @param val — an object to add
	 */
	public void addAt(int index , T val) {
		
		if(index < 0) return;
		
		if(size - current > 0) {

			for(int i = current - 1 ; i >= index ; i --) arr[i + 1] = arr[i];
			arr[index] = val;
			current++;
			
		} else if (resizeStep > 0) {
			
			Object[] newArr = new Object[size *= 2];			
			System.arraycopy(arr, 0, newArr, 0, index + 1);
			System.arraycopy(arr, index , newArr, index + 1, arr.length - index);
			arr = newArr;
			arr[index] = val;
			current++;
			
		}
		
	}
	
	public void replace(int index , T val) {
		
		arr[index] = val;
		
	}
	
	@SuppressWarnings("unchecked") public T get(int index) {
		
		return (T) arr[index];
		
	}
	
	public void set(int index , T val) {
		
		arr[index] = val;
		
	}
	
	public void setIf(int index , T val , Predicate<T> condition) {
		
		if(condition.test(val)) arr[index] = val;
		
	}
	
	public boolean verify(int index) {
		
		return index >= 0 && index < arr.length;
		
	}
		
	public Object[] array() {
		
		return arr;
		
	}
	
	public int length() {
		
		return arr.length;
		
	}
	
	@SuppressWarnings("unchecked") public String toString() {
		
		String str = "";		
		for(int i = 0 ; i < current ; i ++) str += arr[i] != null ? ((T) arr[i]).toString() + "\n":"null\n";		
		return str;
		
	}
	
	public boolean has(T val) {
		
		for(int i = 0 ; i < size ; i ++) if(arr[i] == val) return true;
		return false;
		
	}

	public void clear() {
		
		arr = new Object[size];
		current = 0;
		
	}

	public <Output> Output apply(Function<CSArray<T> , Output> function) {
	
		return function.apply(this);
		
	}
	
 	@SuppressWarnings("unchecked") public void forEach(Consumer<T> function) {
		
		for(int i = 0 ; i < size ; i ++) if(arr[i] != null) function.accept((T) arr[i]);
		
	}
 	
 	public void initialize(Supplier<T> initializer) {
 		
 		for(int i = 0 ; i < size ; i++) arr[i] = initializer.get();
 		
 	}
	
 	public int numberNonNullElements() {
 		
 		int nonNulls = 0;
 		for(int i = 0 ; i < arr.length ; i ++) if (arr[i] != null) nonNulls++;
 		return nonNulls;
 		
 	}
 	
}
