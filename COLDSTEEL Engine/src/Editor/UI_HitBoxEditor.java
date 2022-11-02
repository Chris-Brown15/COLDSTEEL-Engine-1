package Editor;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toBool;
import static CSUtil.BigMixin.translateArray;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
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
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_radio_label;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_text;
import static org.lwjgl.nuklear.Nuklear.nk_slider_int;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import CS.UserInterface;
import CSUtil.RefInt;
import Core.Direction;
import Core.SpriteSets;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;
import Core.Entities.EntityHitBoxes;

public class UI_HitBoxEditor extends UserInterface {

	private ByteBuffer 
		hotBoxCheck = alloc0(ALLOCATOR),
		coldBoxCheck = alloc0(ALLOCATOR),
		leftDirRadio = alloc0(ALLOCATOR),
		rightDirRadio = alloc0(ALLOCATOR),	
		showAnimationsCheck = alloc0(ALLOCATOR),
		showHitBoxSetsCheck = alloc0(ALLOCATOR) 
	;
	
	ByteBuffer sliderSelect = alloc0(ALLOCATOR);
	ByteBuffer[] spritesetChecks , hitboxsetChecks;
	
	SpriteSets selectedSet;
	float[] selectedSprite;
	int selectedSpriteIndex;
		
	public UI_HitBoxEditor(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);

		layoutBody((frame) -> {
			
			HitBoxSetMarker marker = editor.hitboxMarker;
			
			nk_layout_row_dynamic(context , 30 , 3);			
			if(nk_button_label(context , "New HitBoxSet")) editor.schedule(Editor::newHitboxSet);
			
			if(nk_button_label(context, "Load HitBoxSet")) editor.schedule(Editor::loadHitboxSet);
			
			if(nk_button_label(context , "Delete HitBoxSet")) editor.schedule(Editor::deleteHitboxSet);			

			if(editor.activeQuad == null) {
				
				nk_end(context);
				return;
				
			}

			if(editor.isHitboxsetAlive()) {
				
				editor.setState(EditorState.EDITING_HITBOX);
				
				nk_layout_row_dynamic(context , 20 , 1);				
				nk_text(context , marker.editingName, NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);				
				nk_text(context , "Number of Hitboxes:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , "" + marker.getSize() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);
				
				nk_layout_row_dynamic(context , 30 , 2);						
				if(nk_button_label(context , "Add Hit Box")) editor.schedule(Editor::addHitbox);
				
				if(nk_button_label(context , "Remove current Hit Box") && editor.isHitboxSelected()) editor.schedule(Editor::removeHitbox);
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Remove All Hit Boxes")) editor.schedule(Editor::removeAllHitboxes);

				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Active Hit Box:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , marker.active + "" , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
								
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_checkbox_label(context , "Use Slider to Select HitBox" , sliderSelect)) ;
				
				if(toBool(sliderSelect)) {
					
					IntBuffer active = frame.ints(editor.hitboxMarker.active);
					nk_slider_int(context , -1 , active , marker.getSize() - 1 , 1);
					editor.schedule(e -> marker.active = active.get());
					
				}				
								
				if(editor.isHitboxSelected()) {
										
					FloatBuffer sliders = frame.floats(0f , 0f);
					nk_layout_row_dynamic(context , 30 , 1);
					nk_property_float(context , "Slide Horizontally" , -999 , sliders.slice(0 , 1) , 999 , 1 , 1);
					nk_layout_row_dynamic(context , 30 , 1);
					nk_property_float(context , "Slide Vertically" , -999 , sliders.slice(1 , 1) , 999 , 1 , 1);
					
					editor.schedule(e -> translateArray(marker.active() , sliders.get(0) , sliders.get(1)));
					
					nk_layout_row_dynamic(context , 30 , 2);
					put(hotBoxCheck , marker.hotBoxes[marker.active] != -1);
					if(nk_checkbox_label(context , "Hot Box" , hotBoxCheck)) editor.schedule(Editor::toggleActiveHitboxHot);
						
					
					put(coldBoxCheck , marker.coldBoxes[marker.active] != -1);
					if(nk_checkbox_label(context , "Cold Box" , coldBoxCheck)) editor.schedule(Editor::toggleActiveHitboxCold);
								
					nk_layout_row_dynamic(context , 30 , 2);
					if(nk_button_label(context , "Save As")) editor.schedule(Editor::saveHitboxSetAs);							
					
					if(nk_button_label(context , "Save")) editor.schedule(Editor::saveHitboxSet);
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Default Direction" , NK_TEXT_ALIGN_CENTERED);						
					
					nk_layout_row_dynamic(context , 20 , 2);
					
					put(leftDirRadio , marker.defaultDirection == Direction.LEFT);
					put(rightDirRadio , marker.defaultDirection == Direction.RIGHT);
					
					if(nk_radio_label(context , "Left" , leftDirRadio)) editor.schedule(e -> marker.defaultDirection = Direction.LEFT);
					if(nk_radio_label(context , "Right" , rightDirRadio)) editor.schedule(e -> marker.defaultDirection = Direction.RIGHT);
					
				}
				
			}
			
			
			if(!editor.activeQuadValidEntityForHitboxes()) {
				
				nk_end(context);
				return;
									
			}
					
			editor.schedule(e -> e.setState(EditorState.EDITING_HITBOX));
			
			Entities E = (Entities)editor.activeQuad;
			EntityAnimations anims = (EntityAnimations) E.components()[Entities.AOFF];
			EntityHitBoxes entityHitboxes = (EntityHitBoxes) E.components()[Entities.HOFF];

			if(hitboxsetChecks == null) {
				
				hitboxsetChecks = new ByteBuffer[entityHitboxes.numberSets()];
				for(int i = 0 ; i < entityHitboxes.numberSets() ; i ++) hitboxsetChecks[i] = alloc0(ALLOCATOR);
				
			}
			
			boolean hasHitBox = entityHitboxes.has(marker.toHitBoxSet(E));
						
			if(marker.editingName != null) {
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 300);				
				nk_text(context , E.name() + " has " + marker.editingName + ":" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_layout_row_push(context , 70);				
				nk_text(context , Boolean.toString(hasHitBox) , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);
				nk_layout_row_end(context);
								
				if(!(hasHitBox)) {
				
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_button_label(context , "Add " + marker.editingName)) editor.schedule(e -> {
						
						e.addHitboxSetToEntity();
						hitboxsetChecks = new ByteBuffer[entityHitboxes.numberSets()];
						for(int i = 0 ; i < hitboxsetChecks.length ; i ++)hitboxsetChecks[i] = alloc0(ALLOCATOR);
						
					});
					
				}
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_checkbox_label(context , "Show Animations" , showAnimationsCheck)) {
				
				spritesetChecks = new ByteBuffer[anims.size()];
				for(int i = 0 ; i < anims.size() ; i ++) spritesetChecks[i] = alloc0(ALLOCATOR);
				
			}
			
			if(toBool(showAnimationsCheck)) {
				
				editor.setState(EditorState.EDITING_HITBOX);
				
				nk_layout_row_dynamic(context , 300 , 1);
				if(nk_group_begin(context , "Animations" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
					
					for(int i = 0 ; i < anims.size() ; i ++) {
						
						nk_layout_row_dynamic(context , 20 , 1);
						if(nk_selectable_text(context , anims.get(i).name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT , spritesetChecks[i])) {
							
							RefInt captureI = new RefInt(i);
							
							editor.schedule(e -> {
								
								int ii = captureI.get();
								
								selectedSet = anims.get(ii);
								//deselect other selectable texts
								for(int j = 0 ; j < anims.size() ; j ++) if(j != ii) put(spritesetChecks[j] , false);
								selectedSprite = selectedSet.getSprite(0);
								E.swapSpriteFast(selectedSprite);
								selectedSpriteIndex = 0;

								//is true if a sprite of an animation activates a hitbox
								if(selectedSprite.length % 3 != 0) {
									
									if(marker.editingName != null) e.removeAllHitboxes();
									
									marker.fromHitBoxSet(entityHitboxes.get((int)selectedSprite[selectedSprite.length - 1]), E);
									for(int j = 0 ; j < hitboxsetChecks.length ; j ++) if(j != ii) hitboxsetChecks[j].put(0 , (byte) 0);
									
								}
								
							});
														
						}
						
					}
					
					if(selectedSet != null) {
						
						nk_layout_row_dynamic(context , 200 , 1);
						if(nk_group_begin(context , "Select Sprite for Markup" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)){
							
							for(int i = 0 ; i < selectedSet.getNumberSprites() ; i++){
								
								if(i % 3 == 0) nk_layout_row_dynamic(context , 20 , 3);							
								if(nk_button_label(context , "Sprite " + i)) {
									
									RefInt captureI = new RefInt(i);
									
									 editor.schedule(e -> {
											
										int ii = captureI.get();
										 
										selectedSprite = selectedSet.getSprite(selectedSpriteIndex = ii);
										E.swapSpriteFast(selectedSprite);

										if(selectedSprite.length % 3 != 0) {
												
											if(marker.editingName != null) editor.removeAllHitboxes();
											
											marker.fromHitBoxSet(entityHitboxes.get((int)selectedSprite[selectedSprite.length - 1]), E);
											for(int j = 0 ; j < hitboxsetChecks.length ; j ++) if(j != ii) hitboxsetChecks[j].put(0 , (byte) 0);
												
										}
											
									});	
									
								}
								
							}
							
							nk_group_end(context);
							
						}
						
					}
					
					nk_group_end(context);
					
//					if(selectedSet != previousSet) {
//						
//						ArrayList<float[]> hitboxes = hitboxMarker.hitboxes();
//						for(float [] x : hitboxes) editor.renderer().removeFromRawData(x);
//						hitboxMarker.clear();
//					
//						previousSet = selectedSet;
//						selectedSprite = null;
//						selectedSpriteIndex = -1;
//						
//					}
					
				}
				
				if(selectedSet != null && selectedSprite != null) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Sprite " + selectedSpriteIndex + " from " + selectedSet.name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					
					boolean activatesHitBox = selectedSprite.length % 3 != 0;
					if(activatesHitBox) {
						
						nk_layout_row_dynamic(context , 20 , 1);
						//hitboxset activated is the hitboxset at the index found at the end of the sprite
						int index = (int)selectedSprite[selectedSprite.length - 1];
						if(index == -1) nk_text(context , "Disables HitBoxes" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
						else {
							
							try {
								
								nk_text(context , "Activates " + entityHitboxes.get(index).name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
								
							} catch(IndexOutOfBoundsException e) {
								
								nk_text(context , "Error Occurring, is there a hitbox at " + index + "?" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
								
							}
						
						}
						
					} 
										
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_checkbox_label(context , "Show HitBoxSets To Activate" , showHitBoxSetsCheck));

					if(toBool(showHitBoxSetsCheck)) {
						
						nk_layout_row_dynamic(context , 200 , 1);
						if(nk_group_begin(context , "HitBoxSets" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {

							for(int i = 0 ; i < entityHitboxes.numberSets() ; i ++) {
								
								if(i % 2 == 0) nk_layout_row_dynamic(context , 20 , 2);
								
								//whatever is selected from here will be displayed and rendered, but let user confirm they want 
								//whatever is selected.								
								if(nk_selectable_text(context , entityHitboxes.get(i).name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT , hitboxsetChecks[i])) {
									
									RefInt captureI = new RefInt(i);
									
									editor.schedule(e -> {

										int ii = captureI.get();
										
										if(!entityHitboxes.get(ii).name().equals(marker.editingName)) {
											
											if(marker.editingName != null) editor.removeAllHitboxes();

											marker.fromHitBoxSet(entityHitboxes.get(ii), E);
											for(int j = 0 ; j < hitboxsetChecks.length ; j ++) if(j != ii) hitboxsetChecks[j].put(0 , (byte) 0);

										}
										
									});				

								}

							}
							
							nk_group_end(context);
							
						}

					}
					
					if(marker.editingName != null) {
						
						nk_layout_row_dynamic(context , 30 , 1);
						//make the spriteset sprite activate the selected hitbox above
						if(nk_button_label(context , "Make Sprite Activate " + marker.editingName)) editor.schedule(e -> {
							
							float[] replacementSprite = new float [activatesHitBox ? selectedSprite.length : selectedSprite.length + 1];
							System.arraycopy(selectedSprite, 0, replacementSprite, 0, selectedSprite.length);
							replacementSprite[replacementSprite.length - 1] = entityHitboxes.indexOf(marker.toHitBoxSet(E));
							selectedSet.replaceSprite(selectedSpriteIndex, replacementSprite);
							selectedSet.write();
							selectedSprite = replacementSprite;

						});

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
