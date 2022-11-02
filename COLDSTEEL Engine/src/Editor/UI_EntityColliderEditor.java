package Editor;

import static CSUtil.BigMixin.changeColorTo;
import static CSUtil.BigMixin.getArrayHeight;
import static CSUtil.BigMixin.getArrayWidth;
import static CSUtil.BigMixin.getColliderFloatArray;
import static CSUtil.BigMixin.makeTranslucent;
import static CSUtil.BigMixin.modArrayHeight;
import static CSUtil.BigMixin.modArrayWidth;
import static CSUtil.BigMixin.moveTo;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toBool;
import static CSUtil.BigMixin.translateArray;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.nio.FloatBuffer;

import CS.UserInterface;
import Core.ECS;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;

public class UI_EntityColliderEditor extends UserInterface {

	private FloatBuffer scrollHoriz = ALLOCATOR.floats(0);
	private FloatBuffer scrollVert = ALLOCATOR.floats(0);
	
	private FloatBuffer widthScroll = ALLOCATOR.floats(0);
	private FloatBuffer heightScroll = ALLOCATOR.floats(0);
	
	public UI_EntityColliderEditor(Editor editor , UI_AAAManager manager , String title , float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {

			if(!toBool(manager.componentEditor.editColliderCheck)) return;
			
				if(!(editor.activeQuad instanceof Entities)) {
					
					nk_end(context);
					return;
					
				}
				
				Entities E = (Entities)editor.activeQuad;
				Object[] comps = E.components();
				E.roundVertices();
				
				//if active entity has animations, stop them
				if(E.has(ECS.ANIMATIONS))  ((EntityAnimations)comps[Entities.AOFF]).animate(false);
				float[] collisionBounds;
				put(manager.editorEditor.renderDebugCheck , true);
				
				if(comps[Entities.CDOFF] == null) {
					
					//represents an unchanging set of values to use for collision detection. 
					//this is needed to ensure collision detection doesnt break
					collisionBounds = getColliderFloatArray(); 
					collisionBounds = changeColorTo(collisionBounds , 0f , 0.75f , 0f);
					collisionBounds = makeTranslucent(collisionBounds , 0.25f);

					moveTo(E , collisionBounds);
					comps[Entities.CDOFF] = collisionBounds;
					
				} else collisionBounds = (float[])comps[Entities.CDOFF];
				
				collisionBounds = CSUtil.BigMixin.snapDataToPixels(collisionBounds);					
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context,  "Position Horizontal" , -5 , scrollHoriz , 5 , 1 , 1);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context,  "Position Vertical" , -5 , scrollVert , 5 , 1 , 1);
				
				collisionBounds = translateArray(collisionBounds , scrollHoriz.get(0) ,  scrollVert.get(0));
				put(scrollHoriz , 0);
				put(scrollVert , 0);
								
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Width Mod" , -5 , widthScroll , 5 , 1 , 1);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_float(context , "Height Mod" , -5 , heightScroll , 5 , 1 , 1);
				
				collisionBounds = modArrayWidth(collisionBounds , widthScroll.get(0));
				collisionBounds = modArrayHeight(collisionBounds , heightScroll.get(0));							
				
				put(widthScroll , 0);
				put(heightScroll , 0);
				
				if(getArrayWidth(collisionBounds) < 2) collisionBounds = modArrayWidth(collisionBounds , -getArrayWidth(collisionBounds));
				if(getArrayHeight(collisionBounds) < 2) collisionBounds = modArrayHeight(collisionBounds , -getArrayHeight(collisionBounds));
				
				float[] colliderMid = CSUtil.BigMixin.getArrayMidpoint(collisionBounds);
				float[] EMid = E.getMidpoint();
				nk_layout_row_dynamic(context , 20 , 4);
				nk_text(context , "X Offset: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + (EMid[0] - colliderMid[0]) , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_text(context , "Y Offset: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + (EMid[1] - colliderMid[1]) , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);

		});
		
	}
	
	void show() {
		
		show = true;
		
	}
	
	void hide() {
		
		show = false;
		
	}

}
