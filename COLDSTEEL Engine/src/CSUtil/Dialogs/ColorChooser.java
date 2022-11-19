package CSUtil.Dialogs;

import static org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_SELECTABLE;
import static org.lwjgl.nuklear.Nuklear.NK_RGB;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_color_pick;
import static org.lwjgl.nuklear.Nuklear.nk_edit_string;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.system.MemoryUtil.memUTF8Safe;
import static org.lwjgl.system.MemoryUtil.nmemCalloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.nuklear.NkColorf;
import org.lwjgl.system.MemoryStack;

import CS.UserInterface;

public final class ColorChooser extends UserInterface implements Acceptable {

	private NkColorf selectedColor;	
	private ByteBuffer redStringBuffer;
	private IntBuffer  redInputLength;
	
	private ByteBuffer greenStringBuffer;
	private IntBuffer  greenInputLength;
	
	private ByteBuffer blueStringBuffer;
	private IntBuffer  blueInputLength;
	
	private long UIMemory;
	private MemoryStack allocator;
	
	float[] colors = null;
	
	private static final int options = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_TITLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_NO_SCROLLBAR;
	
	ColorChooser(String title , int x , int y){
		
		super(title , (float) x , (float) y , 350f , 375f , options , options);

		show = true;		
	
		UIMemory = nmemCalloc(1 , 128);		
		allocator = MemoryStack.ncreate(UIMemory, 128);
		selectedColor = NkColorf.malloc(allocator);
				
		redStringBuffer = allocator.malloc(4);
		redInputLength = allocator.mallocInt(1);
		greenStringBuffer = allocator.malloc(4);
		greenInputLength = allocator.mallocInt(1);
		blueStringBuffer = allocator.malloc(4);
		blueInputLength = allocator.mallocInt(1);
		
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Return Picker")) {
				
				end = true;
				colors = new float[] {selectedColor.r() , selectedColor.g() , selectedColor.b()};
				
			}	
			
			if(nk_button_label(context , "Return Text")) onAccept();
			
			if(nk_button_label(context , "Cancel")) end = true;
			
			nk_layout_row_dynamic(context , 250 , 1);
			nk_color_pick(context , selectedColor , NK_RGB);
			
			nk_layout_row_dynamic(context , 30 , 3);
			
			nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , redStringBuffer , redInputLength , 4 , NUMBER_FILTER);
			nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , greenStringBuffer , greenInputLength , 4 , NUMBER_FILTER);
			nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , blueStringBuffer , blueInputLength , 4 , NUMBER_FILTER);
		
			
		});
		
		onEnd(() -> nmemFree(UIMemory));
		
	}
	
	boolean ended() {
		
		return end;
		
	}
	
	public void onAccept() {

		end = true;
		colors = new float[] {Float.parseFloat(memUTF8Safe(redStringBuffer)) / 255 , 
							  Float.parseFloat(memUTF8Safe(greenStringBuffer)) / 255, 
							  Float.parseFloat(memUTF8Safe(blueStringBuffer)) / 255};
		
	}
	
}
