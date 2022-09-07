package Networking;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import org.lwjgl.nuklear.NkRect;

import Core.NKUI;

/**
 * 
 * UI for creating and joining servers.
 * 
 *
 */
public class MultiplayerMainMenuPage implements NKUI{

	String name = "Multiplayer";
	NkRect rect = NkRect.malloc(allocator).set(1065 , 540 , 350 , 400);
	int options = NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_TITLE;
	
	public void layout() {
		
		if(nk_begin(context , name , rect , options)) {
		
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Start Server")) {
				
				
				
			}
			
		}
		
		nk_end(context);
		
	}
	
}
