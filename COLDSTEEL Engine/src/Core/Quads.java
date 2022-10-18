package Core;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import Renderer.Textures;
import Renderer.Textures.ImageInfo;

/**
 * Root Class of all renderable objects. This contains vertex data, a texture, a vector 3 for removing a color from the texture,
 * a vector 3 for applying a filter to the texture, data for applying a parallax effect, methods for animating, and some others things.
 * 
 * 
 * @author Chris Brown
 *
 */
public class Quads{

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

    //U, V
    //7, 8 		//bottom right
    //16, 17	//top left
    //25, 26	//top right
    //34, 35	//bottom left
	
	protected int ID = 0;
	protected float[] vertexData;
	public final CSType type;

	protected Textures texture = new Textures();
    protected Vector3f removedColor = new Vector3f();
    protected Vector3f filter = new Vector3f();
    protected Matrix4f rotation = new Matrix4f().identity();
    protected Matrix4f translation = new Matrix4f().identity();
	protected boolean shouldRender = true;
	protected boolean snapToPixels = true;	
	
	/**
	 * 
	 * @param vertices — Quad Vertices for rendering
	 * @param ID — transient ID which quads use to know what position they are in the list that manages them
	 * @param type — CSType of this object
	 */
    public Quads(float [] vertices , int ID , CSType type) {
    	
    	vertexData = vertices;
		this.ID = ID;
		this.type = type;
		//set the filter colors to 2 will result in nothing happening
		removedColor.x = 2.0f;
		removedColor.y = 2.0f;
		removedColor.z = 2.0f;

		filter.x = 0f;
		filter.y = 0f;
		filter.z = 0f;		
		
		snapQuadToPixels();

	}
        
    public Quads(int ID) {
    	
    	vertexData = CSUtil.BigMixin.getFloatArray();
    	type = CSType.GENERIC;
    	this.ID = ID;
    	
    }
    
	public String toString() {
		
		float[] mid = getMidpoint();
		return "Quad " + ID + ", midpoint at x: " + mid[0] + ", y: "+ mid[1];
		
	}
	
	/**
	 * Adds to the object's vertices uniformly the specified amounts, moving it.
	 * 
	 * @param xSpeed — amount to move the object about the x axis
	 * @param ySpeed — amount to move the object about the y axis
	 */
	public void translate(float xSpeed , float ySpeed) {
		
		//x coords
		vertexData[0] += xSpeed; 	//bottom right
		vertexData[9] += xSpeed; 	//top left
		vertexData[18] += xSpeed; 	//top right
		vertexData[27] += xSpeed; 	//bottom left
		//y coords
		vertexData[1] += ySpeed; 	//bottom right
		vertexData[10] += ySpeed; 	//top left
		vertexData[19] += ySpeed; 	// top right
		vertexData[28] += ySpeed; 	//bottom left
	
		if(snapToPixels && !(type == CSType.ENTITY || type == CSType.STATIC_ENTITY || type == CSType.JOINT)) snapQuadToPixels();
		
	}
		
	/**
	 * Decrements this quad's ID. This is needed when selecting things in the editor. 
	 * 
	 * @return this object's ID, decremented 
	 */
	public int decrementID(){

		return --ID;

	}

	/**
	 * Changes one of the caller's vertices color.
	 * 
	 * @param corner — 0 for bottom left, 1 for bottom right, 2 for top left , 3 for top right
	 * @param RVal — red value
	 * @param GVal — green value
	 * @param BVal — blue value
	 */
	public void quickChangeColor(int corner , float RVal , float GVal , float BVal) {

		switch(corner){

			case 0:

				vertexData[30] = RVal;
				vertexData[31] = GVal;
				vertexData[32] = BVal;
				break;

			case 1:

				vertexData[3] = RVal;
				vertexData[4] = GVal;
				vertexData[5] = BVal;
				break;

			case 2:

				vertexData[12] = RVal;
				vertexData[13] = GVal;
				vertexData[14] = BVal;
				break;

			case 3:

				vertexData[21] = RVal;
				vertexData[22] = GVal;
				vertexData[23] = BVal;
				break;

		}

	}

	public void removeTextureColor(float RVal , float GVal , float BVal){
		// these colors will already be normalized when this method receives them,
		// so the shader can propperly interpret them
		removedColor.x = RVal;
		removedColor.y = GVal;
		removedColor.z = BVal;

	}

    public int selectQuad(float xCoord , float yCoord) {
    	
    	if (((xCoord >= vertexData[27]) && (vertexData[18] >= xCoord)) &&
    		((yCoord >= vertexData[1]) && (vertexData[10] >= yCoord))) return ID;
        		
        return -1;
        	    	
    }
    
    public void setTexture(Textures texture) {
    	
    	this.texture = texture;
    	
    }
    
    public Textures getTexture() {
    	
    	return texture;
    	
    }

    public boolean isTextured() {
    	
    	return texture.filledOut();
    	
    }
    
    public void fitQuadToTexture(){

    	if(!texture.filledOut()) return;
    	
    	ImageInfo info = texture.imageInfo;
    	
    	setWidth(info.width());
    	setHeight(info.height());

    }
    
    public String getTextureName() {
    	
    	return CSUtil.BigMixin.toNamePath(texture.imageInfo.path());
    	
    }
    
	public void makeTranslucent(float opacity){

		vertexData[6] = opacity;
		vertexData[15] = opacity;
		vertexData[24] = opacity;
		vertexData[33] = opacity;

	}

	public float getTranslucency() {
		
		return vertexData[6];
		
	}
	
    //This method fits quads that are inbetween pixels directly onto the pixel grid
    void snapQuadToPixels(){

    	vertexData[27] = (float)Math.floor(vertexData[27]);
      	vertexData[28] = (float)Math.floor(vertexData[28]);

      	vertexData[0] = (float)Math.floor(vertexData[0]);
      	vertexData[1] = (float)Math.floor(vertexData[1]);

      	vertexData[9] = (float)Math.floor(vertexData[9]);
      	vertexData[10] = (float)Math.floor(vertexData[10]);

      	vertexData[18] = (float)Math.floor(vertexData[18]);
      	vertexData[19] = (float)Math.floor(vertexData[19]);

    }

    public void setWidth(float width){

    	vertexData[0] = vertexData[27] + width;
    	vertexData[18] = vertexData[27] + width;

    }
    
    public void setHeight(float height){

    	vertexData[10] = vertexData[1] + height;
    	vertexData[19] = vertexData[1] + height;

    }
    
    public void setWidthBi(float width) {
    	
    	float currentWidth = vertexData[0] - vertexData[27];
    	float difference = width - currentWidth;
    	float mod = difference / 2;
    	//height
    	vertexData[0] -= mod;
    	vertexData[18] -= mod;
    	//left
    	vertexData[27] += mod;
    	vertexData[9] += mod;
    	
    }
    
    public void setHeightBi(float height) {
    
    	float currentHeight = vertexData[19] - vertexData[1];
    	float difference = height - currentHeight;
    	vertexData[19] -= difference;
    	vertexData[10] -= difference;
    	
    }
    
    public void resizeWidth(float width){

    	float midpoint = (vertexData[0] - vertexData[27]) / 2;
    	float dimensionAdditive = width - (vertexData[0] - vertexData[27]);
    	float resizeFactor = midpoint + dimensionAdditive;

    	vertexData[9] -= resizeFactor;
    	vertexData[27] -= resizeFactor;
    	vertexData[0] += resizeFactor;
    	vertexData[18] += resizeFactor;
    	
    }

    public void resizeHeight(float height){

    	float midpoint = (vertexData[10] - vertexData[28]) / 2;
    	float dimensionAdditive = height - (vertexData[10] - vertexData[28]);
    	float resizeFactor = midpoint + dimensionAdditive;
    	resizeFactor = Math.round(resizeFactor);
    	vertexData[10] += resizeFactor;
    	vertexData[19] += resizeFactor;   	

    }

    public void modWidthRight(float width) {
    	
    	float currentWidth = vertexData[0] - vertexData[27];
    	float modFactor = width - currentWidth;
    	vertexData[0] += modFactor;
    	vertexData[18] += modFactor;
    	
    }
    
    public void modHeightRight(float height) {
    	
    	float currentHeight = vertexData[10] - vertexData[28];
    	float modFactor = height - currentHeight;
    	vertexData[10] += modFactor;
    	vertexData[19] += modFactor;
    	
    }
    
    public void modWidthLeft(float width) {
    	
    	float currentWidth = vertexData[0] - vertexData[27];
    	float modFactor = width - currentWidth;
    	vertexData[27] -= modFactor;
    	vertexData[9] -= modFactor;
    	
    }
    
    public void modHeightLeft(float height) {
    	
    	float currentHeight = vertexData[10] - vertexData[28];
    	float modFactor = height - currentHeight;
    	vertexData[10] += modFactor;
    	vertexData[19] += modFactor;
    	
    }   
    
    public void modWidthBi(float width) {
    	
    	width /= 2;
    	vertexData[0] += width;
    	vertexData[18] += width;
    	vertexData[27] -= width;
    	vertexData[9] -= width;
    	
    }
    
    public void modHeightBi(float height) {

    	height /= 2;
    	vertexData[10] += height;
    	vertexData[19] += height;
    	
    	vertexData[28] -= height;
    	vertexData[1] -= height;
    	
    }
    
    public void modHeightUp(float height) {
    	
    	vertexData[10] += height;
    	vertexData[19] += height;
    	
    }
    
    
    public float getWidthMidpoint(){

    	return (vertexData[0] - vertexData[27]) / 2;

    }

    public float getHeightMidpoint(){

    	return (vertexData[10] - vertexData[28]) /2;

    }

    public float[] getMidpoint(){

    	float widthwiseMidpoint = (vertexData[0] - vertexData[27]) / 2;
    	float heightwiseMidpoint = (vertexData[10] - vertexData[28]) /2;
    	float[] midpointCoords = {vertexData[27] + widthwiseMidpoint , vertexData[28] + heightwiseMidpoint};
    	return midpointCoords;

    }

    public float xMid() {
    	
    	return vertexData[27] + (vertexData[0] - vertexData[27]) / 2;
    	
    }
    
    public float yMid() {
    	
    	return vertexData[28] + (vertexData[10] - vertexData[28]) /2;
    	
    }
    
    public float getWidth(){

    	return Math.abs(vertexData[18] - vertexData[9]);

    }

    public float getHeight(){

    	return Math.abs(vertexData[19] - vertexData[28]);

    }

    public void swapUVs(float[] sprite) {

    	//left
    	vertexData[16] = sprite[0];
    	vertexData[34] = sprite[0];
    	//right
    	vertexData[7] = sprite[1];
    	vertexData[25] = sprite[1];
    	//top
    	vertexData[17] = sprite[2];
    	vertexData[26] = sprite[2];
    	//bottom
    	vertexData[8] = sprite[3];
    	vertexData[35] = sprite[3];
    	
    }

    public void swapAndFlipUVs(float[] sprite) {

    	//left
    	vertexData[16] = sprite[1];
    	vertexData[34] = sprite[1];
    	//right
    	vertexData[7] = sprite[0];
    	vertexData[25] = sprite[0];
    	//top
    	vertexData[17] = sprite[2];
    	vertexData[26] = sprite[2];
    	//bottom
    	vertexData[8] = sprite[3];
    	vertexData[35] = sprite[3];
    	
    }
    
    public void swapSprite(float[] sprite){
    	
    	//left
    	vertexData[16] = sprite[0];
    	vertexData[34] = sprite[0];
    	//right
    	vertexData[7] = sprite[1];
    	vertexData[25] = sprite[1];
    	//top
    	vertexData[17] = sprite[2];
    	vertexData[26] = sprite[2];
    	//bottom
    	vertexData[8] = sprite[3];
    	vertexData[35] = sprite[3];
    	//width
    	resizeWidth(sprite[4]);
    	//height
    	resizeHeight(sprite[5]);

    }
    
    /**
     * Identical to {@code swapSprite} but will avoid some noise when resizing the object. This will instantly set the width and height
     * of the quad to that of the sprite rather than taking a few frames to reach it like {@code resizeWidth} and {@code resizeHeight}
     * 
     * @param sprite — a float array representation of a sprite on a spritesheet
     */
    public void swapSpriteFast(float[] sprite) {

    	//left
    	vertexData[16] = sprite[0];
    	vertexData[34] = sprite[0];
    	//right
    	vertexData[7] = sprite[1];
    	vertexData[25] = sprite[1];
    	//top
    	vertexData[17] = sprite[2];
    	vertexData[26] = sprite[2];
    	//bottom
    	vertexData[8] = sprite[3];
    	vertexData[35] = sprite[3];
    	//width
    	resizeWidth(sprite[4]);
    	resizeWidth(sprite[4]);
    	resizeWidth(sprite[4]);
    	resizeWidth(sprite[4]);
    	resizeWidth(sprite[4]);
    	//height
    	resizeHeight(sprite[5]);
    	resizeHeight(sprite[5]);
    	resizeHeight(sprite[5]);
    	resizeHeight(sprite[5]);
    	resizeHeight(sprite[5]);
    	
    }
    
    public void swapAndFlipSprite(float[] sprite) {

    	//left
    	vertexData[16] = sprite[1];
    	vertexData[34] = sprite[1];
    	//right
    	vertexData[7] = sprite[0];
    	vertexData[25] = sprite[0];
    	//top
    	vertexData[17] = sprite[2];
    	vertexData[26] = sprite[2];
    	//bottom
    	vertexData[8] = sprite[3];
    	vertexData[35] = sprite[3];
    	//width
    	resizeWidth(sprite[4]);
    	//height
    	resizeHeight(sprite[5]);
    	
    }

    public void swapAndFlipSpriteFast(float[] sprite) {

    	//left
    	vertexData[16] = sprite[1];
    	vertexData[34] = sprite[1];
    	//right
    	vertexData[7] = sprite[0];
    	vertexData[25] = sprite[0];
    	//top
    	vertexData[17] = sprite[2];
    	vertexData[26] = sprite[2];
    	//bottom
    	vertexData[8] = sprite[3];
    	vertexData[35] = sprite[3];
    	//width
    	resizeWidth(sprite[4]);
    	resizeWidth(sprite[4]);
    	resizeWidth(sprite[4]);
    	resizeWidth(sprite[4]);
    	resizeWidth(sprite[4]);
    	//height
    	resizeHeight(sprite[5]);
    	resizeHeight(sprite[5]);
    	resizeHeight(sprite[5]);
    	resizeHeight(sprite[5]);
    	resizeHeight(sprite[5]);
    	
    }
    
    void toggleShouldRender(){

    	shouldRender = shouldRender ? false:true;

    }

    public boolean getShouldRender(){

    	return shouldRender;

    }

    public void shouldRender(boolean should) {
    	
    	shouldRender = should;
    	
    }
    
	public void setRightUCoords(float U){

    	vertexData[7] = U;
    	vertexData[25] = U;

    }

    public void setLeftUCoords(float U){

    	vertexData[16] = U;
    	vertexData[34] = U;

    }

    public void setTopVCoords(float V){

    	vertexData[17] = V;
    	vertexData[26] = V;

    }

    public void setBottomVCoords(float V){

    	vertexData[8] = V;
    	vertexData[35] = V;

    }
    
	public int getID() {

		return ID;

	}
	
	public void setID(int ID) {
		
		this.ID = ID;
		
	}

	public float[] getData() {

		return vertexData;

	}
	
	/**
	 * Returns this quad's current UV coordinates, laid out as: <br><br>
	 *  
	 * getUVs()[0] -> left U <br>
	 * getUVs()[1] -> right U <br>
	 * getUVs()[2] -> top V <br>
	 * getUVs()[0] -> bottom V<br>
	 * 
	 * @return — array representation of UV coordinates.
	 */
	public float[] getUVs() {
		 
		
		//7, 8 		//bottom right
	    //16, 17	//top left
	    //25, 26	//top right
	    //34, 35	//bottom left
		return new float[] {vertexData[16] , vertexData[25] , vertexData[17] , vertexData[35]};
		
	}

	public Vector3f getRemovedColor() {

		return removedColor;

	}

	public void setRemovedColor(Vector3f filterColors) {

		this.removedColor = filterColors;

	}

	public void removeColor(float r , float g , float b) {

		removedColor.x = r;
		removedColor.y = g;
		removedColor.z = b;

	}
	
	public boolean isSnapToPixels() {

		return snapToPixels;

	}

	public void setSnapToPixels(boolean snapToPixels) {

		this.snapToPixels = snapToPixels;

	}

	public void toggleSnapToPixels(){

		snapToPixels = snapToPixels ? false:true;

	}

	public Vector3f getFilter() {
		
		return filter;
		
	}
	
	public void setFilter(float r , float g , float b) {
		
		filter.x = r;
		filter.y = g;
		filter.z = b;
		
	}

	public void modRightUCoords(float mod) {
	
		vertexData[7] += mod;
		vertexData[25] += mod;
		
	}
	
	public void modLeftUCoords(float mod) {
	
		vertexData[16] += mod;
		vertexData[34] += mod;
		
	}
	
	public void modTopVCoords(float mod) {
	
		vertexData[17] += mod;
		vertexData[26] += mod;
		
	}
	
	public void modBottomVCoords(float mod) {
		
		vertexData[8] += mod;
		vertexData[35] += mod;
	}
	
	public void modUCoords(float mod) {
		
		vertexData[16] += mod;
		vertexData[25] += mod;
		vertexData[7] += mod;
		vertexData[34] += mod;		
		
	}
	
	public void modVCoords(float mod) {
		
		vertexData[8] += mod;
		vertexData[17] += mod;
		vertexData[26] += mod;
		vertexData[35] += mod;
		
	}

	public void moveTo(float x , float y) {
		
		float[] mid = getMidpoint();
		float deltaX = x - mid[0] , deltaY = y - mid[1];
		translate(deltaX , deltaY);
		
	}
	
	public void moveTo(Quads target) {
		
		CSUtil.BigMixin.moveTo(target, this);
		
	}
	
	public void moveTo(Vector2f pos) {
		
		moveTo(pos.x , pos.y);
		
	}

	public void moveTo(float [] pos) {
		
		moveTo(pos[0] , pos[1]);
		
	}
		
	public float[] specs() {
		
		return new float[] {getWidth() , getHeight() , getMidpoint()[0] , getMidpoint()[1]};
	
	}	
		
	public float[] dimensions() {
	
		return new float [] {getWidth() , getHeight()};
		
	}			
	
	public void setSpecs(float[] specs) {
		
		setWidth(specs[0]);
		setHeight(specs[1]);
		moveTo(specs[2] , specs[3]);
		
		
	}
	
	public float opacity() {
		
		return vertexData[6];
		
	}
	
	public void roundVertices() {
		
		Math.round(vertexData[0]);
		Math.round(vertexData[9]);
		Math.round(vertexData[18]);
		Math.round(vertexData[27]);
		Math.round(vertexData[1]);
		Math.round(vertexData[10]);
		Math.round(vertexData[19]);
		Math.round(vertexData[28]);
		
	}
	
	public void setDimensions(float[] dims) {
		
		setWidth(dims[0]);
		setHeight(dims[1]);
		
	}

	public void setDimensions(int[] dims) {
		
		setWidth(dims[0]);
		setHeight(dims[1]);
		
	}
	
	public void rotate(float amount) {
		
		rotation.rotate((float)Math.toRadians(amount) , 0 , 0 , 1 , rotation);
				
	}

	public Matrix4f rotation() {
		
		return rotation;
		
	}
	
	public void translation(float x , float y) {
		
		translation.translate(x, y, 0 , translation);
		
	}

	public Matrix4f translation() {
		
		return translation;
		
	}
	
	public float[] getTopLeftVertexColor() {
	
		return new float[] {vertexData[12] , vertexData[13] , vertexData[14]};
		
	}

	public float[] getTopRightVertexColor() {
	
		return new float[] {vertexData[21] , vertexData[22] , vertexData[23]};
		
	}
	
	public float[] getBottomLeftVertexColor() {
		
		return new float[] {vertexData[30] , vertexData[31] , vertexData[32]};
		
	}

	public float[] getBottomRightVertexColor() {
		
		return new float[] {vertexData[3] , vertexData[4] , vertexData[5]};
		
	}
	
	/**
	 * Creates a new Quad whose values are equal to this quad's values, but are not shallow references to them.
	 */
	public Quads copy() {
		
		float[] data = new float[vertexData.length];
		System.arraycopy(vertexData , 0 , data , 0 , data.length);
		Quads cloned = new Quads(data , ID , type);
		cloned.texture = this.texture;
		cloned.removedColor = new Vector3f(this.removedColor.x , this.removedColor.y , this.removedColor.z);
		cloned.filter = new Vector3f(this.filter.x , this.filter.y , this.filter.z);
		cloned.shouldRender = this.shouldRender;
		cloned.snapToPixels = this.snapToPixels;	
		
		return cloned;
		
	}
	
	public Quads copy(int ID) {
		
		float[] data = new float[vertexData.length];
		System.arraycopy(vertexData , 0 , data , 0 , data.length);
		Quads cloned = new Quads(data , ID , type);
		cloned.texture = this.texture;
		cloned.removedColor = new Vector3f(this.removedColor.x , this.removedColor.y , this.removedColor.z);
		cloned.filter = new Vector3f(this.filter.x , this.filter.y , this.filter.z);
		cloned.shouldRender = this.shouldRender;
		cloned.snapToPixels = this.snapToPixels;
		
		return cloned;
		
	}
	
}