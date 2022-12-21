package CSUtil.Dialogs.Debug;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_SCALABLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import static Networking.Utils.NetworkingConstants.*;

import java.util.concurrent.ConcurrentHashMap;

import CS.Controls;
import CS.UserInterface;
import CSUtil.BigMixin;
import CSUtil.RefInt;
import CSUtil.DataStructures.LinkedRingBuffer;
import Networking.NetworkedEntities;
import Networking.UserHostedServer.UserConnection;

public class NetworkedEntityPreviousFrameViewer extends UserInterface {

	private static int options = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_SCALABLE|NK_WINDOW_MINIMIZABLE;
		
	boolean[] displayThese = new boolean[0];
	
	public NetworkedEntityPreviousFrameViewer(ConcurrentHashMap<Integer , UserConnection> connections , float x , float y) {
		
		super("Previous Input Viewer" , x, y, 350 , 600 , options, options);
					
		layoutBody((frame) -> {
					
			resetDisplayThese(connections);
			
			RefInt i = new RefInt(0);
			connections.forEach((hashCode , connection) -> {

				nk_layout_row_dynamic(context , 30 , 1);
				
				if(!connection.initialized()) return;
				
				//selectable text with symbol indicating if you want to see this entity's previous inputs
				if(nk_button_label(context , connection.entity.networked().name())) displayThese[i.get()] = BigMixin.toggle(displayThese[i.get()]);
				
				if(!displayThese[i.get()]) return;
				
				LinkedRingBuffer<byte[]> previous = connection.entity.previousInputs();
								
				previous.forEachIndexed((j , array) -> {
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Update Frame: " + j , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);

					//convert each byte to its ID and then get the name, then display whether it was pressed:					
					for(int k = 1 ; k < array.length ; k++) {

						byte control = array[k];
						
						nk_layout_row_dynamic(context , 20 , 2);
						nk_text(context , Controls.getNameByID((byte) (control & KEYCODE_MASK)) , NK_TEXT_ALIGN_LEFT);
						nk_text(context , "Pressed: " + ((control & CONTROL_PRESSED_MASK) != 0) , NK_TEXT_ALIGN_LEFT);
						
					}
					
				});
				
				i.add();
				
			});
			
		});
				
	}

	public NetworkedEntityPreviousFrameViewer(NetworkedEntities networkedEntity , float x , float y) {
		
		super("Previous Input Viewer" , x, y, 300 , 500 , options, options);
					
		layoutBody((frame) -> {

			LinkedRingBuffer<byte[]> previous = networkedEntity.previousInputs();
			
			previous.forEachIndexed((i , array) -> {

				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Update Frame: " + i , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE);
				
				//convert each byte to its ID and then get the name, then display whether it was pressed:					
				for(int k = 1 ; k < array.length ; k++) {

					byte control = array[k];
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , Controls.getNameByID((byte) (control & KEYCODE_MASK)) , NK_TEXT_ALIGN_LEFT);
					nk_text(context , "Pressed: " + ((control & CONTROL_PRESSED_MASK) != 0) , NK_TEXT_ALIGN_LEFT);
					
				}
				
			});
						
		});
				
	}
	
	public void toggle() {
		
		show = show ? false : true;
		
	}
	
	private void resetDisplayThese(ConcurrentHashMap<Integer , UserConnection> connections) {
		
		boolean[] newArray = new boolean[connections.size()];
		System.arraycopy(displayThese, 0, newArray, 0, displayThese.length);
		displayThese = newArray;
		
	}
	
}
