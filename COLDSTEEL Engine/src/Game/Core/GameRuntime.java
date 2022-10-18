package Game.Core;

import static CS.COLDSTEEL.data;

import java.io.IOException;
import CS.Engine;
import Core.Direction;
import Core.ECS;
import Core.Scene;
import Core.TemporalExecutor;
import Core.UIScriptingInterface;
import Core.Entities.Entities;
import Core.Entities.EntityHitBoxes;
import Core.Entities.EntityScripts;
import Game.Items.Inventories;
import Game.Items.ItemComponents;
import Game.Player.CharacterCreator;
import Game.Player.PlayerCharacter;
import Game.Player.PlayerLoader;
import Networking.NetworkClient;
import Networking.UserHostedServer.UserHostedServer;
import Physics.ColliderLists;
import Physics.Kinematics;
import Renderer.Renderer;

/**
 * 
 * Runtime used during the game. This class is another god class mainly managing game state and the player.
 * 
 * 
 * @author Chris Brown
 *
 */
public class GameRuntime {

	static GameState STATE = GameState.MAIN_MENU;
		
	public static void setState(GameState targetState) {
		
		STATE = targetState;
		
	}

	public static final GameState getState() {
		
		return STATE;
		
	}
	
	private Scene scene;
	private MainMenu mainMenu;
	private CharacterCreator creator;
	private PlayerLoader loadScreen;
	private PlayerCharacter player;
	private DebugInfo debugInfo = new DebugInfo();
	private UserHostedServer hostedServer;
	private NetworkClient client;
	private boolean showPyUI = true;
	private boolean renderDebug = false;
	
	public GameRuntime(Scene scene) {
	
		this.scene = scene;
		
	}	
	
	public void initialize() {}
	
	public void startUserHostedServer(Engine engine) {
		
		hostedServer = new UserHostedServer(engine);
		engine.mg_propogateNewClientAndServer();
		//kinda dubious, we allow the UI for the server to process forever until the program closes. 
		TemporalExecutor.whileTrue(() -> true , hostedServer::instanceUI);
		
		
	}
	
	public boolean isHostedServerRunning() {
		
		return hostedServer != null && hostedServer.running();
		
	}
	
	public String hostedServerIPAddress() {
		
		return hostedServer.IP();
		
	}

	public int hostedServerPort() {
		
		return hostedServer.port();
		
	}
	
	public void run(Engine engine) {
		
		if(renderDebug) renderDebug(engine);
		if(showPyUI) for(int i = 0 ; i < UIScriptingInterface.getPyUIs().size() ; i ++) UIScriptingInterface.getPyUIs().get(i).run();		
		
		switch(STATE) {
		
			case GAME_RUNTIME_SINGLEPLAYER -> {
				
				scene.entities().editorRunSystems(() -> {				
			        
				} , () -> {
				
					Kinematics.process();
					TemporalExecutor.process();
				
				} , () -> {
				
					engine.releaseKeys();
				
				});
							
				engine.g_levelUpdate();
				debugInfo.layout(engine , this);
				
			}
			
			case GAME_RUNTIME_MULTIPLAYER -> {
				
				scene.entities().editorRunSystems(() -> {				
			        
				} , () -> {
				
					Kinematics.process();
					TemporalExecutor.process();
				
				} , () -> {
				
					client.instanceUpdate();				
					engine.releaseKeys();
				
				});
							
				engine.g_levelUpdate();
				client.instanceUI();
				debugInfo.layout(engine , this);
				
				
			}
			
			case MAIN_MENU -> {
				
				if(mainMenu == null) mainMenu = new MainMenu();
				mainMenu.layoutMainMenus(engine);
				
			}
			
			case BUSY -> {
				
				TemporalExecutor.process();
				
			}
			
			case LOAD_SAVE -> {

				TemporalExecutor.process();
				if(loadScreen == null) loadScreen = new PlayerLoader();
				loadScreen.layout();				
				if(loadScreen.load() != null) engine.g_loadSave(loadScreen.load());
				
			}
			
			case LOAD_MULTIPLAYER -> {
				
				TemporalExecutor.process();
				if(loadScreen == null) loadScreen = new PlayerLoader();
				loadScreen.layout();
				if(loadScreen.load() != null) STATE = GameState.MAIN_MENU;
				
			}
			
			case NEW_SINGLEPLAYER -> {
				
				TemporalExecutor.process();
				if(creator == null) creator = new CharacterCreator(true);				
				creator.layout();
				PlayerCharacter createdPlayer = creator.newPlayer();//once this returns something other than null, we have finished making a character
				if(createdPlayer != null) {
					
					setState(GameState.BUSY);
					
					engine.fadeToBlack(1000d);
					TemporalExecutor.onElapseOf(1000d , () -> {
						
						player = createdPlayer;
						scene.entities().addStraightIn(player.playersEntity());
						engine.g_loadClearDeploy(data + "macrolevels/" + creator.startingLevel());
						player.write(engine.currentLevel());
						scene.entities().addStraightIn(player.playersEntity());
						player.moveTo(engine.currentLevel().getLoadDoorByName(creator.startingDoor()).getConditionArea().getMidpoint());
						setState(GameState.GAME_RUNTIME_SINGLEPLAYER);
						engine.fadeIn(250d);
						
					});
					
				}
				
			}
			
			case NEW_MULTIPLAYER -> {
				
				TemporalExecutor.process();
				if(creator == null) creator = new CharacterCreator(false);				
				creator.layout();
				PlayerCharacter createdPlayer = creator.newPlayer();//once this returns something other than null, we have finished making a character
				if(createdPlayer != null) {
					
					setState(GameState.BUSY);
					
					engine.fadeToBlack(1000d);
					TemporalExecutor.onElapseOf(1000d , () -> {

						player = createdPlayer;
						engine.g_loadClearDeploy(data + "macrolevels/" + creator.startingLevel());						
						scene.entities().addStraightIn(player.playersEntity());
						player.write(engine.currentLevel());
						player.moveTo(engine.currentLevel().getLoadDoorByName(creator.startingDoor()).getConditionArea().getMidpoint());
						engine.fadeIn(250d);
						STATE = GameState.MAIN_MENU;

						//multiplayer here
						try {
						
							client = new NetworkClient(scene , engine.currentLevel());							
							engine.mg_propogateNewClientAndServer();
							engine.fadeIn(1000);
							
						} catch (IOException e) {

							System.err.println("Error Connecting to Server.");
							e.printStackTrace();
							STATE = GameState.MAIN_MENU;
							engine.fadeIn(100);
							
						}
						
					});
					
				}
				
			}
			
			case JOIN_MULTIPLAYER -> {
				
				TemporalExecutor.process();

				engine.g_loadSave(loadScreen.load() , GameState.GAME_RUNTIME_MULTIPLAYER , false);

				//multiplayer here
				try {
				
					client = new NetworkClient(scene , engine.currentLevel());
					engine.mg_propogateNewClientAndServer();
					((EntityScripts)player.playersEntity().components()[Entities.SOFF]).recompile();
					
				} catch (IOException e) {

					System.err.println("Error Connecting to Server.");
					e.printStackTrace();
					
				}

				((NetworkClient) client).connectAndStart(player , mainMenu.getServerConnectionInfo());
				
			}
			
		}
			
	}
	
	public void renderDebug(boolean render) {
		
		renderDebug = render;
		
	}

	public boolean renderDebug() {
		
		return renderDebug;
		
	}
	
	/**
	 * Renders colliders, load doors, and entity and item collision bounds and hitboxes 
	 * 
	 */
	private void renderDebug(Engine engine) {

		ColliderLists.getComposite().forEachVal(Renderer::draw_foreground);
		scene.entities().forEach(entity -> {
			
			Object[] comps = entity.components();
			if(entity.has(ECS.COLLISION_DETECTION) && comps[Entities.CDOFF] != null) Renderer.draw_foreground((float[]) comps[Entities.CDOFF]);
			if(entity.has(ECS.HITBOXES)) {
				
				EntityHitBoxes entityHitBoxes = (EntityHitBoxes) comps[Entities.HOFF];
				float[][] boxes = entityHitBoxes.getActiveHitBoxes(entity, (Direction)comps[Entities.DOFF]);
				if(boxes != null) for(float[] x : boxes) Renderer.draw_foreground(x);
				
			}
			
			if(entity.has(ECS.INVENTORY)) {
				
				((Inventories)comps[Entities.IOFF]).getEquipped().forEach(item -> {
					
					if(item.has(ItemComponents.HITBOXABLE)) {
						
						float[][] boxes = item.componentData().getActiveHitBoxes();
						for(float[] x : boxes) Renderer.draw_foreground(x);
						
					}
					
				});
				
			}
			
			engine.currentLevel().forEachLoadDoor(loadDoor -> Renderer.draw_foreground(loadDoor.getConditionArea()));
			engine.currentLevel().forEachTrigger(trigger -> {
				
				trigger.forEachConditionArea(Renderer::draw_foreground);
				trigger.forEachEffectArea(Renderer::draw_foreground);
				
			});
			
		});
		
	}
	
	public void toggleMultiplayerUI() {
		
		if(hostedServer != null) hostedServer.toggleUI();
		
		if(STATE != GameState.GAME_RUNTIME_MULTIPLAYER) return;
		client.toggleUI();
		
	}
	
	public void shutDown() {
		
		if(client != null) client.shutDown();
		if(hostedServer != null) hostedServer.shutDown();
		
	}
	
	public PlayerCharacter player() {
		
		return player;
		
	}
	
	public void player(PlayerCharacter player) {
		
		this.player = player;
		
	}
	
	public Scene scene() {
		
		return scene;
		
	}
	
	public UserHostedServer server() {
		
		return hostedServer;
		
	}
	
	public NetworkClient client() {
		
		return client;
		
	}
	
}