package CSUtil.Dialogs;

import static org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_SELECTABLE;
import static org.lwjgl.nuklear.Nuklear.NK_RGB;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_color_pick;
import static org.lwjgl.nuklear.Nuklear.nk_edit_string;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.system.MemoryUtil.memUTF8Safe;
import static org.lwjgl.system.MemoryUtil.nmemCalloc;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.nuklear.NkColorf;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

public final class ColorChooser extends DialogUtils{

	private NkColorf selectedColor;	
	private ByteBuffer redStringBuffer;
	private IntBuffer  redInputLength;
	
	private ByteBuffer greenStringBuffer;
	private IntBuffer  greenInputLength;
	
	private ByteBuffer blueStringBuffer;
	private IntBuffer  blueInputLength;
	
	float[] colors = null;
	
	ColorChooser(String title , int x , int y){
		
		this.title = title;
		
		UIMemory = nmemCalloc(1 , 128);		
		allocator = MemoryStack.ncreate(UIMemory, 128);
		selectedColor = NkColorf.malloc(allocator);
		rect = NkRect.malloc(allocator).set(x , y , 350 , 375);
		
		redStringBuffer = allocator.malloc(4);
		redInputLength = allocator.mallocInt(1);
		greenStringBuffer = allocator.malloc(4);
		greenInputLength = allocator.mallocInt(1);
		blueStringBuffer = allocator.malloc(4);
		blueInputLength = allocator.mallocInt(1);
		
	}
	
	@Override protected void layout() {
		
		if(nk_begin(context , title , rect , NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_TITLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_NO_SCROLLBAR)) {
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Return Picker")) {
				
				finished = true;
				colors = new float[] {selectedColor.r() , selectedColor.g() , selectedColor.b()};
				
			}	
			
			if(nk_button_label(context , "Return Text")) {
				
				finished = true;
				colors = new float[] {Float.parseFloat(memUTF8Safe(redStringBuffer)) / 255 , 
									  Float.parseFloat(memUTF8Safe(greenStringBuffer)) / 255, 
									  Float.parseFloat(memUTF8Safe(blueStringBuffer)) / 255};
				
			}
			
			if(nk_button_label(context , "Cancel")) {
				
				finished = true;
				
			}
			
			nk_layout_row_dynamic(context , 250 , 1);
			nk_color_pick(context , selectedColor , NK_RGB);
			
			nk_layout_row_dynamic(context , 30 , 3);
			
			nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , redStringBuffer , redInputLength , 4 , NUMBER_FILTER);
			nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , greenStringBuffer , greenInputLength , 4 , NUMBER_FILTER);
			nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , blueStringBuffer , blueInputLength , 4 , NUMBER_FILTER);
		
			
		}
		
		nk_end(context);
		
		
	}

	@Override protected void accept() {}

}
