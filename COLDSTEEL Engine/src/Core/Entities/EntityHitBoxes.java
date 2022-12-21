package Core.Entities;

import static CSUtil.BigMixin.getHitBoxArray;
import static CSUtil.BigMixin.setArrayWidth;
import static CSUtil.BigMixin.setArrayHeight;
import static CSUtil.BigMixin.translateArray;
import static CSUtil.BigMixin.moveTo;
import java.util.ArrayList;
import java.util.Iterator;

import Core.HitBoxSets;
import Core.Quads;
import Core.Direction;

public class EntityHitBoxes implements Iterable<HitBoxSets> {

	private ArrayList<HitBoxSets> sets = new ArrayList<HitBoxSets>();
	
	private float[][] hitboxes;
	private int active = -1;
	
	public EntityHitBoxes(int size){
		
		hitboxes = new float[size][];
		for(int i = 0 ; i < size ; i ++) hitboxes[i] = getHitBoxArray();
		
	}	
	
	public EntityHitBoxes() {
		
		hitboxes = new float[15][];
		for(int i = 0 ; i < hitboxes.length ; i ++) hitboxes[i] = getHitBoxArray();
		
	}
	
	public HitBoxSets get(int index) {
		
		return sets.get(index);
		
	}
	
	public int active() {
		
		return active;
		
	}
	
	public int totalLength() {
		
		return hitboxes.length;
		
	}
	
	public void activate(int active) {
		
		assert active >= -1 && active < sets.size() : "Index " + active + " out of bounds for size " + sets.size();		
		this.active = active;
		
	}
		
	public void addSet(HitBoxSets set) {
		
		sets.add(set);
		
	}
	
	public ArrayList<HitBoxSets> getSets(){
		
		return sets;
		
	}
	
	public float[][] getAllHitBoxes(){
		
		return hitboxes;
		
	}
	
	public int numberSets() {
		
		return sets.size();
		
	}	
	
	/** 
	 * 
	 * @return the number of available hit boxes a hitboxset may use. Defaults to 15.
	 * 
	 */
	public int getNumberAvailableHitBoxes() {
		
		return hitboxes.length;
		
	}
	
	public void setNumberAvailableHitBoxes(int number) {
		
		hitboxes = new float[number][];
		for(int i = 0 ; i < number ; i ++) hitboxes[i] = getHitBoxArray();
		
	}
	
	public int hot(int index) {
		
		return sets.get(active).hot(index);
		
	}
	
	public int cold(int index) {
		
		return sets.get(active).cold(index);
		
	}

	public boolean isHot(int index) {
		
		return sets.get(active).hot(index) != -1;
		
	}
	
	public boolean isCold(int index) {
		
		return sets.get(active).cold(index) != -1;
		
	}
	
	public float[][] getActiveHitBoxes(Quads Q , Direction targetDir){
		
		if(active != -1) {
			
			setupActive(Q , targetDir);
			HitBoxSets activeSet = sets.get(active);
			float[][] boxes = new float[activeSet.size()][];
			for(int i = 0 ; i < activeSet.size() ; i ++) boxes[i] = hitboxes[i];
			return boxes;
			
		} else return null;
		
	}

	/**
	 * 
	 * Sets up the active hitbox by resizing its arrays and moving them into position
	 * 
	 * @param E
	 */
	public void setupActive(Quads E , Direction targetDir) {
		
		if(active != -1) {
			
			HitBoxSets activeSet = sets.get(active);
			float[] data = activeSet.data();
			int index = -1;
			
			for(int i = 0 ; i < data.length ; i += 7) { //iterate through boxes
				
				index = (int)data[i];			
				setArrayWidth(hitboxes[index] , data[i + 1]);
				setArrayHeight(hitboxes[index] , data[i + 2]);//sets its width and height			
				//moves it into position (doesnt work for switching directions)
				moveTo(E , hitboxes[index]);
				if(targetDir != null && targetDir != activeSet.defaultDirection) translateArray(hitboxes[index] , -data[i + 3] , data[i + 4]);
				else translateArray(hitboxes[index] ,  data[i + 3] , data[i + 4]);  
				
			}		
			
		}		
		
	}

	@Override public Iterator<HitBoxSets> iterator() {
		
		return sets.iterator();
				
	}
	
	public boolean has(HitBoxSets set) {
		
		for(int i = 0 ; i < sets.size() ; i++) if(sets.get(i).name().equals(set.name())) return true;
		return false;
		
	}
	
	public int indexOf(HitBoxSets marker) {
		
		for(int i = 0 ; i < sets.size() ; i ++) if(sets.get(i).name().equals(marker.name())) return i;
		return -1;
		
	}
	
	public EntityHitBoxes copy() {
		
		EntityHitBoxes copy = new EntityHitBoxes(sets.size());
		for(HitBoxSets x : this) copy.addSet(x.copy());
		copy.active = active;
		return copy;
		
	}
	
}