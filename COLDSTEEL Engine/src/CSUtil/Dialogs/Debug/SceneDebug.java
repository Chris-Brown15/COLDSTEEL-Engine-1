package CSUtil.Dialogs.Debug;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.util.Arrays;

import static CSUtil.BigMixin.toByte;

import CS.Engine;
import CS.RuntimeState;
import CS.UserInterface;
import Core.Quads;
import Core.Scene;

public class SceneDebug extends UserInterface {

	private static final int options = NK_WINDOW_BORDER|NK_WINDOW_TITLE|NK_WINDOW_MOVABLE;
		
	private Scene scene;
	
	boolean quad1 = false;
	boolean tile1 = false;
	boolean static1 = false;
	boolean entity = false;
	boolean items = false;
	boolean quad2 = false;
	boolean tile2 = false;
	boolean static2 = false;
	boolean quads = false;
	boolean array = false;
	
	public SceneDebug(String name , Scene display , int x , int y) {
	
		super(name , x , y , 350 , 600 , options, options);
		
		scene = display;

		layoutBody(stackFrame -> {
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , Engine.STATE == RuntimeState.EDITOR ? "Editor Scene" : "Game Scene" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Background Quads" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(quad1)))) {
				
				quad1 = quad1 ? false:true;
				
			}
			
			if(quad1) scene.quads1().forEach(this::displayQuad);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Background Tiles" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(tile1)))) {
				
				tile1 = tile1 ? false:true;
				
			}
			
			if(tile1) scene.tiles1().forEach(this::displayQuad);

			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Background Statics" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(static1)))) {
				
				static1 = static1 ? false:true;
				
			}
			
			if(static1) scene.statics1().forEach(this::displayQuad);

			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Entities" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(entity)))) {
				
				entity = entity ? false:true;
				
			}
			
			if(entity) scene.entities().forEach(this::displayQuad);

			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Items" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(items)))) {
				
				items = items ? false:true;
				
			}
			
			if(items) scene.items().forEach(this::displayQuad);

			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Foreground Quads" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(quad2)))) {
				
				quad2 = quad2 ? false:true;
				
			}
			
			if(quad2) scene.quads2().forEach(this::displayQuad);

			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Foreground Tiles" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(tile2)))) {
				
				tile2 = tile2 ? false:true;
				
			}
			
			if(tile2) scene.tiles2().forEach(this::displayQuad);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Foreground Statics" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(static2)))) {
				
				static2 = static2 ? false:true;
				
			}
			
			if(static2) scene.statics2().forEach(this::displayQuad);

			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Final Objects" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(quads)))) {
				
				quads = quads ? false:true;
				
			}
			
			if(quads) scene.finalObjects().forEach(this::displayQuad);

			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_text(context , "Final Objects" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , stackFrame.bytes(toByte(array)))) {
				
				array = array ? false:true;
				
			}
			
			if(array) scene.finalArrays().forEachVal(this::displayArray);
			
		});
		
	}
	
	public void scene(Scene scene) {
		
		this.scene = scene;
		
	}
	
	private <Q extends Quads> void displayQuad(Q q) {
		
		nk_layout_row_begin(context , NK_STATIC ,  20 , 2);
		nk_layout_row_push(context, 40);
		nk_text_wrap(context , "");
		nk_layout_row_push(context , 290);
		nk_text(context , q.toString() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
		nk_layout_row_end(context);
		
	}

	private void displayArray(float[] array) {

		nk_layout_row_begin(context , NK_STATIC ,  20 , 2);
		nk_layout_row_push(context, 40);
		nk_text_wrap(context , "");
		nk_layout_row_push(context , 290);
		nk_text(context , Arrays.toString(array) , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
		nk_layout_row_end(context);
		
	}
	
	public void toggle() {
		
		show = show ? false:true;
		
	}
	
}