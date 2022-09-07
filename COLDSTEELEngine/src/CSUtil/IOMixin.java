package CSUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import CS.COLDSTEEL;
import Core.Direction;

/**
 * This class contains public static final methods used for writing and loading game files. It is used to save
 * new game objects, and stream in new data. It uses {@code java.nio} for this, for its potential speed applications.
 * 
 * @author Chris Brown
 *
 */
public abstract class IOMixin {

	/**
	 * Uses an existing BufferedWriter to write the specified line.
	 * 
	 * @param writer — BufferedWriter to use
	 * @param line — text to write
	 * @throws IOException — if an error occurs during the BufferedWriter's operation
	 */
	public static final void writeString(BufferedWriter writer , String line) throws IOException  {
		
		writer.write(line);
		
	}
	
	/**
	 * Uses an existing BufferedWriter to write the number of hitboxes present in the calling hitboxset.
	 * 
	 * @param writer — a BufferedWriter
	 * @param numberBoxes — the number of nonnull vertex arrays in the caller's hitboxset array
	 * @throws IOException if an error occurs during the BufferedWriter's operation
	 */
	public static final void writeNumberHitBoxes(BufferedWriter writer , int numberBoxes) throws IOException {
		
		writer.write("size:\n");
		writer.write("\t" + numberBoxes / 7 + "\n");
		
	}
	
	/**
	 * Uses an existing BufferedWriter to write the default direction of the calling hitboxset.
	 * 
	 * @param writer — a BufferedWriter 
	 * @param dir — direction enum
	 * @throws IOException if an error occurs during the BufferedWriter's operation
	 */
	public static final void writeDefaultDirection(BufferedWriter writer , Direction dir) throws IOException {
		
		writer.write("default direction:\n");
		writer.write("\t" + dir.toString() + "\n");
		
	}
	
	public static final void writeHitBoxes(BufferedWriter writer , float[] data) throws IOException {
		
		writer.write("hitboxes:\n");
		for(int i = 0 ; i < data.length ; i += 7) {
			
			writer.write("\t");
			
			for(int j = 0 ; j < 7 ; j ++)
				writer.write(data[i + j] + ",");
			
			writer.write("\n");
			
		}
		
	}
	
	/**
	 * Uses an existing BufferedWriter to write a generic int array.
	 * @param writer — a BufferedWriter
	 * @param array — an int array
	 * @throws IOException if an error occurs during the BufferedWriter's operation
	 */
	public static final void writeIntArray(BufferedWriter writer , int [] array) throws IOException {
		
		writer.write("int array:\n\t");
		if(array != null) for(int x : array) writer.write(x + ",");
		else writer.write("none");
		writer.write("\n");
		
	}
	
	/**
	 * Writes a sprite set's sprites, which are arrays of uv data and dimensions
	 * 
	 * @param writer — a BufferedWriter 
	 * @param sprites — an ArrayList of float arrays representing a series of sprite data
	 * @throws IOException if an error occurs during the BufferedWriter's operation
	 */
	public static final void writeSprites(BufferedWriter writer , ArrayList<float[]> sprites) throws IOException {
		
		writer.write("sprites:\n\t");
		for(float[] x : sprites) {
			
			for(float y : x) writer.write(y + ",");
			writer.write("\n\t");
			
		}
		writer.write("fin\n");
	}

	/**
	 * Writes a swap interval for a sprite set
	 * 
	 * @param writer — a BufferedWriter to write the swap interval
	 * @param swapInterval — a float representing a swap interval
	 * @throws IOException if an error occurs during the BufferedWriter's operation
	 */
	public static final void writeSwapInterval(BufferedWriter writer , float swapInterval) throws IOException {
		
		writer.write("swap interval:\n\t");
		writer.write(swapInterval + "\n");
		
	}
			
	/**
	 * Reads the number of hitboxes recorded in the following file.
	 * 
	 * @param reader — a reader of a hit box set file
	 * @return an int representing the number of hit boxes in this file
	 * @throws IOException if an error occurs during the BufferedReader's operation
	 * @throws AssertionError if this method is not called on a camera tracking component portion of a file
	 */
	public static final int readNumberHitBoxes(BufferedReader reader) throws IOException , AssertionError {
		
		String firstLine = reader.readLine();
		if(COLDSTEEL.DEBUG_CHECKS && !firstLine.equals("size:")) assert false: "\nInvalid Call Site, not called on a number boxes:\nline == " + firstLine;
		return Integer.parseInt(reader.readLine().substring(1));		
		
	}
	
	/**
	 * Reads a direction from a file.
	 * 
	 * @param reader — A BufferedReader to read the file
	 * @return a direction enum value representing the parsed direction
	 * @throws IOException if an error occurs during the BufferedReader's operation
	 * @throws AssertionError if this method is called on an incorrect portion of a file
	 */
	public static final Direction readDefaultDirection(BufferedReader reader) throws IOException , AssertionError {
		
		String first = reader.readLine();
		if(COLDSTEEL.DEBUG_CHECKS && !first.equals("default direction:")) 
			assert false: "\nInvalid Call Site, not called on a default direction:\nline == "  + first;
		return Direction.parse(reader.readLine().substring(1));	
		
	}
	
	/**
	 * Reads a series of arrays representing hitboxes. Uses a number to know how many hit boxes to read. This number should be gotten
	 * by the {@code readNumberHitBoxes()} method prior to this call.
	 * 
	 * @param reader — a Buffered Reader to read the hit boxes
	 * @param number — an int representing the number of hit boxes to read
	 * @return an array of float arrays gotten by parsing this file
	 * @throws IOException if an error occurs during the BufferedReader's operation
	 * @throws AssertionError if this method is called on an incorrect portion of a file
	 */
	public static final float[] readHitBoxes(BufferedReader reader , int size) throws IOException, AssertionError{
		
		String first = reader.readLine();
		if(COLDSTEEL.DEBUG_CHECKS && !first.equals("hitboxes:")) assert false: "\nInvalid Call Site, not called on a hitboxes:\nline == " + first;		
		float[] data = new float[size * 7];
		String line;
		for(int i = 0 ; i < data.length ; i += 7) {
		
			line = reader.readLine().substring(1);	
			String[] splitLine = line.split(",");
			for(int j = 0 ; j < 7 ; j ++) { 
				
				data[i + j] = Float.parseFloat(splitLine[j]);
				
			}
			
		}
		
		return data;
		
	}
	
	/**
	 * Reads a SpriteSet's swap interval.
	 * 
	 * @param reader — a BufferedReader
	 * @return a float representing a swap interval
	 * @throws IOException if an error occurs during the BufferedReader's operation
	 * @throws AssertionError if this method is called on an incorrect portion of a file
	 */
	public static final float readSwapInterval(BufferedReader reader) throws IOException , AssertionError{
	
		String first = reader.readLine();
		if(COLDSTEEL.DEBUG_CHECKS && !first.equals("swap interval:")) assert false: "\nInvalid Call Site, not called on a swap interval:\nline == " + first;
		return Float.parseFloat(reader.readLine().substring(1));
				
	}
	
	/**
	 * Reads a generic int array and returns it. If the word {@code none} is read, an empty array of 5 is returned.
	 * 
	 * @param reader — a BufferedReader to read the int array
	 * @return an int array 
	 * @throws IOException if an error occurs during the BufferedReader's operation
	 * @throws AssertionError if this method is called on an incorrect portion of a file
	 */
	public static final int[] readIntArray(BufferedReader reader) throws IOException , AssertionError {
		
		String first = reader.readLine();
		if(COLDSTEEL.DEBUG_CHECKS && !first.equals("int array:")) assert false: "\nInvalid Call Site, not called on a int array:\nline == " + first;
		String line = reader.readLine().substring(1);
		if(!line.equals("none")) {
		
			String[] split = line.split(",");		
			int[] array = new int[split.length];
			for(int i = 0 ; i < split.length ; i++) array[i] = Integer.parseInt(split[i]);
			return array;
			
		} else return new int[5];
		
	}
	
	/**
	 * Reads a SpriteSet's sprites and returns its ArrayList of float arrays
	 * 
	 * @param reader — a BufferedReader
	 * @return an ArrayList of float arrays holding the parsed sprite data
	 * @throws IOException if an error occurs during the BufferedReader's operation
	 * @throws AssertionError if this method is called on an incorrect portion of a file
	 */
	public static final ArrayList<float[]> readSprites(BufferedReader reader) throws IOException , AssertionError {
		
		String first = reader.readLine();
		if(COLDSTEEL.DEBUG_CHECKS && !first.equals("sprites:")) assert false: "\nInvalid Call Site, not called on a sprites:\nline == " + first;
		ArrayList<float[]> sprites = new ArrayList<float[]>();
		String line;
		while(!(line = reader.readLine().substring(1)).equals("fin")) {
		
			String[] split = line.split(",");
			float[] sprite = new float[split.length];
			for(int i = 0 ; i < split.length ; i++) sprite[i] = Float.parseFloat(split[i]);
			sprites.add(sprite);	
			
		}
		
		return sprites;
		
	}
	
}