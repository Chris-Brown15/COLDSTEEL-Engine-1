package Core;

public enum Direction {

	UP , DOWN , LEFT , RIGHT , NONE;

	public boolean equals(Direction compare) {
		
		if(this == compare) return true;
		return false;
		
	}
	
	public Direction opposite() {
		
		switch(this) {
		
			case UP: return DOWN;
			case DOWN: return UP;
			case LEFT: return RIGHT;
			case RIGHT: return LEFT;
			case NONE: return null;
			default: return null;
			
		}		
		
	}
	
	public static Direction parse(String target) {
		
			 if(target.equals("UP")) return Direction.UP;
		else if(target.equals("DOWN")) return Direction.DOWN;
		else if(target.equals("LEFT")) return Direction.LEFT;
		else if(target.equals("RIGHT")) return Direction.RIGHT;
		else return Direction.NONE;
		
	}
	
	public Direction copy() {
		
		return Direction.this;
		
	}
		
}
