package Game.Core;

import static CS.COLDSTEEL.data;

import CS.Engine;
import Core.Direction;
import Core.ECS;
import Core.Scene;
import Core.TemporalExecutor;
import Core.UIScriptingInterface;
import Core.Entities.Entities;
import Core.Entities.EntityHitBoxes;
import Game.Items.Inventories;
import Game.Items.ItemComponents;
import Game.Player.CharacterCreator;
import Game.Player.PlayerCharacter;
import Game.Player.PlayerLoader;
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
	
	private Scene scene;
	private MainMenu mainMenu;
	private CharacterCreator creator;
	private PlayerLoader loadScreen;
	private PlayerCharacter player;
	private DebugInfo debugInfo = new DebugInfo();
	private boolean showPyUI = true;
	private boolean renderDebug = false;
	
	public GameRuntime(Scene scene) {
	
		this.scene = scene;
		
	}	
	
	public void initialize() {}
	
	public void run(Engine engine){
		
		debugInfo.layout(engine , this);
		if(renderDebug) renderDebug(engine);
		if(showPyUI) for(int i = 0 ; i < UIScriptingInterface.getPyUIs().size() ; i ++)UIScriptingInterface.getPyUIs().get(i).run();		
		
		switch(STATE) {
		
			case GAME_RUNTIME -> {
				
				scene.entities().editorRunSystems(() -> {				
			        
				} , () -> {
				
					Kinematics.process();
					TemporalExecutor.process();
				
				} , () -> {
				
					engine.releaseKeys();
				
				});
							
				engine.levelUpdate();
				
			}
			
			case MAIN_MENU -> {
				
				if(mainMenu == null) mainMenu = new MainMenu();
				mainMenu.layoutMainMenu(engine);
				
			}
			
			case BUSY -> {
				
				TemporalExecutor.process();
				
			}
			
			case LOAD_SAVE -> {

				TemporalExecutor.process();
				if(loadScreen == null) loadScreen = new PlayerLoader();
				loadScreen.layout(engine);				
				String savePath = loadScreen.load();
				boolean saveSelected = savePath != null;				
				if(saveSelected) engine.loadSave(savePath);
				
			}
			
			case NEW_CHARACTER -> {
				
				TemporalExecutor.process();
				if(creator == null) creator = new CharacterCreator();				
				creator.layout();
				PlayerCharacter createdPlayer = creator.newPlayer();//once this returns something other than null, we have finished making a character
				if(createdPlayer != null) {
					
					setState(GameState.BUSY);
					
					engine.fadeToBlack(1000d);
					TemporalExecutor.onElapseOf(1000d , () -> {

						player = createdPlayer;
						scene.entities().addStraightIn(player.playersEntity());
						engine.loadClearDeploy(data + "macrolevels/" + creator.startingLevel());
						player.write(engine.currentLevel());
						scene.entities().addStraightIn(player.playersEntity());
						player.moveTo(engine.currentLevel().getLoadDoorByName(creator.startingDoor()).getConditionArea().getMidpoint());
						setState(GameState.GAME_RUNTIME);
						engine.fadeIn(250d);
						
					});
					
				}
				
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
	
	public PlayerCharacter player() {
		
		return player;
		
	}
	
	public Scene scene() {
		
		return scene;
		
	}
	
}