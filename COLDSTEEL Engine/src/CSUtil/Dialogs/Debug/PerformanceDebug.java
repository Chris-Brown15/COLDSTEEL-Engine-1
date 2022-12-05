package CSUtil.Dialogs.Debug;

import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import CS.Engine;
import CS.UserInterface;
import Renderer.Renderer;

public class PerformanceDebug extends UserInterface {

	private static final int uiOptions = NK_WINDOW_MOVABLE|NK_WINDOW_BORDER|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE ;
	
	public PerformanceDebug(Engine engine , String title, float x, float y) {
		
		super(title , x , y , 350 , 600 , uiOptions , uiOptions);

		layoutBody(frame -> {
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Performance" , NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Logic Thread FPS: " + Engine.framesLastSecond() , NK_TEXT_ALIGN_LEFT);
			nk_text(context , "Render Thread FPS: " + engine.renderFramesLastSecond() , NK_TEXT_ALIGN_LEFT);
						
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , "Draw Calls: " + Renderer.sceneDrawCalls() , NK_TEXT_ALIGN_LEFT);
			
			
		});
		
	}

	public void toggle() {
		
		show = show ? false : true;
		
	}
	
}
