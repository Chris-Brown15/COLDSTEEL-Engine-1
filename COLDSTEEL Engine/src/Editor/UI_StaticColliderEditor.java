package Editor;

import static CSUtil.BigMixin.toByte;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_radio_label;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.nio.FloatBuffer;

import CS.UserInterface;
import Core.Statics.Statics;
import Physics.Colliders;

public class UI_StaticColliderEditor extends UserInterface{

	public UI_StaticColliderEditor(Editor editor , UI_AAAManager manager , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			
			if(editor.activeQuad == null || !(editor.activeQuad instanceof Statics) && !((Statics)editor.activeQuad).collidersFocused()) return;
			
			Statics activeAsStatic = (Statics)editor.activeQuad;
			Colliders collider = activeAsStatic.activeCollider();
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Add")) activeAsStatic.addCollider();
			if(nk_button_label(context , "Remove")) activeAsStatic.removeActiveCollider();	
							
			if(collider == null) {
				
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context, 30 , 1);
			nk_text(context , "Triangle State" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			ALLOCATOR.push();
			nk_layout_row_dynamic(context , 30 , 2);					
			if(nk_radio_label(context , "Upper Right" , toByte(ALLOCATOR , collider.isUpperRightTriangle())))collider.makeUpperRightTriangle();
			if(nk_radio_label(context , "Upper Left" , toByte(ALLOCATOR , collider.isUpperLeftTriangle()))) collider.makeUpperLeftTriangle();
			
			nk_layout_row_dynamic(context , 30 , 2);					
			if(nk_radio_label(context , "Lower Right" , toByte(ALLOCATOR , collider.isLowerRightTriangle()))) collider.makeLowerRightTriangle();
			if(nk_radio_label(context , "Lower Left" , toByte(ALLOCATOR , collider.isLowerLeftTriangle()))) collider.makeLowerLeftTriangle();
		
			if(activeAsStatic.isActiveColliderTriangle()) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Undo Triangle")) activeAsStatic.unmakeActiveColliderTriangle();
				
			}
		
			nk_layout_row_dynamic(context , 30 , 2);
						
			FloatBuffer widthMod = ALLOCATOR.callocFloat(1) , heightMod = ALLOCATOR.callocFloat(1);
			
			nk_property_float(context , "Adjust Width" , -10f , widthMod , 10f , 1f , 2f);
			nk_property_float(context , "Adjust Height" , -10f , heightMod , 10f , 1f , 2f);
			
			collider.modWidth(widthMod.get(0));
			collider.modHeight(heightMod.get(0));
			ALLOCATOR.pop();
			
		});
		
	}

	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
