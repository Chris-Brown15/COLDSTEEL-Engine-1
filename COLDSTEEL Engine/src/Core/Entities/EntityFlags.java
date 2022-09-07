package Core.Entities;

import java.util.function.Consumer;

import CSUtil.DataStructures.CSOHashMap;

/**
 * This class is for the ECS FLAGS component. This class holds a hashed array of flags. Flags are key value pairs consisting of a string name, and a
 * boolean state. We hash flags by their state first and their name secondly.
 * 
 * @author Chris Brown
 *
 */
public class EntityFlags {
	
	private final CSOHashMap<String , String> flags = new CSOHashMap<String , String>(10);
	
	public EntityFlags(int size) {
		
	}
	
	public void add(String name) {
		
		flags.add(name, name);
		
	}
	
	public void remove(String name) {
		
		flags.bucket(name).removeVal(name);
		
	}
	
	public int size() {
		
		return flags.size();
		
	}	
	
	public void forEach(Consumer<String> function) {
		
		flags.forEach((hash , list) -> list.forEachVal(function));
		
	}
		
}