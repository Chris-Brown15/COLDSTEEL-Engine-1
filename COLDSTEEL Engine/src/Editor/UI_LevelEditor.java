package Editor;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toBool;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import CS.UserInterface;
import CSUtil.DataStructures.cdNode;
import Core.Entities.Entities;
import Game.Levels.Triggers;

//TODO: MAKE LIBRARY OF BABEL IN GAME
public class UI_LevelEditor extends UserInterface {

	FloatBuffer triggerWidthMod = ALLOCATOR.floats(0);
	FloatBuffer triggerHeightMod = ALLOCATOR.floats(0);
	
	ByteBuffer 
		showTriggerEditorCheck = alloc0(ALLOCATOR),
		selectTriggersCheck = alloc0(ALLOCATOR),
		loadDoorEditorCheck = alloc0(ALLOCATOR) 
	;
	
	public UI_LevelEditor(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {

		super(title, x, y, w, h, normalOptions, unopenedOptions);
		layoutBody((frame) -> {
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) editor.schedule(Editor::newLevel);
			
			if(nk_button_label(context , "Load")) editor.schedule(Editor::loadLevel);
			
			if(nk_button_label(context , "Delete")) {}
			
			if(editor.currentLevel == null) {
				
				nk_end(context);
				return;
				
			}

			editor.schedule((edi) -> edi.setState(EditorState.EDITING_LEVEL));
			
			if (editor.currentLevel != null) editor.schedule(Editor::renderLoadDoors);
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , editor.currentLevel.gameName() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			if(editor.currentLevel.macroLevel() != null) {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , editor.currentLevel.macroLevel() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Associate to Macrolevel")) editor.schedule(Editor::setLevelsMacroLevel);					
		
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Save Scene as Level")) editor.schedule(Editor::saveLevel);
			
			if(editor.activeQuad != null && editor.activeQuad instanceof Entities) {
				
				Entities E = (Entities) editor.activeQuad;
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , E.name() + ", LID: " + E.getID() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
			}
						
			nk_layout_row_dynamic(context , 20 , 2);
			if(nk_checkbox_label(context , "Edit Load Doors" , loadDoorEditorCheck)) {}
			
			if(nk_button_label(context , "Add Load Door")) editor.schedule(Editor::addLoadDoor);
			
			nk_layout_row_dynamic(context , 20 , 2);
			if(nk_checkbox_label(context , "Edit Triggers" , selectTriggersCheck)) {}
			
			if(nk_button_label(context , "Add Trigger")) editor.schedule(Editor::addTrigger);
			
			if(editor.currentTrigger != null) if(nk_checkbox_label(context , "Show Trigger Editor" , showTriggerEditorCheck));
			
			if(toBool(selectTriggersCheck)) {
				
				nk_layout_row_dynamic(context , 200 , 1);
				if(nk_group_begin(context , "Triggers" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
					
					cdNode<Triggers> iter = editor.currentLevel.triggers().get(0);
					for(int i = 0 ; i < editor.currentLevel.triggers().size() ; i ++ , iter = iter.next) {
						
						if(i % 2 == 0) nk_layout_row_dynamic(context , 20 , 2);
						if(nk_button_label(context , iter.val.name())) { 
							
							Triggers selected = iter.val;
							editor.schedule((edi) -> edi.setCurrentTrigger(selected));
							
						}
						
					}
					
					nk_group_end(context);
					
				}
								
			}
						
			if(editor.currentTrigger == null) {
				
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , editor.currentTrigger.name() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Rename " + editor.currentTrigger.name())) editor.schedule(Editor::renameActiveTrigger);
			
			if(nk_button_label(context , "Remove " + editor.currentTrigger.name())) { 

				editor.schedule(Editor::removeActiveTrigger);
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context, "Add Condition Area")) editor.schedule(edi -> edi.currentTrigger.addConditionArea());

			if(nk_button_label(context , "Add Effect Area")) editor.schedule(edi -> edi.currentTrigger.addEffectArea());
			
			if(editor.currentTriggerBound == null) {
				
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Mod " + editor.currentTrigger.name() + " Condition Area " + editor.currentTriggerBound.getID() + " Width" , -99999f , triggerWidthMod , 9999999f , 1 , 1);

			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Mod " + editor.currentTrigger.name() + " Effect Area " + editor.currentTriggerBound.getID()  + " Height" , -99999f , triggerHeightMod , 9999999f , 1 , 1);
			
			editor.currentTriggerBound.modWidthBi(triggerWidthMod.get(0));
			editor.currentTriggerBound.modHeightUp(triggerHeightMod.get(0));
			put(triggerWidthMod , 0f);
			put(triggerHeightMod , 0f);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Remove " + editor.currentTriggerBound.getID())) editor.schedule(Editor::removeCurrentTrigger);
			
			editor.scene.entities().forEach(entity -> {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , entity.name() + ", LID:  " + entity.LID() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
			});
			
		});

	}
	
	void show() {
		
		show = true;
		
	}

	void hide() {
		
		show = false;
		
	}

}
