package Game.Core;

public enum DamageType {

	PHYSICAL,
	FIRE,
	ICE,
	ELECTRIC,
	BLEEDING,
	POISON,
	MANA_DRAIN,
	BLACK_MAGIC,
	HOLY;

	public float value;
	public float effectFactor;
		
	public void setValue(float amount) {
		
		this.value = amount;
				
	}
	
	public float getValue() {
		
		return value;
		
	}
		
}