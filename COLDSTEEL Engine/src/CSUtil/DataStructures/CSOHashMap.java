package CSUtil.DataStructures;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import CSUtil.RefInt;

/**
 * This class represents a minimalistic open hash map solution. A size is specified initially and an array of CSLinked's is allocated.
 * Hashes are modded and the object is placed in the resulting index at the end. Thus the time complexity of this data structure for searching
 * is at worst O(n).  
 * 
 * @author Chris Brown
 *
 * @param <T> — type
 */

public class CSOHashMap <T , Hasher>{
	
	private CSArray<CSLinked<T>> backing;
	private int size;
	private final Function<Hasher , Integer> hashScheme; 
	
	public CSOHashMap(int size) {
		
		backing = new CSArray<CSLinked<T>>(size);
		this.size = size;
		for(int i = 0 ; i < size ; i ++) backing.add(new CSLinked<T>());
		hashScheme = (hasher) -> {
			
			int hashCode = hasher.hashCode();
			return hashCode == 0 ? 0 : Math.abs(hashCode) % size;
		};
		
	}

	public CSOHashMap(int size , Function<Hasher , Integer> hashScheme) {
		
		backing = new CSArray<CSLinked<T>>(size);
		this.size = size;
		for(int i = 0 ; i < size ; i ++) backing.add(new CSLinked<T>());
		this.hashScheme = hashScheme;
		
	}
	
	public T remove(T val , Hasher hashSource) {
		
		return backing.get(hashScheme.apply(hashSource)).removeVal(val).val;

	}
	
	public void add(T val , Hasher hashSource) {
		
		backing.get(hashScheme.apply(hashSource)).add(val);
	}

	public void add(T val , int hashCode) {
		
		backing.get(hashCode).add(val);
		
	}
	
	public void forOnly(Predicate<CSLinked<T>> test , BiConsumer<Integer , CSLinked<T>> function) {
		
		for(int i = 0 ; i < backing.size() ; i ++) {
			
			if(backing.get(i) == null) continue;
			if(test.test(backing.get(i))) function.accept(i, backing.get(i));
			
		}
				
	}
	
	public void forEach(BiConsumer<Integer , CSLinked<T>> function) {
	
		forOnly(list -> list != null , (i , list) -> function.accept(i , list));
		
	}
	
	public boolean has(T value , Hasher hash) {
		
		return backing.get(Math.abs(hash.hashCode() % size)).has(value);
		
	}
	
	public CSLinked<T> bucket(Hasher hash){
		
		return backing.get(hash.hashCode() % size);
		
	}

	public CSLinked<T> bucket(int hash){
		
		return backing.get(hash);
		
	}
	
	public int size() {
		
		RefInt size = new RefInt(0);
		forEach((hash , list) -> size.add(list.size()));
		return size.get();
		
	}
	
}