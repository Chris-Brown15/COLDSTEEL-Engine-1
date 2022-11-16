package Editor;

import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import java.text.DecimalFormat;
import CS.Engine;
import CS.UserInterface;
import Core.TemporalExecutor;
import Renderer.Renderer;

public class UI_Debug extends UserInterface {
	
	private DecimalFormat fpsFormat = new DecimalFormat();
	
	public UI_Debug(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
	
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_label(context , "Iteration Rate (millis): " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , fpsFormat.format(Engine.iterationRateLastSecond()) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Frames last second: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , Integer.toString(Engine.framesLastSecond()) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Frames this second: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , Integer.toString(editor.scene.entities().currentFrame()) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);				
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Average Frames Per Second: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , Integer.toString(Engine.averageFramerate()) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);				
			nk_text(context , "Render Time (millis): " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , fpsFormat.format(Renderer.getRenderTime())  , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Frames per Ticks: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + editor.scene.entities().frameInterval() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Ticks last second: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + editor.scene.entities().ticksLastSecond() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Current Number Scripts:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + editor.scene.entities().numberScripts() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Kinematic Forces: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + editor.scene.kinematics().size() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Temporal Executor Objects:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + TemporalExecutor.totalSize() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_label(context , "Draws: " , NK_TEXT_ALIGN_LEFT);
			nk_label(context ,  "" + Renderer.getNumberDrawCalls() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
		
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Available kilos on JVM : " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + Runtime.getRuntime().totalMemory() / 1024 , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);

			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Free kilos on JVM : " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + Runtime.getRuntime().freeMemory() / 1024 , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 2);
			nk_text(context , "Allocated kilos on JVM: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);

			nk_layout_row_dynamic(context , 30 , 1);		
			if(nk_button_label(context , "Get UI Memory Details")) editor.schedule(Editor::getUIMemoryDetails);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Print Allocations")) editor.schedule(Editor::printEngineAllocations);
			
			if(nk_button_label(context , "Print Stack Traces")) editor.schedule(Editor::printStackTraces);
			
		});
		
	}

	void show() {
		
		show = true;
		
	}

	void hide() {
		
		show = false;
		
	}
	
}
