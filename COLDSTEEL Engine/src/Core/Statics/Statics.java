package Core.Statics;

import static CS.COLDSTEEL.data;
import static CSUtil.BigMixin.getColliderFloatArray;
import static CSUtil.BigMixin.getStaticFloatArray;
import static CSUtil.BigMixin.toByte;
import static CSUtil.BigMixin.toLocalDirectory;
import static CSUtil.BigMixin.toLocalPath;
import static Renderer.Renderer.loadTexture;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;

import org.joml.Vector3f;

import CSUtil.CSTFParser;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.Tuple4;
import Core.CSType;
import Core.GameFiles;
import Core.Quads;
import Physics.Colliders;

public class Statics extends Quads implements GameFiles<Statics> {

	protected String name;	
	protected CSArray<Colliders> colliders = new CSArray<Colliders>(10 , 10);
	private int numberColliders = 0;
	private int activeCollider = -1;
	private boolean collidersFocused = false;
	private boolean renderColliders = true;
	
	private boolean parallax = false;
	private float viewOffsetX = 1f;
	private float viewOffsetY = 1f;
	
	public Statics(String name , int ID){

		super(getStaticFloatArray(), ID, CSType.STATIC);
		this.name = name;

	}

	public Statics(float[] data , int ID , String name) {
		
		super(data , ID , CSType.STATIC);
		this.name = name;
		
	}
	
	public Statics() {
		
		super(getStaticFloatArray() ,  -1 , CSType.STATIC);
		
	}
	
	public Statics(String namePath) {
		
		super(getStaticFloatArray() ,  -1 , CSType.STATIC);
		load(namePath);
		
	}
	
	public void translate(float xSpeed , float ySpeed) {

		if(!collidersFocused){
			
			super.translate(xSpeed , ySpeed);
			colliders.forEach(C -> C.transformAndSnap(xSpeed, ySpeed));

		} else if(activeCollider != -1) colliders.get(activeCollider).transformAndSnap(xSpeed, ySpeed);

	}
	
	public int selectStatic(float cursorX , float cursorY) {

		if(collidersFocused) {
			
			for(int i =  colliders.size() -1 ; i >= 0 ; i--) {
				
				activeCollider = colliders.get(i).selectOwnedCollider(cursorX, cursorY);
				if(activeCollider != -1) return ID;
				
			}
			
		} 
		
		return selectQuad(cursorX , cursorY);
		
	}

	void texture(String filepath){
		
		loadTexture(texture , filepath);
		fitQuadToTexture();

	}

	public void setID(int ID) {
		
		this.ID = ID;
		colliders.forEach(x -> x.setOwnerID(ID));
		
	}

	public void removeColor(float RValue , float GValue , float BValue){

		removeTextureColor(RValue , GValue , BValue);

	}

	void decrementStaticID(){

		decrementID();

	}

	void incrementID() {

		ID++;

	}

	void toggleParallax(){

		parallax = parallax ? false:true;

	}

	public boolean hasParallax(){

		return parallax;

	}

	public void hasParallax(boolean parallax){

		this.parallax = parallax;

	}

	public void setParallaxX(float viewOffsetX){

		this.viewOffsetX = viewOffsetX;

	}

	public float getViewOffsetX(){

		return viewOffsetX;

	}

	public float getViewOffsetY() {

		return viewOffsetY;

	}

	public void setParallaxY(float viewOffsetY) {

		this.viewOffsetY = viewOffsetY;

	}
	
	protected void modParallaxX(float mod) {
		
		viewOffsetX += mod;
		
	}
	
	protected void modParallaxY(float mod) {
		
		viewOffsetY += mod;
		
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

	public void toggleFocusColliders(){

		collidersFocused = collidersFocused ? false:true;

	}

	public boolean collidersFocused(){

		return collidersFocused;

	}

	public Colliders addCollider(){

		Colliders newCollider = new Colliders(getColliderFloatArray() , numberColliders++);
		newCollider.setOwnerID(ID);
		colliders.add(newCollider);
		newCollider.moveTo(this);
		return newCollider;

	}
	
	public int activeColliderIndex(){

		return activeCollider;

	}
	
	public Colliders activeCollider() {
		
		return activeCollider != -1 ? colliders.get(activeCollider) : null;				
		
	}

	void resizeActiveColliderWidth(float width){
		
		if(activeCollider != -1) colliders.get(activeCollider).resizeWidth(width);

	}

	void resizeActiveColliderHeight(float height){

		if(activeCollider != -1) colliders.get(activeCollider).resizeHeight(height);

	}

	public void setActiveColliderUpperRightTriangle(){

		if(activeCollider != -1) colliders.get(activeCollider).makeUpperRightTriangle();

	}

	public void setActiveColliderUpperLeftTriangle(){

		if(activeCollider != -1) colliders.get(activeCollider).makeUpperLeftTriangle();

	}

	public void setActiveColliderLowerRightTriangle(){

		if(activeCollider != -1) colliders.get(activeCollider).makeLowerRightTriangle();

	}

	public void setActiveColliderLowerLeftTriangle(){

		if(activeCollider != -1) colliders.get(activeCollider).makeLowerLeftTriangle();

	}

	public Colliders removeActiveCollider(){

		if(activeCollider != -1 && colliders.size() >= 1) {

			Colliders removed = colliders.remove(activeCollider);
			for(int i = activeCollider ; i < colliders.size() ; i++) colliders.get(i).decrementID();

			activeCollider = -1;
			numberColliders--;
			return removed;
			
		} else return null;


	}

	public void unmakeActiveColliderTriangle(){

		if(activeCollider != -1) colliders.get(activeCollider).resetState();

	}

	Colliders newColliderAndCopyActive(){

		if(activeCollider != -1){

			Colliders newCollider = new Colliders(getColliderFloatArray() , numberColliders++);
			newCollider.setOwnerID(this.ID);
			colliders.add(newCollider);
			Colliders activeCollider = colliders.get(this.activeCollider);

			newCollider.resizeWidth(activeCollider.getWidth() / 2);
			newCollider.resizeHeight(activeCollider.getHeight() / 2);

			Tuple4<Boolean , Boolean , Boolean , Boolean> activeColliderTriangle = activeCollider.getTriangleType();
			if(activeColliderTriangle.getFirst()) newCollider.makeUpperRightTriangle();
			if (activeColliderTriangle.getSecond()) newCollider.makeUpperLeftTriangle();
			else if (activeColliderTriangle.getThird()) newCollider.makeLowerRightTriangle();
			else if (activeColliderTriangle.getFourth()) newCollider.makeLowerLeftTriangle();

			return newCollider;

		} else {

			Colliders newCollider = addCollider();
			return newCollider;
		}

	}

	float getActiveColliderWidth(){

		if(activeCollider != -1) return colliders.get(activeCollider).getWidth();
		else return 0f;

	}

	float getActiveColliderHeight(){

		if(activeCollider != -1) return colliders.get(activeCollider).getHeight();
		else return 0f;

	}


	public CSArray<Colliders> getColliders(){

		return colliders;

	}

	void clear(){

		colliders.clear();
		
	}

	public void setActiveCollider(int active) {
		
		if(active < colliders.size()) activeCollider = active;
		
	}
	
	public void setCollisionList(CSArray<Colliders> newList){

		colliders = newList;
		numberColliders = newList.size();
		activeCollider = -1;

	}

	public boolean isActiveColliderUpperRightTriangle() {
		
		if(activeCollider != -1) return colliders.get(activeCollider).isUpperRightTriangle();
		return false;
		
	}
	
	public boolean isActiveColliderUpperLeftTriangle() {
		
		if(activeCollider != -1) return colliders.get(activeCollider).isUpperLeftTriangle();
		return false;
		
	}
	
	public boolean isActiveColliderLowerRightTriangle() {
		
		if(activeCollider != -1) return colliders.get(activeCollider).isLowerRightTriangle();
		return false;
		
	}
	
	public boolean isActiveColliderLowerLeftTriangle() {
		
		if(activeCollider != -1) return colliders.get(activeCollider).isLowerLeftTriangle();
		return false;
		
	}
	
	public boolean isActiveColliderTriangle() {
		
		if(activeCollider != -1) {
			
			var triangle = colliders.get(activeCollider).getTriangleType();
			if(triangle.getFirst() || triangle.getSecond() || triangle.getThird() || triangle.getFourth()) return true;
			
		}
		
		return false;
			
	}
	
	public void setName(String name) {
		
		this.name = name;
		
	}
	
	public String name() {
		
		return name;
		
	}
	
	void resizeColliderWidth(float mod) {
		
		if(activeCollider != -1) colliders.get(activeCollider).resizeWidth(mod);
		
	}

	void resizeColliderHeight(float mod) {
		
		if(activeCollider != -1) colliders.get(activeCollider).resizeHeight(mod);
		
	}

	public void modColliderWidth(float mod) {
	
		if(activeCollider != -1) colliders.get(activeCollider).modWidth(mod);
		
	}
	
	public void modColliderHeight(float mod) {
		
		if(activeCollider != -1) colliders.get(activeCollider).modHeight(mod);
		
	}
	
	public int numberColliders() {
		
		return colliders.size();
		
	}
	
	protected Colliders getCollider(int index) {
		
		return colliders.get(index);
		
	}
	
	protected void removeCollider(Colliders removeThis) {
		
		colliders.remove(removeThis);
		
	}

	protected Colliders removeCollider(int index) {
		
		return colliders.remove(index);
		
	}
	
	public void moveTo(float x , float y) {
		
		float[] mid = getMidpoint();
		float deltaX = x - mid[0] , deltaY = y - mid[1];
		translate(deltaX , deltaY);
		
	}
	
	public <Output> Output apply(Function<CSArray<Colliders> , Output> function) {
		
		return colliders.apply(function);
		
	}
	
	public void forEach(Consumer<Colliders> function) {
		
		colliders.forEach(function);
		
	}
	
	/**
	 * Deep copies this static, returning the result.
	 * 
	 * @return a deep copy of this.
	 * 
	 */
	public Statics copy() {
				
		float[] data = new float[vertexData.length];
		System.arraycopy(vertexData , 0 , data , 0 , data.length);
		
		Statics cloned = new Statics(data , this.ID , this.name);
		
		cloned.texture = this.texture;
		cloned.removedColor = new Vector3f(this.removedColor.x , this.removedColor.y , this.removedColor.z);
		cloned.filter = new Vector3f(this.filter.x , this.filter.y , this.filter.z);
		cloned.shouldRender = this.shouldRender;
		cloned.setSnapToPixels(this.isSnapToPixels());
		cloned.hasParallax(this.hasParallax());
		cloned.viewOffsetX = this.viewOffsetX;
		cloned.viewOffsetY = this.viewOffsetY;	
		
		
		colliders.forEach(C -> cloned.colliders.add(C.copy()));
		cloned.numberColliders = this.numberColliders;
		cloned.activeCollider = this.activeCollider;
		cloned.collidersFocused = this.collidersFocused;
		cloned.renderColliders = this.renderColliders;
			
		return cloned;
		
	}
	
	@Override public void delete() {

		GameFiles.delete(data + "statics/" + name + ".CStf");
		
		
	}

	@Override public void write(Object...additionalData) {

		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/statics/" + name + ".CStf") , Charset.forName("UTF-8"))){
			
			CSTFParser cstf = new CSTFParser(writer);
			
			cstf.wname(name);
			cstf.wlabelValue("texture" , toLocalPath(texture.imageInfo.path()));
			cstf.wlabelValue("removed" , new float[] {removedColor.x , removedColor.y , removedColor.z});
			
			cstf.wlabelValue("filter" , new float[] {filter.x , filter.y , filter.z});
			cstf.wlabel("parallax");
			if(hasParallax()) cstf.wvalue(new float[] {viewOffsetX , viewOffsetY});
			else cstf.wvalue("null");
			
			cstf.wlabelValue("dimensions", dimensions());
			cstf.wlist("colliders", colliders.size());
			
			float[] sMid = getMidpoint();
			
			colliders.forEach(C -> {
				
				try {
					
					cstf.wlabelValue("dimensions" , C.dimensions());
					cstf.wlabelValue("offset" , new float[] {sMid[0] - C.getMidpoint()[0] , sMid[1] - C.getMidpoint()[1]});
					cstf.wlabelValue("triangle" , new byte[] {
						toByte(C.isUpperRightTriangle()) , 
						toByte(C.isUpperLeftTriangle()) , 
						toByte(C.isLowerRightTriangle()) , 
						toByte(C.isLowerLeftTriangle())
					});
										
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			});
			
			cstf.endList();
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}

	@Override public void load(String namePath) {

		try(BufferedReader reader = Files.newBufferedReader(Paths.get(data + "statics/" + namePath) , Charset.forName("UTF-8"))){
			
			CSTFParser cstf = new CSTFParser(reader);
			
			name = cstf.rname();
			texture(CS.COLDSTEEL.assets + toLocalDirectory(cstf.rlabel("texture")));
			
			//used every time we need to load some data represented as a float array
			float[] array = new float[3];
			
			cstf.rlabel("removed" ,  array);
			removedColor.x = array[0]; removedColor.y = array[1]; removedColor.z = array[2];
			cstf.rlabel("filter" , array);
			filter.x = array[0]; filter.y = array[1]; filter.z = array[2];
			
			boolean noParallax = cstf.rtestNull();
			if(noParallax) {
				
				hasParallax(false);
				cstf.rlabel();
			
			} else {
				
				hasParallax(true);
				cstf.rlabel("parallax" , array);
				viewOffsetX = array[0];
				viewOffsetY = array[1];
				
			}
			
			cstf.rlabel("dimensions" , array);
			setWidth(array[0]);
			setHeight(array[1]);
			
			int numberColliders = cstf.rlist("colliders");
			colliders = new CSArray<Colliders>(numberColliders , 10);
			float[] mid = getMidpoint();
			byte[] triangle = new byte[4];
			
			for(int i = 0 ; i < numberColliders ; i ++) {
				
				//load colliders
				Colliders collider = new Colliders();
				collider.setID(i);
				collider.setOwnerID(ID);
				cstf.rlabel(array);
				collider.setDimensions(array);
				cstf.rlabel(array);
				collider.moveTo(mid[0] - array[0] , mid[1] -  array[1]);
				cstf.rlabel(triangle);
				if(CSUtil.BigMixin.toBool(triangle[0])) collider.makeUpperRightTriangle();
				else if(CSUtil.BigMixin.toBool(triangle[1])) collider.makeUpperLeftTriangle();
				else if(CSUtil.BigMixin.toBool(triangle[2])) collider.makeLowerRightTriangle();
				else if(CSUtil.BigMixin.toBool(triangle[3])) collider.makeLowerLeftTriangle();
				colliders.add(collider);
				
			}
						
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}		
		
	}

	@Override public void write(BufferedWriter writer , Object...additionalData) {}

	@Override public void load(BufferedReader reader) {}

}