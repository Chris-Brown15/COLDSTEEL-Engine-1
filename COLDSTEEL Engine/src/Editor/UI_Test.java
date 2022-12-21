package Editor;

import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import CS.UserInterface;
import Game.Items.LootTables;
import Physics.ForceType;

public class UI_Test extends UserInterface {

	LootTables table;
	volatile boolean shouldRun = false;
	
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
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "chance item")) table.computeLootTable();
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "initialize test")) {

				editor.schedule(e -> {

					editor.scene.quads1().add();
					editor.scene.quads1().add();
					editor.scene.quads1().get(1).translate(1000, 1000);
					
					editor.scene.quads1().get(0).setSnapToPixels(false);
					editor.scene.quads1().get(1).setSnapToPixels(false);
					
					shouldRun = true;
					
				});
								
			}
			
			if(nk_button_label(context , "Test new Impulse")) {
				
				editor.schedule(e -> {
					
					e.scene.kinematics().impulse(ForceType.LINEAR , 1000d , 0.5f , 0.0f , 0.0f, 0.0f , editor.scene.quads1().get(0));
					e.scene.kinematics().then(ForceType.LINEAR , 1000d , 0.5f , 0.0f , 0.0f, 0.0f);

					e.scene.kinematics().impulse(ForceType.LINEAR , 1000d , 0.5f , 0.0f , 0.0f, 0.0f , editor.scene.quads1().get(1));
					e.scene.kinematics().then(ForceType.LINEAR , 1000d , 0.5f , 0.0f , 0.0f, 0.0f);
					
				});
				
			}			
			
			if(shouldRun) {
				
				editor.scene.kinematics().process(editor.scene.quads1().get(0));
				System.out.println("running");
			}
				
		});
		
	}
	
	public void show() {
		
		show = true;
		
	}

}