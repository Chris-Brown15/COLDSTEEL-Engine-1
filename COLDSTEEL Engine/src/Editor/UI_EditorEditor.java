package Editor;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.alloc1;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toBool;
import static CSUtil.BigMixin.toByte;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_radio_label;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import CS.UserInterface;

public class UI_EditorEditor extends UserInterface {
	
	boolean showSpriteSetEdit = true;
	FloatBuffer floatMoveSpeed = ALLOCATOR.floats(1);
	
	ByteBuffer 
		quadsAtCursorCheck = ALLOCATOR.bytes(toByte(false)),
		backgroundRadio = alloc1(ALLOCATOR),
		foregroundRadio = alloc0(ALLOCATOR),	
		buildModeCheck = alloc1(ALLOCATOR),
		testModeCheck = alloc0(ALLOCATOR),
		hybridModeCheck = alloc0(ALLOCATOR) ,		
		renderDebugCheck = alloc0(ALLOCATOR),
		renderDebugColliders = alloc1(ALLOCATOR),
		renderDebugHitBoxes = alloc1(ALLOCATOR),
		renderDebugJoints = alloc1(ALLOCATOR)
	;
	
	public UI_EditorEditor(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context, "Modes" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "Build Mode" , buildModeCheck)) {
				
				editor.schedule((edi) -> {

					edi.switchTo(EditorMode.BUILD_MODE);
					testModeCheck.put(0 , (byte)0);
					hybridModeCheck.put(0 , (byte)0);
					
				});
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "Test Mode" , testModeCheck)) {
				
				editor.schedule((edi) -> {

					edi.switchTo(EditorMode.TEST_MODE);
					buildModeCheck.put(0 , (byte)0);
					hybridModeCheck.put(0 , (byte)0);
					
				});
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "hybrid Mode" , hybridModeCheck)) {
				
				editor.schedule((edi) -> {

					edi.switchTo(EditorMode.HYBRID_MODE);
					buildModeCheck.put(0 , (byte)0);
					testModeCheck.put(0 , (byte)0);
					
				});
				
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
			var cameraPos = editor.camera().cameraPosition;			
			nk_text(context , cameraPos.x + ", " + cameraPos.y , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_RIGHT);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Cursor Position" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			float[] coords = editor.cursorWorldCoords();
			nk_text(context , "X: " + coords[0] + ", Y: " + coords[1] , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Selection Area Dimensions" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_LEFT);
			float[] dims = editor.selection.getDimensions();
			nk_text(context , dims[0] + ", " + dims[1] , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context,  "Options" ,  NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 20 , 1);				
			if(nk_checkbox_label(context , "Spawn Quads At Cursor" , quadsAtCursorCheck)) editor.schedule(Editor::toggleSpawnAtCursor);			
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Render Debug" , renderDebugCheck)) editor.schedule(Editor::toggleRenderDebug);
			
			if(toBool(renderDebugCheck)) {
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 30);
				nk_text_wrap(context , "");
				nk_layout_row_push(context , 235);
				if(nk_checkbox_label(context , "Render Colliders" , renderDebugColliders));
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 30);
				nk_text_wrap(context , "");
				nk_layout_row_push(context , 235);
				if(nk_checkbox_label(context , "Render HitBoxes" , renderDebugHitBoxes));
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 30);
				nk_text_wrap(context , "");
				nk_layout_row_push(context , 235);
				if(nk_checkbox_label(context , "Render Joints" , renderDebugJoints));
				nk_layout_row_end(context);
				
			}
			
			put(foregroundRadio , !editor.background);
			put(backgroundRadio , editor.background);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_radio_label(context , "Edit Background Layers" , backgroundRadio)) editor.schedule((edi) -> edi.background = true);
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_radio_label(context , "Edit Foreground Layers" , foregroundRadio)) editor.schedule((edi) -> edi.background = false);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Set Selection Color")) editor.schedule(Editor::setSelectionColorColor);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Set Selection Opacity")) editor.schedule(Editor::setSelectionColorOpacity);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Reset Editor State to Generic")) editor.setState(EditorState.GENERIC);
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Editor Move Speed" , 0 , floatMoveSpeed , 9999 , 0.25000f , 0.25000f);
			
			editor.schedule((edi) -> edi.setMoveSpeed(floatMoveSpeed.get(0)));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(editor.activeQuad != null) nk_text(context , "Active Object: " + editor.activeQuad.toString() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			else nk_text(context , "No Active Object" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Camera Look At")) editor.schedule(Editor::moveCamera);
				
		});
		
	}

	void show() {
		
		show = true;
		
	}

	void hide() {
		
		show = false;
		
	}
	
}
