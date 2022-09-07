//TODO: DELETE EVERYTHING
package CS;

import static org.lwjgl.system.MemoryUtil.memReport;

import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil.MemoryAllocationReport;

/**
 * Entry of the COLDSTEEL Engine, a 2D game and game editor. 
 * 
 * @author Chris Brown
 *
 */
public class COLDSTEEL {

	public static final String assets;
	public static final String data;
	public static final String deleted;
	public static final String mods;
	public static final String root;
	
	/**
	 * Gets any file folder's filepath for the currently running program. 
	 * This should work regardless of where the program is installed, but requires the queried folder to be located in the same folder
	 * as the running jar file.
	 * 
	 * @return Absolute path of the folder
	 */
	private static final String getPath(String file) {

		String url = COLDSTEEL.class.getProtectionDomain().getCodeSource().getLocation().toString();
		url = url.replaceFirst("file:/", "");
		
		//if running in a jar
		if(url.subSequence(url.length() - 3, url.length()).equals("jar")) 		
			url = url.substring(0 , url.lastIndexOf('/') + 1);
				
		//else running from a bin folder
		else url = url.substring(0, url.length() - 4);		
		
		url += file + "/";
		url = url.replaceAll("%20", " ");//if a folder has a space in it, %20 will be there in its place, so remove it.
		return url;
		
	}

	static {
		
		System.out.println("STATIC:");
		assets = getPath("assets");		
		data = getPath("data");
		deleted = getPath("deleted");
		mods = getPath("mods");
		String incorrectRoot = getPath("");
		root = incorrectRoot.substring(0, incorrectRoot.length() -1);
		
	}
	
	//perform safety checks and tests if this is true
	public static boolean DEBUG_CHECKS = false;
	public static int UI_MEMORY_SIZE_KILOS = 8;
	public static int SOUND_MEMORY_SIZE_MEGAS = 64;
	static int NUMBER_LAYERS = 25;
	
	public static void main(String[] args)  {
		
		if(args[1].equals("debug=true")) {
			
			//config settings
			Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
			Configuration.DEBUG_STACK.set(true);
			Configuration.OPENGL_EXPLICIT_INIT.set(true);
			DEBUG_CHECKS = true;
						
		}
		
		UI_MEMORY_SIZE_KILOS = Integer.parseInt(args[2]);
		SOUND_MEMORY_SIZE_MEGAS = Integer.parseInt(args[3]);
		NUMBER_LAYERS = Integer.parseInt(args[4]);
		
		Engine engine = new Engine(RuntimeState.valueOf(args[0]));
		engine.initialize();
		engine.run();
		engine.shutDown();
		
	    
		//report memory leaks at the very end
	    System.out.println("\nMEMORY LEAK REPORT:");
	    MemoryAllocationReport rep = (address , memory , threadID , threadName , element) ->
	    	System.out.println("At " + address + "; " + memory + " bytes in " + threadName);
	    
	    memReport(rep);
	    
	}

}