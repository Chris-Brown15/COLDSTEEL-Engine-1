package Game.Levels;

/**
 * Use this to select what a trigger condition or effect acts upon.
 * 
 */
public enum TriggerSubjectType {

	PLAYER,
	ID,
	FILE,
	LEVEL,
	NONE,
	
	;
	
	public String toString() {
		
		return name();
		
	}
	
}