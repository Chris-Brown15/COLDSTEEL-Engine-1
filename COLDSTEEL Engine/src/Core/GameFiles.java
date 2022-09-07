package Core;

import static CS.COLDSTEEL.deleted;
import static CSUtil.BigMixin.toLocalPath;
import static CSUtil.BigMixin.toNamePath;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import org.python.google.common.io.Files;

public interface GameFiles <T> {

	public static void delete(File file , File destination) {

		try {
			
			Files.move(file , destination);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static void delete(String path) {
		
		String localPath = (String) toLocalPath(path);
		String namePath = toNamePath(localPath);
		
		try {
			
			Files.move(new File(localPath) , new File(deleted + namePath));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public void delete();	
	public void write(Object... additionalData);
	public void write(BufferedWriter writer , Object... additionalData) throws IOException;
	public void load(String namePath);
	public void load(BufferedReader reader) throws IOException;	
	
}
