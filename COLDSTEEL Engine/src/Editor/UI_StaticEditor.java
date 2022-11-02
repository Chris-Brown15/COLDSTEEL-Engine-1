package Editor;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toBool;
import static org.lwjgl.nuklear.Nuklear.NK_RGB;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_color_pick;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_radio_label;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.nuklear.NkColorf;

import CS.UserInterface;
import CSUtil.RefInt;
import Core.Statics.Statics;

public class UI_StaticEditor extends UserInterface {

	private NkColorf staticColorChooser = NkColorf.malloc(ALLOCATOR);
	private ByteBuffer upRight = alloc0(ALLOCATOR);
	private ByteBuffer upLeft = alloc0(ALLOCATOR);
	private ByteBuffer downRight = alloc0(ALLOCATOR);
	private ByteBuffer downLeft = alloc0(ALLOCATOR);
	private ByteBuffer staticParallaxCheck = alloc0(ALLOCATOR);
	private ByteBuffer staticInfoCheck = alloc0(ALLOCATOR);
	private ByteBuffer editCollidersCheck = alloc0(ALLOCATOR);
	private FloatBuffer xParallax = ALLOCATOR.floats(1f);
	private FloatBuffer yParallax = ALLOCATOR.floats(1f);
	private FloatBuffer widthAdjust = ALLOCATOR.callocFloat(1);
	private FloatBuffer heightAdjust = ALLOCATOR.callocFloat(1);
	
	public UI_StaticEditor(Editor editor , UI_AAAManager manager , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			

			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) editor.schedule(Editor::addStatic);
			
			if(nk_button_label(context , "Load")) editor.schedule(Editor::loadStatic);
			
			if(nk_button_label(context , "Delete")) editor.schedule(Editor::removeActive);

			if(editor.activeQuad == null || !(editor.activeQuad instanceof Statics)) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context , "Select a Static");
				nk_end(context);
				return;
				
			} 
			
			Statics asStatic = (Statics)editor.activeQuad;
			xParallax.put(0 , asStatic.getViewOffsetX());
			yParallax.put(0 , asStatic.getViewOffsetY());
			
			editor.setState(EditorState.EDITING_STATIC);
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , asStatic.name() , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context, "Save Static")) editor.schedule(e -> asStatic.write());
			
			if(nk_button_label(context, "Remove Static")) editor.schedule(Editor::removeActive);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Texture")) editor.schedule(Editor::textureActive);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Remove Color")) editor.schedule(Editor::removeColor);
			
			if(nk_button_label(context , "Filter Color")) editor.schedule(Editor::applyFilter);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Move To Front")) editor.schedule(Editor::moveActiveToFront);
			if(nk_button_label(context , "Move To Back")) editor.schedule(Editor::moveActiveToBack);

			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Move Forward")) editor.schedule(Editor::moveActiveForward);
			if(nk_button_label(context , "Move Backward")) editor.schedule(Editor::moveActiveBackward);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Parallax" , put(staticParallaxCheck , asStatic.hasParallax()))) 
				editor.schedule(e -> asStatic.hasParallax(toBool(staticParallaxCheck)));
			
			if(asStatic.hasParallax()) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Parallax X Offset" , -2f , xParallax , 2f , 0.5f , 0.25f);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Parallax Y Offset" , -2f , yParallax , 2f , 0.5f , 0.25f);
				
				editor.schedule(e -> {
					
					asStatic.setParallaxX(xParallax.get(0));
					asStatic.setParallaxY(yParallax.get(0));
					
				});				
											
			}
		
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , asStatic.name() + " Info" , put(staticInfoCheck , manager.staticInfo.showing()))) {
				
				if(manager.staticInfo.showing()) manager.staticInfo.hide();
				else manager.staticInfo.show();
				
			}
				
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Edit Colliders" , put(editCollidersCheck , asStatic.collidersFocused()))) { 
				
				editor.schedule(Editor::activeStaticToggleColliders);
				
			}
		
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
				
				RefInt corner = new RefInt(-1);
				if(upRight.get(0) == 1) corner.set(3);
				else if(upLeft.get(0) == 1) corner.set(2);
				else if(downRight.get(0) == 1) corner.set(1);
				else if(downLeft.get(0) == 1) corner.set(0);
				
				editor.schedule(e -> asStatic.quickChangeColor(corner.get() , staticColorChooser.r(), staticColorChooser.g(), staticColorChooser.b()));
				
			}
			
			nk_layout_row_dynamic(context , 15 , 1);
			
			nk_layout_row_dynamic(context, 250 , 1);						
			nk_color_pick(context , staticColorChooser , NK_RGB);
		
			nk_layout_row_dynamic(context, 30 ,  2);			
			nk_property_float(context , "Adjust Width" , -10f , widthAdjust , 10f , 1f , 2f);			
			nk_property_float(context , "Adjust Height" , -10f , heightAdjust , 10f , 1f , 2f);
			
			editor.schedule(e -> {
				
				asStatic.modWidthBi(widthAdjust.get(0));
				asStatic.modHeightUp(heightAdjust.get(0));
				
			});
			
			widthAdjust.put(0 , 0);
			heightAdjust.put(0 , 0);
	
		});
		
	}

	void show() {
		
		show = true;
				
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
