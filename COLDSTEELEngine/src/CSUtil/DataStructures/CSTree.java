package CSUtil.DataStructures;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Graph Data Structure. CSGraphs are made up of nodes. A node holds a reference to a data value and a linked list of attached nodes.
 * 
 * @author Chris Brown
 *
 * @param <T> — type of the object to be stored in a node
 * @param <H> — variable used to name or identify nodes 
 */
public class CSTree <T , H>{

	tNode<T , H> root;
	
	int size = 0;
	
	public tNode<T , H> add(T val , H id) {
		
		if(size == 0) {
			
			root = new tNode<T , H>(val , id);			
			size++;
			return root;
			
		} else {
			
			size++;
			return root.add(val , id);
			
		}
			
	}
	
	public tNode<T , H> addEmpty(){
		
		if(size == 0) return (root = new tNode<>());
		else return root.add();
		
	}
	
	public tNode<T , H> add(T val , H id , tNode<T , H> sourceNode){
		
		size++;
		return sourceNode.add(val, id);
		
	}
	
	/**
	 * Executes function for each node and all of each node's attached nodes.
	 * 
	 * @param function — function to call on each node
	 */
	public void forEach(Consumer<tNode<T , H>> function) {
		
		forEach(root , function);
				
	}
	
	public void forOnly(Predicate<tNode<T , H>> test , Consumer<tNode<T , H>> function) {
		
		forOnly(root , test , function);
		
	}

	public void forOnly(tNode<T , H> node , Predicate<tNode<T , H>> test , Consumer<tNode<T , H>> function) {
		
		if(test.test(node)) function.accept(node);
		
		cdNode<tNode<T , H>> iter = node.attached.get(0);
		for(int i = 0 ; i < node.attached.size() ; i ++ , iter = iter.next) {
			
			if(iter.val.attached.size() > 0) forOnly(iter.val , test , function);
			else if(test.test(iter.val)) function.accept(iter.val);
			
		}
		
	}
	
	private void forEach(tNode<T , H> node , Consumer<tNode<T , H>> function) {
		
		function.accept(node);

		cdNode<tNode<T , H>> iter = node.attached.get(0);
		for(int i = 0 ; i < node.attached.size() ; i ++ , iter = iter.next) {
			
			if(iter.val.attached.size() > 0) forEach(iter.val , function);
			else function.accept(iter.val);
			
		}
		
	}

	/**
	 * Executes function for each node and all of each node's attached nodes.
	 * 
	 * @param function — function to call on each node
	 */
	public <O> void forEach(BiConsumer<O , tNode<T , H>> function , O other) {
		
		forEach(root , function , other);
				
	}
	
	private <O> void forEach(tNode<T , H> node , BiConsumer<O , tNode<T , H>> function , O other) {
		
		function.accept(other , node);

		cdNode<tNode<T , H>> iter = node.attached.get(0);
		for(int i = 0 ; i < node.attached.size() ; i ++ , iter = iter.next) {
			
			if(iter.val.attached.size() > 0) forEach(iter.val , function , other);
			else function.accept(other , iter.val);
			
		}
		
	}
	
	public tNode<T , H> get(H id){
				
		if(root.ID.equals(id)) return root;
		else {
			
			cdNode<tNode<T , H>> query = root.attached.getIfExists(node -> node.ID.equals(id));
			return query != null ? query.val : null;
			
		}
		
	}
	
	/**
	 * Depth first search for an element, returning the element if it was found, else null
	 * 
	 * @param node
	 * @param ID
	 * @return
	 */
	public tNode<T , H> searchFor(H ID) {
		
		return searchFor(root , ID);
		
	}
		
	public tNode<T , H> searchFor(tNode<T , H> node , H ID) {
		
		if(node.ID.equals(ID)) return node;
		
		cdNode<tNode<T , H>> iter = node.attached.get(0);
		for(int i = 0 ; i < node.attached.size() ; i ++ , iter = iter.next) {
			
			if(iter.val.ID.equals(ID)) return iter.val;
			else if (node.attached.size() > 0) {
				
				tNode<T , H> found = searchFor(iter.val , ID);
				if(found != null) return found;
				
			}
			
		}
		
		return null;
		
	}
	
	public int size() {
		
		return size;
		
	}
	
}