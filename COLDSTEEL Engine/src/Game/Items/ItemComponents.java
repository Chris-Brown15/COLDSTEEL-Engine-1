package Game.Items;

public enum ItemComponents {
	
	EQUIPPABLE(0),
	USABLE(1),
	MATERIALS(2),
	HITBOXABLE(3),
	CONSUMABLE(4),
	FLAGS(5),
	
	;
	
	public final int index;
	ItemComponents(int index){
		
		this.index = index;
		
	}
	
	public String toString() {
		
		String name = name();
		String[] splitName = name.split("_");
		name = "";
		String current , nameSansFirst;
		for(int i = 0 ; i < splitName.length -1 ; i ++) {
			
			current = splitName[i];
			nameSansFirst = current.substring(1);
			nameSansFirst = nameSansFirst.toLowerCase();
			name += current.substring(0 , 1) + nameSansFirst + " ";
			
		}
		//avoid putting a trailing space
		current = splitName[splitName.length - 1];
		nameSansFirst = current.substring(1);
		nameSansFirst = nameSansFirst.toLowerCase();
		name += current.substring(0 , 1) + nameSansFirst;
						
		return name;
		
	}
	
	public static ItemComponents parse(String ICStoString) {
		
		return switch(ICStoString) {
		
			case "Equippable" -> ItemComponents.EQUIPPABLE;
			case "Usable" -> ItemComponents.USABLE;
			case "Materials" -> ItemComponents.MATERIALS;
			case "Hitboxable" -> ItemComponents.HITBOXABLE;
			case "Consumable" -> ItemComponents.CONSUMABLE;
			case "Flags" -> ItemComponents.FLAGS;
			
			default -> throw new IllegalArgumentException("Unexpected value: " + ICStoString);
		
		};
		
	}
	
}

