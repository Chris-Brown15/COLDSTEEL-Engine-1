package Editor;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.toBool;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.nio.ByteBuffer;
import CS.UserInterface;
import CSUtil.DataStructures.cdNode;

public class UI_MacroLevelEditor extends UserInterface {
	
	private ByteBuffer showOSTLoopSegments = alloc0(ALLOCATOR);
	private ByteBuffer showOSTIntroSegments = alloc0(ALLOCATOR);
	
	public UI_MacroLevelEditor(Editor editor , String title , float x , float y , float w , float h , int normalOptions , int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) editor.schedule(Editor::newMacroLevel);
			
			if(nk_button_label(context , "Load")) editor.schedule(Editor::loadMacroLevel);
			
			if(nk_button_label(context , "Delete")) editor.schedule(Editor::deleteMacroLevel);
						
			if(editor.currentMacroLevel != null) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Save " + editor.currentMacroLevel.name())) editor.schedule(Editor::saveMacroLevel);
				
				//these UI let the user choose some files from the assets/sounds folder which will play in the specified manner
				nk_layout_row_dynamic(context , 30  , 1);
				if(nk_button_label(context , "Add OST Intro Segment")) editor.schedule(Editor::addMacroLevelIntroOSTSegment);
				
				if(nk_checkbox_label(context , "Show OST Intro Segments" , showOSTIntroSegments));
				
				if(nk_button_label(context , "Add OST Loop Segment")) editor.schedule(Editor::addMacroLevelLoopOSTSegment);
					
				if(nk_checkbox_label(context , "Show OST Segments" , showOSTLoopSegments));
				
				if(toBool(showOSTIntroSegments)) {
										
					cdNode<String> iter = editor.currentMacroLevel.OSTIntroSegmentsIter();
					for(int i = 0 ; i < editor.currentMacroLevel.OSTIntroSegmentsSize() ; i ++ , iter = iter.next) {
													
						nk_layout_row_dynamic(context , 20 , 2);
						nk_text(context , iter.val , NK_TEXT_ALIGN_LEFT);
						if(nk_button_label(context , "Remove")) iter = editor.removeMacroLevelIntroOSTSegment(iter);
						
					}
					
				}
				
				if(toBool(showOSTLoopSegments)) {
					
					cdNode<String> iter = editor.currentMacroLevel.OSTLoopSegmentsIter();
					for(int i = 0 ; i < editor.currentMacroLevel.OSTLoopSegmentsSize() ; i ++ , iter = iter.next) {
													
						nk_layout_row_dynamic(context , 20 , 2);
						nk_text(context , iter.val , NK_TEXT_ALIGN_LEFT);
						if(nk_button_label(context , "Remove")) iter = editor.removeMacroLevelLoopOSTSegment(iter);
						
					}
					
				}
				
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
