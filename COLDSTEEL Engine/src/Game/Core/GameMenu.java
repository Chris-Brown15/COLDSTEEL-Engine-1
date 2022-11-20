package Game.Core;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;

import CS.Engine;
import CS.UserInterface;

public class GameMenu extends UserInterface {

	private static final int options = NK_WINDOW_TITLE|NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR; 
	
	public GameMenu(Engine engine , GameRuntime runtime) {
		
		super("Paused" , currentWindowDimensions[0] / 2 - 150 , 80 , 300 , 350 , options , options);
		
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Return")) hide();
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Save")) {
				
			}

			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Load Most Recent")) {
				
			}
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Load")) {
				
			}
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Options")) {
				
			}
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Main Menu")) runtime.leaveGame();

			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Exit Program")) engine.closeOverride();
			
		});
	
		show = false;
		
	}
	
	public void toggle() {
		
		show = show ? false:true;
		
	}
	
	public void show() {
		
		show = true;
		
	}

	public void hide() {
		
		show = false;
		
	}
		
}
