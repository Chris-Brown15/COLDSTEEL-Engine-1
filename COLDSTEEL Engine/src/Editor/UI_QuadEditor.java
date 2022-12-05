package Editor;

import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toByte;
import static org.lwjgl.nuklear.Nuklear.NK_RGB;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_color_pick;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_radio_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.nuklear.NkColorf;

import CS.UserInterface;
import CSUtil.RefInt;
import Core.Quads;

public class UI_QuadEditor extends UserInterface {

	ByteBuffer 
		topLeftRadioButtons = ALLOCATOR.bytes(toByte(false)),
		topRightRadioButtons = ALLOCATOR.bytes(toByte(false)),
		bottomLeftRadioButtons = ALLOCATOR.bytes(toByte(false)),
		bottomRightRadioButtons = ALLOCATOR.bytes(toByte(false))
	;
	
	NkColorf selectedColor = NkColorf.malloc(ALLOCATOR);
	
	FloatBuffer 
		quadWidthMod = ALLOCATOR.callocFloat(1),
		quadHeightMod = ALLOCATOR.callocFloat(1),
		quadTranslucency = ALLOCATOR.callocFloat(1)
	;
	
	public UI_QuadEditor(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {

		super(title, x, y, w, h, normalOptions, unopenedOptions);
		layoutBody((frame) -> {
			
			if(editor.activeQuad == null || !(editor.activeQuad.getClass() == Quads.class)) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context , "Select a quad");
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Add Quad")) editor.schedule(Editor::addQuad);
			if(nk_button_label(context, "Delete Quad")) editor.schedule(Editor::removeActive);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context, "Texture")) editor.schedule(Editor::textureActive);
			if(nk_button_label(context , "Remove Texture")) editor.schedule(Editor::removeActiveTexture);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Resize To Texture")) editor.schedule(Editor::resetDimensionsAndUVs);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context, "Remove Color")) editor.schedule(Editor::removeColor);							
			if(nk_button_label(context , "Apply Filter")) editor.schedule(Editor::applyFilter);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context, "Move To Back")) editor.schedule(Editor::moveActiveToBack);
			if(nk_button_label(context, "Move Backwards"))editor.schedule(Editor::moveActiveBackward);				
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context, "Move To Front")) editor.schedule(Editor::moveActiveToFront);
			if(nk_button_label(context, "Move Forward")) editor.schedule(Editor::moveActiveForward);					
			
			nk_layout_row_dynamic(context , 30 , 1);				
			nk_property_float(context , "Mod Quad Width" , -10 , quadWidthMod  , 10 , 1 , 1);
			
			editor.schedule(e -> e.modActiveWidthBi(quadWidthMod.get(0)));
			quadWidthMod.put(0 , 0);
			
			nk_layout_row_dynamic(context , 30 , 1);				
			nk_property_float(context , "Mod Quad Height" , -10 , quadHeightMod , 10 , 1 , 1);
			
			editor.schedule(e -> e.modActiveHeightUp(quadHeightMod.get(0)));				
			quadHeightMod.put(0 , 0);
			
			quadTranslucency.put(0 , editor.activeQuad.getTranslucency());
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Translucency" , 0f , quadTranslucency , 1f , 0.01f , 0.01f);
			
			editor.schedule(e -> e.modActiveTranslucency(quadTranslucency.get(0)));
			put(quadTranslucency , 0);
			
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
				
				RefInt corner = new RefInt(-1);
				
				if(topLeftRadioButtons.get(0) == 1) corner.set(2);
				else if(topRightRadioButtons.get(0) == 1) corner.set(3);
				else if (bottomRightRadioButtons.get(0) == 1)corner.set(1);
				else if (bottomLeftRadioButtons.get(0) == 1) corner.set(0);
				
				editor.schedule(e -> e.changeActiveColor(corner.get(), selectedColor.r(), selectedColor.g(), selectedColor.b()));
				
			}
							
			nk_layout_row_dynamic(context , 10 , 1);
			nk_layout_row_dynamic(context , 250 , 1);
			if(nk_color_pick(context , selectedColor , NK_RGB)){}});

	}

	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
