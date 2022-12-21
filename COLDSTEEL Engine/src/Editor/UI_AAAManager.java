package Editor;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZED;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_SCALABLE;


/**
 * 
 * Manages all other UI elements the editor needs for its utilities.
 *
 */
public class UI_AAAManager {

	final Editor editor;
	
	private static final int 
		TYPICAL_OPENED_OPTIONS = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE,
		TYPICAL_UNOPENED_OPTIONS = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZABLE|NK_WINDOW_MINIMIZED|NK_WINDOW_TITLE
	;
	
	UI_FilePanel file;
	UI_ToolsPanel tools;
	UI_EditorEditor editorEditor;
	UI_Debug debug;
	UI_MacroLevelEditor macroLevelEditor;
	UI_LevelEditor levelEditor;
	UI_LoadDoorEditor loadDoorEditor;
	UI_QuadEditor quadEditor;
	UI_SpriteSetEditor spriteSetEditor;
	UI_SpriteEditor spriteEditor;
	UI_SpriteSetJointEditor jointEditor;
	UI_ColliderEditor colliderEditor;
	UI_StaticEditor staticEditor;
	UI_StaticInfo staticInfo;
	UI_StaticColliderEditor staticColliderEditor;
	UI_ComponentView componentView;
	UI_ComponentEditor componentEditor;
	UI_EntityColliderEditor entityColliderEditor;
	UI_EntityEditor entityEditor;
	UI_HitBoxEditor hitboxEditor;
	UI_ItemEditor itemEditor;
	UI_RPGStatEditor rpgEditor;
	UI_TilesetEditor tilesetEditor;
	UI_SceneHierarchy sceneView;
	UI_Test test;
	
	UI_AAAManager(Editor editor) {
		
		this.editor = editor;
		file = new UI_FilePanel(editor , "File" , 5f , 5f , 140f , 280f , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		tools = new UI_ToolsPanel("Tools" , 430 , 5 , 140 , 395 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		editorEditor = new UI_EditorEditor(editor , "Editor" , 150 , 5 , 275 , 680 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		debug = new UI_Debug(editor , "Debug" , 1185 , 5 , 345 , 455 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		macroLevelEditor = new UI_MacroLevelEditor(editor , "Macrolevel Editor" , 575 , 5 ,300 , 400 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		levelEditor = new UI_LevelEditor(editor , "Level Editor" , 880 , 5 , 300 , 670 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		loadDoorEditor = new UI_LoadDoorEditor(editor , this ,  "Load Door Editor" , 575 , 125 , 300 , 650 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		quadEditor = new UI_QuadEditor(editor , "Quad Editor" , 5 , 45 , 320 , 885 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		spriteSetEditor = new UI_SpriteSetEditor(editor , this , "Sprite Set Editor" , 330 , 45 , 340 , 475 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		jointEditor = new UI_SpriteSetJointEditor(editor , this , "Sprite Joint Editor" , 330 , 525 , 340 , 220 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		spriteEditor = new UI_SpriteEditor(editor, this, "Sprite Editor", 500, 500, 400 , 400, TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		colliderEditor = new UI_ColliderEditor(editor , "Collider Editor" , 675 , 45 , 300 , 480 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		staticEditor = new UI_StaticEditor(editor , this ,  "Static Editor" , 980 , 45 , 300 , 860 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		staticInfo = new UI_StaticInfo(editor , this , "Static Info" , 1285 , 125 , 300 , 400 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		staticColliderEditor = new UI_StaticColliderEditor(editor , this , "Static Collider Editor" , 1285 , 530 , 300 , 345 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		componentView = new UI_ComponentView(editor , "Component View" , 510 , 85 , 670 , 600 , TYPICAL_OPENED_OPTIONS|NK_WINDOW_SCALABLE , TYPICAL_UNOPENED_OPTIONS);
		componentEditor = new UI_ComponentEditor(editor , "Component Editor" , 5 , 85 , 500 , 500 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		entityColliderEditor = new UI_EntityColliderEditor(editor , this , "Entity Collider Editor" , 510 , 85 , 300 , 200, TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		entityEditor = new UI_EntityEditor(editor , "Entity Editor" , 1285 , 45 , 300 , 650 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		hitboxEditor = new UI_HitBoxEditor(editor , "Hitbox Editor" , 1185 , 85 , 400 , 1050 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		itemEditor = new UI_ItemEditor(editor , "Item Editor" , 5 , 125 , 450 , 550 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		rpgEditor = new UI_RPGStatEditor(editor , this , "RPG Stat Editor" , 510 , 125 , 400 , 900 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		tilesetEditor = new UI_TilesetEditor(editor , "Tileset Editor" , 5 , 165 , 450 , 900 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		sceneView = new UI_SceneHierarchy(editor , editor.scene , "Scene Viewer" , 1590 , 85 , 300 , 600 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		test = new UI_Test(editor , "T*st" , 5 , 1075-500 , 500 , 500 , TYPICAL_OPENED_OPTIONS , TYPICAL_UNOPENED_OPTIONS);
		
	}
	
	void layoutElementsBuildMode() {
		
		file.show();
		tools.show();
		editorEditor.show();
		debug.show();
		macroLevelEditor.show();
		levelEditor.show();
		loadDoorEditor.show();		
		quadEditor.show();
		spriteEditor.show();
		spriteSetEditor.show();
		colliderEditor.show();
		staticEditor.show();
		componentView.show();
		componentEditor.show();
		entityEditor.show();
		hitboxEditor.show();
		itemEditor.show();
		tilesetEditor.show();
		sceneView.show();
		test.show();
		
	}
	
	void layoutElementsTestMode() {
		
		file.show();
		tools.hide();
		editorEditor.show();
		debug.show();
		macroLevelEditor.hide();
		levelEditor.hide();
		loadDoorEditor.hide();		
		quadEditor.hide();
		spriteSetEditor.hide();
		spriteEditor.hide();
		colliderEditor.hide();
		staticEditor.hide();
		componentView.hide();
		componentEditor.hide();
		entityEditor.hide();
		hitboxEditor.hide();
		itemEditor.hide();
		tilesetEditor.hide();
		sceneView.show();
		
	
	}
	
	void hideAllElements() {
		
		file.hide();
		tools.hide();
		editorEditor.hide();
		debug.hide();
		macroLevelEditor.hide();
		levelEditor.hide();
		loadDoorEditor.hide();		
		quadEditor.hide();
		spriteSetEditor.hide();
		spriteEditor.hide();
		colliderEditor.hide();
		staticEditor.hide();
		componentView.hide();
		componentEditor.hide();
		entityEditor.hide();
		hitboxEditor.hide();
		itemEditor.hide();
		tilesetEditor.hide();
		sceneView.hide();
		
	
	}
	
}
