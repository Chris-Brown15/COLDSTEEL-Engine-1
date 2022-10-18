package CSUtil.DataStructures;

/**
 * Hash Map implementing the Hopscotch hashing tecnique to resolve collisions.
 * 
 * @author Chris Brown
 *
 */
@SuppressWarnings("unchecked")
public class CSHHashMap <K , V> {

	//size of neighborhoods
	private static final byte WORD = 16;

	private int size = 17;
	private Tuple3<Short , K , V>[] entries;
	
	public CSHHashMap(int size) {
		
		this.size = size;
		
	}

	public CSHHashMap() {}
	
	{		
		entries = new Tuple3[size];
	}

	public int size() {
		
		return size;
		
	}
	
	public void put(K key , V value) {
		
		int index = key.hashCode() % size;
		
		if(entries[index] == null) {
			
			entries[index] = new Tuple3<Short , K , V>((short) 0 , key , value);
			//i represents some index ahead of index 
			for(int i = index  + 1 , count = 1 ; count < WORD ; i ++ , count ++) {
				
				if(entries[i] != null) {
					
					//i - index represents how far entries[i] is from the newly added element, thus which bit to set
					entries[i].first = (short) (entries[i].first | (short)Math.pow(2, i - index));
					//sets the bit of the newly added element
					entries[index].first = (short) (entries[index].first | (short)Math.pow(2 , i));
					
				}
				
			}
			
		} else {
						
			int iters = 0; 
			for(int i = index ; iters < WORD ; i ++ , iters++) {
				
				if(i == size) i = 0;
				
				if(entries[i] == null) {
					
					entries[i] = new Tuple3<Short , K , V>((short) 0 , key , value);
					for(int j = i , count = 0 ; count < WORD ; count ++ , j ++) {
						
						if(entries[j] != null) entries[i].first = (short) (entries[i].first.shortValue() | (short)Math.pow(2 , j));
						
					}
					
					for(int j = i , count = 0 ; count < WORD ; count ++ , j --) {
						
						if(entries[j] != null) entries[i].first = (short) (entries[i].first.shortValue() | (short)Math.pow(2 , j));
						
					}
						
				}
				
			}
			
			iters = 0 ;
			for(int i = index ; iters < WORD ; i -- , iters++) {
				
				if(i == 0) i = size - 1;
				
				if(entries[i] == null) {
					
					entries[i] = new Tuple3<Short , K , V>((short) 0 , key , value);
					for(int j = i , count = 0 ; count < WORD ; count ++ , j ++) {
						
						if(entries[j] != null) entries[i].first = (short) (entries[i].first.shortValue() | (short)Math.pow(2 , j));
						
					}
					
					for(int j = i , count = 0 ; count < WORD ; count ++ , j --) {
						
						if(entries[j] != null) entries[i].first = (short) (entries[i].first.shortValue() | (short)Math.pow(2 , j));
						
					}
						
					
				}	
				
			}
			
		}
		
	}
	
}