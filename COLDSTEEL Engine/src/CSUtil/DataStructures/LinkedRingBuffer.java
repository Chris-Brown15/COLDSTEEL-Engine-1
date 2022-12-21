package CSUtil.DataStructures;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LinkedRingBuffer <T> {

	public final int capacity;
	
	private LRBNode head = null;
	private LRBNode tail = null;
		
	public LinkedRingBuffer(final int size) {	
	
		this.capacity = size;				
		for(int i = 0 ; i < size ; i ++) newNode();	
		
	}
	
	public void put(T elem) {
		
		LRBNode oldTail = tail;
		oldTail.val = elem;
	
		head.prev = oldTail;
		oldTail.next = head;
		
		tail = oldTail.prev;
		head = oldTail;
				
	}
		
	public T get() {
		
		LRBNode oldHead = head;
		T gotten = oldHead.val;
		oldHead.val = null;
		
		head = oldHead.next;
		tail = oldHead;
		
		return gotten;
		
	}
	
	public T getAndPut() {
		
		T element = get();
		put(element);
		return element;
		
	}
	
	private void newNode() {

		if(head == null) head = new LRBNode();
		else if (tail == null) {
			
			tail = new LRBNode();
			tail.next = head;
			tail.prev = head;
			head.prev = tail;
			head.next = tail;
			
		} else {
			
			LRBNode node = new LRBNode();
			tail.next = node;
			node.prev = tail;
			
			head.prev = node;
			node.next = head;
			
			tail = node;
			
		}
		
	}
		
	public void forEach(Consumer<T> callback) {
	
		LRBNode iter = head;
		for(int i = 0 ; i < capacity ; i++ , iter = iter.next) if(iter.val != null) callback.accept(iter.val);
		
	}

	public void forEachIndexed(BiConsumer<Integer , T> callback) {
	
		LRBNode iter = head;
		for(int i = 0 ; i < capacity ; i++ , iter = iter.next) if(iter.val != null) callback.accept(i , iter.val);
		
	}

	/**
	 * Like {@code forEach} but null elements are passed to {@code callback}.
	 * 
	 * @param callback
	 */
	public void forEachUnsafe(Consumer<T> callback) {
	
		LRBNode iter = head;
		for(int i = 0 ; i < capacity ; i++ , iter = iter.next) callback.accept(iter.val);
		
	}
	
	private class LRBNode {
		
		LRBNode next;
		LRBNode prev;
		T val;
				
	}
	
}
