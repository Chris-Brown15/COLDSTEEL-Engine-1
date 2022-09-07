package Editor;

public enum CursorState {

	SELECTABLE,
	DRAGGING,
	FROZEN
	;
	
	public String toString() {
		
		String name = name().replace('_', ' ');
		String nameSansFirstLetter = name.substring(1);
		return name.substring(0  , 1) + nameSansFirstLetter.toLowerCase();
		
	}
	
	
}
