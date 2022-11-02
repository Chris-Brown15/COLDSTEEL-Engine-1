package Editor;

import static CSUtil.BigMixin.toBool;
import static CSUtil.BigMixin.toLocalDirectory;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.util.function.Supplier;

import CS.UserInterface;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import Core.TemporalExecutor;
import Game.Levels.LevelLoadDoors;
import Game.Levels.Levels;

public class UI_LoadDoorEditor extends UserInterface {

	Levels linkedLevel;
	LevelLoadDoors currentLoadDoor;
	
	public UI_LoadDoorEditor(Editor editor , UI_AAAManager manager , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {

		super(title, x, y, w, h, normalOptions, unopenedOptions);
		layoutBody((frame) -> {

			if(editor.currentLevel == null || !toBool(manager.levelEditor.loadDoorEditorCheck)) return;
					
				nk_layout_row_dynamic(context , 200 , 1);
				if(nk_group_begin(context , "Load Doors" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
					
					cdNode<LevelLoadDoors> iter = editor.currentLevel.loadDoorsIter();
					
					for(int i = 0 ; i < editor.currentLevel.numberLoadDoors() ; i ++,  iter = iter.next) {
						
						if(i % 2 == 0) nk_layout_row_dynamic(context , 20 , 2);
						if(nk_button_label(context ,  iter.val.thisLoadDoorName())) currentLoadDoor = iter.val;
						
					}
					
					nk_group_end(context); 
					
				}
				
				if(currentLoadDoor == null) {
					
					nk_end(context);
					return;
									
				}
				
				ALLOCATOR.push();
				
				var widthModder = ALLOCATOR.callocFloat(1);
				var heightModder = ALLOCATOR.callocFloat(1);
						
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Mod Load Door Width" , -999f , widthModder , 999f , 1.0f , 1.0f);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Mod Load Door Height" , -999f , heightModder , 999f , 1.0f , 1.0f);
				
				currentLoadDoor.modConditionAreaWidth(widthModder.get(0));
				currentLoadDoor.modConditionAreaHeight(heightModder.get(0));
				
				ALLOCATOR.pop();
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Select Target Level to Load")) {
					
					Supplier<String> targetLevelGetter = DialogUtils.newFileExplorer("Select Level to Link To" , 5 , 270 , false , false);
					TemporalExecutor.onTrue(() -> targetLevelGetter.get() != null ,	() -> {
						
						linkedLevel = new Levels((CharSequence)targetLevelGetter.get());
						currentLoadDoor.linkToLevel((String)toLocalDirectory(targetLevelGetter.get()));	
							
					});
					
				}
				
				if(linkedLevel == null) {
					
					nk_end(context);
					return;
					
				}
				
				nk_layout_row_dynamic(context , 170 , 1);
				if(nk_group_begin(context , linkedLevel.gameName() + " Load Loors" , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
					
					cdNode<LevelLoadDoors> targetLevelIter = linkedLevel.loadDoorsIter();
					for(int i = 0 ; i < linkedLevel.numberLoadDoors() ; i ++ , targetLevelIter = targetLevelIter.next) {
						
						if(i % 2 == 0) nk_layout_row_dynamic(context , 20 , 2);				
						if(nk_button_label(context , "Load Door: " + targetLevelIter.val.thisLoadDoorName())) {
							
							currentLoadDoor.linkToLoadDoor(targetLevelIter.val.thisLoadDoorName());
							
						}
						
					}
					
					nk_group_end(context);
					
				}
				
				if(currentLoadDoor.linkedLevel() != null && currentLoadDoor.linkedLoadDoorName() != null) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Linked Level: " + currentLoadDoor.linkedLevel() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_CENTERED);
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Load Door: " + currentLoadDoor.linkedLoadDoorName() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_CENTERED);
					
				}
		});

	}

	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}
	
}
