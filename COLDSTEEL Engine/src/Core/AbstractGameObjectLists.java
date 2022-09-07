package Core;

import static CS.Engine.addGameObjectList;
import static CS.Engine.removeGameObjectList;

import java.util.function.Consumer;
import java.util.function.Predicate;

import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;

public abstract class AbstractGameObjectLists<T extends Quads> {

	protected CSLinked<T> list = new CSLinked<T>();
	protected int renderOrder;
	protected boolean render = true;
	public final CSType TYPE;

	protected AbstractGameObjectLists(int order , CSType type){
		
		renderOrder = order;
		this.TYPE = type;
		addGameObjectList(this);
		
	}

	public void shutDown() {
		
		removeGameObjectList(this);
		
	}
	
	protected void renderOrder(int order) {
		
		this.renderOrder = order;
		
	}

	public int renderOrder() {
		
		return renderOrder;
		
	}
	
	protected void render(boolean render) {
		
		this.render = render;
		
	}
	
	public boolean render() {
		
		return render;
		
	}
	
	protected void toggleRender() {
				
		render = render ? false:true;
		
	}
	
	public void forEach(Consumer<T> function) {
		
		list.forEachVal(function);
		
	}
	
	public void forOnly(Predicate<T> test , Consumer<T> function) {
		
		list.forOnlyVals(test, function);
		
	}
	
	protected Object[] moveForward(int index) {
		
		@SuppressWarnings("unchecked") cdNode<T>[] arr = list.swap(index , index + 1);
		return new Object[] {arr[0].val , arr[1].val};
		
	}
	
	protected Object[] moveToFront(int index) {
		
		@SuppressWarnings("unchecked") cdNode<T>[] arr = list.swap(index , list.size() - 1); 
		return new Object [] {arr[0].val , arr[1].val};
		
	}
	
	protected Object[] moveBackward(int index){
		
		@SuppressWarnings("unchecked") cdNode<T>[] arr = list.swap(index , index - 1); 
		return new Object [] {arr[0].val , arr[1].val};
		
	}

	protected Object[] moveToBack(int index) {
		
		@SuppressWarnings("unchecked") cdNode<T>[] arr = list.swap(index , 0); 
		return new Object [] {arr[0].val , arr[1].val};
		
	}

	protected Object[] moveForward(CSLinked<T> list , int index) {
		
		@SuppressWarnings("unchecked") cdNode<T>[] arr = list.swap(index , index + 1);
		return new Object[] {arr[0].val , arr[1].val};
		
	}
	
	protected Object[] moveToFront(CSLinked<T> list , int index) {
		
		@SuppressWarnings("unchecked") cdNode<T>[] arr = list.swap(index , list.size() - 1); 
		return new Object [] {arr[0].val , arr[1].val};
		
	}
	
	protected Object[] moveBackward(CSLinked<T> list , int index){
		
		@SuppressWarnings("unchecked") cdNode<T>[] arr = list.swap(index , index - 1); 
		return new Object [] {arr[0].val , arr[1].val};
		
	}

	protected Object[] moveToBack(CSLinked<T> list , int index) {
		
		@SuppressWarnings("unchecked") cdNode<T>[] arr = list.swap(index , 0); 
		return new Object [] {arr[0].val , arr[1].val};
		
	}
	
	public int size() {
		
		return list.size();
		
	}
	
	public cdNode<T> iter(){
		
		return list.get(0);
				
	}
		
	@SuppressWarnings("rawtypes") public cdNode[] swap(int index1 , int index2) {
		
		return list.swap(index1, index2);
		
	}
	
}
