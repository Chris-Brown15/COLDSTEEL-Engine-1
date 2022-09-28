package Editor;

import static CS.COLDSTEEL.assets;
import static CS.COLDSTEEL.data;
import static CSUtil.BigMixin.getJoints;
import static CSUtil.BigMixin.toBool;
import static CSUtil.BigMixin.toNamePath;
import static Renderer.Renderer.loadTexture;
import static org.lwjgl.Version.getVersion;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkRect;

import CS.Engine;
import CS.RuntimeState;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import Core.Direction;
import Core.ECS;
import Core.Executor;
import Core.GameFiles;
import Core.NKUI;
import Core.Quads;
import Core.Scene;
import Core.SpriteSets;
import Core.TemporalExecutor;
import Core.UIScriptingInterface;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;
import Core.Entities.EntityHitBoxes;
import Core.Entities.EntityScripts;
import Core.Statics.Statics;
import Core.TileSets.TileSets;
import Core.TileSets.Tiles;
import Editor.UI.NuklearUIElement;
import Game.Items.Inventories;
import Game.Items.ItemComponents;
import Game.Items.Items;
import Game.Levels.Levels;
import Game.Levels.MacroLevels;
import Physics.Colliders;
import Physics.Joints;
import Physics.Kinematics;
import Renderer.Camera;
import Renderer.Renderer;

public class Editor {

	private EditorMode mode = EditorMode.BUILD_MODE;
	private EditorState editorState = EditorState.GENERIC;
	private CursorState cursorState = CursorState.SELECTABLE;

	private EditorUI editorUI;

	Camera cam;
	Scene scene;

	private EditorConsole console;

	// camera move speed
	float moveSpeed = 0;

	boolean showPyUI = true;
	SelectionArea selection;
	Levels backupLevel = new Levels("Editor Backup");
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
	Consumer<RuntimeState> switchStateCallback;

	private Consumer<Levels> onLevelLoadEngine;
	private Consumer<Levels> onLevelLoadNuklear;
	Supplier<float[]> cursorWorldCoords;
	Executor closeProgram;
	
	public void initialize(Renderer renderer, Scene scene, Levels currentLevel, Consumer<Levels> onLevelLoadEngine,
			Consumer<RuntimeState> switchStateCallback , Supplier<float[]> cursorWorldCoords , Executor closeProgram) {

		System.out.println("Beginning Editor initialization...");
		console = new EditorConsole();
		this.cam = renderer.getCamera();

		this.selection = new SelectionArea();
		this.scene = scene;
		
		editorUI = new EditorUI(this);
		this.currentLevel = currentLevel;
		renderer.addToRawData(selection.vertices);
		this.onLevelLoadEngine = onLevelLoadEngine;
		onLevelLoadNuklear = editorUI.onLevelLoad;
		this.switchStateCallback = switchStateCallback;
		backupLevel.associate(data + "macrolevels/Editor/");
		
		this.cursorWorldCoords = cursorWorldCoords;
		this.closeProgram = closeProgram;
		
		console.say("Welcome to the 1STEEL5 editor alpha undef, running LWJGL" + getVersion());
		System.out.println("Editor initialization complete.");

	}

	public void switchTo(EditorMode mode) {

		if (this.mode == mode) return;
				
		if (mode == EditorMode.BUILD_MODE) {// switching into build mode
			
			editorUI.resetUIFirstTimeVariables();
			backupLevel.deploy(scene);
			UIScriptingInterface.getPyUIs().clear();

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
		if (toBool(editorUI.renderDebugCheck)) renderDebug();

		if (toBool(editorUI.renderTileSheet)) {

			if(scene.tiles1().getTileSheet() != null) Renderer.draw_background(scene.tiles1().getTileSheet());
			if(scene.tiles2().getTileSheet() != null) Renderer.draw_background(scene.tiles2().getTileSheet());

		}
		
		jointMarkers.forEach(Renderer::draw_foreground);
		editorUI.hitboxMarker.hitboxes().forEach(Renderer::draw_foreground);
		
		NuklearUIElement.layoutElements();
		
		switch (mode) {

		case BUILD_MODE:

			editorUI.buildModeLayoutElements(engine);
			scene.entities().resetScriptCount();

			if (!(engine.keyboardPressed(GLFW_KEY_LEFT_SHIFT) || engine.keyboardPressed(GLFW_KEY_LEFT_CONTROL))) {

				if(engine.keyboardPressed(GLFW_KEY_UP)) cam.moveCamera(scene, 0, moveSpeed);
				if(engine.keyboardPressed(GLFW_KEY_LEFT)) cam.moveCamera(scene, -moveSpeed, 0);
				if(engine.keyboardPressed(GLFW_KEY_RIGHT)) cam.moveCamera(scene, moveSpeed, 0);
				if(engine.keyboardPressed(GLFW_KEY_DOWN)) cam.moveCamera(scene, 0, -moveSpeed);

			}

			Kinematics.process();
			TemporalExecutor.process();
			scene.tiles1().animateTiles();
			scene.tiles2().animateTiles();
			if(editorState == EditorState.EDITING_HITBOX && engine.mousePressed(GLFW_MOUSE_BUTTON_LEFT)) editorUI.dragHitBoxMarker(cursorWorldCoords.get());
			engine.releaseKeys();
			dragQuad();

			break;

		case TEST_MODE:

			if (showPyUI)
				for (int i = 0; i < UIScriptingInterface.getPyUIs().size(); i++) {

					try {

						UIScriptingInterface.getPyUIs().get(i).run();

					} catch (Exception e) {

						System.err.println("Error occurred calling UI script: "
								+ UIScriptingInterface.getPyUIs().get(i).scriptName());
						e.printStackTrace();
						org.lwjgl.nuklear.Nuklear.nk_end(Engine.NuklearContext());

					}

				}

			editorUI.testModeLayoutElements();
			scene.entities().resetScriptCount();
			scene.tiles1().animateTiles();
			scene.tiles2().animateTiles();
			scene.entities().editorRunSystems(() -> {

			}, () -> {

				Kinematics.process();
				TemporalExecutor.process();

			}, () -> {

				engine.releaseKeys();

			});

			if (currentLevel != null) {

				currentLevel.runScripts();
				
			}

			break;

		case HYBRID_MODE:

			editorUI.buildModeLayoutElements(engine);

			if (!(engine.keyboardPressed(GLFW_KEY_LEFT_SHIFT) || engine.keyboardPressed(GLFW_KEY_LEFT_CONTROL))) {

				if(engine.keyboardPressed(GLFW_KEY_UP)) cam.moveCamera(scene, 0, moveSpeed);
				if(engine.keyboardPressed(GLFW_KEY_LEFT)) cam.moveCamera(scene, -moveSpeed, 0);
				if(engine.keyboardPressed(GLFW_KEY_RIGHT)) cam.moveCamera(scene, moveSpeed, 0);
				if(engine.keyboardPressed(GLFW_KEY_DOWN)) cam.moveCamera(scene, 0, -moveSpeed);

			}

			Kinematics.process();
			scene.entities().resetScriptCount();
			scene.tiles1().animateTiles();
			scene.tiles2().animateTiles();
			scene.entities().editorRunSystems(() -> {

			}, () -> {

				// run kinematics and executor
				Kinematics.process();
				TemporalExecutor.process();

			}, () -> {

				// handle input
				engine.releaseKeys();

			});

			if (currentLevel != null) currentLevel.runScripts();
			break;

		}

	}

	private void renderDebug() {

		boolean renderColliders = toBool(editorUI.renderDebugChoosers.get(0));
		boolean renderHitBoxes = toBool(editorUI.renderDebugChoosers.get(1));
		boolean renderJoints = toBool(editorUI.renderDebugChoosers.get(2));

		cdNode<Entities> iter = scene.entities().iter();
		Entities E;
		Object[] comps;
		Direction dir;
		float[] EData;
		for (int i = 0; i < scene.entities().size(); i++, iter = iter.next) {

			E = iter.val;
			EData = E.getData();
			comps = E.components();

			if (renderColliders && E.has(ECS.COLLISION_DETECTION) && comps[Entities.CDOFF] != null) 
				 Renderer.draw_foreground((float[]) comps[Entities.CDOFF]);
			
			dir = (Direction) comps[Entities.DOFF];

			if (renderHitBoxes && E.has(ECS.HITBOXES)) {
				// if an entity has hitboxes it has direction
				EntityHitBoxes hitboxes = (EntityHitBoxes) comps[Entities.HOFF];
				float[][] boxes = hitboxes.getActiveHitBoxes(E, dir);
				if (boxes != null) for (int j = 0; j < boxes.length; j++) Renderer.draw_foreground(boxes[j]);

			}

			if (renderJoints && E.has(ECS.ANIMATIONS)) {

				EntityAnimations anims = (EntityAnimations) comps[Entities.AOFF];
				SpriteSets currentSet = anims.active();
				float[] currentSprite = currentSet.getActiveSprite();
				float[] joints = getJoints(currentSprite);
				float jointXOffset, jointYOffset;
				// if doesnt have at least a joint, we're done with this entity
				if (currentSprite.length <= 7) continue;

				// iterates over only x and y offset of the joints in the joints array
				for (int j = 1; j < joints.length; j += 3) {

					Joints newJ = new Joints();
					if (dir == Direction.LEFT) {

						jointXOffset = EData[9] + joints[j];
						jointYOffset = EData[10] + joints[j + 1];
						newJ.moveTo(-jointXOffset, jointYOffset);

					} else {

						jointXOffset = EData[18] - joints[j];
						jointYOffset = EData[10] + joints[j + 1];
						newJ.moveTo(-jointXOffset, jointYOffset);

					}

					Renderer.draw_foreground(newJ);

				}

			}

			if (E.has(ECS.INVENTORY)) {

				Inventories EInv = (Inventories) E.components()[Entities.IOFF];
				CSArray<Items> equipped = EInv.getEquipped();
				Items item;
				for (int j = 0; j < equipped.size(); j++)
					if ((item = equipped.get(j)) != null && item.getShouldRender() && renderHitBoxes
							&& item.has(ItemComponents.HITBOXABLE)) {

						EntityHitBoxes hitboxes = item.componentData().HitBoxable();
						float[][] boxes = hitboxes.getActiveHitBoxes(item, dir);
						if (boxes != null)
							for (int k = 0; k < boxes.length; k++)
								Renderer.draw_foreground(boxes[k]);

					}

			}

		}

	}

	/**
	 * We can drag a quad with the cursor if the cursor state allows dragging, and
	 * we have pressed the mouse button to begin dragging, and the editor state
	 * allows dragging.
	 * 
	 */
	private void dragQuad() {

		float[] cursor = cursorWorldCoords.get();
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
						editorUI.currentTriggerBound = activeQuad;
						return reselected;
	
					}
					
					clicked = currentLevel.selectTriggerAreas(x, y);
					if(clicked != null) { 
	
						boolean reselected = activeQuad == clicked;
						activeQuad = clicked;
						editorUI.currentTriggerBound = activeQuad;
						return reselected;
	
					}
	
				}
	
			}
	
			case EDITING_STATIC -> {
	
				if (editorUI.activeAsStatic == null)
					return false;
	
				if (editorUI.activeAsStatic.collidersFocused()) return editorUI.activeAsStatic.apply((colliders) -> {
	
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
	
				if (!toBool(editorUI.sliderSelect)) return false;
	
				for (int i = 0; i < editorUI.hitboxMarker.getSize(); i++) if (CSUtil.BigMixin.selectQuad(editorUI.hitboxMarker.hitboxes().get(i), x, y)) {
	
					editorUI.hitboxMarker.active = i;
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
		if (!anims.hasSpriteSet(editorUI.activeSet)) return;

		float[] selectedSprite = editorUI.activeSet.getSprite(editorUI.activeSpriteID);
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

	public Quads addQuad() {

		Quads added;
		if (background) added = scene.quads1().add();
		else added = scene.quads2().add();

		if (spawnAtCursor) {

			float[] cursor = cursorWorldCoords.get();
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
			editorUI.activeAsStatic = null;

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

			activeQuad.setTexture(loadTexture(filepath.get()));
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

		console.say(say);

	}

	public void say(Object... say) {

		console.say(say);

	}

	public void returnConsole() {

		console.command();

	}

	public void addCollider() {

		Colliders newCollider = scene.colliders().add();
		if (spawnAtCursor) {

			float[] cursor = cursorWorldCoords.get();
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
			float[] cursor = cursorWorldCoords.get();
			newStatic.moveTo(cursor[0] , cursor[1]);

		});

	}

	public void loadStatic() {

		Supplier<String> filepath = DialogUtils.newFileExplorer("Select one or more Statics", 5, 270, false, true);
		TemporalExecutor.onTrue(() -> filepath.get() != null, () -> {

			String[] split = filepath.get().split("\\|");
			Statics newStatic;
			float[] cursor = cursorWorldCoords.get();
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

				float[] cursor = cursorWorldCoords.get();
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
				if (spawnAtCursor) {

					float[] cursor = cursorWorldCoords.get();
					loaded.moveTo(cursor[0] , cursor[1]);

				}

			}

		});

	}

	public void loadItem() {

		Supplier<String> filepaths = DialogUtils.newFileExplorer("Select one or more Entities", 5, 270, false, true,
				data + "items/");
		TemporalExecutor.onTrue(() -> filepaths.get() != null, () -> {

			Items newItem;
			String[] split = filepaths.get().split("\\|");
			for (String y : split) {

				newItem = scene.items().load(toNamePath(y));
				if (spawnAtCursor) {

					float[] cursor = cursorWorldCoords.get();
					newItem.moveTo(cursor[0] , cursor[1]);

				}

			}

		});

	}

	public void newItem() {

		Supplier<String> itemName = DialogUtils.newInputBox("Input Item Name", 5, 270);
		TemporalExecutor.onTrue(() -> itemName.get() != null, () -> scene.items().newItem(itemName.get()));

	}

	public EditorConsole getConsole() {

		return console;

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

		if (editorState == EditorState.EDITING_STATIC)
			editorUI.activeAsStatic.translate(x, y);
		else if (editorState == EditorState.EDITING_JOINT)
			translateActiveJoint(x, y);
		else {

			if (activeQuad != null)
				activeQuad.translate(x, y);
			else if (activeJoint != null)
				activeJoint.translate(x, y);

		}

	}

	void deleteItems(String path) {

		Items deleteThis = new Items((String) CSUtil.BigMixin.toNamePath(path));
		deleteThis.delete();

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
	MacroLevels createMacroLevel(String name) {

		MacroLevels newMacro = new MacroLevels(name);
		newMacro.initialize();
		return newMacro;

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

			Levels newLevel = new Levels((CharSequence) filepath.get());
			newLevel.deploy(scene);
			setupTileSetUIImages(scene.tiles1() , background);
			setupTileSetUIImages(scene.tiles2() , !background);
			currentLevel = newLevel;
			onLevelLoadEngine.accept(newLevel);
			onLevelLoadNuklear.accept(newLevel);
			Engine.TRIGGER_SCRIPTING_INTERFACE.onLevelLoad.accept(newLevel);
			say("Loaded level: " + newLevel.gameName());
			editorUI.currentLoadDoor = null;
			editorUI.currentTrigger = null;
			editorUI.linkedLevel = null;

		});

	}

	public void loadClearDeploy(String levelPath) {

		Levels newLevel = new Levels((CharSequence) (CS.COLDSTEEL.data + "macrolevels\\" + levelPath));

		scene.clear();
		newLevel.deploy(scene);

		currentLevel = newLevel;
		currentLevel = newLevel;
		onLevelLoadEngine.accept(newLevel);
		onLevelLoadNuklear.accept(newLevel);
		Engine.TRIGGER_SCRIPTING_INTERFACE.onLevelLoad.accept(newLevel);
		say("Loaded level: " + newLevel.gameName());
		editorUI.currentLoadDoor = null;
		editorUI.currentTrigger = null;
		editorUI.linkedLevel = null;

	}

	public void setCurrentLevel(Levels newCurrentLevel) {

		if(currentLevel != null && !currentLevel.empty()) scene.clear();
		this.currentLevel = newCurrentLevel;
		editorUI.currentLevel = currentLevel;

	}

	public Entities forName(String name) {

		cdNode<Entities> iter = scene.entities().iter();
		for (int i = 0; i < scene.entities().size(); i++, iter = iter.next) if (iter.val.name().equals(name)) return iter.val;
		return null;

	}

	void setupTileSetUIImages(TileSets target , boolean background) {

		if(target.uninitialized()) return;
		
		NkImage tileSetSheet = background ? editorUI.backgroundTileSetSpriteSheet : editorUI.foregroundTileSetSpriteSheet;
		
		NKUI.image(target.textureInfo().path(), tileSetSheet);

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

			Tuple2<NkImage, NkRect> subRegionResult = NKUI.subRegion(tileSetSheet , target.textureInfo() , leftX , topY , 
																	(short) tileSpecs[4], (short) tileSpecs[5]);
			
			CSLinked<NkImage> tileIcons = background ? editorUI.backgroundTileIcons : editorUI.foregroundTileIcons;
			tileIcons.add(subRegionResult.getFirst());

		});

	}

	public TileSets newTileSet(String name, String filepath) {

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
		float[] cursorPos = cursorWorldCoords.get();
		copied.moveTo(cursorPos[0] , cursorPos[1]);
		return copied;

	}

	public TileSets loadTileSet(String filepath) {

		TileSets target = background ? scene.tiles1() : scene.tiles2();
		NkImage tileSetSheet = background ? editorUI.backgroundTileSetSpriteSheet : editorUI.foregroundTileSetSpriteSheet;
		
		if (!target.uninitialized()) {

			// if another tile set was already loaded and in use:
			target.clear();
			CSLinked<NkImage> tileIcons = background ? editorUI.backgroundTileIcons : editorUI.foregroundTileIcons;
			tileIcons.clear();

		}

		target.load(toNamePath(filepath));
		NKUI.image(target.textureInfo().path(), tileSetSheet);

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

			Tuple2<NkImage, NkRect> subRegionResult = NKUI.subRegion(tileSetSheet , target.textureInfo() , leftX , topY , 
																	(short) tileSpecs[4], (short) tileSpecs[5]);
			
			CSLinked<NkImage> tileIcons = background ? editorUI.backgroundTileIcons : editorUI.foregroundTileIcons;
			tileIcons.add(subRegionResult.getFirst());

		});

		return target;

	}

	public Consumer<Levels> onLevelLoad = newLevel -> currentLevel = newLevel;

	public void leaveEditor() {

		scene.clear();
		switchStateCallback.accept(RuntimeState.GAME);

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

	public void shutDown() {

		editorUI.shutDown();
		console.shutDown();

	}

}
