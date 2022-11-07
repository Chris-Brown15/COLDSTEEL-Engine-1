package Game.Core;

import static CSUtil.BigMixin.toByte;
import static CSUtil.BigMixin.toggle;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_DOWN;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_text;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;

import java.text.DecimalFormat;

import AudioEngine.SoundEngine;
import CS.Engine;
import CS.RuntimeState;
import CS.UserInterface;
import Core.ECS;
import Core.Entities.Entities;
import Game.Player.PlayerCharacter;

/**
 * Intended to be a debug UI for viewing during game play
 * 
 * @author littl
 *
 */
public class DebugInfo extends UserInterface {

	private static final int uiOptions = NK_WINDOW_MOVABLE|NK_WINDOW_BORDER|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE ;
	
	private final DecimalFormat decimalFormatter = new DecimalFormat();
	private boolean showAllEntities = false;
	private boolean showAllSounds = false;
	
	{
		decimalFormatter.setMaximumFractionDigits(1);
	}
	
	private boolean seeAllLoadDoors = false;	
	private boolean freeze = false;
	

	public DebugInfo(Engine engine , GameRuntime runtime) {

		super("Game Debug", 1565 , 5, 350 , 600, uiOptions , uiOptions);
		
		layoutBody((frame) -> {
			
			if(Engine.STATE != RuntimeState.GAME || runtime.player() == null) return;
			
			PlayerCharacter player = runtime.player();
			Entities playerEntity = player.playersEntity();
			
			nk_layout_row_dynamic(context , 20 , 2);
			if(nk_checkbox_label(context , "Render Debug" , frame.bytes(toByte(runtime.renderDebug())))) runtime.renderDebug(true);
			if(nk_checkbox_label(context , "Freeze" , frame.bytes(toByte(freeze)))) {
				
				freeze = freeze ? false:true;
				playerEntity.freeze(freeze);
				
			}
			
			nk_layout_row_dynamic(context , 20 , 2);
			if(nk_checkbox_label(context , "Disable Player Gravity" , frame.bytes(toByte(playerEntity.has(ECS.GRAVITY_CONSTANT))))) {
				
				if(playerEntity.has(ECS.GRAVITY_CONSTANT)) {
					
					Engine.boundScene().entities().toggleComponent(playerEntity , ECS.GRAVITY_CONSTANT);
					
				}
				
			}
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Performance" , NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 20 , 3);
			nk_text(context , "FLS: " + Engine.framesLastSecond() , NK_TEXT_ALIGN_LEFT);
			nk_text(context , "IRLS: " + decimalFormatter.format(Engine.iterationRateLastSecond()) , NK_TEXT_ALIGN_LEFT);
			nk_text(context , "TLS: " + Engine.boundScene().entities().ticksLastSecond() , NK_TEXT_ALIGN_LEFT);
						
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Runtime Variables" , NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "State:" , NK_TEXT_ALIGN_LEFT);
			nk_text(context , GameRuntime.STATE.toString() , NK_TEXT_ALIGN_RIGHT);
			
			nk_text(context , "Current Level:" , NK_TEXT_ALIGN_LEFT);
			String level = engine.currentLevel() != null ? engine.currentLevel().gameName() : "null";
			nk_text(context , level , NK_TEXT_ALIGN_RIGHT);
			
			if(engine.currentLevel() != null) {
				
				nk_text(context , "Current MacroLevel" , NK_TEXT_ALIGN_LEFT);
				nk_text(context , engine.currentMacroLevel().name() , NK_TEXT_ALIGN_RIGHT);
				
				nk_text(context , "Previous Load Door:" , NK_TEXT_ALIGN_LEFT);
				String loadDoor = runtime.player().previouslyUsedLoadDoor() != null ? runtime.player().previouslyUsedLoadDoor().thisLoadDoorName() : "null";
				nk_text(context , loadDoor , NK_TEXT_ALIGN_RIGHT);
				
				if(runtime.player() != null) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Player" , NK_TEXT_ALIGN_CENTERED);
					
					float[] mid = playerEntity.getMidpoint();
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Position" , NK_TEXT_ALIGN_LEFT);
					nk_text(context , "X: " + mid[0] + " Y: " + mid[1] , NK_TEXT_ALIGN_RIGHT);
					
					nk_text(context , "LID:" , NK_TEXT_ALIGN_LEFT);
					nk_text(context , "" + playerEntity.LID() , NK_TEXT_ALIGN_RIGHT);
					
				}
				
				int symbol = seeAllLoadDoors ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
				nk_layout_row_dynamic(context , 20 , 1);				
				if(nk_selectable_symbol_text(context , symbol , "Load Doors" , NK_TEXT_ALIGN_CENTERED , toByte(frame , seeAllLoadDoors)))
					seeAllLoadDoors = seeAllLoadDoors ? false : true;
				
				if(seeAllLoadDoors) {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 3);
					engine.currentLevel().forEachLoadDoor(door -> {
						
						nk_layout_row_push(context , 30);
						nk_text(context , "" , 0);
						nk_layout_row_push(context , 180);
						nk_text(context , door.thisLoadDoorName() , NK_TEXT_ALIGN_LEFT);
						nk_layout_row_push(context , 95);
						float[] doorPos = door.conditionAreaSpecs();
						nk_text(context , "X: " + decimalFormatter.format(doorPos[2]) + " Y: " + decimalFormatter.format(doorPos[3]) , NK_TEXT_ALIGN_LEFT);
						
						nk_layout_row_end(context);
						
					});
					
				}
				
			}
			
			int symbol = showAllEntities ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_text(context , symbol , "Entities" , NK_TEXT_ALIGN_CENTERED , toByte(ALLOCATOR , showAllEntities))) 
				showAllEntities = toggle(showAllEntities);
			
			if(showAllEntities) {
				
				Engine.boundScene().entities().forEach(x -> {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , x.toString() , NK_TEXT_ALIGN_LEFT);
					
				});
				
			}
			
			symbol = showAllSounds ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_text(context , symbol , "Sound Files" , NK_TEXT_ALIGN_CENTERED , toByte(ALLOCATOR , showAllSounds))) 
				showAllSounds = showAllSounds ? false : true;
			
			if(showAllSounds) {
				
				SoundEngine.forEach(x -> {
				
					nk_layout_row_begin(context , NK_STATIC , 20 , 2);
					nk_layout_row_push(context , 260);
					nk_text(context , x.name() , NK_TEXT_ALIGN_LEFT);
					nk_layout_row_push(context , 55);
					nk_text(context , "At Index " + x.ID() , NK_TEXT_ALIGN_RIGHT);
					nk_layout_row_end(context);
					
				});
				
			}
		
			
		});
		
	}
	public void show() {
		
		show = true;
		
	}

	public void hide() {
		
		show = false;
		
	}
	
}

