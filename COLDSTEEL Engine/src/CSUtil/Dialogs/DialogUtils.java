package CSUtil.Dialogs;

import java.util.function.Supplier;

import org.lwjgl.nuklear.NkPluginFilter;
import CSUtil.DataStructures.CSLinked;

public abstract class DialogUtils {

	private static final CSLinked<Acceptable> elements = new CSLinked<>();
	
	public static final Supplier<String> newInputBox(String title , int x , int y) {
		
		InputBox newInputBox = new InputBox(title , x , y);
		elements.add(newInputBox);
		return () -> newInputBox.result;
		
	}

	public static final Supplier<String> newInputBox(String title , int x , int y , NkPluginFilter filter) {
		
		InputBox newInputBox = new InputBox(title , x , y , filter);
		elements.add(newInputBox);
		return () -> newInputBox.result; 
		
	}
	
	public static final Supplier<String> newFileExplorer(String title , int x , int y , boolean allowMultiple , boolean allowFolders){
	
		FileExplorer newFileSelect = new FileExplorer(title , x , y , allowMultiple , allowFolders , null);
		elements.add(newFileSelect);
		return () -> newFileSelect.result;
		
	}

	public static final Supplier<String> newFileExplorer(String title , int x , int y){
	
		FileExplorer newFileSelect = new FileExplorer(title , x , y , false , false , null);
		elements.add(newFileSelect);
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
		elements.add(newFileSelect);
		return () -> newFileSelect.result;
		
	}

	public static final Supplier<String> newFileExplorer(String title , int x , int y , String startingPath){
	
		FileExplorer newFileSelect = new FileExplorer(title , x , y , false , false , startingPath);
		elements.add(newFileSelect);
		return () -> newFileSelect.result;
		
	}
		
	public static final Supplier<float[]> newColorChooser(String title , int x , int y){
		
		ColorChooser chooser = new ColorChooser(title , x , y);
		elements.add(chooser);
		return () -> chooser.colors;
		
	}
	
}
