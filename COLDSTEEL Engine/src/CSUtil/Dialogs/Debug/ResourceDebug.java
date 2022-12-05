package CSUtil.Dialogs.Debug;

import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import static CSUtil.BigMixin.toNamePath;

import Audio.SoundEngine;
import CS.UserInterface;

/**
 * 
 * Displays loaded textures and sounds.
 *
 */
public class ResourceDebug extends UserInterface{

	private static final int uiOptions = NK_WINDOW_MOVABLE|NK_WINDOW_BORDER|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE ;
		
	public ResourceDebug(String title, float x, float y) {
		
		super(title , x ,  y, 350 , 600 , uiOptions, uiOptions);

		layoutBody(frame -> {
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Textures" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			Renderer.Renderer.forEachTexture(texture -> {

				if(texture.filledOut()) {
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , toNamePath(texture.filepath()) , NK_TEXT_ALIGN_LEFT);					
					nk_text(context , "Texture ID: " + texture.textureID() , NK_TEXT_ALIGN_RIGHT);
									
				} else {
				
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "UNINITIALIZED TEXTURE" , NK_TEXT_ALIGN_LEFT);					
					
				}				
				
			});
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Sound Files" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			SoundEngine.forEach(sound -> {
			
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 260);
				nk_text(context , sound.name() , NK_TEXT_ALIGN_LEFT);
				nk_layout_row_push(context , 55);
				nk_text(context , "At Index " + sound.ID() , NK_TEXT_ALIGN_RIGHT);
				nk_layout_row_end(context);
				
			});
			
		
			
		});
		
	}	
	
	public void toggle() {
		
		show = show ? false : true;
		
	}
	
}
