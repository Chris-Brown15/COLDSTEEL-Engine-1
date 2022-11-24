package Editor;

import static CSUtil.BigMixin.getStaticFloatArray;
import static CSUtil.BigMixin.changeColorTo;
import static CSUtil.BigMixin.setArrayWidth;
import static CSUtil.BigMixin.setArrayHeight;
import static CSUtil.BigMixin.getArrayWidth;
import static CSUtil.BigMixin.getArrayHeight;
import static CSUtil.BigMixin.translateArray;

import Core.Quads;

import static CSUtil.BigMixin.dragResize;

public class SelectionArea extends Quads {
		
	public SelectionArea() {
		
		super(0);
		
		vertexData = getStaticFloatArray();
		vertexData = changeColorTo(vertexData, 1, 1, 1);
		makeTranslucent(0.1f);
		vertexData = setArrayWidth(vertexData, 0f);
		vertexData = setArrayHeight(vertexData, 0f);	
		
	}
		
	public void setDimensions(float deltaX , float deltaY) {

		//top left vertex is where we want it, use it as a por
		//width
		vertexData[18] = (vertexData[9] + deltaX);
		vertexData[0] = (vertexData[9] + deltaX);
		//height;
		vertexData[28] = (vertexData[10] + deltaY);
		vertexData[1] = (vertexData[10] + deltaY);
				
	}
	
	public float[] getDimensions() {
		
		return new float[] {getArrayWidth(vertexData) , getArrayHeight(vertexData)};
		
	}
	
	public void moveRightFace(float width) {
		
		vertexData[0] += width;
		vertexData[18] += width;
		
	}
	
	public void moveLeftFace(float amount) {
		
		vertexData[27] -= amount;
		vertexData[9] -= amount;
		
	}
	
	public void moveUpperFace(float amount) {
		
		vertexData[10] += amount;
		vertexData[19] += amount;
				
	}
	
	public void moveLowerFace(float amount) {
		
		vertexData[28] -= amount;
		vertexData[1] -= amount;
		
	}
	
	public void transform(float xAmount , float yAmount) {
	
		vertexData = translateArray(vertexData , xAmount , yAmount);
		
	}
	
	/**
	 * Checks that the cursor is nearer to the bottom right vertex of this Selection Area, and moves its appropriate vertices to the positino
	 * of the cursor.
	 * @param cursorX — position of the x coordinate of the cursor, should be in world coordinates 
	 * @param cursorY — position of the y coordinate of the cursor, should be in world coordinates
	 */
	public void resize(float cursorX , float cursorY) {
		
		vertexData = dragResize(vertexData , cursorX , cursorY);
				
	}
	
	public void makeTranslucent(float opacity) {
		
		vertexData = CSUtil.BigMixin.makeTranslucent(vertexData, opacity);
		
	}
	
}
