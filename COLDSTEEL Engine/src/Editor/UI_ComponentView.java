package Editor;

import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.util.ArrayList;

import AudioEngine.Sounds;
import CS.UserInterface;
import CSUtil.RefInt;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.cdNode;
import Core.Direction;
import Core.ECS;
import Core.HitBoxSets;
import Core.SpriteSets;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;
import Core.Entities.EntityFlags;
import Core.Entities.EntityHitBoxes;
import Game.Items.Inventories;
import Game.Items.Items;

public class UI_ComponentView extends UserInterface {

	public UI_ComponentView(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			
			if(editor.activeQuad == null || !(editor.activeQuad instanceof Entities)) {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text_wrap(context , "Select an Entity");
				return;
				
			}


			Entities E = (Entities)editor.activeQuad;
			Object[] activeComps = E.components();
			
			if(E.has(ECS.HORIZONTAL_PLAYER_CONTROLLER)) {
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Horizontal Controller:" , NK_TEXT_ALIGN_LEFT);
				nk_text(context , Float.toString((float)activeComps[ECS.HORIZONTAL_PLAYER_CONTROLLER.offset]) , NK_TEXT_ALIGN_RIGHT);
				
			}
			
			if(E.has(ECS.VERTICAL_PLAYER_CONTROLLER)) {
				
				nk_layout_row_dynamic(context , 20 , 5);
				nk_text(context , "Vert Cont" , NK_TEXT_ALIGN_LEFT);
				nk_text(context , "Time: " + (float)activeComps[Entities.VCOFF] , NK_TEXT_ALIGN_CENTERED);
				nk_text(context , "Max Time: " + (float)activeComps[Entities.VCOFF + 1] , NK_TEXT_ALIGN_CENTERED);
				nk_text(context , "Vel: " + (float)activeComps[Entities.VCOFF + 2] , NK_TEXT_ALIGN_CENTERED);
				nk_text(context , "Jumping: " + (boolean)activeComps[Entities.VCOFF + 3] , NK_TEXT_ALIGN_RIGHT);
			
			}
			
			if(E.has(ECS.COLLISION_DETECTION)) {
																
				nk_layout_row_dynamic(context , 20  , 3);
				nk_text(context , "Collision Detection" , NK_TEXT_ALIGN_LEFT);
				nk_text(context , "Collider " + activeComps[Entities.CDOFF] == null ? "Null" : "Collider" , NK_TEXT_ALIGN_CENTERED);
				nk_text(context , "Scan Radius: " + (float)activeComps[Entities.CDOFF + 1] , NK_TEXT_ALIGN_RIGHT);
				
			}
			
			if(E.has(ECS.GRAVITY_CONSTANT)) {
				
				nk_layout_row_dynamic(context , 20 , 4);
				nk_text(context , "Gravity: " , NK_TEXT_ALIGN_LEFT);
				nk_text(context , "Constant: " + Float.toString((float)activeComps[Entities.GCOFF]) , NK_TEXT_ALIGN_CENTERED);
				nk_text(context , "Additive: " + Float.toString((float)activeComps[Entities.GCOFF + 1]) , NK_TEXT_ALIGN_RIGHT);
				nk_text(context , "Max: " + Float.toString((float)activeComps[Entities.GCOFF + 2]) , NK_TEXT_ALIGN_RIGHT);
				
			}
									
			if(E.has(ECS.HORIZONTAL_DISPLACEMENT)) {
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Horiz Displ" , NK_TEXT_ALIGN_LEFT);
				nk_text(context , "Displacement: " + activeComps[Entities.HDOFF + 1], NK_TEXT_ALIGN_RIGHT);
											
			}
			
			if(E.has(ECS.VERTICAL_DISPLACEMENT)) {
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Vert Displ" , NK_TEXT_ALIGN_LEFT);
				nk_text(context , "Displacement: " + activeComps[Entities.VDOFF + 1], NK_TEXT_ALIGN_RIGHT);
				
			}
			
			if(E.has(ECS.ANIMATIONS)) {
				
				EntityAnimations anims = (EntityAnimations)activeComps[Entities.AOFF];
				
				nk_layout_row_dynamic(context , 20 , 4);
				nk_text(context , "Animations: " , NK_TEXT_ALIGN_LEFT);					
				nk_text(context , "Active: " + anims.activeIndex() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);					
				nk_text(context , "Hungup: " + anims.isHungup(), NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				
				if(nk_button_label(context , "List in console")) editor.schedule(Editor::printEntityAnimations);
				
			}
			
			if(E.has(ECS.SCRIPT)) {
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Script: " , NK_TEXT_ALIGN_LEFT);
										
			}
			
			if(E.has(ECS.HITBOXES)) {
				
				nk_layout_row_dynamic(context , 20 , 4);
				nk_text(context , "Hit Boxes: " , NK_TEXT_ALIGN_LEFT);
				
				nk_text(context , "Active: " , NK_TEXT_ALIGN_LEFT);
				EntityHitBoxes hitboxes = (EntityHitBoxes)activeComps[Entities.HOFF];
				nk_text(context , "" + hitboxes.active() , NK_TEXT_ALIGN_LEFT);
				
				if(nk_button_label(context , "List in Console")) editor.schedule(Editor::printEntityHitboxSets);
				
			}
			
			if(E.has(ECS.RPG_STATS)) {
				
				nk_layout_row_dynamic(context , 20 , 6);
				nk_text(context , "Cur L: " + activeComps[Entities.RPGOFF] , NK_TEXT_ALIGN_LEFT);
				nk_text(context , "Max L: " + activeComps[Entities.RPGOFF + 1] , NK_TEXT_ALIGN_LEFT);
				
				nk_text(context , "Cur S: " + activeComps[Entities.RPGOFF + 2] , NK_TEXT_ALIGN_CENTERED);
				nk_text(context , "Max S: " + activeComps[Entities.RPGOFF + 3] , NK_TEXT_ALIGN_CENTERED);
				
				nk_text(context , "Cur M: " + activeComps[Entities.RPGOFF + 4] , NK_TEXT_ALIGN_RIGHT);
				nk_text(context , "Max M: " + activeComps[Entities.RPGOFF + 5] , NK_TEXT_ALIGN_RIGHT);
				
				
			}
			
			if(E.has(ECS.INVENTORY)) {

				nk_layout_row_dynamic(context , 20 , 2);
				if(nk_button_label(context , "Print Items To Console")) editor.schedule(Editor::printEntityInventory);
				
				if(nk_button_label(context , "Print Equipped To Console")) editor.schedule(Editor::printEntityEquippedItems);
				
			}
			
			if(E.has(ECS.FLAGS)) {
				
				nk_layout_row_dynamic(context , 20 , 3);
				EntityFlags flags = (EntityFlags)activeComps[Entities.FOFF];
				nk_text(context , "Flags" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "Size: " + flags.size() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				if(nk_button_label(context , "Print To Console")) editor.schedule(e -> flags.forEach(e::say));
				
			}
			
			if(E.has(ECS.DIRECTION)) {
				
				nk_layout_row_dynamic(context , 20 , 4);
				nk_text(context , "horizontal direction: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , ((Direction)activeComps[Entities.DOFF]).toString() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_text(context , "vertical direction: " , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , ((Direction)activeComps[Entities.DOFF + 1]).toString() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
			}
			
			if(E.has(ECS.AUDIO_EMIT)) {
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Audio Emitter" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				if(nk_button_label(context , "Print to console")) editor.schedule(Editor::printEntitySounds);
				
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
