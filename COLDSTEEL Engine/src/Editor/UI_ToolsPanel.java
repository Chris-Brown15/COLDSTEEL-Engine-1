package Editor;

import static CSUtil.BigMixin.alloc0;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import java.nio.ByteBuffer;

import CS.UserInterface;

public class UI_ToolsPanel extends UserInterface {
	
	ByteBuffer //check box pointers
		debug = alloc0(ALLOCATOR),
		console = alloc0(ALLOCATOR),
		quadEditor = alloc0(ALLOCATOR),
		spriteSet = alloc0(ALLOCATOR),
		collisions = alloc0(ALLOCATOR),
		staticEditor = alloc0(ALLOCATOR),
		pythonUI = alloc0(ALLOCATOR),
		entity = alloc0(ALLOCATOR),
		componentEditor = alloc0(ALLOCATOR),
		componentViewer = alloc0(ALLOCATOR),
		hitboxEditor = alloc0(ALLOCATOR),
		itemEditor = alloc0(ALLOCATOR)
	;
	
	public UI_ToolsPanel(String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {

		super(title, x, y, w, h, normalOptions, unopenedOptions);
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Debug" , debug));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Console" , console));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "Quad Editor" , quadEditor));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Spriteset Editor" , spriteSet));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Collision Editor" , collisions));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Static Obj Editor" , staticEditor));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Python UI" , pythonUI));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "Entity Editor" , entity));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "Comp. Editor" , componentEditor));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "Comp. Viewer" , componentViewer));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context, "HitBox Editor" , hitboxEditor));
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context ,  "Item Editor" , itemEditor));
			
		});

	}
	
	void show() {
		
		show = true;
		
	}

	void hide() {
		
		show = false;
		
	}
	
}
