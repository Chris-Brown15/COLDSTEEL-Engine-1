package CSUtil.Dialogs;

import static CSUtil.BigMixin.alloc0;
import static CSUtil.BigMixin.toBool;
import static CSUtil.BigMixin.put;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_DOWN;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MINIMIZABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_text;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;
import static org.lwjgl.system.MemoryUtil.nmemCalloc;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.function.Predicate;

import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import CSUtil.DataStructures.CSStack;
import CSUtil.DataStructures.CSTree;
import CSUtil.DataStructures.cdNode;
import CSUtil.DataStructures.tNode;

/**
 * Representation of a UI Element allowing users to select a file or folder (or multiple of these) from the game archive.
 * <br><br> 
 * There are four archives in root from which a user may want to select, {@code data}, {@code assets}, {@code deleted}, and {@code mods}.
 * Within each of these, there could be any number of folders and within those there may be files, folders, or both.
 * Archives are modelled by a tree structure.
 * <br>
 * <br>
 * Each file is a node. A node will have attached nodes if its file is a directory. The ByteBuffer value of a node represents the state of whether
 * the file referenced in the node is selected or not.   
 *  
 * @author Chris Brown
 *
 */
public final class FileExplorer extends DialogUtils{

	private ByteBuffer[] rootChecks = new ByteBuffer[4];
	 
	private CSTree<ByteBuffer , File> dataTree = new CSTree<>();
	private CSTree<ByteBuffer , File> assetsTree = new CSTree<>();
	private CSTree<ByteBuffer , File> deletedTree = new CSTree<>();
	private CSTree<ByteBuffer , File> modsTree = new CSTree<>();
	
	String result;
	
	boolean allowMultiple = true;
	boolean allowFolders = true;
	
	FileExplorer(String title , int x , int y , boolean allowMultiple , boolean allowFolders , String startingPath){
	
		UIMemory = nmemCalloc(1 , 1024);
		allocator = MemoryStack.ncreate(UIMemory, 1024);
		this.title = title;
		rect = NkRect.malloc(allocator).set(x , y , 500 , 650);
		
		this.allowMultiple = allowMultiple;
		this.allowFolders = allowFolders;
				
		rootChecks[0] = alloc0(allocator);
		rootChecks[1] = alloc0(allocator);
		rootChecks[2] = alloc0(allocator);
		rootChecks[3] = alloc0(allocator);
		
		//add roots		
		dataTree.add(rootChecks[0], DATA_FOLDER);
		assetsTree.add(rootChecks[1], ASSETS_FOLDER);
		deletedTree.add(rootChecks[2], DELETED_FOLDER);
		modsTree.add(rootChecks[3], MODS_FOLDER);
		
		//populate roots				
		populateNode(dataTree.get(DATA_FOLDER) , DATA_FOLDER);
		populateNode(assetsTree.get(ASSETS_FOLDER) , ASSETS_FOLDER);
		populateNode(deletedTree.get(DELETED_FOLDER) , DELETED_FOLDER);
		populateNode(modsTree.get(MODS_FOLDER) , MODS_FOLDER);
		
		if(startingPath != null) {

			try {
				
				File fileAtPath = new File(startingPath);
				CSStack<File> roots = new CSStack<File>();
				File rootsOfFileAtPath = fileAtPath.getParentFile();
				File programArchive = new File(CS.COLDSTEEL.root);
				
				roots.push(rootsOfFileAtPath);				
				while(!(rootsOfFileAtPath = rootsOfFileAtPath.getParentFile()).equals(programArchive))roots.push(rootsOfFileAtPath);
				
				tNode<ByteBuffer , File> node;
				
				if((node = dataTree.get(roots.peek())) != null) ;
				else if((node = assetsTree.get(roots.peek())) != null) ;
				else if((node = deletedTree.get(roots.peek())) != null) ;
				else if((node = modsTree.get(roots.peek())) != null) ;
				
				findStartingPath(roots , node , fileAtPath);
				
			} catch(Exception e) {
				
				System.err.println("Invalid File Path given: " + startingPath);
				
			}
			
		}
		
	}
		
	private void findStartingPath(CSStack<File> roots ,  tNode<ByteBuffer , File> node , File fileAtPath) {
		
		roots.pop();
		put(node.val , true);
		tNode<ByteBuffer , File> testNode;
		File newestRoot;
		while(!roots.empty() && (testNode = node.get(newestRoot = roots.pop())) != null) {
			
			node = testNode;
			put(node.val , true);
			populateNode(node , newestRoot);
			
		}
		
		node = node.get(fileAtPath);
		put(node.val , true);
		populateNode(node , fileAtPath);
		
	}
	
	private void populateNode(tNode<ByteBuffer , File> node , File folder) {
		
		File[] archive = folder.listFiles();		
		for(File f : archive) node.add(alloc0(allocator) , f);
		
	}
	
	@Override protected void layout() {

		if(nk_begin(context , title , rect , NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_TITLE|NK_WINDOW_MINIMIZABLE)) {
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_button_label(context , "Accept")) accept();
			
			if(nk_button_label(context , "Cancel")) finished = true;
			
			//layout the first nodes of the trees
			layoutNode(dataTree.get(DATA_FOLDER) , 0);
			layoutNode(assetsTree.get(ASSETS_FOLDER) , 0);
			layoutNode(deletedTree.get(DELETED_FOLDER) , 0);
			layoutNode(modsTree.get(MODS_FOLDER) , 0);

		}
		
		nk_end(context);
		
	}

	private void layoutNode(tNode<ByteBuffer , File> node , int recursionDepth) {
		
		nk_layout_row_begin(context , NK_STATIC , 20 , 2);
		nk_layout_row_push(context , 30 * recursionDepth);
		nk_text_wrap(context , "");//left hand side padding
		
		if(node.ID.isDirectory()) {
			
			nk_layout_row_push(context , 470 - (2 * (30 * recursionDepth)));			
			int symbol = toBool(node.val) ? NK_SYMBOL_TRIANGLE_DOWN : NK_SYMBOL_TRIANGLE_RIGHT;
			if(nk_selectable_symbol_text(context , symbol , node.ID.getName() , NK_TEXT_ALIGN_LEFT , node.val)) 
				if(node.attached.size() == 0) populateNode(node , node.ID);
			
		} else {

			nk_layout_row_push(context , 470 - (30 * recursionDepth));			
			if(nk_selectable_text(context , node.ID.getName() , NK_TEXT_ALIGN_LEFT , node.val)) if(!allowMultiple) deselectAllBut(node);
						
		}
		
		nk_layout_row_end(context);
		
		if(toBool(node.val)) {
			
			cdNode<tNode<ByteBuffer , File>> iter = node.attached.get(0);
			for(int i = 0 ; i < node.attached.size() ; i ++ , iter = iter.next) {
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 2);
				nk_layout_row_push(context , 30 * (recursionDepth + 1));
				nk_text_wrap(context , "");
				
				if(iter.val.ID.isDirectory()) {
					
					nk_layout_row_push(context , 470 - (2 * (30 * (recursionDepth + 1))));	
					int symbol = toBool(iter.val.val) ? NK_SYMBOL_TRIANGLE_DOWN : NK_SYMBOL_TRIANGLE_RIGHT;
					if(nk_selectable_symbol_text(context , symbol , iter.val.ID.getName() , NK_TEXT_ALIGN_LEFT , iter.val.val))
						if(iter.val.attached.size() == 0)populateNode(iter.val , iter.val.ID);
					
					//layout this nodes archive if this node is selected
					nk_layout_row_end(context);
					if(toBool(iter.val.val)) iter.val.attached.forEachVal(gnode -> layoutNode(gnode , recursionDepth + 2));
					
				} else {

					nk_layout_row_push(context , 470 - (30 * (recursionDepth + 1)));					
					if(nk_selectable_text(context , iter.val.ID.getName() , NK_TEXT_ALIGN_LEFT , iter.val.val)) 
						if(!allowMultiple) deselectAllBut(iter.val);
					
				}
				
				nk_layout_row_end(context);
				
			}			
			
		}		
			
	}
	
	@Override protected void accept() {

		Predicate<tNode<ByteBuffer , File>> test = (tnode) -> 
			toBool(tnode.val) && (!tnode.ID.isDirectory() || tnode.ID.isDirectory() && allowFolders);
		
		//build a string representation of the nodes of the file explorer by passing through the tree and adding the file names of those
		//whose bytebuffer state is true.
		
		dataTree.forOnly(tnode -> test.test(tnode) , (tnode) -> result += tnode.ID.getAbsolutePath() + "|");
		assetsTree.forOnly(tnode -> test.test(tnode) , (tnode) -> result += tnode.ID.getAbsolutePath() + "|");
		deletedTree.forOnly(tnode -> test.test(tnode) , (tnode) -> result += tnode.ID.getAbsolutePath() + "|");
		modsTree.forOnly(tnode -> test.test(tnode) , (tnode) -> result += tnode.ID.getAbsolutePath() + "|");
		
		//to avoid returning the string default of "null" when not assigned. We keep the result unassigned to allow to test if
		//the file dialog has returned yet by checking if result is null
		
		if(result != null) {
			
			result = result.substring(4 , result.length() -1);
			if(allowFolders && !allowMultiple) {
				
				String[] split = result.split("\\|"); 
				result = split[split.length - 1];
				
			}
			
		} else result = "";
		
		finished = true;
		
	}
	
	private void deselectAllBut(tNode<ByteBuffer , File> node) {

		dataTree.forOnly(tnode -> !tnode.ID.equals(node.ID) && tnode.attached.size() == 0 , tnode -> put(tnode.val , false));
		assetsTree.forOnly(tnode -> !tnode.ID.equals(node.ID) && tnode.attached.size() == 0 , tnode -> put(tnode.val , false));
		deletedTree.forOnly(tnode -> !tnode.ID.equals(node.ID) && tnode.attached.size() == 0 , tnode -> put(tnode.val , false));
		modsTree.forOnly(tnode -> !tnode.ID.equals(node.ID) && tnode.attached.size() == 0 , tnode -> put(tnode.val , false));
		
		
	}

}
