package Game.Player;

import static CSUtil.BigMixin.toByte;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_DOWN;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_UP;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_label;

import java.io.File;

import org.lwjgl.nuklear.NkRect;

import Core.NKUI;

/**
 * 
 * UI Element that users use to select a save to load
 *
 *
 */
public class PlayerLoader implements NKUI{

	private final File[] saves;	
	private NkRect rect = NkRect.malloc(allocator).set(810, 540, 300, 400);
	private boolean uiExpanded = false;
	private String playerSavePath = null;
	
	public PlayerLoader(){
		
		saves = new File(CS.COLDSTEEL.data + "saves/").listFiles();
		
	}
		
	public void layout() {
		
		if(nk_begin(context , "Load" , rect , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {

			nk_layout_row_dynamic(context , 30 , 1);
			allocator.push();			
			if(nk_selectable_symbol_label(context , uiExpanded ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_UP , "Saves" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED , toByte(allocator , uiExpanded))) 
				uiExpanded = uiExpanded ? false : true;
			allocator.pop();
			
			if(uiExpanded) for(int i = 0 ; i < saves.length ; i ++) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , saves[i].getName())) playerSavePath = saves[i].listFiles()[0].getAbsolutePath();
				
			}
			
		}
	
		nk_end(context);
		
	}
	
	/**
	 * 
	 * @return either {@code null} or the absolute path of the selected save.
	 */
	public String load() {
		
		return playerSavePath;
		
	}
	
}
