package CSUtil.Dialogs;

import static org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_SELECTABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_edit_string;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.system.MemoryUtil.memUTF8Safe;
import static org.lwjgl.system.MemoryUtil.nmemCalloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.nuklear.NkPluginFilterI;
import org.lwjgl.system.MemoryStack;

import CS.UserInterface;

public final class InputBox extends UserInterface implements Acceptable {

	//variables used for the text input box
	private ByteBuffer stringBuffer; 
	private IntBuffer inputLength;	
	private NkPluginFilterI filter;
	String result = null;
	private long UIMemory;
	private MemoryStack allocator;
	
	private static final int options = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZABLE;
	
	{
		show = true;
	}
	
	InputBox(String title , int x , int y , NkPluginFilterI filter) {

		super(title , x , y , 350 , 110 , options , options);
		
		UIMemory = nmemCalloc(1 , 1024);		
		allocator = MemoryStack.ncreate(UIMemory, 1024);
		
		stringBuffer = allocator.malloc(999);
		inputLength = allocator.mallocInt(1);
		this.filter = filter;
		
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , stringBuffer , inputLength , 999 , filter);
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Accept")) {
				
				end = true;
				onAccept();
				
			}
			
			if(nk_button_label(context , "Cancel")) end = true;
			
		});
		
	}
	
	InputBox(String title , int x , int y) {

		super(title , x , y , 350 , 110 , options , options);
		
		UIMemory = nmemCalloc(1 , 1024);		
		allocator = MemoryStack.ncreate(UIMemory, 1024);
		
		stringBuffer = allocator.malloc(999);
		inputLength = allocator.mallocInt(1);
		filter = DEFAULT_FILTER;

		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , stringBuffer , inputLength , 999 , filter);
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Accept")) {
				
				end = true;
				onAccept();
				
			}
			
			if(nk_button_label(context , "Cancel")) end = true;
			
		});
		
		onEnd(() -> nmemFree(UIMemory));
		
	}
	
	public void onAccept() {
		
		result = memUTF8Safe(stringBuffer.slice(0, inputLength.get(0)));
		
	}

}
