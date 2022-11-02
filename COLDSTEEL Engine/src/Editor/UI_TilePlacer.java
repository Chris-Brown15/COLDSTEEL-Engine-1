package Editor;

import static org.lwjgl.nuklear.Nuklear.nk_button_image;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import org.lwjgl.nuklear.NkImage;

import CS.UserInterface;
import CSUtil.DataStructures.cdNode;
import Core.TileSets.TileSets;
import Core.TileSets.Tiles;

public class UI_TilePlacer extends UserInterface {

	public UI_TilePlacer(Editor editor , UI_AAAManager manager , String title, float x, float y, float w, float h, int normalOptions, int unopenedOptions) {
		
		super(title, x, y, w, h, normalOptions, unopenedOptions);

		layoutBody((frame) -> {
			
			TileSets currentTileSet = editor.background ? editor.scene.tiles1() : editor.scene.tiles2();
			
			if(editor.activeQuad instanceof Tiles) {
				
				Tiles activeTile = (Tiles)editor.activeQuad;
				editor.setState(EditorState.GENERIC);
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Remove " + ((Tiles)editor.activeQuad).toStringAndDetails())) {
					
					//remove from corresponding source tile's list
					activeTile.markForRemoval();
					currentTileSet.removeInstance(activeTile);
					editor.activeQuad = null;
					nk_end(context);
					return;
					
				}
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Move to Front")) currentTileSet.moveInstanceToFront(activeTile.getID());
				if(nk_button_label(context , "Move Forward")) currentTileSet.moveInstanceForward(activeTile.getID());
				
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_button_label(context , "Move to Back")) currentTileSet.moveInstanceToBack(activeTile.getID());					
				if(nk_button_label(context , "Move Backward")) currentTileSet.moveInstanceBackward(activeTile.getID());
								
			}
			
			cdNode<Tiles> iter = currentTileSet.sourcesIter();
			cdNode<NkImage> imageIter = editor.background ?  manager.tilesetEditor.backgroundTileIcons.get(0) : manager.tilesetEditor.foregroundTileIcons.get(0);
			NkImage tileIcon;
			Tiles tile;
			for(int i = 0 ; i < currentTileSet.numberSourceTiles() ; i ++ , iter = iter.next , imageIter = imageIter.next) {
				
				tile = iter.val;
				
				if(i % 2 == 0) nk_layout_row_dynamic(context , 150 , 2);
				tileIcon = imageIter.val;

				if(nk_button_image(context , tileIcon)) {
					
					editor.activeQuad = editor.copyTile(tile);
					editor.cursorState(CursorState.DRAGGING);
					
				}
				
			}

			
		});
			
	}

}
