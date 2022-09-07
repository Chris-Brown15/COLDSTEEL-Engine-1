package Editor;

import java.util.ArrayList;

import Core.HitBoxSets;
import Core.Quads;
import Core.Direction;

import static CSUtil.BigMixin.getHitBoxArray;
import static CSUtil.BigMixin.dragResize;
import static CSUtil.BigMixin.moveTo;
import static CSUtil.BigMixin.translateArray;
import static CSUtil.BigMixin.getArrayWidth;
import static CSUtil.BigMixin.getArrayHeight;
import static CSUtil.BigMixin.getArrayMidpoint;
import static CSUtil.BigMixin.setArrayWidth;
import static CSUtil.BigMixin.setArrayHeight;


/**
 * This class is for use in the editor. It allows the creation and manipulation of marker hitboxes so we can specify hitboxsets.
 * 
 * @author Chris Brown
 * 
 */
public class HitBoxSetMarker {
	
	//list of float arrays which are quads that show the dimensions of each hitbox
	private ArrayList<float[]> hitboxes;
	int[] hotBoxes;
	int[] coldBoxes;
	String editingName = null;	
	Direction defaultDirection = Direction.RIGHT;
	int active;
		
	HitBoxSetMarker(int size){
		
		hitboxes = new ArrayList<float[]>(size);
		hotBoxes = new int[size];
		coldBoxes = new int[size];		
		
	}
	
	HitBoxSetMarker(){
		
		hitboxes = new ArrayList<float[]>(15);
		hotBoxes = new int[15];
		coldBoxes = new int[15];
		
	}

	float[] addHitBox(Quads moveToThis) {
		
		float[] hitbox = getHitBoxArray(); 
		moveTo(moveToThis , hitbox); 
		hitboxes.add(hitbox);
		return hitbox;
		
	}
	
	float[] removeHitBox(int index) {
		
		return hitboxes.remove(index);
		
	}
	
	int getSize() {
		
		return hitboxes.size();
		
	}
	
	void removeHitBoxes() {
		
		hitboxes.clear();
		
	}
	
	float[] get(int index) {
		
		return hitboxes.get(index);
		
	}
	
	void transform(int index , float xTrans , float yTrans) {
		
		translateArray(hitboxes.get(index) , xTrans , yTrans);
		
	}
	
	void drag(int index , float cursorX , float cursorY) {
		
		hitboxes.set(index , dragResize(hitboxes.get(index) , cursorX , cursorY));
		
	}

	void reset() {
		
		editingName = null;
		hitboxes.clear();
		
	}
	
	float[] active() {
		
		return hitboxes.get(active);
		
	}
	
	HitBoxSets toHitBoxSet(Quads relative) {
		
		HitBoxSets newSet = new HitBoxSets(hitboxes.size()) ;
		
		if(hitboxes.size() != 0) {
			
			float[] iter = hitboxes.get(0);
			float[] iterMid = getArrayMidpoint(iter);
			float[] relMid = relative.getMidpoint();
			
			for(int i = 0 ; i < hitboxes.size() ; i++) {
				
				iter = hitboxes.get(i);
				iterMid = getArrayMidpoint(iter);
				newSet.add(i , getArrayWidth(iter) , getArrayHeight(iter) , iterMid[0] - relMid[0] , iterMid[1] - relMid[1] , hotBoxes[i] , coldBoxes[i]);
				
			}
			
		}
		
		newSet.setName(editingName);
		newSet.setDefaultDirection(defaultDirection);
		return newSet;
		
	}
	
	/**
	 * Converts given HitBoxSet into a hitboxset marker
	 * 
	 * @param set
	 * @param relative
	 * @return
	 */
	ArrayList<float[]> fromHitBoxSet(HitBoxSets set , Quads relative) {
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
		float[] data = set.data();
		for(int i = 0 , j = 0 ; i < data.length ; i += 7 , j ++) {
			
			float[] hitbox = getHitBoxArray();
			setArrayWidth(hitbox , data[i + 1]);
			setArrayHeight(hitbox , data[i + 2]);
			moveTo(relative , hitbox);
			translateArray(hitbox , data[i + 3] , data[i + 4]);
			hotBoxes[j] = (int)data[i + 5];
			coldBoxes[j] = (int)data[i + 6];
			hitboxes.add(hitbox);
					
		}
		
		editingName = set.name();
		defaultDirection = set.defaultDirection;
		return hitboxes;
		
	}
	
	ArrayList<float[]> hitboxes(){
		
		return hitboxes;
		
	}
	
	void clear() {
		
		hitboxes.clear();
		
	}
	
	@SuppressWarnings("unchecked")
	void copy(HitBoxSetMarker source) {
		
		this.hitboxes = (ArrayList<float[]>) source.hitboxes.clone();
		this.hotBoxes = source.hotBoxes.clone();
		this.coldBoxes = source.coldBoxes.clone();
		this.editingName = source.editingName;
		this.defaultDirection = source.defaultDirection;
				
	}
	
}
