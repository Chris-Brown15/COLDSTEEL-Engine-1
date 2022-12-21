package Editor;

import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toByte;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import CS.UserInterface;
import Core.Entities.ECS;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;
import Core.Entities.EntityHitBoxes;

public class UI_SpriteEditor extends UserInterface{

	ByteBuffer jointEditorCheck = ALLOCATOR.bytes(toByte(false));
	
	public UI_SpriteEditor(Editor editor , UI_AAAManager manager , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			
			if(!(editor.activeSpriteSet != null && editor.activeQuad != null && editor.activeQuad.isTextured())) return;
			
			editor.schedule(Editor::forceStopPlayingActiveSpriteSet);
			
			nk_layout_row_dynamic(context , 200 , 1);
			if(nk_group_begin(context , "Select Sprite To Edit" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)){
				
				for(int i = 0 ; i < editor.activeSpriteSet.getNumberSprites() ; i++){
					
					if(i % 3 == 0) nk_layout_row_dynamic(context , 20 , 3);							
					if(nk_button_label(context , "Sprite " + i)) editor.activeSpriteID = i;
					
				}
				
				nk_group_end(context);
				
			}
			
			if(editor.activeSpriteID == -1) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context , "Select a sprite to edit");
			
				nk_end(context);
				return;
			
			}
						
			float[] activeSprite = editor.activeSpriteSet.getSprite(editor.activeSpriteID);
			boolean modifiesHitBox = activeSprite.length % 3 != 0;
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Modifying Sprite " + editor.activeSpriteID , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Delete")) editor.schedule(Editor::deleteSpriteFromSet);
			 
			if(nk_button_label(context , "Update Sprite " + editor.activeSpriteID)) editor.schedule(Editor::updateSpriteFromSet);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context, "Replace With Selection")) editor.schedule(Editor::replaceSprite);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Activate Hit Box")) editor.schedule(Editor::activeSpriteActivateHitBox);				
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Swap to Sprite " + editor.activeSpriteID)) editor.schedule(edi -> edi.activeQuad.swapSpriteFast(activeSprite));
			
			nk_layout_row_dynamic(context , 20 , 2);
			if(nk_checkbox_label(context , "Joint Editor" , put(jointEditorCheck , manager.jointEditor.showing()))) { 
				
				if(manager.jointEditor.showing()) manager.jointEditor.hide();
				else manager.jointEditor.show();
				
			}
				
			nk_layout_row_dynamic(context , 20 , 2);
			
			nk_text(context , "Saving Joint Marker: " , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_LEFT);
			nk_text(context , editor.jointMarkers.size() > 0 ? "true":"false" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
								
			//set up a way to know what is being saved
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Granular Details" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			//display UV coordinates for selected sprite
			nk_layout_row_dynamic(context , 20 , 4);
			nk_text(context , "Left U:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			nk_text(context , "" + activeSprite[0] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);				
			nk_text(context , "Right U:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			nk_text(context , "" + activeSprite[1] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			
			nk_layout_row_dynamic(context , 20 , 4);
			nk_text(context , "Top V:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			nk_text(context , "" + activeSprite[2] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);				
			nk_text(context , "Bottom V" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			nk_text(context , "" + activeSprite[3] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
					
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Width: " + activeSprite[4] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			nk_text(context , "Height: " + activeSprite[5] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
		
			Entities E;				
			//end the element if we are not selecting an entity or we are selecting an entity who does not have animations
			if(!(editor.activeQuad instanceof Entities) || !(E = (Entities) editor.activeQuad).has(ECS.ANIMATIONS)) return;
			
			Object[] comps = E.components();
			
			boolean hasSet = ((EntityAnimations)(comps[Entities.AOFF])).hasSpriteSet(editor.activeSpriteSet);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , E.name() + " has " + editor.activeSpriteSet.name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , Boolean.toString(hasSet) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			if(!hasSet) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Add Set")) editor.schedule(Editor::addActiveSpriteSetToEntity);
				
			}
			
			if(E.has(ECS.HITBOXES)) {
			
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Modifies a Hitbox" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , Boolean.toString(modifiesHitBox) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				if(modifiesHitBox) {
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Modifies Hitbox: ", NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
					nk_text(context , (int)activeSprite[activeSprite.length - 1] + ", " + ((EntityHitBoxes)comps[Entities.HOFF]).get((int) activeSprite[activeSprite.length - 1]).name(), NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					
				}
				
			}				
			
			boolean modifies = activeSprite.length % 3 != 0 && activeSprite.length > 7 || activeSprite.length > 6;
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Modifies Joints:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			nk_text(context , Boolean.toString(modifies) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			if(modifies) {
				
				ArrayList<float[]> joints = editor.activeSpriteSet.getJoints(editor.activeSpriteID);
				
				float[] jointSprites;
				
				for(int i = 0 ; i < joints.size() ; i ++) {
					
					jointSprites = joints.get(i);
					
					nk_layout_row_dynamic(context , 20 , 3);
					nk_text(context , "ID: " + jointSprites[0] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
					nk_text(context , "X Off: " + jointSprites[1] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
					nk_text(context , "Y Off: " + jointSprites[2] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);
					
				}
				
			}

		});
		
	}

	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
