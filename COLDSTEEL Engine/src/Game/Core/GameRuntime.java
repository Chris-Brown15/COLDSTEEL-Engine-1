package Game.Core;

import static CS.COLDSTEEL.data;

import java.io.IOException;
import CS.Engine;
import Core.Scene;
import Core.TemporalExecutor;
import Core.Entities.Entities;
import Core.Entities.EntityScripts;
import Game.Player.CharacterCreator;
import Game.Player.PlayerCharacter;
import Game.Player.PlayerLoader;
import Networking.NetworkClient;
import Networking.UserHostedServer.UserHostedServer;
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
	
	private MainMenu mainMenu;
	private CharacterCreator creator;
	private PlayerLoader loadScreen;
	private PlayerCharacter player;
	private UserHostedServer hostedServer;
	private NetworkClient client;
	private boolean runEntitySystems = true;
	private Scene gameScene;
	
	public GameRuntime() {}	
	
	public void initialize(Engine engine , Renderer renderer) {
		
		this.gameScene = new Scene(renderer , engine);
		
	}
	
	public Scene gameScene() {
		
		return gameScene;
		
	}
	
	public void startUserHostedServer(Engine engine , Renderer renderer) {
		
		hostedServer = new UserHostedServer(engine , renderer);
		runEntitySystems = false;
		
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
		
//		if(showPyUI) for(int i = 0 ; i < UIScriptingInterface.getPyUIs().size() ; i ++) UIScriptingInterface.getPyUIs().get(i).run();		
		
		switch(STATE) {
		
			case GAME_RUNTIME_SINGLEPLAYER -> {
				
				if(runEntitySystems) gameScene.entities().entitySystems(() -> {				
			        
				} , () -> {
				
					gameScene.kinematics().process();
					TemporalExecutor.process();
				
				} , () -> {
				
					engine.releaseKeys();
				
				});
							
				engine.g_levelUpdate();
				
			}
			
			case GAME_RUNTIME_MULTIPLAYER -> {
				
				//only called on clients, where there will aways be exactly one scene which will always be bound
				if(runEntitySystems) gameScene.entities().entitySystems(() -> {				
			        
				} , () -> {
				
					gameScene.kinematics().process();
					TemporalExecutor.process();
				
				} , () -> {
				
					engine.releaseKeys();
					client.instanceUpdate();				
				
				});
							
				engine.g_levelUpdate();	
				
			}
			
			case MAIN_MENU -> {
				
				if(mainMenu == null) mainMenu = new MainMenu(engine);
				mainMenu.layoutMainMenus();
				
			}
			
			case BUSY -> {
				
				TemporalExecutor.process();
				
			}
			
			case LOAD_SAVE -> {

				TemporalExecutor.process();
				if(loadScreen == null) loadScreen = new PlayerLoader();				
				loadScreen.show();
				if(loadScreen.load() != null) { 
					
					engine.g_loadSave(loadScreen.load() , GameState.GAME_RUNTIME_SINGLEPLAYER , true);
					mainMenu.hideAll();
					
				}
				
			}
			
			case LOAD_MULTIPLAYER -> {
				
				TemporalExecutor.process();
				if(loadScreen == null) { 
					
					loadScreen = new PlayerLoader();
					loadScreen.show();
					
				}
				
				if(loadScreen.load() != null) { 
					
					STATE = GameState.MAIN_MENU;
					loadScreen.hide();
					
				}
				
			}
			
			case NEW_SINGLEPLAYER -> {
				
				TemporalExecutor.process();
				mainMenu.hideAll();
				if(creator == null) { 
					
					creator = new CharacterCreator(gameScene , true);
										
				}
				
				PlayerCharacter createdPlayer = creator.newPlayer();//once this returns something other than null, we have finished making a character
				if(createdPlayer != null) {
					
					creator.hideElements();
					setState(GameState.BUSY);
					
					engine.fadeToBlack(1000d);
					TemporalExecutor.onElapseOf(1000d , () -> {
						
						player = createdPlayer;
						
						engine.g_loadClearDeploy(data + "macrolevels/" + creator.startingLevel());
						gameScene.entities().addStraightIn(player.playersEntity());
						player.write(engine.currentLevel());
						player.moveTo(engine.currentLevel().getLoadDoorByName(creator.startingDoor()).getConditionArea().getMidpoint());
						setState(GameState.GAME_RUNTIME_SINGLEPLAYER);
						creator.hideElements();
						engine.fadeIn(250d);
						
					});
					
				}
				
			}
			
			case NEW_MULTIPLAYER -> {
				
				TemporalExecutor.process();
				if(creator == null) creator = new CharacterCreator(gameScene , false);				
				PlayerCharacter createdPlayer = creator.newPlayer();//once this returns something other than null, we have finished making a character
				if(createdPlayer != null) {

					creator.hideElements();
					setState(GameState.BUSY);
					
					engine.fadeToBlack(1000d);
					TemporalExecutor.onElapseOf(1000d , () -> {

						player = createdPlayer;
						engine.g_loadClearDeploy(data + "macrolevels/" + creator.startingLevel());						
						gameScene.entities().addStraightIn(player.playersEntity());
						player.write(engine.currentLevel());
						player.moveTo(engine.currentLevel().getLoadDoorByName(creator.startingDoor()).getConditionArea().getMidpoint());
						engine.fadeIn(250d);
						STATE = GameState.MAIN_MENU;

						//multiplayer here
						try {
						
							client = new NetworkClient(gameScene , engine.currentLevel());
							clientSideUpdateMultiplayerVariables();
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
				
					client = new NetworkClient(gameScene , engine.currentLevel());
					clientSideUpdateMultiplayerVariables();
					((EntityScripts)player.playersEntity().components()[Entities.SOFF]).recompile();
					
				} catch (IOException e) {

					System.err.println("Error Connecting to Server.");
					e.printStackTrace();
					
				}

				((NetworkClient) client).connectAndStart(player , mainMenu.getServerConnectionInfo());
				
			}
			
		}
			
	}
	
	private void clientSideUpdateMultiplayerVariables() {
		
		gameScene.entityScriptingInterface().setNetworkingVariables(client(), server());
		gameScene.entities().setNetworkInstance(server());
		
	}
	
	public void toggleMultiplayerUI() {
		
		if(hostedServer != null) hostedServer.toggleUI();
		
		if(STATE != GameState.GAME_RUNTIME_MULTIPLAYER) return;
		client.toggleUI();
		
	}
	
	public void shutDown() {
		
		if(client != null) client.shutDown();
		if(hostedServer != null) hostedServer.shutDown();
		mainMenu.shutDown();
		gameScene.entityScriptingInterface().shutDown();
		
	}
	
	public PlayerCharacter player() {
		
		return player;
		
	}
	
	public void player(PlayerCharacter player) {
		
		this.player = player;
		
	}
	
	public UserHostedServer server() {
		
		return hostedServer;
		
	}
	
	public NetworkClient client() {
		
		return client;
		
	}
	
}