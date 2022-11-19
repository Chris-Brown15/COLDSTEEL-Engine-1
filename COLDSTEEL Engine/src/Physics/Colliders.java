package Physics;

import static CSUtil.BigMixin.getSCToWCForX;
import static CSUtil.BigMixin.getSCToWCForY;
import static CSUtil.QuadIndices.BLX;
import static CSUtil.QuadIndices.BRX;
import static CSUtil.QuadIndices.TLX;
import static CSUtil.QuadIndices.TRX;

import CSUtil.DataStructures.Tuple4;
import Core.CSType;
import Core.Quads;
import Renderer.Camera;

public class Colliders extends Quads{

    protected boolean isUpperRightTriangle = false;
    protected boolean isUpperLeftTriangle = false;
    protected boolean isLowerRightTriangle = false;
    protected boolean isLowerLeftTriangle = false;
    protected boolean isPlatform = false;
    private int ownerID;
    
	public Colliders(float[] vertexdata, int ID) {
		
		super(vertexdata , ID , CSType.COLLIDER);
		makeTranslucent(0.5f);
		ownerID = -1;
		
	}
	
	public Colliders() {
		
		super(CSUtil.BigMixin.getColliderFloatArray() , -1 , CSType.COLLIDER);
		makeTranslucent(0.5f);
		
	}

	int ID() {
		
		return ID;
		
	}
	
	public float[] getData() {
		
		return vertexData;
		
	}
	
	int selectCollider(double mouseX , double mouseY , int width , int height , int activeObject , Camera camera){

    	if(ownerID == -1){

    		double cursorX = getSCToWCForX(mouseX , width , height , camera , 1 , 1);
    		double cursorY = getSCToWCForY(mouseY , width , height , camera , 1 , 1);

    		if(isUpperRightTriangle){

    			if (((cursorX >= vertexData[27]) && (vertexData[0] >= cursorX)) &&
    				((cursorY >= vertexData[1]) && (vertexData[10] >= cursorY))) return ID;
    			
    		} else if (isLowerLeftTriangle){

    			if((cursorX >= vertexData[9] && vertexData[18] >= cursorX) &&
    			   (cursorY >= vertexData[1] && vertexData[19] >= cursorY)) return ID;   			
    		}

    		else {

    			if (((cursorX >= vertexData[27]) && (vertexData[18] >= cursorX)) &&
    				((cursorY >= vertexData[1]) && (vertexData[10] >= cursorY))) return ID;    			

    		}

    		return activeObject;

    	} else {

    		return activeObject;

    	}

    }
	
	int selectCollider(float cursorX, float cursorY) {
		
		if(ownerID == -1) {
			
    		if(isUpperRightTriangle){

    			if (((cursorX >= vertexData[27]) && (vertexData[0] >= cursorX)) &&
    				((cursorY >= vertexData[1]) && (vertexData[10] >= cursorY))) return ID;
    			
    		} else if (isLowerLeftTriangle){

    			if((cursorX >= vertexData[9] && vertexData[18] >= cursorX) &&
    			   (cursorY >= vertexData[1] && vertexData[19] >= cursorY)) return ID;   			
    		}

    		else {

    			if (((cursorX >= vertexData[27]) && (vertexData[18] >= cursorX)) &&
    				((cursorY >= vertexData[1]) && (vertexData[10] >= cursorY))) return ID;    			

    		}

    		return -1;

    	} else return -1;			
		
	}
	
    public int selectOwnedCollider(double xCoord , double yCoord , int width , int  height , int activeQuad , Camera camera) {

    	if(ownerID != -1){

    		double cursorX = getSCToWCForX(xCoord , width , height , camera , 1 , 1);
    		double cursorY = getSCToWCForY(yCoord , width , height , camera , 1 , 1);

    		if(isUpperRightTriangle){

    			if (((cursorX >= vertexData[27]) && (vertexData[0] >= cursorX)) &&
    				((cursorY >= vertexData[1]) && (vertexData[10] >= cursorY))) return ID;    			

    		} else if (isLowerLeftTriangle){

    			if((cursorX >= vertexData[9] && vertexData[18] >= cursorX) &&
    			   (cursorY >= vertexData[1] && vertexData[19] >= cursorY)) return ID;   			

    		} else {

    			if (((cursorX >= vertexData[27]) && (vertexData[18] >= cursorX)) &&
    				((cursorY >= vertexData[1]) && (vertexData[10] >= cursorY))) return ID;    			

    		}

    		return activeQuad;

    	} else {

    		return activeQuad;

    	}

    }
    
    public int selectOwnedCollider(float cursorX , float cursorY) {
    	
    	if(ownerID != -1) {
    		
      		if(isUpperRightTriangle){

    			if (((cursorX >= vertexData[27]) && (vertexData[0] >= cursorX)) &&
    				((cursorY >= vertexData[1]) && (vertexData[10] >= cursorY))) return ID;    			

    		} else if (isLowerLeftTriangle){

    			if((cursorX >= vertexData[9] && vertexData[18] >= cursorX) &&
    			   (cursorY >= vertexData[1] && vertexData[19] >= cursorY)) return ID;   			

    		} else {

    			if (((cursorX >= vertexData[27]) && (vertexData[18] >= cursorX)) &&
    				((cursorY >= vertexData[1]) && (vertexData[10] >= cursorY))) return ID;    			

    		}

    		return -1;
    		
    	} else return -1;
    	
    }

	float[] getPosition(){

		/*
		 * first two are top left x and y
		 * second two are top right x and y
		 * third two are bottom left x and y
		 * fourth two are bottom right x and y
		 *
		 */
		float[] position = {vertexData[9] , vertexData[10],
							vertexData[18] , vertexData[19],
							vertexData[27] , vertexData[28],
							vertexData[0] , vertexData[1]};

		return position;

	}

	boolean getColliderIsUpperRightTriangle(){

		return isUpperRightTriangle;

	}

	boolean getColliderIsUpperLeftTriangle(){

		return isUpperLeftTriangle;

	}

	boolean getColliderIsLowerRightTriangle(){

		return isLowerRightTriangle;

	}

	boolean getColliderIsLowerLeftTriangle(){

		return isLowerLeftTriangle;

	}
	
    /*
     *
     //x coords
    	vertexData[0] 	//bottom right
    	vertexData[9]	//top left
    	vertexData[18]  //top right
    	vertexData[27]  //bottom left
    	//y coords
    	vertexData[1]  	//bottom right
    	vertexData[10]	//top left
    	vertexData[19] 	// top right
    	vertexData[28] 	//bottom left
     *
     */

	public float getWidth() {
		
		if(!isTriangle()) return vertexData[0] - vertexData[27];
		else if (isUpperRightTriangle || isUpperLeftTriangle) return vertexData[0] - vertexData[27];
		else if (isLowerRightTriangle || isLowerLeftTriangle) return vertexData[18] - vertexData[9];
		else return 0f;
		
	}
	
	public float getHeight(){

		if(isUpperRightTriangle || isLowerRightTriangle) return (vertexData[10] - vertexData[28]);
		else return (vertexData[19] - vertexData[1]);

	}
	
	public float[] getMidpoint() {
		
		if(!isTriangle() || isUpperRightTriangle || isUpperLeftTriangle || isLowerRightTriangle) 
			return new float[] {vertexData[27] + (getWidth() / 2f) , vertexData[28] + (getHeight() / 2f)};
		else return new float[] {vertexData[9] + (getWidth() / 2f) , vertexData[10] - (getHeight() / 2f)};
		
	}
			
    public void makeUpperRightTriangle(){

    	if(isUpperLeftTriangle) vertexData[9] = vertexData[27];
    	if(isLowerRightTriangle) vertexData[0] = vertexData[18];
    	if(isLowerLeftTriangle) vertexData[27] = vertexData[9];
    	
    	vertexData[18] = vertexData[9];
    	
    	isUpperRightTriangle = true;
    	isUpperLeftTriangle = false;
    	isLowerRightTriangle = false;
    	isLowerLeftTriangle = false;
    	isPlatform = false;

    }

    public void makeUpperLeftTriangle(){
    	
    	if(isUpperRightTriangle) vertexData[18] = vertexData[0];
    	if(isLowerRightTriangle) vertexData[0] = vertexData[18];
    	if(isLowerLeftTriangle) vertexData[27] = vertexData[9];
    	
    	vertexData[9] = vertexData[18];
    	
    	isUpperLeftTriangle = true;
    	isUpperRightTriangle = false;
    	isLowerRightTriangle = false;
    	isLowerLeftTriangle = false;
    	isPlatform = false;
    	
    }

    public void makeLowerRightTriangle(){

    	if(isUpperRightTriangle) vertexData[18] = vertexData[0];
    	if(isUpperLeftTriangle) vertexData[9] = vertexData[27];
    	if(isLowerLeftTriangle) vertexData[27] = vertexData[9];
    	
    	vertexData[0] = vertexData[27];
    	
    	isLowerRightTriangle = true;
    	isLowerLeftTriangle = false;
    	isUpperRightTriangle = false;
    	isUpperLeftTriangle = false; 
    	isPlatform = false;

    }

    public void makeLowerLeftTriangle(){

    	if(isUpperRightTriangle) vertexData[18] = vertexData[0];
    	if(isUpperLeftTriangle) vertexData[9] = vertexData[27];
    	if(isLowerRightTriangle) vertexData[0] = vertexData[18];
    	
    	vertexData[27] = vertexData[0];
    	
    	isLowerLeftTriangle = true;
    	isLowerRightTriangle = false;
    	isUpperRightTriangle = false;
    	isUpperLeftTriangle = false;  	
    	isPlatform = false;

    }

    /**
     * Gets and returns the status of this object's being a triangle.
     *
     * @return — first is true if the object is an upper right triangle, second is true if upper left triangle, third is true if lower
     * right triangle, and fourth is true if lower left triangle.
     */
    public Tuple4<Boolean , Boolean , Boolean , Boolean> getTriangleType(){

    	Tuple4<Boolean , Boolean , Boolean , Boolean> returnTuple = new Tuple4<>
    		(isUpperRightTriangle , isUpperLeftTriangle , isLowerRightTriangle , isLowerLeftTriangle);
    	
    	return returnTuple;

    }

	public boolean isTriangle(){

		return isUpperRightTriangle || isUpperLeftTriangle || isLowerRightTriangle || isLowerLeftTriangle;

    }

    public boolean isUpperRightTriangle(){

    	return isUpperRightTriangle;

    }

    public boolean isUpperLeftTriangle(){

    	return isUpperLeftTriangle;

    }

    public boolean isLowerRightTriangle(){

    	return isLowerRightTriangle;

    }

    public boolean isLowerLeftTriangle(){

    	return isLowerLeftTriangle;

    }
    
    public void resetState(){

    	if(isUpperRightTriangle) vertexData[18] = vertexData[0];
    	if(isUpperLeftTriangle) vertexData[9] = vertexData[27];
    	if(isLowerRightTriangle) vertexData[0] = vertexData[18];
    	if(isLowerLeftTriangle) vertexData[27] = vertexData[9];

    	isUpperRightTriangle = false;
    	isUpperLeftTriangle = false;
    	isLowerRightTriangle = false;
    	isLowerLeftTriangle = false;
    	isPlatform = false;
    	
    }
    
	public void setID(int ID){

		this.ID = ID;

	}

	public void resizeWidth(float width) {

    	float midpoint = (vertexData[0] - vertexData[27]) / 2;
    	float dimensionAdditive = width - (vertexData[0] - vertexData[27]);
    	float resizeFactor = midpoint + dimensionAdditive;

    	if(!isUpperRightTriangle && !isUpperLeftTriangle && !isLowerRightTriangle && !isLowerLeftTriangle){

    		vertexData[9] -= resizeFactor;
    		vertexData[27] -= resizeFactor;
    		vertexData[0] += resizeFactor;
    		vertexData[18] += resizeFactor;

    	} else if (isUpperRightTriangle) {

    		vertexData[0] += resizeFactor;

    	} else if (isUpperLeftTriangle) {

    		vertexData[27] -= resizeFactor;

    	} else if (isLowerLeftTriangle) {

    		midpoint = (vertexData[18] - vertexData[9]) / 2;
    		dimensionAdditive = width - (vertexData[18] - vertexData[9]);
    		resizeFactor = midpoint + dimensionAdditive;

    		vertexData[9] -= resizeFactor;

    	} else if (isLowerRightTriangle){

    		midpoint = (vertexData[18] - vertexData[9]) / 2;
    		dimensionAdditive = width - (vertexData[18] - vertexData[9]);
    		resizeFactor = midpoint + dimensionAdditive;

    		vertexData[18] +=resizeFactor;

    	}
		
	}
	
	public void resizeHeight(float height) {

    	if(isUpperRightTriangle || isUpperLeftTriangle){

    		int heightDifference = (int) ((height * 2) - (vertexData[10] - vertexData[28]));
       		vertexData[10] += heightDifference;
    		vertexData[19] += heightDifference;

    	} else if(isLowerRightTriangle || isLowerLeftTriangle){

    		int heightDifference = (int) ((height * 2) - (vertexData[10] - vertexData[28]));
       		vertexData[1] -= heightDifference;
    		vertexData[28] -= heightDifference;

    	} else {

    		float midpoint = (vertexData[10] - vertexData[28]) / 2;
    		float dimensionAdditive = height - (vertexData[10] - vertexData[28]);
    		float resizeFactor = midpoint + dimensionAdditive;
    		resizeFactor = Math.round(resizeFactor);
    		vertexData[10] += resizeFactor;
    		vertexData[19] += resizeFactor;

    	}		
		
	}

	public void setOwnerID(int ownerID) {
		
		this.ownerID = ownerID;
		
	}
	
	public int getOwnerID() {
		
		return ownerID;
		
	}

	public void modWidth(float mod) {
		
		if(isUpperRightTriangle) vertexData[BRX] += mod;
		else if (isUpperLeftTriangle) vertexData[BLX] -= mod;
		else if (isLowerRightTriangle) vertexData[TRX] += mod;
		else if (isLowerLeftTriangle) vertexData[TLX] -= mod; 
		else {
			
			vertexData[0] += (mod / 2f);
			vertexData[18] += (mod/2f);
			vertexData[27] -= (mod / 2f);
			vertexData[9] -= (mod / 2f);			
			
		}
		
	}
	
	public void modHeight(float mod) {
		
		if(isLowerLeftTriangle || isLowerRightTriangle) {

			vertexData[1] -= (mod / 2f);			
			vertexData[28] -= (mod / 2f);
						
		} else {

			vertexData[19] += (mod / 2f);
			vertexData[10] += (mod / 2f);
			
		}
		
	}
		
	public void transformAndSnap(float x , float y) {
		
		this.translate(x , y);		
		roundVertices();
		
	}
	
	public void makePlatform() {
		
		resetState();
		isPlatform = true;		
		
	}
	
	public boolean isPlatform() {
		
		return isPlatform;
		
	}
	
	public void setWidth(float width) {
		
		Tuple4<Boolean , Boolean , Boolean , Boolean> triType = getTriangleType();
		//stretch out bottom right vertex	
		if(triType.getFirst()) vertexData[0] = vertexData[27] + width;
		//stretch out bottom left vertex
		else if (triType.getSecond()) vertexData[27] = vertexData[0] - width;
		//stretch out top right vertex
		else if (triType.getThird()) vertexData[18] = vertexData[9] + width;
		//stretch out top left Vertex
		else if(triType.getFourth()) vertexData[9] = vertexData[18] - width;
		else super.setWidth(width);		
		
	}
	
	public String toString() {
		
		String toStr = "Collider " + ID;
		if(ownerID != -1) toStr += " owner: " + ownerID;
		return toStr;
		
	}
	
	/**
	 * Deep copies this and returns the result. The resulting object is a unique reference, but it's fields are equal in value to this.
	 * 
	 * @return a deep copy of this
	 */
	public Colliders copy() {
	
		float[] vertices = new float[vertexData.length];
		System.arraycopy(vertexData, 0, vertices, 0 , vertexData.length);
		
		Colliders copy = new Colliders(vertices , this.ID);
		if(this.isUpperLeftTriangle) copy.makeUpperLeftTriangle();
		else if(this.isUpperRightTriangle) copy.makeUpperRightTriangle();
		else if(this.isLowerLeftTriangle) copy.makeLowerLeftTriangle();
		else if(this.isLowerRightTriangle) copy.makeLowerRightTriangle();
		copy.ownerID = this.ownerID;
		return copy;		
		
	}
	
}
