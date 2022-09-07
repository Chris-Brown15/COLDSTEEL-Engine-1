package CS;

import static CS.COLDSTEEL.assets;
import static CS.COLDSTEEL.data;
import static CS.COLDSTEEL.root;
import static CSUtil.BigMixin.getSCToWCForX;
import static CSUtil.BigMixin.getSCToWCForY;
import static org.lwjgl.Version.getVersion;
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
import static org.lwjgl.system.MemoryUtil.nmemAlloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.python.core.PyType;

import AudioEngine.SoundEngine;
import AudioEngine.Sounds;
import CSUtil.CSTFParser;
import CSUtil.NkInitialize;
import CSUtil.Timer;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import Core.AbstractGameObjectLists;
import Core.Console;
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
	
	/*engine static objects*/
	//layer manager. Array of lists of game objects
	private static final CSArray<AbstractGameObjectLists<? extends Quads>> OBJECTS = new CSArray<>(CS.COLDSTEEL.NUMBER_LAYERS , 1);
	//publicly accessable python interpreter
	private static final EnginePython INTERNAL_ENGINE_PYTHON = new EnginePython();
	//scripting singletons, static so that anything that wants to have a script does not need a reference to the engine
	public static EntityScriptingInterface ENTITY_SCRIPTING_INTERFACE;
	public static UIScriptingInterface UI_SCRIPTING_INTERFACE;
	public static ItemScriptingInterface ITEM_SCRIPTING_INTERFACE;
	public static TriggerScriptingInterface TRIGGER_SCRIPTING_INTERFACE;
	public static ProjectileScriptingInterface PROJECTILE_SCRIPTING_INTERFACE;	
	//ui structs
	private static NkContext NuklearContext;
	private static NkBuffer NuklearDrawCommands;
	//ui memory, allocated once and freed once
	private static final long UI_ALLOCATOR_MEMORY = nmemAlloc(CS.COLDSTEEL.UI_MEMORY_SIZE_KILOS * 1024);
	private static final MemoryStack UI_ALLOCATOR = MemoryStack.ncreate(UI_ALLOCATOR_MEMORY , CS.COLDSTEEL.UI_MEMORY_SIZE_KILOS * 1024);
	//utilities
	public static final PyType PYFUNCTION_PYTYPE = INTERNAL_ENGINE_PYTHON.get("functionForGettingType").getType();
	public static Supplier<int[]> windowDims;
		
	public static final void addGameObjectList(AbstractGameObjectLists<? extends Quads> list) {
		
		OBJECTS.addAt(list.renderOrder(), list);
		
	}
	
	public static final void removeGameObjectList(AbstractGameObjectLists<? extends Quads> list) {
		
		OBJECTS.remove(list.renderOrder());
		
	}
	
	public static final void forEachObjectList(Consumer<AbstractGameObjectLists<? extends Quads>> function) {
		
		OBJECTS.forEach(function);
		
	}
	
	public static final void removeAllGameObjectLists() {
		
		OBJECTS.clear();
		
	}

	Editor editor;
	GameRuntime gameRuntime;
	
	Renderer renderer;
	private Levels currentLevel;
	private MacroLevels currentMacroLevel;
	private GLFWWindow window;
	private NkInitialize guiInitializer;
	private Console engineConsole;
	
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
		
		window = new GLFWWindow();	
		
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
		
    	window.intialize(this , STATE);
    	
    	if(COLDSTEEL.DEBUG_CHECKS) GL.create();
    	GL.createCapabilities();
    	
    	guiInitializer = new NkInitialize(window.glfwWindow);
    	guiInitializer.initNKGUI();
    	NuklearContext = guiInitializer.getContext(); 
    	NuklearDrawCommands = guiInitializer.getCommands();
    	
    	renderer.initialize(window); 
    	
        // disable v-sync while loading
        glfwSwapInterval(0);
        
    	window.setNuklearContext(NuklearContext);   	        	
    	
        try(MemoryStack stack = stackPush()){

        	glfwGetFramebufferSize(window.glfwWindow , window.winWidth , window.winHeight);
        	glViewport(0, 0, window.winWidth.get(0), window.winHeight.get(0));

        }
        
        // Make the window visible
		glfwShowWindow(window.glfwWindow);
		System.out.println("Window initialization complete.");
		windowDims = () -> window.getWindowDimensions();
		
		switch(STATE) {
			
			case EDITOR:
				
	    		editor = new Editor(window);
	    		editor.initialize(renderer , scene, currentLevel , onLevelLoad , (targetState) -> switchState(targetState));
	    		
				break;
				
			case GAME:
				
				gameRuntime = new GameRuntime(scene);
				gameRuntime.initialize();
								
				break;
				
			default:
	
				System.out.println("Fatal Error in initialization, closing program");
				closeOverride();
				break;
	
		}
		
		ENTITY_SCRIPTING_INTERFACE = new EntityScriptingInterface(renderer, scene , window, engineConsole);
		UI_SCRIPTING_INTERFACE = new UIScriptingInterface(engineConsole);
		ITEM_SCRIPTING_INTERFACE = new ItemScriptingInterface(engineConsole , window , renderer , scene.entities());
		TRIGGER_SCRIPTING_INTERFACE = new TriggerScriptingInterface(scene , currentLevel);
		PROJECTILE_SCRIPTING_INTERFACE = new ProjectileScriptingInterface(scene);
		
		System.gc();
		
	}
	
	/**
	 * Designed to be a safe way to switch runtime state.
	 * 
	 */
	private void reinitialize() {

		switch(STATE) {
			
			case EDITOR:

	    		editor = new Editor(window);
	    		editor.initialize(renderer , scene, currentLevel , onLevelLoad , (targetState) -> switchState(targetState));
	    		
				break;
				
			case GAME:
				
				scene.clear();
				gameRuntime = new GameRuntime(scene);
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

        glClearColor(window.R, window.G, window.B, window.a);
        glfwSwapInterval(1);
        System.out.println("Window running.");
        //This is the main loop.
        while (!glfwWindowShouldClose(window.getGlfwWindow())) {
        	        	
	    	nk_input_begin(NuklearContext);
	        glfwPollEvents();
	        nk_input_end(NuklearContext);
	        
	        DialogUtils.layoutDialogs();
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
	        		
	        		editor.run();
	        		if(!nk_window_is_any_hovered(NuklearContext) && window.isLMousePressed() && !window.isLShiftPressed()) {
	        			
	        			glfwGetCursorPos(window.glfwWindow , window.newX , window.newY);
	        			int[] windowDims = window.getWindowDimensions();
	        			window.newX.put(0 , getSCToWCForX(window.newX.get(0) , windowDims[0] , windowDims[1] , renderer.getCamera()));
	        			window.newY.put(0 , getSCToWCForY(window.newY.get(0) , windowDims[0] , windowDims[1] , renderer.getCamera()));	        			
	        			editor.resizeSelectionArea((float)window.newX.get(0), (float)window.newY.get(0));
	        				        			
	        		}
	        			        		
	        		break;

	        	case GAME:
	        		
	        		gameRuntime.run(this);
	        		renderer.run();
	        		
	        		break;

        	}
        	
            glfwSwapBuffers(window.glfwWindow);
            framesThisSecond++;
            millisThisFrame += frameTimer.getElapsedTimeMillis();

        }

    }
		
	void shutDown() {
    	
		guiInitializer.shutDown();
		renderer.shutDown();	        
		INTERNAL_ENGINE_PYTHON.shutDown();
		guiInitializer.shutDownAllocator();
		SoundEngine.shutDown();
		window.shutDown();
		ENTITY_SCRIPTING_INTERFACE.shutDown();
		DialogUtils.shutDownDialogs();
		nmemFree(UI_ALLOCATOR_MEMORY);
		Console.shutDown();
		
    	switch(STATE) {
    	
    	case EDITOR:
    	
    		editor.shutDown();
    		
    		break;
    	
    	case GAME:
    		
    		
    		break;
    		
    	}
    	
    	System.out.println("Program shut down, until next time...");    	
    	
    }
	
	/**
	 * 
	 * Designed to be a way to free resources only relevent to the current game state without stopping the entire program.
	 * If for example this is called during the Game runtime, GameRuntime resources will be freed and a new state can be initialized.
	 * 
	 */
	void preShutDown() {

		switch(STATE) {

	    	case EDITOR:

	    		editor.shutDown();

	    		break;

	    	case GAME:

	    		break;

    	}

	}

	public void closeOverride() {
		
		window.overrideCloseWindow();
		
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
		
		preShutDown();
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
	public void levelUpdate() {
		
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
			
			loadLevelFromLoadDoor(door);
			
		} else if (door == null) player.previouslyUsedLoadDoor(null);
		
	}
	
	/**
	 * 
	 * Takes the user to the level linked to this load door.
	 * 
	 * @param loadDoor
	 */
	public void loadLevelFromLoadDoor(LevelLoadDoors loadDoor) {
				
		GameRuntime.setState(GameState.BUSY);
		fadeToBlack(100d);
		TemporalExecutor.onElapseOf(100d , () -> {
			
			PlayerCharacter player = gameRuntime.player();
			loadClearDeploy(data + "macrolevels/" + loadDoor.linkedLevel());
			//this sets the player used load door to the load door linked to the one they just walked through
			player.previouslyUsedLoadDoor(currentLevel.getLoadDoorByName(loadDoor.linkedLoadDoorName()));
			player.moveTo(player.previouslyUsedLoadDoor().getConditionArea().getMidpoint());
			scene.entities().addStraightIn(player.playersEntity());
			player.playersEntity().LID(0);
			GameRuntime.setState(GameState.GAME_RUNTIME);
			fadeIn(100d);
			
		});
		
	}
	
	public void loadSave(String selectedSave) {
		
		fadeToBlack(2400);
		GameRuntime.setState(GameState.BUSY);
		
		TemporalExecutor.onElapseOf(2400d , () -> {
			
			String filepath = root + selectedSave;			
			
			try (BufferedReader reader = Files.newBufferedReader(Paths.get(filepath))){
				
				PlayerCharacter player = gameRuntime.player();
				
				if(player == null) player = new PlayerCharacter();
				player.load(reader);
				player.nextSave(Character.getNumericValue(filepath.length() - 6));
				player.playersEntity().LID(0);
				
				CSTFParser cstf = new CSTFParser(reader);
				String playerLevel = cstf.rlabel("location");
				
				loadClearDeploy(data + "macrolevels/" + playerLevel + ".CStf");
				float[] playersPosition = new float[2];
				cstf.rlabel("position" , playersPosition);
				
				player.moveTo(playersPosition);
				scene.entities().addStraightIn(player.playersEntity());
	
				GameRuntime.setState(GameState.GAME_RUNTIME);
				fadeIn(1000);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
		});
		
	}

	private void buildOSTIntro(CSLinked<Sounds> introSegments) {
		
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
	
	private void buildOSTLoop(CSLinked<Sounds> loopSegments) {
		
		cdNode<Sounds> iter = loopSegments.get(0);
		iter.val.play();
		if (loopSegments.size() > 1) {

			iter = iter.next;
			for(int i = 1 ; i < loopSegments.size() - 1; i ++ , iter = iter.next) {
				
				cdNode<Sounds> node = iter;
				TemporalExecutor.onTrue(() -> node.prev.val.stopped() , () -> node.val.play());
				
			}
			
			cdNode<Sounds> node = iter;
			TemporalExecutor.onTrue(() -> node.prev.val.stopped() , () -> buildOSTLoop(loopSegments));
			
		} else {
			
			cdNode<Sounds> node = iter;
			TemporalExecutor.onTrue(() -> node.val.stopped() , () -> buildOSTLoop(loopSegments));
		
		}
		
	}
	
	/**
	 * Loads the level at the given absolute file path, clears the scene, and deploys the new level. This should be called
	 * <b> any </b> time a level should be loaded. 
	 * 
	 * @param filepath � filepath to the level
	 */
	public void loadClearDeploy(String filepath) {
				
		Levels newLevel = new Levels((CharSequence)(filepath));
		MacroLevels macroLevel = newLevel.associatedMacroLevel();
		
		if(currentMacroLevel == null) {
			
			//temporary holder of sounds loaded
			CSLinked<Sounds> introSegments = new CSLinked<>();			
			macroLevel.forEachIntroSegment(x -> introSegments.add(SoundEngine.add(assets + "sounds/" + x)));
		
			CSLinked<Sounds> loopSegments = new CSLinked<>();			
			macroLevel.forEachLoopSegment(x -> loopSegments.add(SoundEngine.add(assets + "sounds/" + x)));
			
			if(introSegments.size() > 0) buildOSTIntro(introSegments);
			
			if(loopSegments.size() > 0) {			
			
				if(introSegments.size() > 0) 
					TemporalExecutor.onTrue(() -> introSegments.get(introSegments.size() - 1).val.stopped() , () -> buildOSTLoop(loopSegments));
				else buildOSTLoop(loopSegments);
				
			}
			
		} else if(currentMacroLevel != null && !macroLevel.equals(currentMacroLevel)) {
			
			Renderer.freeMacroLevel(currentMacroLevel);
			SoundEngine.freeMacroLevel(currentMacroLevel);

			//temporary holder of sounds loaded
			CSLinked<Sounds> introSegments = new CSLinked<>();			
			macroLevel.forEachIntroSegment(x -> introSegments.add(SoundEngine.add(assets + "sounds/" + x)));
		
			CSLinked<Sounds> loopSegments = new CSLinked<>();			
			macroLevel.forEachLoopSegment(x -> loopSegments.add(SoundEngine.add(assets + "sounds/" + x)));
			
			if(introSegments.size() > 0) buildOSTIntro(introSegments);
			
			if(loopSegments.size() > 0) {			
			
				if(introSegments.size() > 0) 
					TemporalExecutor.onTrue(() -> introSegments.get(introSegments.size() - 1).val.stopped() , () -> buildOSTLoop(loopSegments));
				else buildOSTLoop(loopSegments);
				
			}
		
		}
		
		currentMacroLevel = macroLevel;
		
		scene.clear();
		newLevel.deploy(scene);
		
		currentLevel = newLevel;
		onLevelLoad.accept(newLevel);
		TRIGGER_SCRIPTING_INTERFACE.onLevelLoad.accept(newLevel);
		
		
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
	
	void e_returnConsole() {
		
		if(STATE != RuntimeState.EDITOR) return;
		editor.returnConsole();
		
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

    public static NkContext NuklearContext() {
    	
    	return NuklearContext;
    	
    }

    public static NkBuffer NuklearDrawCommands() {
    	
    	return NuklearDrawCommands;
    	
    }
    
    public static MemoryStack UIAllocator() {
    	
    	return UI_ALLOCATOR;
    	
    }
    
    public static EnginePython INTERNAL_ENGINE_PYTHON() {
    	
    	return INTERNAL_ENGINE_PYTHON;
    	
    }
    
}