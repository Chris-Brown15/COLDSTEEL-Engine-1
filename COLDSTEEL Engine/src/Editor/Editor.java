package Editor;

import static CS.COLDSTEEL.assets;
import static CS.COLDSTEEL.data;
import static CSUtil.BigMixin.changeColorTo;
import static CSUtil.BigMixin.toBool;
import static CSUtil.BigMixin.toNamePath;
import static Physics.MExpression.toNumber;
import static Renderer.Renderer.loadTexture;
import static org.lwjgl.system.MemoryUtil.memReport;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryUtil.MemoryAllocationReport;

import Audio.SoundEngine;
import Audio.Sounds;
import CS.Engine;
import CS.RuntimeState;
import CS.UserInterface;
import CSUtil.RefInt;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSQueue;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import Core.Console;
import Core.ECS;
import Core.Executor;
import Core.GameFiles;
import Core.HitBoxSets;
import Core.Quads;
import Core.Scene;
import Core.SpriteSets;
import Core.TemporalExecutor;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;
import Core.Entities.EntityFlags;
import Core.Entities.EntityHitBoxes;
import Core.Entities.EntityLists;
import Core.Entities.EntityScripts;
import Core.Statics.Statics;
import Core.TileSets.TileSets;
import Core.TileSets.Tiles;
import Game.Items.Inventories;
import Game.Items.ItemComponents;
import Game.Items.Items;
import Game.Levels.Levels;
import Game.Levels.MacroLevels;
import Game.Levels.Triggers;
import Physics.Colliders;
import Physics.Joints;
import Renderer.Renderer;
import Renderer.Camera;

public class Editor {

	private EditorMode mode = EditorMode.BUILD_MODE;
	private EditorState editorState = EditorState.GENERIC;
	private CursorState cursorState = CursorState.SELECTABLE;

	private ReentrantLock lock = new ReentrantLock();
	private final CSQueue<Consumer<Editor>> events = new CSQueue<>();
	
	Scene scene;
	Console console;
	UI_AAAManager uiManager;
	private Engine engine;
	// camera move speed
	float moveSpeed = 0;
	
	boolean showPyUI = true;
	SelectionArea selection;
	Levels backupLevel = new Levels(scene , "Editor Backup");
	// reference to any instance of quads that was selected
	Quads activeQuad = null;
	boolean background = true;
	// whether the active object should follow the cursor or not, toggled each left click
	boolean spawnAtCursor = false;

	CSArray<Joints> jointMarkers = new CSArray<Joints>(50);
	Joints activeJoint;
	boolean renderJoints = true;

	// Object used to mark up hitboxes with a quad. The active quad will be the quad referenced for the math relating to the hitboxset
	Levels currentLevel;
	MacroLevels currentMacroLevel;
	Triggers currentTrigger;
	Quads currentTriggerBound;
	
	public void initialize(Engine engine , Levels currentLevel , Console console) {

		this.engine = engine;
		uiManager = new UI_AAAManager(this);
		System.out.println("Beginning Editor initialization...");
		this.console = console;
		this.selection = new SelectionArea();
		this.scene = new Scene(engine);
		
		this.currentLevel = currentLevel;
		backupLevel.associate(data + "macrolevels/Editor/");
		
		System.out.println("Editor initialization complete.");

	}

	public void switchTo(EditorMode mode) {

		if (this.mode == mode) return;
				
		if (mode == EditorMode.BUILD_MODE) {// switching into build mode
			
//			editorUI.resetUIFirstTimeVariables();TODO
			backupLevel.deploy(scene);

		} else if (mode == EditorMode.TEST_MODE || mode == EditorMode.HYBRID_MODE) { // switching into test or hybrid mode

			backupLevel.clear();
			scene.entities().forOnly(x -> x.has(ECS.SCRIPT), x -> {

				EntityScripts script = (EntityScripts) x.components()[Entities.SOFF];
				if(script != null) script.recompile();

			});

			scene.entities().forOnly(x -> x.has(ECS.INVENTORY), x -> {

				Inventories inv = (Inventories) x.components()[Entities.IOFF];
				inv.getItems().forEachVal(tuple -> {

					if(tuple.getFirst().has(ItemComponents.USABLE)) tuple.getFirst().componentData().recompileUseScript();
					if(tuple.getFirst().has(ItemComponents.EQUIPPABLE)) tuple.getFirst().componentData().recompileOnEquipAndOnUnequipScripts();

				});

			});

			scene.items().forOnly(x -> x.has(ItemComponents.USABLE), x -> x.componentData().recompileUseScript());
			backupLevel.snapShotScene(scene);

			setState(EditorState.GENERIC);

		}

		this.mode = mode;

	}

	public void run(Engine engine) {

		// render collision bounds, hitboxes, and joints for all entities in the scene if render debug is on

		Renderer.draw_foreground(selection.vertices);
		
		if (toBool(uiManager.tilesetEditor.renderTileSheet)) {

			if(scene.tiles1().getTileSheet() != null) Renderer.draw_background(scene.tiles1().getTileSheet());
			if(scene.tiles2().getTileSheet() != null) Renderer.draw_background(scene.tiles2().getTileSheet());

		}
		
		jointMarkers.forEach(Renderer::draw_foreground);
		hitboxMarker.hitboxes().forEach(Renderer::draw_foreground);
		
		handleEvents();
		
		switch (mode) {

		case BUILD_MODE:

			uiManager.layoutElementsBuildMode();
			scene.entities().resetScriptCount();

			if (!(Engine.keyboardPressed(GLFW_KEY_LEFT_SHIFT) || Engine.keyboardPressed(GLFW_KEY_LEFT_CONTROL))) {

				if(Engine.keyboardPressed(GLFW_KEY_UP)) engine.getCamera().moveCamera(scene, 0, moveSpeed);
				if(Engine.keyboardPressed(GLFW_KEY_LEFT)) engine.getCamera().moveCamera(scene, -moveSpeed, 0);
				if(Engine.keyboardPressed(GLFW_KEY_RIGHT)) engine.getCamera().moveCamera(scene, moveSpeed, 0);	
				if(Engine.keyboardPressed(GLFW_KEY_DOWN)) engine.getCamera().moveCamera(scene, 0, -moveSpeed);

			}

			scene.kinematics().process();
			TemporalExecutor.process();
			scene.tiles1().animateTiles();
			scene.tiles2().animateTiles();
			if(editorState == EditorState.EDITING_HITBOX && Engine.mousePressed(GLFW_MOUSE_BUTTON_LEFT)) dragHitBoxMarker(engine.cursorWorldCoords());
			engine.releaseKeys();
			dragQuad();

			break;

		case TEST_MODE:

			uiManager.layoutElementsTestMode();
			scene.entities().resetScriptCount();
			scene.tiles1().animateTiles();
			scene.tiles2().animateTiles();
			scene.entities().entitySystems(() -> {

			}, () -> {

				scene.kinematics().process();
				TemporalExecutor.process();

			}, () -> {

				engine.releaseKeys();
				
			});

			if (currentLevel != null) {

				currentLevel.runScripts();
				
			}

			break;

		case HYBRID_MODE:

			uiManager.layoutElementsBuildMode();

			if (!(Engine.keyboardPressed(GLFW_KEY_LEFT_SHIFT) || Engine.keyboardPressed(GLFW_KEY_LEFT_CONTROL))) {

				if(Engine.keyboardPressed(GLFW_KEY_UP)) engine.getCamera().moveCamera(scene, 0, moveSpeed);
				if(Engine.keyboardPressed(GLFW_KEY_LEFT)) engine.getCamera().moveCamera(scene, -moveSpeed, 0);
				if(Engine.keyboardPressed(GLFW_KEY_RIGHT)) engine.getCamera().moveCamera(scene, moveSpeed, 0);
				if(Engine.keyboardPressed(GLFW_KEY_DOWN)) engine.getCamera().moveCamera(scene, 0, -moveSpeed);

			}


			scene.kinematics().process();
			scene.entities().resetScriptCount();
			scene.tiles1().animateTiles();
			scene.tiles2().animateTiles();
			scene.entities().entitySystems(() -> {

			}, () -> {

				// run kinematics and executor
				scene.kinematics().process();
				TemporalExecutor.process();

			}, () -> {

				// handle input
				engine.releaseKeys();

			});

			if (currentLevel != null) currentLevel.runScripts();
			break;

		}

	}

	/**
	 * We can drag a quad with the cursor if the cursor state allows dragging, and
	 * we have pressed the mouse button to begin dragging, and the editor state
	 * allows dragging.
	 * 
	 */
	private void dragQuad() {

		float[] cursor = engine.cursorWorldCoords();
		if(cursorState == CursorState.DRAGGING && activeQuad != null) {
			
//			System.out.println("cursorCoords: ");
			if (editorState == EditorState.EDITING_JOINT) activeJoint.moveTo(cursor[0] , cursor[1]);
			else activeQuad.moveTo(cursor[0] , cursor[1]);			
			
		}
			
	}

	/**
	 * Activates a quad on clicking and if a quad is under the cursor. Switches
	 * based on the state of the cursor (which is basically the state of the user)
	 * 
	 */
	public boolean selectQuad(float x, float y) {

		Quads clicked = null;
		switch (editorState) {

			case EDITING_LEVEL -> {
	
				if (currentLevel != null) {
	
					clicked = currentLevel.selectLoadDoors(x, y);
					
					if (clicked != null) {
	
						boolean reselected = activeQuad == clicked;
						activeQuad = clicked;
						currentTriggerBound = activeQuad;
						return reselected;
	
					}
					
					clicked = currentLevel.selectTriggerAreas(x, y);
					if(clicked != null) { 
	
						boolean reselected = activeQuad == clicked;
						activeQuad = clicked;
						currentTriggerBound = activeQuad;
						return reselected;
	
					}
	
				}
	
			}
	
			case EDITING_STATIC -> {
	
				if (activeQuad == null) return false;
	
				if (((Statics)activeQuad).collidersFocused()) return ((Statics)activeQuad).apply((colliders) -> {
	
					for (int i = 0; i < colliders.size(); i++) if (colliders.get(i).selectOwnedCollider(x, y) != -1) {
	
							Colliders clickedCollider = colliders.get(i);
							boolean selectedSame = clickedCollider == activeQuad;
							activeQuad = clickedCollider;
							return selectedSame;
	
					}
	
					return false;
	
				});
	
				return false;
	
			}
	
			case EDITING_HITBOX -> {
	
				if (!toBool(uiManager.hitboxEditor.sliderSelect)) return false;
	
				for (int i = 0; i < hitboxMarker.getSize(); i++) if (CSUtil.BigMixin.selectQuad(hitboxMarker.hitboxes().get(i), x, y)) {
	
					hitboxMarker.active = i;
					return false;
	
				}
	
			}
	
			case EDITING_JOINT -> {
	
				for (int i = 0; i < jointMarkers.size(); i++) if (jointMarkers.get(i) != null && jointMarkers.get(i).selectQuad(x, y) != -1) {
	
					boolean selectedSame = activeJoint != null && activeJoint.getID() == jointMarkers.get(i).getID();
					activeJoint = jointMarkers.get(i);
					return selectedSame;
	
				}
	
			}
			
			case EDITING_TILESET -> {
	
				TileSets ts;
				if ((ts = background ? scene.tiles1() : scene.tiles2()) != null) {
	
					clicked = ts.select(x, y);
	
					if (clicked != null) {
	
						boolean selectedSame = activeQuad == clicked;
						activeQuad = clicked;
						return selectedSame;
	
					}
	
				}
	
			}
	
			default -> {
					
				clicked = scene.colliders().selectCollider(x, y);
				
				if (clicked != null) {
					
	
					boolean selectedSame = activeQuad == clicked;
					activeQuad = clicked;
					return selectedSame;
	
				}
	
				clicked = scene.statics2().selectStatic(x, y);
	
				if (clicked != null) {
	
					boolean selectedSame = activeQuad == clicked;
					activeQuad = clicked;
					return selectedSame;
	
				}
	
				clicked = scene.quads2().selectQuad(x, y);
	
				if (clicked != null) {
	
					boolean selectedSame = activeQuad == clicked;
					activeQuad = clicked;
					return selectedSame;
	
				}
	
				clicked = scene.entities().selectEntity(x, y);
	
				if (clicked != null) {
	
					boolean selectedSame = activeQuad == clicked;
					activeQuad = clicked;
					return selectedSame;
	
				}
	
				clicked = scene.items().selectItems(x, y);
	
				if (clicked != null) {
	
					boolean selectedSame = activeQuad == clicked;
					activeQuad = clicked;
					return selectedSame;
	
				}
	
				clicked = scene.statics1().selectStatic(x, y);
	
				if (clicked != null) {
	
					boolean selectedSame = activeQuad == clicked;
					activeQuad = clicked;
					return selectedSame;
	
				}
	
				clicked = scene.quads1().selectQuad(x, y);
	
				if (clicked != null) {
	
					boolean selectedSame = activeQuad == clicked;
					activeQuad = clicked;
					return selectedSame;
	
				}
	
				activeQuad = null;
	
			}

		}

		return false;

	}

	Camera camera() {
		
		return engine.getCamera();
		
	}
	
	public Scene scene() {
		
		return scene;
		
	}
	
	public float[] cursorWorldCoords() {
		
		return engine.cursorWorldCoords();
		
	}
	
	public Joints getJoint(int index) {

		return jointMarkers.get(index);

	}

	public void removeJoint(int index) {

		jointMarkers.remove(index);

	}

	public void removeAllJoints() {

		jointMarkers.clear();
		activeJoint = null;

	}

	public void loadJoints() {

		if (!(activeQuad instanceof Entities))
			return;

		Entities E = (Entities) activeQuad;
		EntityAnimations anims = (EntityAnimations) E.components()[Entities.AOFF];
		if (!anims.hasSpriteSet(activeSpriteSet)) return;

		float[] selectedSprite = activeSpriteSet.getSprite(activeSpriteID);
		if (selectedSprite.length < 6) return;
		removeAllJoints();
		float[] activeData = E.getData();
		int iterations = selectedSprite.length % 3 == 0 ? selectedSprite.length : selectedSprite.length - 1;
		for (int i = 6; i < iterations; i += 3) {

			Joints newJoint = new Joints();
			newJoint.setID((int) selectedSprite[i]);
			newJoint.moveTo(selectedSprite[i + 1] - activeData[9], selectedSprite[i + 2] - activeData[10]);
			jointMarkers.add(newJoint);
			System.out.println("Added Joint " + newJoint.getID() + " at x: " + newJoint.getMidpoint()[0] + ", y: " + newJoint.getMidpoint()[1]);

		}

	}

	public void translateActiveJoint(float x, float y) {

		if (activeJoint != null)
			activeJoint.translate(x, y);

	}

	public Quads activeQuad() {

		return activeQuad;

	}

	public void resizeSelectionArea(float width, float height) {

		selection.resize(width, height);

	}

	public void moveSelectionAreaTo(float x, float y) {

		selection.moveTo(x, y);

	}

	public void setSelectionAreaDimensions(float width, float height) {

		selection.setDimensions(width, height);

	}

	public void moveSelectionAreaRightFace(float amount) {

		selection.moveRightFace(amount);

	}

	public void moveSelectionAreaLeftFace(float amount) {

		selection.moveLeftFace(amount);

	}

	public void moveSelectionAreaUpperFace(float amount) {

		selection.moveUpperFace(amount);

	}

	public void moveSelectionAreaLowerFace(float amount) {

		selection.moveLowerFace(amount);

	}

	public EditorMode getState() {

		return mode;

	}

	void toggleRenderDebug() {
		
		engine.toggleRenderDebug(backupLevel);
		
	}
	
	public Quads addQuad() {

		Quads added;
		if (background) added = scene.quads1().add();
		else added = scene.quads2().add();

		if (spawnAtCursor) {

			float[] cursor = engine.cursorWorldCoords();
			added.moveTo(cursor[0], cursor[1]);

		}

		return added;

	}

	public Quads removeActive() {

		if (activeQuad == null)
			return null;

		if (activeQuad instanceof Colliders) {

			scene.colliders().delete((Colliders) activeQuad);

		} else if (activeQuad instanceof Statics) {

			Statics activeStatic = (Statics) activeQuad;
			scene.statics1().remove(activeStatic);
			scene.statics2().remove(activeStatic);

		} else if (activeQuad instanceof Entities) {

			scene.entities().remove((Entities) activeQuad);

		} else if (activeQuad instanceof Quads) {

			scene.quads1().delete(activeQuad);
			scene.quads1().delete(activeQuad);

		} else if (activeQuad instanceof Items) {

			scene.items().remove((Items) activeQuad);

		}

		Quads removed = activeQuad;
		activeQuad = null;
		return removed;

	}

	@SuppressWarnings("rawtypes") public void deleteActive() {

		if (activeQuad == null || !(activeQuad instanceof GameFiles)) return;
		((GameFiles) removeActive()).delete();

	}

	public void textureActive() {

		if (activeQuad == null) return;
		Supplier<String> filepath = DialogUtils.newFileExplorer("Select Texture" , 5 , 270 , false , false , assets + "entities/");
		TemporalExecutor.onTrue(() -> filepath.get() != null, () -> {

			loadTexture(activeQuad.getTexture() , filepath.get());
			if (activeQuad instanceof Statics) activeQuad.fitQuadToTexture();

		});

	}

	public void removeActiveTexture() {

		activeQuad.setTexture(null);

	}

	public void removeActiveColor(float r, float g, float b) {

		if (activeQuad != null) activeQuad.removeColor(r, g, b);

	}

	public void filterActiveColor(float r, float g, float b) {

		if (activeQuad != null) activeQuad.setFilter(r, g, b);

	}

	public void toggleSpawnAtCursor() {

		spawnAtCursor = spawnAtCursor ? false : true;

	}

	void resetDimensionsAndUVs() {

		activeQuad.fitQuadToTexture();
		activeQuad.setLeftUCoords(0f);
		activeQuad.setRightUCoords(1f);
		activeQuad.setTopVCoords(1f);
		activeQuad.setBottomVCoords(0f);

	}

	void moveActiveForward() {

		if (activeQuad == null)
			return;

		if (activeQuad.getClass() == Quads.class) {

			if (scene.quads1().has(activeQuad)) scene.quads1().forward(activeQuad.getID());
			else if (scene.quads2().has(activeQuad)) scene.quads2().forward(activeQuad.getID());

		} else if (activeQuad.getClass() == Statics.class) {

			if (scene.statics1().has((Statics) activeQuad)) scene.statics1().forward(activeQuad.getID());
			else if (scene.statics2().has((Statics) activeQuad)) scene.statics2().forward(activeQuad.getID());

		}

	}

	void moveActiveToFront() {

		if (activeQuad == null) return;

		if (activeQuad.getClass() == Quads.class) {

			if (scene.quads1().has(activeQuad)) scene.quads1().toFront(activeQuad.getID());
			else if (scene.quads2().has(activeQuad)) scene.quads2().toFront(activeQuad.getID());

		} else if (activeQuad.getClass() == Statics.class) {

			if (scene.statics1().has((Statics) activeQuad)) scene.statics1().toFront(activeQuad.getID());
			else if (scene.statics2().has((Statics) activeQuad)) scene.statics2().toFront(activeQuad.getID());

		}

	}

	void moveActiveBackward() {

		if (activeQuad == null) return;

		if (activeQuad.getClass() == Quads.class) {

			if (scene.quads1().has(activeQuad)) scene.quads1().backward(activeQuad.getID());
			else if (scene.quads2().has(activeQuad)) scene.quads2().backward(activeQuad.getID());

		} else if (activeQuad.getClass() == Statics.class) {

			if (scene.statics1().has((Statics) activeQuad)) scene.statics1().backward(activeQuad.getID());
			else if (scene.statics2().has((Statics) activeQuad)) scene.statics2().backward(activeQuad.getID());

		}

	}

	void moveActiveToBack() {

		if (activeQuad == null) return;

		if (activeQuad.getClass() == Quads.class) {

			if (scene.quads1().has(activeQuad)) scene.quads1().toBack(activeQuad.getID());
			else if (scene.quads2().has(activeQuad)) scene.quads2().toBack(activeQuad.getID());

		} else if (activeQuad.getClass() == Statics.class) {

			if (scene.statics1().has((Statics) activeQuad)) scene.statics1().toBack(activeQuad.getID());
			else if (scene.statics2().has((Statics) activeQuad)) scene.statics2().toBack(activeQuad.getID());

		}

	}

	public void deleteScene() {

		scene.clear();

	}

	public void say(Object say) {

		console.sayln(say);

	}

	public void say(Object... say) {

		console.sayln(say);

	}

	public void addCollider() {

		Colliders newCollider = scene.colliders().add();
		if (spawnAtCursor) {

			float[] cursor = engine.cursorWorldCoords();
			newCollider.moveTo(cursor[0] , cursor[1]);

		}

	}

	public void addCollider(Colliders collider) {

		collider.setID(scene.colliders().size());
		scene.colliders().add(collider);

	}

	public void addStatic() {

		Supplier<String> input = DialogUtils.newInputBox("Input Static Name", 5, 270);
		TemporalExecutor.onTrue(() -> input.get() != null, () -> {

			Statics newStatic = background ? scene.statics1().newStatic(input.get()) : scene.statics2().newStatic(input.get());
			float[] cursor = engine.cursorWorldCoords();
			newStatic.moveTo(cursor[0] , cursor[1]);

		});

	}

	public void loadStatic() {

		Supplier<String> filepath = DialogUtils.newFileExplorer("Select one or more Statics", 5, 270, false, true);
		TemporalExecutor.onTrue(() -> filepath.get() != null, () -> {

			String[] split = filepath.get().split("\\|");
			Statics newStatic;
			float[] cursor = engine.cursorWorldCoords();
			for (String y : split) {

				if (background) newStatic = scene.statics1().loadStatic((String) toNamePath(y));
				else newStatic = scene.statics1().loadStatic(((String) toNamePath(y)));
				if (spawnAtCursor) newStatic.moveTo(cursor[0] , cursor[1]);
			}

		});

	}

	public void addEntity() {

		scene.entities().newEntity();
		int size = scene.entities().size();
		if (spawnAtCursor) {

			TemporalExecutor.onTrue(() -> size < scene.entities().size(), () -> {

				float[] cursor = engine.cursorWorldCoords();
				scene.entities().get(scene.entities().size() - 1).moveTo( cursor[0] , cursor[1]);

			});

		}

	}

	public void loadEntity() {

		Supplier<String> filepaths = DialogUtils.newFileExplorer("Select one or more Entities", 5, 270, false, true,
				data + "entities/");
		TemporalExecutor.onTrue(() -> filepaths.get() != null, () -> {

			Entities loaded;
			String[] split = filepaths.get().split("\\|");
			for (String y : split) {

				loaded = scene.entities().loadEntity(toNamePath(y));
				System.out.println(loaded.getTexture().imageInfo);
				if (spawnAtCursor) {

					float[] cursor = engine.cursorWorldCoords();
					loaded.moveTo(cursor[0] , cursor[1]);

				}

			}

		});

	}

	public void loadItem() {

		Supplier<String> filepaths = DialogUtils.newFileExplorer("Select one or more Items", 5, 270, false, true, data + "items/");
		TemporalExecutor.onTrue(() -> filepaths.get() != null, () -> {

			Items newItem;
			String[] split = filepaths.get().split("\\|");
			for (String y : split) {

				newItem = scene.items().load(toNamePath(y));
				if (spawnAtCursor) {

					float[] cursor = engine.cursorWorldCoords();
					newItem.moveTo(cursor[0] , cursor[1]);

				}

			}

		});

	}

	public void newItem() {

		Supplier<String> itemName = DialogUtils.newInputBox("Input Item Name", 5, 270);
		TemporalExecutor.onTrue(() -> itemName.get() != null, () -> scene.items().newItem(itemName.get()));			
	
	}

	public Joints addJoint() {

		Joints newJoint = new Joints();
		newJoint.setID(jointMarkers.size());
		jointMarkers.add(newJoint);
		return newJoint;

	}

	public void removeActiveJoint() {

		if (activeJoint == null) return;
		jointMarkers.remove(activeJoint.getID());
		activeJoint = null;

	}

	public void translateActive(float x, float y) {

		if (editorState == EditorState.EDITING_STATIC) ((Statics)activeQuad).translate(x, y);
		else if (editorState == EditorState.EDITING_JOINT) translateActiveJoint(x, y);
		else {

			if (activeQuad != null) activeQuad.translate(x, y);
			else if (activeJoint != null) activeJoint.translate(x, y);

		}

	}

	void deleteItems() {

		
		Supplier<String> loadPath = DialogUtils.newFileExplorer("Delete an Item", 5 , 270 , false , false);
		TemporalExecutor.onTrue(() -> loadPath.get() != null , () -> {
			
			Items deleteThis = new Items(scene , (String) CSUtil.BigMixin.toNamePath(loadPath.get()));
			deleteThis.delete();
			
		});

	}

	int numberObjects() {

		return scene.numberObjects();

	}

	/**
	 * Creates a new file under data/macrolevels with the given name. Macro Level
	 * files should contain a manifest file, which notates which assets are loaded
	 * at the initialization of a macro level.
	 * 
	 * @param name
	 */
	void createMacroLevel(String name) {

		currentMacroLevel = new MacroLevels(name);
		currentMacroLevel.initialize();		

	}

	MacroLevels loadMacroLevel(String filepath) {

		MacroLevels newMacro = new MacroLevels((String) CSUtil.BigMixin.toNamePath(filepath));
		newMacro.load(toNamePath(filepath));
		return newMacro;

	}

	public void loadLevel() {

		Supplier<String> filepath = DialogUtils.newFileExplorer("Load a Level", 5, 270, false, false,
				data + "macrolevels/");
		TemporalExecutor.onTrue(() -> filepath.get() != null, () -> {

			Levels newLevel = new Levels(scene , (CharSequence) filepath.get());
			newLevel.deploy(scene);
			setupTileSetUIImages(scene.tiles1() , background);
			setupTileSetUIImages(scene.tiles2() , !background);
			currentLevel = newLevel;
			say("Loaded level: " + newLevel.gameName());
			uiManager.loadDoorEditor.currentLoadDoor = null;
			currentTrigger = null;
			uiManager.loadDoorEditor.linkedLevel = null;

		});

	}

	public void loadClearDeploy(String levelPath) {

		Levels newLevel = new Levels(scene , (CharSequence) (CS.COLDSTEEL.data + "macrolevels\\" + levelPath));

		scene.clear();
		newLevel.deploy(scene);

		currentLevel = newLevel;
		currentLevel = newLevel;
		say("Loaded level: " + newLevel.gameName());
		uiManager.loadDoorEditor.currentLoadDoor = null;
		currentTrigger = null;
		uiManager.loadDoorEditor.linkedLevel = null;

	}

	public void setCurrentLevel(Levels newCurrentLevel) {

		if(currentLevel != null && !currentLevel.empty()) scene.clear();
		this.currentLevel = newCurrentLevel;

	}
	
	public void newLevel(String name) {

		if(currentLevel != null && !currentLevel.empty()) scene.clear();
		this.currentLevel = new Levels(scene , name);

	}

	public Entities forName(String name) {

		cdNode<Entities> iter = scene.entities().iter();
		for (int i = 0; i < scene.entities().size(); i++, iter = iter.next) if (iter.val.name().equals(name)) return iter.val;
		return null;

	}

	void setupTileSetUIImages(TileSets target , boolean background) {

		if(target.uninitialized()) return;
		
		NkImage tileSetSheet = background ? uiManager.tilesetEditor.backgroundTileSetSpriteSheet : uiManager.tilesetEditor.foregroundTileSetSpriteSheet;
		
		UserInterface.image(target.textureInfo().path(), tileSetSheet);

		Quads tileSheet = new Quads(-1);
		tileSheet.setTexture(target.texture());
		tileSheet.fitQuadToTexture();
		target.setTileSheet(tileSheet);
		float[] removed = target.removed();
		tileSheet.removeColor(removed[0], removed[1], removed[2]);
		// generate a subimage for each tile for the UI
		float tileSheetWidth = tileSheet.getWidth(), tileSheetHeight = tileSheet.getHeight();
		target.forEachSource(x -> {

			float[] tileSpecs = x.tileData();
			short leftX = (short) (tileSheetWidth * tileSpecs[0]);
			short topY = (short) (tileSheetHeight - (tileSheetHeight * tileSpecs[2]));

			Tuple2<NkImage, NkRect> subRegionResult = UserInterface.subRegion(tileSetSheet , target.textureInfo() , leftX , topY , 
																	(short) tileSpecs[4], (short) tileSpecs[5]);
			
			CSLinked<NkImage> tileIcons = background ? uiManager.tilesetEditor.backgroundTileIcons : uiManager.tilesetEditor.foregroundTileIcons;
			tileIcons.add(subRegionResult.getFirst());

		});

	}

	private TileSets newTileSet(String name, String filepath) {

		TileSets target = background ? scene.tiles1() : scene.tiles2();
		target.clear();
		target.name(name);
		Quads tileSheet = new Quads(-1);
		target.texture(filepath);
		tileSheet.setTexture(target.texture());
		tileSheet.fitQuadToTexture();
		target.setTileSheet(tileSheet);
		tileSheet.roundVertices();

		return target;

	}

	/**
	 * Creates and returns a tile given a tile in the current tile set.
	 * 
	 * @return
	 */
	public Tiles copyTile(Tiles copy) {

		Tiles copied = (background ? scene.tiles1() : scene.tiles2()).copy(copy);
		float[] cursorPos = engine.cursorWorldCoords();
		copied.moveTo(cursorPos[0] , cursorPos[1]);
		return copied;

	}

	public TileSets loadTileSet(String filepath) {

		TileSets target = background ? scene.tiles1() : scene.tiles2();
		NkImage tileSetSheet = background ? uiManager.tilesetEditor.backgroundTileSetSpriteSheet : uiManager.tilesetEditor.foregroundTileSetSpriteSheet;
		
		if (!target.uninitialized()) {

			// if another tile set was already loaded and in use:
			target.clear();
			CSLinked<NkImage> tileIcons = background ? uiManager.tilesetEditor.backgroundTileIcons : uiManager.tilesetEditor.foregroundTileIcons;
			tileIcons.clear();

		}

		target.load(toNamePath(filepath));
		UserInterface.image(target.textureInfo().path(), tileSetSheet);

		Quads tileSheet = new Quads(-1);
		tileSheet.setTexture(target.texture());
		tileSheet.fitQuadToTexture();
		target.setTileSheet(tileSheet);
		float[] removed = target.removed();
		tileSheet.removeColor(removed[0], removed[1], removed[2]);
		// generate a subimage for each tile for the UI
		float tileSheetWidth = tileSheet.getWidth(), tileSheetHeight = tileSheet.getHeight();
		target.forEachSource(x -> {

			float[] tileSpecs = x.tileData();
			short leftX = (short) (tileSheetWidth * tileSpecs[0]);
			short topY = (short) (tileSheetHeight - (tileSheetHeight * tileSpecs[2]));

			Tuple2<NkImage, NkRect> subRegionResult = UserInterface.subRegion(tileSetSheet , target.textureInfo() , leftX , topY , 
																	(short) tileSpecs[4], (short) tileSpecs[5]);
			
			CSLinked<NkImage> tileIcons = background ? uiManager.tilesetEditor.backgroundTileIcons : uiManager.tilesetEditor.foregroundTileIcons;
			tileIcons.add(subRegionResult.getFirst());

		});

		return target;

	}

	public Consumer<Levels> onLevelLoad = newLevel -> currentLevel = newLevel;

	public void leaveEditor() {

		scene.clear();
		setState(EditorState.BUSY);
		engine.switchState(RuntimeState.GAME);

	}

	public void snapSelectionArea() {

		selection.roundVertices();

	}

	public void saveTriggerToScript(String triggerName) {

		File newFile = new File(CS.COLDSTEEL.data + "scripts/t_" + triggerName + ".py");
		try {

			newFile.createNewFile();
			if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(newFile);

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public void setState(EditorState state) {

		// freeze the cursor if necessary or unfreeze it if it previously was frozen
		if (!state.allowsSelecting) cursorState = CursorState.FROZEN;
		else if (!editorState.allowsSelecting && state.allowsSelecting) cursorState = CursorState.SELECTABLE;

		editorState = state;		
		if(editorState == EditorState.BUSY) uiManager.hideAllElements();

	}

	public void toggleCursorState() {

		// toggle state if something is selected
		if (activeQuad != null) {

			if (cursorState == CursorState.DRAGGING) cursorState = CursorState.SELECTABLE;
			else if (cursorState == CursorState.SELECTABLE) cursorState = CursorState.DRAGGING;

		}

	}

	public EditorState editorState() {

		return editorState;

	}

	public CursorState cursorState() {

		return cursorState;

	}

	public void cursorState(CursorState state) {

		cursorState = state;

	}

	void tryCatch(Executor code, String errorString) {

		try {

			code.execute();

		} catch (Exception e) {

			System.err.println(errorString);
			e.printStackTrace();

		}

	}
	
	void schedule(Consumer<Editor> callback) {
		
		lock.lock();
		events.enqueue(callback);
		lock.unlock();
		
	}
	
	private void handleEvents() {
		
		lock.lock();
		while(!events.empty())events.dequeue().accept(this);
		lock.unlock();
		
	}
	
	void dragHitBoxMarker(float[] coords) { 
		
		if(hitboxMarker.editingName != null && hitboxMarker.active != -1) {
			
			hitboxMarker.drag(hitboxMarker.active, coords[0], coords[1]);
			
		}
		
	}
	
	void createScriptFile( ) {

		Supplier<String> strGetter = DialogUtils.newInputBox("Script Name" , 5 , 270);
		
		TemporalExecutor.onTrue(
		() -> strGetter.get() != null , 
		() -> {
			
			File newFile = new File(CS.COLDSTEEL.data + "scripts/" + strGetter.get() + ".py");
			try {

				newFile.createNewFile();
				if(Desktop.isDesktopSupported()) Desktop.getDesktop().open(newFile);
				
			} catch (IOException e) {

				e.printStackTrace();
				
			}
			
		});
	
	}

	void loadScriptFile() {
				
		if(!Desktop.isDesktopSupported()) say("ERROR: Desktop class not supported on your platform");
		
		Supplier<String> filepath = DialogUtils.newFileExplorer("Select one or more script files" , 5 , 270 , false , true , data + "scripts/");
		TemporalExecutor.onTrue(() -> filepath.get() != null , () -> {
			
			String fp = filepath.get();
			try {
				
				if(fp.contains("|")) {//multiple files
					
					String[] splitFilePaths = fp.split("\\|");
					for(String split : splitFilePaths) {
						
						if(!split.endsWith(".py")) throw new IOException("Script files not selected");
						Desktop.getDesktop().open(new File(split));
						
					}
					
				} else {
					
					if(!fp.endsWith(".py")) throw new IOException("Script files not selected");
					Desktop.getDesktop().open(new File(fp));
					
				}
				
			} catch(IOException e) {
				
				say("ERROR: Invalid file/s selected");					
				e.printStackTrace();
				
			}			
			
		});
		
	}
	
	void setSelectionColorColor() {

		Supplier<float[]> colors = DialogUtils.newColorChooser("Set Selection Area to this color" , 5 , 270);
		TemporalExecutor.onTrue(() -> colors.get() != null , () -> changeColorTo(selection.vertices , colors.get()[0], colors.get()[1], colors.get()[2]));
	
	}
	
	void setSelectionColorOpacity() {
	
		Supplier<String> opacity = DialogUtils.newInputBox("Input Integer for Opacity Between 0 and 100", 5, 270, UserInterface.NUMBER_FILTER);
		TemporalExecutor.onTrue(() -> opacity.get() != null , () -> selection.makeTranslucent((float) toNumber(opacity.get()) / 100));
	
	}
	
	void setMoveSpeed(float speed) {
		
		moveSpeed = speed;
		
	}

	void moveCamera() {
	
		Supplier<String> xInput = DialogUtils.newInputBox("Input X Coordinate" , 5 , 270);
		Supplier<String> yInput = DialogUtils.newInputBox("Input X Coordinate" , 360 , 120);
		TemporalExecutor.onTrue(() -> xInput != null && yInput != null , () -> {
			
			engine.getCamera().lookAt((float)toNumber(xInput.get()) , (float)toNumber(yInput.get()));
			
		});
				
	}
	
	void newLevel() {

		Supplier<String> name = DialogUtils.newInputBox("Input Level Name" , 5 , 270);
		TemporalExecutor.onTrue(() -> name.get() != null, () -> newLevel(name.get()));
			
	}

	void renderLoadDoors() {

		currentLevel.forEachLoadDoor(loadDoor -> Renderer.draw_foreground(loadDoor.getConditionArea()));
		if (currentTrigger != null) { 
			
			currentTrigger.forEachConditionArea(Renderer::draw_foreground);
			currentTrigger.forEachEffectArea(Renderer::draw_foreground);
			
		}
		
	}
	
	void setLevelsMacroLevel() {
		
		Supplier<String> macroLevelPath = DialogUtils.newFileExplorer("Select a Macro Level archive" , 5 , 270 , false , true);
		TemporalExecutor.onTrue(() -> macroLevelPath.get() != null, () -> currentLevel.associate(macroLevelPath.get()));
			
	}
	
	void saveLevel() {
		
		currentLevel.snapShotScene(scene);
		
	}
	
	void addLoadDoor() {
				
		Supplier<String> newLoadDoorName = DialogUtils.newInputBox("Input Load Door Name", 5, 270);
		TemporalExecutor.onTrue(() -> newLoadDoorName.get() != null , () -> currentLevel.addLoadDoor(newLoadDoorName.get()));
			
	}
	
	void addTrigger() {
			
		Supplier<String> triggerName = DialogUtils.newInputBox("Trigger Name", 5, 270);
		TemporalExecutor.onTrue(() -> triggerName.get() != null , () -> {
			
			currentLevel.addTrigger(triggerName.get());			
			saveTriggerToScript(triggerName.get());
			currentTrigger = null;
			
		});
				
	}
	
	void setCurrentTrigger(Triggers someTrigger) {
		
		currentTrigger = someTrigger;
		
	}

	void renameActiveTrigger() {
		
		Supplier<String> input = DialogUtils.newInputBox("Input a Name for This Trigger" , 5 , 270);
		TemporalExecutor.onTrue(() -> input.get() != null, () -> currentTrigger.name(input.get()));
		
	}
	
	void removeActiveTrigger() {
		
		removeActive();
		currentLevel.removeTrigger(currentTrigger);
		currentTrigger = null;
		
	}
	
	void removeCurrentTrigger() {
		
		currentTrigger.remove(currentTriggerBound);
		currentTriggerBound = null;
		activeQuad = null;
		
	}
	
	void newMacroLevel() {
		
		Supplier<String> macroLevelName = DialogUtils.newInputBox("Input Macro Level Name", 5 , 270);
		TemporalExecutor.onTrue(() -> macroLevelName.get() != null , () -> {
			
			String macroLevelNameEdit = macroLevelName.get().replace(" ", "");
			if(currentMacroLevel != null) currentMacroLevel.write();
			createMacroLevel(macroLevelNameEdit);
			
		});
						
	}
		
	void loadMacroLevel() {
		
		Supplier<String> filepath = DialogUtils.newFileExplorer("Select a Macro Level" , 5 , 270 , true, false);
		TemporalExecutor.onTrue(() -> filepath.get() != null, () -> currentMacroLevel = new MacroLevels((CharSequence)toNamePath(filepath.get())));
		
	}
	
	void deleteMacroLevel() {  
		
		Supplier<String> filepath = DialogUtils.newFileExplorer("Select a Macro Level to delete", 5 , 270, true , false);
		TemporalExecutor.onTrue(() -> filepath.get() != null, () -> {
			
			MacroLevels newLevel = new MacroLevels((String) toNamePath(filepath.get()));
			newLevel.delete();
			if(currentMacroLevel.name().equals(newLevel.name())) currentMacroLevel = null;		
			
		});
		
	}
	
	void saveMacroLevel() {
		
		currentMacroLevel.write();
		
	}
	
	void addMacroLevelIntroOSTSegment() {
		
		Supplier<String> intro = DialogUtils.newFileExplorer("Select OST Intro", 5, 270, true , false , assets + "sounds/");
		TemporalExecutor.onTrue(() -> intro.get() != null, () -> currentMacroLevel.addOSTIntroSegment(toNamePath(intro.get())));
		
	}
	
	void addMacroLevelLoopOSTSegment() {
		
		Supplier<String> loop = DialogUtils.newFileExplorer("Select OST Segment" , 5 , 270 , true , false , assets + "sounds/");
		TemporalExecutor.onTrue(() -> loop.get() != null, () -> currentMacroLevel.addOSTLoopSegment(toNamePath(loop.get())));
	
	}
	
	cdNode<String> removeMacroLevelIntroOSTSegment(cdNode<String> iter) {
		
		return currentMacroLevel.safeRemoveIntroSegment(iter);
		
	}

	cdNode<String> removeMacroLevelLoopOSTSegment(cdNode<String> iter) {
		
		return currentMacroLevel.safeRemoveLoopSegment(iter);
		
	}
	
	void getUIMemoryDetails() {
		
		UserInterface.printMemoryDetails(console);
		
	}
	
	void printEngineAllocations() {
		
		ArrayList<String> allocations = new ArrayList<String>();
		
		MemoryAllocationReport rep = (address , memory , threadID , threadName , element) -> {
		
			allocations.add("At " + address + ": " + memory + " bytes in " + threadName);
		
		};
		
		memReport(rep);		
		for(int i = 0 ; i < allocations.size()  ; i ++) say(allocations.get(i));

	}
	
	void printStackTraces() {
		
		MemoryAllocationReport rep = (address , memory , threadID , threadName , element) -> {
			
			System.err.println("At " + address + ": " + memory + " bytes in " + threadName);
			for(StackTraceElement ste : element) System.err.println(ste);
			
		};
		
		memReport(rep);
		
	}
	
	void removeColor() {
		
		Supplier<float[]> colors = DialogUtils.newColorChooser("Remove Color" , 5 , 270);
		TemporalExecutor.onTrue(() -> colors.get() != null , () -> removeActiveColor(colors.get()[0] , colors.get()[1] , colors.get()[2]));
							
	}
	
	void applyFilter() {
		
		Supplier<float[]> colors = DialogUtils.newColorChooser("Filter Color" , 5 , 270);
		TemporalExecutor.onTrue(() -> colors.get() != null , () -> filterActiveColor(colors.get()[0] , colors.get()[1] , colors.get()[2]));
		
	}

	SpriteSets activeSpriteSet;
	int activeSpriteID = -1;
	
	void newSpriteSet() {
		
		Supplier<String> name = DialogUtils.newInputBox("Spriteset Name", 5 , 270);
		TemporalExecutor.onTrue(() -> name.get() != null, () -> {

			activeSpriteSet = new SpriteSets((CharSequence) name.get());
			activeSpriteID = -1;
			
		});
		
	}
	
	void loadSpriteSet() {
		
		Supplier<String> filepath = DialogUtils.newFileExplorer("Select a Sprite Set", 5 , 270 , false , false , data + "spritesets/");
		TemporalExecutor.onTrue(() -> filepath.get() != null, () -> {
			
			activeSpriteSet = new SpriteSets(toNamePath(filepath.get()));	
			activeSpriteID = -1;
			
		});
			
	}
	
	boolean spriteSetEditorValidity() {
		
		return activeSpriteSet != null && activeQuad != null && activeQuad.isTextured();
		
	}
	
	void deleteActiveSpriteSet() {

		activeSpriteSet.delete();
		activeSpriteSet = null;
		activeSpriteID = -1;
		
	}
	
	void saveSelectionAreaAsSpriteSetFrame() {

		//gets the U and V coordinates from the proportion of the selection area's position  
		//over the object divided by the total width or height of the quad.						
		float[] vertices = activeQuad.getData();
		float[] selection = this.selection.vertices;
		
		float leftUDistance = selection[27] - vertices[27];
		float rightUDistance = selection[0] - vertices[27];
		
		float[] qDims = {activeQuad.getWidth() , activeQuad.getHeight()};
		
		float leftU = leftUDistance / qDims[0];
		float rightU = rightUDistance / qDims[0];
		
		float bottomVDistance = selection[1] - vertices[1];
		float topVDistance = selection[10] - vertices[1];
		
		float bottomV = bottomVDistance / qDims[1];
		float topV = topVDistance / qDims[1];
		
		float[] selectionDims = this.selection.getDimensions();
		
		if(leftU < 0.0f) leftU = 0.0f;
		if(rightU > 1.0f) rightU = 1.0f;
		if(topV > 1.0f) topV = 1.0f;
		if(bottomV < 0f) bottomV = 0f;
		
		activeSpriteSet.storeSprite(leftU, rightU , topV, bottomV , Math.round(selectionDims[0] / 2) , Math.round(selectionDims[1] / 2f));
	
	}
	
	void tryPlayActiveSpriteSet() {
		
		if(activeSpriteSet.getNumberSprites() > 0) activeSpriteSet.setRunSpriteSet(true);
	
	}
	
	void swapActiveAnimSprite() {

		float[] currentValues = activeSpriteSet.swapSprite();
		activeQuad.swapSprite(currentValues);
	
	}
	
	void forceStopPlayingActiveSpriteSet() {
		
		activeSpriteSet.setRunSpriteSet(false);
		
	}
	
	void deleteSpriteFromSet() {
		
		activeSpriteSet.deleteSprite(activeSpriteID);
		activeSpriteID = -1;
		
	}
	
	void updateSpriteFromSet() {
		
		float[] activeSprite = activeSpriteSet.getSprite(activeSpriteID);
		boolean modifiesHitBox = activeSprite.length % 3 != 0;
		
		float[] UV = activeQuad.getUVs();
		float[] spriteData;
		int size = 6;
		int numberJoints = 0;
		int hitboxActivatedIndex = -2;
		//get the size of the sprite array
		for(int i = 0 ; i < jointMarkers.length() ; i ++) if (jointMarkers.get(i) != null) numberJoints ++;
		if(jointMarkers.size() > 0) size += (numberJoints * 3);
		if(modifiesHitBox) { 
			
			size += 1;
			hitboxActivatedIndex = (int) activeSprite[activeSprite.length - 1];
		
		}
		
		spriteData = new float [size];
		
		spriteData[0] = UV[0];		
		spriteData[1] = UV[1];
		spriteData[2] = UV[2];
		spriteData[3] = UV[3];
		spriteData[4] = activeSprite[4];
		spriteData[5] = activeSprite[5];
		
		if(jointMarkers.size() > 0) {
			
			//i is iterator of joint markers, j is the offsetinth the sprite array
			
			Joints currentJoint = getJoint(0);
			float[] jointMid;
			float[] quadData = activeQuad.getData();
			
			for(int i = 0 , j = 6 ; i < jointMarkers.size() ; i ++ , currentJoint = getJoint(i)) {
				
				if(currentJoint == null) continue;
				
				jointMid = currentJoint.getMidpoint();
				
				//joint ID
				spriteData[j] = currentJoint.getID();
				//joint x offset
				spriteData[j + 1] = jointMid[0] - quadData[9];
				//joint y offset
				spriteData[j + 2] = jointMid[1] - quadData[10];							
				j += 3;
				
			}
			
		}
		
		if(modifiesHitBox) spriteData[size - 1] = hitboxActivatedIndex;
		
		activeSpriteSet.replaceSprite(activeSpriteID , spriteData);							
		activeSpriteSet.write();
		activeSprite = spriteData;					
	
	}
	
	void replaceSprite() {
		
		float[] vertices = activeQuad.getData();
		float[] selection = this.selection.vertices;
		
		float leftUDistance = selection[27] - vertices[27];
		float rightUDistance = selection[0] - vertices[27];
		
		float[] qDims = {activeQuad.getWidth() , activeQuad.getHeight()};
		
		float leftU = leftUDistance / qDims[0];
		float rightU = rightUDistance / qDims[0];
		
		float bottomVDistance = selection[1] - vertices[1];
		float topVDistance = selection[10] - vertices[1];
		
		float bottomV = bottomVDistance / qDims[1];
		float topV = topVDistance / qDims[1];
		
		float[] selectionDims = this.selection.getDimensions();
		
		if(leftU < 0.0f) leftU = 0.0f;
		if(rightU > 1.0f) rightU = 1.0f;
		if(topV > 1.0f) topV = 1.0f;
		if(bottomV < 0f) bottomV = 0f;
		
		activeSpriteSet.replaceSprite(activeSpriteID , leftU, rightU , topV, bottomV , Math.round(selectionDims[0] / 2) , Math.round(selectionDims[1] / 2f));
		
	}
	
	void activeSpriteActivateHitBox() {
		
		Supplier<String> hitboxToActivate = DialogUtils.newInputBox("Input an Integer Index of a HitBox to Activate", 5, 270, UserInterface.NUMBER_FILTER);
		TemporalExecutor.onTrue(() -> hitboxToActivate.get() != null , () -> {
			
			try {
				
				int hitbox = (int)toNumber(hitboxToActivate.get());
				float[] sprite = activeSpriteSet.getSprite(activeSpriteID);
				int length = sprite.length % 3 == 0 ? sprite.length + 1 : sprite.length;
				float[] newSprite = new float[length];
				System.arraycopy(sprite, 0, newSprite, 0, sprite.length);
				newSprite[newSprite.length -1] = hitbox;
				activeSpriteSet.replaceSprite(activeSpriteID, newSprite);
				
			} catch(Exception e) {
				
				say(hitboxToActivate.get() + " not castable to int");
				
			}
						
		});
		
	}
	
	void addActiveSpriteSetToEntity() {
		
		if(!(activeQuad instanceof Entities)) return;
		
		Entities E = (Entities)activeQuad;		
		((EntityAnimations)(E.components()[Entities.AOFF])).add(activeSpriteSet);
		E.write();
			
	}
	
	void addJointMarkerForSprite() {
		
		addJoint();
		int next = jointMarkers.size() - 1;
		getJoint(next).moveTo(activeQuad);

	}
	
	void setActiveColliderWidth() {
		
		Supplier<String> width = DialogUtils.newInputBox("Set Width", 5, 270, UserInterface.NUMBER_FILTER);
		TemporalExecutor.onTrue(() -> width.get() != null , () -> ((Colliders)activeQuad).setWidth(Float.parseFloat(width.get())));
		
	}
	
	void setActiveColliderHeight() {
		
		Supplier<String> height = DialogUtils.newInputBox("Set Height", 5, 270, UserInterface.NUMBER_FILTER);
		TemporalExecutor.onTrue(() -> height.get() != null , () -> ((Colliders)activeQuad).setHeight(Float.parseFloat(height.get())));
		
	}
	
	void activeStaticToggleColliders() {
		
		((Statics)activeQuad).toggleFocusColliders();
		if(((Statics)activeQuad).collidersFocused()) uiManager.staticColliderEditor.show();
		else uiManager.staticColliderEditor.hide();
		
	}
	
	void deleteEntityFilePath() {
		
		Supplier<String> filepathToDelete = DialogUtils.newFileExplorer("Select an Entity to Delete" , 5 , 270 , false , false , data + "entities/");
		TemporalExecutor.onTrue(() -> filepathToDelete.get() != null , () -> {
			
			try {
				
				GameFiles.delete(filepathToDelete.get());
				
			} catch(Exception e) {
			
				System.err.println("Error occured tring to delete " + filepathToDelete.get() + ", terminating action");
				
			}					
			
		});
		
	}
	
	void toggleComponent(Entities E , ECS component) {
		
		EntityLists.toggleComponent(E, component);
		
	}
	
	void selectEntityScript() {
		
		Object[] comps = ((Entities)activeQuad).components();
		
		Supplier<String> scriptPath = DialogUtils.newFileExplorer("Select script", 5 , 270 , false , false , data + "scripts/");
		TemporalExecutor.onTrue(() -> scriptPath.get() != null, () -> {
			
			tryCatch(() -> {

				EntityScripts script = new EntityScripts(scene , (Entities)comps[0] , (String) toNamePath(scriptPath.get()));
				comps[Entities.SOFF] = script;
				
			}, "Error loading script: " + scriptPath.get() + ", terminating action");
			
		});
		
	}
	
	void recompileEntityScript() {

		Object[] comps = ((Entities)activeQuad).components();
		
		try {
			
			EntityScripts interpreter = (EntityScripts) comps[Entities.SOFF];							
			interpreter.recompile();
			
		} catch (Exception e) {
			
			e.printStackTrace();								
			say("Entity Script Compilation Error");
			EntityLists.toggleComponent((Entities)comps[0], ECS.SCRIPT);
			
		}
			
	}
	
	void addEntityAnimation() {

		Object[] comps = ((Entities)activeQuad).components();
		
		Supplier<String> spriteSets = DialogUtils.newFileExplorer("Select one or more SpriteSets" , 5 , 270 , true , false , data + "spritesets/");
		TemporalExecutor.onTrue(() -> spriteSets.get() != null, () -> {
			
			tryCatch(() -> {

				EntityAnimations anims = (EntityAnimations)comps[Entities.AOFF];

				if(spriteSets.get().contains("|")) {//split
					
					String[] splitSets = spriteSets.get().split("\\|");									
					for(String split : splitSets) anims.add(new SpriteSets(toNamePath(split)));
					
				} else anims.add(new SpriteSets(spriteSets.get()));

			}, "Error adding SpriteSets, terminating action.");							
			
		});
		
	
	}
	
	void addEntityHitbox() {

		Object[] comps = ((Entities)activeQuad).components();

		Supplier<String> hitboxes = DialogUtils.newFileExplorer("Select one or more HitBoxSets", 5 , 270 , true , false , data + "hitboxsets/");
		TemporalExecutor.onTrue(() -> hitboxes.get() != null, () -> {
			
			tryCatch(() -> {
				
				EntityHitBoxes Ehitboxes = (EntityHitBoxes)comps[Entities.HOFF];
				String res = hitboxes.get();
				
				if(res.contains("|")) {
					
					String [] hitboxSets = res.split("\\|");
					for(String split : hitboxSets) Ehitboxes.addSet(new HitBoxSets(split));
					
				} else Ehitboxes.addSet(new HitBoxSets(res));
				
			}, "Error occurred adding hitbox, terminating action.");
			
		});
		
	}
	
	void setEntityMaxNumberBoxes() {

		Object[] comps = ((Entities)activeQuad).components();

		Supplier<String> input = DialogUtils.newInputBox("Input Max Number of HitBoxes" , 5 , 270);
		TemporalExecutor.onTrue(() -> input.get() != null, () -> {
			
			try {
				
				int numberBoxes = (int)toNumber(input.get());
				comps[Entities.HOFF] = new EntityHitBoxes(numberBoxes);
				
			} catch(NumberFormatException e) {
				
				say("Invalid input for an integer; " + input);
				
			}
			
		});
		
	}
	
	void addItemToEntityInventory() {

		Object[] comps = ((Entities)activeQuad).components();
		
		Supplier<String> items = DialogUtils.newFileExplorer("Select One or More Items", 5, 270, true , false, data + "items/");
		TemporalExecutor.onTrue(() -> items.get() != null , () -> {
			
			String filepaths = items.get();
			Inventories inv = ((Inventories) comps[Entities.IOFF]);
			
			if(filepaths.contains("|")) {
				
				String[] paths = filepaths.split("\\|");
				for(String split : paths) inv.acquire(new Items(scene , toNamePath(split)));
				
			} else inv.acquire(new Items(scene , toNamePath(filepaths)));
			
		});
	
	}
	
	void equipItemOnEntity() {

		Object[] comps = ((Entities)activeQuad).components();
		
		Supplier<String> item = DialogUtils.newFileExplorer("Select an Equippable Item", 5, 270, false , false, data + "items/");
		TemporalExecutor.onTrue(() -> item.get() != null , () -> {

			tryCatch(() -> ((Inventories) comps[Entities.IOFF]).equip(new Items(scene , toNamePath(item.get()))) , 
				"Error adding item to equip, terminating action");
			
		});
		
	}
	
	void addFlagToEntity() {

		Object[] comps = ((Entities)activeQuad).components();
		
		Supplier<String> flagName = DialogUtils.newInputBox("Input Flag Name", 5 , 270);											
		TemporalExecutor.onTrue(() -> flagName.get() != null , () -> ((EntityFlags)comps[Entities.FOFF]).add(flagName.get()));
								
	}
	
	void addSoundToEntity() {

		Object[] comps = ((Entities)activeQuad).components();
		
		Supplier<String> soundFiles = DialogUtils.newFileExplorer("Select one or more Sounds", 5 , 270 , false , true);
		TemporalExecutor.onTrue(() -> soundFiles.get() != null, () -> {
			
			String[] split = soundFiles.get().split("\\|");
			@SuppressWarnings("unchecked") CSArray<Sounds> sounds = (CSArray<Sounds>)comps[Entities.AEOFF];
			for(String file : split) sounds.add(SoundEngine.add(file));
			
		});

	}
	
	void printEntityHitboxSets() {

		Object[] comps = ((Entities)activeQuad).components();
		ArrayList<HitBoxSets> list = ((EntityHitBoxes) comps[Entities.HOFF]).getSets();
		for(HitBoxSets hb : list) {
			
			say(hb.name());
			say(hb.size());
			for(int i = 0 ; i < hb.size() ; i ++) say(hb.hitboxAsString(i));
			
		}
	
	}
	
	void printEntityAnimations() {

		Object[] comps = ((Entities)activeQuad).components();		
		EntityAnimations anims = (EntityAnimations)comps[Entities.AOFF];		
		for(SpriteSets ss : anims.anims()) if(ss != null) say(ss.name());
		
	}
	
	void printEntityInventory() {
		
		Object[] comps = ((Entities)activeQuad).components();
		Inventories inv = (Inventories) comps[Entities.IOFF];
	
		say("Inventory:");
		cdNode<Tuple2<Items , RefInt>> iter = inv.iter();
		for(int i = 0 ; i < inv.inventorySize() ; i ++ , iter = iter.next) say(iter.val.getFirst().name());
			
	}
	
	void printEntityEquippedItems() {

		Object[] comps = ((Entities)activeQuad).components();
		Inventories inv = (Inventories) comps[Entities.IOFF];

		say("Equipped:");
		CSArray<Items> equipped = inv.getEquipped();
		for(int i = 0 ; i < equipped.size() ; i ++) if(equipped.get(i) != null) say(equipped.get(i).name());		
	
	}
	
	void printEntitySounds() {

		Object[] comps = ((Entities)activeQuad).components();		
		@SuppressWarnings("unchecked") CSArray<Sounds> sounds = (CSArray<Sounds>)comps[Entities.AEOFF];
		for(int i = 0 ; i < sounds.length() ; i ++) say(sounds.get(i).name());
	
	}

	//Object used to mark up hitboxes with a quad. The active quad will be the quad referenced for the math relating to the 
	//hitboxset 
	HitBoxSetMarker hitboxMarker = new HitBoxSetMarker();
	
	void newHitboxSet() {
		
		Supplier<String> nameInput = DialogUtils.newInputBox("Input HitBoxSet Name", 5 , 270);
		TemporalExecutor.onTrue(() -> nameInput.get() != null , () -> {
			
			hitboxMarker.clear();
			hitboxMarker.active = -1;				
			hitboxMarker.editingName = nameInput.get();
			
		});
					
	}	
	
	void loadHitboxSet() {
		
		Supplier<String> filepath = DialogUtils.newFileExplorer("Select a HitBoxSet File" , 5 , 270 , false , false);
		TemporalExecutor.onTrue(() -> filepath.get() != null , () -> {
			
			hitboxMarker.clear();
			hitboxMarker.active = -1;
			
		});
		
	}
	
	void deleteHitboxSet() {
		
		Supplier<String> filepath = DialogUtils.newFileExplorer("Select a HitBoxSet File" , 5 , 270 , false , false);
		TemporalExecutor.onTrue(() -> filepath.get() != null , () -> {

			try {
				
				HitBoxSets hb = new HitBoxSets(filepath.get());
				hb.delete();
				
			} catch(Exception e) {
				
				say("error: " + filepath.get() + " is not a valid hitboxset file");							
				
			}
								
		});
		
	}
	
	boolean isHitboxsetAlive() {
		
		return hitboxMarker.editingName != null;
		
	}
	
	void addHitbox() {
		
		hitboxMarker.addHitBox(activeQuad);
		
	}
	
	void removeHitbox() {
		
		hitboxMarker.removeHitBox(hitboxMarker.active);
		
	}
	
	void removeAllHitboxes() {
		
		hitboxMarker.clear();
		hitboxMarker.active = -1;
		
	}
	
	boolean isHitboxSelected() {
		
		return hitboxMarker.active != -1;
		
	}
	
	void toggleActiveHitboxHot() {
		
		hitboxMarker.hotBoxes[hitboxMarker.active] = hitboxMarker.hotBoxes[hitboxMarker.active] != -1 ? -1 : hitboxMarker.active;
		
	}
	
	void toggleActiveHitboxCold() {
		
		hitboxMarker.coldBoxes[hitboxMarker.active] = hitboxMarker.coldBoxes[hitboxMarker.active] != -1 ? -1:hitboxMarker.active;
		
	}
	
	void saveHitboxSetAs() {

		Supplier<String> fileName = DialogUtils.newInputBox("Input new HitBoxSet Name", 5 , 270);
		TemporalExecutor.onTrue(() -> fileName.get() != null , () -> {
			
			HitBoxSetMarker newHitboxMarker = new HitBoxSetMarker();
			newHitboxMarker.copy(hitboxMarker);
			newHitboxMarker.editingName = fileName.get();
			hitboxMarker = newHitboxMarker;
			hitboxMarker.toHitBoxSet(activeQuad).write();
			
		});
		
	}
	
	void saveHitboxSet() {
		
		hitboxMarker.toHitBoxSet(activeQuad).write();
		
	}
	
	boolean activeQuadValidEntityForHitboxes() {
		
		return (activeQuad instanceof Entities) && ((Entities)activeQuad).has(ECS.ANIMATIONS , ECS.HITBOXES);
		
	}
	
	void addHitboxSetToEntity() {

		Entities E = (Entities)activeQuad;
		EntityHitBoxes entityHitboxes = (EntityHitBoxes) E.components()[Entities.HOFF];

		entityHitboxes.addSet(hitboxMarker.toHitBoxSet(E));
		E.write();
	
	}
	
	void resetActiveDims() { 

		Items activeItem = (Items) activeQuad;
		
		activeItem.fitQuadToTexture();
		activeItem.setLeftUCoords(0);
		activeItem.setRightUCoords(1);
		activeItem.setTopVCoords(1);
		activeItem.setBottomVCoords(0);
		
	}
	
	void saveSelectionAreaAsItemIcon() {

		Items activeItem = (Items) activeQuad;
		
		float[] vertices = activeItem.getData();
		float[] selection = this.selection.vertices;
		
		float leftUDistance = selection[27] - vertices[27];
		float rightUDistance = selection[0] - vertices[27];
		
		float[] qDims = {activeItem.getWidth() , activeItem.getHeight()};
		
		float leftU = leftUDistance / qDims[0];
		float rightU = rightUDistance / qDims[0];
		
		float bottomVDistance = selection[1] - vertices[1];
		float topVDistance = selection[10] - vertices[1];
		
		float bottomV = bottomVDistance / qDims[1];
		float topV = topVDistance / qDims[1];
		
		float[] selectionDims = this.selection.getDimensions();
		
		if(leftU < 0.0f) leftU = 0.0f;
		if(rightU > 1.0f) rightU = 1.0f;
		if(topV > 1.0f) topV = 1.0f;
		if(bottomV < 0f) bottomV = 0f;
		
		activeItem.iconSprite(new float [] {leftU, rightU , topV, bottomV , Math.round(selectionDims[0] / 2) , Math.round(selectionDims[1] / 2f)});
		
	}
	
	void setItemMaxStackSize() {

		Items activeItem = (Items) activeQuad;
		
		Supplier<String> stackSize = DialogUtils.newInputBox("Max Size of a Stack in an Inventory", 5, 270, UserInterface.NUMBER_FILTER);
		TemporalExecutor.onTrue(() -> stackSize.get() != null , () -> activeItem.maxStackSize((int)toNumber(stackSize.get())));
		
	}
	
	void setItemIconAnimation() {

		Items activeItem = (Items) activeQuad;
		
		Supplier<String> animation = DialogUtils.newFileExplorer("Select an Animation", 5, 270, data + "spritesets/");
		TemporalExecutor.onTrue(() -> animation.get() != null , () -> activeItem.setIconAnimation(toNamePath(animation.get())));
		
	}
	
	void setItemEquippable() {

		Items activeItem = (Items) activeQuad;
		activeItem.componentData().setEquippable();
		
	}
		
	void setItemUsable() {
	
		Supplier<String> file =  DialogUtils.newFileExplorer("Select Item Use Script", 5 , 270);
		TemporalExecutor.onTrue(() -> file.get() != null , () -> {

			Items activeItem = (Items) activeQuad;
			activeItem.componentData().setUsable(toNamePath(file.get()));
		
		});
		
	}	
	
	void setItemMaterial() {

		Items activeItem = (Items) activeQuad;
		activeItem.componentData().setMaterials();
		
	}

	void setItemHitboxable() {

		Items activeItem = (Items) activeQuad;
		activeItem.componentData().setHitboxable();
		
	}

	void setItemConsumable() {

		Items activeItem = (Items) activeQuad;
		activeItem.componentData().setConsumable();
		
	}

	void setItemFlags() {

		Items activeItem = (Items) activeQuad;
		activeItem.componentData().setFlags();
		
	}
	
	void setItemEquipSlot() {

		Items activeItem = (Items) activeQuad;
		
		Supplier<String> slotNumber = DialogUtils.newInputBox("Equip Slot", 5 , 270 , UserInterface.DEFAULT_FILTER);
		TemporalExecutor.onTrue(() -> slotNumber.get() != null , () -> {
			
			try {
				
				int slotInt = (int)toNumber(slotNumber.get());
				if(slotInt < 0) throw new NumberFormatException();
				activeItem.componentData().equipSlot(slotInt);
												
			} catch(NumberFormatException e) {
				
				say("Not a valid number: " + slotNumber);
				
			}
			
		});
		
	}
	
	void setItemUseScript() {

		Items activeItem = (Items) activeQuad;
		
		Supplier<String> scriptFile = DialogUtils.newFileExplorer("Select a Script to Execute on Item Use", 5 , 270 , false , false , data + "scripts/");
		TemporalExecutor.onTrue(() -> scriptFile.get() != null, () -> {
			
			String scriptPath = (String) toNamePath(scriptFile.get());
			if(scriptPath.endsWith(".py")) activeItem.componentData().onUse(scriptPath);
			else say("Not a valid python script: " + scriptFile.get());			
			
		});
												
	}
	
	void recompileItemUseScript() {
		
		((Items) activeQuad).componentData().recompileUseScript();
		
	}
	
	void addItemHitboxSet() {

		Items activeItem = (Items) activeQuad;
		
		Supplier<String> hitboxPaths = DialogUtils.newFileExplorer("Select one or more HitBoxSets" , 120,  120 , false , true);
		TemporalExecutor.onTrue(() -> hitboxPaths.get() != null, () -> {
			
			String[] split = hitboxPaths.get().split("\\|");
			for(String hb : split) activeItem.componentData().addHitBox(new HitBoxSets(hb));
			
		});			
	
	}
	
	void setItemChanceToConsume() {

		Supplier<String> input = DialogUtils.newInputBox("Input a Number Between 1 and 100", 120, 120);
    	TemporalExecutor.onTrue(() -> input.get() != null , () -> ((Items) activeQuad).componentData().chanceToConsume((int)toNumber(input.get())));
    	
	}
	
	void addFlagToItem() {
		
		Supplier<String> flagName = DialogUtils.newInputBox("Input Flag's Name" , 5 , 270);
		TemporalExecutor.onTrue(() -> flagName.get() != null , () -> ((Items) activeQuad).componentData().addFlag(flagName.get()));
	
	}
	
	void removeFlagFromItem() {
		
		
		
	}
	
	void printItemHitboxSets() {
		
		EntityHitBoxes hb = ((Items) activeQuad).componentData().HitBoxable();
		ArrayList<HitBoxSets> sets = hb.getSets();
		for(int i = 0 ; i < sets.size() ; i ++) say(sets.get(i).name());
		
	}
	
	void newTileset() {
		
		Supplier<String> name = DialogUtils.newInputBox("Input Tile Set Name", 5, 270);
		TemporalExecutor.onTrue(() -> name.get() != null, () -> {
			
			Supplier<String> texture = DialogUtils.newFileExplorer("Select a Texture", 5, 270, false, false , assets + "spritesheets/");
			TemporalExecutor.onTrue(() -> texture.get() != null	, () -> {
				
				newTileSet(name.get() , texture.get());
				
			});

		});
		
	}
	
	void loadTileset() {
		
		Supplier<String> filepath = DialogUtils.newFileExplorer("Select Tile Set", 5 , 270, data + "tilesets/");
		TemporalExecutor.onTrue(() -> filepath.get() != null , () -> loadTileSet(filepath.get()));
		
	}
	
	void deleteTileset() {
		
		Supplier<String> fileToDelete = DialogUtils.newFileExplorer("Delete a file", 5, 270, false, false, data + "tilesets/");
		TemporalExecutor.onTrue(() -> fileToDelete.get() != null , () -> GameFiles.delete(fileToDelete.get()));
		
	}
	
	void renameTileset() {

		TileSets currentTileSet = background ? scene.tiles1() : scene.tiles2();
		
		Supplier<String> name = DialogUtils.newInputBox("Input Tile Set Name", 5, 270);
		TemporalExecutor.onTrue(() -> name.get() != null , () -> currentTileSet.name(name.get()));
		
	}
	
	void clearTileset() {

		TileSets currentTileSet = background ? scene.tiles1() : scene.tiles2();		
		currentTileSet.clearInstances();
			
	}
	
	void removeColorFromTileset() {

		TileSets currentTileSet = background ? scene.tiles1() : scene.tiles2();
		
		Supplier<float[]> removedColor = DialogUtils.newColorChooser("Remove Color", 5, 270);
		TemporalExecutor.onTrue(() -> removedColor.get() != null , () -> currentTileSet.remove(removedColor.get()));
		
	}
	
	void retextureTileset() {

		TileSets currentTileSet = background ? scene.tiles1() : scene.tiles2();
		
		Supplier<String> texturePath = DialogUtils.newFileExplorer("Select a Texture", 5, 270, false, false, assets + "spritesheets/");
		TemporalExecutor.onTrue(() -> texturePath.get() != null , () -> currentTileSet.texture(texturePath.get()));
		
	}
	
	Tuple2<NkImage , NkRect> saveSelectionAreaAsTile(NkImage tilesImage) {

		TileSets currentTileSet = background ? scene.tiles1() : scene.tiles2();
		
		float[] vertices = currentTileSet.getTileSheet().getData();
		float[] selection = this.selection.vertices;
		
		float leftUDistance = selection[27] - vertices[27];
		float rightUDistance = selection[0] - vertices[27];
		
		float[] qDims = {currentTileSet.getTileSheet().getWidth() , currentTileSet.getTileSheet().getHeight()};
		
		float leftU = leftUDistance / qDims[0];
		float rightU = rightUDistance / qDims[0];
		
		float bottomVDistance = selection[1] - vertices[1];
		float topVDistance = selection[10] - vertices[1];
		
		float bottomV = bottomVDistance / qDims[1];
		float topV = topVDistance / qDims[1];
		
		float[] selectionDims = this.selection.getDimensions();
		
		if(leftU < 0.0f) leftU = 0.0f;
		if(rightU > 1.0f) rightU = 1.0f;
		if(topV > 1.0f) topV = 1.0f;
		if(bottomV < 0f) bottomV = 0f;
		
		currentTileSet.addSourceTile(new float[] {leftU , rightU , topV , bottomV , selectionDims[0] , selectionDims[1]});				
		
		Tuple2<NkImage , NkRect> subRegionResult = UserInterface.subRegion(
				tilesImage , currentTileSet.textureInfo() , 
				(short)leftUDistance , (short)(vertices[10] - selection[10]) , (short)selectionDims[0] , (short)selectionDims[1]);
		
		return subRegionResult;
		
	}
	
	void setTileName(Tiles tile) { 

		Supplier<String> newName = DialogUtils.newInputBox("Rename Tile" , 5 , 270);
		Tiles query = tile;
		TemporalExecutor.onTrue(() -> newName.get() != null , () -> query.setName(newName.get()));
		
	}
	
	void removeTile(cdNode<Tiles> iter , cdNode<NkImage> iterImage) {

		TileSets currentTileSet = background ? scene.tiles1() : scene.tiles2();
		CSLinked<NkImage> tileIcons = background ? uiManager.tilesetEditor.backgroundTileIcons : uiManager.tilesetEditor.foregroundTileIcons;
		
		iter.val.forEachInstance(removeTile -> currentTileSet.removeInstance(removeTile));
		iter.val.removeInstances();
		iter = currentTileSet.safeRemoveSource(iter);
		iterImage = tileIcons.safeRemove(iterImage);
	}
	
	void setTileAnimation(Tiles tile) {

		Supplier<String> animation = DialogUtils.newFileExplorer("Select Animation", 5, 270, data + "spritesets/");
		TemporalExecutor.onTrue(() -> animation.get() != null , () -> tile.setAnimation(new SpriteSets(toNamePath(animation.get()))));	
		
	}
	
	void engineShutDown() {
		
		engine.closeOverride();
		
	}
	
}
