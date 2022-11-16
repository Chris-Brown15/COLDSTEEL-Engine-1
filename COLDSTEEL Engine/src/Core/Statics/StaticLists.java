package Core.Statics;

import org.joml.Vector3f;

import CSUtil.DataStructures.CSArray;
import Core.AbstractGameObjectLists;
import Core.CSType;
import Core.Quads;
import Core.Scene;
import Physics.ColliderLists;
import Physics.Colliders;

public class StaticLists extends AbstractGameObjectLists<Statics>{

	public StaticLists(Scene owningScene , int order) {
	
		super(owningScene , order , CSType.STATIC);
	
	}

    private boolean removeAllStaticsColors = true;
    private Vector3f globalFilterColor = new Vector3f();
    
	public Statics newStatic(String name){
		
		Statics newStatic = new Statics(name , list.size());
		list.add(newStatic);
		if(removeAllStaticsColors) newStatic.removeColor(globalFilterColor.x, globalFilterColor.y, globalFilterColor.z);
		return newStatic;
		
	}

	public void add(Statics addThis) {
		
		addThis.setID(list.size());		
		list.add(addThis);
		ColliderLists colliderList = owningScene.colliders();
		addThis.forEach(colliderList::add);
		
	}
	
	public Statics get(int index) {
		
		return list.getVal(index);
		
	}
	
	public Quads selectStatic(float cursorX , float cursorY) {
				
		for(int i = list.size() - 1 ; i >= 0 ; i--) if(list.getVal(i).selectStatic(cursorX, cursorY) != -1) return list.getVal(i);		
		return null;
		
	}

	public void translate(int index , float xSpeed , float ySpeed){

		list.getVal(index).translate(xSpeed, ySpeed);

	}

	public void texture(int index , String filepath){

		list.getVal(index).texture(filepath);

	}
	
	public Statics remove(int index) {
		
		if(has(index)) {
			
			removeColliders(index);
			return list.removeVal(index);
			
		}
		
		return null;
		
	}
	
	public void remove(Statics removeThis) {
		
		ColliderLists colliderList = owningScene.colliders();
		
		removeThis.forEach((collider) -> {
		
			colliderList.delete(collider);
			
		});
		
		list.removeVal(removeThis);		
		
	}

	public void delete(int index) {
		
		list.removeVal(index).delete();		
		
	}
	
	public void delete(Statics deleteThis) {
		
		if(list.has(deleteThis)) {
			
			list.removeVal(deleteThis);
			deleteThis.delete();
			
		}
		
	}
	
	public void removeColor(int index , float RValue , float GValue , float BValue){

		list.getVal(index).removeColor(RValue, GValue, BValue);

	}

	public void setGlobalRemovedColor(float RValue , float GValue , float BValue){

		globalFilterColor.set(RValue , GValue , BValue);

	}

	public void forward(int index){
		
		Object[] quads = super.moveForward(index);
		((Quads)quads[0]).setID(index + 1);
		((Quads)quads[1]).setID(index);
		
	}

	public void toFront(int index){
		
		Object[] quads = super.moveToFront(index);
		((Quads) quads[0]).setID(list.size() - 1);
		((Quads) quads[1]).setID(index);
		
	}

	public void backward(int index){
		
		Object[] quads = super.moveBackward(index);
		((Quads)quads[0]).setID(index - 1);
		((Quads)quads[1]).setID(index);
		
	}

	public void toBack(int index){
		
		Object[] quads = super.moveToBack(index);
		((Quads)quads[0]).setID(0);
		((Quads)quads[1]).setID(index);
		
	}
	
	public void deleteStatic(int index) {
		
		list.getVal(index).delete();
		removeTargetStatic(index);
			
	}
	
	public void clear(){

		clear(0);
		
	}

	private void clear(int index) {
		
		if(list.size() > 0) {
			
			remove(0);
			clear(0);
			
		}
		
	}
	
	public void removeTargetStatic(int ID){ 

		if(list.size() != 0) {

			Statics removed = list.removeVal(ID);
			ColliderLists colliderList = owningScene.colliders();
			removed.forEach((collider) -> colliderList.delete(collider));		
			for(int i = ID ; i <= list.size()-1 ;  i++) list.getVal(i).decrementStaticID();
			
		}

	}

	public void changeColor(int index , int corner , float r , float g , float b) {
		
		list.getVal(index).quickChangeColor(corner , r , g , b);
		
	}

	public boolean shouldRemoveColorFromAllStatics(){

		return removeAllStaticsColors;

	}

	public void toggleRemoveColorFromAllStatics(){

		removeAllStaticsColors = removeAllStaticsColors ? false:true;

	}

	public void toggleParallax(int index){

		list.getVal(index).toggleParallax();

	}

	public boolean hasParallax(int index){

		return list.getVal(index).hasParallax();
		
	}

	public void setParallaxXOffset(int index , float XOffset){

		list.getVal(index).setParallaxX(XOffset);

	}

	public void setParallaxYOffset(int index , float YOffset){

		list.getVal(index).setParallaxY(YOffset);

	}

	public float getParallaxX(int index){

		return list.getVal(index).getViewOffsetX();

	}

	public float getParallaxY(int index){

		return list.getVal(index).getViewOffsetY();

	}

	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Collider Control Methods			|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	public CSArray<Colliders> getStaticColliders(int ID){

		return list.getVal(ID).getColliders();

	}

	public void removeTargetColliders(Statics target){
		
		target.clear();

	}

	public void addCollider(int index){

		Colliders newCollider = list.getVal(index).addCollider();
		owningScene.colliders().add(newCollider);

	}

	public void copyActiveCollider(int index){

		Colliders newCollider = list.getVal(index).newColliderAndCopyActive();
		owningScene.colliders().add(newCollider);

	}

	public void removeColliders(int index){

		list.getVal(index).clear();

	}

	public void removeCollider(int index) {
		
		owningScene.colliders().delete(list.getVal(index).removeActiveCollider());
		
	}
	
	public void toggleFocusColliders(int index){

		list.getVal(index).toggleFocusColliders();		

	}

	public void resizeActiveColliderWidth(int index , float width){

		list.getVal(index).resizeActiveColliderWidth(width);

	}

	public void resizeActiveColliderHeight(int index , float height){

		 list.getVal(index).resizeActiveColliderHeight(height);

	}

	public void setActiveColliderUpperRightTriangle(int index){

		list.getVal(index).setActiveColliderUpperRightTriangle();

	}

	public void setActiveColliderUpperLeftTriangle(int index){

		list.getVal(index).setActiveColliderUpperLeftTriangle();

	}

	public void setActiveColliderLowerRightTriangle(int index){

		list.getVal(index).setActiveColliderLowerRightTriangle();

	}

	public void setActiveColliderLowerLeftTriangle(int index){

		list.getVal(index).setActiveColliderLowerLeftTriangle();

	}

	public boolean isActiveColliderUpperRightTriangle(int index) {
		
		return list.getVal(index).isActiveColliderUpperRightTriangle();
		
	}
	
	public boolean isActiveColliderUpperLeftTriangle(int index) {
		
		return list.getVal(index).isActiveColliderUpperLeftTriangle();		
		
	}
	
	public boolean isActiveColliderLowerRightTriangle(int index) {
		
		return list.getVal(index).isActiveColliderLowerRightTriangle();
				
	}
	
	public boolean isActiveColliderLowerLeftTriangle(int index) {
		
		return list.getVal(index).isActiveColliderLowerLeftTriangle();
		
	}
	
	public boolean isActiveColliderAnyTriangle(int index) {
		
		return list.getVal(index).isActiveColliderTriangle();
		
	}
	
	public void unmakeActiveColliderTriangle(int index){

		list.getVal(index).unmakeActiveColliderTriangle();

	}

	public float getActiveColliderWidth(int index){

		return list.getVal(index).getActiveColliderWidth();

	}

	public float getActiveColliderHeight(int index){

		return list.getVal(index).getActiveColliderHeight();

	}
	
	public void modActiveColliderWidth(int index , float mod) {
		
		list.getVal(index).modColliderWidth(mod);
		
	}
	
	public void modActiveColliderHeight(int index , float mod) {
		
		list.getVal(index).modColliderHeight(mod);
		
	}
	
	public void filter(int index , float r ,float g , float b) {
		
		list.getVal(index).setFilter(r, g, b);
		
	}

	public void save(int index){

		list.getVal(index).write();

	}
	
	public Statics loadStatic(String namePath){

		Statics loadedStatic = new Statics(namePath);
		loadedStatic.setID(list.size());
		loadedStatic.getColliders().forEach(c -> c.setOwnerID(loadedStatic.getID()));
		list.add(loadedStatic);		
		loadedStatic.forEach(owningScene.colliders()::add);
		
		return loadedStatic;
		
	}

	public void setActiveCollider(int staticIndex , int colliderIndex) {
		
		list.getVal(staticIndex).setActiveCollider(colliderIndex);
		
	}
	
	public int size() {
		
		return list.size();
		
	}
	
	public boolean has(Statics x) {
		
		return list.has(x);
		
	}
	
	public boolean has(int index) {
		
		return list.has(index);
		
	}
	
	public void modParallaxX(int index , float mod) {
	
		list.getVal(index).modParallaxX(mod);
		
	}
	
	public void modParallaxY(int index , float mod)	{
		
		list.getVal(index).modParallaxY(mod);
		
	}
	
	/**
	 * Recursively removes the first element in the list until none are left
	 */	
	public void shutDownList() {
		
		if(list.size() != 0) {
			
			list.removeVal(0).clear();
			shutDownList();
			
		}
		
	}
	
}