package Editor;

import static CSUtil.BigMixin.getStaticFloatArray;
import static CSUtil.BigMixin.changeColorTo;
import static CSUtil.BigMixin.setArrayWidth;
import static CSUtil.BigMixin.setArrayHeight;
import static CSUtil.BigMixin.getArrayWidth;
import static CSUtil.BigMixin.getArrayHeight;
import static CSUtil.BigMixin.translateArray;
import static CSUtil.BigMixin.dragResize;

public class SelectionArea {
	
	public float[] vertices;
	
	public SelectionArea() {
		
		vertices = getStaticFloatArray();
		vertices = changeColorTo(vertices, 1, 1, 1);
		makeTranslucent(0.1f);
		vertices = setArrayWidth(vertices, 0f);
		vertices = setArrayHeight(vertices, 0f);	
		
	}
	
	public SelectionArea(float deltaX , float deltaY){
		
		vertices = getStaticFloatArray();
		vertices = changeColorTo(vertices, 1, 1, 1);
		makeTranslucent(0.1f);
		vertices = setArrayWidth(vertices, deltaX);
		vertices = setArrayHeight(vertices, deltaY);
		
	}
	
	public void setDimensions(float deltaX , float deltaY) {

		//top left vertex is where we want it, use it as a por
		//width
		vertices[18] = (vertices[9] + deltaX);
		vertices[0] = (vertices[9] + deltaX);
		//height;
		vertices[28] = (vertices[10] + deltaY);
		vertices[1] = (vertices[10] + deltaY);
				
	}
	
	public void moveTo(float topLeftX , float topLeftY) {
	
		float verWidth = getArrayWidth(vertices);
		float verHeight = getArrayHeight(vertices);
		
		vertices[9] = topLeftX;
		vertices[10] = topLeftY;
		vertices[27] = vertices[9];
		vertices[28] = vertices[10] - verHeight;
		vertices[18] = vertices[9] + verWidth;
		vertices[19] = vertices[10];
		vertices[0] = vertices[18];
		vertices[1] = vertices[28];
				
	}
	
	public float[] getDimensions() {
		
		return new float[] {getArrayWidth(vertices) , getArrayHeight(vertices)};
		
	}
	
	public void moveRightFace(float width) {
		
		vertices[0] += width;
		vertices[18] += width;
		
	}
	
	public void moveLeftFace(float amount) {
		
		vertices[27] -= amount;
		vertices[9] -= amount;
		
	}
	
	public void moveUpperFace(float amount) {
		
		vertices[10] += amount;
		vertices[19] += amount;
				
	}
	
	public void moveLowerFace(float amount) {
		
		vertices[28] -= amount;
		vertices[1] -= amount;
		
	}
	
	public void transform(float xAmount , float yAmount) {
	
		vertices = translateArray(vertices , xAmount , yAmount);
		
	}
	
	public void roundVertices() {
		
		vertices[0] = Math.round(vertices[0]);
		vertices[1] = Math.round(vertices[1]);
		
		vertices[9] = Math.round(vertices[9]);
		vertices[10] = Math.round(vertices[10]);
		
		vertices[18] = Math.round(vertices[18]);
		vertices[19] = Math.round(vertices[19]);
		
		vertices[27] = Math.round(vertices[27]);
		vertices[28] = Math.round(vertices[28]);
		
	}
	
	/**
	 * Checks that the cursor is nearer to the bottom right vertex of this Selection Area, and moves its appropriate vertices to the positino
	 * of the cursor.
	 * @param cursorX — position of the x coordinate of the cursor, should be in world coordinates 
	 * @param cursorY — position of the y coordinate of the cursor, should be in world coordinates
	 */
	public void resize(float cursorX , float cursorY) {
		
		vertices = dragResize(vertices , cursorX , cursorY);
				
	}
	
	public void makeTranslucent(float opacity) {
		
		vertices = CSUtil.BigMixin.makeTranslucent(vertices, opacity);
		
	}
	
}
