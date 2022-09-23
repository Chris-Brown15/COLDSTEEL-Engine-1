package Editor;

import static CS.COLDSTEEL.assets;
import static CS.COLDSTEEL.data;
import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.alloc1;
import static CSUtil.BigMixin.changeColorTo;
import static CSUtil.BigMixin.getArrayHeight;
import static CSUtil.BigMixin.getArrayWidth;
import static CSUtil.BigMixin.getColliderFloatArray;
import static CSUtil.BigMixin.makeTranslucent;
import static CSUtil.BigMixin.modArrayHeight;
import static CSUtil.BigMixin.modArrayWidth;
import static CSUtil.BigMixin.moveTo;
import static CSUtil.BigMixin.offloadFloatAssignment;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toBool;
import static CSUtil.BigMixin.toByte;
import static CSUtil.BigMixin.toLocalDirectory;
import static CSUtil.BigMixin.toNamePath;
import static CSUtil.BigMixin.toggle;
import static CSUtil.BigMixin.translateArray;
import static Core.NKUI.subRegion;
import static Physics.MExpression.toNumber;
import static org.lwjgl.nuklear.Nuklear.NK_CHART_COLUMN;
import static org.lwjgl.nuklear.Nuklear.NK_RGB;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_DOWN;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_TOP;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZED;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_SCALABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_button_image;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_chart_begin;
import static org.lwjgl.nuklear.Nuklear.nk_chart_end;
import static org.lwjgl.nuklear.Nuklear.nk_chart_push;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_color_pick;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_image;
import static org.lwjgl.nuklear.Nuklear.nk_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_radio_label;
import static org.lwjgl.nuklear.Nuklear.nk_radio_text;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_label;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_text;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;
import static org.lwjgl.nuklear.Nuklear.nk_slider_int;
import static org.lwjgl.system.MemoryUtil.memReport;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkColorf;
import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil.MemoryAllocationReport;
import org.python.core.PyObject;

import AudioEngine.SoundEngine;
import AudioEngine.Sounds;
import CS.Engine;
import CS.RuntimeState;
import CSUtil.RefInt;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.Tuple3;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import Core.AbstractGameObjectLists;
import Core.Direction;
import Core.ECS;
import Core.GameFiles;
import Core.HitBoxSets;
import Core.NKUI;
import Core.Quads;
import Core.Scene;
import Core.SpriteSets;
import Core.TemporalExecutor;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;
import Core.Entities.EntityFlags;
import Core.Entities.EntityHitBoxes;
import Core.Entities.EntityRPGStats;
import Core.Entities.EntityScripts;
import Core.Statics.Statics;
import Core.TileSets.TileSets;
import Core.TileSets.Tiles;
import Game.Items.Inventories;
import Game.Items.ItemComponents;
import Game.Items.Items;
import Game.Items.LootTables;
import Game.Levels.LevelLoadDoors;
import Game.Levels.Levels;
import Game.Levels.MacroLevels;
import Game.Levels.Triggers;
import Physics.ColliderLists;
import Physics.Colliders;
import Physics.Joints;
import Physics.Kinematics;
import Physics.MExpression;
import Renderer.Renderer;
import Renderer.Textures.ImageInfo;

public class EditorUI implements NKUI{

    /*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Global Variables					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private Editor editor;	
	private EditorConsole console;
	private NkPluginFilter defaultFilter = NkPluginFilter.create(Nuklear::nnk_filter_default);
	Quads active;
	Levels currentLevel;
	Scene scene;
	
	public EditorUI(Editor editor){
		
		Configuration.DEBUG_STACK.set(true);
		this.editor = editor;
			
		this.console = editor.getConsole();
		scene = editor.scene;
	
	}
	
	void testModeLayoutElements() {
		
		layoutEditor();
		layoutDebug();
		layoutEntityPythonIntrospector(true);
		layoutSceneInfo();
//		layoutTest();
		
	}
	
	void buildModeLayoutElements(){
		
		active = editor.activeQuad;
		currentLevel = editor.currentLevel;
			
		layoutFilePanel();
		layoutTools();
		console.layout();
		layoutEditor();
		layoutDebug();
		layoutMacroLevelEditor();
		layoutLevelEditor();
		layoutQuadEdit();
		layoutSpriteSetEdit();
		layoutCollisionsEditor();
		layoutStaticObjects();
		layoutStaticColliderEditor();
		layoutStaticInfo();
		layoutSpriteSetSpriteEditor();
		layoutComponentViewer();
		layoutComponentEditor();
		layoutEntityColliderEditor();
		layoutEntityEditor();
		layoutHitBoxEditor();
		layoutItemEditor();
		layoutSpriteSetJointEditor();
		layoutEntityPythonIntrospector(false);
		layoutTriggerEditor();
		layoutRPGStatEditor();
		layoutLoadDoorEditor();
		layoutTileSetEditor();
		layoutTilePlacer();
		layoutSceneInfo();
//		layoutTest();

	}
	
    /*
     * ______________________________________________________
     * |													|
     * |													|
     * |					 File Panel						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	private String filePanelName = "File";
	private NkRect filePanelRect = NkRect.malloc(allocator).x(5).y(5).w(140).h(280);
	boolean firstTimeOpeningFilePanel = true;
	private ByteBuffer printFPSCheck = alloc1(allocator);
		
	void layoutFilePanel(){

		int rectOptions = firstTimeOpeningFilePanel ?
		NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED :
		NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE;

		if(nk_begin(context , filePanelName  ,  filePanelRect , rectOptions)){
			
			firstTimeOpeningFilePanel = false;
					
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Back To Main Menu")) {
				
				editor.switchStateCallback.accept(RuntimeState.GAME);
				editor.leaveEditor();
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "New Script")) {
				
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
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Open Script")) {
				
				if(!Desktop.isDesktopSupported()) editor.say("ERROR: Desktop class not supported on your platform");
				
				Supplier<String> filepath = DialogUtils.newFileExplorer("Select one or more script files" , 5 , 270 , false , true , data + "scripts/");
				TemporalExecutor.onTrue(() -> filepath.get() != null , () -> {
					
					String fp = filepath.get();
					try {
						
						if(fp.contains("|")) {//multiple files
							
							String[] splitFilePaths = fp.split("\\|");
							for(String y : splitFilePaths) {
								
								if(!y.endsWith(".py")) throw new IOException("Script files not selected");
								Desktop.getDesktop().open(new File(y));
								
							}
							
						} else {
							
							if(!fp.endsWith(".py")) throw new IOException("Script files not selected");
							Desktop.getDesktop().open(new File(fp));
							
						}
						
					} catch(IOException e) {
						
						editor.say("ERROR: Invalid file/s selected");					
						e.printStackTrace();
						
					}			
					
				});
					
			}
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "FPS Print" , printFPSCheck)) Engine.printFPS  = toBool(printFPSCheck) ;
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Close Program")) editor.getGlfw().overrideCloseWindow();
						
			nk_layout_row_dynamic(context , 30 , 1);
			nk_text(context , "COLDSTEEL Engine!" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
		}		

		nk_end(context);

	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |						Editor						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	boolean firstTimeOpeningEngineEditor = true;

	private FloatBuffer floatMoveSpeed = allocator.floats(1);
	private boolean showQuadEditor = true;
	private boolean showDebug = true;
	private boolean showCollisionsEditor = true;
	boolean showSpriteSetEdit = true;
	private NkRect editRect = NkRect.malloc(allocator).x(150).y(5).w(275).h(660);
	private ByteBuffer quadsAtCursorCheck = allocator.bytes(toByte(false));
	
	private ByteBuffer backgroundRadio = alloc1(allocator);
	private ByteBuffer foregroundRadio = alloc0(allocator);

	private ByteBuffer buildModeCheck = alloc1(allocator);
	private ByteBuffer testModeCheck = alloc0(allocator);
	private ByteBuffer hybridModeCheck = alloc0(allocator);
	
	ByteBuffer renderDebugCheck = alloc0(allocator);
	//index 0 == render colliders , 1 == render hitboxes, 2 == render joints
	ByteBuffer renderDebugChoosers = allocator.bytes((byte) 1 , (byte) 1 , (byte) 1);
	
	void layoutEditor(){

		int editOptions = firstTimeOpeningEngineEditor ?
				NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR |NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE | NK_WINDOW_MINIMIZED :
				NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR |NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE ;
		
		if(nk_begin(context , "Editor" , editRect , editOptions)){
			
			firstTimeOpeningEngineEditor = false;
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context, "Modes" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "Build Mode" , buildModeCheck)) {
				
				editor.switchTo(EditorMode.BUILD_MODE);
				testModeCheck.put(0 , (byte)0);
				hybridModeCheck.put(0 , (byte)0);
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "Test Mode" , testModeCheck)) {
				
				editor.switchTo(EditorMode.TEST_MODE);
				buildModeCheck.put(0 , (byte)0);
				hybridModeCheck.put(0 , (byte)0);
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "hybrid Mode" , hybridModeCheck)) {
				
				editor.switchTo(EditorMode.HYBRID_MODE);
				buildModeCheck.put(0 , (byte)0);
				testModeCheck.put(0 , (byte)0);
				
			}
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Info" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Editor State: " , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			nk_text(context , editor.editorState().toString() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);

			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Cursor State: " , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			nk_text(context , editor.cursorState().toString() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Camera Position" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			var cameraPos = editor.renderer().getCamera().cameraPosition;			
			nk_text(context , cameraPos.x + ", " + cameraPos.y , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Selection Area Dimensions" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			float[] dims = editor.selection.getDimensions();
			nk_text(context , dims[0] + ", " + dims[1] , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context,  "Options" ,  NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 20 , 1);				
			if(nk_checkbox_label(context , "Spawn Quads At Cursor" , quadsAtCursorCheck)) editor.toggleSpawnAtCursor();			
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Render Debug" , renderDebugCheck));
			
			if(toBool(renderDebugCheck)) {
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 30);
				nk_text_wrap(context , "");
				nk_layout_row_push(context , 235);
				if(nk_checkbox_label(context , "Render Colliders" , renderDebugChoosers.slice(0, 1)));
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 30);
				nk_text_wrap(context , "");
				nk_layout_row_push(context , 235);
				if(nk_checkbox_label(context , "Render HitBoxes" , renderDebugChoosers.slice(1, 1)));
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 30);
				nk_text_wrap(context , "");
				nk_layout_row_push(context , 235);
				if(nk_checkbox_label(context , "Render Joints" , renderDebugChoosers.slice(2, 1)));
				nk_layout_row_end(context);
				
			}
			
			put(foregroundRadio , !editor.background);
			put(backgroundRadio , editor.background);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_radio_label(context , "Edit Background Layers" , backgroundRadio)) editor.background = true;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_radio_label(context , "Edit Foreground Layers" , foregroundRadio)) editor.background = false;
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Set Selection Color")) {

				Supplier<float[]> colors = DialogUtils.newColorChooser("Set Selection Area to this color" , 5 , 270);
				TemporalExecutor.onTrue(() -> colors.get() != null , () -> changeColorTo(editor.selection.vertices , colors.get()[0], colors.get()[1], colors.get()[2]));
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Set Selection Opacity")) {
				
				Supplier<String> opacity = DialogUtils.newInputBox("Input Integer for Opacity Between 0 and 100", 5, 270, textFilter);
				TemporalExecutor.onTrue(() -> opacity.get() != null , () -> editor.selection.makeTranslucent((float) toNumber(opacity.get()) / 100));
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Reset Editor State to Generic")) editor.setState(EditorState.GENERIC);
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Editor Move Speed" , 0 , floatMoveSpeed , 9999 , 0.25000f , 0.25000f);
			
			editor.moveSpeed = floatMoveSpeed.get(0);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(active != null) nk_text(context , "Active Object: " + active.toString() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			else nk_text(context , "No Active Object" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Camera Look At")) {
				
				Supplier<String> xInput = DialogUtils.newInputBox("Input X Coordinate" , 5 , 270);
				Supplier<String> yInput = DialogUtils.newInputBox("Input X Coordinate" , 360 , 120);
				TemporalExecutor.onTrue(() -> xInput != null && yInput != null , () -> 
					editor.renderer().getCamera().lookAt((float)toNumber(xInput.get()) , (float)toNumber(yInput.get())));
								
			}
			
		}		

		nk_end(context);

	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |						Tools						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private String toolsName = "Tools";
	boolean firstTimeOpeningTools = true;
	private NkRect toolsRect = NkRect.malloc(allocator).x(430).y(5).w(140).h(395);
	private ByteBuffer debugCheck = alloc0(allocator);
	private ByteBuffer consoleCheck = alloc0(allocator);
	private ByteBuffer quadEditorCheck = alloc0(allocator);
	private ByteBuffer spriteSetCheck = alloc0(allocator);
	private ByteBuffer collisionsCheck = alloc0(allocator);
	private ByteBuffer staticEditorCheck = alloc0(allocator);
	private ByteBuffer pythonUICheck = alloc0(allocator);
	private ByteBuffer entityCheck = alloc0(allocator);
	private ByteBuffer componentEditorCheck = alloc0(allocator);
	private ByteBuffer componentViewerCheck = alloc0(allocator);
	private ByteBuffer hitboxEditorCheck = alloc0(allocator);
	private ByteBuffer itemEditorCheck = alloc0(allocator);
	
	void layoutTools(){

		int toolsOptions = firstTimeOpeningTools ?
				NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE |NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZED:
				NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE |NK_WINDOW_NO_SCROLLBAR;
		
		if(nk_begin(context , toolsName , toolsRect , toolsOptions)){
			
			firstTimeOpeningTools = false;
		
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Debug" , put(debugCheck , showDebug)))
				showDebug = toggle(showDebug);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Console" , put(consoleCheck , editor.getConsole().showConsole())))
				console.toggleShowConsole();
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "Quad Editor" , put(quadEditorCheck , showQuadEditor)))
				showQuadEditor = toggle(showQuadEditor);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Spriteset Editor" , put(spriteSetCheck , showSpriteSetEdit))) 
				showSpriteSetEdit = toggle(showSpriteSetEdit);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Collision Editor" , put(collisionsCheck , showCollisionsEditor)))
				showCollisionsEditor = toggle(showCollisionsEditor);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Static Obj Editor" , put(staticEditorCheck , showStaticObjects)))
				showStaticObjects = toggle(showStaticObjects);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Python UI" , put(pythonUICheck , editor.showPyUI)))
				editor.showPyUI = toggle(editor.showPyUI);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "Entity Editor" , put(entityCheck , showEntityEditor)))
				showEntityEditor = toggle(showEntityEditor);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "Comp. Editor" , put(componentEditorCheck , showComponentsEditor)))
				showComponentsEditor = toggle(showComponentsEditor);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "Comp. Viewer" , put(componentViewerCheck , showComponentViewer)))
				showComponentViewer = toggle(showComponentViewer);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "HitBox Editor" , put(hitboxEditorCheck , showHitBoxEditor)))
				showHitBoxEditor = toggle(showHitBoxEditor);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context ,  "Item Editor" , put(itemEditorCheck , showItemEditor)))
				showItemEditor = toggle(showItemEditor);
			
		}
		
		nk_end(context);

	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					FPS Graph						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect FPSGraphRect = NkRect.malloc(allocator).x(575 + 485).y(5).w(170).h(210);
	private boolean firstTimeOpeningFPSGraph = true;
	private boolean showFPSGraph = true;
	
	void layoutFPSGraph(){
		
		int options = firstTimeOpeningFPSGraph ? NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_MINIMIZED|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR:
												 NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR;
		
		if(showFPSGraph && nk_begin(context , "Performance" , FPSGraphRect , options)){
			
			firstTimeOpeningFPSGraph = false;
			
			nk_layout_row_dynamic(context , 160 , 1);
			nk_chart_begin(context , NK_CHART_COLUMN , 10 , 0 , 100);
			nk_chart_push(context , 1f);
			nk_chart_push(context , 3f);
			nk_chart_push(context , 5f);
			nk_chart_push(context , 7f);
			nk_chart_push(context , 2f);
			nk_chart_end(context);
//			nk_plot(context , NK_CHART_COLUMN , fpsBuffer , 120 , 0);
			
		}
	
		nk_end(context);

	}


	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					MacroLevel Editor				|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect macroLevelEditorRect = NkRect.malloc(allocator).x(575).y(5).w(300).h(400);
	private boolean showMacroLevelEditor = true;
	boolean firstTimeOpeningMacroLevelEditor = true;
	MacroLevels currentMacroLevel;
	private ByteBuffer showOSTLoopSegments = alloc0(allocator);
	private ByteBuffer showOSTIntroSegments = alloc0(allocator);
	
	void layoutMacroLevelEditor() {
		
		if(!showMacroLevelEditor) return;
		
		int options = firstTimeOpeningMacroLevelEditor ?
			NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_MINIMIZED|NK_WINDOW_NO_SCROLLBAR:
			NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_NO_SCROLLBAR; 
		
		if(nk_begin(context , "Macro Level Editor" , macroLevelEditorRect , options)) {
			
			firstTimeOpeningMacroLevelEditor = false;
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) {
				
				Supplier<String> macroLevelName = DialogUtils.newInputBox("Input Macro Level Name", 5 , 270);
				TemporalExecutor.onTrue(() -> macroLevelName.get() != null , () -> {
					
					String macroLevelNameEdit = macroLevelName.get().replace(" ", "");
					if(currentMacroLevel != null) currentMacroLevel.write();
					currentMacroLevel = editor.createMacroLevel(macroLevelNameEdit);
					
				});
								
			}
			
			if(nk_button_label(context , "Load")) {
				
				Supplier<String> filepath = DialogUtils.newFileExplorer("Select a Macro Level" , 5 , 270 , true, false);
				TemporalExecutor.onTrue(() -> filepath.get() != null, () -> {
					
					currentMacroLevel = new MacroLevels((CharSequence)toNamePath(filepath.get()));
					
				});
				
			}
			
			if(nk_button_label(context , "Delete")) {
				
				Supplier<String> filepath = DialogUtils.newFileExplorer("Select a Macro Level to delete", 5 , 270, true , false);
				TemporalExecutor.onTrue(() -> filepath.get() != null, () -> {
					
					MacroLevels newLevel = new MacroLevels((String) toNamePath(filepath.get()));
					newLevel.delete();
					if(currentMacroLevel.name().equals(newLevel.name())) currentMacroLevel = null;		
					
				});
								
			}
						
			if(currentMacroLevel != null) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Save " + currentMacroLevel.name())) {
					
					currentMacroLevel.write();
					
				}
				
				//these UI let the user choose some files from the assets/sounds folder which will play in the specified manner
				nk_layout_row_dynamic(context , 30  , 1);
				if(nk_button_label(context , "Add OST Intro Segment")) {
					
					Supplier<String> intro = DialogUtils.newFileExplorer("Select OST Intro", 5, 270, true , false , assets + "sounds/");
					TemporalExecutor.onTrue(() -> intro.get() != null, () -> currentMacroLevel.addOSTIntroSegment(toNamePath(intro.get())));
					
				}
				
				if(nk_checkbox_label(context , "Show OST Intro Segments" , showOSTIntroSegments));
				
				if(nk_button_label(context , "Add OST Loop Segment")) {
					
					Supplier<String> loop = DialogUtils.newFileExplorer("Select OST Segment" , 5 , 270 , true , false , assets + "sounds/");
					TemporalExecutor.onTrue(() -> loop.get() != null, () -> currentMacroLevel.addOSTLoopSegment(toNamePath(loop.get())));
					
				}
					
				if(nk_checkbox_label(context , "Show OST Segments" , showOSTLoopSegments));
				
				if(toBool(showOSTIntroSegments)) {
					
					cdNode<String> iter = currentMacroLevel.OSTIntroSegmentsIter();
					for(int i = 0 ; i < currentMacroLevel.OSTIntroSegmentsSize() ; i ++ , iter = iter.next) {
													
						nk_layout_row_dynamic(context , 20 , 2);
						nk_text(context , iter.val , NK_TEXT_ALIGN_LEFT);
						if(nk_button_label(context , "Remove")) {
							
							iter = currentMacroLevel.safeRemoveIntroSegment(iter);
							nk_end(context);
							return;
						
						}
						
					}
					
				}
				
				if(toBool(showOSTLoopSegments)) {
					
					cdNode<String> iter = currentMacroLevel.OSTLoopSegmentsIter();
					for(int i = 0 ; i < currentMacroLevel.OSTLoopSegmentsSize() ; i ++ , iter = iter.next) {
													
						nk_layout_row_dynamic(context , 20 , 2);
						nk_text(context , iter.val , NK_TEXT_ALIGN_LEFT);
						if(nk_button_label(context , "Remove")) iter = currentMacroLevel.safeRemoveLoopSegment(iter);
						
					}
					
				}
				
			}
			
		}
		
		nk_end(context);
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Scene Info						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect sceneInfoRect = NkRect.malloc(allocator).x(1590).y(85).w(300).h(600);
	boolean firstTimeOpeningSceneInfo = true;
	
	private ByteBuffer backgroundQuads = alloc0(allocator);
	private ByteBuffer BackgroundTiles = alloc0(allocator);
	private ByteBuffer backgroundStatics = alloc0(allocator);
	private ByteBuffer entities = alloc0(allocator);
	private ByteBuffer items = alloc0(allocator);
	private ByteBuffer foregroundQuads = alloc0(allocator);
	private ByteBuffer foregroundTiles = alloc0(allocator);
	private ByteBuffer foregroundStatics = alloc0(allocator);
	
	private ByteBuffer backupBackgroundTiles = alloc0(allocator);
	private ByteBuffer backupBackgroundStatics = alloc0(allocator);
	private ByteBuffer backupEntities = alloc0(allocator);
	private ByteBuffer backupItems = alloc0(allocator);
	private ByteBuffer backupForegroundTiles = alloc0(allocator);
	private ByteBuffer backupForegroundStatics = alloc0(allocator);
	
	private void layoutListInfo(AbstractGameObjectLists<? extends Quads> list , Function<Quads , String> text) {
		
		list.forEach(x -> {

			nk_layout_row_begin(context , NK_STATIC , 20 , 2);
			nk_layout_row_push(context , 30);
			nk_text_wrap(context , "");
			nk_layout_row_push(context , 250);
		
			nk_text(context , text.apply(x) , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_end(context);			
			
		});
		
	}

	private void layoutListInfo(CSLinked<Tuple2<String , float[]>> list) {
		
		list.forEachVal(x -> {

			nk_layout_row_begin(context , NK_STATIC , 20 , 2);
			nk_layout_row_push(context , 30);
			nk_text_wrap(context , "");
			nk_layout_row_push(context , 250);
		
			nk_text(context , x.getFirst() + ", pos: " + x.getSecond()[0] + ", " + x.getSecond()[1], NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_end(context);			
			
		});
		
	}

	private void layoutTileListInfo(CSLinked<Tuple3<String , float[] , Integer>> list) {
		
		list.forEachVal(x -> {

			nk_layout_row_begin(context , NK_STATIC , 20 , 2);
			nk_layout_row_push(context , 30);
			nk_text_wrap(context , "");
			nk_layout_row_push(context , 250);
		
			nk_text(context , x.getFirst() + ", pos: " + x.getSecond()[0] + ", " + x.getSecond()[1], NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_end(context);			
			
		});
		
	}

	void layoutSceneInfo() {
		
		int options = firstTimeOpeningSceneInfo ? 
				NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED|NK_WINDOW_NO_SCROLLBAR:
				NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE;
		
		if(nk_begin(context , "Scene Info" , sceneInfoRect , options)) {
			
			firstTimeOpeningSceneInfo = false;
			
			nk_layout_row_dynamic(context , 40 , 1);
			nk_text(context , "Current Scene" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
						
			int symbol = toBool(backgroundQuads) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			nk_selectable_symbol_label(context , symbol , "Background Quads, Order: " + scene.quads1().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backgroundQuads);			
			if(toBool(backgroundQuads))  layoutListInfo(scene.quads1() , (q) -> "Quad " + q.getID());
			
			symbol = toBool(BackgroundTiles) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			nk_selectable_symbol_label(context , symbol , "Background Tiles, Order: " + scene.tiles1().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , BackgroundTiles);		
			if(toBool(BackgroundTiles))  layoutListInfo(scene.tiles1() , (T) -> ((Tiles)T).toStringAndDetails());

			symbol = toBool(backgroundStatics) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			nk_selectable_symbol_label(context , symbol , "Background Statics, Order: " + scene.statics1().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backgroundStatics);			
			if(toBool(backgroundStatics))  layoutListInfo(scene.statics1() , (S) -> ((Statics)S).toString());
				
			symbol = toBool(entities) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_label(context , symbol , "Entities, Order: " + scene.entities().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , entities));			
			if(toBool(entities)) layoutListInfo(scene.entities() , (E) -> ((Entities)E).name() + ", LID: " + ((Entities)E).LID() + ", ID: " + E.getID());

			symbol = toBool(items) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_label(context , symbol , "Items, Order: " + scene.items().renderOrder(), NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , items));			
			if(toBool(items)) layoutListInfo(scene.items() , (I) -> ((Items)I).name() + ", ID: " + I.getID());
				
			symbol = toBool(foregroundQuads) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_label(context , symbol , "Foreground Quads, Order: " + scene.quads2().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , foregroundQuads));			
			if(toBool(foregroundQuads)) layoutListInfo(scene.quads2() , (Q) -> "Quad " + Q.getID());
				
			symbol = toBool(foregroundTiles) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_label(context , symbol , "Foreground Tiles, Order: " + scene.tiles2().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , foregroundTiles));			
			if(toBool(foregroundTiles)) layoutListInfo(scene.tiles2() , (T) -> ((Tiles)T).toStringAndDetails());
				
			symbol = toBool(foregroundStatics) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			nk_selectable_symbol_label(context , symbol , "Foreground Statics, Order: " + scene.statics2().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , foregroundStatics);	
			if(toBool(backgroundStatics)) layoutListInfo(scene.statics2() , (S) -> ((Statics)S).toString());
			
			/*     		*/
			/*  backup  */
			/*     		*/
			
			nk_layout_row_dynamic(context , 40 , 1);
			nk_text(context , "Backup Scene" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			Levels backup = editor.backupLevel;
			
			if(backup == null) {
				
				nk_end(context);
				return;
				
			}
			
			symbol = toBool(backupBackgroundTiles) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1);						
			nk_selectable_symbol_label(context , symbol , "Background Tile Set" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupBackgroundTiles);
			if(toBool(backupBackgroundTiles)) layoutTileListInfo(backup.backgroundTiles());
				
			symbol = toBool(backupBackgroundStatics) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Background Statics" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupBackgroundStatics);
			if(toBool(backupBackgroundStatics)) layoutListInfo(backup.backgroundStatics());
			
			symbol = toBool(backupEntities) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Entities" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupEntities);
			if(toBool(backupEntities)) layoutListInfo(backup.entities());

			symbol = toBool(backupItems) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Items" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupItems);
			if(toBool(backupItems)) layoutListInfo(backup.items());

			symbol = toBool(backupItems) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Items" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupItems);
			if(toBool(backupItems)) layoutListInfo(backup.items());

			symbol = toBool(backupForegroundTiles) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Foreground Tile Set" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupForegroundTiles);
			if(toBool(backupForegroundTiles)) layoutTileListInfo(backup.foregroundTiles());
				
			symbol = toBool(backupForegroundStatics) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Foreground Statics" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupForegroundStatics);
			if(toBool(backupForegroundStatics)) layoutListInfo(backup.foregroundStatics());
					
		}
		
		nk_end(context);
				
	}
			
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Level Editor					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect levelEditorRect = NkRect.malloc(allocator).x(880).y(5).w(300).h(670);
	private boolean showLevelEditor = true;
	boolean firstTimeOpeningLevelEditor = true;
	
	//this trigger reference is here for brevity
	Triggers currentTrigger;
	
	private FloatBuffer triggerWidthMod = allocator.floats(0);
	private FloatBuffer triggerHeightMod = allocator.floats(0);
	private ByteBuffer showTriggerEditorCheck = alloc0(allocator);
	
	ByteBuffer selectTriggersCheck = alloc0(allocator);
	ByteBuffer loadDoorEditorCheck = alloc0(allocator);
	
	Quads currentTriggerBound;
	//TODO: MAKE LIBRARY OF BABEL IN GAME
		
	void layoutLevelEditor() {
		
		int options = firstTimeOpeningLevelEditor ? NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED|NK_WINDOW_NO_SCROLLBAR:
			 										NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR;
		
		if(showLevelEditor && nk_begin(context , "Level Editor" , levelEditorRect , options)) {
			
			firstTimeOpeningLevelEditor = false;
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) {
			
				Supplier<String> name = DialogUtils.newInputBox("Input Level Name" , 5 , 270);
				TemporalExecutor.onTrue(() -> name.get() != null, () -> {
					
					currentLevel = new Levels(name.get());
					editor.setCurrentLevel(currentLevel);
					
				});
				
			}
			
			if(nk_button_label(context , "Load")) editor.loadLevel();
			
			if(nk_button_label(context , "Delete")) {}
			
			if(currentLevel == null) {
				
				nk_end(context);
				return;
				
			}

			editor.setState(EditorState.EDITING_LEVEL);

			if (currentLevel != null) {

				currentLevel.forEachLoadDoor(x -> Renderer.draw_foreground(x.getConditionArea()));
				if (currentTrigger != null) { 
					
					currentTrigger.forEachConditionArea(Renderer::draw_foreground);
					currentTrigger.forEachEffectArea(Renderer::draw_foreground);
				}
				
			}
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , currentLevel.gameName() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			if(currentLevel.macroLevel() != null) {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , currentLevel.macroLevel() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Associate to Macrolevel")) {
				
				Supplier<String> macroLevelPath = DialogUtils.newFileExplorer("Select a Macro Level archive" , 5 , 270 , false , true);
				TemporalExecutor.onTrue(() -> macroLevelPath.get() != null, () -> currentLevel.associate(macroLevelPath.get()));
				
			}					
		
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Save Scene as Level")) {
				
				currentLevel.snapShotScene(scene);
				
			}
			
			if(active != null && active instanceof Entities) {
				
				Entities E = (Entities) active;
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , E.name() + ", LID: " + E.getID() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
			}
						
			nk_layout_row_dynamic(context , 20 , 2);
			if(nk_checkbox_label(context , "Edit Load Doors" , loadDoorEditorCheck)) {}
			
			if(nk_button_label(context , "Add Load Door")) {
				
				Supplier<String> newLoadDoorName = DialogUtils.newInputBox("Input Load Door Name", 5, 270);
				TemporalExecutor.onTrue(() -> newLoadDoorName.get() != null , () -> currentLevel.addLoadDoor(newLoadDoorName.get()));
				
			}
			
			nk_layout_row_dynamic(context , 20 , 2);
			if(nk_checkbox_label(context , "Edit Triggers" , selectTriggersCheck)) {}
			
			if(nk_button_label(context , "Add Trigger")) {
				
				Supplier<String> triggerName = DialogUtils.newInputBox("Trigger Name", 5, 270);
				TemporalExecutor.onTrue(() -> triggerName.get() != null , () -> {
					
					currentLevel.addTrigger(triggerName.get());					
					currentTrigger = null;
					
				});
				
			}
			
			if(currentTrigger != null) if(nk_checkbox_label(context , "Show Trigger Editor" , showTriggerEditorCheck));
			
			if(toBool(selectTriggersCheck)) {
				
				try(MemoryStack stack = allocator.push()) {
					
					nk_layout_row_dynamic(context , 200 , 1);
					if(nk_group_begin(context , "Triggers" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
						
						cdNode<Triggers> iter = currentLevel.triggers().get(0);
						for(int i = 0 ; i < currentLevel.triggers().size() ; i ++ , iter = iter.next) {
							
							if(i % 2 == 0) nk_layout_row_dynamic(context , 20 , 2);
							if(nk_button_label(context , iter.val.name())) currentTrigger = iter.val;
							
						}
						
						nk_group_end(context);
						
					}
					
				}
				
			}
						
			if(currentTrigger == null) {
				
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , currentTrigger.name() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Rename " + currentTrigger.name())) {
				
				Supplier<String> input = DialogUtils.newInputBox("Input a Name for This Trigger" , 5 , 270);
				TemporalExecutor.onTrue(() -> input.get() != null, () -> currentTrigger.name(input.get()));
				
			}
			
			if(nk_button_label(context , "Remove " + currentTrigger.name())) {
				
				editor.removeActive();
				currentLevel.removeTrigger(currentTrigger);
				currentTrigger = null;
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context, "Add Condition Area")) currentTrigger.addConditionArea();
			
			if(nk_button_label(context , "Add Effect Area")) currentTrigger.addEffectArea();
			
			if(currentTriggerBound == null) {
				
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Mod " + currentTrigger.name() + " Condition Area " + currentTriggerBound.getID() + " Width" , -99999f , triggerWidthMod , 9999999f , 1 , 1);

			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Mod " + currentTrigger.name() + " Effect Area " + currentTriggerBound.getID()  + " Height" , -99999f , triggerHeightMod , 9999999f , 1 , 1);
			
			currentTriggerBound.modWidthBi(triggerWidthMod.get(0));
			currentTriggerBound.modHeightUp(triggerHeightMod.get(0));
			put(triggerWidthMod , 0f);
			put(triggerHeightMod , 0f);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Remove " + currentTriggerBound.getID())) {
				
				currentTrigger.remove(currentTriggerBound);
				currentTriggerBound = null;
				editor.activeQuad = null;
				
			}
			
			scene.entities().forEach(x -> {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , x.name() + ", LID:  " + x.LID() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
			});
			
		}
		
		nk_end(context);
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Load Door Editor					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect loadDoorEditorRect = NkRect.malloc(allocator).set(575 , 125 , 300 , 650);
	Levels linkedLevel;
	LevelLoadDoors currentLoadDoor;
	
	void layoutLoadDoorEditor() {
		
		if(currentLevel == null || !toBool(loadDoorEditorCheck)) return;
		
		if(nk_begin(context , "Load Door Editor" , loadDoorEditorRect , 
				NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR))
		{
			
			nk_layout_row_dynamic(context , 200 , 1);
			if(nk_group_begin(context , "Load Doors" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
				
				cdNode<LevelLoadDoors> iter = currentLevel.loadDoorsIter();
				
				for(int i = 0 ; i < currentLevel.numberLoadDoors() ; i ++,  iter = iter.next) {
					
					if(i % 2 == 0) nk_layout_row_dynamic(context , 20 , 2);
					if(nk_button_label(context ,  iter.val.thisLoadDoorName())) currentLoadDoor = iter.val;
					
				}
				
				nk_group_end(context); 
				
			}
			
			if(currentLoadDoor == null) {
				
				nk_end(context);
				return;
								
			}
			
			allocator.push();
			
			var widthModder = allocator.callocFloat(1);
			var heightModder = allocator.callocFloat(1);
					
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Mod Load Door Width" , -999f , widthModder , 999f , 1.0f , 1.0f);
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Mod Load Door Height" , -999f , heightModder , 999f , 1.0f , 1.0f);
			
			currentLoadDoor.modConditionAreaWidth(widthModder.get(0));
			currentLoadDoor.modConditionAreaHeight(heightModder.get(0));
			
			allocator.pop();
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Select Target Level to Load")) {
				
				Supplier<String> targetLevelGetter = DialogUtils.newFileExplorer("Select Level to Link To" , 5 , 270 , false , false);
				TemporalExecutor.onTrue(() -> targetLevelGetter.get() != null ,	() -> {
					
					linkedLevel = new Levels((CharSequence)targetLevelGetter.get());
					currentLoadDoor.linkToLevel((String)toLocalDirectory(targetLevelGetter.get()));	
						
				});
				
			}
			
			if(linkedLevel == null) {
				
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 170 , 1);
			if(nk_group_begin(context , linkedLevel.gameName() + " Load Loors" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
				
				cdNode<LevelLoadDoors> targetLevelIter = linkedLevel.loadDoorsIter();
				for(int i = 0 ; i < linkedLevel.numberLoadDoors() ; i ++ , targetLevelIter = targetLevelIter.next) {
					
					if(i % 2 == 0) nk_layout_row_dynamic(context , 20 , 2);				
					if(nk_button_label(context , "Load Door: " + targetLevelIter.val.thisLoadDoorName())) {
						
						currentLoadDoor.linkToLoadDoor(targetLevelIter.val.thisLoadDoorName());
						
					}
					
				}
				
				nk_group_end(context);
				
			}
			
			if(currentLoadDoor.linkedLevel() != null && currentLoadDoor.linkedLoadDoorName() != null) {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Linked Level: " + currentLoadDoor.linkedLevel() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_CENTERED);
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Load Door: " + currentLoadDoor.linkedLoadDoorName() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_CENTERED);
				
			}
			
		}
		
		nk_end(context);
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Trigger Editor						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	private NkRect triggerEditor = NkRect.malloc(allocator).set(1185 , 360 , 300 , 700);
	boolean selectSomething = false;
	
	void layoutTriggerEditor() {
		
		if(!toBool(showTriggerEditorCheck)) return;
		
		if(nk_begin(context , "Trigger Editor" , triggerEditor , 
			NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR)) {

			allocator.push();
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Save Trigger to Script")) editor.saveTriggerToScript(currentTrigger.name());
			
			allocator.pop();
			
		}
		
		nk_end(context);
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Debug Info						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	boolean firstTimeOpeningDebugInfo = true;
	private NkRect debugRect = NkRect.malloc(allocator).x(1185).y(5).w(345).h(455);
	private DecimalFormat fpsFormat = new DecimalFormat();
	private ArrayList<String> allocations = new ArrayList<String>();
	
	
	void layoutDebug(){

		if(showDebug){

			int debugOptions = firstTimeOpeningDebugInfo ?
				NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED:
				NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR
				|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE;
						
			if(nk_begin(context , "Debug" , debugRect , debugOptions)){
				
				firstTimeOpeningDebugInfo = false;
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_label(context , "Iteration Rate (millis): " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , fpsFormat.format(Engine.iterationRateLastSecond()) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Frames last second: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , Integer.toString(Engine.framesLastSecond()) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Frames this second: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , Integer.toString(Engine.currentFrame()) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);				
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Average Frames Per Second: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , Integer.toString(Engine.averageFramerate()) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);				
				nk_text(context , "Render Time (millis): " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , fpsFormat.format(editor.renderer().getRenderTime())  , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
					
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Frames per Ticks: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + scene.entities().frameInterval() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Ticks last second: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + scene.entities().ticksLastSecond() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Current Number Scripts:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + scene.entities().numberScripts() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Kinematic Forces: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + Kinematics.size() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Temporal Executor Objects:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + TemporalExecutor.totalSize() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_label(context , "Draws: " , NK_TEXT_ALIGN_LEFT);
				nk_label(context ,  Integer.toString(editor.renderer().getNumberDrawCalls()) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Available kilos on JVM : " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + Runtime.getRuntime().totalMemory() / 1024 , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);

				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Free kilos on JVM : " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + Runtime.getRuntime().freeMemory() / 1024 , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Allocated kilos on JVM: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
	
				nk_layout_row_dynamic(context , 30 , 1);		
				if(nk_button_label(context , "Get UI Memory Details")) {
					
					console.say("Allocated (bytes): " + allocator.getSize());
					console.say("Remaining (bytes): " + allocator.getPointer());
									
				}
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Print Allocations")) {
										
					allocations.clear();

					MemoryAllocationReport rep = (address , memory , threadID , threadName , element) ->{
						
						allocations.add("At " + address + ": " + memory + " bytes in " + threadName);
						
					};

					memReport(rep);
					
					for(int i = 0 ; i < allocations.size()  ; i ++) editor.say(allocations.get(i));

				}
				
				if(nk_button_label(context , "Print Stack Traces")) {
					
					MemoryAllocationReport rep = (address , memory , threadID , threadName , element) -> {
						
						System.err.println("At " + address + ": " + memory + " bytes in " + threadName);
						for(StackTraceElement x : element) System.err.println(x);
						
					};
					
					memReport(rep);
					
				}
								
			}
			
			nk_end(context);

		}

	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Quad Editor						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	
	private String editQuadName = "Edit Active Quad";
	private NkRect editQuadRect = NkRect.malloc(allocator).x(5).y(45).w(320).h(885);
	boolean firstTiemOpeningQuadEditor = true;
	
	private ByteBuffer topLeftRadioButtons = allocator.bytes(toByte(false));
	private ByteBuffer topRightRadioButtons = allocator.bytes(toByte(false));
	private ByteBuffer bottomLeftRadioButtons = allocator.bytes(toByte(false));
	private ByteBuffer bottomRightRadioButtons = allocator.bytes(toByte(false));
	
	private NkPluginFilter textFilter = NkPluginFilter.create(Nuklear::nnk_filter_float);
		
	private NkColorf selectedColor = NkColorf.malloc(allocator);	
	private FloatBuffer quadWidthMod = allocator.callocFloat(1);
	private FloatBuffer quadHeightMod = allocator.callocFloat(1);
	private FloatBuffer quadTranslucency = allocator.callocFloat(1);
	
	void layoutQuadEdit(){

		if(showQuadEditor){

			int editQuadOptions = firstTiemOpeningQuadEditor ?
					NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED:
					NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE;
			
			if(nk_begin(context , editQuadName , editQuadRect , editQuadOptions)){

				firstTiemOpeningQuadEditor = false;
				
				if(active == null || !(active.getClass() == Quads.class)) {
					
					nk_layout_row_dynamic(context , 30 , 1);
					nk_text_wrap(context , "Select a quad");
					nk_end(context);
					return;
					
				}
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Add Quad")) editor.addQuad();
				if(nk_button_label(context, "Delete Quad")) {
					
					editor.removeActive();
					nk_end(context);
					return;
					
				}
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context, "Texture")) editor.textureActive();
				if(nk_button_label(context , "Remove Texture")) editor.removeActiveTexture();
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Resize To Texture")) editor.resetDimensionsAndUVs();
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context, "Remove Color")){
					
					Supplier<float[]> colors = DialogUtils.newColorChooser("Remove Color" , 5 , 270);
					TemporalExecutor.onTrue(() -> colors.get() != null , () -> editor.removeActiveColor(colors.get()[0] , colors.get()[1] , colors.get()[2]));
										
				}
								
				if(nk_button_label(context , "Apply Filter")) {
				
					Supplier<float[]> colors = DialogUtils.newColorChooser("Filter Color" , 5 , 270);
					TemporalExecutor.onTrue(() -> colors.get() != null , () -> editor.filterActiveColor(colors.get()[0] , colors.get()[1] , colors.get()[2]));
					
				}
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context, "Move To Back")) editor.moveActiveToBack();				
				if(nk_button_label(context, "Move Backwards"))editor.moveActiveBackward();				
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context, "Move To Front")) editor.moveActiveToFront();
				if(nk_button_label(context, "Move Forward")) editor.moveActiveForward();					
				
				nk_layout_row_dynamic(context , 30 , 1);				
				nk_property_float(context , "Mod Quad Width" , -10 , quadWidthMod  , 10 , 1 , 1);
				active.modWidthBi(quadWidthMod .get(0));
				quadWidthMod .put(0 , 0);
				
				nk_layout_row_dynamic(context , 30 , 1);				
				nk_property_float(context , "Mod Quad Height" , -10 , quadHeightMod , 10 , 1 , 1);
				active.modHeightUp(quadHeightMod.get(0));				
				quadHeightMod.put(0 , 0);
				
				quadTranslucency.put(0 , active.getTranslucency());
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Translucency" , 0f , quadTranslucency , 1f , 0.01f , 0.01f);
				active.makeTranslucent(quadTranslucency.get(0));
				
				nk_layout_row_dynamic(context , 30 , 4);
				if(nk_radio_text(context , "Top L" , topLeftRadioButtons)){
					
					put(topRightRadioButtons , false);
					put(bottomLeftRadioButtons , false);					
					put(bottomRightRadioButtons , false);
					
				}
				
				if(nk_radio_text(context , "Top R" , topRightRadioButtons)){
					
					put(topLeftRadioButtons , false);
					put(bottomLeftRadioButtons , false);
					put(bottomRightRadioButtons , false);
					
				}
				
				if(nk_radio_text(context , "Bot L" , bottomLeftRadioButtons)){
					
					put(topLeftRadioButtons , false);
					put(topRightRadioButtons , false);
					put(bottomRightRadioButtons , false);
					
				}
				
				if(nk_radio_text(context , "Bot R" , bottomRightRadioButtons)){
					
					put(topLeftRadioButtons , false);
					put(topRightRadioButtons , false);
					put(bottomLeftRadioButtons , false);
					
				}
								
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context, "Color Quad")){
					
					int corner = -1;
					
					if(topLeftRadioButtons.get(0) == 1) corner = 2;
					else if(topRightRadioButtons.get(0) == 1) corner = 3;
					else if (bottomRightRadioButtons.get(0) == 1)corner = 1;
					else if (bottomLeftRadioButtons.get(0) == 1) corner = 0;
					
					console.say("R: " + Float.toString(selectedColor.r()));
					console.say("G: " + Float.toString(selectedColor.g()));
					console.say("B: " + Float.toString(selectedColor.b()));
					
					active.quickChangeColor(corner, selectedColor.r(), selectedColor.g(), selectedColor.b());
					
				}
								
				nk_layout_row_dynamic(context , 10 , 1);
				nk_layout_row_dynamic(context , 250 , 1);
				if(nk_color_pick(context , selectedColor , NK_RGB)){}
				
			}
			
		
			nk_end(context);

		}

	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Sprite Set Editor 					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	private String spriteSetEditName = "Sprite Set Editor";
	private NkRect spriteSetEditRect = NkRect.malloc(allocator).x(330).y(45).w(340).h(475);
	boolean firstTimeOpeningSpriteSetEditor = true;

	SpriteSets activeSet;
	private float[] swapInterval = {1};
	private boolean swapSprites = false;
		
	private ByteBuffer leftDefaultDirRadio = allocator.bytes(toByte(false));
	private ByteBuffer rightDefaultDirRadio = allocator.bytes(toByte(false));
	private ByteBuffer upDefaultDirRadio = allocator.bytes(toByte(false));
	private ByteBuffer downDefaultDirRadio = allocator.bytes(toByte(false));
	private ByteBuffer showSpriteEditorCheck = allocator.bytes(toByte(false));
	private ByteBuffer runAnimCheck = allocator.bytes(toByte(false));
		
	void layoutSpriteSetEdit(){

		if(showSpriteSetEdit){

			int spriteSetEditOptions = firstTimeOpeningSpriteSetEditor?
					NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE | NK_WINDOW_MINIMIZED:
					NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE | NK_WINDOW_NO_SCROLLBAR;			
			
			if(nk_begin(context , spriteSetEditName , spriteSetEditRect , spriteSetEditOptions)){
				
				firstTimeOpeningSpriteSetEditor = false;
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "New Sprite Set")) {
					
					Supplier<String> name = DialogUtils.newInputBox("Spriteset Name", 5 , 270);
					TemporalExecutor.onTrue(() -> name.get() != null, () -> {

						activeSet = new SpriteSets((CharSequence) name.get());
						activeSpriteID = -1;
						
					});
					
				}

				if(nk_button_label(context, "Load Sprite Set")) {
					
					Supplier<String> filepath = DialogUtils.newFileExplorer("Select a Sprite Set", 5 , 270 , false , false , data + "spritesets/");
					TemporalExecutor.onTrue(() -> filepath.get() != null, () -> {
						
						activeSet = new SpriteSets(toNamePath(filepath.get()));	
						activeSpriteID = -1;
						swapInterval[0] = activeSet.getSwapInterval();	
						
					});
						
				}
				
				Quads markup = active;
				
				if(activeSet != null && markup != null && markup.isTextured()) {

					if(!toBool(jointEditorCheck)) editor.setState(EditorState.EDITING_ANIMATION);
					
					nk_layout_row_dynamic(context , 30 , 1);
					nk_text(context, activeSet.name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);

					nk_layout_row_dynamic(context,  30 , 2);
					if(nk_button_label(context, "Save Set")) activeSet.write();
					if(nk_button_label(context, "Delete Set")) {
						
						activeSet.delete();
						activeSet = null;
						activeSpriteID = -1;
						nk_end(context);
						return;
						
					}

					editor.snapSelectionArea();
					markup.roundVertices();
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_button_label(context , "Reset Dimensions And UVs")) editor.resetDimensionsAndUVs();
					
					nk_layout_row_dynamic(context , 30 , 1);
					nk_property_float(context , "Swap Rate" , 0.0f , swapInterval , 1000.0f , 0.01f , 0.01f);
					
					activeSet.setSwapInterval(swapInterval[0]);
					
					nk_layout_row_dynamic(context , 30 , 1);
					nk_text(context ,"Default Direction" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
					
					nk_layout_row_dynamic(context , 30 , 4);
					if(nk_radio_label(context , "Left" , put(leftDefaultDirRadio , activeSet.defaultDirection().equals(Direction.LEFT)))) 
						activeSet.setDefaultDirection(Direction.LEFT);
					
					if(nk_radio_label(context, "Right" , put(rightDefaultDirRadio , activeSet.defaultDirection().equals(Direction.RIGHT)))) 
						activeSet.setDefaultDirection(Direction.RIGHT);
					
					if(nk_radio_label(context , "Up" , put(upDefaultDirRadio , activeSet.defaultDirection().equals(Direction.UP))))
						activeSet.setDefaultDirection(Direction.UP);
					
					if(nk_radio_label(context , "Down" , put(downDefaultDirRadio , activeSet.defaultDirection().equals(Direction.DOWN))))
						activeSet.setDefaultDirection(Direction.DOWN);
					
					nk_layout_row_dynamic(context , 30 , 1);		
					if(nk_button_label(context , "Reverse Nonroot Sprites")) activeSet.appendReverseFromEnd();
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_checkbox_label(context , "Toggle Set Sprite Editor" , put(showSpriteEditorCheck , showSpriteSetSpriteEditor)))
						showSpriteSetSpriteEditor = toggle(showSpriteSetSpriteEditor);
					
					nk_layout_row_dynamic(context , 30 , 1);
					
					//gets the U and V coordinates from the proportion of the selection area's position  
					//over the object divided by the total width or height of the quad.						
					if(nk_button_label(context , "Save Selection Area")) {
						
						float[] vertices = markup.getData();
						float[] selection = editor.selection.vertices;
						
						float leftUDistance = selection[27] - vertices[27];
						float rightUDistance = selection[0] - vertices[27];
						
						float[] qDims = {markup.getWidth() , markup.getHeight()};
						
						float leftU = leftUDistance / qDims[0];
						float rightU = rightUDistance / qDims[0];
						
						float bottomVDistance = selection[1] - vertices[1];
						float topVDistance = selection[10] - vertices[1];
						
						float bottomV = bottomVDistance / qDims[1];
						float topV = topVDistance / qDims[1];
						
						float[] selectionDims = editor.selection.getDimensions();
						
						if(leftU < 0.0f) leftU = 0.0f;
						if(rightU > 1.0f) rightU = 1.0f;
						if(topV > 1.0f) topV = 1.0f;
						if(bottomV < 0f) bottomV = 0f;
						
						activeSet.storeSprite(leftU, rightU , topV, bottomV , Math.round(selectionDims[0] / 2) , Math.round(selectionDims[1] / 2f));
						
					}
					
					nk_layout_row_dynamic(context , 30 , 2);
																	
					if(nk_button_label(context , "Width Fix")) {

						ImageInfo info = active.getTexture().imageInfo;
						activeSet.fixWidth(info.width());
					}
					
					if(nk_button_label(context ,  "Height Fix")) {

						ImageInfo info = active.getTexture().imageInfo;
						activeSet.fixHeight(info.height());
					}
											
					nk_layout_row_dynamic(context , 30 , 1);
					
					if(nk_checkbox_label(context , "Play Animation" , put(runAnimCheck , swapSprites))) {
							
						if(activeSet.getNumberSprites() > 0) {

							activeSet.setRunSpriteSet(true);							
							swapSprites = !(showSpriteSetSpriteEditor) && toggle(swapSprites);
							
						}
						
					}					
					
					if(swapSprites){
						
						float[] currentValues = activeSet.swapSprite();
						markup.swapSprite(currentValues);
						
					} 
					
				} else if (activeSet == null) {
					
					nk_layout_row_dynamic(context , 30 , 1);
					nk_text_wrap(context , "Select a sprite set");
					
				} else if (active == null) {
					
					nk_layout_row_dynamic(context , 30 , 1);
					nk_text_wrap(context , "Select a quad");
										
				} else if (!active.isTextured()) {
					
					nk_layout_row_dynamic(context , 30 , 1);
					nk_text_wrap(context , "Select a texture for " + active.toString());
					
				}
									
			}
			
			nk_end(context);

		}

	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |			Sprite Set Sprite Editor				|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	private NkRect spritesetSpriteEditorRect = NkRect.malloc(allocator).x(5).y(45).w(320).h(720);
	private int spritesetSpriteEditorOptions = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR;
	boolean showSpriteSetSpriteEditor = false;
	int activeSpriteID = -1;
	boolean showSpriteSetJointEditor = false;
	private ByteBuffer jointEditorCheck = allocator.bytes(toByte(false));
	
	private void layoutSpriteSetSpriteEditor(){
		
		if(showSpriteSetSpriteEditor && activeSet != null && active != null && active.isTextured()){
			
			if(nk_begin(context , "Sprite Set Sprite Editor" , spritesetSpriteEditorRect , spritesetSpriteEditorOptions)){
				
				activeSet.setRunSpriteSet(false);
				
				nk_layout_row_dynamic(context , 200 , 1);
				if(nk_group_begin(context , "Select Sprite To Edit" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)){
					
					for(int i = 0 ; i < activeSet.getNumberSprites() ; i++){
						
						if(i % 3 == 0) nk_layout_row_dynamic(context , 20 , 3);							
						if(nk_button_label(context , "Sprite " + i)) activeSpriteID = i;
						
					}
					
					nk_group_end(context);
					
				}
				
				if(activeSpriteID == -1) {
					
					nk_layout_row_dynamic(context , 30 , 1);
					nk_text_wrap(context , "Select a sprite to edit");
				
					nk_end(context);
					return;
				
				}
							
				float[] activeSprite = activeSet.getSprite(activeSpriteID);
				boolean modifiesHitBox = activeSprite.length % 3 != 0;
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Modifying Sprite " + activeSpriteID , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Delete")) {
					
					activeSet.deleteSprite(activeSpriteID);
					activeSpriteID = -1;
					nk_end(context);
					return;
					
				}
				 
				if(nk_button_label(context , "Update Sprite " + activeSpriteID)) {
										
					float[] UV = active.getUVs();
					float[] spriteData;
					int size = 6;
					int numberJoints = 0;
					int hitboxActivatedIndex = -2;
					//get the size of the sprite array
					for(int i = 0 ; i < editor.jointMarkers.length() ; i ++) if (editor.jointMarkers.get(i) != null) numberJoints ++;
					if(editor.jointMarkers.size() > 0) size += (numberJoints * 3);
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
					
					if(editor.jointMarkers.size() > 0) {
						
						//i is iterator of joint markers, j is the offsetinth the sprite array
						
						Joints currentJoint = editor.getJoint(0);
						float[] jointMid;
						float[] quadData = editor.activeQuad.getData();
						
						for(int i = 0 , j = 6 ; i < editor.jointMarkers.size() ; i ++ , currentJoint = editor.getJoint(i)) {
							
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
					
					activeSet.replaceSprite(activeSpriteID , spriteData);							
					activeSet.write();
					activeSprite = spriteData;					
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context, "Replace With Selection")) {
					
					float[] vertices = active.getData();
					float[] selection = editor.selection.vertices;
					
					float leftUDistance = selection[27] - vertices[27];
					float rightUDistance = selection[0] - vertices[27];
					
					float[] qDims = {active.getWidth() , active.getHeight()};
					
					float leftU = leftUDistance / qDims[0];
					float rightU = rightUDistance / qDims[0];
					
					float bottomVDistance = selection[1] - vertices[1];
					float topVDistance = selection[10] - vertices[1];
					
					float bottomV = bottomVDistance / qDims[1];
					float topV = topVDistance / qDims[1];
					
					float[] selectionDims = editor.selection.getDimensions();
					
					if(leftU < 0.0f) leftU = 0.0f;
					if(rightU > 1.0f) rightU = 1.0f;
					if(topV > 1.0f) topV = 1.0f;
					if(bottomV < 0f) bottomV = 0f;
					
					activeSet.replaceSprite(activeSpriteID , leftU, rightU , topV, bottomV , Math.round(selectionDims[0] / 2) , Math.round(selectionDims[1] / 2f));
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Activate Hit Box")) {
				
					Supplier<String> hitboxToActivate = DialogUtils.newInputBox("Input an Integer Index of a HitBox to Activate", 5, 270, textFilter);
					TemporalExecutor.onTrue(() -> hitboxToActivate.get() != null , () -> {
						
						try {
							
							int hitbox = (int)toNumber(hitboxToActivate.get());
							float[] sprite = activeSet.getSprite(activeSpriteID);
							int length = sprite.length % 3 == 0 ? sprite.length + 1 : sprite.length;
							float[] newSprite = new float[length];
							System.arraycopy(sprite, 0, newSprite, 0, sprite.length);
							newSprite[newSprite.length -1] = hitbox;
							activeSet.replaceSprite(activeSpriteID, newSprite);
							
						} catch(Exception e) {
							
							editor.say(hitboxToActivate.get() + " not castable to int");
							
						}
						
						
					});
					
				}				
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Swap to Sprite " + activeSpriteID)) {
					
					active.swapSpriteFast(activeSprite);
					
				}
				
				nk_layout_row_dynamic(context , 20 , 2);
				if(nk_checkbox_label(context , "Joint Editor" , put(jointEditorCheck , showSpriteSetJointEditor))) 
					showSpriteSetJointEditor = toggle(showSpriteSetJointEditor);
				
				nk_layout_row_dynamic(context , 20 , 2);
				
				nk_text(context , "Saving Joint Marker: " , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_LEFT);
				nk_text(context , editor.jointMarkers.size() > 0 ? "true":"false" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
									
				//set up a way to know what is being saved
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Granular Details" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				//display UV coordinates for selected sprite
				nk_layout_row_dynamic(context , 20 , 4);
				nk_text(context , "Left U:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , "" + activeSprite[0] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);				
				nk_text(context , "Right U:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , "" + activeSprite[1] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				
				nk_layout_row_dynamic(context , 20 , 4);
				nk_text(context , "Top V:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , "" + activeSprite[2] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);				
				nk_text(context , "Bottom V" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , "" + activeSprite[3] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
						
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Width: " + activeSprite[4] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , "Height: " + activeSprite[5] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			
				Entities E;				
				//end the element if we are not selecting an entity or we are selecting an entity who does not have animations
				if(!(active instanceof Entities) || !(E = (Entities) active).has(ECS.ANIMATIONS)) {

					nk_end(context);
					return;
					
				}
				
				Object[] comps = E.components();
				
				boolean hasSet = ((EntityAnimations)(comps[Entities.AOFF])).hasSpriteSet(activeSet);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , E.name() + " has " + activeSet.name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , Boolean.toString(hasSet) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				if(!hasSet) {
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_button_label(context , "Add Set")) {
						
						((EntityAnimations)(comps[Entities.AOFF])).add(activeSet);
						E.write();
						
					}
					
				}
				
				if(E.has(ECS.HITBOXES)) {
				
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Modifies a Hitbox" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
					nk_text(context , Boolean.toString(modifiesHitBox) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
					
					if(modifiesHitBox) {
						
						nk_layout_row_dynamic(context , 20 , 2);
						nk_text(context , "Modifies Hitbox: ", NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
						nk_text(context , (int)activeSprite[activeSprite.length - 1] + ", " + ((EntityHitBoxes)comps[Entities.HOFF]).get((int) activeSprite[activeSprite.length - 1]).name(), NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
						
					}
					
				}				
				
				boolean modifies = activeSprite.length % 3 != 0 && activeSprite.length > 7 || activeSprite.length > 6;
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Modifies Joints:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , Boolean.toString(modifies) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				if(modifies) {
					
					ArrayList<float[]> joints = activeSet.getJoints(activeSpriteID);
					
					float[] jointSprites;
					
					for(int i = 0 ; i < joints.size() ; i ++) {
						
						jointSprites = joints.get(i);
						
						nk_layout_row_dynamic(context , 20 , 3);
						nk_text(context , "ID: " + jointSprites[0] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
						nk_text(context , "X Off: " + jointSprites[1] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
						nk_text(context , "Y Off: " + jointSprites[2] , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);
						
					}
					
				}
				
			
				
			}
			
			nk_end(context);
			
		}

	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |			Sprite Set Joint Editor					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
		
	private NkRect spritesetJointEditorRect = NkRect.malloc(allocator).x(330).y(525).w(340).h(220);
	
	/*
	 
	 Lay the joint over the quad where we want it to be. When this UI element is open, we are not allowed to select another quad, 
	 and we can only move the joint where it goes.
	 
	 */
	
	void layoutSpriteSetJointEditor() {
		
		if(showSpriteSetJointEditor && active != null) {
			
			if(nk_begin(context , "Sprite Set Joint Editor" , spritesetJointEditorRect ,  spritesetSpriteEditorOptions^NK_WINDOW_NO_SCROLLBAR)){
				
				float[] activeQuadData = active.getData();
								
				if(activeSpriteID == -1) {
					
					nk_end(context);
					return;
					
				}
				
				editor.setState(EditorState.EDITING_JOINT);
								
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Sprite " + activeSpriteID + " from " + activeSet.name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Add Joint Marker")) {
										
					editor.addJoint();
					int next = editor.jointMarkers.size() - 1;
					editor.getJoint(next).moveTo(active);
					
				}
				
				if(nk_button_label(context , "Clear Joints")) editor.removeAllJoints();
				
				if(editor.jointMarkers.size() == 0) {
					
					nk_end(context);
					return;
					
				}
								
				if(editor.activeJoint != null) {
										
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_button_label(context , "Remove Joint")) editor.removeActiveJoint();
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Active Joint:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
					nk_text(context , "" + editor.activeJoint.getID() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
																
					for(int i = 0 ; i < editor.jointMarkers.size() ; i ++) {
						
						if(editor.getJoint(i) == null) continue; 
						else {
							
							float[] markerMid = editor.getJoint(i).getMidpoint();
							float x = markerMid[0] - activeQuadData[9];
							float y = markerMid[1] - activeQuadData[10];
																
							nk_layout_row_dynamic(context , 20 , 3);
							nk_text(context , "Pos: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
							nk_text(context , "X: " + x , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
							nk_text(context , "Y: " + y , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
							
						}
					
					}
					
				}					
										
			} 
			
			nk_end(context);
						
		} 
		
	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Collisions Editor					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect collisionsEditorRect =  NkRect.malloc(allocator).x(675).y(45).w(300).h(480);
	boolean firstTimeOpeningCollisionsEditor = true;

	private ByteBuffer renderCollidersCheck = allocator.bytes(toByte(true));

	void layoutCollisionsEditor(){

		if(showCollisionsEditor){

			int physicsEditorOptions = firstTimeOpeningCollisionsEditor ?
					NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZED:
					NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR;

			if(nk_begin(context , "Collisions Editor" , collisionsEditorRect , physicsEditorOptions)){
				
				firstTimeOpeningCollisionsEditor = false;
				var CList = scene.colliders();
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_checkbox_label(context , "Render Colliders" , put(renderCollidersCheck , ColliderLists.shouldRender()))) 
					ColliderLists.toggleShouldRender();
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "New Collider")) editor.addCollider();

				if(active == null || active.getClass() != Colliders.class) {
					
					nk_layout_row_dynamic(context , 30 , 1);
					nk_text_wrap(context,  "Select a Collider");
					nk_end(context);
					return;
					
				}
				
				Colliders activeCollider = (Colliders) active;
				
				try(MemoryStack stack = allocator.push()){
					
					ByteBuffer triangleRadios = stack.bytes(
						toByte(activeCollider.isUpperRightTriangle()) ,
						toByte(activeCollider.isUpperLeftTriangle()) ,
						toByte(activeCollider.isLowerRightTriangle()) ,
						toByte(activeCollider.isLowerLeftTriangle()) ,
						toByte(activeCollider.isPlatform())
					);
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_radio_label(context , "Upper Right Triangle" , triangleRadios.slice(1 , 1))) activeCollider.makeUpperRightTriangle();
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_radio_label(context , "Upper Left Triangle" , triangleRadios.slice(0 , 1))) activeCollider.makeUpperLeftTriangle();
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_radio_label(context , "Lower Right Triangle" , triangleRadios.slice(3 , 1))) activeCollider.makeLowerRightTriangle();
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_radio_label(context , "Lower Left Triangle" , triangleRadios.slice(2 , 1))) activeCollider.makeLowerLeftTriangle();
					
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_radio_label(context , "Platform" , triangleRadios.slice(4 , 1))) activeCollider.makePlatform();
					
					nk_layout_row_dynamic(context , 30 , 2);
					if(nk_button_label(context , "Set Width")) {
						
						Supplier<String> width = DialogUtils.newInputBox("Set Width", 5, 270, DialogUtils.NUMBER_FILTER);
						TemporalExecutor.onTrue(() -> width.get() != null , () -> activeCollider.setWidth(Float.parseFloat(width.get())));
						
					}
					
					if(nk_button_label(context , "Set Height")) {
						
						Supplier<String> height = DialogUtils.newInputBox("Set Height", 5, 270, DialogUtils.NUMBER_FILTER);
						TemporalExecutor.onTrue(() -> height.get() != null , () -> activeCollider.setHeight(Float.parseFloat(height.get())));
						
					}
					
					FloatBuffer dimMod = stack.callocFloat(2);
					
					nk_layout_row_dynamic(context , 30 , 1);				
					nk_property_float(context , "Mod Collider Width" , -15f , dimMod.slice(0 , 1) , 15f , 1f , 1f);
					
					nk_layout_row_dynamic(context , 30 , 1);				
					nk_property_float(context , "Mod Collider Height" , -15f , dimMod.slice(1 , 1) , 15f , 1f , 1f);
					
					activeCollider.modWidth(dimMod.get());
					activeCollider.modHeight(dimMod.get());
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Clone Active Collider")) CList.copyCollider(activeCollider.getID());
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Reset State")) activeCollider.resetState();
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Delete Active Collider")) editor.removeActive();	
				
			}
			
			nk_end(context);

		}

	}


	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Static Objects					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	private NkRect staticObjectEditorRect = NkRect.malloc(allocator).x(980).y(45).w(300).h(860);
	
	boolean firstTimeOpeningStaticObjects = true;
	private boolean showStaticObjects = true;

	private NkColorf staticColorChooser = NkColorf.malloc(allocator);
	private ByteBuffer upRight = alloc0(allocator);
	private ByteBuffer upLeft = alloc0(allocator);
	private ByteBuffer downRight = alloc0(allocator);
	private ByteBuffer downLeft = alloc0(allocator);
	private ByteBuffer staticParallaxCheck = alloc0(allocator);
	private ByteBuffer staticInfoCheck = alloc0(allocator);
	private ByteBuffer editCollidersCheck = alloc0(allocator);
	private FloatBuffer xParallax = allocator.floats(1f);
	private FloatBuffer yParallax = allocator.floats(1f);
	private FloatBuffer widthAdjust = allocator.callocFloat(1);
	private FloatBuffer heightAdjust = allocator.callocFloat(1);
	Statics activeAsStatic = null;

	void layoutStaticObjects(){

		int staticObjectOptions = firstTimeOpeningStaticObjects ?
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZED:
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR;

		if(!showStaticObjects) return;		
		
		if(nk_begin(context , "Static Object Editor" , staticObjectEditorRect , staticObjectOptions)){
			
			firstTimeOpeningStaticObjects = false;
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) editor.addStatic();
			
			if(nk_button_label(context , "Load")) editor.loadStatic();
			
			if(nk_button_label(context , "Delete")) {
				
				editor.removeActive();
				nk_end(context);
				return;
				
			}

			if(active instanceof Statics) activeAsStatic = (Statics) active;
			
			if(activeAsStatic == null) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context , "Select a Static");
				nk_end(context);
				return;
				
			} 
			
			xParallax.put(0 , activeAsStatic.getViewOffsetX());
			yParallax.put(0 , activeAsStatic.getViewOffsetY());
			
			editor.setState(EditorState.EDITING_STATIC);
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , activeAsStatic.name() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context, "Save Static")) activeAsStatic.write();
			
			if(nk_button_label(context, "Remove Static")) {
				
				editor.removeActive();
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Texture")) editor.textureActive();
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Remove Color")) {
				
				Supplier<float[]> color = DialogUtils.newColorChooser("Remove Color", 5, 270);
				TemporalExecutor.onTrue(() -> color.get() != null, () -> editor.removeActiveColor(color.get()[0] , color.get()[1] , color.get()[2]));
				
			}
			
			if(nk_button_label(context , "Filter Color")) {
				
				Supplier<float[]> color = DialogUtils.newColorChooser("Filter Color", 5, 270);
				TemporalExecutor.onTrue(() -> color.get() != null, () -> editor.filterActiveColor(color.get()[0] , color.get()[1] , color.get()[2]));
				
			}
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Move To Front")) editor.moveActiveToFront();
			if(nk_button_label(context , "Move To Back")) editor.moveActiveToBack();

			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Move Forward")) editor.moveActiveForward();
			if(nk_button_label(context , "Move Backward")) editor.moveActiveBackward();
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Parallax" , put(staticParallaxCheck , activeAsStatic.hasParallax()))) 
				activeAsStatic.hasParallax(toBool(staticParallaxCheck));
			
			if(activeAsStatic.hasParallax()) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Parallax X Offset" , -2f , xParallax , 2f , 0.5f , 0.25f);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Parallax Y Offset" , -2f , yParallax , 2f , 0.5f , 0.25f);
				
				activeAsStatic.setParallaxX(xParallax.get(0));
				activeAsStatic.setParallaxY(yParallax.get(0));
											
			}
		
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , activeAsStatic.name() + " Info" , put(staticInfoCheck , showStaticInfo)))
				showStaticInfo = toggle(showStaticInfo);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Edit Colliders" , put(editCollidersCheck , activeAsStatic.collidersFocused())))
				activeAsStatic.toggleFocusColliders();
		
			nk_layout_row_dynamic(context, 30 , 1);
			nk_text(context, "Color Chooser" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context, 30 , 2);
			if(nk_radio_label(context, "Up-Right" , upRight)) {
				
				put(upLeft , false);
				put(downRight , false);
				put(downLeft , false);
											
			}
			
			if(nk_radio_label(context, "Up-Left" , upLeft)) {
				
				put(upRight , false);
				put(downRight , false);
				put(downLeft , false);
											
			}
			
			nk_layout_row_dynamic(context, 30 , 2);
			if(nk_radio_label(context, "Down-Right" , downRight)) {
				
				put(upRight , false);
				put(upLeft , false);
				put(downLeft , false);
												
			}
			
			if(nk_radio_label(context, "Down-Left" , downLeft)) {
				
				put(upRight , false);
				put(upLeft , false);
				put(downRight , false);
											
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Color Vertex")) {
				
				int corner = -1;
				if(upRight.get(0) == 1) corner = 3;
				else if(upLeft.get(0) == 1) corner = 2;
				else if(downRight.get(0) == 1) corner = 1;
				else if(downLeft.get(0) == 1) corner = 0;
				
				activeAsStatic.quickChangeColor(corner , staticColorChooser.r(), staticColorChooser.g(), staticColorChooser.b());
				
			}
			
			nk_layout_row_dynamic(context , 15 , 1);
			
			nk_layout_row_dynamic(context, 250 , 1);						
			nk_color_pick(context , staticColorChooser , NK_RGB);
		
			nk_layout_row_dynamic(context, 30 ,  2);			
			nk_property_float(context , "Adjust Width" , -10f , widthAdjust , 10f , 1f , 2f);			
			nk_property_float(context , "Adjust Height" , -10f , heightAdjust , 10f , 1f , 2f);
			
			activeAsStatic.modWidthBi(widthAdjust.get(0));
			activeAsStatic.modHeightUp(heightAdjust.get(0));
			widthAdjust.put(0 , 0);
			heightAdjust.put(0 , 0);
			
					
		}
	
		nk_end(context);

	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Static Collider Editor				|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect staticColliderEditorRect = NkRect.malloc(allocator).x(1285).y(530).w(300).h(345);
	
	void layoutStaticColliderEditor() {
		
		if(activeAsStatic == null) return;
				
		Colliders collider = activeAsStatic.activeCollider();
		
		int options = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR;
					
		if(activeAsStatic.collidersFocused() && nk_begin(context , "Static Collider Editor" , staticColliderEditorRect , options)) {
		
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Add")) activeAsStatic.addCollider();
			if(nk_button_label(context , "Remove")) activeAsStatic.removeActiveCollider();	
							
			if(collider == null) {
				
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context, 30 , 1);
			nk_text(context , "Triangle State" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			allocator.push();
			nk_layout_row_dynamic(context , 30 , 2);					
			if(nk_radio_label(context , "Upper Right" , toByte(allocator , collider.isUpperRightTriangle())))collider.makeUpperRightTriangle();
			if(nk_radio_label(context , "Upper Left" , toByte(allocator , collider.isUpperLeftTriangle()))) collider.makeUpperLeftTriangle();
			
			nk_layout_row_dynamic(context , 30 , 2);					
			if(nk_radio_label(context , "Lower Right" , toByte(allocator , collider.isLowerRightTriangle()))) collider.makeLowerRightTriangle();
			if(nk_radio_label(context , "Lower Left" , toByte(allocator , collider.isLowerLeftTriangle()))) collider.makeLowerLeftTriangle();
		
			if(activeAsStatic.isActiveColliderTriangle()) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Undo Triangle")) activeAsStatic.unmakeActiveColliderTriangle();
				
			}
		
			nk_layout_row_dynamic(context , 30 , 2);
						
			FloatBuffer widthMod = allocator.callocFloat(1) , heightMod = allocator.callocFloat(1);
			
			nk_property_float(context , "Adjust Width" , -10f , widthMod , 10f , 1f , 2f);
			nk_property_float(context , "Adjust Height" , -10f , heightMod , 10f , 1f , 2f);
			
			collider.modWidth(widthMod.get(0));
			collider.modHeight(heightMod.get(0));
			allocator.pop();
						
		}
		
		nk_end(context);
				
	}	
	
	private NkRect staticInfoRect =  NkRect.malloc(allocator).x(1285).y(125).w(300).h(400);
	boolean firstTimeOpeningStaticInfo = true;
	private boolean showStaticInfo = false;
	private boolean showStaticColliders;
	private ByteBuffer showStaticCollidersCheck = alloc0(allocator);
	
	void layoutStaticInfo() {
		
		if(showStaticInfo) {
		
			int options = firstTimeOpeningStaticInfo ? 
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZED:
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_SCALABLE;
			
			if(nk_begin(context , "Static Info" , staticInfoRect , options)) {
			
				firstTimeOpeningStaticInfo = false;

				if(active == null || !(active instanceof Statics)) return;
				
				Statics activeStatic = (Statics) active;
				
				float[] data = active.getData();
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text(context , activeStatic.name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text(context , "position:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				nk_layout_row_dynamic(context , 25 , 2);
				nk_text_wrap(context , "Top Left X: " + data[9]);
				nk_text_wrap(context , "Top Left Y: " + data[10]);
				
				nk_layout_row_dynamic(context , 25 , 2);
				nk_text_wrap(context , "Top Right X: " + data[18]);
				nk_text_wrap(context , "Top Right Y: " + data[19]);
				
				nk_layout_row_dynamic(context , 25 , 2);
				nk_text_wrap(context , "Bot Left X: " + data[27]);
				nk_text_wrap(context , "Bot Left Y: " + data[28]);
				
				nk_layout_row_dynamic(context , 25 , 2);
				nk_text_wrap(context , "Bot Right X: " + data[0]);
				nk_text_wrap(context , "Bot Right Y: " + data[1]);
				
				nk_layout_row_dynamic(context , 25 , 2);
				nk_text_wrap(context , "Midpoint X: " + active.getMidpoint()[0]);
				nk_text_wrap(context , "Midpoint Y: " + active.getMidpoint()[1]);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text(context , "colliders:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				nk_layout_row_dynamic(context , 25 , 1);
				nk_text_wrap(context , "Number: " + activeStatic.numberColliders());
				nk_layout_row_dynamic(context , 25 , 1);
				nk_text_wrap(context , "Active: " + activeStatic.activeColliderIndex());
				
				if(nk_checkbox_label(context , "View" , put(showStaticCollidersCheck , showStaticColliders)))
					showStaticColliders = toggle(showStaticColliders);
				
			}
			
			nk_end(context);
		
		}
		
	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Entity Editor					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect entityEditorRect = NkRect.malloc(allocator).x(1285).y(45).w(300).h(650);
	boolean firstTimeOpeningEntityEditor = true;
	private boolean showEntityEditor = true;
	private ByteBuffer pythonVariablesCheck = alloc0(allocator);
	private ByteBuffer filterFunctions = alloc0(allocator);
	CSLinked<Tuple2<PyObject , PyObject>> variables;
	
	void layoutEntityEditor() {
				
		int options = firstTimeOpeningEntityEditor ?
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZED:
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR;
		
		if(showEntityEditor && nk_begin(context , "Entity Editor" , entityEditorRect , options)){
			
			firstTimeOpeningEntityEditor = false;
			
			nk_layout_row_dynamic(context , 30 , 3);					
			if(nk_button_label(context , "New Entity")) editor.addEntity();
			if(nk_button_label(context , "Load Entity")) editor.loadEntity();
			if(nk_button_label(context , "Delete Entity")) {
				
				Supplier<String> filepathToDelete = DialogUtils.newFileExplorer("Select an Entity to Delete" , 5 , 270 , false , false , data + "entities/");
				TemporalExecutor.onTrue(() -> filepathToDelete.get() != null , () -> {
					
					try {
						
						GameFiles.delete(filepathToDelete.get());
						
					} catch(Exception e) {
					
						System.err.println("Error occured tring to delete " + filepathToDelete.get() + ", terminating action");
						
					}					
					
				});
				
			}
			
			if(active == null || !(active instanceof Entities)) {
				
				nk_end(context);
				return;
				
			}
			
			Entities E = (Entities) active;			
			nk_layout_row_dynamic(context , 20 , 2);			
			nk_text(context , "Active Entity:", NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , E.name(), NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			 
			
			nk_layout_row_dynamic(context, 30 , 2);
			if(nk_button_label(context , "Save Entity")) E.write();						
			if(nk_button_label(context , "Remove Entity")) editor.removeActive();
		
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Texture")) editor.textureActive();
			
			if(nk_button_label(context , "Remove Color")) {

				Supplier<float[]> color = DialogUtils.newColorChooser("Remove Color", 5, 270);
				TemporalExecutor.onTrue(() -> color.get() != null , () -> editor.removeActiveColor(color.get()[0] , color.get()[1] , color.get()[2]));
				
			}
			
			if(nk_button_label(context , "Filter")) {
				
				Supplier<float[]> color = DialogUtils.newColorChooser("Filter Color", 5, 270);
				TemporalExecutor.onTrue(() -> color.get() != null, () -> editor.filterActiveColor(color.get()[0] , color.get()[1] , color.get()[2]));

			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Save Current Sprite as Default")) E.setDefaultSprite();
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Reset Current Sprite to Default")) E.resetToDefaultSprite();
			
		}
		
		nk_end(context);
				
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Python Introspector					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect pythonIntrospectorRect = NkRect.malloc(allocator).set(1590 , 45, 325, 400);
	boolean firstTimeOpeningEntityPythonIntrospector = true;
	
	void layoutEntityPythonIntrospector(boolean constantUpdate) {		
		
		int options = firstTimeOpeningEntityPythonIntrospector ?
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED:
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE;
		
		String name;
		Entities E = null;
		
		if(active == null || !(active instanceof Entities)) {
			
			name = "Python Variable View";
			
		} else {
			
			E = (Entities) active;
			name =  E.name() + " Python Variable View";
			
		}
		
		if(nk_begin(context , name , pythonIntrospectorRect , options)) {
			
			firstTimeOpeningEntityPythonIntrospector = false;
			
			if(active == null || !(active instanceof Entities)) {
				
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text_wrap(context , "Select an Entity");
				
				nk_end(context);
				return;
			}
			
			firstTimeOpeningEntityPythonIntrospector = false;
			
			if(E.has(ECS.SCRIPT)) {
				
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_radio_label(context , "All Members" , pythonVariablesCheck)) {
					
					variables = ((EntityScripts) E.components()[Entities.SOFF]).getAllMembers();
					put(filterFunctions , false);
					
				}
				
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_radio_label(context , "All Variables" , filterFunctions)) {
					variables = ((EntityScripts) E.components()[Entities.SOFF]).getAllVariables();
					put(pythonVariablesCheck , false);
					
				}
				
			}
			
			if(variables == null) {
				
				nk_end(context);
				return;
				
			}
			
			if(constantUpdate) variables = ((EntityScripts) E.components()[Entities.SOFF]).getAllVariables();
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Variable Name" , NK_TEXT_ALIGN_CENTERED);
			nk_text(context , "Variable Value" , NK_TEXT_ALIGN_CENTERED);
			
			cdNode<Tuple2<PyObject , PyObject>> iter = variables.get(0);
			for(int i = 0 ; i < variables.size() ; i ++ , iter = iter.next) {
								
				try{
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , iter.val.getFirst() + "" , NK_TEXT_ALIGN_LEFT);
					nk_text(context , iter.val.getSecond() + "" , NK_TEXT_ALIGN_RIGHT);
					
				} catch(NullPointerException e) {}
								
			}
			
		}
		
		nk_end(context);
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * 					Components Editor					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	private NkRect componentEditorRect = NkRect.malloc(allocator).x(5).y(85).w(500).h(500);
	boolean firstTimeOpeningComponentsEditor = true;
	private boolean showComponentsEditor = true;

	private boolean renderHitBox = false;
	private int previousID = -1;
	private ByteBuffer colliderCheckPtr = alloc0(allocator);
	private ByteBuffer gravityCheck = alloc0(allocator);
	private ByteBuffer horizontalDisplacementCheck = alloc0(allocator);
	private ByteBuffer verticalDisplacementCheck = alloc0(allocator);
	private ByteBuffer scriptCheck = alloc0(allocator);
	private ByteBuffer animationCheck = alloc0(allocator);
	private ByteBuffer hitboxesCheck = alloc0(allocator);
	private ByteBuffer RPGStatsCheck = alloc0(allocator);
	private ByteBuffer cameraTrack = alloc0(allocator);
	private ByteBuffer directionCheck = alloc0(allocator);
	private ByteBuffer inventoryCheck = alloc0(allocator);	
	private ByteBuffer editColliderCheck = alloc0(allocator);
	private ByteBuffer flagCheck = alloc0(allocator);
	private ByteBuffer audioCheck = alloc0(allocator);
	
	private ByteBuffer editStatsCheck = alloc0(allocator);
	
	void layoutComponentEditor() {
		
		if(showComponentsEditor) {
			
			int componentsOptions = firstTimeOpeningComponentsEditor ? 
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZED:
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR;
							
			if(nk_begin(context , "Components Editor" , componentEditorRect , componentsOptions)) {
			
				firstTimeOpeningComponentsEditor = false;
			
				if(active == null || !(active instanceof Entities)) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text_wrap(context , "Select an Entity");
					nk_end(context);
					return;
					
				}
				
				Entities E = (Entities)active;			
				if(previousID != E.getID()) {
						
					put(editColliderCheck , (byte) 0);
					renderHitBox = false;
					previousID = E.getID();
					
				}
									
				Object[] comps = E.components();
						
				try(MemoryStack stack = allocator.push()){
					
					boolean has = false;
					
					if(has = E.has(ECS.HORIZONTAL_PLAYER_CONTROLLER)) {
						
						nk_layout_row_begin(context , NK_STATIC , 20 , 3);
						nk_layout_row_push(context , 200);
						if(nk_checkbox_label(context , ECS.HORIZONTAL_PLAYER_CONTROLLER.toString() , toByte(stack , has)))
							scene.entities().toggleComponent(E , ECS.HORIZONTAL_PLAYER_CONTROLLER);
						
						nk_layout_row_push(context , 90);
						if(nk_button_label(context , "Speed")) 
							offloadFloatAssignment("Move Speed" , x -> comps[Entities.HCOFF] = x);
						
						nk_layout_row_end(context);
						
					} else {
						
						nk_layout_row_begin(context , NK_STATIC , 20 , 1);
						nk_layout_row_push(context , 200);
						if(nk_checkbox_label(context , "Horizontal Controller" , toByte(stack , has)))
							scene.entities().toggleComponent(E, ECS.HORIZONTAL_PLAYER_CONTROLLER);
						
						nk_layout_row_end(context);
						
					}
					
					if(has = E.has(ECS.VERTICAL_PLAYER_CONTROLLER)) {
						
						nk_layout_row_begin(context , NK_STATIC , 20 , 3);
						
						nk_layout_row_push(context , 200);
						if(nk_checkbox_label(context ,  ECS.VERTICAL_PLAYER_CONTROLLER.toString() , toByte(stack , has))) 
							scene.entities().toggleComponent(E, ECS.VERTICAL_PLAYER_CONTROLLER);
						
						nk_layout_row_push(context , 90);
						if(nk_button_label(context , "Time")) 
							offloadFloatAssignment("Jump Time (Ticks)" , f -> comps[Entities.VCOFF + 1] = f);
						
						nk_layout_row_push(context , 90);
						if(nk_button_label(context, "Velocity"))
							offloadFloatAssignment("Jump Velocity" , f -> comps[Entities.VCOFF + 2] = f);
						
						nk_layout_row_end(context);
						
					} else {
						
						nk_layout_row_begin(context , NK_STATIC , 20 , 2);
						
						nk_layout_row_push(context , 200);
						if(nk_checkbox_label(context , ECS.VERTICAL_PLAYER_CONTROLLER.toString() , toByte(stack , has))) 
							scene.entities().toggleComponent(E, ECS.VERTICAL_PLAYER_CONTROLLER);
						
						nk_layout_row_end(context);
						
					}
					
				}												
											
				put(colliderCheckPtr , E.has(ECS.COLLISION_DETECTION));
				
				if(toBool(colliderCheckPtr)) {
	
					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Collider" , colliderCheckPtr)) scene.entities().toggleComponent(E, ECS.COLLISION_DETECTION);

					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Scan Radius")) offloadFloatAssignment("Scan Radius" , f -> comps[Entities.CDOFF + 1] = f);
					
					nk_layout_row_push(context , 90);
					if(nk_checkbox_label(context , "Edit Col." , editColliderCheck)) if(toBool(editColliderCheck)) ((EntityAnimations)comps[Entities.AOFF]).animate(true);
					
					nk_layout_row_end(context);
					
				} else {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 3);
					nk_layout_row_push(context , 100);
					if(nk_checkbox_label(context , "Collider" , colliderCheckPtr)) 
						scene.entities().toggleComponent(E, ECS.COLLISION_DETECTION);
					
					nk_layout_row_end(context);
					
				}
				
				put(gravityCheck , E.has(ECS.GRAVITY_CONSTANT));
				
				if(toBool(gravityCheck)) {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Gravity" , gravityCheck)) 
						scene.entities().toggleComponent(E, ECS.GRAVITY_CONSTANT);
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Constant")) 
						offloadFloatAssignment("Gravity Constant" , f -> comps[Entities.GCOFF] = f);
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Additive")) 
						offloadFloatAssignment("Max Additive" , f -> comps[Entities.GCOFF + 2] = f);
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Velocity")) 
						offloadFloatAssignment("Velocity" , f -> comps[Entities.GCOFF + 3] = f);
					
					nk_layout_row_end(context);
					
				} else {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 3);
					nk_layout_row_push(context , 100);
					if(nk_checkbox_label(context , "Gravity" , gravityCheck)) 
						scene.entities().toggleComponent(E, ECS.GRAVITY_CONSTANT);
					
					nk_layout_row_end(context);
												
				}

				put(horizontalDisplacementCheck , E.has(ECS.HORIZONTAL_DISPLACEMENT));
				
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_checkbox_label(context , "Horizontal Displacement" , horizontalDisplacementCheck)) 
					scene.entities().toggleComponent(E, ECS.HORIZONTAL_DISPLACEMENT);

				put(verticalDisplacementCheck , E.has(ECS.VERTICAL_DISPLACEMENT)); 
					
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_checkbox_label(context , "Vertical Displacement" , verticalDisplacementCheck))
					scene.entities().toggleComponent(E, ECS.VERTICAL_DISPLACEMENT);
				
				put(scriptCheck , E.has(ECS.SCRIPT)); 
					 
				if(toBool(scriptCheck)) {

					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Script" , scriptCheck)) 
						scene.entities().toggleComponent(E, ECS.SCRIPT);
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Select")) {
						
						Supplier<String> scriptPath = DialogUtils.newFileExplorer("Select script", 5 , 270 , false , false , data + "scripts/");
						TemporalExecutor.onTrue(() -> scriptPath.get() != null, () -> {
							
							editor.tryCatch(() -> {

								EntityScripts script = new EntityScripts((Entities)comps[0] , (String) toNamePath(scriptPath.get()));
								comps[Entities.SOFF] = script;
								
							}, "Error loading script: " + scriptPath.get() + ", terminating action");
							
						});
						
					}
					
					if(comps[Entities.SOFF] != null) {
						
						nk_layout_row_push(context ,90);
						if(nk_button_label(context , "Recompile")) {
							
							try {
								
								EntityScripts interpreter = (EntityScripts) comps[Entities.SOFF];
								renderHitBox = false;								
								interpreter.recompile();
								
							} catch (Exception e) {
								
								e.printStackTrace();								
								editor.getConsole().say("Entity Script Compilation Error");
								scene.entities().toggleComponent(E, ECS.SCRIPT);
								
							}
							
						}							
						
						nk_layout_row_push(context , 90);
						if(nk_button_label(context , "Pause Script")) 
							comps[Entities.SOFF + 3] = (boolean) comps[Entities.SOFF + 3] ? false:true;
						
					}
					
				} else {
					
					nk_layout_row_dynamic(context , 20 , 1);
					if(nk_checkbox_label(context , "Script" , scriptCheck)){
						
						scene.entities().toggleComponent(E, ECS.SCRIPT);
						Supplier<String> scriptPath = DialogUtils.newFileExplorer("Select script", 5 , 270 , false , false , data + "scripts/");
						TemporalExecutor.onTrue(() -> scriptPath.get() != null, () -> {
							
							editor.tryCatch(() -> {

								EntityScripts script = new EntityScripts((Entities)comps[0] , (String) toNamePath(scriptPath.get()));
								comps[Entities.SOFF] = script;							
								
							}, "Error loading script: " + scriptPath.get() + ", terminating action");
							
						});
						
					}
												
				}
				
				put(animationCheck , E.has(ECS.ANIMATIONS));
				
				if(toBool(animationCheck)) {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Animations" , animationCheck)) {
						
						scene.entities().toggleComponent(E, ECS.ANIMATIONS);
						
					}
					
					nk_layout_row_push(context ,90);
					if(nk_button_label(context , "Add")) {
						
						Supplier<String> spriteSets = DialogUtils.newFileExplorer("Select one or more SpriteSets" , 5 , 270 , true , false , data + "spritesets/");
						TemporalExecutor.onTrue(() -> spriteSets.get() != null, () -> {
							
							editor.tryCatch(() -> {

								EntityAnimations anims = (EntityAnimations)comps[Entities.AOFF];

								if(spriteSets.get().contains("|")) {//split
									
									String[] splitSets = spriteSets.get().split("\\|");									
									for(String y : splitSets) anims.add(new SpriteSets(toNamePath(y)));
									
								} else anims.add(new SpriteSets(spriteSets.get()));

							}, "Error adding SpriteSets, terminating action.");							
							
						});
						
					}
											
					nk_layout_row_end(context);
					
				} else {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 2);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Animations" , animationCheck)) {
						
						scene.entities().toggleComponent(E, ECS.ANIMATIONS);
						comps[Entities.AOFF] = new EntityAnimations(10);
						
					}
					
				}
				
				put(hitboxesCheck , E.has(ECS.HITBOXES)); 
										
				if(toBool(hitboxesCheck)) {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context, 200);
					if(nk_checkbox_label(context, "HitBoxes" , hitboxesCheck)) {
						
						scene.entities().toggleComponent(E , ECS.HITBOXES);
						comps[Entities.HOFF] = new EntityHitBoxes();
						
					}
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Add")) {
						
						Supplier<String> hitboxes = DialogUtils.newFileExplorer("Select one or more HitBoxSets", 5 , 270 , true , false , data + "hitboxsets/");
						TemporalExecutor.onTrue(() -> hitboxes.get() != null, () -> {
							
							editor.tryCatch(() -> {
								
								EntityHitBoxes Ehitboxes = (EntityHitBoxes)comps[Entities.HOFF];
								String res = hitboxes.get();
								
								if(res.contains("|")) {
									
									String [] hitboxSets = res.split("\\|");
									for(String y : hitboxSets) Ehitboxes.addSet(new HitBoxSets(y));
									
								} else Ehitboxes.addSet(new HitBoxSets(res));
								
							}, "Error occurred adding hitbox, terminating action.");
							
						});
						
					}
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Max Boxes")) {
						
						Supplier<String> input = DialogUtils.newInputBox("Input Max Number of HitBoxes" , 5 , 270);
						TemporalExecutor.onTrue(() -> input.get() != null, () -> {
							
							try {
								
								int numberBoxes = (int)toNumber(input.get());
								comps[Entities.HOFF] = new EntityHitBoxes(numberBoxes);
								
							} catch(NumberFormatException e) {
								
								console.say("Invalid input for an integer; " + input);
								
							}
							
						});
						
					}
					
					nk_layout_row_end(context);
					
				} else {
					
					nk_layout_row_dynamic(context , 20 , 1);
					if(nk_checkbox_label(context, "HitBoxes" , hitboxesCheck)) {
						
						scene.entities().toggleComponent(E , ECS.HITBOXES);
						comps[Entities.HOFF] = new EntityHitBoxes();
						
					}
					
				}
				
				put(RPGStatsCheck , E.has(ECS.RPG_STATS)); 
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context, "RPG Stats" , RPGStatsCheck)) {
					
					scene.entities().toggleComponent(E, ECS.RPG_STATS);
					comps[Entities.RPGOFF] = new EntityRPGStats(E);
					
				}
				
				if(toBool(RPGStatsCheck)) {
										
					nk_layout_row_push(context , 90);
					if(nk_checkbox_label(context , "Edit Stats" , editStatsCheck));
					
				}
				
				nk_layout_row_end(context);
				
				put(cameraTrack , E.has(ECS.CAMERA_TRACK)); 
				
				if(toBool(cameraTrack)) {
				
					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Camera Tracks" , cameraTrack))
						scene.entities().toggleComponent(E, ECS.CAMERA_TRACK);
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Horizontal Additive")) 
						offloadFloatAssignment("Camera Horizontal Position" , f -> comps[Entities.CTOFF] = f);
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Vertical Additive"))
						offloadFloatAssignment("Camera Vertical Position" , f -> comps[Entities.CTOFF + 1] = f);
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Zoom Factor")) 
						offloadFloatAssignment("Camera Zoom" , f -> comps[Entities.CTOFF + 2] = f);
					
				} else {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 1);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Camera Tracks" , cameraTrack))
						scene.entities().toggleComponent(E, ECS.CAMERA_TRACK);
					
				}
				
				put(directionCheck , E.has(ECS.DIRECTION));
				
				if(toBool(directionCheck)) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					if(nk_checkbox_label(context , "Direction" , directionCheck)) scene.entities().toggleComponent(E, ECS.DIRECTION);
												
				} else {
					
					nk_layout_row_dynamic(context , 20 , 1);
					if(nk_checkbox_label(context , "Direction" , directionCheck)) scene.entities().toggleComponent(E, ECS.DIRECTION);
					
				}
				
				put(inventoryCheck , E.has(ECS.INVENTORY));
				if(inventoryCheck.get(0) == 1) {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Inventory" , inventoryCheck)) {
						
						scene.entities().toggleComponent(E, ECS.INVENTORY);
						
					}
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Max Weight")) 
						offloadFloatAssignment("Max Weight" , f -> ((Inventories) comps[Entities.IOFF]).weightLimit(f));
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Add Item")) {
					
						Supplier<String> items = DialogUtils.newFileExplorer("Select One or More Items", 5, 270, true , false, data + "items/");
						TemporalExecutor.onTrue(() -> items.get() != null , () -> {
							
							String filepaths = items.get();
							Inventories inv = ((Inventories) comps[Entities.IOFF]);
							
							if(filepaths.contains("|")) {
								
								String[] paths = filepaths.split("\\|");
								for(String x : paths) inv.acquire(new Items(toNamePath(x)));
								
							} else inv.acquire(new Items(toNamePath(filepaths)));
							
						});
						
					}		
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Add Equipped")) {
						
						Supplier<String> item = DialogUtils.newFileExplorer("Select an Equippable Item", 5, 270, false , false, data + "items/");
						TemporalExecutor.onTrue(() -> item.get() != null , () -> {

							editor.tryCatch(() -> ((Inventories) comps[Entities.IOFF]).equip(new Items(toNamePath( item.get()))) , 
								"Error adding item to equip, terminating action");
							
						});
						
					}
					
				} else {
					
					nk_layout_row_dynamic(context , 20 , 1);
					if(nk_checkbox_label(context , "Inventory" , inventoryCheck)) {
						
						scene.entities().toggleComponent(E, ECS.INVENTORY);
						comps[Entities.IOFF] = new Inventories(E);
						
					}
					
				}
														
				put(flagCheck , E.has(ECS.FLAGS));
				if(toBool(flagCheck)) {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Flags" , flagCheck)) {
						
						scene.entities().toggleComponent(E , ECS.FLAGS);
						comps[Entities.FOFF] = new EntityFlags(10);
						
					}
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Add Flag")) {
						
						Supplier<String> flagName = DialogUtils.newInputBox("Input Flag Name", 5 , 270);
												
						TemporalExecutor.onTrue(() -> flagName.get() != null , () -> ((EntityFlags)comps[Entities.FOFF]).add(flagName.get()));
												
					}
					
					nk_layout_row_end(context);
					
					
				} else {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Flags" , flagCheck)) {
						
						scene.entities().toggleComponent(E , ECS.FLAGS);
						comps[Entities.FOFF] = new EntityFlags(10);
						
					}
					
					nk_layout_row_end(context);
					
				}
				
				put(audioCheck , E.has(ECS.AUDIO_EMIT));
				if(toBool(audioCheck)) {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 4);
					nk_layout_row_push(context , 200);
					if(nk_checkbox_label(context , "Audio Emmitter" , audioCheck)) {
						
						scene.entities().toggleComponent(E, ECS.AUDIO_EMIT);
						comps[Entities.AEOFF] = new CSArray<Integer>(10 , -1);
						
					}
					
					nk_layout_row_push(context , 70);
					if(nk_button_label(context , "Add")) {
						
						Supplier<String> soundFiles = DialogUtils.newFileExplorer("Select one or more Sounds", 5 , 270 , false , true);
						TemporalExecutor.onTrue(() -> soundFiles.get() != null, () -> {
							
							String[] split = soundFiles.get().split("\\|");
							@SuppressWarnings("unchecked") CSArray<Sounds> sounds = (CSArray<Sounds>)comps[Entities.AEOFF];
							for(String y : split) sounds.add(SoundEngine.add(y));
							
						});

					}
					
					nk_layout_row_push(context , 70);
					if(nk_button_label(context, "Remove Sound")) {
						
						//TODO
						
					}							
					
					nk_layout_row_end(context);
					
				} else {
					
					nk_layout_row_dynamic(context , 20 , 3);
					if(nk_checkbox_label(context , "Audio Emmitter" , audioCheck)) {
						
						scene.entities().toggleComponent(E, ECS.AUDIO_EMIT);
						comps[Entities.AEOFF] = new CSArray<Integer>(10 , -1);
						
					}
					
				}					
				
				if(renderHitBox) scene.entities().updateHitBoxes(E);
				
			}
			
			nk_end(context);
					
		}	
		
	}	
	
	private NkRect entityColliderEditorRect = NkRect.malloc(allocator).x(510).y(85).w(300).h(200);
	
	private FloatBuffer scrollHoriz = allocator.floats(0);
	private FloatBuffer scrollVert = allocator.floats(0);
	
	private FloatBuffer widthScroll = allocator.floats(0);
	private FloatBuffer heightScroll = allocator.floats(0);
	
	private void layoutEntityColliderEditor() {
		
		if(!toBool(editColliderCheck)) return;
		
		if(nk_begin(context , "Edit Collision Bound" , entityColliderEditorRect , NK_WINDOW_MOVABLE|NK_WINDOW_BORDER|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR)) {
		
			if(!(active instanceof Entities)) {
				
				nk_end(context);
				return;
				
			}
			
			Entities E = (Entities)active;
			Object[] comps = E.components();
			E.roundVertices();
			
			//if active entity has animations, stop them
			if(E.has(ECS.ANIMATIONS))  ((EntityAnimations)comps[Entities.AOFF]).animate(false);
			float[] collisionBounds;
			put(renderDebugCheck , true);
			
			if(comps[Entities.CDOFF] == null) {
				
				//represents an unchanging set of values to use for collision detection. 
				//this is needed to ensure collision detection doesnt break
				collisionBounds = getColliderFloatArray(); 
				collisionBounds = changeColorTo(collisionBounds , 0f , 0.75f , 0f);
				collisionBounds = makeTranslucent(collisionBounds , 0.25f);

				moveTo(E , collisionBounds);
				comps[Entities.CDOFF] = collisionBounds;
				
			} else collisionBounds = (float[])comps[Entities.CDOFF];
			
			collisionBounds = CSUtil.BigMixin.snapDataToPixels(collisionBounds);					
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context,  "Position Horizontal" , -5 , scrollHoriz , 5 , 1 , 1);
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context,  "Position Vertical" , -5 , scrollVert , 5 , 1 , 1);
			
			collisionBounds = translateArray(collisionBounds , scrollHoriz.get(0) ,  scrollVert.get(0));
			put(scrollHoriz , 0);
			put(scrollVert , 0);
							
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Width Mod" , -5 , widthScroll , 5 , 1 , 1);
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Height Mod" , -5 , heightScroll , 5 , 1 , 1);
			
			collisionBounds = modArrayWidth(collisionBounds , widthScroll.get(0));
			collisionBounds = modArrayHeight(collisionBounds , heightScroll.get(0));							
			
			put(widthScroll , 0);
			put(heightScroll , 0);
			
			if(getArrayWidth(collisionBounds) < 2) collisionBounds = modArrayWidth(collisionBounds , -getArrayWidth(collisionBounds));
			if(getArrayHeight(collisionBounds) < 2) collisionBounds = modArrayHeight(collisionBounds , -getArrayHeight(collisionBounds));
			
			float[] colliderMid = CSUtil.BigMixin.getArrayMidpoint(collisionBounds);
			float[] EMid = E.getMidpoint();
			nk_layout_row_dynamic(context , 20 , 4);
			nk_text(context , "X Offset: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + (EMid[0] - colliderMid[0]) , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_text(context , "Y Offset: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + (EMid[1] - colliderMid[1]) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
						
		}				
				
		nk_end(context);
		
	}
	
	//allows for hand setting of RPG stats, all of which default to 0.
	
	private NkRect RPGStatRect = NkRect.malloc(allocator).x(510).y(125).w(400).h(900);
		
	void layoutRPGStatEditor() {
		
		Entities E;
		if(active instanceof Entities && active != null && toBool(editStatsCheck) && nk_begin(context , "RPG Stats Editor" , RPGStatRect , NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE)) {
			
			E = (Entities)active;
			Object[] components = E.components();
			
			if(components[Entities.RPGOFF] == null) {
				
				nk_layout_row_dynamic(context ,20 , 1);
				nk_text_wrap(context , "Select an Entity With RPG Stats");
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 20 , 3);
			nk_text(context , "Variable Name" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "Variable Value" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "Set Value" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			EntityRPGStats stats = (EntityRPGStats) components[Entities.RPGOFF];
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Randomize All")) {
				
				Supplier<String> upperBound = DialogUtils.newInputBox("Random Upper Bound" , 5 , 590 , DialogUtils.NUMBER_FILTER);
				TemporalExecutor.onTrue(() -> upperBound.get() != null , () -> {
					
					editor.tryCatch(() -> {
						
						int upperBoundValue = Integer.parseInt(upperBound.get());	

						MExpression rand = new MExpression("rand x");
						rand.setInt("x" , () -> upperBoundValue);
						
						stats.forEachStat((name , value) -> stats.setCharacteristicForName(name , rand.at()));
						stats.forEachLSM((name , value) -> stats.setLSMForName(name, rand.at())); 					
						stats.forEachSkill((name , value) -> stats.setSkillForName(name , rand.at()));
						
					}, "Error randomizing all stats, terminating action.");					
					
				});
				
			}
			
			if(nk_button_label(context , "Set All")) {
				
				Supplier<String> valueString = DialogUtils.newInputBox("Set to" , 5 , 590 , DialogUtils.NUMBER_FILTER);
				TemporalExecutor.onTrue(() -> valueString.get() != null , () -> {
					
					editor.tryCatch(() -> {
						
						int value = Integer.parseInt(valueString.get());
						stats.forEachStat((name , adsf) -> stats.setCharacteristicForName(name , value));
						stats.forEachLSM((name , sdf) -> stats.setLSMForName(name, value));
						stats.forEachSkill((name , asdf) -> stats.setSkillForName(name, value));
						
					} , "Error setting all stats, terminating action.");
					
				});
				
			}

			stats.forEachStat((name , value) -> {
				
				nk_layout_row_dynamic(context , 20 , 3);
				name = name.substring(0 , 1).toUpperCase() + name.substring(1);
				nk_text(context , name , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , value + "" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);

				if(nk_button_label(context , name +  " Value")) {
					
					Supplier<String> input = DialogUtils.newInputBox("Input Value for " + name , 5 , 590 , DialogUtils.NUMBER_FILTER);
					String currentName = name;
					TemporalExecutor.onTrue(() -> input.get() != null , () -> 
						editor.tryCatch(() -> stats.setCharacteristicForName(currentName , Integer.parseInt(input.get())) , ""));
					
				}
				
			});
			
			stats.forEachLSM((name , value) -> {
				
				nk_layout_row_dynamic(context , 20 , 3);
				name = name.substring(0 , 1).toUpperCase() + name.substring(1);
				nk_text(context , name , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , value + "" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);

				if(nk_button_label(context , name +  " Value")) {
					
					Supplier<String> input = DialogUtils.newInputBox("Input Value for " + name , 5 , 590 , DialogUtils.NUMBER_FILTER);
					String currentName = name;
					TemporalExecutor.onTrue(() -> input.get() != null , () -> 
						editor.tryCatch(() -> stats.setLSMForName(currentName , Float.parseFloat(input.get())) , ""));
					
				}
				
			});
			
			stats.forEachSkill((name , value) -> {
				
				nk_layout_row_dynamic(context , 20 , 3);
				name = name.substring(0 , 1).toUpperCase() + name.substring(1);
				nk_text(context , name , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , value + "" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);

				if(nk_button_label(context , name +  " Value")) {
					
					Supplier<String> input = DialogUtils.newInputBox("Input Value for " + name , 5 , 590 , DialogUtils.NUMBER_FILTER);
					String currentName = name;
					TemporalExecutor.onTrue(() -> input.get() != null , () -> 
						editor.tryCatch(() -> stats.setSkillForName(currentName , Integer.parseInt(input.get())) , ""));
					
				}
				
			});
			
		}
		
		nk_end(context);
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Component Viewer				|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect componentViewerRect = NkRect.malloc(allocator).x(510).y(85).w(670).h(600);
	boolean firstTimeOpeningComponentViewer = true;
	private boolean showComponentViewer = true;
	
	void layoutComponentViewer() {
		
		if(showComponentViewer) {
			
			int options = firstTimeOpeningComponentViewer ? 
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED:
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_SCALABLE;
			
			if(nk_begin(context , "Component Viewer" , componentViewerRect , options)) {
				
				firstTimeOpeningComponentViewer = false;
				
				if(active == null || !(active instanceof Entities)) {
						
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text_wrap(context , "Select an Entity");
					nk_end(context);
					return;
					
				}
				
				Entities E = (Entities)active;
				Object[] activeComps = E.components();
				
				if(E.has(ECS.HORIZONTAL_PLAYER_CONTROLLER)) {
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Horizontal Controller:" , NK_TEXT_ALIGN_LEFT);
					nk_text(context , Float.toString((float)activeComps[ECS.HORIZONTAL_PLAYER_CONTROLLER.offset]) , NK_TEXT_ALIGN_RIGHT);
					
				}
				
				if(E.has(ECS.VERTICAL_PLAYER_CONTROLLER)) {
					
					nk_layout_row_dynamic(context , 20 , 5);
					nk_text(context , "Vert Cont" , NK_TEXT_ALIGN_LEFT);
					nk_text(context , "Time: " + (float)activeComps[Entities.VCOFF] , NK_TEXT_ALIGN_CENTERED);
					nk_text(context , "Max Time: " + (float)activeComps[Entities.VCOFF + 1] , NK_TEXT_ALIGN_CENTERED);
					nk_text(context , "Vel: " + (float)activeComps[Entities.VCOFF + 2] , NK_TEXT_ALIGN_CENTERED);
					nk_text(context , "Jumping: " + (boolean)activeComps[Entities.VCOFF + 3] , NK_TEXT_ALIGN_RIGHT);
				
				}
				
				if(E.has(ECS.COLLISION_DETECTION)) {
																	
					nk_layout_row_dynamic(context , 20  , 3);
					nk_text(context , "Collision Detection" , NK_TEXT_ALIGN_LEFT);
					nk_text(context , "Collider " + activeComps[Entities.CDOFF] == null ? "Null" : "Collider" , NK_TEXT_ALIGN_CENTERED);
					nk_text(context , "Scan Radius: " + (float)activeComps[Entities.CDOFF + 1] , NK_TEXT_ALIGN_RIGHT);
					
				}
				
				if(E.has(ECS.GRAVITY_CONSTANT)) {
					
					nk_layout_row_dynamic(context , 20 , 4);
					nk_text(context , "Gravity: " , NK_TEXT_ALIGN_LEFT);
					nk_text(context , "Constant: " + Float.toString((float)activeComps[Entities.GCOFF]) , NK_TEXT_ALIGN_CENTERED);
					nk_text(context , "Additive: " + Float.toString((float)activeComps[Entities.GCOFF + 1]) , NK_TEXT_ALIGN_RIGHT);
					nk_text(context , "Max: " + Float.toString((float)activeComps[Entities.GCOFF + 2]) , NK_TEXT_ALIGN_RIGHT);
					
				}
										
				if(E.has(ECS.HORIZONTAL_DISPLACEMENT)) {
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Horiz Displ" , NK_TEXT_ALIGN_LEFT);
					nk_text(context , "Displacement: " + activeComps[Entities.HDOFF + 1], NK_TEXT_ALIGN_RIGHT);
												
				}
				
				if(E.has(ECS.VERTICAL_DISPLACEMENT)) {
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Vert Displ" , NK_TEXT_ALIGN_LEFT);
					nk_text(context , "Displacement: " + activeComps[Entities.VDOFF + 1], NK_TEXT_ALIGN_RIGHT);
					
				}
				
				if(E.has(ECS.ANIMATIONS)) {
					
					EntityAnimations anims = (EntityAnimations)activeComps[Entities.AOFF];
					
					nk_layout_row_dynamic(context , 20 , 4);
					nk_text(context , "Animations: " , NK_TEXT_ALIGN_LEFT);					
					nk_text(context , "Active: " + anims.activeIndex() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);					
					nk_text(context , "Hungup: " + anims.isHungup(), NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					
					if(nk_button_label(context , "List in console")) for(SpriteSets x : anims.anims()) if(x != null) console.say(x.name());
				}
				
				if(E.has(ECS.SCRIPT)) {
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Script: " , NK_TEXT_ALIGN_LEFT);
											
				}
				
				if(E.has(ECS.HITBOXES)) {
					
					nk_layout_row_dynamic(context , 20 , 4);
					nk_text(context , "Hit Boxes: " , NK_TEXT_ALIGN_LEFT);
					
					nk_text(context , "Active: " , NK_TEXT_ALIGN_LEFT);
					EntityHitBoxes hitboxes = (EntityHitBoxes)activeComps[Entities.HOFF];
					nk_text(context , "" + hitboxes.active() , NK_TEXT_ALIGN_LEFT);
					
					if(nk_button_label(context , "List in Console")) {
						
						ArrayList<HitBoxSets> list = ((EntityHitBoxes) activeComps[Entities.HOFF]).getSets();
						
						for(HitBoxSets x : list) {
							
							console.say(x.name());
							console.say(x.size());
							for(int i = 0 ; i < x.size() ; i ++) console.say(x.hitboxAsString(i));
							
						}
						
					}
					
				}
				
				if(E.has(ECS.RPG_STATS)) {
					
					nk_layout_row_dynamic(context , 20 , 6);
					nk_text(context , "Cur L: " + activeComps[Entities.RPGOFF] , NK_TEXT_ALIGN_LEFT);
					nk_text(context , "Max L: " + activeComps[Entities.RPGOFF + 1] , NK_TEXT_ALIGN_LEFT);
					
					nk_text(context , "Cur S: " + activeComps[Entities.RPGOFF + 2] , NK_TEXT_ALIGN_CENTERED);
					nk_text(context , "Max S: " + activeComps[Entities.RPGOFF + 3] , NK_TEXT_ALIGN_CENTERED);
					
					nk_text(context , "Cur M: " + activeComps[Entities.RPGOFF + 4] , NK_TEXT_ALIGN_RIGHT);
					nk_text(context , "Max M: " + activeComps[Entities.RPGOFF + 5] , NK_TEXT_ALIGN_RIGHT);
					
					
				}
				
				if(E.has(ECS.INVENTORY)) {

					Inventories inv = (Inventories) activeComps[Entities.IOFF];
				
					nk_layout_row_dynamic(context , 20 , 2);
					if(nk_button_label(context , "Print Items To Console")) {
						
						editor.say("Inventory:");
						cdNode<Tuple2<Items , RefInt>> iter = inv.iter();
						for(int i = 0 ; i < inv.inventorySize() ; i ++ , iter = iter.next) editor.say(iter.val.getFirst().name());
						
					}
					
					if(nk_button_label(context , "Print Equipped To Console")) {
						
						editor.say("Equipped:");
						CSArray<Items> equipped = inv.getEquipped();
						for(int i = 0 ; i < equipped.size() ; i ++) if(equipped.get(i) != null) editor.say(equipped.get(i).name());
						
					}
					
				}
				
				if(E.has(ECS.FLAGS)) {
					
					nk_layout_row_dynamic(context , 20 , 3);
					EntityFlags flags = (EntityFlags)activeComps[Entities.FOFF];
					nk_text(context , "Flags" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					nk_text(context , "Size: " + flags.size() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					if(nk_button_label(context , "Print To Console")) flags.forEach(console::say);
					
				}
				
				if(E.has(ECS.DIRECTION)) {
					
					nk_layout_row_dynamic(context , 20 , 4);
					nk_text(context , "horizontal direction: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					nk_text(context , ((Direction)activeComps[Entities.DOFF]).toString() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					
					nk_text(context , "vertical direction: " , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
					nk_text(context , ((Direction)activeComps[Entities.DOFF + 1]).toString() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
					
				}
				
				if(E.has(ECS.AUDIO_EMIT)) {
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Audio Emitter" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					if(nk_button_label(context , "Print to console")) {
						
						@SuppressWarnings("unchecked") CSArray<Sounds> sounds = (CSArray<Sounds>)activeComps[Entities.AEOFF];
						for(int i = 0 ; i < sounds.length() ; i ++) editor.say(sounds.get(i).name());
						
					}
					
				}
							
			}
			
			nk_end(context);
					
		}
		
	}
/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					HitBox Editor					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect hitboxEditorRect = NkRect.malloc(allocator).x(1185).y(85).w(400).h(1050);
	boolean firstTimeOpeningHitBoxEditor = true;
	private boolean showHitBoxEditor = true;
	//Object used to mark up hitboxes with a quad. The active quad will be the quad referenced for the math relating to the 
	//hitboxset 
	HitBoxSetMarker hitboxMarker = new HitBoxSetMarker();
	private ByteBuffer hotBoxCheck = alloc0(allocator);
	private ByteBuffer coldBoxCheck = alloc0(allocator);
	private ByteBuffer leftDirRadio = alloc0(allocator);
	private ByteBuffer rightDirRadio = alloc0(allocator);	
	private ByteBuffer showAnimationsCheck = alloc0(allocator);
	private ByteBuffer showHitBoxSetsCheck = alloc0(allocator);
	private ByteBuffer[] spritesetChecks;
	private ByteBuffer[] hitboxsetChecks;
		
	private SpriteSets selectedSet;
	private float[] selectedSprite;
	private int selectedSpriteIndex;
	ByteBuffer sliderSelect = alloc0(allocator);
		
	void layoutHitBoxEditor() {

		int options = firstTimeOpeningHitBoxEditor ?
			NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED:
			NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE;
			
		if(showHitBoxEditor && nk_begin(context , "Hit Box Editor" , hitboxEditorRect , options)) {
			
			firstTimeOpeningHitBoxEditor = false;
			
			nk_layout_row_dynamic(context , 30 , 3);			
			if(nk_button_label(context , "New HitBoxSet")) {
				
				Supplier<String> nameInput = DialogUtils.newInputBox("Input HitBoxSet Name", 5 , 270);
				TemporalExecutor.onTrue(() -> nameInput.get() != null , () -> {
					
					ArrayList<float[]> arrays = hitboxMarker.hitboxes();
					for(int i = 0 ; i < arrays.size() ; i ++) editor.renderer().removeFromRawData(arrays.get(i));
					hitboxMarker.clear();
					hitboxMarker.active = -1;
				
					hitboxMarker.editingName = nameInput.get();
					
				});
							
			}
			
			if(nk_button_label(context, "Load HitBoxSet")) {
			
				Supplier<String> filepath = DialogUtils.newFileExplorer("Select a HitBoxSet File" , 5 , 270 , false , false);
				TemporalExecutor.onTrue(() -> filepath.get() != null , () -> {
					
					ArrayList<float[]> arrays = hitboxMarker.hitboxes();
					for(int i = 0 ; i < arrays.size() ; i ++) editor.renderer().removeFromRawData(arrays.get(i));
					hitboxMarker.clear();
					hitboxMarker.active = -1;
					
					ArrayList<float[]> hitboxes = hitboxMarker.fromHitBoxSet(new HitBoxSets(filepath.get()) , active);
					for(float[] y : hitboxes) editor.renderer().addToRawData(y);
					
				});
				
			}
			
			if(nk_button_label(context , "Delete HitBoxSet")) {
				
				Supplier<String> filepath = DialogUtils.newFileExplorer("Select a HitBoxSet File" , 5 , 270 , false , false);
				TemporalExecutor.onTrue(() -> filepath.get() != null , () -> {

					try {
						
						HitBoxSets hb = new HitBoxSets(filepath.get());
						hb.delete();
						
					} catch(Exception e) {
						
						console.say("error: " + filepath.get() + " is not a valid hitboxset file");							
						
					}
										
				});
				
			}			

			if(active == null) {
				
				nk_end(context);
				return;
				
			}

			if(hitboxMarker.editingName != null) {
				
				editor.setState(EditorState.EDITING_HITBOX);
				
				nk_layout_row_dynamic(context , 20 , 1);				
				nk_text(context , hitboxMarker.editingName, NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);				
				nk_text(context , "Number of Hitboxes:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_text(context , "" + hitboxMarker.getSize() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);
				
				nk_layout_row_dynamic(context , 30 , 2);						
				if(nk_button_label(context , "Add Hit Box")) editor.addToRawData(hitboxMarker.addHitBox(active));
				
				if(nk_button_label(context , "Remove current Hit Box") && hitboxMarker.active != -1) 
					editor.renderer().removeFromRawData(hitboxMarker.removeHitBox(hitboxMarker.active));
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Remove All Hit Boxes")) {
					
					var hitboxes = hitboxMarker.hitboxes();
					for(float [] x : hitboxes) editor.renderer().removeFromRawData(x);
					hitboxMarker.clear();
					hitboxMarker.active = -1;
					
				}

				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Active Hit Box:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , hitboxMarker.active + "" , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				allocator.push();
				
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_checkbox_label(context , "Use Slider to Select HitBox" , sliderSelect)) ;
				
				if(toBool(sliderSelect)) {
					
					IntBuffer active = allocator.ints(hitboxMarker.active);
					nk_slider_int(context , -1 , active , hitboxMarker.getSize() - 1 , 1);
					hitboxMarker.active = active.get();
					
				}				
				
				allocator.pop();
				
				if(hitboxMarker.active != -1) {
					
					allocator.push();
					
					FloatBuffer sliders = allocator.floats(0f , 0f);
					nk_layout_row_dynamic(context , 30 , 1);
					nk_property_float(context , "Slide Horizontally" , -999 , sliders.slice(0 , 1) , 999 , 1 , 1);
					nk_layout_row_dynamic(context , 30 , 1);
					nk_property_float(context , "Slide Vertically" , -999 , sliders.slice(1 , 1) , 999 , 1 , 1);
					translateArray(hitboxMarker.active() , sliders.get(0) , sliders.get(1));
					
					allocator.pop();
					
					if(editor.getGlfw().isLMousePressed()) {
						
						float[] cursorCoords = editor.getCursorWorldCoords();
						hitboxMarker.drag(hitboxMarker.active, cursorCoords[0], cursorCoords[1]);
						
					}
					
					nk_layout_row_dynamic(context , 30 , 2);
					put(hotBoxCheck , hitboxMarker.hotBoxes[hitboxMarker.active] != -1);
					if(nk_checkbox_label(context , "Hot Box" , hotBoxCheck)) 
						hitboxMarker.hotBoxes[hitboxMarker.active] = hitboxMarker.hotBoxes[hitboxMarker.active] != -1 ? -1 : hitboxMarker.active;
					
					put(coldBoxCheck , hitboxMarker.coldBoxes[hitboxMarker.active] != -1);
					if(nk_checkbox_label(context , "Cold Box" , coldBoxCheck)) 
						hitboxMarker.coldBoxes[hitboxMarker.active] = hitboxMarker.coldBoxes[hitboxMarker.active] != -1 ? -1:hitboxMarker.active;
								
					nk_layout_row_dynamic(context , 30 , 2);
					if(nk_button_label(context , "Save As")) {
						
						Supplier<String> fileName = DialogUtils.newInputBox("Input new HitBoxSet Name", 5 , 270);
						TemporalExecutor.onTrue(() -> fileName.get() != null , () -> {
							
							HitBoxSetMarker newHitboxMarker = new HitBoxSetMarker();
							newHitboxMarker.copy(hitboxMarker);
							newHitboxMarker.editingName = fileName.get();
							hitboxMarker = newHitboxMarker;
							hitboxMarker.toHitBoxSet(active).write();
							
						});
											
					}							
					
					if(nk_button_label(context , "Save")) hitboxMarker.toHitBoxSet(active).write();
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Default Direction" , NK_TEXT_ALIGN_CENTERED);						
					
					nk_layout_row_dynamic(context , 20 , 2);
					
					put(leftDirRadio , hitboxMarker.defaultDirection == Direction.LEFT);
					put(rightDirRadio , hitboxMarker.defaultDirection == Direction.RIGHT);
					
					if(nk_radio_label(context , "Left" , leftDirRadio)) hitboxMarker.defaultDirection = Direction.LEFT;
					if(nk_radio_label(context , "Right" , rightDirRadio)) hitboxMarker.defaultDirection = Direction.RIGHT;
					
				}
				
			}
			
			Entities E ;
			if(!(active instanceof Entities) || !(E = (Entities) active).has(ECS.ANIMATIONS , ECS.HITBOXES)) {
				
				nk_end(context);
				return;
									
			}
					
			editor.setState(EditorState.EDITING_HITBOX);
			
			EntityAnimations anims = (EntityAnimations) E.components()[Entities.AOFF];
			EntityHitBoxes entityHitboxes = (EntityHitBoxes) E.components()[Entities.HOFF];

			if(hitboxsetChecks == null) {
				
				hitboxsetChecks = new ByteBuffer[entityHitboxes.numberSets()];
				for(int i = 0 ; i < entityHitboxes.numberSets() ; i ++) hitboxsetChecks[i] = alloc0(allocator);
				
			}
			
			boolean hasHitBox = entityHitboxes.has(hitboxMarker.toHitBoxSet(E));
						
			if(hitboxMarker.editingName != null) {
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 300);				
				nk_text(context , E.name() + " has " + hitboxMarker.editingName + ":" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
				nk_layout_row_push(context , 70);				
				nk_text(context , Boolean.toString(hasHitBox) , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);
				nk_layout_row_end(context);
								
				if(!(hasHitBox)) {
				
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_button_label(context , "Add " + hitboxMarker.editingName)) {
						
						entityHitboxes.addSet(hitboxMarker.toHitBoxSet(E));
						E.write();
						//update hitboxSet data structures
						hitboxsetChecks = new ByteBuffer[entityHitboxes.numberSets()];
						for(int i = 0 ; i < hitboxsetChecks.length ; i ++)hitboxsetChecks[i] = alloc0(allocator);
						
					}
					
				}
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_checkbox_label(context , "Show Animations" , showAnimationsCheck)) {
				
				spritesetChecks = new ByteBuffer[anims.size()];
				for(int i = 0 ; i < anims.size() ; i ++) spritesetChecks[i] = alloc0(allocator);
				
			}
			
			if(toBool(showAnimationsCheck)) {
				
				editor.setState(EditorState.EDITING_HITBOX);
				
				nk_layout_row_dynamic(context , 300 , 1);
				if(nk_group_begin(context , "Animations" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
					
					for(int i = 0 ; i < anims.size() ; i ++) {
						
						nk_layout_row_dynamic(context , 20 , 1);
						if(nk_selectable_text(context , anims.get(i).name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT , spritesetChecks[i])) {
							
							selectedSet = anims.get(i);
							//deselect other selectable texts
							for(int j = 0 ; j < anims.size() ; j ++) if(j != i) put(spritesetChecks[j] , false);
							selectedSprite = selectedSet.getSprite(0);
							E.swapSpriteFast(selectedSprite);
							selectedSpriteIndex = 0;

							if(selectedSprite.length % 3 != 0) {
								
								if(hitboxMarker.editingName != null) {
									
									ArrayList<float[]> hitboxes = hitboxMarker.hitboxes();
									for(float [] x : hitboxes) editor.renderer().removeFromRawData(x);
									hitboxMarker.clear();
									hitboxMarker.active = -1;
									
								}
								
								hitboxMarker.fromHitBoxSet(entityHitboxes.get((int)selectedSprite[selectedSprite.length - 1]), E);
								ArrayList<float[]> hitboxes = hitboxMarker.hitboxes();
								for(float[] x : hitboxes) editor.renderer().addToRawData(x);
								for(int j = 0 ; j < hitboxsetChecks.length ; j ++) if(j != i) hitboxsetChecks[j].put(0 , (byte) 0);
								
							}
							
						}
						
					}
					
					if(selectedSet != null) {
						
						nk_layout_row_dynamic(context , 200 , 1);
						if(nk_group_begin(context , "Select Sprite for Markup" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)){
							
							for(int i = 0 ; i < selectedSet.getNumberSprites() ; i++){
								
								if(i % 3 == 0) nk_layout_row_dynamic(context , 20 , 3);							
								if(nk_button_label(context , "Sprite " + i)) {
									
									selectedSprite = selectedSet.getSprite(selectedSpriteIndex = i);
									E.swapSpriteFast(selectedSprite);

									if(selectedSprite.length % 3 != 0) {
										
										if(hitboxMarker.editingName != null) {
											
											ArrayList<float[]> hitboxes = hitboxMarker.hitboxes();
											for(float [] x : hitboxes) editor.renderer().removeFromRawData(x);
											hitboxMarker.clear();
											hitboxMarker.active = -1;
											
										}
										
										hitboxMarker.fromHitBoxSet(entityHitboxes.get((int)selectedSprite[selectedSprite.length - 1]), E);
										ArrayList<float[]> hitboxes = hitboxMarker.hitboxes();
										for(float[] x : hitboxes) editor.renderer().addToRawData(x);
										for(int j = 0 ; j < hitboxsetChecks.length ; j ++) if(j != i) hitboxsetChecks[j].put(0 , (byte) 0);
										
									}
									
								}							
								
							}
							
							nk_group_end(context);
							
						}
						
					}
					
					nk_group_end(context);
					
//					if(selectedSet != previousSet) {
//						
//						ArrayList<float[]> hitboxes = hitboxMarker.hitboxes();
//						for(float [] x : hitboxes) editor.renderer().removeFromRawData(x);
//						hitboxMarker.clear();
//					
//						previousSet = selectedSet;
//						selectedSprite = null;
//						selectedSpriteIndex = -1;
//						
//					}
					
				}
				
				if(selectedSet != null && selectedSprite != null) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Sprite " + selectedSpriteIndex + " from " + selectedSet.name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					
					boolean activatesHitBox = selectedSprite.length % 3 != 0;
					if(activatesHitBox) {
						
						nk_layout_row_dynamic(context , 20 , 1);
						//hitboxset activated is the hitboxset at the index found at the end of the sprite
						int index = (int)selectedSprite[selectedSprite.length - 1];
						if(index == -1) nk_text(context , "Disables HitBoxes" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
						else {
							
							try {
								
								nk_text(context , "Activates " + entityHitboxes.get(index).name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
								
							} catch(IndexOutOfBoundsException e) {
								
								nk_text(context , "Error Occurring, is there a hitbox at " + index + "?" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
								
							}
						
						}
						
					} 
										
					nk_layout_row_dynamic(context , 30 , 1);
					if(nk_checkbox_label(context , "Show HitBoxSets To Activate" , showHitBoxSetsCheck)) {
						
					}

					if(toBool(showHitBoxSetsCheck)) {
						
						nk_layout_row_dynamic(context , 200 , 1);
						if(nk_group_begin(context , "HitBoxSets" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {

							for(int i = 0 ; i < entityHitboxes.numberSets() ; i ++) {
								
								if(i % 2 == 0) nk_layout_row_dynamic(context , 20 , 2);
								
								//whatever is selected from here will be displayed and rendered, but let user confirm they want 
								//whatever is selected.								
								if(nk_selectable_text(context , entityHitboxes.get(i).name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT , hitboxsetChecks[i])) {
									
									if(!entityHitboxes.get(i).name().equals(hitboxMarker.editingName)) {
										
										if(hitboxMarker.editingName != null) {
											
											ArrayList<float[]> hitboxes = hitboxMarker.hitboxes();
											for(float [] x : hitboxes) editor.renderer().removeFromRawData(x);
											hitboxMarker.clear();
											hitboxMarker.active = -1;
											
										}

										hitboxMarker.fromHitBoxSet(entityHitboxes.get(i), E);
										ArrayList<float[]> hitboxes = hitboxMarker.hitboxes();
										for(float[] x : hitboxes) editor.renderer().addToRawData(x);
										for(int j = 0 ; j < hitboxsetChecks.length ; j ++) if(j != i) hitboxsetChecks[j].put(0 , (byte) 0);

									} 								

								}

							}
							
							nk_group_end(context);
							
						}

					}
					
					if(hitboxMarker.editingName != null) {
						
						nk_layout_row_dynamic(context , 30 , 1);
						//make the spriteset sprite activate the selected hitbox above
						if(nk_button_label(context , "Make Sprite Activate " + hitboxMarker.editingName)) {
							
							float[] replacementSprite = new float [activatesHitBox ? selectedSprite.length : selectedSprite.length + 1];
							System.arraycopy(selectedSprite, 0, replacementSprite, 0, selectedSprite.length);
							replacementSprite[replacementSprite.length - 1] = entityHitboxes.indexOf(hitboxMarker.toHitBoxSet(E));
							selectedSet.replaceSprite(selectedSpriteIndex, replacementSprite);
							selectedSet.write();
							selectedSprite = replacementSprite;

						}

					}

				}	

			}
			
		}
			
		nk_end(context);
		
	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Item Editor						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect itemEditorRect = NkRect.malloc(allocator).x(5).y(125).w(450).h(550);
	private boolean showItemEditor = true;
	boolean firstTimeOpeningItemEditor = true;	
	IntBuffer activeItemSelector = allocator.ints(-1);
	private ByteBuffer equippableCheck = alloc0(allocator);
	private ByteBuffer usableCheck = alloc0(allocator);
	private ByteBuffer materialsCheck = alloc0(allocator);
	private ByteBuffer hitboxableCheck = alloc0(allocator);
	private ByteBuffer consumableCheck = alloc0(allocator);	
	private ByteBuffer flagsCheck = alloc0(allocator);
	private ByteBuffer itemInfoCheck = alloc0(allocator);	
	
	void layoutItemEditor() {
		
		if(!showItemEditor) return;
		
		int options = firstTimeOpeningItemEditor ?
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED:
				NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE; 

		if(nk_begin(context , "Items Editor" , itemEditorRect , options)) {
			
			firstTimeOpeningItemEditor = false;
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) {
				
				Supplier<String> newItemName = DialogUtils.newInputBox("Item Name", 5 , 270);
				TemporalExecutor.onTrue(() -> newItemName.get() != null , () -> scene.items().newItem(newItemName.get()));
								
			}
			
			if(nk_button_label(context , "Load")) {
				
				Supplier<String> loadPath = DialogUtils.newFileExplorer("Load an Item", 5 , 270 , false , false);
				TemporalExecutor.onTrue(() -> loadPath.get() != null , () -> {
					
					Items newItem = scene.items().load((String) toNamePath(loadPath.get()));
					float[] cursor = editor.getCursorWorldCoords(); 
					newItem.moveTo(cursor[0] , cursor[1]);
						
				});
				
			}
			
			if(nk_button_label(context , "Delete")) {
				
				Supplier<String> loadPath = DialogUtils.newFileExplorer("Delete an Item", 5 , 270 , false , false);
				TemporalExecutor.onTrue(() -> loadPath.get() != null , () -> editor.deleteItems(loadPath.get()));
				
			}
			
			if(active == null || !(active instanceof Items)) {
				
				nk_end(context);
				return;
				
			}
			
			editor.cursorState(CursorState.FROZEN);
			
			Items activeItem = (Items) active;
			
			//set checkboxes of item components each frame becasue other items may be selected
			put(equippableCheck , activeItem.has(ItemComponents.EQUIPPABLE));
			put(usableCheck , activeItem.has(ItemComponents.USABLE));
			put(materialsCheck , activeItem.has(ItemComponents.MATERIALS));
			put(hitboxableCheck , activeItem.has(ItemComponents.HITBOXABLE));
			put(consumableCheck , activeItem.has(ItemComponents.CONSUMABLE));
			put(flagsCheck , activeItem.has(ItemComponents.FLAGS));
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , activeItem.name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Save")) activeItem.write();
			
			if(nk_button_label(context , "Save As")) {}
			
			if(nk_button_label(context , "Remove")) {
				
				editor.removeActive();
				nk_end(context);
				return;				
				
			}
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Texture")) {
				
				editor.textureActive();
				TemporalExecutor.onTrue(() -> active.isTextured() , () -> active.fitQuadToTexture());
			
			}
			
			if(nk_button_label(context , "Remove Color")) {
				
				Supplier<float[]> color = DialogUtils.newColorChooser("Select a Color to Remove", 5 , 270);
				TemporalExecutor.onTrue(() -> color.get() != null, () -> activeItem.removeColor(color.get()[0] , color.get()[1], color.get()[2]));
				
			}
			
			if(nk_button_label(context , "Filter Color")) {
				
				Supplier<float[]> color = DialogUtils.newColorChooser("Select a Color to Filter", 5 , 270);
				TemporalExecutor.onTrue(() -> color.get() != null, () -> activeItem.setFilter(color.get()[0] , color.get()[1], color.get()[2]));
								
			}
			
			active.roundVertices();
			editor.selection.roundVertices();
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Reset Dims and UVs")) { 
				
				active.fitQuadToTexture();
				active.setLeftUCoords(0);
				active.setRightUCoords(1);
				active.setTopVCoords(1);
				active.setBottomVCoords(0);
				
			}
			
			if(nk_button_label(context , "Save Selection as Icon")) {
				
				float[] vertices = activeItem.getData();
				float[] selection = editor.selection.vertices;
				
				float leftUDistance = selection[27] - vertices[27];
				float rightUDistance = selection[0] - vertices[27];
				
				float[] qDims = {activeItem.getWidth() , activeItem.getHeight()};
				
				float leftU = leftUDistance / qDims[0];
				float rightU = rightUDistance / qDims[0];
				
				float bottomVDistance = selection[1] - vertices[1];
				float topVDistance = selection[10] - vertices[1];
				
				float bottomV = bottomVDistance / qDims[1];
				float topV = topVDistance / qDims[1];
				
				float[] selectionDims = editor.selection.getDimensions();
				
				if(leftU < 0.0f) leftU = 0.0f;
				if(rightU > 1.0f) rightU = 1.0f;
				if(topV > 1.0f) topV = 1.0f;
				if(bottomV < 0f) bottomV = 0f;
				
				activeItem.iconSprite(new float [] {leftU, rightU , topV, bottomV , Math.round(selectionDims[0] / 2) , Math.round(selectionDims[1] / 2f)});
				
			}
						
			if(nk_button_label(context , "Iconify")) activeItem.iconify();
			
			nk_layout_row_dynamic(context , 30 , 2);			
			if(nk_button_label(context , "Max Stack Size")) {
				
				Supplier<String> stackSize = DialogUtils.newInputBox("Max Size of a Stack in an Inventory", 5, 270, DialogUtils.NUMBER_FILTER);
				TemporalExecutor.onTrue(() -> stackSize.get() != null , () -> activeItem.maxStackSize((int)toNumber(stackSize.get())));
				
			}
			
			if(nk_button_label(context , "Icon Animation")) {
				
				Supplier<String> animation = DialogUtils.newFileExplorer("Select an Animation", 5, 270, data + "spritesets/");
				TemporalExecutor.onTrue(() -> animation.get() != null , () -> activeItem.setIconAnimation(toNamePath(animation.get())));
				
			}
			
			nk_layout_row_dynamic(context , 250 , 1);
			if(nk_group_begin(context , "Item Components" , NK_WINDOW_TITLE|NK_WINDOW_BORDER)) {

				nk_layout_row_begin(context , NK_STATIC , 30 , 2);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Equippable" , equippableCheck)) activeItem.toggleComponents(ItemComponents.EQUIPPABLE);
				if(activeItem.has(ItemComponents.EQUIPPABLE)) {
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , " Slot (" + activeItem.componentData().equipSlot() + ")")) {
						
						Supplier<String> slotNumber = DialogUtils.newInputBox("Equip Slot", 5 , 270 , textFilter);
						TemporalExecutor.onTrue(() -> slotNumber.get() != null , () -> {
							
							try {
								
								int slotInt = (int)toNumber(slotNumber.get());
								if(slotInt < 0) throw new NumberFormatException();
								activeItem.componentData().equipSlot(slotInt);
								
							
								
							} catch(NumberFormatException e) {
								
								editor.say("Not a valid number: " + slotNumber);
								
							}
							
						});
						
					}
					
				}
				
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 3);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Usable" , usableCheck)) activeItem.toggleComponents(ItemComponents.USABLE);
				if(activeItem.has(ItemComponents.USABLE)) {
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Script")) {
						
						Supplier<String> scriptFile = DialogUtils.newFileExplorer("Select a Script to Execute on Item Use", 5 , 270 , false , false , data + "scripts/");
						TemporalExecutor.onTrue(() -> scriptFile.get() != null, () -> {
							
							String scriptPath = (String) toNamePath(scriptFile.get());
							if(scriptPath.endsWith(".py"))activeItem.componentData().onUse(scriptPath);
							else editor.say("Not a valid python script: " + scriptFile.get());
							
							
						});
																
					}
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Recompile Script")) activeItem.componentData().recompileUseScript();
					
				}			
				
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 1);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Materials" , materialsCheck)) activeItem.toggleComponents(ItemComponents.MATERIALS);
				
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 2);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Hitboxable" , hitboxableCheck)) activeItem.toggleComponents(ItemComponents.HITBOXABLE);
				if(activeItem.has(ItemComponents.HITBOXABLE)) {
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Add Hit Box")) {
						
						Supplier<String> hitboxPaths = DialogUtils.newFileExplorer("Select one or more HitBoxSets" , 120,  120 , false , true);
						TemporalExecutor.onTrue(() -> hitboxPaths.get() != null, () -> {
							
							String[] split = hitboxPaths.get().split("\\|");
							for(String y : split) activeItem.componentData().addHitBox(new HitBoxSets(y));
							
						});
											
					}
					
				}
				
				nk_layout_row_end(context);

				nk_layout_row_begin(context , NK_STATIC , 30 , 2);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Consumable" , consumableCheck)) activeItem.toggleComponents(ItemComponents.CONSUMABLE);
							
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Chance To Consume")) {

					Supplier<String> input = DialogUtils.newInputBox("Input a Number Between 1 and 100", 120, 120);
			    	TemporalExecutor.onTrue(() -> input.get() != null , () -> activeItem.componentData().chanceToConsume((int)toNumber(input.get())));
			    	
				}
				
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 3);
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Flags" , flagsCheck)) activeItem.toggleComponents(ItemComponents.FLAGS);
				nk_layout_row_push(context , 90);
				if(activeItem.has(ItemComponents.FLAGS)) {

					if(nk_button_label(context , "Add Flag")) {
						
						Supplier<String> flagName = DialogUtils.newInputBox("Input Flag's Name" , 5 , 270);
						TemporalExecutor.onTrue(() -> flagName.get() != null , () -> activeItem.componentData().addFlag(flagName.get()));
					
					}
					
					if(nk_button_label(context , "Remove Flag")) {
						
						
						
					}
					
				}
				
				nk_layout_row_end(context);
				
				nk_group_end(context);
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_checkbox_label(context , "Show Info" , itemInfoCheck)) ;
			
			if(itemInfoCheck.get(0) == 1) {
								
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Equippable" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "slot:" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + activeItem.componentData().equipSlot() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Equip Script:" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , activeItem.componentData().onEquipScript() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Unequip Script:" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , activeItem.componentData().onUnequipScript() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Usable" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Use Script" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , activeItem.componentData().useScript() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Hitboxable" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_button_label(context , "List Hit Boxes")) {
					
					EntityHitBoxes hb = activeItem.componentData().HitBoxable();
					ArrayList<HitBoxSets> sets = hb.getSets();
					for(int i = 0 ; i < sets.size() ; i ++) editor.say(sets.get(i).name());
					
				}
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Consumable" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Chance to Consume" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + activeItem.componentData().chanceToConsume() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
			
				if(activeItem.has(ItemComponents.FLAGS)) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Flags" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
					
					activeItem.componentData().getFlagsMap().forEach((hash , list) -> {
						
						list.forEachVal(flag -> {
							
							nk_layout_row_dynamic(context , 20 , 1);
							nk_text(context , flag , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
							
						}) ;
						
					});
					
				}
				
			}
			
		}				
		
		nk_end(context);
	
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Tile Set Editor					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect tileSetEditorRect = NkRect.malloc(allocator).set(5, 165, 450, 1075 - 170);
	private boolean showTileSetEditor = true;
	private boolean firstTimeOpeningTileSetEditor = true;
	
	ByteBuffer renderTileSheet = alloc1(allocator);
	private boolean showTilePlacer = false;
	NkImage backgroundTileSetSpriteSheet = NkImage.malloc(allocator);
	NkImage foregroundTileSetSpriteSheet = NkImage.malloc(allocator);
	CSLinked<NkImage> backgroundTileIcons = new CSLinked<>();
	CSLinked<NkImage> foregroundTileIcons = new CSLinked<>();
	
	void layoutTileSetEditor() {
		
		if(!showTileSetEditor) return;
		
		int options;			
		
		if(firstTimeOpeningTileSetEditor) {
			
			options = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_TITLE|NK_WINDOW_MINIMIZED|NK_WINDOW_MINIMIZABLE;
			firstTimeOpeningTileSetEditor = false;
			
		} else options = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_SCALABLE;
		
		if(nk_begin(context , "Tile Set Editor" , tileSetEditorRect , options)) {
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) {
				
				Supplier<String> name = DialogUtils.newInputBox("Input Tile Set Name", 5, 270);
				TemporalExecutor.onTrue(() -> name.get() != null, () -> {
					
					Supplier<String> texture = DialogUtils.newFileExplorer("Select a Texture", 5, 270, false, false , assets + "spritesheets/");
					TemporalExecutor.onTrue(() -> texture.get() != null	, () -> {
						
						editor.newTileSet(name.get() , texture.get());
						
					});

				});
				
			}
			
			if(nk_button_label(context , "Load")) {
				
				Supplier<String> filepath = DialogUtils.newFileExplorer("Select Tile Set", 5 , 270, data + "tilesets/");
				TemporalExecutor.onTrue(() -> filepath.get() != null , () -> editor.loadTileSet(filepath.get()));
				
			}
			
			if(nk_button_label(context , "Delete")) {
				
				Supplier<String> fileToDelete = DialogUtils.newFileExplorer("Delete a file", 5, 270, false, false, data + "tilesets/");
				TemporalExecutor.onTrue(() -> fileToDelete.get() != null , () -> GameFiles.delete(fileToDelete.get()));
				
			}

			try (MemoryStack s = allocator.push()){
				
				nk_layout_row_dynamic(context , 20 , 2);
				if(nk_checkbox_label(context , "Background" , toByte(s , editor.background))) {
									
					editor.background = true;					
					
				}
				
				if(nk_checkbox_label(context , "Foreground" , toByte(s , !editor.background))) {
									
					editor.background = false;
					
				}
				
				TileSets currentTileSet = editor.background ? scene.tiles1() : scene.tiles2();
				
				if(currentTileSet.uninitialized()) {
					
					nk_end(context);
					return;
					
				}
				
				editor.setState(EditorState.EDITING_TILESET);
				
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_checkbox_label(context , "Show Tile Placer" , toByte(allocator , showTilePlacer))) showTilePlacer = toggle(showTilePlacer);
				
				nk_layout_row_dynamic(context , 30 , 2);
				nk_text(context , currentTileSet.name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				if(nk_button_label(context , "Rename")) {
					
					Supplier<String> name = DialogUtils.newInputBox("Input Tile Set Name", 5, 270);
					TemporalExecutor.onTrue(() -> name.get() != null , () -> currentTileSet.name(name.get()));
					
				}
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Save")) currentTileSet.write();
				
				if(nk_button_label(context , "Clear")) {
					
					showTilePlacer = false;
					currentTileSet.clearInstances();
					put(renderTileSheet , false);
					nk_end(context);
					return;
					
				}
				
				if(currentTileSet.uninitialized() || currentTileSet.getTileSheet() == null) {
					
					nk_end(context);
					return;
					
				}
				
				editor.selection.roundVertices();
				
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_checkbox_label(context , "Render Sprite Sheet" , renderTileSheet));
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Remove Color")) {
					
					Supplier<float[]> removedColor = DialogUtils.newColorChooser("Remove Color", 5, 270);
					TemporalExecutor.onTrue(() -> removedColor.get() != null , () -> currentTileSet.remove(removedColor.get()));
					
				}
				
				if(nk_button_label(context , "ReTexture")) {
					
					Supplier<String> texturePath = DialogUtils.newFileExplorer("Select a Texture", 5, 270, false, false, assets + "spritesheets/");
					TemporalExecutor.onTrue(() -> texturePath.get() != null , () -> currentTileSet.texture(texturePath.get()));
					
				}
				
				FloatBuffer sliders = s.callocFloat(2);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Move Tile Sheet Horizontally" , -999 , sliders.slice(0 , 1) , 999 , 1.0f , 1.0f);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Move Tile Sheet Vertically" , -999 , sliders.slice(1 , 1) , 999 , 1.0f , 1.0f);
				
				currentTileSet.getTileSheet().translate(sliders.get() , sliders.get());	
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Save Selection Area As Tile") && editor.selection != null) {
					
					float[] vertices = currentTileSet.getTileSheet().getData();
					float[] selection = editor.selection.vertices;
					
					float leftUDistance = selection[27] - vertices[27];
					float rightUDistance = selection[0] - vertices[27];
					
					float[] qDims = {currentTileSet.getTileSheet().getWidth() , currentTileSet.getTileSheet().getHeight()};
					
					float leftU = leftUDistance / qDims[0];
					float rightU = rightUDistance / qDims[0];
					
					float bottomVDistance = selection[1] - vertices[1];
					float topVDistance = selection[10] - vertices[1];
					
					float bottomV = bottomVDistance / qDims[1];
					float topV = topVDistance / qDims[1];
					
					float[] selectionDims = editor.selection.getDimensions();
					
					if(leftU < 0.0f) leftU = 0.0f;
					if(rightU > 1.0f) rightU = 1.0f;
					if(topV > 1.0f) topV = 1.0f;
					if(bottomV < 0f) bottomV = 0f;
					
					currentTileSet.addSourceTile(new float[] {leftU , rightU , topV , bottomV , selectionDims[0] , selectionDims[1]});				
					Tuple2<NkImage , NkRect> subRegionResult = subRegion(
							editor.background ? backgroundTileSetSpriteSheet : foregroundTileSetSpriteSheet , currentTileSet.textureInfo() , 
							(short)leftUDistance , (short)(vertices[10] - selection[10]) , (short)selectionDims[0] , (short)selectionDims[1]);
					
					if(editor.background) backgroundTileIcons.add(subRegionResult.getFirst());
					else foregroundTileIcons.add(subRegionResult.getFirst());
					
				}
				
				nk_layout_row_dynamic(context , 600 , 1);
				if(nk_group_begin(context, "Tiles" , NK_WINDOW_TITLE|NK_WINDOW_BORDER)) {
					
					CSLinked<NkImage> tileIcons = editor.background ? backgroundTileIcons : foregroundTileIcons;
					cdNode<Tiles> iter = currentTileSet.sourcesIter();
					cdNode<NkImage> iterImage = tileIcons.get(0);
					
					Tiles tile;
					NkImage tileIcon;
					
					for(int i = 0 ; i < currentTileSet.numberSourceTiles() ; i ++ , iter = iter.next , iterImage = iterImage.next) {
						
						tile = iter.val;
						tileIcon = iterImage.val;
						
						if(i % 2 == 0) {
							
							int rowSize = 230;
							if(tile.hasCollider()) rowSize += 50;
							if(tile.isColliderTriangle()) rowSize += 100;
							
							nk_layout_row_dynamic(context , rowSize , 2);
							
						}
						
						nk_image(context , tileIcon);
						
						tile.removeMarked();
						
						if(nk_group_begin(context , "" , NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR)) {
							
							nk_layout_row_dynamic(context , 20 , 1);
							nk_text(context , tile.name() + " ID: " + tile.getID(), NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_TOP);
							
							nk_layout_row_dynamic(context , 30 , 2);
							if(nk_button_label(context , "Name")) {
								
								Supplier<String> newName = DialogUtils.newInputBox("Rename Tile" , 5 , 270);
								Tiles query = tile;
								TemporalExecutor.onTrue(() -> newName.get() != null , () -> query.setName(newName.get()));
								
							}
							
							if(nk_button_label(context , "Remove")) {
								
								iter.val.forEachInstance(removeTile -> currentTileSet.removeInstance(removeTile));
								iter.val.removeInstances();
								iter = currentTileSet.safeRemoveSource(iter);
								iterImage = tileIcons.safeRemove(iterImage);
								nk_end(context);
								return;
								
							}
							
							nk_layout_row_dynamic(context , 30 , 1);
							if(nk_button_label(context , "Animate")) {
								
								Supplier<String> animation = DialogUtils.newFileExplorer("Select Animation", 5, 270, data + "spritesets/");
/*java*/						Tiles t = tile;
								TemporalExecutor.onTrue(() -> animation.get() != null , () -> t.setAnimation(new SpriteSets(toNamePath(animation.get()))));	
								
							}
							
							if(iter.val.getAnimation() != null) { 
								
								nk_layout_row_dynamic(context , 20 , 2);
								nk_text(context , "Animation" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
								nk_text(context , iter.val.getAnimation().name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
								
							}
														
							nk_layout_row_dynamic(context , 30 , 2);
							if(nk_button_label(context , "Copy")) {
								
								Tiles deepCopy = iter.val.copy();
								currentTileSet.addSourceTile(deepCopy);
								NkImage iterImageCopy = NkImage.malloc(allocator).set(tileIcon);
								tileIcons.add(iterImageCopy);
								
							}
														
							if(nk_button_label(context , "Filter")) {
								
								Supplier<float[]> color = DialogUtils.newColorChooser("Filter a Color", 5, 270);
								Tiles query = iter.val;
								TemporalExecutor.onTrue(() -> color.get() != null , () -> query.filterColor(color.get()));
								
							}
							
							nk_layout_row_dynamic(context , 30 , 2);
							if(nk_button_label(context , "Reflect X-Wise")) iter.val.reflectXWise();
							
							if(nk_button_label(context , "Reflect Y-Wise")) iter.val.reflectYWise();
							
							nk_layout_row_dynamic(context , 20 , 1);
							
							if(nk_checkbox_label(context , "Has Collider" , toByte(s , tile.hasCollider()))) tile.toggleCollider();
							
							if(tile.hasCollider()) {
																
								nk_layout_row_dynamic(context , 20 , 2);
								if(nk_checkbox_label(context , "Upper Right" , toByte(s , tile.isColliderUpperRightTriangle()))) 
									tile.toggleUpperRightTriangle();
								
								if(nk_checkbox_label(context , "Upper Left" , toByte(s , tile.isColliderUpperLeftTriangle()))) 
									tile.toggleUpperLeftTriangle();
								
								nk_layout_row_dynamic(context , 20 , 2);
								if(nk_checkbox_label(context , "Lower Right" , toByte(s , tile.isColliderLowerRightTriangle()))) 
									tile.toggleLowerRightTriangle();
								
								if(nk_checkbox_label(context , "Lower Left" , toByte(s , tile.isColliderLowerLeftTriangle()))) 
									tile.toggleLowerLeftTriangle();							
								
								nk_layout_row_dynamic(context , 20 , 1);
								if(nk_checkbox_label(context , "Platform" , toByte(s , tile.isColliderPlatform()))) tile.togglePlatform();
								
								if(tile.isColliderTriangle()) {
									
									FloatBuffer triangleSliders = s.callocFloat(4);
									
									nk_layout_row_dynamic(context , 20 , 1);
									nk_property_float(context , "Mod Width" , - 999 , triangleSliders.slice(0, 1) , 999 , 1 , 1);
									
									nk_layout_row_dynamic(context , 20 , 1);
									nk_property_float(context , "Mod Height" , - 999 , triangleSliders.slice(1, 1) , 999 , 1 , 1);
									
									nk_layout_row_dynamic(context , 20 , 1);
									nk_property_float(context , "Mod X Pos" , - 999 , triangleSliders.slice(2, 1) , 999 , 1 , 1);
									
									nk_layout_row_dynamic(context , 20 , 1);
									nk_property_float(context , "Mod Y Pos" , - 999 , triangleSliders.slice(3, 1) , 999 , 1 , 1);
									
									tile.modColliderWidth(triangleSliders.get());
									tile.modColliderHeight(triangleSliders.get());
									tile.modColliderOffset(triangleSliders.get() , triangleSliders.get());
									
								} 
								
							}
							
							nk_group_end(context);
							
						}
						
					}
					
					nk_group_end(context);
					
				}
				
			}
			
		}
				
		nk_end(context);
				
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Tile Placer						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	NkRect tilePlacerRect = NkRect.malloc(allocator).set(460 , 165 , 375 , 800);
	
	void layoutTilePlacer() {
		
		if(!showTilePlacer) return;
		
		if(nk_begin(context , "Tile Placer" , tilePlacerRect , NK_WINDOW_BORDER |NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE |NK_WINDOW_TITLE)) {

			TileSets currentTileSet = editor.background ? scene.tiles1() : scene.tiles2();
			
			if(active instanceof Tiles) {
				
				Tiles activeTile = (Tiles)active;
				editor.setState(EditorState.GENERIC);
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Remove " + ((Tiles)active).toStringAndDetails())) {
					
					//remove from corresponding source tile's list
					activeTile.markForRemoval();
					currentTileSet.removeInstance(activeTile);
					editor.activeQuad = null;
					nk_end(context);
					return;
					
				}
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Move to Front")) currentTileSet.moveInstanceToFront(activeTile.getID());
				if(nk_button_label(context , "Move Forward")) currentTileSet.moveInstanceForward(activeTile.getID());
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Move to Back")) currentTileSet.moveInstanceToBack(activeTile.getID());					
				if(nk_button_label(context , "Move Backward")) currentTileSet.moveInstanceBackward(activeTile.getID());
								
			}
			
			cdNode<Tiles> iter = currentTileSet.sourcesIter();
			cdNode<NkImage> imageIter = editor.background ?  backgroundTileIcons.get(0) : foregroundTileIcons.get(0);
			NkImage tileIcon;
			Tiles tile;
			for(int i = 0 ; i < currentTileSet.numberSourceTiles() ; i ++ , iter = iter.next , imageIter = imageIter.next) {
				
				tile = iter.val;
				
				if(i % 2 == 0) nk_layout_row_dynamic(context , 150 , 2);
				tileIcon = imageIter.val;

				if(nk_button_image(context , tileIcon)) {
					
					editor.activeQuad = editor.copyTile(tile);
					editor.cursorState(CursorState.DRAGGING);
					
				}
				
			}
			
		}
		
		nk_end(context);
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Testing Panel					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
	
	private NkRect testRect = NkRect.malloc(allocator).x(5).y(1070 - 405).w(400).h(400);
	private boolean firstTimeOpeningTest = true;
	
	LootTables table;
	
	void layoutTest(){	
					
		if(nk_begin(context , "T*st" , testRect , NK_WINDOW_BORDER|NK_WINDOW_MOVABLE| NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_SCALABLE)) {
	
			if(firstTimeOpeningTest) { 
				
				firstTimeOpeningTest = false;
				table = new LootTables(scene);
				table.addItem("Heart", 100 , 1);
				table.addItem("Holy Cross" , 5.0f , 1);
				table.addItem("Sword", 25, 1);
				table.addItem("testConsumable", 39, 1);
				table.moveTo(640 , 240);
				
			}
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "chance item")) table.computeLootTable();
			
			if(nk_button_label(context , "print")) table.print();
				
			if(nk_button_label(context , "text")) {
				
				Supplier<String> input = DialogUtils.newInputBox("test number", 5, 270, DialogUtils.NUMBER_FILTER);
				TemporalExecutor.onTrue(() -> input.get() != null, () -> table.computeLootTable(Float.parseFloat(input.get())));
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "test")) {
				
				
				
			}		
			
		}	
		
		nk_end(context);

	} 
	
	float getMoveSpeed(){
	
		return floatMoveSpeed.get(0);

	}

	Consumer<Levels> onLevelLoad = newLevel -> currentLevel = newLevel; 
	
	void shutDown() {
				
		textFilter.free();
		defaultFilter.free();	
		
		
	}
	
	void resetUIFirstTimeVariables() {
		
		firstTimeOpeningFilePanel = true;
		firstTimeOpeningTools = true;
		firstTimeOpeningEngineEditor = true;
		firstTimeOpeningDebugInfo = true;
		firstTimeOpeningMacroLevelEditor = true;
		firstTimeOpeningLevelEditor = true;
		firstTiemOpeningQuadEditor = true;
		firstTimeOpeningSpriteSetEditor = true;
		firstTimeOpeningCollisionsEditor = true;
		firstTimeOpeningStaticObjects = true;
		firstTimeOpeningStaticInfo = true;
		firstTimeOpeningComponentViewer = true;
		firstTimeOpeningComponentsEditor = true;
		firstTimeOpeningEntityEditor = true;
		firstTimeOpeningHitBoxEditor = true;
		firstTimeOpeningItemEditor = true;
		firstTimeOpeningEntityPythonIntrospector = true;
		firstTimeOpeningTileSetEditor = true;
		firstTimeOpeningSceneInfo = true;
		
	}
	
}
