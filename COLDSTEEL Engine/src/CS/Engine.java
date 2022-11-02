package CS;

import static CS.COLDSTEEL.assets;
import static CS.COLDSTEEL.data;
import static CSUtil.BigMixin.getSCToWCForX;
import static CSUtil.BigMixin.getSCToWCForY;
import static org.lwjgl.Version.getVersion;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.nuklear.Nuklear.nk_input_begin;
import static org.lwjgl.nuklear.Nuklear.nk_input_end;
import static org.lwjgl.nuklear.Nuklear.nk_window_is_any_hovered;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import AudioEngine.SoundEngine;
import AudioEngine.Sounds;
import CS.Controls.Control;
import CSUtil.CSLogger;
import CSUtil.CSTFParser;
import CSUtil.Timer;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSQueue;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import Core.AbstractGameObjectLists;
import Core.Console;
import Core.Executor;
import Core.ObjectLists;
import Core.Quads;
import Core.Scene;
import Core.TemporalExecutor;
import Core.UIScriptingInterface;
import Core.Entities.EntityLists;
import Core.Entities.EntityScriptingInterface;
import Core.Statics.StaticLists;
import Core.TileSets.TileSets;
import Editor.CursorState;
import Editor.Editor;
import Editor.EditorMode;
import Game.Core.DebugInfo;
import Game.Core.GameRuntime;
import Game.Core.GameState;
import Game.Items.ItemScriptingInterface;
import Game.Items.UnownedItems;
import Game.Levels.LevelLoadDoors;
import Game.Levels.Levels;
import Game.Levels.MacroLevels;
import Game.Levels.TriggerScriptingInterface;
import Game.Player.PlayerCharacter;
import Game.Projectiles.ProjectileScriptingInterface;
import Networking.NetworkedInstance;
import Physics.ColliderLists;
import Renderer.Camera;
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
public class Engine {
	
	public static RuntimeState STATE;	
	private static final Thread MAIN_THREAD;
	
	/*engine static objects*/
	private static GLFWWindow WINDOW;
	//layer manager. Array of lists of game objects
	private static final CSArray<Scene> OBJECTS = new CSArray<>(CS.COLDSTEEL.NUMBER_LAYERS , 1);
	//publicly accessable python interpreter
	private static final EnginePython INTERNAL_ENGINE_PYTHON = new EnginePython();
	//scripting singletons, static so that anything that wants to have a script does not need a reference to the engine
	public static EntityScriptingInterface ENTITY_SCRIPTING_INTERFACE;
	public static UIScriptingInterface UI_SCRIPTING_INTERFACE;
	public static ItemScriptingInterface ITEM_SCRIPTING_INTERFACE;
	public static TriggerScriptingInterface TRIGGER_SCRIPTING_INTERFACE;
	public static ProjectileScriptingInterface PROJECTILE_SCRIPTING_INTERFACE;	
	public static Controls clientControls;
	
	static {
		
		MAIN_THREAD = Thread.currentThread();
		
	}
		
	public static final int addScene(Scene newScene) {
		
		int sceneIndex = OBJECTS.size();
		OBJECTS.add(newScene);
		return sceneIndex;
		
	}
	
	public static final Scene boundScene() {
		
		return OBJECTS.get(0);
		
	}
	
	public static final void bindScene(Scene bindThis) {
		
		Scene oldBound = OBJECTS.get(0);
		int newBoundOldIndex = bindThis.index;
		OBJECTS.set(0 , bindThis);
		OBJECTS.set(newBoundOldIndex , oldBound);
		
	}
	
	public static final void forBoundScene(Consumer<AbstractGameObjectLists<? extends Quads>> function) {
		
		OBJECTS.get(0).forEach(function);
		
	}
	
	private ReentrantLock lock = new ReentrantLock();
	private final CSQueue<Executor> events = new CSQueue<>();
	final Editor editor = new Editor();
	final GameRuntime gameRuntime = new GameRuntime();
	
	Renderer renderer;
	private Levels currentLevel;
	private MacroLevels currentMacroLevel;
	
	private Console engineConsole;
	DebugInfo debugInfo;
	
	/**
	 * The Scene is the star of the show. It holds references to game object manager classes, which are typically named
	 * [name of object type]List. A Scene is made up of a background and a foreground which is why there are copies of objects.
	 * 
	 */
	private Scene scene;	
	
	private Consumer<Levels> onLevelLoad = newLevel -> currentLevel = newLevel;	
	
	public Engine(RuntimeState state) {
	
		Engine.STATE = state;
		
	}
			
	void initialize() {
		
		System.out.println("END STATIC\n");

		System.out.println("Welcome to the COLDSTEEL editor alpha undef, running LWJGL " + getVersion());
		
		System.out.println("Initializing Engine...");		
		System.out.println("Engine Initialization Complete.");
		
		renderer = new Renderer();
		SoundEngine.initialize();
		
		WINDOW = new GLFWWindow();	
		
		scene = new Scene(
			new ObjectLists(1) , 
			new TileSets(3) , 
			new StaticLists(2) , 
			new EntityLists(4 , renderer.getCamera()) , 
			new UnownedItems(5) ,
			new ObjectLists(6) ,
			new TileSets(7) ,
			new StaticLists(8) , 
			new ColliderLists()	
		);
				
    	WINDOW.intialize(this);
    	
    	if(COLDSTEEL.DEBUG_CHECKS) GL.create();
    	GL.createCapabilities();
    	
    	renderer.initialize(() -> WINDOW.getWindowDimensions() , () -> WINDOW.getFramebufferDimensions() , UserInterface.context , UserInterface.drawCommands); 
    	
    	engineConsole = new Console();
    	
        // disable v-sync while loading
        glfwSwapInterval(0);
        
    	WINDOW.setNuklearContext(UserInterface.context);   	        	
    	
        try(MemoryStack stack = stackPush()){

        	glfwGetFramebufferSize(WINDOW.glfwWindow , WINDOW.winWidth , WINDOW.winHeight);
        	glViewport(0, 0, WINDOW.winWidth.get(0), WINDOW.winHeight.get(0));

        }
        
        // Make the window visible
		glfwShowWindow(WINDOW.glfwWindow);
		System.out.println("Window initialization complete.");
		
		switch(STATE) {
			
			case EDITOR:
				
	    		editor.initialize(this , renderer , scene , currentLevel , engineConsole , onLevelLoad ,
	    			(targetState) -> switchState(targetState) , () -> WINDOW.getCursorWorldCoords());	    		
				break;
				
			case GAME:
				
				gameRuntime.initialize();								
				break;
				
			default:
	
				System.out.println("Fatal Error in initialization, closing program");
				closeOverride();
				break;
	
		}
		
		ENTITY_SCRIPTING_INTERFACE = new EntityScriptingInterface(renderer, scene , engineConsole);
		UI_SCRIPTING_INTERFACE = new UIScriptingInterface(this , engineConsole);
		ITEM_SCRIPTING_INTERFACE = new ItemScriptingInterface(engineConsole , renderer , scene.entities());
		TRIGGER_SCRIPTING_INTERFACE = new TriggerScriptingInterface(scene , currentLevel);
		PROJECTILE_SCRIPTING_INTERFACE = new ProjectileScriptingInterface(scene);
		
		debugInfo = new DebugInfo(this , gameRuntime);
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

		UserInterface.threadSpinup();
		System.gc();
		CSLogger.log("Successfully initialized");
	}
	
	/**
	 * Designed to be a safe way to switch runtime state.
	 * 
	 */
	private void reinitialize() {

		switch(STATE) {
			
			case EDITOR:

	    		editor.initialize(this , renderer , scene, currentLevel , engineConsole , onLevelLoad , 
	    			(targetState) -> switchState(targetState) , () -> WINDOW.getCursorWorldCoords());
	    		
				break;
				
			case GAME:
				
				scene.clear();
				GameRuntime.setState(GameState.MAIN_MENU);
				gameRuntime.initialize();
				
				break;
				
			default:
	
				System.out.println("Fatal Error in initialization, closing program");
				closeOverride();
				break;
	
		}
				
	}	
	
    private Timer frameTimer = new Timer();
    private static int framesThisSecond = 1;
    private static int framesLastSecond = 0;
    private static final Timer engineTimer = new  Timer();
    private static double iterRate;
	private static double millisThisFrame;
	private static boolean secondPassed;
	private static long totalNumberFrames = 0;
	private static int runtimeSeconds = 0;
    public static boolean printFPS = true;
    
	void run() {

        glClearColor(WINDOW.r, WINDOW.g, WINDOW.b, 1);
        glfwSwapInterval(1);
        System.out.println("Window running.");
        //This is the main loop.
        while (!glfwWindowShouldClose(WINDOW.getGlfwWindow())) {
        	        	
	    	nk_input_begin(UserInterface.context);
	        glfwPollEvents();
	        nk_input_end(UserInterface.context);
	        
	        handleEvents();
	        
	        frameTimer.start();

        	if(engineTimer.getElapsedTimeMillis() >= 1000) {

        		secondPassed = true;
        		engineTimer.start();
        		framesLastSecond = framesThisSecond;
        		iterRate = millisThisFrame / framesLastSecond;
        		millisThisFrame = 0;
        		framesThisSecond = 0;
        		secondPassed = true;
        		totalNumberFrames += framesLastSecond;
        		runtimeSeconds++;
        		if(printFPS) System.out.println("Frames in second " + runtimeSeconds + ": " + framesLastSecond);
        		
        	} else secondPassed = false;

        	glClear(GL_COLOR_BUFFER_BIT); //wipe out previous frame
    		
        	switch (STATE){

	        	case EDITOR:
	        		
	        		editor.run(this);
	        		if(!nk_window_is_any_hovered(UserInterface.context) && WINDOW.mousePressed(GLFW_MOUSE_BUTTON_LEFT) && !WINDOW.keyboardPressed(GLFW_KEY_LEFT_SHIFT)) {
	        			
	        			glfwGetCursorPos(WINDOW.glfwWindow , WINDOW.newX , WINDOW.newY);
	        			int[] windowDims = WINDOW.getWindowDimensions();
	        			WINDOW.newX.put(0 , getSCToWCForX(WINDOW.newX.get(0) , windowDims[0] , windowDims[1] , renderer.getCamera()));
	        			WINDOW.newY.put(0 , getSCToWCForY(WINDOW.newY.get(0) , windowDims[0] , windowDims[1] , renderer.getCamera()));	        			
	        			editor.resizeSelectionArea((float)WINDOW.newX.get(0), (float)WINDOW.newY.get(0));
	        				        			
	        		}
	        			        		
	        		break;

	        	case GAME:

	        		gameRuntime.run(this);
	        		
	        		break;

        	}
        	
        	renderer.run();
        	
            glfwSwapBuffers(WINDOW.glfwWindow);
            framesThisSecond++;
            millisThisFrame += frameTimer.getElapsedTimeMillis();

        }

    }
		
	public void schedule(Executor code) {
		
		lock.lock();
		events.enqueue(code);
		lock.unlock();
		
	}
	
	private void handleEvents() {
		
		lock.lock();
		while(!events.empty()) events.dequeue().execute();
		lock.unlock();
		
	}
	
	void shutDown() {
    	
		UserInterface.shutDown1();
		renderer.shutDown();	        
		INTERNAL_ENGINE_PYTHON.shutDown();
		UserInterface.shutDown2();
		SoundEngine.shutDown();
		WINDOW.shutDown();
		ENTITY_SCRIPTING_INTERFACE.shutDown();
		gameRuntime.shutDown();
		CSLogger.shutDown();
    	System.out.println("Program shut down, until next time...");    	
    	
    }
	
	public void closeOverride() {
		
		WINDOW.overrideCloseWindow();
		
	}
		
	public static boolean secondPassed() {
		
		return secondPassed;
		
	}
	
	public static int framesLastSecond() {
		
		return framesLastSecond;
		
	}
	
	public static int currentFrame() {
		
		return framesThisSecond;
		
	}
	
	public static double millisThisFrame() {
		
		return millisThisFrame;
		
	}
	
	public static int averageFramerate() {
		
		return (int) (totalNumberFrames / runtimeSeconds);
		
	}
	
	public void currentLevel(Levels currentLevel) {
		
		this.currentLevel = currentLevel;
		
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
		
		WINDOW.releaseKeys();
		
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
		
		return WINDOW.keyboardPressed(glfwKey);
		
	}
	
	public static boolean keyboardStruck(int glfwKey) { 
		
		return WINDOW.keyboardStruck(glfwKey);
		
	}
	
	/*
	 * Mouse state queriers
	 */
	public static boolean mousePressed(int glfwKey) { 
		
		return WINDOW.mousePressed(glfwKey);
		
	}

	public static boolean mouseStruck(int glfwKey) {
		
		return WINDOW.mouseStruck(glfwKey);
		
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
				
		GameState previousState = GameRuntime.getState();
		
		GameRuntime.setState(GameState.BUSY);
		fadeToBlack(100d);
		TemporalExecutor.onElapseOf(100d , () -> {
			
			PlayerCharacter player = gameRuntime.player();
			g_loadClearDeploy(data + "macrolevels/" + loadDoor.linkedLevel());
			//this sets the player used load door to the load door linked to the one they just walked through
			player.previouslyUsedLoadDoor(currentLevel.getLoadDoorByName(loadDoor.linkedLoadDoorName()));
			float[] doorPos = player.previouslyUsedLoadDoor().getConditionArea().getMidpoint();
			player.moveTo(doorPos);
			scene.entities().addStraightIn(player.playersEntity());
			player.playersEntity().LID(0);

			NetworkedInstance multiplayer = gameRuntime.server();
			if(multiplayer != null) { 
				
				try {
					
					multiplayer.onLevelLoad(currentLevel , doorPos);
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			GameRuntime.setState(previousState);
			fadeIn(100d);
			
		});
		
	}
	
	public void g_loadSave(String selectedSaveNamePath , GameState targetState , boolean fade) {
		
		if(fade) {

			fadeToBlack(2000);
			GameRuntime.setState(GameState.BUSY);
			
			TemporalExecutor.onElapseOf(2400d , () -> {
				
				try (BufferedReader reader = Files.newBufferedReader(Paths.get(selectedSaveNamePath))){
					
					PlayerCharacter player = gameRuntime.player();
					
					if(player == null) player = new PlayerCharacter();
					player.load(reader);
					player.nextSave(Character.getNumericValue(selectedSaveNamePath.charAt(selectedSaveNamePath.length() - 6)));
					player.playersEntity().LID(0);
					
					CSTFParser cstf = new CSTFParser(reader);
					cstf.rlist();
					
					g_loadClearDeploy(data + "macrolevels/" + cstf.rlabel("level") + ".CStf");
					float[] playersPosition = new float[2];
					cstf.rlabel("position" , playersPosition);
					
					cstf.endList();
					
					player.moveTo(playersPosition);
					scene.entities().addStraightIn(player.playersEntity());
									
					GameRuntime.setState(targetState);				
					gameRuntime.player(player);
									
					fadeIn(1000);
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			});
		
		} else {

			GameRuntime.setState(GameState.BUSY);

			try (BufferedReader reader = Files.newBufferedReader(Paths.get(selectedSaveNamePath))){
				
				PlayerCharacter player = gameRuntime.player();
				
				if(player == null) player = new PlayerCharacter();
				player.load(reader);
				player.nextSave(Character.getNumericValue(selectedSaveNamePath.charAt(selectedSaveNamePath.length() - 6)));
				player.playersEntity().LID(0);
				
				CSTFParser cstf = new CSTFParser(reader);
				cstf.rlist();
				
				g_loadClearDeploy(data + "macrolevels/" + cstf.rlabel("level") + ".CStf");
				float[] playersPosition = new float[2];
				cstf.rlabel("position" , playersPosition);
				
				cstf.endList();
				
				player.moveTo(playersPosition);
				scene.entities().addStraightIn(player.playersEntity());
								
				GameRuntime.setState(targetState);				
				gameRuntime.player(player);
							
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
		}
			
	}

	private void g_buildOSTIntro(CSLinked<Sounds> introSegments) {
		
		//play sounds and queue all the next ones.
		cdNode<Sounds> iter = introSegments.get(0);
		iter.val.play();
		
		if(introSegments.size() > 1) {

			iter = iter.next;
			//plays each intro segment by starting it after the last has stopped
			for(int i = 1 ; i < introSegments.size() ; i ++ , iter = iter.next) {
				
				cdNode<Sounds> node = iter;
				TemporalExecutor.onTrue(() -> node.prev.val.stopped() , () -> node.val.play());
				
			}
			
		} 
		
	}
	
	private void g_buildOSTLoop(CSLinked<Sounds> loopSegments) {
		
		cdNode<Sounds> iter = loopSegments.get(0);
		iter.val.play();
		if (loopSegments.size() > 1) {

			iter = iter.next;
			for(int i = 1 ; i < loopSegments.size() - 1; i ++ , iter = iter.next) {
				
				cdNode<Sounds> node = iter;
				TemporalExecutor.onTrue(() -> node.prev.val.stopped() , () -> node.val.play());
				
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
				
		Levels newLevel = new Levels((CharSequence)(filepath));
		MacroLevels macroLevel = newLevel.associatedMacroLevel();
		
		if(currentMacroLevel == null) setupMacroLevelOST(macroLevel); 
		else if(currentMacroLevel != null && !macroLevel.equals(currentMacroLevel)) {
			
			Renderer.freeMacroLevel(currentMacroLevel);
			SoundEngine.freeMacroLevel(currentMacroLevel);
			setupMacroLevelOST(macroLevel);
			
		}
		
		currentMacroLevel = macroLevel;
		
		scene.clear();
		newLevel.deploy(scene);
		
		currentLevel = newLevel;

		onLevelLoad.accept(newLevel);
		TRIGGER_SCRIPTING_INTERFACE.onLevelLoad.accept(newLevel);		
		
	}

	public void g_clearDeploy(Levels level , Scene scene) {
		
		MacroLevels macroLevel = level.associatedMacroLevel();
		
		if(currentMacroLevel == null) setupMacroLevelOST(macroLevel);
		else if(currentMacroLevel != null && !macroLevel.equals(currentMacroLevel)) {
			
			Renderer.freeMacroLevel(currentMacroLevel);
			SoundEngine.freeMacroLevel(currentMacroLevel);
			setupMacroLevelOST(macroLevel);
		
		}
		
		currentMacroLevel = macroLevel;
		
		this.scene.clear();
		this.scene = scene;
		
		currentLevel = level;
		
		onLevelLoad.accept(level);
		TRIGGER_SCRIPTING_INTERFACE.onLevelLoad.accept(level);		
	
	}

	public boolean g_keyPressed(int key) {
		
		return WINDOW.keyboardPressed(key); 
		
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
	
	public void mg_propogateNewClientAndServer() {
		
		ENTITY_SCRIPTING_INTERFACE.setNetworkingVariables(gameRuntime.client() , gameRuntime.server());
		
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
	
	Editor e_getEditor() {

		if(STATE != RuntimeState.EDITOR) return null;
		return editor;
		
	}
	
	void e_toggleMouseHold() {

		if(STATE != RuntimeState.EDITOR) return;
		editor.toggleCursorState();
		
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
		scene.entities().newEntity();
		
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
    
    public static boolean isMainThread() {
    	
    	return Thread.currentThread() == MAIN_THREAD;
    	
    }
    
}
