package CSUtil.Dialogs.Debug;

import CS.Engine;
import CS.UserInterface;
	
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;


public class GameDebug extends UserInterface {

	private static final int options = NK_WINDOW_BORDER|NK_WINDOW_TITLE|NK_WINDOW_MOVABLE;
	
	public GameDebug(Engine engine) {
		
		super("Game Debug" , 5 , 5 , 350 , 600 , options , options);
		 
		layoutBody((stackFrame) -> {
			
			
			
		});
		
	}

}
