package Game.Core;

import static CSUtil.BigMixin.put;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_SELECTABLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_edit_string;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.system.MemoryUtil.memCalloc;
import static org.lwjgl.system.MemoryUtil.memCallocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8Safe;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.nuklear.NkRect;

import Audio.SoundEngine;
import Audio.Sounds;
import CS.Engine;
import CS.RuntimeState;
import CS.UserInterface;
import Core.TemporalExecutor;

/**
 * 
 * 
 * Class for the main menu of the game which will appear in the initial state of the program.
 *
 */
public class MainMenu  {

	private enum MenuStates {
		
		LOAD,
		BUSY,
		MAIN,
		MULTIPLAYER_MAIN,
		MULTIPLAYER_JOINING,
		OPTIONS;
		
	}

	private static final int uiOptions = NK_WINDOW_MOVABLE|NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR;
	
	MenuStates menuState = MenuStates.MAIN;
	
	public boolean menuReturned = false;	
	NkRect rect;	
	ByteBuffer portAndInetAddrInput = memCalloc(1 , 23);
	IntBuffer portAndInetAddrLength = memCallocInt(1);
	private final Main main;
	private final Multiplayer multi ;
	private final MultiplayerJoiner multiplayerJoin;
	private final Options options;
	
	public MainMenu(Engine engine) {
		
		Sounds intro = SoundEngine.add(CS.COLDSTEEL.assets + "sounds/" + "OST_SOTN Draculas Castle Intro.ogg");
		Sounds seg1 = SoundEngine.add(CS.COLDSTEEL.assets + "sounds/" + "OST_SOTN Draculas Castle Segment1.ogg");
		Sounds loop = SoundEngine.add(CS.COLDSTEEL.assets + "sounds/" + "OST_SOTN Draculas Castle Loop.ogg");
		
		intro.play();
		TemporalExecutor.onTrue(() -> intro.stopped() , () -> {
		
			seg1.play();
			TemporalExecutor.onTrue(() -> seg1.stopped() , () -> restartLoop(loop));
				
		});

		main = new Main(engine);
		multi = new Multiplayer(engine);
		multiplayerJoin = new MultiplayerJoiner(engine); 
		options = new Options(engine);
		
	}
	
	private void restartLoop(Sounds start) {
				
		start.play();
		TemporalExecutor.onTrue(() -> start.stopped() , () -> restartLoop(start));

	}
	
	void layoutMainMenus() {
		
		TemporalExecutor.process();		
		switch(menuState) {
			
			case LOAD -> {
			
				GameRuntime.setState(GameState.LOAD_SAVE);
				
			}
			
			case BUSY -> {

				main.hide();
				multi.hide();
				multiplayerJoin.hide();
				options.hide();
				
			}
			
			case MAIN -> {
				
				main.show();
				multi.hide();
				multiplayerJoin.hide();
				options.hide();
				
			}
			
			case MULTIPLAYER_MAIN -> {
				
				multi.show();
				multiplayerJoin.hide();
				options.hide();
				main.hide();
				
			}
			
			case MULTIPLAYER_JOINING -> {
				
				multiplayerJoin.show();
				multi.hide();
				options.hide();
				main.hide();
				
			}
			
			case OPTIONS -> {
				
				options.show();
				multi.hide();
				multiplayerJoin.hide();
				main.hide();
				
			}
						
		}
		
	}
	
	void hideAll() {
		
		options.hide();
		multi.hide();
		multiplayerJoin.hide();
		main.hide();		
		
	}
	
	String getServerConnectionInfo() {
		
		if(portAndInetAddrLength.get(0) == 0) return "25.68.228.205:32900";
		else return memUTF8Safe(portAndInetAddrInput.slice(0, portAndInetAddrLength.get(0)));
		
	}
	
	void shutDown() {
		
		memFree(portAndInetAddrInput);
		memFree(portAndInetAddrLength);
		
	}
	
	class Main extends UserInterface {

		public Main(Engine engine) {
			
			super("GAMEMAINMENU" , 760 , 540 , 400, 310, uiOptions|NK_WINDOW_MOVABLE , uiOptions|NK_WINDOW_MOVABLE);
			
			layoutBody((frame) -> {
				
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Continue")) {
				
				GameRuntime.STATE = GameState.GAME_RUNTIME_SINGLEPLAYER;
				menuReturned = true;
				
			}	
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "New Character")) {
				
				GameRuntime.STATE = GameState.NEW_SINGLEPLAYER;
				menuReturned = true;
				
			}			
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Load Character")) {

				GameRuntime.STATE = GameState.LOAD_SAVE;
				menuReturned = true;
				
			}			
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Multiplayer")) {
					
				menuState = MenuStates.MULTIPLAYER_MAIN;
				
			}
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Options")) {
				
				menuState = MenuStates.OPTIONS;
				
			}
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Open Editor")) {
				
				engine.schedule(() -> {
					
					engine.switchState(RuntimeState.EDITOR);
					GameRuntime.STATE = GameState.BUSY;
					menuReturned = true;
					hide();
					
				});
				
			}	
			
			nk_layout_row_dynamic(context , 40 , 1);
			if(nk_button_label(context , "Close")) {
				
				engine.closeOverride();
				menuReturned = true;
				
			}});
			
		}
		
		void show() {
			
			show = true;
			
		}

		void hide() {
			
			show = false;
			
		}
		
	}
	
	class Multiplayer extends UserInterface {

		public Multiplayer(Engine engine) {
			
			super("" , 810 , 540 , 300 , 310, uiOptions|NK_WINDOW_TITLE , uiOptions|NK_WINDOW_TITLE);
			 
			layoutBody((frame) -> {

				if(engine.mg_isHostedServerRunning()) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Server is Running. Join it to Play" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "IP Address: " + engine.mg_hostedServerIPAddress() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Port: " + engine.mg_hostedServerPort() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
					
				} else {
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_button_label(context , "Host Session")) engine.mg_startHostedServer();
					
				}

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Join Server With New Character")) {
					
					GameRuntime.setState(GameState.NEW_MULTIPLAYER);
					menuState = MenuStates.MULTIPLAYER_MAIN;
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Join Server With Existing Character")) {
					
					menuState = MenuStates.MULTIPLAYER_JOINING;
					GameRuntime.setState(GameState.LOAD_MULTIPLAYER);
				
				}
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Back")) menuState = MenuStates.MAIN;
				
			});
			
		}
		
		void show() {
			
			show = true;
			
		}

		void hide() {
			
			show = false;
			
		}
		
	}
	
	class MultiplayerJoiner extends UserInterface {

		public MultiplayerJoiner(Engine engine) {
			super("" , 810 , 540 , 300 , 310, uiOptions , uiOptions);

			layoutBody((frame) -> {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context  , "IP Address:Port" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , portAndInetAddrInput , portAndInetAddrLength , 22 , UserInterface.DEFAULT_FILTER);
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Join")) { 
					
					GameRuntime.setState(GameState.JOIN_MULTIPLAYER);
					hideAll();
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Back")) menuState = MenuStates.MULTIPLAYER_MAIN;
				
			});
				
		}
		
		void show() {
			
			show = true;
			
		}

		void hide() {
			
			show = false;
			
		}
		
	}
	
	class Options extends UserInterface {

		private ByteBuffer borderlessFullscreen = ALLOCATOR.bytes((byte) 1);
		private ByteBuffer borderedNonfullscreen = ALLOCATOR.bytes((byte) 0);
		
		public Options(Engine engine) {
			
			super("" , 810 , 540 , 300 , 400, uiOptions , uiOptions);

			layoutBody((frame) -> {

				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Sound Options" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				nk_layout_row_dynamic(context , 30 , 1);
				FloatBuffer slider = frame.floats(SoundEngine.getGlobalVolume());
				nk_property_float(context , "Sound Volume" , -999 , slider , 999 , 0.05f , 0.05f);
				SoundEngine.setGlobalVolume(slider.get());
				
				nk_layout_row_dynamic(context, 20 , 1);
				nk_text(context , "Window Options" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_checkbox_label(context , "Borderless Fullscreen" , borderlessFullscreen)) { 
				
					if(borderlessFullscreen.get(0) == 1) { 
						
						engine.schedule(() -> engine.fullscreenifyWindow());
						put(borderedNonfullscreen , (byte) 0);
						
					}
					
				}					

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_checkbox_label(context , "Bordered Non-Fullscreen" , borderedNonfullscreen)) { 
				
					if(borderedNonfullscreen.get(0) == 1) { 
						
						engine.schedule(() -> engine.windowifyWindow());
						put(borderlessFullscreen , (byte) 0);
						
						
					}
					
				}					
								
							
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Back")) menuState = MenuStates.MAIN;
				
			});
			
		}
		
		void show() {
			
			show = true;
			
		}
		
		void hide() {
			
			show = false;
			
		}
		
	}
	
}
