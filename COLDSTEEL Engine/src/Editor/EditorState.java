package Editor;

public enum EditorState {

	//cursor can select stuff and an active object is not following it
	GENERIC(true),
	EDITING_ANIMATION(false),
	EDITING_HITBOX(false),
	EDITING_TILESET(true),
	EDITING_STATIC(true),
	EDITING_JOINT(true),
	EDITING_LEVEL(true),
	BUSY(false)
	
	;
	
	public final boolean allowsSelecting;
	
	EditorState(boolean allowsSelecting){
		
		this.allowsSelecting = allowsSelecting;
		
	}
	
	public String toString() {
	
		String name = name().replace('_', ' ');
		String nameSansFirstLetter = name.substring(1);
		nameSansFirstLetter = nameSansFirstLetter.toLowerCase();
		return name.substring(0, 1) + nameSansFirstLetter;
		
	}	
	
}