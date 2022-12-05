package CSUtil.DataStructures;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Coockoo Hash Map Implementation where no duplicate keys are allowed.
 * 
 * @author Chris Brown
 *
 */
public class CSCHashMap <T , K> {

	int mapSize;
	
	Tuple2<T , K>[] map1;
	Tuple2<T , K>[] map2;
	
	BiFunction<K , Integer , Integer> hashScheme;
	
	@SuppressWarnings("unchecked") public CSCHashMap(final int mapSize , BiFunction<K , Integer , Integer> hashScheme) {
		
		this.mapSize = mapSize;
		map1 = new Tuple2[mapSize];
		map2 = new Tuple2[mapSize];
		this.hashScheme = hashScheme;
		
	}
	
	public CSCHashMap(final int mapSize) {
		
		this(mapSize , (keyInstance , size) -> Math.abs(keyInstance.hashCode() % size));
		
	}
	
	/* PUT OPERATIONS */
	
	/**
	 * Attemps to put {@code object} in {@code this}, but may fail if {@code key} is found anywhere in the map.
	 * 
	 * @param object — data to store
	 * @param key — key into the map to locate object
	 * @return true if the put operation was successful.
	 */
	public boolean put(T object , K key) {
		
		return put(new Tuple2<T , K>(object , key));
				
	}
	
	/**
	 * Tuple version of {@code put}.
	 * 
	 * @param tuple — Tuple2 representation of an entry in the map.
	 * @return — true if the put operation was successful.
	 */ 
	public boolean put(Tuple2<T , K> tuple) {

		return putInternal(map1 , tuple , 1);
		
	}
	
	private boolean putInternal(final Tuple2<T , K>[] targetMap , Tuple2<T , K> entry , int recursionDepth) {

		int hashedIndex = hashScheme.apply(entry.getSecond(), mapSize);		
		if(hasKey(entry.getSecond())) return false;
		//resize 
		if(recursionDepth == 3) {
			
			resize();
			return putInternal(map1 , entry , 1);

		} else {

			final Tuple2<T , K>[] altMap = alternateMap(targetMap);
			//if the spot we want to add entry is empty and the other map does not already contain entry (we dont want to have dupes)
			if(targetMap[hashedIndex] == null) {			
				
				targetMap[hashedIndex] = entry;
				return true;
				
			} else {
				 			
				if (altMap[hashedIndex] == null) {
					
					altMap[hashedIndex] = entry;
					return true;
					
				} else {
					
					Tuple2<T , K> removed = targetMap[hashedIndex];
					targetMap[hashedIndex] = entry;									
					return putInternal(altMap , removed , ++recursionDepth);
					
				}
				
			}
		
		}
		
	}
	
	/* GET OPERATIONS */
	
	public T get(K key) {
		
		return getInternal(map1 , key);
		
	}
	
	private T getInternal(final Tuple2<T , K>[] map , K key) {
		
		int hashedIndex = hashScheme.apply(key , mapSize);
		Tuple2<T , K> indexed = map[hashedIndex];
		if (indexed != null && indexed.getSecond().equals(key)) return indexed.getFirst();
		else return getInternal(alternateMap(map) , key);
		
	}
	
	public boolean hasKey(K key) {
		
		int hashedIndex = hashScheme.apply(key , mapSize);
		return hasKeyInternal(key , hashedIndex);
		
	}
	
	private boolean hasKeyInternal(K key , int hashedIndex) {

		return (map1[hashedIndex] != null && map1[hashedIndex].getSecond().equals(key)) || 
			   (map2[hashedIndex] != null && map2[hashedIndex].getSecond().equals(key));
		
	
	}

	public boolean hasValue(T object) {
		
		for(int i = 0 ; i < mapSize ; i ++) if(map1[i] != null && map1[i].getFirst().equals(object)) return true;
		for(int i = 0 ; i < mapSize ; i ++) if(map2[i] != null && map2[i].getFirst().equals(object)) return true;
		return false;				
		
	}

	public boolean hasEntry(T object , K key) {
						
		return hasEntry(new Tuple2<>(object , key));
		
	}

	public boolean hasEntry(Tuple2<T , K> entry) {
						
		int hashedIndex = hashScheme.apply(entry.getSecond(), mapSize);
		return (map1[hashedIndex] != null && map1[hashedIndex].getFirst().equals(entry.getFirst()) 
										  && map1[hashedIndex].getSecond().equals(entry.getSecond())) ||
			   (map2[hashedIndex] != null && map2[hashedIndex].getFirst().equals(entry.getFirst()) 
			   							  && map2[hashedIndex].getSecond().equals(entry.getSecond()));
		
	}

	/* REMOVE OPERATIONS */
	
	public Tuple2<T , K> remove(K key) {
		
		int hashedIndex = hashScheme.apply(key, mapSize);
		Tuple2<T , K> removedEntry;
		if((removedEntry = map1[hashedIndex]) != null) map1[hashedIndex] = null;
		else if ((removedEntry = map2[hashedIndex]) != null) map1[hashedIndex] = null;
		return removedEntry;
		
	}
	
	/* ITERATOR OPERATIONS */
	
	/**
	 * Invokes {@code callback} on each map of this hash map. 
	 * 
	 * @param callback — code to invoke on entry arrays
	 */
	public void forEachMap(Consumer<Tuple2<T , K>[]> callback) {
		
		callback.accept(map1);
		callback.accept(map2);
		
	}

	/**
	 * Like {@code forEachMap}, but also provides an integer indexing the map in question.
	 * 
	 * @param callback — code to invoke on entry arrays
	 */
	public void forEachMapIndexed(BiConsumer<Integer , Tuple2<T , K>[]> callback) {
		
		callback.accept(0 , map1);
		callback.accept(1 , map2);
		
	}

	/**
	 * Invokes {@code callback} on each nonnull element within both maps.
	 * 
	 * @param callback — code to invoke on nonnull entries in both entry arrays
	 */
	public void forEachEntry(Consumer<Tuple2<T , K>> callback) {
		
		for(Tuple2<T , K> x : map1) if(x != null) callback.accept(x);
		for(Tuple2<T , K> x : map2) if(x != null) callback.accept(x);
		
	}

	/**
	 * Like {@code forEachEntry}, but the index of the entry in the underlying entry array is also passed into the callback.
	 * 
	 * @param callback — code to invoke on all nonnull entries in both arrays
	 */
	public void forEachEntryIndexed(BiConsumer<Integer , Tuple2<T , K>> callback) {
	
		for(int i = 0 ; i < mapSize ; i ++) if(map1[i] != null) callback.accept(i, map1[i]);
		for(int i = 0 ; i < mapSize ; i ++) if(map2[i] != null) callback.accept(i, map2[i]);
		
	}
	
	private void resize() {

		CSCHashMap<T , K> newStore = new CSCHashMap<T , K>(mapSize <<= 1 , hashScheme);
		forEachEntry(tuple -> newStore.put(tuple));		
		
		map1 = newStore.map1;
		map2 = newStore.map2;
		
	}
	
	private Tuple2<T , K>[] alternateMap(final Tuple2<T , K>[] alternateOfThis) {
		
		return alternateOfThis == map1 ? map2 : map1;

	}
	
	public int size() {
		
		return mapSize;
		
	}
	
}