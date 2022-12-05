package CS;

import static CS.COLDSTEEL.assets;
import static CS.COLDSTEEL.data;
import static org.lwjgl.Version.getVersion;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.nuklear.Nuklear.nk_input_begin;
import static org.lwjgl.nuklear.Nuklear.nk_input_end;
import static org.lwjgl.nuklear.Nuklear.nk_window_is_any_hovered;
import static org.lwjgl.opengl.GL11C.glViewport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import Audio.SoundEngine;
import Audio.Sounds;
import CS.Controls.Control;
import CSUtil.BigMixin;
import CSUtil.CSLogger;
import CSUtil.CSTFParser;
import CSUtil.Timer;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSQueue;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import CSUtil.Dialogs.Debug.PerformanceDebug;
import CSUtil.Dialogs.Debug.ResourceDebug;
import CSUtil.Dialogs.Debug.SceneDebug;
import Core.Console;
import Core.Executor;
import Core.Scene;
import Core.TemporalExecutor;
import Editor.CursorState;
import Editor.Editor;
import Editor.EditorMode;
import Game.Core.DebugInfo;
import Game.Core.GameRuntime;
import Game.Core.GameState;
import Game.Levels.LevelLoadDoors;
import Game.Levels.Levels;
import Game.Levels.MacroLevels;
import Game.Player.PlayerCharacter;
import Networking.NetworkClient;
import Renderer.Camera;
import Renderer.RenderCommands;
import Renderer.Renderer;

/**
 * 
 * The application class for the program. Is the highest level class because it creates everything and manages the state of the application as a whole.
 * This will create the GLFWWindow, Audio System, Renderer, Editor and or Game classes. This class's constructor should only ever be called on startup.
 * Engine handles the scene which is represented as a record, and UI singleton structs. 
 * It also handles some game runtime behavior, mainly loading levels. 
 * The engine is the authority for distributing important data to other systems within the program.
 * 
 * @author Chris Brown
 *
 */
public final class Engine {
	
	public static RuntimeState STATE;
	
	/*engine static objects*/
	
	private static GLFWWindow window;
	//publicly accessable python interpreter
	private static final EnginePython INTERNAL_ENGINE_PYTHON = new EnginePython();
	//whatever the client is currently using as their bound controls
	public static Controls clientControls;
	
	//update variables
	private static final Timer engineTimer = new  Timer();
    private static final Timer frameTimer = new Timer();
    private static int framesThisSecond = 1;
    private static int framesLastSecond = 0;
    private static double iterRate;
	private static int runtimeSeconds = 0;
    public static boolean printFPS = false;
    
	public EngineConfig config = new EngineConfig();	
	private final CSQueue<Executor> events = new CSQueue<>();
	private Console engineConsole;
	private SceneDebug sceneViewerDebug;
	private SceneDebug renderSceneViewerDebug;
	private DebugInfo debugInfo;
	private ResourceDebug resourceDebugInfo = new ResourceDebug("Native Resources" , 715 , 5);
	private PerformanceDebug performanceDebugInfo;
	
	private Levels currentLevel;
	private MacroLevels currentMacroLevel;
	
	private Editor editor;
	private GameRuntime gameRuntime;
	private Renderer renderer;
	private ReentrantLock shutDownOrder = new ReentrantLock();		
	
	public Engine(RuntimeState state) {
	
		Engine.STATE = state;
		
	}
			
	void initialize() {
		
		System.out.println("END STATIC\n");
		System.out.println("Welcome to the COLDSTEEL Engine alpha undef, running LWJGL " + getVersion());		
		System.out.println("Initializing Engine...");		
		
		SoundEngine.threadSpinup();
		
		window = new GLFWWindow();	
		window.intialize(this);
		
    	//start the renderer
    	renderer = new Renderer(shutDownOrder , null , window);
    	
    	UserInterface.initialize(renderer);
    	
    	//wait for the UI to finish initializing
    	while(!UserInterface.initialized());
    	
    	//setup renderer nuklear environment
    	renderer.setNuklearEnv(UserInterface.getContext() , UserInterface.getCommands());
    	renderer.schedule(RenderCommands.INITIALIZE_NUKLEAR);
    	    	
    	engineConsole = new Console();
    	    	
    	window.setNuklearContext(UserInterface.context);  
    	UserInterface.currentWindowDimensions = window.getWindowDimensions();    	
        window.show();
        		
		switch(STATE) {
			
			case EDITOR -> {
				
				editor = new Editor();
				editor.initialize(this , engineConsole);
				renderer.toggleRenderDebug(null);
				renderer.renderScene(editor.scene());
				sceneViewerDebug = new SceneDebug("Editor Scene" , editor.scene() , 5 , 5);
				
			}
				
			case GAME -> {
				
				gameRuntime = new GameRuntime();
				gameRuntime.initialize(this);
				renderer.renderScene(gameRuntime.scene());
				sceneViewerDebug = new SceneDebug("Editor Scene" , gameRuntime.scene() , 5 , 5);
				
			}			
			
		}
	
		renderSceneViewerDebug = new SceneDebug("Render Scene" , renderer.renderScene() , 360 , 5);		
		debugInfo = new DebugInfo(this , gameRuntime);
		performanceDebugInfo = new PerformanceDebug(this , "Performance Debug" , 1070 , 5);
		clientControls = new Controls();
		
		//read and set controls
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(data + "engine/controls.CStf"))){ 
			
			CSTFParser cstf = new CSTFParser(reader);
			int listSize = cstf.rlist("game");
			
			int[] key = new int[2];			
			for(int i = 0 ; i < listSize ; i ++) {
				
				 String name = cstf.rlabel(key);
				 clientControls.setByName(name, (byte) key[0] , key[1]);				 
			}
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}

		System.gc();
		CSLogger.log("Successfully initialized");
		System.out.println("Engine Initialization Complete.");
		
	}
	
	/**
	 * Designed to be a safe way to switch runtime state.
	 * 
	 */
	private void reinitialize() {

		switch(STATE) {
			
			case EDITOR:

				if(editor == null) { 
					
					editor = new Editor();
					editor.initialize(this , engineConsole);
					renderer.toggleRenderDebug(null);	    		
					sceneViewerDebug.scene(editor.scene());
					
				}
				
	    		renderer.renderScene(editor.scene());
	    		
				break;
				
			case GAME:
				
				if(gameRuntime == null) { 
					
					gameRuntime = new GameRuntime();
					gameRuntime.initialize(this);
										
				}
				
				gameRuntime.setState(GameState.MAIN_MENU);
				renderer.renderScene(gameRuntime.scene());	
				sceneViewerDebug.scene(gameRuntime.scene());
								
				break;
				
			default:
	
				System.out.println("Fatal Error in initialization, closing program");
				closeOverride();
				break;
	
		}
				
		renderSceneViewerDebug.scene(renderer.renderScene());
		
	}	
	
	private void updateEngineState() {

		//handle enqueued events
		synchronized(events) {
			
			while(!events.empty()) events.dequeue().execute();
			
		}	
		
        frameTimer.start();

    	if(engineTimer.getElapsedTimeMillis() >= 1000) {

    		engineTimer.start();
    		framesLastSecond = framesThisSecond;
    		framesThisSecond = 0;
    		runtimeSeconds++;
    		if(printFPS) System.out.println("Frames in second " + runtimeSeconds + ": " + framesLastSecond);
    		        		
    	}
    	
	}
	
	void run() {

        while(window.persist()) {
        	
        	//starts the UI processing thread and gives a future to the renderer to use to determine if to renderUI
        	
        	nk_input_begin(UserInterface.context);
        	glfwPollEvents(); 
       		nk_input_end(UserInterface.context);        	

    		updateEngineState();
    		
    		switch (STATE) {
    		
	    		case EDITOR -> editor.run(this);	        		
	    		case GAME -> gameRuntime.run(this);
	    		    		
    		}
    		
    		framesThisSecond++;
    		
        	//I lock us into 62 fps by waiting until 16 ms have passed because 
       		//I don't think theres any real benefit of high frame rates
       		//for this engine, although they certainly could be achieved 
    		while(16 - frameTimer.getElapsedTimeMillis() > 0.0d);
    		    		
        }
        
    }
		
	public void schedule(Executor code) {
		
		synchronized(events) {
			
			events.enqueue(code);
			
		}
		
	}
	
	public float[] cursorWorldCoords() {
		
		return window.getCursorWorldPos();
		
	}
	
	public boolean isUIHovered() {
		
		return nk_window_is_any_hovered(UserInterface.context);
		
	}
	
	public void windowifyWindow() {

		window.windowify();		
		
	}
	
	public void fullscreenifyWindow() {
		
		window.fullscreenify();
		
	}
	
	void updateWorldOnWindowResize(int width , int height) {
		
		Renderer.schedule(() -> renderer.setViewport(width , height));
		UserInterface.currentWindowDimensions[0] = width;
		UserInterface.currentWindowDimensions[1] = height;
		
	}
	
	void setViewPort(int width , int height) {
		
		Renderer.schedule(() -> glViewport(0, 0, width , height));
		
	}
	
	/*
	 * Steps to shutdown:
	 * 		1) stop rendering
	 * 		2) shutdown UI
	 * 		3) shut down renderer
	 * 		4) shutdown UI part 2
	 * 		5) shutdown everything else
	 */
	void shutDown() {
		
		//initiates shutdown of the sound engine
		SoundEngine.persist = false;
		
		//write the current state of EngineConfig.cstf
		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(data + "engine/EngineConfig.cstf"))) {
			
			CSTFParser parser = new CSTFParser(writer);
			
			parser.wlist("previous saves");
			
			if(config.lastSinglePlayerSave != null) parser.wlabelValue("singleplayer" , config.lastSinglePlayerSave);
			else parser.wnullLabel("singleplayer");
			
			parser.endList();
			
			parser.wlabelValue("main menu wallpaper" , config.mainMenuWallpaper);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}		
		
		renderer.schedule(RenderCommands.INITIATE_SHUT_DOWN);
		
		renderer.join();
		
		shutDownOrder.lock();
		
		UserInterface.static_shutDown();
		
		INTERNAL_ENGINE_PYTHON.shutDown();
		window.shutDown();
	 	if(gameRuntime != null) gameRuntime.shutDown();
		CSLogger.shutDown();
    	System.out.println("Program shut down, until next time...");
    	    	
    	shutDownOrder.unlock();
    	
    	BigMixin.asyncShutDown();
    	
    }
	
	public void closeOverride() {
		
		window.overrideCloseWindow();
		
	}
	
	public static int framesLastSecond() {
		
		return framesLastSecond;
		
	}

	public int renderFramesLastSecond() {
		
		return renderer.framesLastSecond;
		
	}
	
	public static int currentFrame() {
		
		return framesThisSecond;
		
	}
	
	public void currentLevel(Levels currentLevel) {
		
		this.currentLevel = currentLevel;
		
	}
	
	void toggleDebugInfo() {

		debugInfo.toggle();
		sceneViewerDebug.toggle();
		renderSceneViewerDebug.toggle();
		resourceDebugInfo.toggle();
		performanceDebugInfo.toggle();
		
	}
		
	public Levels currentLevel() {
		
		return currentLevel;
		
	}
		
	public MacroLevels currentMacroLevel() {
		
		return currentMacroLevel;
		
	}
	
	public void switchState(RuntimeState state) {
		
		STATE = state;
		reinitialize();
	
	}
	
	public void fadeToBlack(double milliTime) {
		
		TemporalExecutor.withElapseOf(milliTime , x -> renderer.screenQuad.makeTranslucent((float)(x / milliTime)));
		
	}

	public void fadeIn(double milliTime) {
		
		TemporalExecutor.withElapseOf(milliTime , x -> renderer.screenQuad.makeTranslucent((float)(-x /milliTime) + 1));
		
	}
	
	public void releaseKeys() {
		
		window.releaseKeys();
		
	}
	
	public void setRenderScene(Scene renderScene) {
		
		renderer.renderScene(renderScene);
		
	}
	
	public void toggleRenderDebug(Levels levelToDebugRender) {
		
		renderer.toggleRenderDebug(levelToDebugRender);
		
	}

	public boolean isRenderingDebug() {
		
		return renderer.isRenderingDebug();
		
	}
	
	/**
	 * True if the key referenced by {@code query} is pressed.
	 * 
	 * @param query � a User Control object
	 * @return true if the key referenced in {@code query} is pressed
	 */
	public static boolean controlKeyPressed(Control query) {

		return switch(query.peripheral()) {
		
			case Controls.KEYBOARD -> keyboardPressed(query.key());
			case Controls.MOUSE -> mousePressed(query.key());
			case Controls.GAMEPAD -> throw new IllegalArgumentException("Unexpected value: " + query.peripheral());
			default -> throw new IllegalArgumentException("Unexpected value: " + query.peripheral());
		
		};
		
	}

	/**
	 * True if the key referenced by {@code query} is pressed.
	 * 
	 * @param query � a User Control object
	 * @return true if the key referenced in {@code query} is pressed
	 */
	public static boolean controlKeyPressed(byte queriedID) {

		Control query = clientControls.getByIndex(queriedID);
		
		return switch(query.peripheral()) {
		
			case Controls.KEYBOARD -> keyboardPressed(query.key());
			case Controls.MOUSE -> mousePressed(query.key());
			case Controls.GAMEPAD -> throw new IllegalArgumentException("Unexpected value: " + query.peripheral());
			default -> throw new IllegalArgumentException("Unexpected value: " + query.peripheral());
		
		};
		
	}

	public static boolean controlKeyStruck(Control query) {

		return switch(query.peripheral()) {
		
			case Controls.KEYBOARD -> keyboardStruck(query.key());					
			case Controls.MOUSE -> mouseStruck(query.key());
			case Controls.GAMEPAD -> throw new IllegalArgumentException("Unexpected value: " + query.peripheral());
			default -> throw new IllegalArgumentException("Unexpected value: " + query.peripheral());
		
		};
		
	}

	public static boolean controlKeyStruck(byte queriedID) {

		Control query = clientControls.getByIndex(queriedID);
		
		return switch(query.peripheral()) {
		
			case Controls.KEYBOARD -> keyboardStruck(query.key());					
			case Controls.MOUSE -> mouseStruck(query.key());
			case Controls.GAMEPAD -> throw new IllegalArgumentException("Unexpected value: " + query.peripheral());
			default -> throw new IllegalArgumentException("Unexpected value: " + query.peripheral());
		
		};
		
	}
	
	/*
	 * Keyboard state queriers 
	 */
	
	public static boolean keyboardPressed(int glfwKey) { 
		
		return window.keyboardPressed(glfwKey);
		
	}
	
	public static boolean keyboardStruck(int glfwKey) { 
		
		return window.keyboardStruck(glfwKey);
		
	}
	
	/*
	 * Mouse state queriers
	 */
	public static boolean mousePressed(int glfwKey) { 
		
		//if a ui wigit is hovered, we will say the key is not pressed for the purposes of checking if a mouse control is pressed
		return !nk_window_is_any_hovered(window.NuklearContext) && window.mousePressed(glfwKey);
		
	}

	public static boolean mouseStruck(int glfwKey) {
		
		return window.mouseStruck(glfwKey);
		
	}
	
	public Editor editor() {
		
		return editor;
		
	}
	
	public GameRuntime gameRuntime() {
		
		return gameRuntime;
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Game Functions					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	void g_openGameMenu() {
		
		if(STATE.equals(RuntimeState.GAME)) gameRuntime.toggleGameMenu();
		
	}
	
	/**
	 * In charge of calling scripts and checking players against load doors
	 * 
	 */
	public void g_levelUpdate() {
		
		currentLevel.runScripts();
		LevelLoadDoors door;
		PlayerCharacter player = gameRuntime.player();
		/*
		 * if we are colliding with a load door and this load door has a linked load door
		 * and the player can pass through this load door (they cannot pass through it if they just used it
		 * 
		 *  
		 */
		if((door = currentLevel.playerCollidingWithLoadDoor(player)) != null  && door != player.previouslyUsedLoadDoor() && door.hasLinkedDoor()) {
			
			g_loadLevelFromLoadDoor(door);
			
		} else if (door == null) player.previouslyUsedLoadDoor(null);
		
	}
	
	/**
	 * 
	 * Takes the user to the level linked to this load door.
	 * 
	 * @param loadDoor
	 */
	public void g_loadLevelFromLoadDoor(LevelLoadDoors loadDoor) {
				
		GameState previousState = gameRuntime.getState();
		
		gameRuntime.setState(GameState.BUSY);
		fadeToBlack(100d);
		TemporalExecutor.onElapseOf(100d , () -> {
			
			PlayerCharacter player = gameRuntime.player();
			g_loadClearDeploy(data + "macrolevels/" + loadDoor.linkedLevel());
			//this sets the player used load door to the load door linked to the one they just walked through
			player.previouslyUsedLoadDoor(currentLevel.getLoadDoorByName(loadDoor.linkedLoadDoorName()));
			float[] doorPos = player.previouslyUsedLoadDoor().getConditionArea().getMidpoint();
			player.moveTo(doorPos[0] , doorPos[1]);
			gameRuntime.scene().entities().addStraightIn(player.playersEntity());
			player.playersEntity().LID(0);

			NetworkClient playerAsClient = gameRuntime.client();
			
			if(playerAsClient != null) { 
				
				try {
					
					playerAsClient.onLevelLoad(currentLevel , doorPos);
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			gameRuntime.setState(previousState);
			fadeIn(100d);
			
		});
		
	}
	
	public void g_loadLastSave() {

		int endOfSaveName = config.lastSinglePlayerSave.indexOf("#");		
//		g_loadSave(data + "saves/" + config.lastSinglePlayerSave.substring(0, endOfSaveName - 1) , GameState.GAME_RUNTIME_SINGLEPLAYER, true);
		
		g_loadSave(
			data + "saves/" + config.lastSinglePlayerSave.substring(0, endOfSaveName - 1) + "/" + config.lastSinglePlayerSave , 
			GameState.GAME_RUNTIME_SINGLEPLAYER , 
			true
		);
	
	}
	
	public void g_loadSave(String selectedSaveAbsPath , GameState targetState , boolean fade) {
		
		if(fade) {

			fadeToBlack(2000);
			gameRuntime.setState(GameState.BUSY);
			
			TemporalExecutor.onElapseOf(2400d , () -> {
				
				try (BufferedReader reader = Files.newBufferedReader(Paths.get(selectedSaveAbsPath))){
					
					PlayerCharacter player = gameRuntime.player();
					
					if(player == null) player = new PlayerCharacter(gameRuntime.scene());
					player.load(reader);
					player.nextSave(Character.getNumericValue(selectedSaveAbsPath.charAt(selectedSaveAbsPath.length() - 6)));
					player.playersEntity().LID(0);
					
					CSTFParser cstf = new CSTFParser(reader);
					cstf.rlist();
					
					g_loadClearDeploy(data + "macrolevels/" + cstf.rlabel("level") + ".CStf");
					float[] playersPosition = new float[2];
					cstf.rlabel("position" , playersPosition);
					
					cstf.endList();
					
					player.moveTo(playersPosition[0] , playersPosition[1]);
					gameRuntime.scene().entities().addStraightIn(player.playersEntity());
									
					gameRuntime.setState(targetState);				
					gameRuntime.player(player);
										
					config.lastSinglePlayerSave = CSUtil.BigMixin.toNamePath(selectedSaveAbsPath);
					
					fadeIn(1000);
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			});
		
		} else {

			gameRuntime.setState(GameState.BUSY);

			try (BufferedReader reader = Files.newBufferedReader(Paths.get(selectedSaveAbsPath))){
				
				PlayerCharacter player = gameRuntime.player();
				
				if(player == null) player = new PlayerCharacter(gameRuntime.scene());
				player.load(reader);
				player.nextSave(Character.getNumericValue(selectedSaveAbsPath.charAt(selectedSaveAbsPath.length() - 6)));
				player.playersEntity().LID(0);
				
				CSTFParser cstf = new CSTFParser(reader);
				cstf.rlist();
				
				g_loadClearDeploy(data + "macrolevels/" + cstf.rlabel("level") + ".CStf");
				float[] playersPosition = new float[2];
				cstf.rlabel("position" , playersPosition);
				
				cstf.endList();
				
				player.moveTo(playersPosition[0] , playersPosition[1]);
				gameRuntime.scene().entities().addStraightIn(player.playersEntity());
								
				gameRuntime.setState(targetState);				
				gameRuntime.player(player);
							
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
		}
			
	}

	private void g_buildOSTIntro(CSLinked<Sounds> introSegments) {
		
		//play sounds and queue all the next ones.
		cdNode<Sounds> iter = introSegments.get(0);
		SoundEngine.play(iter.val);
		
		if(introSegments.size() > 1) {

			iter = iter.next;
			//plays each intro segment by starting it after the last has stopped
			for(int i = 1 ; i < introSegments.size() ; i ++ , iter = iter.next) {
				
				cdNode<Sounds> node = iter;
				TemporalExecutor.onTrue(() -> node.prev.val.stopped() , () -> SoundEngine.play(node.val));
				
			}
			
		} 
		
	}
	
	private void g_buildOSTLoop(CSLinked<Sounds> loopSegments) {
		
		cdNode<Sounds> iter = loopSegments.get(0);
		SoundEngine.play(iter.val);		
		if (loopSegments.size() > 1) {

			iter = iter.next;
			for(int i = 1 ; i < loopSegments.size() - 1; i ++ , iter = iter.next) {
				
				cdNode<Sounds> node = iter;
				TemporalExecutor.onTrue(() -> node.prev.val.stopped() , () -> SoundEngine.play(node.val));
				
			}
			
			cdNode<Sounds> node = iter;
			TemporalExecutor.onTrue(() -> node.prev.val.stopped() , () -> g_buildOSTLoop(loopSegments));
			
		} else {
			
			cdNode<Sounds> node = iter;
			TemporalExecutor.onTrue(() -> node.val.stopped() , () -> g_buildOSTLoop(loopSegments));
		
		}
		
	}
	
	private void setupMacroLevelOST(MacroLevels macro) {

		//temporary holder of sounds loaded
		CSLinked<Sounds> introSegments = new CSLinked<>();			
		macro.forEachIntroSegment(x -> introSegments.add(SoundEngine.add(assets + "sounds/" + x)));
	
		CSLinked<Sounds> loopSegments = new CSLinked<>();			
		macro.forEachLoopSegment(x -> loopSegments.add(SoundEngine.add(assets + "sounds/" + x)));
		
		if(introSegments.size() > 0) g_buildOSTIntro(introSegments);
		
		if(loopSegments.size() > 0) {			
		
			if(introSegments.size() > 0) 
				TemporalExecutor.onTrue(() -> introSegments.get(introSegments.size() - 1).val.stopped() , () -> g_buildOSTLoop(loopSegments));
			else g_buildOSTLoop(loopSegments);
			
		}
	}
	
	/**
	 * Loads the level at the given absolute file path, clears the scene, and deploys the new level. This should be called
	 * <b> any </b> time a level should be loaded. 
	 * 
	 * @param filepath � filepath to the level
	 */
	public void g_loadClearDeploy(String filepath) {
				
		Levels newLevel = new Levels(gameRuntime.scene() , (CharSequence)(filepath));
		MacroLevels macroLevel = newLevel.associatedMacroLevel();
		
		if(currentMacroLevel == null) setupMacroLevelOST(macroLevel); 
		else if(currentMacroLevel != null && !macroLevel.equals(currentMacroLevel)) {
			
			Renderer.freeMacroLevel(currentMacroLevel);
			SoundEngine.freeMacroLevel(currentMacroLevel);
			setupMacroLevelOST(macroLevel);
			
		}
		
		currentMacroLevel = macroLevel;
		
		gameRuntime.scene().clear();
		newLevel.deploy(gameRuntime.scene());
		
		currentLevel = newLevel;
	
	}

	public boolean g_keyPressed(int key) {
		
		return window.keyboardPressed(key); 
		
	}
	
	public void mg_toggleMultiplayerUI() {
		
		if(STATE != RuntimeState.GAME) return;
		gameRuntime.toggleMultiplayerUI();
		
	}
	
	public void mg_enterTextChat() {
		
	}
	
	public void mg_startHostedServer() {
		
		gameRuntime.startUserHostedServer(this);
		
	}
	
	public boolean mg_isHostedServerRunning() {
		
		return gameRuntime.isHostedServerRunning();
		
	}
	
	public String mg_hostedServerIPAddress() {
		
		return gameRuntime.hostedServerIPAddress();
		
	}
	
	public int mg_hostedServerPort() {
		
		return gameRuntime.hostedServerPort();
		
	}
	
	public void mg_scrollCamera(float x , float y) {
		
		renderer.getCamera().moveCamera(x, y);
		
	}
	
    /*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Editor Functions				|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	void  e_returnToMainMenu() {
		
		if(STATE == RuntimeState.EDITOR) editor.leaveEditor();
		
	}
	
	Editor e_getEditor() {

		if(STATE != RuntimeState.EDITOR) return null;
		return editor;
		
	}
	
	void e_toggleMouseHold() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.toggleCursorState();
		
	}
	
	void e_moveCamera(float xWorld , float yWorld) {
		
		if(Engine.STATE == RuntimeState.EDITOR && editor.getState() == EditorMode.BUILD_MODE && editor.activeQuad() == null) {
			
			renderer.getCamera().moveCamera(xWorld, yWorld);
			
		}
		
	}
	
	/*
	 * Add Functions 
	 * 
	 */
	
	void e_addQuad() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.addQuad();
		
	}
		
	void e_addCollider() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.addCollider();
		
	}
	
	void e_addStatic() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.addStatic();
		
	}
	
	void e_loadStatic() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.loadStatic();
		
	}
	
	void e_addEntity() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.addEntity();
		
	}
	
	void e_loadEntity() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.loadEntity();
		
	}
		
	void e_loadItem() {
		
		if(STATE != RuntimeState.EDITOR) return;
		editor.loadItem();
		
	}
	
	void e_newItem() {
		
		if(STATE != RuntimeState.EDITOR) return;
		editor.newItem();
		
	}
	
	
	/*
	 * Tranform functions
	 * 
	 */

	void e_attemptTransform(float x , float y) {

		if(STATE != RuntimeState.EDITOR || editor.getState() != EditorMode.BUILD_MODE) return;
		editor.translateActive(x, y);
		
	}

	/*
	 * Delete Functions
	 * 
	 */
	
	void e_removeActiveQuad() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.removeActive();
		
	}
	
	void e_deleteScene() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.deleteScene();
		
	}
	
	/*
	 * Helpers
	 * 
	 */	
	
	boolean e_select(float x , float y) {
		
		//returns true if the same object was clicked
		if(STATE != RuntimeState.EDITOR) return false;
		return editor.selectQuad(x , y);
		
	}
	
	void e_setCursorState(CursorState state) {
		
		editor.cursorState(state);
		
	}
	
	CursorState e_cursorState() {
		
		return editor.cursorState();
		
	}
	
	void returnConsole() {
		
		if(STATE != RuntimeState.EDITOR) return;
		engineConsole.enter();
		
	}
	
	public Console getConsole() {
		
		return engineConsole;
		
	}
	
	boolean e_isNothingSelected() {
		
		if(STATE != RuntimeState.EDITOR) return false;
		return editor.activeQuad() == null;
		
	}
	
	void e_setActiveQuadColor() {
		
		if(STATE != RuntimeState.EDITOR) return;
		Supplier<float[]> colors = DialogUtils.newColorChooser("Apply This Color as a Filter", 5, 270);
		TemporalExecutor.onTrue(() -> colors.get() != null , () -> editor.filterActiveColor(colors.get()[0] , colors.get()[1] , colors.get()[2]));
				
	}
	
	void e_filterActiveObjectColor() {

		if(STATE != RuntimeState.EDITOR) return;
		Supplier<float[]> colors = DialogUtils.newColorChooser("Remove This Color", 5, 270);
		TemporalExecutor.onTrue(() -> colors.get() != null , () -> editor.removeActiveColor(colors.get()[0] , colors.get()[1] , colors.get()[2]));
		
	}

	void e_textureActiveObject() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.textureActive();
		
	}
	
	void e_copyActiveObject() {}	
	
	void e_pasteCopyAtCursor() {}
		
	void e_newEntity() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.addEntity();
		
	}
	
	void e_loadLevel() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.loadLevel();
					
	}

	void e_snapSelectionArea() {
		
		editor.snapSelectionArea();
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |						Getters						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
    public Camera getCamera() {
    	
    	return renderer.getCamera();
    	
    }
    
    public static double iterationRateLastSecond(){

    	return iterRate;

    }

    public int getNumberFrames(){

    	return framesThisSecond;

    }
    
    public static EnginePython INTERNAL_ENGINE_PYTHON() {
    	
    	return INTERNAL_ENGINE_PYTHON;
    	
    }
    
    public static int[] getWindowDimensions() {
    	
    	return window.getWindowDimensions();
    	
    }
    
    /**
     * Class representing config files the engine loads, the contents of which will be publicly accessable.
     *
     */
    public class EngineConfig {
    
    	public String lastSinglePlayerSave;
    	public String mainMenuWallpaper;
    	
    	EngineConfig() {
    		
    		try(BufferedReader reader = Files.newBufferedReader(Paths.get(data + "engine/EngineConfig.cstf"))){

    			CSTFParser parser = new CSTFParser(reader);
    			
    			parser.rlist();
    			if(!parser.rtestNull()) {
    				
    				lastSinglePlayerSave = parser.rlabel("singleplayer");	
    				
    			} else {
    				
    				parser.rlabel();
    				lastSinglePlayerSave = null;
    				
    			}    	
    			
    			parser.endList();
    			
    			mainMenuWallpaper = parser.rlabel("main menu wallpaper");
    			    			
    		} catch (IOException e) {
    			
    			e.printStackTrace();
    			throw new IllegalStateException("Failed to initialize engine");
				
			}
    		
    	}
    	    	
    }    
    
}
