package Core.TileSets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.function.Consumer;

import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple4;
import CSUtil.DataStructures.cdNode;
import Core.Scene;
import Core.SpriteSets;
import Core.Statics.Statics;
import Physics.Colliders;
import Renderer.Textures;

public class Tiles extends Statics {
	
	private boolean hasCollider = false;
	private boolean isSource = true;
	public boolean markedForRemoval = false;
	private CSLinked<Tiles> instances;
	float[] offset = {0f , 0f};
	SpriteSets animation;
	
	Tiles(float[] UVsAndDims , Textures texture , String name , int ID){
		
		super(name , ID);
		setTexture(texture);
		swapSprite(UVsAndDims);
		setWidth(UVsAndDims[4]);
		setHeight(UVsAndDims[5]);
		
	}

	public float[] tileData() {
		
		float[] UVs = getUVs();
		float width = getWidth() , height = getHeight();
		return new float[] {UVs[0] , UVs[1] , UVs[2] , UVs[3] , width , height};
		
	}
		
	public boolean hasCollider() {
		
		return hasCollider;
		
	}
	
	public boolean isSource() {
		
		return isSource;
		
	}
	
	public void isSource(boolean source) {
		
		if(source) instances = new CSLinked<Tiles>();
		isSource = source;
		
	}
	
	public void addInstance(Tiles instance) {
		
		instances.add(instance);
		
	}

	public void removeInstance(Tiles instance) {
		
		instances.removeVal(instance);
		
	}
	
	public int numberInstances() {
		
		return instances.size();
		
	}
	
	void hasCollider(Scene owner , boolean collider) {
		
		if(hasCollider && !collider) {
			
			Colliders gotten = getCollider(0);
			if(!isSource) owner.colliders().delete(gotten);
			
		} else if (!hasCollider && collider) {
			
			Colliders added = addCollider();
			added.setWidth(getWidth());
			added.setHeight(getHeight());
			added.moveTo(this);
			if(!isSource) owner.colliders().add(added);
			
		}
		
		this.hasCollider = collider;
		
	}
	
	public float leftU() {
		
		return vertexData[16];
		
	}
	
	public float topV() {
		
		return vertexData[17];
		
	}
	
	public void toggleCollider(Scene owner) {
		
		hasCollider(owner , hasCollider ? false:true);		
		
	}
	
	public String toString() {
	
		return name + ", ID: " + ID;
		
	}
	
	public String toStringAndDetails() {
		float[] mid = getMidpoint();
		return name + ", ID: " + ID + ", pos: " + mid[0] + ", " + mid[1];
	}
	
	public boolean isColliderUpperRightTriangle() {
		
		return colliders.get(0).isUpperRightTriangle();
		
	}

	public boolean isColliderUpperLeftTriangle() {
		
		return colliders.get(0).isUpperLeftTriangle();
		
	}

	public boolean isColliderLowerRightTriangle() {
		
		return colliders.get(0).isLowerRightTriangle();
		
	}

	public boolean isColliderLowerLeftTriangle() {
		
		return colliders.get(0).isLowerLeftTriangle();
		
	}
	
	public boolean isColliderPlatform() {
		
		return colliders.get(0).isPlatform();
		
	}
	
	public void toggleUpperRightTriangle() {
		
		if(isColliderUpperRightTriangle()) colliders.get(0).resetState();
		else colliders.get(0).makeUpperRightTriangle();
		
		if(isSource) instances.forEachVal(x -> x.toggleUpperRightTriangle());
		
	}

	public void toggleUpperLeftTriangle() {
		
		if(isColliderUpperLeftTriangle()) colliders.get(0).resetState();
		else colliders.get(0).makeUpperLeftTriangle();
	
		if(isSource) instances.forEachVal(x -> x.toggleUpperLeftTriangle());
		
	}

	public void toggleLowerRightTriangle() {
		
		if(isColliderLowerRightTriangle()) colliders.get(0).resetState();
		else colliders.get(0).makeLowerRightTriangle();
		
		if(isSource) instances.forEachVal(x -> x.toggleLowerRightTriangle());
		
	}

	public void toggleLowerLeftTriangle() {
		
		if(isColliderLowerLeftTriangle()) colliders.get(0).resetState();
		else colliders.get(0).makeLowerLeftTriangle();

		if(isSource) instances.forEachVal(x -> x.toggleLowerLeftTriangle());
		
	}

	public void togglePlatform() {
		
		if(isColliderPlatform()) colliders.get(0).resetState();
		else { 
			
			colliders.get(0).makePlatform();
			colliders.get(0).setWidth(getWidth());
			colliders.get(0).setHeight(getHeight());
			colliders.get(0).moveTo(getMidpoint()[0] , getMidpoint()[1]);
			
		} if(isSource) instances.forEachVal(tile -> tile.togglePlatform());
		
	}
	
	boolean colliderAdditionalData() {
		
		return hasCollider && (colliders.get(0).isTriangle() || colliders.get(0).isPlatform());
		
	}
	
	public void modColliderWidth(float mod) {
	
		if(hasCollider) colliders.get(0).modWidth(mod);
		if(isSource) instances.forEachVal(x -> x.modColliderWidth(mod));
		
	}
	
	public void modColliderHeight(float mod) {
		
		if(hasCollider) colliders.get(0).modHeight(mod);
		if(isSource) instances.forEachVal(x -> x.modColliderHeight(mod));
		
	}
	
	public void modColliderOffset(float x , float y) {
		
		if(hasCollider) {
			
			offset[0] += x;
			offset[1] += y;
			
			instances.forEachVal(instance -> instance.moveCollider(x , y));
			
		}
				
	}
	
	public void moveCollider(float x , float y) {
	
		if(hasCollider) colliders.get(0).translate(x, y);
		if(isSource) instances.forEachVal(tile -> tile.moveCollider(x, y));
		
	}

	public float colliderWidth() {
		
		return colliders.get(0).getWidth();
		
	}

	public float colliderHeight() {
		
		return colliders.get(0).getHeight();
		
	}

	public void colliderWidth(float width) {
		
		colliders.get(0).setWidth(width);
		if(isSource) instances.forEachVal(tile -> tile.colliderWidth(width));
		
	}

	public void colliderHeight(float height) {
		
		colliders.get(0).setHeight(height);
		if(isSource) instances.forEachVal(tile -> tile.colliderHeight(height));
		
	}
	
	public void removeInstances() {
		
		if(isSource) instances.clear();
		
	}
	
	public float[] colliderMidpoint() {
	
		return colliders.get(0).getMidpoint();
		
	}	
	
	public void forEachInstance(Consumer<Tiles> function) {
		
		instances.forEachVal(function);		
		
	}
	
	public void markForRemoval() {
		
		markedForRemoval = true;
		
	}
	
	public void removeMarked() {

		cdNode<Tiles> iter = instances.get(0);
		if(iter == null) return;
		
		for(int i = 0 ; i < instances.size() ; i ++) {
			
			if(iter.val.markedForRemoval) iter = instances.safeRemove(iter);
			else iter = iter.next;
			
		}
		
	}

	public boolean isColliderTriangle() {
		
		return hasCollider && colliders.get(0).isTriangle();
		
	}
	
	/**
	 * Used for copying a source tile in the editor.
	 * 
	 */
	public Tiles copy(Scene owner) {
		
		float[] tileData = tileData();
		Tiles newTile = new Tiles(tileData , texture , name + " copy" , -1);
		newTile.isSource(isSource);
		
		newTile.hasCollider(owner , this.hasCollider);
		newTile.removeColor(removedColor.x , removedColor.y , removedColor.z);
		
		if(newTile.hasCollider()) {
			
			if(this.isColliderLowerLeftTriangle()) newTile.toggleLowerLeftTriangle();
			else if (this.isColliderLowerRightTriangle()) newTile.toggleLowerRightTriangle();
			else if (this.isColliderUpperLeftTriangle()) newTile.toggleUpperLeftTriangle();
			else if (this.isColliderUpperRightTriangle()) newTile.toggleUpperRightTriangle();
						
			newTile.colliderWidth(this.colliderWidth());
			newTile.colliderHeight(this.colliderHeight());
			
			float[] thisMid = this.getMidpoint();
			float[] thisColliderMid = this.colliderMidpoint();
			
			float xOffset = thisMid[0] - thisColliderMid[0];
			float yOffset = thisColliderMid[1] - thisMid[1];
						
			newTile.moveCollider(xOffset , 2 * yOffset);
			
		}
		
		if(animation != null) newTile.animation = animation.copy();
		return newTile;
		
	}

	public void filterColor(float[] filter) {
		
		this.filter.x = filter[0];
		this.filter.y = filter[1];
		this.filter.z = filter[1];
		
		if(isSource) instances.forEachVal(x -> x.filterColor(filter));		
		
	}
	
	public void reflectXWise() {
		
		float[] sprite = tileData();
		
		//left u goes to right u and vise versa
		
    	vertexData[16] = sprite[1];
    	vertexData[34] = sprite[1];
    	vertexData[25] = sprite[0];
    	vertexData[7] = sprite[0];
	
    	if(hasCollider) {
    		
    		Colliders coll = colliders.get(0);
    		Tuple4<Boolean , Boolean , Boolean , Boolean> type = coll.getTriangleType();
    		if(type.getFirst()) toggleUpperLeftTriangle();
    		else if (type.getSecond()) toggleUpperRightTriangle();
    		else if (type.getThird()) toggleLowerLeftTriangle();
    		else if (type.getFourth()) toggleLowerRightTriangle();
    		
    	}
    	
    	if(isSource) instances.forEachVal(x -> x.reflectXWise());
    	
    	
	}
	
	public void reflectYWise() {

		float[] sprite = tileData();
		
		vertexData[8] = sprite[2];
		vertexData[35] = sprite[2];
		vertexData[17] = sprite[3];
		vertexData[26] = sprite[3];
		
		if(hasCollider) {
    		
    		Colliders coll = colliders.get(0);
    		Tuple4<Boolean , Boolean , Boolean , Boolean> type = coll.getTriangleType();
    		if(type.getFirst()) toggleLowerRightTriangle();
    		else if (type.getSecond()) toggleLowerLeftTriangle();
    		else if (type.getThird()) toggleUpperRightTriangle();
    		else if (type.getFourth()) toggleUpperLeftTriangle();
    		
    	}
    	
		if(isSource) instances.forEachVal(x -> x.reflectYWise());
		
	}
	
	public boolean triangleCollider() {
		
		return hasCollider && colliders.get(0).isTriangle();
		
	}
	
	public Tuple4<Boolean , Boolean , Boolean , Boolean> triangleType(){
		
		if(hasCollider) return colliders.get(0).getTriangleType();
		return null;
		
	}
	
	public float[] getColliderDimensions() {
		
		if(hasCollider) {
			
			Colliders coll = colliders.get(0);
			return new float[] {coll.getWidth() , coll.getHeight()};
			
		} 
		
		return null;
		
	}
	
	public float[] getColliderMidpoint() {
		
		if(hasCollider) return colliders.get(0).getMidpoint();		
		return null;
		
	}

	public float[] getOffset() {
		
		return offset;
		
	}
	
	public void setAnimation(SpriteSets animation) {
		
		this.animation = animation;
		
	}
		
	public SpriteSets getAnimation() {
		
		return animation;
	}
	
	public void animate() {
		
		if(animation != null) swapSprite(animation.swapSprite());
		
	}
	
	public void animateFast() {
		
		if(animation != null) swapSpriteFast(animation.swapSprite());
		
	}
	
	public void clearInstances() {
		
		instances.clear();
		
	}
	
	@Override public void delete() {
		
		throw new UnsupportedOperationException("ERROR: Tile objects are NOT to be deleted.");
		
	}

	@Override public void write(Object... additionalData) {

		throw new UnsupportedOperationException("ERROR: Tile objects are NOT to be written or loaded.");
		
	}

	@Override public void write(BufferedWriter writer, Object... additionalData) {
	
		throw new UnsupportedOperationException("ERROR: Tile objects are NOT to be written or loaded.");
		
	}

	@Override public void load(String filepath) {

		throw new UnsupportedOperationException("ERROR: Tile objects are NOT to be written or loaded.");
		
	}

	@Override public void load(BufferedReader reader) {

		throw new UnsupportedOperationException("ERROR: Tile objects are NOT to be written or loaded.");
		
	}
	
}