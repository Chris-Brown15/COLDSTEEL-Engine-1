package Game.Items;

import Core.Entities.ECS;
import Core.Entities.Entities;

/**
 * 
 * Class wrapping an entity that may or may not hold a private reference to an entity. This class will be used by items and item scripts
 * because it protects against breaking of encapsulation and allowing outsiders to modify an entity directly.
 * 
 * @author Chris Brown
 *
 */
public class EntityOptional {

	final Entities optional;
	public final float xDistance;
	public final float yDistance;
	
	public EntityOptional(Entities result , float xDistance , float yDistance){
		
		this.optional = result;
		this.xDistance = xDistance;
		this.yDistance = yDistance;
		
	}
	
	public boolean hasEntity() {
		
		return optional != null;
		
	}
	
	public boolean has(ECS...comps) {
		
		return optional.has(comps);
		
	}
	
}
