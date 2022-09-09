package Game.Core;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;

import java.nio.FloatBuffer;

import static org.lwjgl.nuklear.Nuklear.nk_button_label;

import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import AudioEngine.SoundEngine;
import AudioEngine.Sounds;
import CS.Engine;
import CS.RuntimeState;
import Core.NKUI;
import Core.Quads;
import Core.TemporalExecutor;

/**
 * 
 * 
 * Class for the main menu of the game which will appear in the initial state of the program.
 *
 */
public class MainMenu implements NKUI{

	public boolean menuReturned = false;	
	NkRect rect;	
	Quads mainMenuBillboard = new Quads(-1);
	
	public MainMenu() {
		
		rect = NkRect.malloc(allocator).set(760 , 540 , 400, 310);
		mainMenuBillboard.setTexture(Renderer.Renderer.loadTexture(CS.COLDSTEEL.assets + "ui/minecraft.png"));
		mainMenuBillboard.translate(0, 150);
		
	}
	
	private void restartLoop(Sounds start) {
				
		start.play();
		TemporalExecutor.onTrue(() -> start.stopped() , () -> restartLoop(start));
				
	}	
	
	boolean showOptions = false;
	boolean showMultiplayer = false;
	
	public void layoutMainMenu(Engine engine) {
		
		TemporalExecutor.process();
		
		Renderer.Renderer.draw_foreground(mainMenuBillboard);
		
		if(nk_begin(context , "" , rect , NK_WINDOW_NO_SCROLLBAR)) {
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Continue")) {
				
				GameRuntime.STATE = GameState.GAME_RUNTIME;
				menuReturned = true;
				
			}	
			
			if(nk_button_label(context , "New Character")) {
				
				GameRuntime.STATE = GameState.NEW_CHARACTER;
				menuReturned = true;
				
			}			
			
			if(nk_button_label(context , "Load Character")) {

				GameRuntime.STATE = GameState.LOAD_SAVE;
				menuReturned = true;
				
			}			
			
			if(nk_button_label(context , "Multiplayer")) {
								
//				System.out.println(SoundEngine.getGlobalVolume());
//				
//				Sounds intro = SoundEngine.add(assets + "sounds/" + "OST_SOTN Draculas Castle Intro.ogg");
//				Sounds seg1 = SoundEngine.add(assets + "sounds/" + "OST_SOTN Draculas Castle Segment1.ogg");
//				Sounds loop = SoundEngine.add(assets + "sounds/" + "OST_SOTN Draculas Castle Loop.ogg");
//				
//				intro.play();
//				TemporalExecutor.onTrue(() -> intro.stopped() , () -> {
//					
//					seg1.play();
//					TemporalExecutor.onTrue(() -> seg1.stopped() , () -> restartLoop(loop));
//					
//				});
				
				showMultiplayer = showMultiplayer ? false:true;
				
			}
			
			if(nk_button_label(context , "Options")) {
				
				showOptions = showOptions ? false:true;
				
			}
			
			if(nk_button_label(context , "Open Editor")) {
				
				engine.switchState(RuntimeState.EDITOR);
				GameRuntime.STATE = GameState.BUSY;
				menuReturned = true;
				
			}	
			
			if(nk_button_label(context , "Close")) {
				
				engine.closeOverride();
				menuReturned = true;
				
			}
			
		}
		
		nk_end(context);
		
		if(showOptions) {
			
			try(MemoryStack stack = allocator.push()){
				
				NkRect optionsRect = NkRect.malloc(stack).set(1165 , 540 , 300 , 400);
				if(nk_begin(context , "Options" , optionsRect , NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_TITLE)) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Sound Options" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
					nk_layout_row_dynamic(context , 30 , 1);
					FloatBuffer slider = stack.floats(SoundEngine.getGlobalVolume());
					nk_property_float(context , "Sound Volume" , -999 , slider , 999 , 0.05f , 0.05f);
					SoundEngine.setGlobalVolume(slider.get());
					
				}
				
				nk_end(context);
				
			}
			
		}
		
		if(showMultiplayer) {
			
			try(MemoryStack stack = allocator.push()) {
				
				NkRect multiplayerRect = NkRect.malloc(allocator).set(showOptions ? 1470 : 1165 , 540 , 300 , 310);
				if(nk_begin(context , "Multiplayer" , multiplayerRect ,  NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_TITLE)) {
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_button_label(context , "Host Multiplayer Session")) {
						
						
						
					}
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_button_label(context , "Start Server")) {
						
						
						
					}
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_button_label(context , "Join Session")) {
						
						
						
					}
					
				}
				
				nk_end(context);
				
			}
			
		}
		
	}
	
}
