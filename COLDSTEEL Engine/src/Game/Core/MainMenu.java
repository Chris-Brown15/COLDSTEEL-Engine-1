package Game.Core;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
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
import Game.Player.CharacterCreator;

/**
 * 
 * 
 * Class for the main menu of the game which will appear in the initial state of the program.
 *
 */
public class MainMenu implements NKUI{

	/**
	 * Used to switch on UI elements to render where each possible state is a different UI.
	 */
	private static enum MenuStates {
		
		MAIN,
		LOAD,
		MULTIPLAYER,
		MULTIPLAYER_HOSTING,
		MULTIPLAYER_JOINING,
		NEW,
		OPTIONS,
		;
				
	}
	
	private MenuStates menuState = MenuStates.MAIN;
	
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
	//ui class for creating characters
	private CharacterCreator characterCreator = null;
	
	public void layoutMainMenus(Engine engine) {
		
		TemporalExecutor.process();		
		switch(menuState) {
			
			case LOAD -> {}
			case MAIN -> layoutMainMenu(engine);
			case MULTIPLAYER -> layoutMainMultiplayer();
			case MULTIPLAYER_HOSTING -> layoutMultiplayerHosting();
			case MULTIPLAYER_JOINING -> {}
			case NEW -> characterCreator.layout();
			case OPTIONS -> layoutOptions();	
			
		}
				
	}
	
	private void layoutMainMenu(Engine engine) {
		
		Renderer.Renderer.draw_foreground(mainMenuBillboard);
		
		if(nk_begin(context , "" , rect , NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_BORDER)) {
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Continue")) {
				
				GameRuntime.STATE = GameState.GAME_RUNTIME;
				menuReturned = true;
				
			}	
			
			if(nk_button_label(context , "New Character")) {
				
				characterCreator = new CharacterCreator();
				menuState = MenuStates.NEW;
				
//				GameRuntime.STATE = GameState.NEW_CHARACTER;
//				menuReturned = true;
				
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
				
				menuState = MenuStates.MULTIPLAYER;
				
			}
			
			if(nk_button_label(context , "Options")) {
				
				menuState = MenuStates.OPTIONS;
				
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
		
	}
	
	private void layoutOptions() {

		try(MemoryStack stack = allocator.push()){
			
			NkRect optionsRect = NkRect.malloc(stack).set(810 , 540 , 300 , 400);
			if(nk_begin(context , "Options" , optionsRect , NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_TITLE)) {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Sound Options" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				nk_layout_row_dynamic(context , 30 , 1);
				FloatBuffer slider = stack.floats(SoundEngine.getGlobalVolume());
				nk_property_float(context , "Sound Volume" , -999 , slider , 999 , 0.05f , 0.05f);
				SoundEngine.setGlobalVolume(slider.get());
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Back")) { 
					
					menuState = MenuStates.MAIN;
					
				}
				
			}
			
			nk_end(context);
			
		}
	
	}
	
	private void layoutMainMultiplayer() { 
		
		try(MemoryStack stack = allocator.push()) {
			
			NkRect multiplayerRect = NkRect.malloc(allocator).set(810 , 540 , 300 , 310);
			if(nk_begin(context , "Multiplayer" , multiplayerRect , NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_TITLE)) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Host Session")) {
					
					menuState = MenuStates.MULTIPLAYER_HOSTING;
					
				}

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Join Session")) {
					
					
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Start Server")) {
					
					
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Back")) menuState = MenuStates.MAIN;
				
				
			}
			
			nk_end(context);
			
		}
			
	}
	
	private void layoutMultiplayerHosting() {
		
		try (MemoryStack stack = allocator.push()) { 
			
			NkRect multiplayerRect = NkRect.malloc(allocator).set(810 , 540 , 300 , 310);
			if(nk_begin(context , "Host a Session" , multiplayerRect , NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_TITLE)) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "New Character")) { 
					
					menuState = MenuStates.NEW;							
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);				
				if(nk_button_label(context , "Load Character")) {
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Back")) menuState = MenuStates.MULTIPLAYER;			
				
			}
			
			nk_end(context);
			
		}
		
	}
	
}
