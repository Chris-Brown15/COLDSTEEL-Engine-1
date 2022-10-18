package Networking;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_SELECTABLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_edit_string;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.system.MemoryUtil.nmemCalloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;

import static Networking.Utils.NetworkingConstants.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;
import Core.NKUI;
import Networking.UserHostedServer.UserHostedServer;
import Networking.Utils.PacketCoder;

/**
 * 
 * Represents the Chat UI users can use to send messages to all other players.
 * <br><br>
 * All this class should do is display messages from other players and allow the user to send messages from their chat box.
 * Messages will arrive from other networking threads and messages the user wants to send need to be passed from this to the coresponding
 * {@code NetworkedInstance}.
 * 
 * @author Chris Brown
 *
 */
public class MultiplayerChatUI implements NKUI {

	private final NkPluginFilter textFilter = NkPluginFilter.create(Nuklear::nnk_filter_default);
	private final NetworkedInstance instance;
	private boolean showChatUI = false;
	
	private long chatInputMemory = nmemCalloc(1 , 1024);
	private MemoryStack stack = MemoryStack.ncreate(chatInputMemory, 1024);
	private ByteBuffer stringInputBuffer = stack.malloc(999);
	private IntBuffer stringInputLength = stack.mallocInt(1);
	private CSLinked<String> chatLog = new CSLinked<>();
	private final String playerName;
	
	public MultiplayerChatUI(String playerName , NetworkedInstance instance) {
		
		this.instance = instance;
		this.playerName = playerName;
		
	}
	
	public void appendChatMessage(String message) {
		
		chatLog.add(message);
		
	}
	
	public void layout() {
		
		if(!showChatUI) return;
		
		try (MemoryStack stack = allocator.push()){
			
			if(nk_begin(context , "Chat" , NkRect.malloc(stack).set(1515 , 675 , 400 , 400) , NK_WINDOW_TITLE|NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR)) {
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 2);
				nk_layout_row_push(context , 310);
				nk_edit_string(context , NK_EDIT_FIELD|NK_EDIT_SELECTABLE , stringInputBuffer , stringInputLength , 998 , textFilter);
				nk_layout_row_push(context , 70);
				if(nk_button_label(context , "Send")) {
					
					String input = org.lwjgl.system.MemoryUtil.memUTF8(stringInputBuffer.slice(0 , stringInputLength.get(0)));
					
					PacketCoder coder = new PacketCoder()
						.bflag(CHAT_MESSAGE)
						.bstring(playerName + " says: " + input);
					;

					chatLog.add("You: " + input);

					try {
						
						if(instance instanceof UserHostedServer) ((UserHostedServer) instance).broadCastReliable(coder.get() , 413890341 , 1500); 
						else instance.sendReliable(coder.get() , 413890341 , 1500);
						
					} catch (IOException e) {

						e.printStackTrace();
						
					} finally { 
						
						stringInputBuffer.clear();
						stringInputLength.put(0 , 0);
						
					}
					
				}
				
				nk_layout_row_end(context);
				
				nk_layout_row_dynamic(context , 350 , 1);
				if(nk_group_begin(context , "" , NK_WINDOW_BORDER)) {

					cdNode<String> iter = chatLog.get(0);
					for(int i = 0 ; i < chatLog.size() ; i ++ , iter = iter.next) {
						
						nk_layout_row_dynamic(context , 20 , 1);
						nk_text(context , iter.val , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);					
												
					}					
				
					nk_group_end(context);
					
				}
				
			}
			
			nk_end(context);
			
		}
		
	}
	
	public void toggle() {
	
		showChatUI = showChatUI ? false : true;
		
	}	
	
	public void shutDown() {
		
		nmemFree(chatInputMemory);
		textFilter.free();
		
	}
	
}
