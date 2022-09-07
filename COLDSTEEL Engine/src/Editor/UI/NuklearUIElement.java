package Editor.UI;

import static CSUtil.BigMixin.pointer;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZED;
import static org.lwjgl.nuklear.Nuklear.nnk_begin;
import static org.lwjgl.nuklear.Nuklear.nnk_end;
import static org.lwjgl.system.MemoryUtil.memUTF8;

import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;
import Core.Executor;
import Core.NKUI;

public class NuklearUIElement implements NKUI{

	private static final Thread UIThread = new Thread(new Runnable() {

		@Override public void run() {
			
			elements.forEachVal(NuklearUIElement::layout);
			
		}
		
	});  
	
	private static final CSLinked<NuklearUIElement> elements = new CSLinked<>();
	public static final long context = NKUI.context.address();	
	public static final int DEFAULT_OPTIONS = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_MINIMIZABLE|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR;
	public static final int CLOSED_OPTIONS = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_TITLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_MINIMIZED;
	
	public static final void layoutElements() {
		
		UIThread.run();
		
	}
	
	public static final void removeElementByName(String name) {
		
		cdNode<NuklearUIElement> iter = elements.get(0);
		for(int i = 0 ; i < elements.size() ; i ++ , iter = iter.next) if(name.equals(memUTF8(iter.val.namePtr))) {
		
			iter = elements.safeRemove(iter);
			return;
			
		}
		
	}
	
	public static final void addElement(String name , float x , float y , float width , float height , int defaultOptions , int closedOptions , Executor body) {
		
		NuklearUIElement element = new NuklearUIElement(name);
		element.rect = NkRect.malloc(allocator).set(5, 600 , 450, 980);
		element.defaultOptions = defaultOptions;
		element.closedOptions = closedOptions;
		element.layoutBody(body);
		
	}

	public static final void addElement(String name , float x , float y , float width , float height , Executor body) {
		
		NuklearUIElement element = new NuklearUIElement(name);
		element.rect = NkRect.malloc(allocator).set(5, 600 , 450, 980);
		element.defaultOptions = DEFAULT_OPTIONS;
		element.closedOptions = CLOSED_OPTIONS;
		element.layoutBody(body);
		
	}
	
	private final long namePtr;
	protected NkRect rect;
	protected boolean firstTimeOpening = true;
	protected boolean show = true;
	protected Executor layoutBody;
	protected int defaultOptions = DEFAULT_OPTIONS;
	protected int closedOptions = CLOSED_OPTIONS;
	
	protected NuklearUIElement(String name) {
		
		elements.add(this);
		namePtr = pointer(name , allocator);
		
	}
	
	protected boolean firstTimeOpening() {
		
		if(firstTimeOpening) {
			
			firstTimeOpening = false;
			return true;
		
		}
		
		return false;
		
	}
	
	protected void layoutBody(Executor code) {
		
		this.layoutBody = code;
		
	}
			
	/**
	 * Called for each element in the list of ui elements. This method handles the boilerplate of Nuklear so the outside world
	 * can simply provide the body of the UI element and its options. A Memory Stack frame is pushed and popped for each element so native 
	 * allocations don't need to be worried about by users of this class.
	 * 
	 */
	private final void layout() {
		
		try (MemoryStack stack = allocator.push()){
			
			int options = firstTimeOpening() ? closedOptions:defaultOptions;			
			if(show && nnk_begin(context , namePtr , rect.address() , options)) layoutBody.execute();			
			nnk_end(context);
			
		}
		
	}
	
}