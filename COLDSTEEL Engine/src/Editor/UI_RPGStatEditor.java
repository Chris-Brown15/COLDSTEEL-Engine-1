package Editor;

import static CSUtil.BigMixin.toBool;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.util.function.Supplier;

import CS.UserInterface;
import CSUtil.Dialogs.DialogUtils;
import Core.TemporalExecutor;
import Core.Entities.Entities;
import Core.Entities.EntityRPGStats;
import Physics.MExpression;

public class UI_RPGStatEditor extends UserInterface {

	public UI_RPGStatEditor(Editor editor , UI_AAAManager manager , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			
			if(editor.activeQuad != null && !(editor.activeQuad instanceof Entities) && toBool(manager.componentEditor.editStatsCheck)) return;
				
			Entities E;
			
			E = (Entities)editor.activeQuad;
			Object[] components = E.components();
			
			if(components[Entities.RPGOFF] == null) {
				
				nk_layout_row_dynamic(context ,20 , 1);
				nk_text_wrap(context , "Select an Entity With RPG Stats");
				nk_end(context);
				return;
				
			}
			
			nk_layout_row_dynamic(context , 20 , 3);
			nk_text(context , "Variable Name" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "Variable Value" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			nk_text(context , "Set Value" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			EntityRPGStats stats = (EntityRPGStats) components[Entities.RPGOFF];
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Randomize All")) {
				
				Supplier<String> upperBound = DialogUtils.newInputBox("Random Upper Bound" , 5 , 590 , UserInterface.NUMBER_FILTER);
				TemporalExecutor.onTrue(() -> upperBound.get() != null , () -> {
					
					editor.tryCatch(() -> {
						
						int upperBoundValue = Integer.parseInt(upperBound.get());	

						MExpression rand = new MExpression("rand x");
						rand.setInt("x" , () -> upperBoundValue);
						
						stats.forEachStat((name , value) -> stats.setCharacteristicForName(name , rand.at()));
						stats.forEachLSM((name , value) -> stats.setLSMForName(name, rand.at())); 					
						stats.forEachSkill((name , value) -> stats.setSkillForName(name , rand.at()));
						
					}, "Error randomizing all stats, terminating action.");					
					
				});
				
			}
			
			if(nk_button_label(context , "Set All")) {
				
				Supplier<String> valueString = DialogUtils.newInputBox("Set to" , 5 , 590 , UserInterface.NUMBER_FILTER);
				TemporalExecutor.onTrue(() -> valueString.get() != null , () -> {
					
					editor.tryCatch(() -> {
						
						int value = Integer.parseInt(valueString.get());
						stats.forEachStat((name , adsf) -> stats.setCharacteristicForName(name , value));
						stats.forEachLSM((name , sdf) -> stats.setLSMForName(name, value));
						stats.forEachSkill((name , asdf) -> stats.setSkillForName(name, value));
						
					} , "Error setting all stats, terminating action.");
					
				});
				
			}

			stats.forEachStat((name , value) -> {
				
				nk_layout_row_dynamic(context , 20 , 3);
				name = name.substring(0 , 1).toUpperCase() + name.substring(1);
				nk_text(context , name , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , value + "" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);

				if(nk_button_label(context , name +  " Value")) {
					
					Supplier<String> input = DialogUtils.newInputBox("Input Value for " + name , 5 , 590 , UserInterface.NUMBER_FILTER);
					String currentName = name;
					TemporalExecutor.onTrue(() -> input.get() != null , () -> 
						editor.tryCatch(() -> stats.setCharacteristicForName(currentName , Integer.parseInt(input.get())) , ""));
					
				}
				
			});
			
			stats.forEachLSM((name , value) -> {
				
				nk_layout_row_dynamic(context , 20 , 3);
				name = name.substring(0 , 1).toUpperCase() + name.substring(1);
				nk_text(context , name , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , value + "" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);

				if(nk_button_label(context , name +  " Value")) {
					
					Supplier<String> input = DialogUtils.newInputBox("Input Value for " + name , 5 , 590 , UserInterface.NUMBER_FILTER);
					String currentName = name;
					TemporalExecutor.onTrue(() -> input.get() != null , () -> 
						editor.tryCatch(() -> stats.setLSMForName(currentName , Float.parseFloat(input.get())) , ""));
					
				}
				
			});
			
			stats.forEachSkill((name , value) -> {
				
				nk_layout_row_dynamic(context , 20 , 3);
				name = name.substring(0 , 1).toUpperCase() + name.substring(1);
				nk_text(context , name , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , value + "" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);

				if(nk_button_label(context , name +  " Value")) {
					
					Supplier<String> input = DialogUtils.newInputBox("Input Value for " + name , 5 , 590 , UserInterface.NUMBER_FILTER);
					String currentName = name;
					TemporalExecutor.onTrue(() -> input.get() != null , () -> 
						editor.tryCatch(() -> stats.setSkillForName(currentName , Integer.parseInt(input.get())) , ""));
					
				}
				
			});

		});

	}

}
