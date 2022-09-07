package CSUtil.Dialogs;

import static CS.COLDSTEEL.assets;
import static CS.COLDSTEEL.data;
import static CS.COLDSTEEL.deleted;
import static CS.COLDSTEEL.mods;
import static org.lwjgl.system.MemoryUtil.nmemFree;

import java.io.File;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;
import Core.NKUI;

public abstract sealed class DialogUtils implements NKUI permits ColorChooser , FileExplorer , InputBox {

	private static final CSLinked<DialogUtils> elements = new CSLinked<>();
	
	public static final NkPluginFilter DEFAULT_FILTER = NkPluginFilter.create(Nuklear::nnk_filter_default);
	public static final NkPluginFilter NUMBER_FILTER = NkPluginFilter.create(Nuklear::nnk_filter_float);
	
	protected static final File DATA_FOLDER = new File(data);
	protected static final File ASSETS_FOLDER = new File(assets);
	protected static final File DELETED_FOLDER = new File(deleted);
	protected static final File MODS_FOLDER = new File(mods);
	
	public static final Supplier<String> newInputBox(String title , int x , int y) {
		
		InputBox newInputBox = new InputBox(title , x , y);
		return () -> newInputBox.result;
		
	}

	public static final Supplier<String> newInputBox(String title , int x , int y , NkPluginFilter filter) {
		
		InputBox newInputBox = new InputBox(title , x , y , filter);
		return () -> newInputBox.result; 
		
	}
	
	public static final Supplier<String> newFileExplorer(String title , int x , int y , boolean allowMultiple , boolean allowFolders){
	
		FileExplorer newFileSelect = new FileExplorer(title , x , y , allowMultiple , allowFolders , null);
		return () -> newFileSelect.result;
		
	}

	public static final Supplier<String> newFileExplorer(String title , int x , int y){
	
		FileExplorer newFileSelect = new FileExplorer(title , x , y , false , false , null);
		return () -> newFileSelect.result;
		
	}

	/**
	 * Adds a new File Explorer Dialog to the scene.
	 * 
	 * @param title — title of the dialog
	 * @param x — starting x coordinate of the dialog in screen coordinates
	 * @param y — starting y coordinate of the dialog in screen coordinates
	 * @param allowMultiple — if true, multiple files and/or folders can be selected, with the "|" separating them
	 * @param allowFolders — if true, folders can be returned as results
	 * @param startingPath — a filepath that the dialog will be opened to initially
	 * @return {@code java.util.function.Supplier} which will return a nonnull result once the dialog has returned.
	 */
	public static final Supplier<String> newFileExplorer(String title , int x , int y , boolean allowMultiple , boolean allowFolders , String startingPath){
	
		FileExplorer newFileSelect = new FileExplorer(title , x , y , allowMultiple , allowFolders , startingPath);
		return () -> newFileSelect.result;
		
	}

	public static final Supplier<String> newFileExplorer(String title , int x , int y , String startingPath){
	
		FileExplorer newFileSelect = new FileExplorer(title , x , y , false , false , startingPath);
		return () -> newFileSelect.result;
		
	}
		
	public static final Supplier<float[]> newColorChooser(String title , int x , int y){
		
		ColorChooser chooser = new ColorChooser(title , x , y);
		return () -> chooser.colors;
		
	}
	
	public static final void layoutDialogs() {
		
		cdNode<DialogUtils> iter = elements.get(0);
		for(int i = 0 ; i < elements.size() ; i ++) {
			
			if(iter.val.finished) {
				
				iter.val.shutDown();
				iter = elements.safeRemove(iter);
				
			} else {
				
				iter.val.layout();
				iter = iter.next;
				
			}
			
		}
		
	}
	
	public static final void acceptLast() {
		
		if(elements.size() == 0) return;
		cdNode<DialogUtils> last = elements.get(elements.size() - 1);
		last.val.accept();
		last.val.shutDown();
		elements.safeRemove(last);
		
	}
	
	public static final void shutDownDialogs() {
		
		elements.forEachVal(x -> x.shutDown());
		DEFAULT_FILTER.free();
		NUMBER_FILTER.free();
		
	}

	protected NkRect rect;
	protected String title;
	protected long UIMemory;
	protected MemoryStack allocator;
	protected boolean finished = false;

	protected DialogUtils() {
		
		elements.add(this);
		
	}
	
	protected final void shutDown() {
		
		nmemFree(UIMemory);
		
	}	
	
	protected abstract void layout();
	protected abstract void accept();
	
}
