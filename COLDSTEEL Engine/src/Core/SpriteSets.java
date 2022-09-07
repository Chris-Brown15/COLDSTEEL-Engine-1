package Core;

import static CS.COLDSTEEL.data;
import static CSUtil.IOMixin.writeSwapInterval;
import static CSUtil.IOMixin.writeDefaultDirection;
import static CSUtil.IOMixin.writeSprites;
import static CSUtil.IOMixin.readSwapInterval;
import static CSUtil.IOMixin.readDefaultDirection;
import static CSUtil.IOMixin.readSprites;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import CS.Engine;
import CSUtil.Timer;
/**
 * sprite sets are subsets of sprite sheets. sprite sets are backed by two data structures.
 * an ArrayList of float arrays hold the data for individual sprites and a sprite set as a whole is intended to display for
   an object's state. For example, a player entity may have several states, and each state gets a sprite set, but only one texture
   which is the sprite sheet.

	The sprite might also have three additional data points after its height value. These are; int ID, float xOffset , float yOffset.
	These refer to a joint. This data will be pushed up to the object being animated by this sprite set, and it will use it to move joints around.
	The ID refers to the ID of the joint to move, the x offset refers to the distance from the top left vertex's x component, and the yOffset
	refers to the distance from the y component of the top left vertex.   

 * @author Chris Brown
 *
 */
public class SpriteSets implements GameFiles<SpriteSets> { 

	private ArrayList<float[]> sprites = new ArrayList<float[]>();	
	// first element is 	left U values
	// second element is 	right U values
	// third element is 	top V values
	// fourth element is 	bottom V values
	// fith element is 		width
	// sixth element is 	height
	// all these values describe a sprite within a single array, and the elements of the sprites LinkedList are different sprites.
	
	private Timer timer = new Timer();
	private float swapInterval = 1.0f;
	private CharSequence name;
	private int currentSprite = 0; //index to starting sprite, increments as new sprites are needed
	private boolean runSpriteSet = true;
	public boolean lastSprite = false;
	public boolean freeze = false;	
	private boolean reverse = false;
	public Direction defaultDirection = Direction.RIGHT;
	
	public SpriteSets(CharSequence name){

		this.name = name;

	}
	
	public SpriteSets(String namePath) {
		
		load(namePath);
		
	}
	
	public void print() {
		
		for(float[] x : sprites) for(float y : x) System.out.println(y);
		
	}
		
	/**
	 * 	
		Width and height should be as distances between respective faces from the midpoint. The width then should be the distance from the midpoint
		to the left or right face. Likewise length should be distance from midpoint to top face.
		
	 * @param leftU 
	 * @param rightU
	 * @param topV
	 * @param bottomV
	 * @param width
	 * @param height
	 */
	public void storeSprite(float leftU , float rightU , float topV , float bottomV , float width , float height){

		sprites.add(new float [] {leftU , rightU , topV , bottomV , width , height});

	}
	
	public void replaceSprite(int index , float leftU , float rightU , float topV , float bottomV , float width , float height) {
		
		sprites.set(index, new float[] {leftU , rightU , topV , bottomV , width , height});
		
	}
	
	public void storeSprite(float[] sprite) {
		
		sprites.add(sprite);
		
	}

	/**
	 * Main method for animation. This returns an array of data which notates how an object should display a sector of a texture. Animations are based
	 * on millisecond timers rather than frame counters so there's no gaurantee each sprite of an animation will display for exactly the same number of 
	 * frames.
	 * 
	 * @return array of data instructing quad how to manipulate itself to display sprites.
	 */
	public float[] swapSprite(){

		if(freeze) return sprites.get(currentSprite);				
		lastSprite = false;
		
		if(runSpriteSet){
			
			if (reverse) {
				
				if(timer.getElapsedTimeSecs() > swapInterval) {
					
					timer.start();
					currentSprite--;
					if(currentSprite == 0) lastSprite = true;
									
				}
				
				if(lastSprite) currentSprite = sprites.size() - 1;
				return sprites.get(currentSprite);
				
			} else {
				
				if(timer.getElapsedTimeSecs() > swapInterval){
					
					timer.start();
					currentSprite++;
					//if the index to the sprite is the last valid index of the List,
					if(currentSprite > sprites.size() -1) currentSprite = 0;
					
					if(sprites.size()-1 > 0){//so long as there is at least one sprite in the sprites List
						
						if (currentSprite == sprites.size() - 1) lastSprite = true;			
						sprites.get(currentSprite);
						
					}
					
				}
				
			}			

			return sprites.get(currentSprite);			

		} else {

			float[] defaultData = {0 , 1 , 1 , 0 , 100 , 100};
			return defaultData;

		}

	}
	
	/**
	 * Will return sprite data, but only swap through sprites if run is true. This means setting run to false will make this always return the 
	 * currentSprite index of the sprites array.
	 * 
	 * @param run — whether to loop through sprites, returning the appropriate value, or return a constant value
	 * @return float array containing data for use with animations
	 */
	public float[] swapSprite(boolean run){//return array of data instructing quad how to manipulate itself to display sprites.

		if(freeze) return sprites.get(currentSprite);				
		lastSprite = false;		
		
		if(!reverse) {
			
			if(timer.getElapsedTimeSecs() > swapInterval){
				
				timer.start();
				if (run) currentSprite++;
				if(currentSprite > sprites.size()-1) currentSprite = 0; //if the index to the sprite is the last valid index of the List,						
				
			}
			
			if (currentSprite == sprites.size() - 1) lastSprite = true;
			return sprites.get(currentSprite);
			
		} else {
			
			if(timer.getElapsedTimeSecs() > swapInterval) {
				
				timer.start();
				if(run) currentSprite--;
				if(currentSprite == 0) lastSprite = true;
								
			}
			
			if(lastSprite) currentSprite = sprites.size() - 1;
			return sprites.get(currentSprite);
			
		}
			
	}
	
	public void replaceSprite(int index , float[] values){

		sprites.add(index, values);
		sprites.remove(index + 1);
		
	}

	public void deleteSprite(int index){

		sprites.remove(index);

	}
	
	public float[] getActiveSprite() {
		
		return sprites.get(currentSprite);
		
	}
	
	public float[] getSprite(int index){

		return sprites.get(index);

	}
	
	public int getNumberSprites(){

		return sprites.size();

	}
	
	public void setSprites(ArrayList<float[]> sprites){

		this.sprites = sprites;

	}
	
	public double getCurrentTime(){

		return timer.getElapsedTimeSecs();

	}
	
	/**
	 * Returns the remaining time in millis until the animation will repeat itself
	 * 
	 * @return total time to complete the animation minus the elapsed time from the start of the animation
	 */
	public double getRemainingTime() {
		
		return (sprites.size() * swapInterval) - (currentSprite * swapInterval);
		
	}
	
	public double completionTime() {
		
		return sprites.size() * swapInterval;
		
	}
	
	public boolean lastFrameOfLastSprite() {
		
		if(!(currentSprite == sprites.size() - 1)) return false;
		else {
			
			/*
			 * return true if by the next frame it will be time to swap sprites to the first one.
			 * if there are more milliseconds in one frame than there are remaining in the animation, which must
			 * be in it's last frame to be in this block
			 *
			 */
			
			return Engine.iterationRateLastSecond() < timer.getElapsedTimeMillis();
			
		}
		
	}
	
	public ArrayList<float[]> getSprites(){

		return sprites;

	}
	
	/**
	 * Returns a list of joints. Each joint is represented by a 3-float array where index 0 represents the joint's ID, index 1 represents
	 * its x offset from the quad's top left vertex X position, and index 2 represents the joint's y offset from the quad's top left Y position.
	 * 
	 * @return
	 */
	public ArrayList<float[]> getJoints(int index){
		
		ArrayList<float[]> joints = new ArrayList<float[]>();
		float[] query = sprites.get(index);		
		int endingIndex = query.length % 3 != 0 ? query.length -2 : query.length -1;				
		for(int i = 6 ; i < endingIndex ; i += 3) joints.add(new float[] {query[i] , query[i + 1] , query[i + 2]});		
		return joints;
		
	}
	
	public CharSequence name(){

		return name;

	}
	
	public float getSwapInterval() {

		return swapInterval;

	}
	
	public Direction defaultDirection() {
		
		return defaultDirection;
		
	}
	
	public void setSwapInterval(float swapInterval) {

		this.swapInterval = swapInterval;

	}
	
	public int getCurrentSprite() {

		return currentSprite;

	}
	
	public void setCurrentSprite(int currentSprite) {

		this.currentSprite = currentSprite;

	}
	
	public void setCurrentToLast() {
		
		currentSprite = sprites.size() - 1;
		
	}
	
	public void setRunSpriteSet(boolean runSpriteSet){

		this.runSpriteSet = runSpriteSet;

	}
		
	public void toggleRunSpriteSet(){

		runSpriteSet = runSpriteSet ? false:true;

	}
	
	public boolean getRunSpriteSet(){

		return runSpriteSet;

	}
	
	/**
	 * Duplicates sprites not at the beginning of end of the list and appends then in descending order.
	 */
	public void appendReverseFromEnd() {
		
		for(int i = sprites.size() - 2 ; i > 0 ; i--) sprites.add(sprites.get(i));
		
	}

	/**
	 * Pass 1 for facing right, 0 for facing left.
	 * 
	 * @param directionInt — integer representation of the default direction
	 */
	public void setDefaultDirection(int directionInt) {
		
		if(directionInt == 1) this.defaultDirection = Direction.RIGHT;
		else if(directionInt == 0) this.defaultDirection = Direction.LEFT;
		else if(directionInt == 2) defaultDirection = Direction.UP;
		else if(directionInt == 3) defaultDirection = Direction.DOWN;
		
	}
	
	public void setDefaultDirection(Direction defaultDirection) {
		
		this.defaultDirection = defaultDirection;
		
	}
		
	public void reset() {
		
		currentSprite = 0;		
		timer.start();
		freeze = false;
		runSpriteSet = true;
		reverse = false;
		lastSprite = false;
		
	}

	public void fixWidth(float textureWidth) {
		
		float largestWidth = Float.MIN_VALUE;
		for(float[] x : sprites) if(x[4] > largestWidth) largestWidth = x[4];//x[4] = sprite width
		for(float[] x : sprites) {
			
			float difference = (largestWidth - x[4]) / textureWidth;
			x[0] -= difference;
			x[1] += difference;
			x[4] = largestWidth;
			
		}
		
	}
	
	public void fixHeight(float textureHeight) {
		
		float largestHeight = Float.MIN_VALUE;
		for(float[] x : sprites) if(x[5] > largestHeight) largestHeight = x[5];//x[5] = sprite height
		for(float[] x : sprites) {
			
			float difference = (largestHeight - x[5]) / textureHeight;
			x[2] += difference * 2f;
			x[5] = largestHeight;
			
		}
		
	}
	
	public void reverse(boolean reverse) {
		
		this.reverse = reverse;
		timer.start();
		currentSprite = sprites.size() -1 ;
		
	}
	
	public boolean reverse() {
		
		return reverse;
		
	}
	
	public SpriteSets copy() {
		
		SpriteSets copy = new SpriteSets(name);
		for(float[] x : sprites) {
			
			float[] xCopy = new float[x.length];
			System.arraycopy(x, 0, xCopy, 0, x.length);
			copy.storeSprite(xCopy);
			
		}
		
		copy.swapInterval = swapInterval;		
		copy.defaultDirection = defaultDirection;
		
		return copy;
		
		
	}
	
	public float getWidthOf(int index) {
		
		return sprites.get(index)[4];
		
	}

	public float getHeightOf(int index) {
		
		return sprites.get(index)[5];
		
	}
	
	@Override public void delete() {

		try {
			
			Files.move(Paths.get(CS.COLDSTEEL.data + "/spritesets/" + name + ".CStf") , Paths.get(CS.COLDSTEEL.deleted + name + ".CStf"));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
					
	}

	@Override public void write(Object...additionalData) {

		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/spritesets/" + name + ".CStf"), Charset.forName("UTF-8"))){

			writer.write(name + "\n");
			writeSwapInterval(writer , swapInterval);
			writeDefaultDirection(writer , defaultDirection);
			
			writeSprites(writer, sprites);
						
		} catch (IOException e1) {

			e1.printStackTrace();
		}

	}
	
	@Override public void load(String filepath) {

		try(BufferedReader reader = Files.newBufferedReader(Paths.get(data + "spritesets/" + filepath))){
 
			name = reader.readLine();
			swapInterval = readSwapInterval(reader);
			defaultDirection = readDefaultDirection(reader);
			sprites = readSprites(reader);
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
	}

	@Override public void write(BufferedWriter writer , Object...additionalData) {
		
		try {
			
			writer.write(name + "\n");
			writeSwapInterval(writer , swapInterval);
			writeDefaultDirection(writer , defaultDirection);
			writeSprites(writer, sprites);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override
	public void load(BufferedReader reader) {
		// TODO Auto-generated method stub
		
	}

}