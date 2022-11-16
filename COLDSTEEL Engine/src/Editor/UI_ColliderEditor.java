package Editor;

import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toByte;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_radio_label;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import CS.UserInterface;
import Physics.ColliderLists;
import Physics.Colliders;

public class UI_ColliderEditor extends UserInterface {

	private ByteBuffer renderCollidersCheck = ALLOCATOR.bytes(toByte(true));
	
	public UI_ColliderEditor(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			
			var CList = editor.scene.colliders();
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_checkbox_label(context , "Render Colliders" , put(renderCollidersCheck , ColliderLists.shouldRender()))) 
				editor.schedule((edi) -> ColliderLists.toggleShouldRender());
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "New Collider")) editor.schedule((edi) -> editor.addCollider());

			if(editor.activeQuad == null || editor.activeQuad.getClass() != Colliders.class) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context,  "Select a Collider");
				nk_end(context);
				return;
				
			}

			Colliders activeCollider = (Colliders)editor.activeQuad;
			
			ByteBuffer triangleRadios = frame.bytes(
				toByte(activeCollider.isUpperRightTriangle()) ,
				toByte(activeCollider.isUpperLeftTriangle()) ,
				toByte(activeCollider.isLowerRightTriangle()) ,
				toByte(activeCollider.isLowerLeftTriangle()) ,
				toByte(activeCollider.isPlatform())
			);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "Upper Right Triangle" , triangleRadios.slice(1 , 1))) editor.schedule((e) -> activeCollider.makeUpperRightTriangle());
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "Upper Left Triangle" , triangleRadios.slice(0 , 1))) editor.schedule(e -> activeCollider.makeUpperLeftTriangle());
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "Lower Right Triangle" , triangleRadios.slice(3 , 1))) editor.schedule(e -> activeCollider.makeLowerRightTriangle());
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "Lower Left Triangle" , triangleRadios.slice(2 , 1))) editor.schedule(e -> activeCollider.makeLowerLeftTriangle());
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_radio_label(context , "Platform" , triangleRadios.slice(4 , 1))) editor.schedule(e -> activeCollider.makePlatform());
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Set Width")) editor.schedule(Editor::setActiveColliderWidth);			
			if(nk_button_label(context , "Set Height")) editor.schedule(Editor::setActiveColliderHeight);
			
			FloatBuffer widthMod = frame.callocFloat(1);
			FloatBuffer heightMod = frame.callocFloat(1);
			
			nk_layout_row_dynamic(context , 30 , 1);				
			nk_property_float(context , "Mod Collider Width" , -15f , widthMod , 15f , 1f , 1f);
			
			nk_layout_row_dynamic(context , 30 , 1);				
			nk_property_float(context , "Mod Collider Height" , -15f , heightMod , 15f , 1f , 1f);
			
			editor.schedule(e -> activeCollider.modWidth(widthMod.get()));
			editor.schedule(e -> activeCollider.modHeight(heightMod.get()));
					
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Clone Active Collider")) editor.schedule(e -> CList.copyCollider(activeCollider.getID()));
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Reset State")) editor.schedule(e -> activeCollider.resetState());
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Delete Active Collider")) editor.schedule(e -> editor.removeActive());	
			
		});

	}
	
	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}

}
