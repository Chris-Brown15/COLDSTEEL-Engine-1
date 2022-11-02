package Editor;

import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import CS.UserInterface;

/*
 
 Lay the joint over the quad where we want it to be. When this UI element is open, we are not allowed to select another quad, 
 and we can only move the joint where it goes.
 
 */

public class UI_SpriteSetJointEditor extends UserInterface {

	public UI_SpriteSetJointEditor(Editor editor , UI_AAAManager manager , String title, float x, float y, float w, float h, int normalOptions , int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			
			if(editor.activeQuad == null) return;

			float[] activeQuadData = editor.activeQuad.getData();
							
			if(editor.activeSpriteID == -1) {
				
				nk_end(context);
				return;
				
			}
			
			editor.setState(EditorState.EDITING_JOINT);
							
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Sprite " + editor.activeSpriteID + " from " + editor.activeSpriteSet.name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Add Joint Marker")) editor.schedule(Editor::addJointMarkerForSprite);
			
			if(nk_button_label(context , "Clear Joints")) editor.schedule(Editor::removeAllJoints);
			
			if(editor.jointMarkers.size() == 0) {
				
				nk_end(context);
				return;
				
			}
							
			if(editor.activeJoint != null) {
									
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Remove Joint")) editor.schedule(Editor::removeActiveJoint);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Active Joint:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , "" + editor.activeJoint.getID() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
															
				for(int i = 0 ; i < editor.jointMarkers.size() ; i ++) {
					
					if(editor.getJoint(i) == null) continue; 
					else {
						
						float[] markerMid = editor.getJoint(i).getMidpoint();
						float xOff = markerMid[0] - activeQuadData[9];
						float yOff = markerMid[1] - activeQuadData[10];
															
						nk_layout_row_dynamic(context , 20 , 3);
						nk_text(context , "Pos: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
						nk_text(context , "X: " + xOff , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
						nk_text(context , "Y: " + yOff , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
						
					}
				
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
