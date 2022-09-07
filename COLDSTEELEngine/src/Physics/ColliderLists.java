package Physics;

import static CSUtil.BigMixin.getColliderFloatArray;

import java.util.function.Consumer;

import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple4;
import CSUtil.DataStructures.cdNode;
import Core.AbstractGameObjectLists;
import Core.CSType;

public class ColliderLists extends AbstractGameObjectLists<Colliders>{
	
	public ColliderLists() {

		super(-1 , CSType.COLLIDER);

	}

	private static final CSLinked<Colliders> appended = new CSLinked<Colliders>();  // managed externally and internally
	private static final CSLinked<Colliders> composite = new CSLinked<Colliders>(); // viewed externally
	private static boolean SHOULD_RENDER = true;

	public void newCollider(){

		Colliders newCollider = add();
		list.add(newCollider);
		
		composite.add(newCollider);

	}

	public Colliders get(int index) {
		
		return list.getVal(index);
		
	}
	
	public Colliders add(){
		
		Colliders newCollider = new Colliders(getColliderFloatArray() , list.size());
		list.add(newCollider);
		composite.add(newCollider);
		return newCollider;

	}

	public void add(Colliders addThis) {
		
		list.add(addThis);
		composite.add(addThis);
		
	}
	
	public void add(float[] colliderData) {
		
		Colliders newCollider = new Colliders();
		newCollider.setWidth(colliderData[2]);
		newCollider.setHeight(colliderData[3]);
		
		float state = colliderData[4];
		if(state != 0) {

			if(state == 1) newCollider.makeUpperRightTriangle();
			else if(state == 2) newCollider.makeUpperLeftTriangle();
			else if(state == 3) newCollider.makeLowerRightTriangle();
			else if(state == 4) newCollider.makeLowerLeftTriangle();
			else if(state == 5) newCollider.makePlatform();
			
		}
		
		newCollider.moveTo(colliderData[0] , colliderData[1]);
		newCollider.setID(list.size());
		newCollider.setOwnerID(-1);
		add(newCollider);
		
	}
	
	public static void addAppended(Colliders addThis){

		appended.add(addThis);
		composite.add(addThis);
		
	}

	public static void deleteAppended(Colliders shutDown) {

		appended.removeVal(shutDown);
		composite.removeVal(shutDown);
		
	}

	public CSLinked<Colliders> getAppended(){

		return appended;

	}

	public static boolean shouldRender(){

		return SHOULD_RENDER;

	}

	public static void toggleShouldRender(){

		SHOULD_RENDER = SHOULD_RENDER ? false:true;

	}

	public static void setShouldRender(boolean shouldRender){

		SHOULD_RENDER = shouldRender;

	}

	public void setWidth(int index , float width){

		list.getVal(index).resizeWidth(width);

	}

	public void setHeight(int index , float height){

		list.getVal(index).resizeHeight(height);

	}

	public Colliders selectCollider(float cursorX , float cursorY) {
		
		cdNode<Colliders> iter = list.get(list.size() - 1);
		for(int i = list.size() -1 ; i >= 0 ; i-- , iter = iter.prev) if(iter.val.selectCollider(cursorX , cursorY) != -1) return iter.val;
		return null;
		
	}

	public void translate(int index , float x , float y ){

		list.getVal(index).translate(x, y);

	}

	public void setUpperRightTriangle(int index){

		list.getVal(index).makeUpperRightTriangle();

	}

	public void setActiveColliderUpperLeftTriangle(int index){

		list.getVal(index).makeUpperLeftTriangle();

	}

	public void setActiveColliderLowerRightTriangle(int index){

		list.getVal(index).makeLowerRightTriangle();

	}

	public void setActiveColliderLowerLeftTriangle(int index){

		list.getVal(index).makeLowerLeftTriangle();

	}

	public float getWidth(int index){

		return list.getVal(index).getWidth();
	}

	public float getHeight(int index){

		return list.getVal(index).getHeight();
		
	}

	/**
	 * This method returns a tuple containing booleans notating whether a collider is a triangle. Only one of the four values in the tuple
	 * should be true.
	 *
	 * @return — Tuple4 of booleans where first value reflects upper right triangle, second value is upper left triangle , third value is lower right
	 * triangle, and fourth value is lower left triangle.
	 */

	public Tuple4<Boolean , Boolean , Boolean , Boolean> getTriangleStatus(int index){

		//first value is upper right triangle, second is upper left triangle, third is lower right triangle , fourth is lower left triangle
		Tuple4<Boolean , Boolean , Boolean , Boolean> returnTuple = new Tuple4<>(false , false , false , false);
		Colliders activeCollider = list.getVal(index);

		if(activeCollider.getColliderIsUpperRightTriangle()) returnTuple.setFirst(true);
		if(activeCollider.getColliderIsUpperLeftTriangle()) returnTuple.setSecond(true);
		if(activeCollider.getColliderIsLowerRightTriangle()) returnTuple.setThird(true);
		if(activeCollider.getColliderIsLowerLeftTriangle()) returnTuple.setFourth(true);

		return returnTuple;

	}

	public boolean has(Colliders x) {
		
		return list.has(x);
		
	}
	
	public void delete(Colliders deleteThis) {
		
		if(has(deleteThis)) {
    		
    		cdNode<Colliders> iter = list.removeVal(deleteThis);
    		composite.removeVal(deleteThis);
    		for(int i = deleteThis.getID() ; i < list.size() ; i ++ , iter = iter.next) iter.val.decrementID();
    		
    	}
		
	}
	
	public void deleteCollider(int index){
		
		list.removeVal(index);		
		composite.removeVal(index);
		cdNode<Colliders> iter = list.get(index); 
		for(int i = index ; i < list.size() ; i ++ , iter = iter.next) {
			iter.val.decrementID();
		}

	}

	public void undoTriangle(int index){

		list.getVal(index).resetState();			

	}

	public void copyCollider(int index){

		Colliders activeCollider = list.getVal(index);
		Colliders newCollider = new Colliders(getColliderFloatArray() , list.size());

		if(activeCollider.isPlatform) newCollider.makePlatform();
		else if(activeCollider.isUpperRightTriangle) newCollider.makeUpperRightTriangle();
		else if (activeCollider.isUpperLeftTriangle) newCollider.makeUpperLeftTriangle();
		else if (activeCollider.isLowerRightTriangle) newCollider.makeLowerRightTriangle();
		else if (activeCollider.isLowerLeftTriangle) newCollider.makeLowerLeftTriangle();		
		
		newCollider.setWidth(activeCollider.getWidth());
		newCollider.setHeight(activeCollider.getHeight());

		list.add(newCollider);
		composite.add(newCollider);
	
	}

	public CSLinked<Colliders> getAll(){
		
		return composite;
		
	}
		
	public static final CSLinked<Colliders> getComposite(){
		
		return composite;
		
	}
	
	public int getListSizeSum() {
		
		return list.size() + appended.size();
		
	}

	public CSLinked<Colliders> getList() {

		return this.list;

	}

	public void modWidth(int index , float mod) {
	
		list.getVal(index).modWidth(mod);
		
	}
	
	public void modActiveHeight(int index , float mod) {
		
		list.getVal(index).modHeight(mod);
		
	}
	
	public float [] getData(int index) {
		
		return list.getVal(index).getData();
		
	}
	
	public float[] getMidpoint(int index) {
		
		return list.getVal(index).getMidpoint();
		
	}
	
	public static void forAll(Consumer<Colliders> function) {
		
		composite.forEachVal(function);
		
	}
	
	public void clear() {
				
		if(list.size() > 0) {
			
			Colliders x = list.removeVal(0);
			composite.removeVal(x);			
			appended.removeVal(x);
			clear();
			
		}
		
	}
	
	public static final void clearAppended() {
		
		appended.clear();
		
	}
	
	public static final void clearComposite() {
		
		composite.clear();
		
	}
	
}