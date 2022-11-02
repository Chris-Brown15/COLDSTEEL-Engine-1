package Editor;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toggle;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.nio.ByteBuffer;

import CS.UserInterface;
import Core.Statics.Statics;

public class UI_StaticInfo extends UserInterface {

	boolean showStaticColliders;
	ByteBuffer showStaticCollidersCheck = alloc0(ALLOCATOR);
	
	public UI_StaticInfo(Editor editor , UI_AAAManager manager , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
	
		layoutBody((frame) -> {
			
			if(editor.activeQuad == null || !(editor.activeQuad instanceof Statics)) return;
			
			Statics activeStatic = (Statics) editor.activeQuad;
			
			float[] data = activeStatic.getData();
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
			nk_text_wrap(context , "Midpoint X: " + activeStatic.getMidpoint()[0]);
			nk_text_wrap(context , "Midpoint Y: " + activeStatic.getMidpoint()[1]);
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_text(context , "colliders:" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 25 , 1);
			nk_text_wrap(context , "Number: " + activeStatic.numberColliders());
			nk_layout_row_dynamic(context , 25 , 1);
			nk_text_wrap(context , "Active: " + activeStatic.activeColliderIndex());
			
			if(nk_checkbox_label(context , "View" , put(showStaticCollidersCheck , showStaticColliders)))
				showStaticColliders = toggle(showStaticColliders);

		});
		
	}

	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
