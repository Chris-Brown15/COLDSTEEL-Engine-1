package Editor;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.offloadFloatAssignment;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toBool;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.nio.ByteBuffer;
import CS.UserInterface;
import CSUtil.DataStructures.CSArray;
import Core.Entities.ECS;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;
import Core.Entities.EntityFlags;
import Core.Entities.EntityHitBoxes;
import Core.Entities.EntityLists;
import Core.Entities.EntityRPGStats;
import Game.Items.Inventories;

public class UI_ComponentEditor extends UserInterface {

	private boolean renderHitBox = false;
	private int previousID = -1;
	ByteBuffer 
		horizControllerPtr = alloc0(ALLOCATOR),
		vertControllerPtr = alloc0(ALLOCATOR),
		colliderCheckPtr = alloc0(ALLOCATOR),
		gravityCheck = alloc0(ALLOCATOR),
		horizontalDisplacementCheck = alloc0(ALLOCATOR),
		verticalDisplacementCheck = alloc0(ALLOCATOR),
		scriptCheck = alloc0(ALLOCATOR),
		animationCheck = alloc0(ALLOCATOR),
		hitboxesCheck = alloc0(ALLOCATOR),
		RPGStatsCheck = alloc0(ALLOCATOR),
		cameraTrack = alloc0(ALLOCATOR),
		directionCheck = alloc0(ALLOCATOR),
		inventoryCheck = alloc0(ALLOCATOR),	
		editColliderCheck = alloc0(ALLOCATOR),
		flagCheck = alloc0(ALLOCATOR),
		audioCheck = alloc0(ALLOCATOR),		
		editStatsCheck = alloc0(ALLOCATOR)
	;
	
	public UI_ComponentEditor(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			
			if(editor.activeQuad == null || !(editor.activeQuad instanceof Entities)) {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text_wrap(context , "Select an Entity");
				return;
				
			}
			
			Entities E = (Entities)editor.activeQuad;			
			if(previousID != E.getID()) {
					
				put(editColliderCheck , (byte) 0);
				renderHitBox = false;
				previousID = E.getID();
				
			}
								
			Object[] comps = E.components();

			nk_layout_row_begin(context , NK_STATIC , 20 , 3);
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context , ECS.HORIZONTAL_PLAYER_CONTROLLER.toString() , horizControllerPtr))
				editor.schedule(e -> e.toggleComponent(E,  ECS.HORIZONTAL_PLAYER_CONTROLLER));

			if(E.has(ECS.HORIZONTAL_PLAYER_CONTROLLER)) {
								
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Speed")) 
					editor.schedule(e -> offloadFloatAssignment("Move Speed" , speed -> comps[Entities.HCOFF] = speed));
				
				
			} 
			
			nk_layout_row_end(context);
			
			nk_layout_row_begin(context , NK_STATIC , 20 , 3);			
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context ,  ECS.VERTICAL_PLAYER_CONTROLLER.toString() , vertControllerPtr))
				editor.schedule(e -> e.toggleComponent(E, ECS.VERTICAL_PLAYER_CONTROLLER));
			
			if(E.has(ECS.VERTICAL_PLAYER_CONTROLLER)) {
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Time")) {
					
					editor.schedule(e -> offloadFloatAssignment("Jump Time (Ticks)" , f -> comps[Entities.VCOFF + 1] = f));
					
				}	
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context, "Velocity")) {
					
					editor.schedule(e -> offloadFloatAssignment("Jump Velocity" , f -> comps[Entities.VCOFF + 2] = f));
					
				}
				
			}
			
			nk_layout_row_end(context);
										
			put(colliderCheckPtr , E.has(ECS.COLLISION_DETECTION));

			nk_layout_row_begin(context , NK_STATIC , 20 , 3);
			nk_layout_row_push(context , 100);
			if(nk_checkbox_label(context , "Collider" , colliderCheckPtr)) editor.schedule(e -> e.toggleComponent(E, ECS.COLLISION_DETECTION));
			
			if(E.has(ECS.COLLISION_DETECTION)) {

				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Scan Radius")) { 
					
					editor.schedule(e -> offloadFloatAssignment("Scan Radius" , f -> comps[Entities.CDOFF + 1] = f));
					
				}
				
				nk_layout_row_push(context , 90);
				if(nk_checkbox_label(context , "Edit Col." , editColliderCheck))  { 
					
					if(toBool(editColliderCheck)) editor.schedule(e -> ((EntityAnimations)comps[Entities.AOFF]).animate(true));
					
				}
				
			}
			
			nk_layout_row_end(context);
			
			put(gravityCheck , E.has(ECS.GRAVITY_CONSTANT));
			
			nk_layout_row_begin(context , NK_STATIC , 20 , 4);
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context , "Gravity" , gravityCheck)) editor.schedule(e -> e.toggleComponent(E, ECS.GRAVITY_CONSTANT));
			
			if(E.has(ECS.GRAVITY_CONSTANT)) {
								
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Constant")) {
					
					editor.schedule(e -> offloadFloatAssignment("Gravity Constant" , f -> comps[Entities.GCOFF] = f));
					
				}
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Additive")) { 
					
					editor.schedule(e -> offloadFloatAssignment("Max Additive" , f -> comps[Entities.GCOFF + 2] = f));
					
				}
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Velocity")) { 
					
					editor.schedule(e -> offloadFloatAssignment("Velocity" , f -> comps[Entities.GCOFF + 3] = f));
					
				}
				
				nk_layout_row_end(context);
				
			}

			nk_layout_row_end(context);
			
			put(horizontalDisplacementCheck , E.has(ECS.HORIZONTAL_DISPLACEMENT));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Horizontal Displacement" , horizontalDisplacementCheck)) {
				
				editor.schedule(e -> e.toggleComponent(E, ECS.HORIZONTAL_DISPLACEMENT));
				
			}

			put(verticalDisplacementCheck , E.has(ECS.VERTICAL_DISPLACEMENT)); 
				
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Vertical Displacement" , verticalDisplacementCheck)) {
				
				EntityLists.toggleComponent(E, ECS.VERTICAL_DISPLACEMENT);
				
			}
			
			put(scriptCheck , E.has(ECS.SCRIPT)); 
				 
			nk_layout_row_begin(context , NK_STATIC , 20 , 4);
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context , "Script" , scriptCheck)) editor.schedule(e -> e.toggleComponent(E, ECS.SCRIPT));
			
			if(toBool(scriptCheck)) {

				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Select")) editor.schedule(Editor::selectEntityScript);
				
				if(comps[Entities.SOFF] != null) {
					
					nk_layout_row_push(context ,90);
					if(nk_button_label(context , "Recompile")) editor.schedule(e -> {

						e.recompileEntityScript();
						renderHitBox = false;								
						
					});
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Pause Script")) { 
						
						editor.schedule(e -> comps[Entities.SOFF + 3] = (boolean) comps[Entities.SOFF + 3] ? false:true);
						
					}	
					
				}
				
			} 

			nk_layout_row_end(context);
						
			put(animationCheck , E.has(ECS.ANIMATIONS));
			
			nk_layout_row_begin(context , NK_STATIC , 20 , 4);
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context , "Animations" , animationCheck)) editor.schedule(e -> e.toggleComponent(E , ECS.ANIMATIONS));
			
			if(toBool(animationCheck)) {
								
				nk_layout_row_push(context ,90);
				if(nk_button_label(context , "Add")) editor.schedule(Editor::addEntityAnimation);

			} 

			nk_layout_row_end(context);
				
			put(hitboxesCheck , E.has(ECS.HITBOXES)); 
									
			nk_layout_row_begin(context , NK_STATIC , 20 , 4);
			nk_layout_row_push(context, 200);
			if(nk_checkbox_label(context, "HitBoxes" , hitboxesCheck)) editor.schedule(e -> {
					
				e.toggleComponent(E ,  ECS.HITBOXES);
				comps[Entities.HOFF] = new EntityHitBoxes();
					
			});
				
			if(toBool(hitboxesCheck)) {
								
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Add")) editor.schedule(Editor::addEntityHitbox);
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Max Boxes")) editor.schedule(Editor::setEntityMaxNumberBoxes);
				
			} 
			
			nk_layout_row_end(context);
			
			put(RPGStatsCheck , E.has(ECS.RPG_STATS)); 
			
			nk_layout_row_begin(context , NK_STATIC , 20 , 2);
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context, "RPG Stats" , RPGStatsCheck)) editor.schedule(e -> {
				
				e.toggleComponent(E, ECS.RPG_STATS);
				comps[Entities.RPGOFF] = new EntityRPGStats(E);
				
			});
			
			if(toBool(RPGStatsCheck)) {
									
				nk_layout_row_push(context , 90);
				if(nk_checkbox_label(context , "Edit Stats" , editStatsCheck));
				
			}
			
			nk_layout_row_end(context);
			
			put(cameraTrack , E.has(ECS.CAMERA_TRACK)); 
			
			nk_layout_row_begin(context , NK_STATIC , 20 , 4);
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context , "Camera Tracks" , cameraTrack)) editor.schedule(e -> e.toggleComponent(E, ECS.CAMERA_TRACK));
			
			if(toBool(cameraTrack)) {
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Horizontal Additive")) {
					
					editor.schedule(e -> offloadFloatAssignment("Camera Horizontal Position" , f -> comps[Entities.CTOFF] = f));
					
				}
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Vertical Additive")) {
					
					editor.schedule(e -> offloadFloatAssignment("Camera Vertical Position" , f -> comps[Entities.CTOFF + 1] = f));
					
				}
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Zoom Factor")) {
					
					editor.schedule(e -> offloadFloatAssignment("Camera Zoom" , f -> comps[Entities.CTOFF + 2] = f));
					
				}
								
			}

			nk_layout_row_end(context);
			
			put(directionCheck , E.has(ECS.DIRECTION));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Direction" , directionCheck)) editor.schedule(e -> e.toggleComponent(E, ECS.DIRECTION));
			
			put(inventoryCheck , E.has(ECS.INVENTORY));
			
			nk_layout_row_begin(context , NK_STATIC , 20 , 4);
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context , "Inventory" , inventoryCheck)) editor.schedule(e -> e.toggleComponent(E, ECS.INVENTORY));
			
			if(E.has(ECS.INVENTORY)) {				
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Max Weight")) {
					
					editor.schedule(e -> offloadFloatAssignment("Max Weight" , f -> ((Inventories) comps[Entities.IOFF]).weightLimit(f)));
					
				}
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Add Item")) editor.schedule(Editor::addItemToEntityInventory);		
				
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Add Equipped")) editor.schedule(Editor::equipItemOnEntity);
				
			} 

			nk_layout_row_end(context);
											
			put(flagCheck , E.has(ECS.FLAGS));

			nk_layout_row_begin(context , NK_STATIC , 20 , 4);
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context , "Flags" , flagCheck)) editor.schedule(e -> {
				
				e.toggleComponent(E, ECS.FLAGS);
				comps[Entities.FOFF] = new EntityFlags(10);
				
			});
			
			if(toBool(flagCheck)) {
								
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Add Flag")) editor.schedule(Editor::addFlagToEntity);
				
			} 
			
			nk_layout_row_end(context);			
			
			put(audioCheck , E.has(ECS.AUDIO_EMIT));
			
			nk_layout_row_begin(context , NK_STATIC , 20 , 4);
			nk_layout_row_push(context , 200);
			if(nk_checkbox_label(context , "Audio Emitter" , audioCheck)) editor.schedule(e -> {
				
				e.toggleComponent(E, ECS.AUDIO_EMIT);
				comps[Entities.AEOFF] = new CSArray<Integer>(10 , -1);
				
			});
			
			if(toBool(audioCheck)) {
				
				nk_layout_row_push(context , 70);
				if(nk_button_label(context , "Add")) editor.schedule(Editor::addSoundToEntity);
				
				nk_layout_row_push(context , 70);
				if(nk_button_label(context, "Remove Sound")) {
					
					//TODO
					
				}							
				
				
			}
			
			nk_layout_row_end(context);
			
			if(renderHitBox) EntityLists.updateHitBoxes(E);

		});

	}

	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
