package Editor;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toBool;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import CS.UserInterface;
import Core.TemporalExecutor;
import Game.Items.ItemComponents;
import Game.Items.Items;

public class UI_ItemEditor extends UserInterface {

	IntBuffer activeItemSelector = ALLOCATOR.ints(-1);
	private ByteBuffer 
		equippableCheck = alloc0(ALLOCATOR),
		usableCheck = alloc0(ALLOCATOR),
		materialsCheck = alloc0(ALLOCATOR),
		hitboxableCheck = alloc0(ALLOCATOR),
		consumableCheck = alloc0(ALLOCATOR),	
		flagsCheck = alloc0(ALLOCATOR),
		itemInfoCheck = alloc0(ALLOCATOR)
	;	
	
	public UI_ItemEditor(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);

		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) editor.schedule(Editor::newItem);
			
			if(nk_button_label(context , "Load")) editor.schedule(Editor::loadItem);
			
			if(nk_button_label(context , "Delete")) editor.schedule(Editor::deleteItems);
			
			if(editor.activeQuad == null || !(editor.activeQuad instanceof Items)) {
				
				nk_end(context);
				return;
				
			}
			
			editor.cursorState(CursorState.FROZEN);
			
			Items activeItem = (Items) editor.activeQuad;
			
			//set checkboxes of item components each frame becasue other items may be selected
			put(equippableCheck , activeItem.has(ItemComponents.EQUIPPABLE));
			put(usableCheck , activeItem.has(ItemComponents.USABLE));
			put(materialsCheck , activeItem.has(ItemComponents.MATERIALS));
			put(hitboxableCheck , activeItem.has(ItemComponents.HITBOXABLE));
			put(consumableCheck , activeItem.has(ItemComponents.CONSUMABLE));
			put(flagsCheck , activeItem.has(ItemComponents.FLAGS));
			
			nk_layout_row_dynamic(context , 20 , 1);
			nk_text(context , activeItem.name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Save")) editor.schedule(e -> activeItem.write());
			
			if(nk_button_label(context , "Save As")) {}
			
			if(nk_button_label(context , "Remove")) editor.schedule(Editor::removeActive);
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Texture")) editor.schedule(e -> {
				
				e.textureActive();
				TemporalExecutor.onTrue(() -> e.activeQuad.isTextured() , () -> e.activeQuad.fitQuadToTexture());
			
			});
			
			if(nk_button_label(context , "Remove Color")) editor.schedule(Editor::removeColor);
			
			if(nk_button_label(context , "Filter Color")) editor.schedule(Editor::applyFilter);
			
			editor.schedule(e -> {

				activeItem.roundVertices();
				e.selection.roundVertices();
				
			});
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "Reset Dims and UVs")) editor.schedule(Editor::resetActiveDims);
			
			if(nk_button_label(context , "Save Selection as Icon")) editor.schedule(Editor::saveSelectionAreaAsItemIcon);
						
			if(nk_button_label(context , "Iconify")) editor.schedule(e -> activeItem.iconify());
			
			nk_layout_row_dynamic(context , 30 , 2);			
			if(nk_button_label(context , "Max Stack Size")) editor.schedule(Editor::setItemMaxStackSize);
			
			if(nk_button_label(context , "Icon Animation")) editor.schedule(Editor::setItemIconAnimation);
			
			nk_layout_row_dynamic(context , 250 , 1);
			if(nk_group_begin(context , "Item Components" , NK_WINDOW_TITLE|NK_WINDOW_BORDER)) {

				nk_layout_row_begin(context , NK_STATIC , 30 , 2);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Equippable" , equippableCheck)) editor.schedule(e -> e.setItemEquippable());
				if(activeItem.has(ItemComponents.EQUIPPABLE)) {
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , " Slot (" + activeItem.componentData().equipSlot() + ")")) editor.schedule(Editor::setItemEquipSlot);
					
				}
				
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 3);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Usable" , usableCheck)) editor.schedule(e -> e.setItemUsable());
				if(activeItem.has(ItemComponents.USABLE)) {
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Script")) editor.schedule(Editor::setItemUseScript);
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Recompile Script")) editor.schedule(Editor::recompileItemUseScript) ;
					
				}			
				
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 1);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Materials" , materialsCheck)) editor.schedule(e -> e.setItemMaterial());
				
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 2);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Hitboxable" , hitboxableCheck)) editor.schedule(e -> e.setItemHitboxable());
				if(activeItem.has(ItemComponents.HITBOXABLE)) {
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Add Hit Box")) editor.schedule(Editor::addItemHitboxSet);
					
				}
				
				nk_layout_row_end(context);

				nk_layout_row_begin(context , NK_STATIC , 30 , 2);			
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Consumable" , consumableCheck)) editor.schedule(e -> e.setItemConsumable());
							
				nk_layout_row_push(context , 90);
				if(nk_button_label(context , "Chance To Consume")) editor.schedule(Editor::setItemChanceToConsume);
				
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 3);
				nk_layout_row_push(context , 200);
				if(nk_checkbox_label(context , "Flags" , flagsCheck)) editor.schedule(e -> e.setItemFlags());
				if(activeItem.has(ItemComponents.FLAGS)) {

					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Add Flag")) editor.schedule(Editor::addFlagToItem);
					
					nk_layout_row_push(context , 90);
					if(nk_button_label(context , "Remove Flag")) editor.schedule(Editor::removeFlagFromItem);
					
				}
				
				nk_layout_row_end(context);
				
				nk_group_end(context);
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_checkbox_label(context , "Show Info" , itemInfoCheck)) ;
			
			if(toBool(itemInfoCheck)) {
								
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Equippable" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "slot:" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + activeItem.componentData().equipSlot() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Equip Script:" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , activeItem.componentData().onEquipScript() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Unequip Script:" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , activeItem.componentData().onUnequipScript() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Usable" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Use Script" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , activeItem.componentData().useScript() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Hitboxable" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_button_label(context , "List Hit Boxes")) editor.schedule(Editor::printItemHitboxSets);				
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Consumable" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Chance to Consume" , NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + activeItem.componentData().chanceToConsume() , NK_TEXT_ALIGN_RIGHT | NK_TEXT_ALIGN_MIDDLE);
			
				if(activeItem.has(ItemComponents.FLAGS)) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Flags" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
					
					activeItem.componentData().getFlagsMap().forEach((hash , list) -> {
						
						list.forEachVal(flag -> {
							
							nk_layout_row_dynamic(context , 20 , 1);
							nk_text(context , flag , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
							
						}) ;
						
					});
					
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
