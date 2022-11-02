package Editor;

import static org.lwjgl.nuklear.Nuklear.NK_CHART_COLUMN;
import static org.lwjgl.nuklear.Nuklear.nk_chart_begin;
import static org.lwjgl.nuklear.Nuklear.nk_chart_end;
import static org.lwjgl.nuklear.Nuklear.nk_chart_push;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import CS.UserInterface;


public class UI_PerformanceGraph extends UserInterface {

	public UI_PerformanceGraph(String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 160 , 1);
			nk_chart_begin(context , NK_CHART_COLUMN , 10 , 0 , 100);
			nk_chart_push(context , 1f);
			nk_chart_push(context , 3f);
			nk_chart_push(context , 5f);
			nk_chart_push(context , 7f);
			nk_chart_push(context , 2f);
			nk_chart_end(context);
//			nk_plot(context , NK_CHART_COLUMN , fpsBuffer , 120 , 0);
		});

	}

}
