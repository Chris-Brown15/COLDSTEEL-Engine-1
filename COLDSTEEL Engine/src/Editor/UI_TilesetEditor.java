package Editor;

import static CS.COLDSTEEL.assets;
import static CS.COLDSTEEL.data;
import static CSUtil.BigMixin.alloc1;
import static CSUtil.BigMixin.put;
import static CSUtil.BigMixin.toByte;
import static CSUtil.BigMixin.toNamePath;
import static CSUtil.BigMixin.toggle;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_TOP;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_image;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import CS.UserInterface;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import Core.GameFiles;
import Core.SpriteSets;
import Core.TemporalExecutor;
import Core.TileSets.TileSets;
import Core.TileSets.Tiles;

public class UI_TilesetEditor extends UserInterface {

	ByteBuffer renderTileSheet = alloc1(ALLOCATOR);
	private boolean showTilePlacer = false;
	NkImage backgroundTileSetSpriteSheet = NkImage.malloc(ALLOCATOR);
	NkImage foregroundTileSetSpriteSheet = NkImage.malloc(ALLOCATOR);
	CSLinked<NkImage> backgroundTileIcons = new CSLinked<>();
	CSLinked<NkImage> foregroundTileIcons = new CSLinked<>();
	
	public UI_TilesetEditor(Editor editor , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);

		layoutBody((frame) -> {
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_button_label(context , "New")) editor.schedule(Editor::newTileset);
			
			if(nk_button_label(context , "Load")) editor.schedule(Editor::loadTileset);
			
			if(nk_button_label(context , "Delete")) editor.schedule(Editor::deleteTileset);
			
			nk_layout_row_dynamic(context , 20 , 2);
			if(nk_checkbox_label(context , "Background" , toByte(frame , editor.background))) editor.schedule(e -> e.background = true);
			
			if(nk_checkbox_label(context , "Foreground" , toByte(frame , !editor.background))) editor.schedule(e -> e.background = false);
			
			TileSets currentTileSet = editor.background ? editor.scene.tiles1() : editor.scene.tiles2();
			
			if(currentTileSet.uninitialized()) {
				
				nk_end(context);
				return;
				
			}
			
			editor.setState(EditorState.EDITING_TILESET);
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Show Tile Placer" , toByte(ALLOCATOR , showTilePlacer))) showTilePlacer = toggle(showTilePlacer);
			
			nk_layout_row_dynamic(context , 30 , 2);
			nk_text(context , currentTileSet.name() , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED);
			
			if(nk_button_label(context , "Rename")) editor.schedule(Editor::renameTileset);
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Save")) editor.schedule(e -> currentTileSet.write());
			
			if(nk_button_label(context , "Clear")) editor.schedule(e -> {
				
				e.clearTileset();
				showTilePlacer = false;
				put(renderTileSheet , false);
				
			});
			
			if(currentTileSet.uninitialized() || currentTileSet.getTileSheet() == null) {
				
				nk_end(context);
				return;
				
			}
			
			editor.schedule(e -> e.selection.roundVertices());
			
			nk_layout_row_dynamic(context , 20 , 1);
			if(nk_checkbox_label(context , "Render Sprite Sheet" , renderTileSheet));
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Remove Color")) editor.schedule(Editor::removeColorFromTileset);
			
			if(nk_button_label(context , "ReTexture")) {}
			
			FloatBuffer 
				width = frame.callocFloat(1),
				height = frame.callocFloat(1)
			;
			
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Move Tile Sheet Horizontally" , -999 , width , 999 , 1.0f , 1.0f);
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Move Tile Sheet Vertically" , -999 , height , 999 , 1.0f , 1.0f);
			
			currentTileSet.getTileSheet().translate(width.get() , height.get());	
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_label(context , "Save Selection Area As Tile") && editor.selection != null) {
				
				editor.schedule(e -> {
					
					NkImage newImage = editor.background ? backgroundTileSetSpriteSheet : foregroundTileSetSpriteSheet;
					Tuple2<NkImage , NkRect> subRegionResult = e.saveSelectionAreaAsTile(newImage);

					if(editor.background) backgroundTileIcons.add(subRegionResult.getFirst());
					else foregroundTileIcons.add(subRegionResult.getFirst());
									
				});
				
			}
			
			nk_layout_row_dynamic(context , 600 , 1);
			if(nk_group_begin(context, "Tiles" , NK_WINDOW_TITLE|NK_WINDOW_BORDER)) {
				
				CSLinked<NkImage> tileIcons = editor.background ? backgroundTileIcons : foregroundTileIcons;
				cdNode<Tiles> iter = currentTileSet.sourcesIter();
				cdNode<NkImage> iterImage = tileIcons.get(0);
				
				Tiles tile;
				NkImage tileIcon;
				
				for(int i = 0 ; i < currentTileSet.numberSourceTiles() ; i ++ , iter = iter.next , iterImage = iterImage.next) {
					
					tile = iter.val;
					tileIcon = iterImage.val;
					
					if(i % 2 == 0) {
						
						int rowSize = 230;
						if(tile.hasCollider()) rowSize += 50;
						if(tile.isColliderTriangle()) rowSize += 100;
						
						nk_layout_row_dynamic(context , rowSize , 2);
						
					}
					
					nk_image(context , tileIcon);
					
					tile.removeMarked();
					
					if(nk_group_begin(context , "" , NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR)) {
						
						nk_layout_row_dynamic(context , 20 , 1);
						nk_text(context , tile.name() + " ID: " + tile.getID(), NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_TOP);
						
//						Tiles alias = tile;
//						nk_layout_row_dynamic(context , 30 , 2);
//						if(nk_button_label(context , "Name")) editor.schedule(e -> e.setTileName(alias));
						
						if(nk_button_label(context , "Remove")) { 
							
							cdNode<Tiles> iterAlias = iter;
							cdNode<NkImage> iterImageAlias = iterImage;							
							editor.schedule(e -> e.removeTile(iterAlias, iterImageAlias));
							
						}	
						
						nk_layout_row_dynamic(context , 30 , 1);
						if(nk_button_label(context , "Eraser 1")) {
							
//							alias = tile;
//							editor.schedule(e -> e.setTileAnimation(alias));
							
						}
						
						if(iter.val.getAnimation() != null) { 
							
							nk_layout_row_dynamic(context , 20 , 2);
							nk_text(context , "Animation" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
							nk_text(context , iter.val.getAnimation().name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
							
						}
													
						nk_layout_row_dynamic(context , 30 , 2);
						if(nk_button_label(context , "Copy")) {
							
							Tiles deepCopy = iter.val.copy();
							currentTileSet.addSourceTile(deepCopy);
							NkImage iterImageCopy = NkImage.malloc(ALLOCATOR).set(tileIcon);
							tileIcons.add(iterImageCopy);
							
						}
													
						if(nk_button_label(context , "Filter")) {
							
							Supplier<float[]> color = DialogUtils.newColorChooser("Filter a Color", 5, 270);
							Tiles query = iter.val;
							TemporalExecutor.onTrue(() -> color.get() != null , () -> query.filterColor(color.get()));
							
						}
						
						nk_layout_row_dynamic(context , 30 , 2);
						if(nk_button_label(context , "Reflect X-Wise")) iter.val.reflectXWise();
						
						if(nk_button_label(context , "Reflect Y-Wise")) iter.val.reflectYWise();
						
						nk_layout_row_dynamic(context , 20 , 1);
						
						if(nk_checkbox_label(context , "Has Collider" , toByte(frame , tile.hasCollider()))) tile.toggleCollider();
						
						if(tile.hasCollider()) {
															
							nk_layout_row_dynamic(context , 20 , 2);
							if(nk_checkbox_label(context , "Upper Right" , toByte(frame , tile.isColliderUpperRightTriangle()))) 
								tile.toggleUpperRightTriangle();
							
							if(nk_checkbox_label(context , "Upper Left" , toByte(frame, tile.isColliderUpperLeftTriangle()))) 
								tile.toggleUpperLeftTriangle();
							
							nk_layout_row_dynamic(context , 20 , 2);
							if(nk_checkbox_label(context , "Lower Right" , toByte(frame , tile.isColliderLowerRightTriangle()))) 
								tile.toggleLowerRightTriangle();
							
							if(nk_checkbox_label(context , "Lower Left" , toByte(frame , tile.isColliderLowerLeftTriangle()))) 
								tile.toggleLowerLeftTriangle();							
							
							nk_layout_row_dynamic(context , 20 , 1);
							if(nk_checkbox_label(context , "Platform" , toByte(frame , tile.isColliderPlatform()))) tile.togglePlatform();
							
							if(tile.isColliderTriangle()) {
								
								FloatBuffer triangleSliders = frame.callocFloat(4);
								
								nk_layout_row_dynamic(context , 20 , 1);
								nk_property_float(context , "Mod Width" , - 999 , triangleSliders.slice(0, 1) , 999 , 1 , 1);
								
								nk_layout_row_dynamic(context , 20 , 1);
								nk_property_float(context , "Mod Height" , - 999 , triangleSliders.slice(1, 1) , 999 , 1 , 1);
								
								nk_layout_row_dynamic(context , 20 , 1);
								nk_property_float(context , "Mod X Pos" , - 999 , triangleSliders.slice(2, 1) , 999 , 1 , 1);
								
								nk_layout_row_dynamic(context , 20 , 1);
								nk_property_float(context , "Mod Y Pos" , - 999 , triangleSliders.slice(3, 1) , 999 , 1 , 1);
								
								tile.modColliderWidth(triangleSliders.get());
								tile.modColliderHeight(triangleSliders.get());
								tile.modColliderOffset(triangleSliders.get() , triangleSliders.get());
								
							} 
							
						}
						
						nk_group_end(context);
						
					}
					
				}
				
				nk_group_end(context);
				
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
