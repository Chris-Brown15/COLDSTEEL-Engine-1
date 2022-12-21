package CSUtil.Dialogs.Debug;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;

import java.util.function.Supplier;
import CS.UserInterface;

public class FPSDebug extends UserInterface {

	private static final int options = NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR;
	//no UI elements may have the same name so we use this to change the name of each subsequent one
	private static int ID = 0;
	
	public FPSDebug(String fpsPrefix , int x , int y , Supplier<Integer> FPSGetter) {
		
		super("FPSDISPLAY" + ID++ , x , y ,  (fpsPrefix.length() * 10) + 45 , 30 , options , options);
		layoutBody((frame) -> {
			
			nk_layout_row_begin(context , NK_STATIC , 25 , 2);
			nk_layout_row_push(context , w - 40);
			nk_text(context , fpsPrefix +  " FPS:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_layout_row_push(context , 40);
			nk_text(context , "" + FPSGetter.get() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_layout_row_end(context);
			
		});
		
	}

	public void toggle() {
		
		show = show ? false : true;
		
	}
	
	public float getWidth() {
		
		return w;
		
	}
	
}
