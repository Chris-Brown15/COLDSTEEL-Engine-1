package Core;

public enum ECS {
	
	HORIZONTAL_PLAYER_CONTROLLER(1 ,  1 , new Object[] {2.0f , true , false}),
	COLLISION_DETECTION(		 2 ,  4 , new Object[] {null , 1000f , false}),
	GRAVITY_CONSTANT(			 3 ,  7 , new Object[] {3.0f , 0f , 30f , 5f}),
	HORIZONTAL_DISPLACEMENT(	 4 , 11 , new Object[] {0f , 0f}),
	VERTICAL_PLAYER_CONTROLLER(	 5 , 13 , new Object[] {00f , 30.0f , 5.0f , false , true , null , false}),
	VERTICAL_DISPLACEMENT(		 6 , 20 , new Object[] {0f , 0f}),
	SCRIPT(						 7 , 22 , new Object[] {null , true}),
	ANIMATIONS(					 8 , 24 , new Object[] {null , true}),
	HITBOXES(					 9 , 26 , new Object[] {null}),
	RPG_STATS(					10 , 27 , new Object[] {null}),	
	CAMERA_TRACK(				11 , 28 , new Object[] {-320f , -80f , 16f}),
	DIRECTION(				   	12 , 31 , new Object[] {null , null , true}),
	INVENTORY(					13 , 34 , new Object[] {null}),
	FLAGS(						14 , 35 , new Object[] {null}),
	AUDIO_EMIT(					15 , 36 , new Object[] {null}),
	
	;
	
	/*
	 
	 [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X]
	 [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X]
	 [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X]
	 [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X]
	 [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X]
	 [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X] [X]
	 
	 */
	
	public static final int NUMBER_COMPONENTS = values().length;
	
	public final int index;
	public final int offset;
	public Object[] data;

	ECS(int index , int offset , Object[] data){
		
		this.index = index;
		this.offset = offset;
		this.data = data;
		
	}
	
	public static final int numberComps() {
				
		int numbComps = 0;
		for(ECS x : values()) numbComps += x.data.length;
		return numbComps;		
		
	}
	
	public static ECS componentAtIndex(int index) {
		
		return values()[index];
		
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
	
}