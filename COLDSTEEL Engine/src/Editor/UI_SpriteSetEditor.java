package Editor;

import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toBool;
import static CSUtil.BigMixin.toByte;
import static CSUtil.BigMixin.toggle;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_radio_label;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import CS.UserInterface;
import Core.Direction;

public class UI_SpriteSetEditor extends UserInterface {
	
	private FloatBuffer swapInterval = ALLOCATOR.floats(1);	
	private boolean swapSprites = false;
		
	ByteBuffer 
		leftDefaultDirRadio = ALLOCATOR.bytes(toByte(false)),
		rightDefaultDirRadio = ALLOCATOR.bytes(toByte(false)),
		upDefaultDirRadio = ALLOCATOR.bytes(toByte(false)),
		downDefaultDirRadio = ALLOCATOR.bytes(toByte(false)),
		showSpriteEditorCheck = ALLOCATOR.bytes(toByte(false)),
		runAnimCheck = ALLOCATOR.bytes(toByte(false))
	;
	
	public UI_SpriteSetEditor(Editor editor , UI_AAAManager manager , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		layoutBody((frame) -> {	
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "New Sprite Set")) editor.schedule(Editor::newSpriteSet);
	
			if(nk_button_label(context, "Load Sprite Set")) editor.schedule(Editor::loadSpriteSet);
			
			if(editor.spriteSetEditorValidity()) {
	
				if(!toBool(manager.spriteEditor.jointEditorCheck)) editor.setState(EditorState.EDITING_ANIMATION);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text(context, editor.activeSpriteSet.name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
	
				nk_layout_row_dynamic(context,  30 , 2);
				if(nk_button_label(context, "Save Set")) editor.schedule(edi -> edi.activeSpriteSet.write());
				if(nk_button_label(context, "Delete Set")) {
					
					editor.schedule(Editor::deleteActiveSpriteSet);
					nk_end(context);
					return;
					
				}
	
				editor.snapSelectionArea();
				editor.activeQuad.roundVertices();
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Reset Dimensions And UVs")) editor.schedule(Editor::resetDimensionsAndUVs);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Swap Rate" , 0.0f , swapInterval , 1000.0f , 0.01f , 0.01f);
				
				editor.schedule(edi -> edi.activeSpriteSet.setSwapInterval(swapInterval.get(0)));
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text(context ,"Default Direction" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
				
				Direction activesDir = editor.activeSpriteSet.defaultDirection;
				
				nk_layout_row_dynamic(context , 30 , 4);
				if(nk_radio_label(context , "Left" , put(leftDefaultDirRadio , activesDir.equals(Direction.LEFT)))) 
					editor.schedule(edi -> edi.activeSpriteSet.setDefaultDirection(Direction.LEFT));
				
				if(nk_radio_label(context, "Right" , put(rightDefaultDirRadio , activesDir.equals(Direction.RIGHT))))
					editor.schedule(edi -> edi.activeSpriteSet.setDefaultDirection(Direction.RIGHT));
				
				if(nk_radio_label(context , "Up" , put(upDefaultDirRadio , activesDir.equals(Direction.UP))))
					editor.schedule(edi -> edi.activeSpriteSet.setDefaultDirection(Direction.UP));
				
				if(nk_radio_label(context , "Down" , put(downDefaultDirRadio , activesDir.equals(Direction.DOWN))))
					editor.schedule(edi -> edi.activeSpriteSet.setDefaultDirection(Direction.DOWN));
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_checkbox_label(context , "Toggle Set Sprite Editor" , put(showSpriteEditorCheck , manager.spriteEditor.showing()))) {

					if(manager.spriteEditor.showing()) manager.spriteEditor.hide();
					else manager.spriteEditor.show();
					
				}
				
				nk_layout_row_dynamic(context , 30 , 1);					
				if(nk_button_label(context , "Save Selection Area")) editor.schedule(Editor::saveSelectionAreaAsSpriteSetFrame);
					
				nk_layout_row_dynamic(context , 30 , 1);				
				if(nk_checkbox_label(context , "Play Animation" , put(runAnimCheck , swapSprites))) {
					
					editor.schedule(Editor::tryPlayActiveSpriteSet);					
					swapSprites = !(manager.spriteEditor.showing()) && toggle(swapSprites);
					
				}					
				
				if(swapSprites) editor.schedule(Editor::swapActiveAnimSprite);
				
			} else if (editor.activeSpriteSet == null) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context , "Select a sprite set");
				
			} else if (editor.activeQuad == null) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context , "Select a quad");
									
			} else if (!editor.activeQuad.isTextured()) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context , "Select a texture for " + editor.activeQuad.toString());
				
			}
		
		});
		
	}

	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
