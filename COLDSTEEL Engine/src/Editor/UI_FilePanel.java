package Editor;

import static CSUtil.BigMixin.alloc1;
import static CSUtil.BigMixin.toBool;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.nio.ByteBuffer;
import CS.Engine;
import CS.UserInterface;

public class UI_FilePanel extends UserInterface {
	
	private ByteBuffer printFPSCheck = alloc1(ALLOCATOR);
	
	public UI_FilePanel(Editor editor , String title, float x, float y, float w, float h, int normalOptions , int unopenedOptions) {

		super(title, x, y, w, h, normalOptions , unopenedOptions);
		
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Back To Main Menu")) editor.schedule(Editor::leaveEditor);
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "New Script")) editor.schedule(Editor::createScriptFile);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Open Script")) editor.schedule(Editor::loadScriptFile);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "FPS Print" , printFPSCheck)) Engine.printFPS  = toBool(printFPSCheck) ;
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Close Program")) editor.schedule(Editor::engineShutDown);
						
			nk_layout_row_dynamic(context , 30 , 1);
			nk_text(context , "COLDSTEEL Engine!" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
		});
		
	}
	
	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
