package Core.Entities;

import CS.COLDSTEEL;
import CS.RuntimeState;
import CSUtil.DataStructures.CSArray;
import Core.Executor;
import Core.SpriteSets;

/**
 * 
 * Animation manager for entities. This class holds a list of animations and extra utilities for scripting with animations.
 * <br><br>
 *
 * One of these is the hangup feature. When {@code startHangup} is called, a hangup is started. A hangup means the current animation will continue to play until 
 * it is finished.  Another feature is to pause this object's ability to play animations at all.
 
 * @author Chris Brown
 *
 */
public class EntityAnimations {

	private CSArray<SpriteSets> anims;
	private int active = 0;
	private boolean animate = true;
	private boolean hangup = false;
	private int hangupIndex = -1;
	private int hangupRepititions = 0;
	private Executor hangupCallback;
		
	/**
	 * This constructor will prevent the animation list from growing, making it fixed in size
	 * @param size — the number of elements that can be stored 
	 */
	public EntityAnimations(int size) {
		
		if(CS.Engine.STATE == RuntimeState.EDITOR) anims = new CSArray<SpriteSets>(size , 2);
		else if (CS.Engine.STATE == RuntimeState.GAME) anims = new CSArray<SpriteSets>(size);
		
	}
	
	public void add(SpriteSets anim) {
		
		anims.add(anim);
		
	}
	
	public SpriteSets remove(int index) {
		
		return anims.remove(index);
		
	}
	
	public SpriteSets get(int index) {
		
		return anims.get(index);
		
	}
	
	public SpriteSets active() {
		
		if(hangup) return anims.get(hangupIndex);
		else return anims.get(active);
						
	}

	public int activeIndex() {
		
		return active;
		
	}
	
	/**
	 * Main way of selecting an animation to play by the ECS. If a hangup isn't active, sets the active anim to index.
	 * If a hangup is active, nothing happens. 
	 * 
	 * @param index
	 */
	public void activate(int index) {
				
		if(!hangup) {
			
			if(COLDSTEEL.DEBUG_CHECKS) {

				assert index < anims.size() && anims.get(index) != null : "Invalid index for entity animation: " + index + "\n" +
					"either size is greater than or equal to " + anims.size() + " or, the element at " + index + " is null";
				
			}
			
			if(active != index) anims.get(index).reset();//reset the queried anim if it is newly activated
			active = index;
			
		}
		
	}
	
	public int size() {
		
		return anims.size();
		
	}
	
	public SpriteSets[] anims() {
		
		SpriteSets[] arr = new SpriteSets[anims.size()];
		for(int i = 0 ; i < arr.length ; i ++) arr[i] = (SpriteSets)anims.get(i);
		return arr;
		
	}
	
	public boolean animate() {
		
		return animate;
		
	}
	
	public void animate(boolean animate) {
		
		this.animate = animate;
		
	}
	
	/**
	 * Stops any other animations from being activated until the current one is finished
	 */
	public void startHangup() {
		
		hangup = true;
		hangupIndex = active;
		
	}
		
	public void startHangup(int repititions) {
		
		hangup = true;
		hangupIndex = active;
		hangupRepititions = repititions;
		
	}
	
	public void handleHangup() {
		
		if(isHungup() && active().lastFrameOfLastSprite()) {
			
			hangupRepititions -= 1;
			if(hangupRepititions < 0) hangup = false;
			if(!hangup && hangupCallback != null) {
				
				hangupCallback.execute();
				hangupCallback = null;		
				
			}
			
		}		
		
	}
	
	/**
	 * Will end this hangup if the hangupRepititions is currently 0. If {@code startHangup()} is called, after one cycle of the animation, the hangup will end.
	 * <br> <br>
	 * If {@code startHangup(int n)} is called however, the hungup animation must complete {@code n} times before the hangup is ended. 
	 */
	public void tryEndHangup() {
		
		if(hangupRepititions <= 0) hangup = false;
		if(!hangup && hangupCallback != null) {
			
			hangupCallback.execute();
			hangupCallback = null;		
		
		}
		
	}
	
	/**
	 * Ends any current hangup even if there were more repititions needed to be completed before the hangup would normally be ended. 
	 */
	public void endHangup() {
		
		hangupRepititions = 0;
		hangup = false;
		if(hangupCallback != null) {
						
			hangupCallback.execute();
			hangupCallback = null;
			
		}
		
	}
	
	public void onHangupEnd(Executor callback) {
		
		this.hangupCallback = callback;
		
	}
	
	public boolean hasSpriteSet(SpriteSets anim) {
		
		SpriteSets x = anims.get(0);		
		for(int i = 0 ; i < anims.size() ; i ++) {
			
			x = anims.get(i);
			if(x.name().equals(anim.name())) return true;
			
		}
		
		return false;
		
	}
	
	public float[] currentSprite() {
		
		return anims.get(active).getActiveSprite();
		
	}
	
	public int indexOf(SpriteSets anim) {
		
		SpriteSets x = anims.get(0);		
		for(int i = 0 ; i < anims.size() ; i ++) {
			
			x = anims.get(i);
			if(x.name().equals(anim.name())) return i;
			
		}
		
		return -1;
		
	}
	
	public void setAnimation(int index , SpriteSets set) {
		
		anims.set(index, set);
		
	}
	
	public boolean isHungup() {
		
		return hangup;
		
	}
	
}