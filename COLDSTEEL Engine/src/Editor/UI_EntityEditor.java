package Editor;

import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import CS.UserInterface;
import Core.Entities.Entities;

public class UI_EntityEditor extends UserInterface {

	public UI_EntityEditor(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 30 , 3);					
			if(nk_button_label(context , "New Entity")) editor.schedule(Editor::addEntity);
			if(nk_button_label(context , "Load Entity")) editor.schedule(Editor::loadEntity);
			if(nk_button_label(context , "Delete Entity")) editor.schedule(Editor::deleteEntityFilePath);
			
			if(editor.activeQuad == null || !(editor.activeQuad instanceof Entities)) {
				
				nk_end(context);
				return;
				
			}
			
			Entities E = (Entities) editor.activeQuad;			
			nk_layout_row_dynamic(context , 20 , 2);			
			nk_text(context , "Active Entity:", NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , E.name(), NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			 
			
			nk_layout_row_dynamic(context, 30 , 2);
			if(nk_button_label(context , "Save Entity")) editor.schedule(e -> E.write());						
			if(nk_button_label(context , "Remove Entity")) editor.schedule(Editor::removeActive);
		
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Texture")) editor.schedule(Editor::textureActive);
			
			if(nk_button_label(context , "Remove Color")) editor.schedule(Editor::removeColor);
			
			if(nk_button_label(context , "Filter")) editor.schedule(Editor::applyFilter);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Save Current Sprite as Default")) editor.schedule(e -> E.setDefaultSprite());
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Reset Current Sprite to Default")) editor.schedule(e -> E.resetToDefaultSprite());

			
		});
		
	}

	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
