package Core;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_SCALABLE;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_SELECTABLE;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_button_symbol;
import static org.lwjgl.nuklear.Nuklear.nk_edit_string;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import static org.lwjgl.system.MemoryUtil.memUTF8;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import CS.UserInterface;
import CSUtil.DataStructures.CircularQueue;


/**
 * 
 * This class represents the game console. In here we can execute code or debug issues.
 * We'll make this a singleton-like class. We'll make only one, but we don't need to have only one instance.
 * 
 *
 */
public class Console extends UserInterface {

	private ByteBuffer inputBuffer = ALLOCATOR.malloc(100);
	private IntBuffer inputBuffersLength = ALLOCATOR.mallocInt(1);
	
	private final CircularQueue<String> consoleText = new CircularQueue<String>(50);
	
	/*
	 * The console has to destroy old text because otherwise nuklear will slow to a crawl trying to display thousands of lines.
	 * I don't think there's a way to necessarily compute which text the user could be looking at and only make nuklear calls
	 * on that data.
	 * 
	 */
		
	public Console() {
		
		super("Console", 5f , 5f , 400f , 700f , NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_TITLE|NK_WINDOW_SCALABLE , 
												 NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_TITLE|NK_WINDOW_SCALABLE);
		layoutBody((frame) -> {
			
			nk_layout_row_begin(context , NK_STATIC , 30 , 2);
			nk_layout_row_push(context , 30);
			if(nk_button_symbol(context , NK_SYMBOL_TRIANGLE_RIGHT)) {
			
				//sends whatever they typed
				enter();
				
			}
			
			nk_layout_row_push(context  , 340);
			nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , inputBuffer , inputBuffersLength , 100 , DEFAULT_FILTER);
			nk_layout_row_end(context);
			
			consoleText.forEach((string) -> {
				
				nk_layout_row_dynamic(context , 40 , 1);
				nk_text_wrap(context , string);
				
			});
			
		});
		
	}
	
	public synchronized void sayln(Object line) {
		
		consoleText.add(line.toString());
		
	}
	
	public synchronized void enter() {
		
		if(inputBuffersLength.get(0) != 0) {
					
			String ln = memUTF8(inputBuffer.slice(0 , inputBuffersLength.get()));
			inputBuffer.reset();
			inputBuffersLength.reset();
			consoleText.add(ln);
					
		}
		
	}
	
}