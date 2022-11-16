package Core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.System.arraycopy;

import static CSUtil.IOMixin.writeNumberHitBoxes;
import static CSUtil.IOMixin.writeDefaultDirection;
import static CSUtil.IOMixin.writeHitBoxes;
import static CSUtil.IOMixin.readNumberHitBoxes;
import static CSUtil.IOMixin.readDefaultDirection;
import static CSUtil.IOMixin.readHitBoxes;

/**
 * 
 * This class represents hit boxes for entities. It exposes functionality for game effects to be received and dealt. 
 * This class holds metadata for hitboxes; their width and height as floats, their indices, and which are hot or cold.
 * Hot boxes can deal effects, whereas coldboxes can receive effects. Nothing precludes a hitbox from being both hot and cold,
 * but if it is neither, it has no effect on anything and is wasted.
 * 
 * @author Chris Brown
 *
 */
public class HitBoxSets implements GameFiles<HitBoxSets>{
	
	private String name;
	private float[] data;
	private int next = 0;
	public Direction defaultDirection = Direction.RIGHT;
	
	/*
	 * i = int index
	 * i + 1 = float x dimension
	 * i + 2 = float y dimension
	 * i + 3 = float midpoint x offset
	 * i + 4 = float midpoint y offset
	 * i + 5 = int hot box
	 * i + 6 = int cold box
	 * 
	 */
	
	public HitBoxSets(int size) {
		
		data = new float[size * 7];
		
	}
	
	public HitBoxSets() {
	
		data = new float[15 * 7];
		
	}
	
	public HitBoxSets(String namePath) {
		
		load(namePath);
		
	}
	
	public float[] get(int index) {
		
		float[] hitbox = new float[7];
		int offset = index * 7; 
		hitbox[0] = data[offset];
		hitbox[1] = data[offset + 1];
		hitbox[2] = data[offset + 2];
		hitbox[3] = data[offset + 3];
		hitbox[4] = data[offset + 4];
		hitbox[5] = data[offset + 5];
		hitbox[6] = data[offset + 6];
		return hitbox;
		
	}
	
	public void add(int ID , float xSize , float ySize , float midpointXOffset , float midpointYOffset , int hot , int cold) {
		
		data[next] = ID;
		data[next + 1] = xSize;
		data[next + 2] = ySize;
		data[next + 3] = midpointXOffset;
		data[next + 4] = midpointYOffset;
		data[next + 5] = hot;
		data[next + 6] = cold;
		next += 7;
		
		
	}
	
	public void setName(String name) {
		
		this.name = name;
		
	}
	
	public String name() {
		
		return name;
		
	}
	
	public Direction defaultDirection() {
		
		return defaultDirection;
		
	}
	
	public float[] data() {
		
		return data;
		
	}
	
	public void setDefaultDirection(Direction dir) {
		
		this.defaultDirection = dir;
		
	}
	
	public String toString() {
		
		String ret = name + "\n size: " + data.length + "\n boxes;\n";
		for(int i = 0 ; i < data.length ; i ++) ret = ret + data[i] + "\n";		
		return ret; 
		
	}
		
	public void resize(int newSize) {
		
		if (data == null) data = new float[newSize * 7];
		else if(newSize == next / 7) return;		
		else {
			
			float[] newData = new float[newSize * 7];
			if(newSize / 7 > next / 7) arraycopy(data , 0 , newData , 0 , newData.length); //making it smaller
			else arraycopy(data , 0 , newData , 0 , data.length); // making it larger
			data = newData;
			
		}		
		
	}
	
	public int size() {
		
		return data.length / 7;
		
	}
	
	public String hitboxAsString(int index) {
		
		int off = index * 7;
		return data[off] + ", " + 
			   data[off + 1] + ", " + 
			   data[off + 2] + ", " + 
			   data[off + 3] + ", " + 
			   data[off + 4] + ", " + 
			   data[off + 5] + ", " + 
			   data[off + 6];
		
	}
	
	public int[] getIndices() {
		
		int[] indices = new int[data.length / 7];
		for(int i = 0 , k = 0 ; i < data.length ; i += 7 , k ++) indices[k] = (int)data[i];
		return indices;
		
	}
	
	public int hot(int index) {
		
		return (int)data[index + 5];
		
	}
	
	public int cold(int index) {
		
		return (int)data[index + 6];
		
	}
	
	public HitBoxSets copy() {
		
		HitBoxSets newHitBoxSet = new HitBoxSets(data.length / 7);
		newHitBoxSet.next = next;
		newHitBoxSet.name = name;
		newHitBoxSet.defaultDirection = defaultDirection;
		System.arraycopy(data, 0, newHitBoxSet.data, 0, data.length);
		return newHitBoxSet;
		
	}
	
	@Override public void delete() {
		
		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("deleted/" + name + ".CStf"), Charset.forName("UTF-8"))){
			
			writer.write(name + "\n");
			writeNumberHitBoxes(writer , next);
			writeDefaultDirection(writer , defaultDirection);
			writeHitBoxes(writer , data);

			Files.deleteIfExists(Paths.get("data/hitboxes/" + name + ".CStf"));
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}		
		
	}

	@Override public void write(Object...additionalData) {
	
		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/hitboxsets/" + name + ".CStf"), Charset.forName("UTF-8"))){
			
			writer.write(name + "\n");
			writeNumberHitBoxes(writer , next);
			writeDefaultDirection(writer , defaultDirection);
			writeHitBoxes(writer , data);
						
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}	

	@Override public void load(String namePath) {
		
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(CS.COLDSTEEL.data + "hitboxsets/" + namePath))){
		
			name = reader.readLine();
			resize(readNumberHitBoxes(reader));
			defaultDirection = readDefaultDirection(reader);
			data = readHitBoxes(reader , data.length / 7);			
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}

	@Override public void write(BufferedWriter writer , Object...additionalData) {
		
		try {
			
			writer.write(name);
			writer.newLine();
			writeNumberHitBoxes(writer , next);
			writeDefaultDirection(writer , defaultDirection);
			writeHitBoxes(writer , data);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

}