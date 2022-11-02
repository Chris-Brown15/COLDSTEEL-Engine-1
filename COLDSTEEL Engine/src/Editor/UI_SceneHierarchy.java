package Editor;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.toBool;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_DOWN;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_label;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.nio.ByteBuffer;
import java.util.function.Function;

import CS.UserInterface;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.Tuple3;
import Core.AbstractGameObjectLists;
import Core.Quads;
import Core.Scene;
import Core.Entities.Entities;
import Core.Statics.Statics;
import Core.TileSets.Tiles;
import Game.Items.Items;
import Game.Levels.Levels;

public class UI_SceneHierarchy extends UserInterface {

	private ByteBuffer 
		backgroundQuads 			= alloc0(ALLOCATOR),
		BackgroundTiles 			= alloc0(ALLOCATOR),
		backgroundStatics 			= alloc0(ALLOCATOR),
		entities 					= alloc0(ALLOCATOR),
		items 						= alloc0(ALLOCATOR),
		foregroundQuads 			= alloc0(ALLOCATOR),
		foregroundTiles 			= alloc0(ALLOCATOR),
		foregroundStatics 			= alloc0(ALLOCATOR),
		backupBackgroundTiles 		= alloc0(ALLOCATOR),
		backupBackgroundStatics 	= alloc0(ALLOCATOR),
		backupEntities 				= alloc0(ALLOCATOR),
		backupItems 				= alloc0(ALLOCATOR),
		backupForegroundTiles 		= alloc0(ALLOCATOR),
		backupForegroundStatics 	= alloc0(ALLOCATOR)
	;
	
	private void layoutListInfo(AbstractGameObjectLists<? extends Quads> list , Function<Quads , String> text) {
		
		list.forEach(x -> {

			nk_layout_row_begin(context , NK_STATIC , 20 , 2);
			nk_layout_row_push(context , 30);
			nk_text_wrap(context , "");
			nk_layout_row_push(context , 250);
		
			nk_text(context , text.apply(x) , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_end(context);			
			
		});
		
	}

	private void layoutListInfo(CSLinked<Tuple2<String , float[]>> list) {
		
		list.forEachVal(x -> {

			nk_layout_row_begin(context , NK_STATIC , 20 , 2);
			nk_layout_row_push(context , 30);
			nk_text_wrap(context , "");
			nk_layout_row_push(context , 250);
		
			nk_text(context , x.getFirst() + ", pos: " + x.getSecond()[0] + ", " + x.getSecond()[1], NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_end(context);			
			
		});
		
	}

	private void layoutTileListInfo(CSLinked<Tuple3<String , float[] , Integer>> list) {
		
		list.forEachVal(x -> {

			nk_layout_row_begin(context , NK_STATIC , 20 , 2);
			nk_layout_row_push(context , 30);
			nk_text_wrap(context , "");
			nk_layout_row_push(context , 250);
		
			nk_text(context , x.getFirst() + ", pos: " + x.getSecond()[0] + ", " + x.getSecond()[1], NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
			
			nk_layout_row_end(context);			
			
		});
		
	}

	
	public UI_SceneHierarchy(Editor editor , Scene scene , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);
		
		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 40 , 1);
			nk_text(context , "Current Scene" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
						
			int symbol = toBool(backgroundQuads) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			nk_selectable_symbol_label(context , symbol , "Background Quads, Order: " + scene.quads1().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backgroundQuads);			
			if(toBool(backgroundQuads))  layoutListInfo(editor.scene.quads1() , (q) -> "Quad " + q.getID());
			
			symbol = toBool(BackgroundTiles) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			nk_selectable_symbol_label(context , symbol , "Background Tiles, Order: " + scene.tiles1().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , BackgroundTiles);		
			if(toBool(BackgroundTiles))  layoutListInfo(scene.tiles1() , (T) -> ((Tiles)T).toStringAndDetails());

			symbol = toBool(backgroundStatics) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			nk_selectable_symbol_label(context , symbol , "Background Statics, Order: " + scene.statics1().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backgroundStatics);			
			if(toBool(backgroundStatics))  layoutListInfo(scene.statics1() , (S) -> ((Statics)S).toString());
				
			symbol = toBool(entities) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_label(context , symbol , "Entities, Order: " + scene.entities().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , entities));			
			if(toBool(entities)) layoutListInfo(scene.entities() , (E) -> ((Entities)E).name() + ", LID: " + ((Entities)E).LID() + ", ID: " + E.getID());

			symbol = toBool(items) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_label(context , symbol , "Items, Order: " + scene.items().renderOrder(), NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , items));			
			if(toBool(items)) layoutListInfo(scene.items() , (I) -> ((Items)I).name() + ", ID: " + I.getID());
				
			symbol = toBool(foregroundQuads) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_label(context , symbol , "Foreground Quads, Order: " + scene.quads2().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , foregroundQuads));			
			if(toBool(foregroundQuads)) layoutListInfo(scene.quads2() , (Q) -> "Quad " + Q.getID());
				
			symbol = toBool(foregroundTiles) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_selectable_symbol_label(context , symbol , "Foreground Tiles, Order: " + scene.tiles2().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , foregroundTiles));			
			if(toBool(foregroundTiles)) layoutListInfo(scene.tiles2() , (T) -> ((Tiles)T).toStringAndDetails());
				
			symbol = toBool(foregroundStatics) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;
			nk_layout_row_dynamic(context , 20 , 1);
			nk_selectable_symbol_label(context , symbol , "Foreground Statics, Order: " + scene.statics2().renderOrder() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , foregroundStatics);	
			if(toBool(backgroundStatics)) layoutListInfo(scene.statics2() , (S) -> ((Statics)S).toString());
			
			/*     		*/
			/*  backup  */
			/*     		*/
			
			nk_layout_row_dynamic(context , 40 , 1);
			nk_text(context , "Backup Scene" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			Levels backup = editor.backupLevel;
			
			if(backup == null) {
				
				nk_end(context);
				return;
				
			}
			
			symbol = toBool(backupBackgroundTiles) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1);						
			nk_selectable_symbol_label(context , symbol , "Background Tile Set" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupBackgroundTiles);
			if(toBool(backupBackgroundTiles)) layoutTileListInfo(backup.backgroundTiles());
				
			symbol = toBool(backupBackgroundStatics) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Background Statics" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupBackgroundStatics);
			if(toBool(backupBackgroundStatics)) layoutListInfo(backup.backgroundStatics());
			
			symbol = toBool(backupEntities) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Entities" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupEntities);
			if(toBool(backupEntities)) layoutListInfo(backup.entities());

			symbol = toBool(backupItems) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Items" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupItems);
			if(toBool(backupItems)) layoutListInfo(backup.items());

			symbol = toBool(backupItems) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Items" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupItems);
			if(toBool(backupItems)) layoutListInfo(backup.items());

			symbol = toBool(backupForegroundTiles) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Foreground Tile Set" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupForegroundTiles);
			if(toBool(backupForegroundTiles)) layoutTileListInfo(backup.foregroundTiles());
				
			symbol = toBool(backupForegroundStatics) ? NK_SYMBOL_TRIANGLE_DOWN:NK_SYMBOL_TRIANGLE_RIGHT;			
			nk_layout_row_dynamic(context , 20 , 1) ;
			nk_selectable_symbol_label(context , symbol , "Backup Foreground Statics" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE , backupForegroundStatics);
			if(toBool(backupForegroundStatics)) layoutListInfo(backup.foregroundStatics());

			
		});
		
	}
	
	void show() {
		
		show = true;
		
	}

	void hide() {
		
		show = false;
		
	}
	
}
