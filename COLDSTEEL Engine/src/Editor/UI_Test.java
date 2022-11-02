package Editor;

import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import java.util.function.Supplier;

import CS.UserInterface;
import CSUtil.Dialogs.DialogUtils;
import Core.TemporalExecutor;
import Game.Items.LootTables;

public class UI_Test extends UserInterface {

	LootTables table;
	
	public UI_Test(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);

		layoutBody((frame) -> {
			
			if(firstTimeOpening()) { 
				
				table = new LootTables(editor.scene);
				table.addItem("Heart", 100 , 1);
				table.addItem("Holy Cross" , 5.0f , 1);
				table.addItem("Sword", 25, 1);
				table.addItem("testConsumable", 39, 1);
				table.moveTo(640 , 240);
				
			}
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "chance item")) table.computeLootTable();
			
			if(nk_button_label(context , "print")) table.print();
				
			if(nk_button_label(context , "text")) {
				
				Supplier<String> input = DialogUtils.newInputBox("test number", 5, 270, UserInterface.NUMBER_FILTER);
				TemporalExecutor.onTrue(() -> input.get() != null, () -> table.computeLootTable(Float.parseFloat(input.get())));
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "test")) {}
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "test 2")) {}
			
			if(nk_button_label(context , "test 3")) {}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "test 4")) {}
				
		});
		
	}

}