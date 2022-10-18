package Core;

import static CSUtil.BigMixin.getFloatArray;

import CSUtil.DataStructures.cdNode;

public class ObjectLists extends AbstractGameObjectLists<Quads> {

    public ObjectLists(int renderOrder){
    	
    	super(renderOrder , CSType.GENERIC);
    	
    }
    
    public ObjectLists(){
    	
    	super(1 , CSType.GENERIC);
    	
    }
    
	public String toString() {
		
		return "Object List" + renderOrder;
		
	}

    private Quads add(float[] vertices , int id , CSType type){

    	Quads newQuad = new Quads(vertices , id , type);

		return newQuad;

    }

    public Quads add() {

    	Quads newQuad = add(getFloatArray() , list.size() , CSType.GENERIC);
    	list.add(newQuad);

    	return newQuad;
    	
    }

    public void delete(Quads deleteThis){
    	
    	if(has(deleteThis)) {
    		
    		cdNode<Quads> iter = list.removeVal(deleteThis);
    		for(int i = deleteThis.getID() ; i < list.size() ; i ++ , iter = iter.next) iter.val.decrementID();
    		
    	}
    	
    }

    public Quads get(int index){

    	return list.getVal(index);

    }

    public Quads selectQuad(float cursorX , float  cursorY) {
    	
    	Quads x;
		for(int i = list.size() -1 ; i >= 0 ; i--) if((x = list.getVal(i)).selectQuad(cursorX, cursorY) != -1) return x;		
		return null;
		
    }
    
    public void translateQuad(int index , float x , float y) {
    	
    	list.getVal(index).translate(x, y);
    	
    }
    
    public void changeQuadColor(int index , int corner , float RVal , float GVal , float BVal) {

		list.getVal(index).quickChangeColor(corner, RVal, GVal, BVal);

    }

    public void texture(int index , String filepath){

    	Renderer.Renderer.loadTexture(list.getVal(index).texture , filepath);

    }

    public void removeColor(int index , float RVal , float GVal , float BVal){

    	list.getVal(index).removeTextureColor(RVal, GVal, BVal);

    }

    public void removeTexture(int index){

    	list.getVal(index).setTexture(null);

    }

    public void swapQuadSprite(int index , float[] sprite){

    	list.getVal(index).swapSprite(sprite);

    }

    public void changeOpacity(int index , float opacity){

    	list.getVal(index).makeTranslucent(opacity);

    }

    public void clear(){

    	list.clear();

    }

    public boolean shouldRender(int index){

    	return list.getVal(index).getShouldRender();
    	
    }

    public float[] getMidpoint(int index){
    	
    	return list.getVal(index).getMidpoint();
    	
    }

    public float getWidth(int index){
    	
    	return list.getVal(index).getWidth();

    }
    
    public float getHeight(int index){
    	
    	return list.getVal(index).getHeight();
    	
    }
    
    /**
     * Gets {@code index}'s UV coordinates. This Array is laid out in this order; LeftU , RightU , TopV , BottomV. I
     * 
     */
    public float[] getUVs(int index) {
		
		float[] data = list.getVal(index).getData();
		return new float[] {data[16] , data[25] , data[17] , data[35]};
		    	
	}

    /**
     * Gets the Quad at {@code index}'s dimensions with respect to the midpoint of the quad. 
     * The values returned by this method are half the object's standard dimensions. 
     * The resulting array is laid out as; widthMid , heightMid,
     * 
     * @return float array representation of index's midpoint dimensions, formated as WidthMid, HeightMid 
     * 
     */
    public float[] getMidpointDimensions(int index) {
    			
    	Quads x = list.getVal(index); 
		return new float[] {x.getWidthMidpoint() , x.getHeightMidpoint()};	
    	
    }
    
    public void setWidthAndHeight(int index , float width , float height){

    	Quads x = list.getVal(index);
		x.setWidth(width);
		x.setHeight(height);

    }

    public void resizeWidth(int index , float width){

    	list.getVal(index).resizeWidth(width);    	

    }

    public void resizeHeight(int index , float height){

    	list.getVal(index).resizeHeight(height);    	

    }

	public float getOpacity(int index) {
		
		return list.getVal(index).getData()[6];

	}

	public boolean snapToPixels(int index){

		return list.getVal(index).isSnapToPixels();

	}

	public void snapToPixels(int index , boolean snapToPixels){

		list.getVal(index).setSnapToPixels(snapToPixels);

	}

	public void toggleSnapToPixels(int index) {

		list.getVal(index).toggleSnapToPixels();

	}

	public void resizeToTexture(int index) {

		Quads active = list.getVal(index); 
		active.fitQuadToTexture();
		
	}
	
	public int size() {
		
		return list.size();
		
	}

	public void setFilterColor(int index , float R , float G , float B) {
		
		list.getVal(index).setFilter(R, G, B);
		
	}
	
	public boolean has(Quads x) {
		
		return list.has(x);
		
	}
	
	public void toFront(int index) {
		
		if(index == list.size() -1) return;
		Object[] quads = super.moveToFront(index);
		int q0ID = ((Quads)quads[0]).getID();
		((Quads)quads[0]).setID(((Quads)quads[1]).getID());
		((Quads)quads[1]).setID(q0ID);
		
	}
	
	public void forward(int index) {
		
		if(index == list.size() - 1) return;
		Object[] quads = super.moveForward(index);
		int q0ID = ((Quads)quads[0]).getID();
		((Quads)quads[0]).setID(((Quads)quads[1]).getID());
		((Quads)quads[1]).setID(q0ID);
		
	}
	
	public void toBack(int index) {
		
		if(index == 0) return;
		Object[] quads = super.moveToBack(index);
		int q0ID = ((Quads)quads[0]).getID();
		((Quads)quads[0]).setID(((Quads)quads[1]).getID());
		((Quads)quads[1]).setID(q0ID);
		
	}
	
	public void backward(int index) {
		
		if(index == 0) return;
		Object[] quads = super.moveBackward(index);
		int q0ID = ((Quads)quads[0]).getID();
		((Quads)quads[0]).setID(((Quads)quads[1]).getID());
		((Quads)quads[1]).setID(q0ID);
		
	}
		
	public void renderOrder(int order) {
		
		super.renderOrder(order);
		
	}
	
}
