package Editor;

import static CSUtil.BigMixin.read;
import static CSUtil.BigMixin.returnText;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD;
import static org.lwjgl.nuklear.Nuklear.NK_EDIT_SELECTABLE;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZED;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_SCALABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_edit_string;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.nmemCalloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;
import org.python.util.PythonInterpreter;

import CS.Engine;

public class EditorConsole {

	ArrayList<String> printLines = new ArrayList<String>(25);
	
	private static final NkContext context = Engine.NuklearContext();
	private Editor editor;	
	private String consoleName = "Console";
	boolean firstTimeOpeningConsole = true;
	private boolean showConsole = true;
	int consoleTextFieldOptions = NK_EDIT_FIELD|NK_EDIT_SELECTABLE;
	private ByteBuffer consoleIn = BufferUtils.createByteBuffer(255);
	private IntBuffer consoleInLength = Engine.UIAllocator().callocInt(1);
	final NkRect consoleRect;
	
	public EditorConsole(Editor editor){
		
		this.editor = editor;
		consoleIn.mark();
		for(int i = 0 ; i < printLines.size() ; i ++) say("");
		consoleRect = NkRect.malloc(Engine.UIAllocator()).x(1535).y(5).w(380).h(1070);
		
	}

	public void say(Object say){

		
		if(say.getClass() == String.class){

			String said = (String)say;
			if(said.contains("\n")) {
			
				String[] saidSplit = said.split("\n"); 
				for(String x : saidSplit) say(x);
				
			}
			
			else printLines.add(say.toString());

		} else {
			
			printLines.add(say + "");
			
			
		}

		consoleIn = BufferUtils.createByteBuffer(255);

	}
	
	public void say(Object... say) {		
	
		for(Object x : say) say(x);			
		
	}
	
	public void put(int index , Object say) {
		
		 printLines.set(index, say.toString());
		
	}

	private long inputAddress = nmemCalloc(1 , 255);
	private long inputLengthPtr = nmemCalloc(1 , 4);
		
	void layout(){

		if(showConsole) {

			int consoleOptions =  firstTimeOpeningConsole ?
					NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_SCALABLE | NK_WINDOW_MINIMIZED:
					NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_SCALABLE;

			try(MemoryStack stack = stackPush()){
								
				if(nk_begin(context, consoleName , consoleRect , consoleOptions)){
					
					firstTimeOpeningConsole = false;
					
					nk_layout_row_begin(context , NK_STATIC ,  30 , 2);
					nk_layout_row_push(context , 15);
					nk_text(context ,  ">" , NK_TEXT_ALIGN_LEFT);
					nk_layout_row_push(context , consoleRect.w()-50);
					nk_edit_string(context,  consoleTextFieldOptions , consoleIn , consoleInLength , 99 , null);
//					nnk_edit_string(context , consoleTextFieldOptions , consoleIn , consoleInLength , 255 , null);
//					nnk_edit_string(context.address() , consoleTextFieldOptions , inputAddress , inputLengthPtr , 255 , NULL);
					nk_layout_row_end(context);
					
					for(String s : printLines){
						
						nk_layout_row_dynamic(context , 20 , 1);
						nk_text_wrap(context , s);
						
					}
					
				}
				
			}

			nk_end(context);

		}

	}

	private void execPython(String pythonCode) {
		
		try(PythonInterpreter p = new PythonInterpreter()){
			
			p.exec(pythonCode);
			
		}
		
	}
	
	void command(){

		String input = read(consoleIn);
		say(input);

		if(input.contains(",")) for(String x : input.split(",")) command(x);
		else if (input.equals("end")) editor.overrideShutDown();
		else if (input.contains("-")) execPython(input.substring(input.indexOf("-")));
		else if (input.equals("gc")) System.gc();
		else if (input.equals("newConsole")) printLines = new ArrayList<String>();
		else if (input.equals("fps")) say(Engine.framesLastSecond());
				
		consoleIn = returnText(consoleIn);

	}
	
	void command(String input){

		if(input.equals("end")) editor.overrideShutDown();
		else if (input.equals("gc")) System.gc();
		else if (input.equals("newConsole")) printLines = new ArrayList<String>();		
		else if (input.equals("fps")) say(Engine.framesLastSecond());
				
	}

	void toggleShowConsole(){

		showConsole = showConsole ? false:true;

	}
	
	
	void shutDown() {
		
		nmemFree(inputAddress);
		nmemFree(inputLengthPtr);
		
	}
	
	boolean showConsole() {
		
		return showConsole;
		
	}

}
