package Core;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_SCALABLE;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_SELECTABLE;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_button_symbol;
import static org.lwjgl.nuklear.Nuklear.nnk_edit_string;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import static org.lwjgl.system.MemoryUtil.nmemFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;

import static CSUtil.BigMixin.dr;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import CS.Engine;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;

/**
 * 
 * This class represents the game console. In here we can execute code or debug issues.
 * We'll make this a singleton-like class. We'll make only one, but we don't need to have only one instance.
 * 
 *
 */
public class Console {

	private static final NkContext context = Engine.NuklearContext();
	private static final MemoryStack allocator = Engine.UIAllocator();
	private static final int TEXT_FIELD_OPTIONS = NK_EDIT_FIELD|NK_EDIT_SELECTABLE;
	private static final long FILTER_ADDRESS = NkPluginFilter.create(Nuklear::nnk_filter_default).address();

	int x , y , w , h;
	String title;	
	private long stringInputMemory = allocator.nmalloc(999);
	private long stringLengthMemory = allocator.nmalloc(4);
	private CSLinked<String> consoleLines = new CSLinked<String>();
	private boolean renderConsole = false;
	
	
	public Console(String title , int x , int y , int w , int h) {
		
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.title = title;
		
	}
	
	public void layout() {
		
		if(renderConsole && nk_begin(context , title , NkRect.malloc(allocator).set(x, y, w, h) , NK_WINDOW_BORDER|NK_WINDOW_TITLE|NK_WINDOW_MOVABLE|NK_WINDOW_SCALABLE)) {
			
			nk_layout_row_begin(context , NK_STATIC , 30 , 2);
			nk_layout_row_push(context , 30);
			if(nk_button_symbol(context , NK_SYMBOL_TRIANGLE_RIGHT)) {
				
				consoleLines.add(memUTF8(stringInputMemory , dr(stringLengthMemory)));
				
			}
			
			nk_layout_row_push(context , w - 50);
			nnk_edit_string(context.address() , TEXT_FIELD_OPTIONS , stringInputMemory , stringLengthMemory , 999 , FILTER_ADDRESS);
		
			nk_layout_row_end(context);
			
			cdNode<String> iter = consoleLines.get(0);
			for(int i = 0 ; i < consoleLines.size() ; i ++ , iter = iter.next) {
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text_wrap(context , iter.val);
				
			}
			
		}
		
		nk_end(context);
		
	}
	
	public static final void shutDown() {
		
		System.out.println("Shutting Down Console");		
		nmemFree(FILTER_ADDRESS);
		
	}	

	public void toggleConsole() {
		
		renderConsole = renderConsole ? false:true;
		
	}
	
	
}

