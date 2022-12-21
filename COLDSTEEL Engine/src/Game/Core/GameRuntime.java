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
import Networking.NetworkedEntities;
import Networking.NetworkedInstance;
import Networking.UserHostedServer.UserHostedServer;

/**
 * 
 * Runtime used during the game. This class is another god class mainly managing game state and the player.
 * 
 * 
 * @author Chris Brown
 *
 */
public class GameRuntime {
		
	public void setState(GameState targetState) {
		
		gameState = targetState;
		
	}

	public GameState getState() {
		
		return gameState;
		
	}
	
	private GameState gameState = GameState.MAIN_MENU;	
	private MainMenu mainMenu;
	private CharacterCreator creator;
	private PlayerLoader loadScreen;
	private PlayerCharacter player;
	private UserHostedServer hostedServer;
	private NetworkClient client;
	private boolean runEntitySystems = true;
	private Scene gameScene;
	private GameMenu menu;
	
	public GameRuntime() {}	
	
	public void initialize(Engine engine) {
		
		this.gameScene = new Scene(engine);
		this.menu = new GameMenu(engine , this);
		
	}
	
	public Scene scene() {
		
		return gameScene;
		
	}
	
	public void startUserHostedServer(Engine engine , String serverName) {
		
		hostedServer = new UserHostedServer(engine , serverName);
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
		
		switch(gameState) {
		
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
				if(runEntitySystems) {
					
					gameScene.entities().entitySystems((entity) -> {
						
						NetworkedEntities networked = NetworkedInstance.getNetworkedEntityForEntity(client , entity);
						return networked == null || networked.isReady;
												
					} ,
					null , 
					() -> {
						
						gameScene.kinematics().process();
						TemporalExecutor.process();
							
					} , 
					() -> engine.releaseKeys());
								
				}
				
				client.instanceUpdate();
				engine.g_levelUpdate();	
				client.forEachNetworkedEntity(networked -> networked.isReady = false);
								
			}
			
			case MAIN_MENU -> {
				
				if(mainMenu == null) mainMenu = new MainMenu(engine , this);
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
					
					engine.g_loadSave(loadScreen.load() , GameState.GAME_RUNTIME_MULTIPLAYER , false);
					((EntityScripts)player.playersEntity().components()[Entities.SOFF]).recompile();
					((NetworkClient) client).connectAndStart(player , engine.currentLevel());
					clientSideUpdateMultiplayerVariables();
					
				}
				
			}
			
			case NEW_SINGLEPLAYER -> {
				
				TemporalExecutor.process();
				mainMenu.hideAll();
				if(creator == null) { 
					
					creator = new CharacterCreator(this , true);
										
				}
				
				PlayerCharacter createdPlayer = creator.newPlayer();//once this returns something other than null, we have finished making a character
				if(createdPlayer != null) {
					
					setState(GameState.BUSY);
					
					engine.fadeToBlack(1000d);
					TemporalExecutor.onElapseOf(1000d , () -> {
						
						player = createdPlayer;
						
						engine.g_loadClearDeploy(data + "macrolevels/" + creator.startingLevel());
						gameScene.entities().addStraightIn(player.playersEntity());
						player.write(engine.currentLevel());
						float[] conditionAreaPos = engine.currentLevel().getLoadDoorByName(creator.startingDoor()).getConditionArea().getMidpoint();
						player.moveTo(conditionAreaPos[0] , conditionAreaPos[1]);
						setState(GameState.GAME_RUNTIME_SINGLEPLAYER);
						engine.fadeIn(250d);
						
					});
					
				}
				
			}
			
			case NEW_MULTIPLAYER -> {
				
				TemporalExecutor.process();
				if(creator == null) creator = new CharacterCreator(this , false);				
				PlayerCharacter createdPlayer = creator.newPlayer();//once this returns something other than null, we have finished making a character
				if(createdPlayer != null) {

					setState(GameState.BUSY);
					
					engine.fadeToBlack(1000d);
					TemporalExecutor.onElapseOf(1000d , () -> {

						player = createdPlayer;
						engine.g_loadClearDeploy(data + "macrolevels/" + creator.startingLevel());						
						gameScene.entities().addStraightIn(player.playersEntity());
						player.write(engine.currentLevel());
						float[] conditionAreaPos = engine.currentLevel().getLoadDoorByName(creator.startingDoor()).getConditionArea().getMidpoint();
						player.moveTo(conditionAreaPos[0] , conditionAreaPos[1]);
						engine.fadeIn(250d);
						gameState = GameState.MAIN_MENU;

						//multiplayer here
						try {
						
							client = new NetworkClient(this , mainMenu.getServerConnectionInfo());
							clientSideUpdateMultiplayerVariables();
							engine.fadeIn(1000);
							
						} catch (IOException e) {

							System.err.println("Error Connecting to Server.");
							e.printStackTrace();
							gameState = GameState.MAIN_MENU;
							engine.fadeIn(100);
							
						}
						
					});
					
				}
				
			}
			
			/*
			 * To join a server, submit a connection request and await a response. 
			 * 
			 */
			case JOIN_MULTIPLAYER -> {
				
				TemporalExecutor.process();

				try {
					
					client = new NetworkClient(this , mainMenu.getServerConnectionInfo());
					
				} catch (IOException e) {
					
					System.err.println("Error Connecting to Server.");
					e.printStackTrace();
					
				}
				
				setState(GameState.LOAD_MULTIPLAYER);
				mainMenu.hideAll();
				
			}
			
		}
			
	}
	
	private void clientSideUpdateMultiplayerVariables() {
		
		gameScene.entityScriptingInterface().setNetworkingVariables(client(), server());
		gameScene.entities().setNetworkInstance(server());
		
	}
	
	void leaveGame() {
		
		gameScene.clear();
		gameState = GameState.MAIN_MENU;		
		
	}
	
	public void toggleMultiplayerUI() {
		
		if(hostedServer != null) hostedServer.toggleUI();
		
		if(gameState != GameState.GAME_RUNTIME_MULTIPLAYER) return;
		client.toggleUI();
		
	}
		
	public void toggleGameMenu() {
		
		if(gameState == GameState.GAME_RUNTIME_MULTIPLAYER || gameState == GameState.GAME_RUNTIME_SINGLEPLAYER) menu.toggle();
		
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

	public void shutDown() {
		
		if(client != null) client.shutDown();
		if(hostedServer != null) hostedServer.shutDown();
		mainMenu.shutDown();
		gameScene.entityScriptingInterface().shutDown();
		
	}
			
}