package CSUtil;

import static CS.COLDSTEEL.logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;

/**
 * 
 * Class for logging information to files. Each day will create a new folder within the logs folder. For each application startup, a new text file
 * will be created which will contain all logged data.
 * 
 * @author Chris Brown
 *
 */
public class CSLogger {

	private static FileOutputStream writer;
	public static boolean LOGGING_ENABLED = false;

	public static final void initialize() {

		if(LOGGING_ENABLED) {

			//determine if a file for this day has been created
			Calendar c = Calendar.getInstance();
			
			int year = c.get(Calendar.YEAR); int month = c.get(Calendar.MONTH); int day = c.get(Calendar.DAY_OF_MONTH);
			
			String fileName = year + "." + month + "." + day;		
			File[] logFolders = new File(logs).listFiles();
			boolean thisDaysFolderExists = false;
			
			for(File x : logFolders) {
				
				if(x.getName().equals(fileName)) {
					
					thisDaysFolderExists = true;
					
				}
				
			}
			
			try {

				if(!thisDaysFolderExists) Files.createDirectory(Paths.get(logs + fileName + "/"));			
				writer = new FileOutputStream(logs + fileName + "/" + System.currentTimeMillis() + ".txt");
				log("LOGGER INITIALIZED");
				
			} catch(IOException e) {
				
				e.printStackTrace();
				
				throw new IllegalStateException("Unable to create logging object");
				
			}
			
		} else writer = null;
		
	}
	
	public static final void log(String log) {
		
		if(!LOGGING_ENABLED) return;
		
		try {
			
			writer.write((log + System.lineSeparator()).getBytes());
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
	public static final void shutDown() {

		if(!LOGGING_ENABLED) return;
		
		try {
			
			writer.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
}
