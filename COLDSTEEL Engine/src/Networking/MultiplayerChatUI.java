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
					
					String prependedText = playerName + " says: ";									//prefix to messages sender sends
					byte[] prependedTextBytes = prependedText.getBytes();							//bytes

					byte[] data = new byte[prependedText.length() + stringInputLength.get(0) + 1];	//array of bytes to be sent 
					data[0] = NetworkedInstance.CHAT_MESSAGE;										//flag this message a chat message
					
					int i = 1; 																		//start i at 1 because 0 is taken	
					for(; i <= prependedText.length() ; i ++) data[i] = prependedTextBytes[i - 1];	//fill out data with prepended bytes
					for(int j = 0; i < data.length ; i ++ , j++) data[i] = stringInputBuffer.get(j);//copy the actual text from the input box
																									//create string for the sender to see in chat
					String myMessage = "You: " + new String(data , i - stringInputLength.get(0) , stringInputLength.get(0));
					chatLog.add(myMessage);

					try {
						
						if(instance instanceof UserHostedSession) ((UserHostedSession) instance).broadcastReliable(data , chatLog.size() * 10 , 1000); 
						else instance.sendReliable(data , stringInputLength.get(0) + 1 , 1000);
						
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
