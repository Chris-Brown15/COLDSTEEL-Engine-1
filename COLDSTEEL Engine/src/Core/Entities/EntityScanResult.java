package Core.Entities;

import Core.ECS;

/**
 * This small class is used as a way to handle scans for entities in way that ensures scripts cannot break encapsulation.
 * To prevent breaking of encapsulation, the Entity result field is protected, so scripts, which require fields and methods
 * to be public in order to be used, cannot see it. As well, scripts cannot create instances of this class because res is reqired.
 * 
 * @author Chris Brown
 *
 */

public class EntityScanResult {

	final Entities result;
	public final float xDistance;
	public final float yDistance;
	
	public ECS[] matchingComps;
	
	public EntityScanResult(Entities res , float xDist , float yDist) {
		
		result = res;
		xDistance = xDist;
		yDistance = yDist;
		
	}

	public void setMatchingComps(ECS...comps) {
		
		this.matchingComps = comps; 
		
	}

	public boolean hasEntity() {
		
		return result != null;
		
	}
	
}
