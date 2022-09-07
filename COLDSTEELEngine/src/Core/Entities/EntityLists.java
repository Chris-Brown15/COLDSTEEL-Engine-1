package Core.Entities;

import static Renderer.Renderer.loadTexture;
import static CSUtil.BigMixin.getCoordinateRectangleArea;
import static CSUtil.BigMixin.getLowerTriangleArea;
import static CSUtil.BigMixin.getTriangleArea;
import static CSUtil.BigMixin.getTrianglePointArea;
import static CSUtil.BigMixin.getArrayMidpoint;
import static CSUtil.BigMixin.toggle;

import CSUtil.QuadIndices;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.DialogUtils;
import Core.Executor;
import Core.Quads;
import Core.SpriteSets;
import Core.TemporalExecutor;
import Core.ECS;
import Core.AbstractGameObjectLists;
import Core.CSType;
import Core.Direction;
import Game.Items.Inventories;
import Game.Items.Items;
import Game.Items.UnownedItems;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.joml.Math;
import org.joml.Vector3f;

import CS.Engine;
import CS.GLFWWindow;
import Physics.ColliderLists;
import Physics.Colliders;
import Renderer.Camera;
import Renderer.Renderer;
import Game.Projectiles.ProjectileIndices;
import Game.Projectiles.Projectiles;

public class EntityLists extends AbstractGameObjectLists<Entities>{
	
	private static boolean playAnimations = true;
	private final Camera camera;
		
	public EntityLists(int renderOrder , Camera camera) {
		
		super(renderOrder , CSType.ENTITY);
		this.camera = camera;
		
	}
	
	public void newEntity() {
		
		Supplier<String> name = DialogUtils.newInputBox("Input Entity Name" , 5 , 270);
		TemporalExecutor.onTrue(() -> name.get() != null , () -> newEntity(name.get()));	
		
	}
	
	public Entities newEntity(String name) {
		
		Entities newEntity = new Entities(name , list.size());
		list.add(newEntity);
		return newEntity;
		
	}
		
	public void addStraightIn(Entities E) {
		
		list.add(E);
		
	}
	
	public void add(Entities E) {
		
		E.setID(list.size());
		list.add(E);
		
	}
	
	public void remove(int removeThis) {
		
		if(removeThis > -1 && removeThis < list.size()) {
			
			Entities removed = list.removeVal(removeThis);
			Object[] comps = removed.components();
			
			if(removed.has(ECS.SCRIPT)) {
				
				EntityScripts script = (EntityScripts) comps[Entities.SOFF];
				script.call("shutDown()");
				
			}
			
			for(int i = removeThis ; i < list.size() ; i++) list.removeVal(i).decrementID();
						
		}
		
	}
		
	public void remove(Entities removeThis) {
		
		if(list.has(removeThis)) {
			
			list.removeVal(removeThis);
			Object[] comps = removeThis.components();
			
			if(removeThis.has(ECS.SCRIPT)) ((EntityScripts) comps[Entities.SOFF]).call("shutDown()");			
			for(int i = removeThis.getID() ; i < list.size() ; i++) list.getVal(i).decrementID();
			
		}
		
	}

	public void delete(Entities E) {
		
		list.removeVal(E);
		E.delete();
		
	}
	
	public Entities loadEntity(String namePath) {
		
		Entities loaded = new Entities(namePath);
		loaded.setID(list.size());
		list.add(loaded);		
		return loaded;
		
	}
	
	public void saveEntity(int index) {
		
		list.getVal(index).write();		
		
	}
		
	public void filterColor(int index , float R , float G , float B) {
		
		list.getVal(index).setFilter(R, G, B);
		
	}
		
	public Entities get(int index) {
		
		return list.getVal(index);
		
	}
		
	public cdNode<Entities> iter(){
		
		return list.get(0);
		
	}
	
	public void removeAllEntities() {
		
		if(list.size() > 0) {
			
			remove(0);
			removeAllEntities();			
			
		}
		
	}
	
	public Quads selectEntity(float cursorX , float cursorY) {
				
		for(int i = list.size() -1 ; i >= 0 ; i--) if(list.getVal(i).selectEntity(cursorX , cursorY) != -1) return list.getVal(i);		
		return null;
		
	}

	public void translate(int index , float xSpeed , float ySpeed) {
		
		list.getVal(index).translate(xSpeed, ySpeed);
		
	}

	public int size() {
		
		return list.size();
		
	}

	public void textureEntity(int index , String filePath) {
		
		list.getVal(index).setTexture(loadTexture(filePath));
		
	}
	
	public void textureEntity(Entities E , String filepath) {
		
		E.setTexture(loadTexture(filepath));
		
	}
	
	public void removeTexture(int index) {
		
		list.getVal(index).setTexture(null);	
		
	}
	
	public void filterColor(int index , Vector3f color) {
		
		list.getVal(index).setFilter(color.x, color.y, color.z);
		
	}
	
	public void removeColor(int index , Vector3f color) {
		
		list.getVal(index).removeColor(color);
		
	}
	
	public boolean has(Entities E) {
		
		return list.has(E);
		
	}

	public void toggleAnimations() {
		
		playAnimations = playAnimations ? false:true;
		
	}
	
	public void toggleComponent(Entities E , ECS component) {
		
		if(E.has(component)) E.removeComponents(component);
		else E.addComponents(component);
			
	}
		
	public void modWidth(int index , float mod) {
		
		list.getVal(index).modWidthBi(mod);
		
	}
	
	public void modActiveHeight(int index , float mod) {
		
		list.getVal(index).modHeightUp(mod);
		
	}
	
	public static final void toggleHorizontalControl(Entities E) {
		
		Object[] comps = E.components();
		if(E.has(ECS.HORIZONTAL_PLAYER_CONTROLLER)) comps[Entities.HCOFF + 1] = (int)comps[Entities.HCOFF + 1] == 1 ? 0:1;
		
	}

	public static final void toggleVerticalControl(Entities E) {

		Object[] comps = E.components();
		if(E.has(ECS.VERTICAL_PLAYER_CONTROLLER)) comps[Entities.VCOFF + 4] = (int) comps[Entities.VCOFF + 4] == 1 ? 0:1;
		
	}
	
	public static final void setHorizontalControl(Entities E , boolean state) {
		
		E.components()[Entities.HCOFF + 1] = state;
	
	}
	
	public static final void setVerticalControl(Entities E , boolean state) {
		
		E.components()[Entities.VCOFF + 4] = state;
		
	}
	
	/**
	 * Moves E left or right speed, checking for collisions along the way. This scans for triangle collisions and box collisions in one go.
	 * 
	 * @param E — Entity to move
	 * @param speed — amount to move 
	 * @return true if E collided
	 */
	public static final boolean moveHorizChecked(Entities E , float speed) {
	
		boolean didCollide = false;
		Object[] comps = E.components();			
		
		if(E.has(ECS.COLLISION_DETECTION)) {
			
			final int 
			TLX , TLY ,
			TRX , TRY ,
			BLX , BLY , 
			BRX , BRY ,
			BY , TY ;
			
			float[] startingMid;
			float[] targetData;

			float scanningDistance = (float) E.components()[ECS.COLLISION_DETECTION.offset + 1];
			Direction moveDirection = speed > 0f ? Direction.RIGHT : Direction.LEFT;

			if(E.type == CSType.ENTITY) {
				
				TLX = QuadIndices.TLX;
				TLY = QuadIndices.TLY;
				TRX = QuadIndices.TRX;
				TRY = QuadIndices.TRY;
				BLX = QuadIndices.BLX;
				BLY = QuadIndices.BLY;
				BRX = QuadIndices.BRX;
				BRY = QuadIndices.BRY;
				BY = QuadIndices.BY;
				TY = QuadIndices.TY;
				
				targetData = comps[Entities.CDOFF] != null ? (float[]) comps[Entities.CDOFF]:E.getData();
				startingMid = E.getMidpoint();
				
			} else {

				TLX = ProjectileIndices.TLX;
				TLY = ProjectileIndices.TLY;
				TRX = ProjectileIndices.TRX;
				TRY = ProjectileIndices.TRY;
				BLX = ProjectileIndices.BLX;
				BLY = ProjectileIndices.BLY;
				BRX = ProjectileIndices.BRX;
				BRY = ProjectileIndices.BRY;
				BY = ProjectileIndices.BY;
				TY = ProjectileIndices.TY;
								
				targetData = ((Projectiles)E).getPosition();
				startingMid = ((Projectiles)E).getMidpoint();
				
			}

			ArrayList<Colliders> validBoxes = new ArrayList<Colliders>();
			ArrayList<Colliders> validTriangles = new ArrayList<Colliders>();
			float[] xData;
			
			//checks for and removes invalid colliders
			CSLinked<Colliders> allColliders = ColliderLists.getComposite();
			
			cdNode<Colliders> iter = allColliders.get(0);
			Colliders x;
			for(int i = 0 ; i < allColliders.size() ; i ++ , iter = iter.next) {
				
				x = iter.val;
				xData = x.getData();
				
				float[] xMid = x.getMidpoint(); //if the collider is out of range skip it.
				if((Math.abs(startingMid[0] - xMid[0]) >= scanningDistance || Math.abs(startingMid[1] - xMid[1]) >= scanningDistance)
 				 ||(Math.abs(xMid[0] - startingMid[0]) >= scanningDistance || Math.abs(xMid[1] - startingMid[1]) >= scanningDistance)) continue;
				
				if(!x.isTriangle()) {				
					
					if(x.isPlatform() && targetData[TY] > xData[BY]) continue;
					
					if((moveDirection == Direction.LEFT && x.getMidpoint()[0] > startingMid[0])//if its behind the subject 
					 ||(moveDirection == Direction.RIGHT&& x.getMidpoint()[0] < startingMid[0])
 					 // if its above or below target
					 ||(targetData[BRY] >= xData[19] || xData[1] >= targetData[TRY]))continue;
					
					validBoxes.add(x);
					
				} else if(x.isTriangle()) {

					//if the triangle is below or above the subject, ignore it					
					if((x.isLowerLeftTriangle() || x.isUpperLeftTriangle()) && (xData[1] >= targetData[TRY] || targetData[BRY] >= xData[19])) continue;					
					if ((x.isLowerRightTriangle() || x.isUpperRightTriangle()) && (xData[28] > targetData[TY] || xData[10] < targetData[BY])) continue;
					//if the triangle is behind the subject, ignore it
					if(x.isUpperRightTriangle() || x.isUpperLeftTriangle()) {
						
						if(moveDirection == Direction.RIGHT && targetData[BLX] >= xData[0]) continue;
						else if (moveDirection == Direction.LEFT && xData[27] >= targetData[BRX]) continue;
						
					} else if (x.isLowerLeftTriangle() || x.isLowerRightTriangle()) {
						
						if(moveDirection == Direction.RIGHT && targetData[BLX] >= xData[18]) continue;
						else if (moveDirection == Direction.LEFT && xData[0] >= targetData[BRX]) continue;
						
					} 
				
					validTriangles.add(x);
					
				}
		
			}
			
//			System.out.println("Number valid triangles: " + validTriangles.size());
			
			E.translate(speed, 0f);
			
			
			for(Colliders b : validBoxes) {
				
				xData = b.getData();
							
				boolean isColliding = false;
				float distance;
				if(moveDirection == Direction.RIGHT) {
					
					distance = xData[27] - targetData[BRX];
					if(targetData[BLY] < xData[19] && targetData[TLY] > xData[28] && targetData[BLX] < xData[18]){
						
						int XArea = (int) getCoordinateRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]);
						int targetArea = (int)getCoordinateRectangleArea(targetData[BRX] , targetData[BRY] , xData[18] , xData[19] , xData[0] , xData[1]);
						isColliding = XArea > targetArea ? true:false; //only works for rectangles
						
					}
					
				} else {
					
					distance = targetData[BLX] - xData[0];
					if(targetData[BLY] < xData[19] && targetData[TLY] > xData[28] && targetData[BLX] < xData[18] ){

						int XArea = (int) getCoordinateRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]);
						int targetArea = (int)getCoordinateRectangleArea(targetData[BLX] , targetData[BLY] , xData[18] , xData[19] , xData[0] , xData[1]);	
						isColliding = XArea > targetArea ? true:false; 
					
					}
	
				}
								
				if(isColliding) {
					
					if(moveDirection == Direction.RIGHT) {
						
						E.translate(distance , 0f);
						didCollide = true;
						return true;
						
					} else {					

						E.translate(-distance, 0f);
						didCollide = true;
						return true;
						
					}
					
				} else {

					//if this collider moved passed a collider entirely, move it back to its appropriate position.
					if(moveDirection == Direction.RIGHT) if(xData[0] < targetData[BRX]) E.translate(-distance, 0f);
					else if(moveDirection == Direction.LEFT) if(xData[0] > targetData[BLX]) E.translate(distance, 0f);
										
				}
										
			}
			
			for(Colliders t : validTriangles) {
				
				xData = t.getData();
				boolean isColliding = false;
				float distance;
				
				if(moveDirection == Direction.LEFT) {
					
					//treat as triangles
					
					if(t.isUpperRightTriangle()) {
						
						float colliderArea = getTriangleArea(xData);
						colliderArea = Math.round(colliderArea);
				   		float targetArea = 0;
				   		targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[BLX] , targetData[BLY] , xData[0] , xData[1]);
				   		targetArea += getTrianglePointArea(targetData[BLX] , targetData[BLY] , xData[9] , xData[10]  , xData[0] , xData[1]);
				   		targetArea += getTrianglePointArea(xData[27] , xData[28] , xData[9] , xData[10] , targetData[BLX] , targetData[BLY]);
				   		targetArea = Math.round(targetArea);

				   		isColliding = colliderArea >= targetArea;
				   		
				   		if(isColliding) {
				   			
				   			//V1 is bottom right , V2 is top left
				   			float slope = (xData[10] - xData[1]) / (xData[9] - xData[0]);
				   			float intercept = xData[1] - (slope * xData[0]);
				   			
				   			float yValue = (slope * targetData[BLX]) + intercept;
				   			distance = yValue - targetData[BLY];
				   			E.translate(0f, distance);
				   			didCollide = true;
				   			
				   		} else if (xData[27] >= targetData[BRX]) {//if E moved passed this
				   			
				   			distance = xData[10] - targetData[BRY];
				   			E.translate(0f, distance);
				   			didCollide = true;
				   			
				   		} else if (xData[0] > targetData[BLX] && xData[1] > targetData[BRY]) {//if E collides with the point
							
							distance = targetData[BLX] - xData[0]; 
							E.translate(distance, 0f);
							didCollide = true;
							
						}
											
					}
					
					else if (t.isLowerRightTriangle()) {
					
						float colliderArea = getLowerTriangleArea(xData);
				    	colliderArea = Math.round(colliderArea);

				    	float targetArea = 0f;
				    	targetArea += getTrianglePointArea(targetData[TLX] , targetData[TLY] , xData[9] , xData[10] , xData[18] , xData[19]);
				    	targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[TLX] , targetData[TLY] , xData[18] , xData[19]);
				    	targetArea += getTrianglePointArea(xData[27] , xData[28] , xData[9] , xData[10] , targetData[TLX] , targetData[TLY]);
				    	targetArea = Math.round(targetArea);
				    	
				    	isColliding = colliderArea >= targetArea;
				    	
				    	if(isColliding) {
				    		
				    		//V1 is bottom left , V2 is top right
				    		float slope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
				    		float intercept = xData[28] - (slope * xData[27]);
				    		
				    		float yValue = (slope * targetData[TLX]) + intercept;
				    		distance = targetData[TLY] - yValue;
				    		E.translate(0f, -distance);
				    		didCollide = true;
				    		
				    	} else if (xData[27] >= targetData[BRX]) {//if E moved past this
				    		
				    		distance = targetData[TRY] - xData[28];
				    		E.translate(0f, -distance);
				    		didCollide = true;
				    		
				    	} else if (xData[18] > targetData[BLX] && targetData[TLY] > xData[19] && xData[19] > targetData[BLY]) {//if E is touching point
				    		
				    		distance = xData[18] - targetData[BLX];
				    		E.translate(-distance, 0f);
				    		didCollide = true;
				    		
				    	}
						
					}
					
					//treat as boxes 
					
//					else if(x.isUpperLeftTriangle()) {
//					
//						int xArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]); 
//						int testArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , targetData[TLX] , xData[1]);
//						isColliding = xArea >= testArea;
//												
//						if(isColliding) {
//							 
//							distance = xData[0] - targetData[BLX];
//							E.translate(-distance, 0f);
//							didCollide = true;
//							
//						} else if (xData[0] >= targetData[BRX]) {	
//							
//							distance = xData[0] - targetData[BLX];
//							E.translate(distance, 0f);
//							didCollide = true;
//							
//						} 
//						
//					}
//					
//					else if (x.isLowerLeftTriangle()) {
//						
//						int xArea = (int) getCoordianteRectangleArea(xData[9] , xData[10] , xData[18] , xData[19] , xData[0] , xData[1]);
//						int testArea = (int) getCoordianteRectangleArea(xData[9] , xData[10] , targetData[BLX] , xData[19] ,  xData[0] , xData[1]);
//						isColliding = xArea >= testArea;
//						
//						if(isColliding) {
//							
//							distance = xData[0] - targetData[BLX];
//							E.translate(-distance, 0f);
//							didCollide = true;
//							
//						} else if (xData[9] > targetData[BRX]) {
//							
//							distance = xData[0] - targetData[BLX];
//							E.translate(distance, 0f);
//							didCollide = true;
//							
//						} 
//						
//					}
						
				} else if (moveDirection == Direction.RIGHT) {
					
					//treat as triangles
					
					if(t.isUpperLeftTriangle()) {
						
						float colliderArea = getTriangleArea(xData);
				   		colliderArea = Math.round(colliderArea);

				   		float targetArea = 0;
				   		targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[BRX] , targetData[BRY] , xData[0] , xData[1]);
				   		targetArea += getTrianglePointArea(targetData[BRX] , targetData[BRY] , xData[18] , xData[19]  , xData[0] , xData[1]);
				   		targetArea += getTrianglePointArea(xData[27] , xData[28], xData[18] , xData[19] , targetData[BRX] , targetData[BRY]);
				   		targetArea = Math.round(targetArea);

				   		isColliding = colliderArea >= targetArea; 
				   		
						if(isColliding) {
							//V1 = bottom left , V2 = top right
							float slope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
							float intercept = xData[19] - (slope * xData[18]);
							
							float yValue = (slope * targetData[BRX]) + intercept;
							distance = yValue - targetData[BRY];
							
							E.translate(0f, distance);
							didCollide = true;
							
						} else if (xData[28] > targetData[BRY] && targetData[BRX] > xData[27]) {//colliding with point
							
							distance = targetData[BRX] - xData[27];
							E.translate(distance, 0f);
							didCollide = true;
							
						} else if (targetData[BLX] > xData[0]) {
							
							distance = xData[19] - targetData[BLY];
							E.translate(0f, distance);
							didCollide = true;
							
						}
				   						   		
					}
					
					else if(t.isLowerLeftTriangle()) {
						
						float colliderArea = Math.round(getLowerTriangleArea(xData));
						
				    	float targetArea = getTrianglePointArea(targetData[TRX] , targetData[TRY] , xData[18] , xData[19] , xData[0] , xData[1]);
				    	targetArea += getTrianglePointArea(xData[9] , xData[10] , targetData[TRX] , targetData[TRY]  , xData[0] , xData[1]);
				    	targetArea += getTrianglePointArea(xData[9] , xData[10] , xData[18] , xData[19] , targetData[TRX] , targetData[TRY]);
				    	targetArea = Math.round(targetArea);

				    	isColliding = colliderArea >= targetArea;
				    	
				    	if(isColliding) {
				    		
				    		//V1 = bottom Right , V2 is top left
				    		float slope = (xData[10] - xData[1]) / (xData[9] - xData[0]);
				    		float intercept = xData[1] - (slope * xData[0]);
				    		
				    		float yValue = (targetData[TRX] * slope) + intercept;
				    		distance = targetData[TRY] - yValue;
				    		E.translate(0f, -distance);
				    		didCollide = true;
				    		
				    	} else if (targetData[BLX] > xData[0]) {//moved past it
				    		
				    		distance = targetData[BRX] - xData[9];
				    		E.translate(-distance, 0f);
				    		didCollide = true;
				    						    		
				    	} else if (targetData[TLY] >= xData[10] && targetData[BRX] > xData[9]) {//touching its point
							
							distance = targetData[BRX] - xData[9];
							E.translate(distance, 0f);
							didCollide = true;
							
						}						
				    	
					}
					
					//treat as boxes
					
//					else if(x.isUpperRightTriangle() && xData[27] > targetData[BRX]) {
//					
//						int xArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , xData[10] , xData[0] , xData[1]); 
//						int testArea = (int) getCoordianteRectangleArea(targetData[BRX] , xData[28] , xData[9] , xData[10] , xData[0] , xData[1]);
//						
//						isColliding = xArea > testArea;
//						
//						if(isColliding) {
//																			
//							distance = targetData[BRX] - xData[27];
//							E.translate(distance, 0f);
//							didCollide = true;
//							
//						}						
//						
//					}
//					
//					else if (x.isLowerRightTriangle()) {
//						
//						int xArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , xData[10] , xData[18] , xData[19]);
//						int testArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , targetData[BRX] , xData[10] , xData[18] , xData[19]);
//						
//						isColliding = xArea > testArea;
//						
//						if(isColliding) {
//							
//							distance = targetData[BRX] - xData[27];
//							E.translate(distance, 0f);
//							didCollide = true;
//							
//						} 		
//						
//					}
					
				}
				
			}
			
		} else E.translate(speed, 0f);
		
		comps[Entities.CDOFF + 2] = didCollide;
		if(didCollide && E.type == CSType.PROJECTILE) ((Projectiles)E).onCollide();
		return didCollide;
		
	}
	
	/**
	 * Moves E speed, then scans for collisions on valid box and triangle colliders. Operates in three major steps;
	 * First, any invalid colliders are removed from the scan. A series of conditions have to be met in order to be valid.
	 * Next, valid box colliders are scanned. Collisions will be detected no matter E's movespeed, and E will be moved back accordingly
	 * Finally, valid triangles are scanned. E can collide with the point of a triangle or its hypoteneus, and in either case will be moved
	 * back to the nearest point outside of the collider.
	 * 
	 * @param E — entity to move, with collisions 
	 * @param speed — amount of units to move E
	 * @return true if the object collided, false otherwise
	 */
	public static final boolean moveVertChecked(Entities E , float speed) {
		
		boolean didCollide = false;
		Object[] comps = E.components();
		
		if(E.has(ECS.COLLISION_DETECTION)) {

			final int 
			TLX , TLY ,
			TRX , TRY ,
			BLX , BLY , 
			BRX , BRY ;
			
			float[] startingMid;
			float[] targetData;

			float scanningDistance = (float) E.components()[ECS.COLLISION_DETECTION.offset + 1];
			Direction moveDirection = speed > 0f ? Direction.UP : Direction.DOWN;

			if(E.type == CSType.ENTITY) {
				
				TLX = QuadIndices.TLX;
				TLY = QuadIndices.TLY;
				TRX = QuadIndices.TRX;
				TRY = QuadIndices.TRY;
				BLX = QuadIndices.BLX;
				BLY = QuadIndices.BLY;
				BRX = QuadIndices.BRX;
				BRY = QuadIndices.BRY;
				
				targetData = comps[Entities.CDOFF] != null ? (float[]) comps[Entities.CDOFF]:E.getData();
				startingMid = E.getMidpoint();
				
			} else {

				TLX = ProjectileIndices.TLX;
				TLY = ProjectileIndices.TLY;
				TRX = ProjectileIndices.TRX;
				TRY = ProjectileIndices.TRY;
				BLX = ProjectileIndices.BLX;
				BLY = ProjectileIndices.BLY;
				BRX = ProjectileIndices.BRX;
				BRY = ProjectileIndices.BRY;
				
				targetData = ((Projectiles)E).getPosition();
				startingMid = ((Projectiles)E).getMidpoint();
				
			}
			
			float[] xData;
			ArrayList<Colliders> validBoxes = new ArrayList<Colliders>();
			ArrayList<Colliders> validTriangles = new ArrayList<Colliders>();			
			
			//checks for and removes invalid colliders
			CSLinked<Colliders> allColliders = ColliderLists.getComposite();
			cdNode<Colliders> iter = allColliders.get(0);
			Colliders x;
			for(int i = 0 ; i < allColliders.size() ; i ++ , iter = iter.next) {
				
				x = iter.val;
				float[] xMid = x.getMidpoint();
				//if the collider is too far, ignore it
				if((Math.abs(startingMid[0] - xMid[0]) >= scanningDistance || Math.abs(startingMid[1] - xMid[1]) >= scanningDistance)
		  		 ||(Math.abs(xMid[0] - startingMid[0]) >= scanningDistance || Math.abs(xMid[1] - startingMid[1]) >= scanningDistance)) continue;
				
				//boxes that are to the left or right of the E 
				xData = x.getData();//collider is to the left or right of E
				if(!x.isTriangle()) {
					
					if(x.isPlatform()) {
						
						Colliders previousPlatform = (Colliders)comps[Entities.VCOFF + 5];
						//if we arent on top of the platform or if we are passing through it, continue
						if(moveDirection == Direction.UP || x == previousPlatform) continue;

					}
					
					//if the object is behind the subject, ignore it
					if(moveDirection == Direction.UP && xMid[1] < startingMid[1] ||
	  				   moveDirection == Direction.DOWN && xMid[1] > startingMid[1]) continue;
				
        			if(targetData[BLX] >= xData[0] || xData[27] >= targetData[BRX]) continue;
        			
        			validBoxes.add(x);
        			
    			}
				//triangles that are to the left or right of E or behind it  
				if(x.isLowerRightTriangle() || x.isLowerLeftTriangle()) {
					
					if(xData[9] >= targetData[BRX] || targetData[BLX] >= xData[18]) continue;
					
					if(x.isLowerRightTriangle() && 
				      (moveDirection == Direction.UP && targetData[BLY] >= xData[10]) 
				     ||moveDirection == Direction.DOWN && xData[28] >= targetData[BLY]) continue;
					else if (x.isLowerLeftTriangle() &&
				      (moveDirection == Direction.UP && targetData[BLY] >= xData[10])
				     ||moveDirection == Direction.DOWN && xData[1] >= xData[1]) continue;
					
					validTriangles.add(x);
					
				} 
				else if (x.isUpperLeftTriangle() || x.isUpperRightTriangle()) {
					
					if(targetData[BLX] >= xData[0] || xData[27] >= targetData[BRX]) continue;
					
					if(x.isUpperLeftTriangle() &&
					  (moveDirection == Direction.UP && targetData[BLY] >= xData[28])
					||(moveDirection == Direction.DOWN && xData[28] >= targetData[TLY])) continue; 	
					else if (x.isUpperRightTriangle() &&
					  (moveDirection == Direction.UP && targetData[BLY] >= xData[28])
					||(moveDirection == Direction.DOWN &&  xData[28] >= targetData[TLY])) continue;
					
					validTriangles.add(x);
					
				}
			    			
			}
			
			E.translate(0f, speed);
			
			for(Colliders b : validBoxes) {
				
				xData = b.getData();				
				boolean isColliding = false;
				
				float distance;
				if(moveDirection == Direction.UP) distance = xData[1] - targetData[TRY];
				else distance = targetData[BRY] - xData[19];

				if(targetData[BLY] < xData[19] && targetData[TLY] > xData[28]){
					
					if(targetData[BLX] < xData[18] ){
						
						int XArea = (int) getCoordinateRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]);
						int targetArea = (int)getCoordinateRectangleArea(targetData[BRX] , targetData[BRY] , xData[18] , xData[19] , xData[0] , xData[1]);
						isColliding = XArea > targetArea ? true:false;
					
					}
					
				}
				
				if(isColliding) {
					
					didCollide = true;
										
					if(moveDirection == Direction.UP) {
						
						E.translate(0f, distance);
						break;
						
					} else {
						
						E.translate(0f, -distance);
						break;
						
					}					
					
				} else {
					
					if(moveDirection == Direction.UP) {
						
						if(targetData[BRY] > xData[1]) {
							
							E.translate(0f, -distance);
							didCollide = true;
							continue;
							
						}
						
					} else {
						
						if(xData[19] > targetData[TRY]) {
							
							E.translate(0f, distance);
							didCollide = true;
							continue;
							
						}						
						
					}					
					
				}
						
			}
			
			//triangles ahead
			
			for(Colliders t : validTriangles) {
				
				boolean isColliding = false;
				float distance;
				xData = t.getData();						
				
				if(moveDirection == Direction.UP) {
					
					// treat as triangles

					if(t.isLowerLeftTriangle()) {
						
				    	float colliderArea = getLowerTriangleArea(xData);
				    	colliderArea = Math.round(colliderArea);

				    	float targetArea = 0f;
				    	//target's top right vertex replaces A
				    	targetArea += getTrianglePointArea(targetData[TRX] , targetData[TRY] ,  xData[18] , xData[19] , xData[0] , xData[1]);
				    	//targets top right vertex replaces B
				    	targetArea += getTrianglePointArea(xData[9] , xData[10] , targetData[TRX] , targetData[TRY] , xData[0] , xData[1]);
				    	//target's top right vertex replaces C
				    	targetArea += getTrianglePointArea(xData[9] , xData[10] , xData[18] , xData[19] , targetData[TRX] , targetData[TRY]);
				    	targetArea = Math.round(targetArea);

				    	isColliding = colliderArea == targetArea;

				    	if(isColliding && xData[0] > targetData[BRX]) {				    	
				    		
				    		didCollide = true;
				    		//slope of hypoteneus V1 is top left,  V2 is bottom right
				    		float triHypSlope = (xData[1] - xData[10]) / (xData[0] - xData[9]);//y2 - y1 / x2 - x1
				    		float mx = triHypSlope * xData[9]; //use slope and V1 to find intercept;
				    		float intercept = xData[10] - mx;
				    		//triangle hypoteneus formula = tryHypeSlope * x + intercept
				    		//plug in target's top right x to get the y value on the hyp line 
				    		//then move back the distance of top right y and distance
				    		float yValue = (targetData[TRX] * triHypSlope) + intercept; 
				    		distance = targetData[TRY] - yValue;
				    		E.translate(0f, -distance);				    		
				    		
				    	} else if (targetData[TLY] > xData[1] && targetData[BRX] >= xData[0]) {				    		
				    		
				    		distance = targetData[TRY] - xData[1];
				    		E.translate(0f, -distance);
				    		didCollide = true;				    		
				    		
				    	} else if(targetData[TRY] > xData[19]) {
				    	    
				    		didCollide = true;
				    		float triHypSlope = (xData[1] - xData[10]) / (xData[0] - xData[9]);
				    		float intercept = xData[10] - (triHypSlope * xData[9]);
				    		float yValue = (targetData[TRX] * triHypSlope) + intercept; 
				    		distance = targetData[TRY] - yValue;
				    		E.translate(0f, -distance);		
				    		
				    	}
				    					    	
					} else if (t.isLowerRightTriangle()) {

				    	float colliderArea = Math.round(getLowerTriangleArea(xData));
				    	
				    	float targetArea = getTrianglePointArea(targetData[TLX] , targetData[TLY] , xData[9] , xData[10] , xData[18] , xData[19]);
				    	targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[TLX] , targetData[TLY] , xData[18] , xData[19]);
				    	targetArea += getTrianglePointArea(xData[27] , xData[28] , xData[9] , xData[10] , targetData[TLX] , targetData[TLY]);
				    	targetArea = Math.round(targetArea);
				    	isColliding = colliderArea == targetArea;	    	
				    	
				    	if(isColliding || targetData[TLY] > xData[10]) {
				    	    
					    	//V1 is bottom left , V2 is top right
					    	float triHypSlope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
					    	float intercept = xData[28] - (triHypSlope * xData[27]);
					    	float yValue = (targetData[TLX] * triHypSlope) + intercept;
					    	distance = targetData[TLY] - yValue;
					    	E.translate(0f, -distance);
					    	didCollide = true;
					    						    	
				    	} else if (xData[27] >= targetData[TLX] && targetData[TLY] > xData[28]) {
				    		
				    		distance = targetData[TLY] - xData[28];
				    		E.translate(0f, -distance);
				    		didCollide = true;
				    		
				    	}
						
					}
					
//					//treat as boxes
//					
//					else if(x.isUpperRightTriangle() ) {
//						
//						int XArea = (int)getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , xData[19] , xData[0] , xData[1]);
//						int areaTest = (int)getCoordianteRectangleArea(xData[27] , targetData[TRY] , xData[9] , xData[10] , xData[0] , xData[1]);
//						isColliding = XArea > areaTest ? true:false;
//						
//						if(isColliding) {
//							
//							didCollide = true;
//							distance = targetData[TRY] - xData[1];
//							E.translate(0f, -distance);							
//
//						} else { //check if E moved past this
//							
//							if(targetData[BLY] >= xData[10]) {
//								
//								didCollide = true;
//								distance = targetData[TLY] - xData[1];
//								E.translate(0f, -distance);							
//																
//							}
//							
//						}
//						
//					} else if (x.isUpperLeftTriangle()) {
//						
//						int XArea = (int)getCoordianteRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]);
//						int areaTest = (int)getCoordianteRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , targetData[TRY]);
//						isColliding = XArea > areaTest ? true:false;
//						
//						if(isColliding) {
//							
//							didCollide = true;
//							distance = targetData[TRY] - xData[1];
//							E.translate(0f, -distance);							
//								
//						} else {  //check if E moved past this
//
//							if(targetData[BLY] >= xData[19]) {
//								
//								didCollide = true;
//								distance = targetData[TLY] - xData[1];
//								E.translate(0f, -distance);		
//								
//							}
//							
//						}
						
//					}
						
				} else if (moveDirection == Direction.DOWN) {
					
					//treat as triangles
					
					if(t.isUpperLeftTriangle()) {
				
						float colliderArea = getTriangleArea(xData);
				   		colliderArea = Math.round(colliderArea);

				   		float targetArea = 0;
				   		targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[BRX] , targetData[BRY] , xData[0] , xData[1]);
				   		targetArea += getTrianglePointArea(targetData[BRX] , targetData[BRY] , xData[18] , xData[19]  , xData[0] , xData[1]);
				   		targetArea += getTrianglePointArea(xData[27] , xData[28], xData[18] , xData[19] , targetData[BRX] , targetData[BRY]);
				   		targetArea = Math.round(targetArea);
				   		isColliding = colliderArea == targetArea;

				   		if(isColliding || xData[28] > targetData[BLY]) {

				   			didCollide = true;
				   			//let V1 = bottom left , V2 = top right
				   			float hypSlope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
				   			float intercept = xData[28] - (hypSlope * xData[27]);				   			
				   			float yValue = (targetData[BRX] * hypSlope) + intercept;
				   			distance = yValue - targetData[BRY];
				   			E.translate(0f, distance);
				   			
				   		} else if (targetData[BRX] > xData[0] && xData[19] > targetData[BRY]) {
				   			
				   			distance = xData[19] - targetData[BRY];
				   			E.translate(0f, distance);
				   			didCollide = true;
				   			
				   		}
						
					} else if (t.isUpperRightTriangle()) {
				
						float colliderArea = getTriangleArea(xData);
						colliderArea = Math.round(colliderArea);
				   		float targetArea = 0;
				   		targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[BLX] , targetData[BLY] , xData[0] , xData[1]);
				   		targetArea += getTrianglePointArea(targetData[BLX] , targetData[BLY] , xData[9] , xData[10]  , xData[0] , xData[1]);
				   		targetArea += getTrianglePointArea(xData[27] , xData[28] , xData[9] , xData[10] , targetData[BLX] , targetData[BLY]);
				   		targetArea = Math.round(targetArea);
				   		
				    	isColliding = colliderArea == targetArea;	
						
						if(isColliding || xData[28] > targetData[BLY]) {

							didCollide = true;
				   			//let V1 = bottom right , V2 = top left
				   			float hypSlope = (xData[10] - xData[1]) / (xData[9] - xData[0]);
				   			float intercept = xData[1] - (hypSlope * xData[0]);				   			
				   			float yValue = (targetData[BLX] * hypSlope) + intercept;
				   			distance = yValue - targetData[BLY];
				   			E.translate(0f, distance);
				   			
				   		} else if (xData[9] >= targetData[BLX] && xData[10] > targetData[BLY]) {
				   			
				   			distance = xData[10] - targetData[BLY];
				   			E.translate(0f, distance);
				   			didCollide = true;
				   			
				   		}
						
					}
				
					//treat as boxes
					
//					else if(x.isLowerLeftTriangle()) {
//						
//						int xArea = (int) getCoordianteRectangleArea(xData[9] , xData[10] , xData[18] , xData[19] , xData[0] , xData[1]);
//						int testArea = (int) getCoordianteRectangleArea(xData[9] , xData[10] , xData[18] , targetData[BRY] , xData[0] , xData[1]);
//						isColliding = xArea > testArea ? true:false;
//						
//						if(isColliding) {
//							
//							didCollide = true;
//							distance = xData[10] - targetData[BRY];
//							E.translate(0f, distance);							
//								
//						} else {  //check if E moved past this
//							
//							if(xData[1] > targetData[TRY]) {
//								
//								didCollide = true;
//								distance = xData[10] - targetData[BRY];
//								E.translate(0f, distance);
//								
//							}
//							
//						}
//						
//					} else if(x.isLowerRightTriangle()) {
//						
//						int xArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , xData[10] , xData[18] , xData[19]);
//						int testArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , targetData[BLY] , xData[18] , xData[19]);
//						isColliding = xArea > testArea ? true:false;
//						
//						if(isColliding) {
//							
//							didCollide = true;
//							distance = xData[10] - targetData[BRY];
//							E.translate(0f, distance);							
//								
//						} else {  //check if E moved past this
//							
//							if(xData[28] > targetData[TLY]) {
//								
//								distance = xData[10] - targetData[BLY];
//								E.translate(0f , distance);
//								didCollide = true;
//								
//							}
//							
//						}
//						
//					}
					
				}
				
			}
			
		} else E.translate(0f, speed);	
		
		comps[Entities.CDOFF + 2] = didCollide;
		if(didCollide && E.type == CSType.PROJECTILE) ((Projectiles)E).onCollide();
		if(didCollide) comps[Entities.VCOFF + 5] = null;
		return didCollide;
		
	}

	/**
	 * Moves E left or right speed, checking for collisions along the way. This scans for triangle collisions and box collisions in one go.
	 * 
	 * @param E — Entity to move
	 * @param speed — amount to move 
	 * @return true if E collided
	 */
	public static final boolean moveHorizChecked(Quads Q , float speed) {
	
		boolean didCollide = false;			
			
		final int 
		TLX , TLY ,
		TRX , TRY ,
		BLX , BLY , 
		BRX , BRY ,
		BY , TY ;

		
		TLX = QuadIndices.TLX;
		TLY = QuadIndices.TLY;
		TRX = QuadIndices.TRX;
		TRY = QuadIndices.TRY;
		BLX = QuadIndices.BLX;
		BLY = QuadIndices.BLY;
		BRX = QuadIndices.BRX;
		BRY = QuadIndices.BRY;
		BY = QuadIndices.BY;
		TY = QuadIndices.TY;
		
		float[] targetData = Q.getData();
		float[] startingMid = Q.getMidpoint();
	
		float scanningDistance = 10000f;
		Direction moveDirection = speed > 0f ? Direction.RIGHT : Direction.LEFT;

		ArrayList<Colliders> validBoxes = new ArrayList<Colliders>();
		ArrayList<Colliders> validTriangles = new ArrayList<Colliders>();
		float[] xData;
		
		//checks for and removes invalid colliders
		CSLinked<Colliders> allColliders = ColliderLists.getComposite();
		
		cdNode<Colliders> iter = allColliders.get(0);
		Colliders x;
		for(int i = 0 ; i < allColliders.size() ; i ++ , iter = iter.next) {
			
			x = iter.val;
			xData = x.getData();
			
			float[] xMid = x.getMidpoint(); //if the collider is out of range skip it.
			if((Math.abs(startingMid[0] - xMid[0]) >= scanningDistance || Math.abs(startingMid[1] - xMid[1]) >= scanningDistance)
				 ||(Math.abs(xMid[0] - startingMid[0]) >= scanningDistance || Math.abs(xMid[1] - startingMid[1]) >= scanningDistance)) continue;
			
			if(!x.isTriangle()) {				
				
				
				if((moveDirection == Direction.LEFT && x.getMidpoint()[0] > startingMid[0])//if its behind the subject 
				 ||(moveDirection == Direction.RIGHT&& x.getMidpoint()[0] < startingMid[0])
					 // if its above or below target
				 ||(targetData[BRY] >= xData[19] || xData[1] >= targetData[TRY]))continue;
				
				validBoxes.add(x);
				
			} else if(x.isTriangle()) {

				//if the triangle is below or above the subject, ignore it					
				if((x.isLowerLeftTriangle() || x.isUpperLeftTriangle()) && (xData[1] >= targetData[TRY] || targetData[BRY] >= xData[19])) continue;					
				if ((x.isLowerRightTriangle() || x.isUpperRightTriangle()) && (xData[28] > targetData[TY] || xData[10] < targetData[BY])) continue;
				//if the triangle is behind the subject, ignore it
				if(x.isUpperRightTriangle() || x.isUpperLeftTriangle()) {
					
					if(moveDirection == Direction.RIGHT && targetData[BLX] >= xData[0]) continue;
					else if (moveDirection == Direction.LEFT && xData[27] >= targetData[BRX]) continue;
					
				} else if (x.isLowerLeftTriangle() || x.isLowerRightTriangle()) {
					
					if(moveDirection == Direction.RIGHT && targetData[BLX] >= xData[18]) continue;
					else if (moveDirection == Direction.LEFT && xData[0] >= targetData[BRX]) continue;
					
				} 
			
				validTriangles.add(x);
				
			}
	
		}
		
//		System.out.println("Number valid triangles: " + validTriangles.size());
		
		Q.translate(speed, 0f);
		
		
		for(Colliders b : validBoxes) {
			
			xData = b.getData();
						
			boolean isColliding = false;
			float distance;
			if(moveDirection == Direction.RIGHT) {
				
				distance = xData[27] - targetData[BRX];
				if(targetData[BLY] < xData[19] && targetData[TLY] > xData[28] && targetData[BLX] < xData[18]){
					
					int XArea = (int) getCoordinateRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]);
					int targetArea = (int)getCoordinateRectangleArea(targetData[BRX] , targetData[BRY] , xData[18] , xData[19] , xData[0] , xData[1]);
					isColliding = XArea > targetArea ? true:false; //only works for rectangles
					
				}
				
			} else {
				
				distance = targetData[BLX] - xData[0];
				if(targetData[BLY] < xData[19] && targetData[TLY] > xData[28] && targetData[BLX] < xData[18] ){

					int XArea = (int) getCoordinateRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]);
					int targetArea = (int)getCoordinateRectangleArea(targetData[BLX] , targetData[BLY] , xData[18] , xData[19] , xData[0] , xData[1]);	
					isColliding = XArea > targetArea ? true:false; 
				
				}

			}
							
			if(isColliding) {
				
				if(moveDirection == Direction.RIGHT) {
					
					Q.translate(distance , 0f);
					didCollide = true;
					continue;
					
				} else {					

					Q.translate(-distance, 0f);
					didCollide = true;
					continue;
					
				}
				
			} else {

				//if this collider moved passed a collider entirely, move it back to its appropriate position.
				if(moveDirection == Direction.RIGHT) if(xData[0] < targetData[BRX]) Q.translate(-distance, 0f);
				else if(moveDirection == Direction.LEFT) if(xData[0] > targetData[BLX]) Q.translate(distance, 0f);
									
			}
									
		}
		
		for(Colliders t : validTriangles) {
			
			xData = t.getData();
			boolean isColliding = false;
			float distance;
			
			if(moveDirection == Direction.LEFT) {
				
				//treat as triangles
				
				if(t.isUpperRightTriangle()) {
					
					float colliderArea = getTriangleArea(xData);
					colliderArea = Math.round(colliderArea);
			   		float targetArea = 0;
			   		targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[BLX] , targetData[BLY] , xData[0] , xData[1]);
			   		targetArea += getTrianglePointArea(targetData[BLX] , targetData[BLY] , xData[9] , xData[10]  , xData[0] , xData[1]);
			   		targetArea += getTrianglePointArea(xData[27] , xData[28] , xData[9] , xData[10] , targetData[BLX] , targetData[BLY]);
			   		targetArea = Math.round(targetArea);

			   		isColliding = colliderArea >= targetArea;
			   		
			   		if(isColliding) {
			   			
			   			//V1 is bottom right , V2 is top left
			   			float slope = (xData[10] - xData[1]) / (xData[9] - xData[0]);
			   			float intercept = xData[1] - (slope * xData[0]);
			   			
			   			float yValue = (slope * targetData[BLX]) + intercept;
			   			distance = yValue - targetData[BLY];
			   			Q.translate(0f, distance);
			   			didCollide = true;
			   			
			   		} else if (xData[27] >= targetData[BRX]) {//if E moved passed this
			   			
			   			distance = xData[10] - targetData[BRY];
			   			Q.translate(0f, distance);
			   			didCollide = true;
			   			
			   		} else if (xData[0] > targetData[BLX] && xData[1] > targetData[BRY]) {//if E collides with the point
						
						distance = targetData[BLX] - xData[0]; 
						Q.translate(distance, 0f);
						didCollide = true;
						
					}
										
				}
				
				else if (t.isLowerRightTriangle()) {
				
					float colliderArea = getLowerTriangleArea(xData);
			    	colliderArea = Math.round(colliderArea);

			    	float targetArea = 0f;
			    	targetArea += getTrianglePointArea(targetData[TLX] , targetData[TLY] , xData[9] , xData[10] , xData[18] , xData[19]);
			    	targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[TLX] , targetData[TLY] , xData[18] , xData[19]);
			    	targetArea += getTrianglePointArea(xData[27] , xData[28] , xData[9] , xData[10] , targetData[TLX] , targetData[TLY]);
			    	targetArea = Math.round(targetArea);
			    	
			    	isColliding = colliderArea >= targetArea;
			    	
			    	if(isColliding) {
			    		
			    		//V1 is bottom left , V2 is top right
			    		float slope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
			    		float intercept = xData[28] - (slope * xData[27]);
			    		
			    		float yValue = (slope * targetData[TLX]) + intercept;
			    		distance = targetData[TLY] - yValue;
			    		Q.translate(0f, -distance);
			    		didCollide = true;
			    		
			    	} else if (xData[27] >= targetData[BRX]) {//if E moved past this
			    		
			    		distance = targetData[TRY] - xData[28];
			    		Q.translate(0f, -distance);
			    		didCollide = true;
			    		
			    	} else if (xData[18] > targetData[BLX] && targetData[TLY] > xData[19] && xData[19] > targetData[BLY]) {//if E is touching point
			    		
			    		distance = xData[18] - targetData[BLX];
			    		Q.translate(-distance, 0f);
			    		didCollide = true;
			    		
			    	}
					
				}
				
				//treat as boxes 
				
//				else if(x.isUpperLeftTriangle()) {
//				
//					int xArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]); 
//					int testArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , targetData[TLX] , xData[1]);
//					isColliding = xArea >= testArea;
//											
//					if(isColliding) {
//						 
//						distance = xData[0] - targetData[BLX];
//						E.translate(-distance, 0f);
//						didCollide = true;
//						
//					} else if (xData[0] >= targetData[BRX]) {	
//						
//						distance = xData[0] - targetData[BLX];
//						E.translate(distance, 0f);
//						didCollide = true;
//						
//					} 
//					
//				}
//				
//				else if (x.isLowerLeftTriangle()) {
//					
//					int xArea = (int) getCoordianteRectangleArea(xData[9] , xData[10] , xData[18] , xData[19] , xData[0] , xData[1]);
//					int testArea = (int) getCoordianteRectangleArea(xData[9] , xData[10] , targetData[BLX] , xData[19] ,  xData[0] , xData[1]);
//					isColliding = xArea >= testArea;
//					
//					if(isColliding) {
//						
//						distance = xData[0] - targetData[BLX];
//						E.translate(-distance, 0f);
//						didCollide = true;
//						
//					} else if (xData[9] > targetData[BRX]) {
//						
//						distance = xData[0] - targetData[BLX];
//						E.translate(distance, 0f);
//						didCollide = true;
//						
//					} 
//					
//				}
					
			} else if (moveDirection == Direction.RIGHT) {
				
				//treat as triangles
				
				if(t.isUpperLeftTriangle()) {
					
					float colliderArea = getTriangleArea(xData);
			   		colliderArea = Math.round(colliderArea);

			   		float targetArea = 0;
			   		targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[BRX] , targetData[BRY] , xData[0] , xData[1]);
			   		targetArea += getTrianglePointArea(targetData[BRX] , targetData[BRY] , xData[18] , xData[19]  , xData[0] , xData[1]);
			   		targetArea += getTrianglePointArea(xData[27] , xData[28], xData[18] , xData[19] , targetData[BRX] , targetData[BRY]);
			   		targetArea = Math.round(targetArea);

			   		isColliding = colliderArea >= targetArea; 
			   		
					if(isColliding) {
						//V1 = bottom left , V2 = top right
						float slope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
						float intercept = xData[19] - (slope * xData[18]);
						
						float yValue = (slope * targetData[BRX]) + intercept;
						distance = yValue - targetData[BRY];
						
						Q.translate(0f, distance);
						didCollide = true;
						
					} else if (xData[28] > targetData[BRY] && targetData[BRX] > xData[27]) {//colliding with point
						
						distance = targetData[BRX] - xData[27];
						Q.translate(distance, 0f);
						didCollide = true;
						
					} else if (targetData[BLX] > xData[0]) {
						
						distance = xData[19] - targetData[BLY];
						Q.translate(0f, distance);
						didCollide = true;
						
					}
			   						   		
				}
				
				else if(t.isLowerLeftTriangle()) {
					
					float colliderArea = Math.round(getLowerTriangleArea(xData));
					
			    	float targetArea = getTrianglePointArea(targetData[TRX] , targetData[TRY] , xData[18] , xData[19] , xData[0] , xData[1]);
			    	targetArea += getTrianglePointArea(xData[9] , xData[10] , targetData[TRX] , targetData[TRY]  , xData[0] , xData[1]);
			    	targetArea += getTrianglePointArea(xData[9] , xData[10] , xData[18] , xData[19] , targetData[TRX] , targetData[TRY]);
			    	targetArea = Math.round(targetArea);

			    	isColliding = colliderArea >= targetArea;
			    	
			    	if(isColliding) {
			    		
			    		//V1 = bottom Right , V2 is top left
			    		float slope = (xData[10] - xData[1]) / (xData[9] - xData[0]);
			    		float intercept = xData[1] - (slope * xData[0]);
			    		
			    		float yValue = (targetData[TRX] * slope) + intercept;
			    		distance = targetData[TRY] - yValue;
			    		Q.translate(0f, -distance);
			    		didCollide = true;
			    		
			    	} else if (targetData[BLX] > xData[0]) {//moved past it
			    		
			    		distance = targetData[BRX] - xData[9];
			    		Q.translate(-distance, 0f);
			    		didCollide = true;
			    						    		
			    	} else if (targetData[TLY] >= xData[10] && targetData[BRX] > xData[9]) {//touching its point
						
						distance = targetData[BRX] - xData[9];
						Q.translate(distance, 0f);
						didCollide = true;
						
					}						
			    	
				}
				
				//treat as boxes
				
//				else if(x.isUpperRightTriangle() && xData[27] > targetData[BRX]) {
//				
//					int xArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , xData[10] , xData[0] , xData[1]); 
//					int testArea = (int) getCoordianteRectangleArea(targetData[BRX] , xData[28] , xData[9] , xData[10] , xData[0] , xData[1]);
//					
//					isColliding = xArea > testArea;
//					
//					if(isColliding) {
//																		
//						distance = targetData[BRX] - xData[27];
//						E.translate(distance, 0f);
//						didCollide = true;
//						
//					}						
//					
//				}
//				
//				else if (x.isLowerRightTriangle()) {
//					
//					int xArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , xData[10] , xData[18] , xData[19]);
//					int testArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , targetData[BRX] , xData[10] , xData[18] , xData[19]);
//					
//					isColliding = xArea > testArea;
//					
//					if(isColliding) {
//						
//						distance = targetData[BRX] - xData[27];
//						E.translate(distance, 0f);
//						didCollide = true;
//						
//					} 		
//					
//				}
				
			}
			
		}
	
		return didCollide;
		
	}
	
	/**
	 * Moves E speed, then scans for collisions on valid box and triangle colliders. Operates in three major steps;
	 * First, any invalid colliders are removed from the scan. A series of conditions have to be met in order to be valid.
	 * Next, valid box colliders are scanned. Collisions will be detected no matter E's movespeed, and E will be moved back accordingly
	 * Finally, valid triangles are scanned. E can collide with the point of a triangle or its hypoteneus, and in either case will be moved
	 * back to the nearest point outside of the collider.
	 * 
	 * @param E — entity to move, with collisions 
	 * @param speed — amount of units to move E
	 * @return true if the object collided, false otherwise
	 */
	public static final boolean moveVertChecked(Quads Q , float speed) {
		
		boolean didCollide = false;

		final int 
		TLX , TLY ,
		TRX , TRY ,
		BLX , BLY , 
		BRX , BRY ;

		TLX = QuadIndices.TLX;
		TLY = QuadIndices.TLY;
		TRX = QuadIndices.TRX;
		TRY = QuadIndices.TRY;
		BLX = QuadIndices.BLX;
		BLY = QuadIndices.BLY;
		BRX = QuadIndices.BRX;
		BRY = QuadIndices.BRY;
		
		float[] targetData = Q.getData();
		float[] startingMid = Q.getMidpoint();
		
		float scanningDistance = 10000f;
		Direction moveDirection = speed > 0f ? Direction.UP : Direction.DOWN;
		
		float[] xData;
		ArrayList<Colliders> validBoxes = new ArrayList<Colliders>();
		ArrayList<Colliders> validTriangles = new ArrayList<Colliders>();			
		
		//checks for and removes invalid colliders
		CSLinked<Colliders> allColliders = ColliderLists.getComposite();
		cdNode<Colliders> iter = allColliders.get(0);
		Colliders x;
		for(int i = 0 ; i < allColliders.size() ; i ++ , iter = iter.next) {
			
			x = iter.val;
			float[] xMid = x.getMidpoint();
			//if the collider is too far, ignore it
			if((Math.abs(startingMid[0] - xMid[0]) >= scanningDistance || Math.abs(startingMid[1] - xMid[1]) >= scanningDistance)
	  		 ||(Math.abs(xMid[0] - startingMid[0]) >= scanningDistance || Math.abs(xMid[1] - startingMid[1]) >= scanningDistance)) continue;
			
			//boxes that are to the left or right of the E 
			xData = x.getData();//collider is to the left or right of E
			if(!x.isTriangle()) {
				
				//if the object is behind the subject, ignore it
				if(moveDirection == Direction.UP && xMid[1] < startingMid[1] ||
  				   moveDirection == Direction.DOWN && xMid[1] > startingMid[1]) continue;
			
    			if(targetData[BLX] >= xData[0] || xData[27] >= targetData[BRX]) continue;
    			
    			validBoxes.add(x);
    			
			}
			//triangles that are to the left or right of E or behind it  
			if(x.isLowerRightTriangle() || x.isLowerLeftTriangle()) {
				
				if(xData[9] >= targetData[BRX] || targetData[BLX] >= xData[18]) continue;
				
				if(x.isLowerRightTriangle() && 
			      (moveDirection == Direction.UP && targetData[BLY] >= xData[10]) 
			     ||moveDirection == Direction.DOWN && xData[28] >= targetData[BLY]) continue;
				else if (x.isLowerLeftTriangle() &&
			      (moveDirection == Direction.UP && targetData[BLY] >= xData[10])
			     ||moveDirection == Direction.DOWN && xData[1] >= xData[1]) continue;
				
				validTriangles.add(x);
				
			} 
			else if (x.isUpperLeftTriangle() || x.isUpperRightTriangle()) {
				
				if(targetData[BLX] >= xData[0] || xData[27] >= targetData[BRX]) continue;
				
				if(x.isUpperLeftTriangle() &&
				  (moveDirection == Direction.UP && targetData[BLY] >= xData[28])
				||(moveDirection == Direction.DOWN && xData[28] >= targetData[TLY])) continue; 	
				else if (x.isUpperRightTriangle() &&
				  (moveDirection == Direction.UP && targetData[BLY] >= xData[28])
				||(moveDirection == Direction.DOWN &&  xData[28] >= targetData[TLY])) continue;
				
				validTriangles.add(x);
				
			}
		    			
		}
		
		Q.translate(0f, speed);
		
		for(Colliders b : validBoxes) {
			
			xData = b.getData();				
			boolean isColliding = false;
			
			float distance;
			if(moveDirection == Direction.UP) distance = xData[1] - targetData[TRY];
			else distance = targetData[BRY] - xData[19];

			if(targetData[BLY] < xData[19] && targetData[TLY] > xData[28]){
				
				if(targetData[BLX] < xData[18] ){
					
					int XArea = (int) getCoordinateRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]);
					int targetArea = (int)getCoordinateRectangleArea(targetData[BRX] , targetData[BRY] , xData[18] , xData[19] , xData[0] , xData[1]);
					isColliding = XArea > targetArea ? true:false;
				
				}
				
			}
			
			if(isColliding) {
				
				didCollide = true;
				
				if(moveDirection == Direction.UP) {
					
					Q.translate(0f, distance);
					break;
					
				} else {
					
					Q.translate(0f, -distance);
					break;
					
				}					
				
			} else {
				
				if(moveDirection == Direction.UP) {
					
					if(targetData[BRY] > xData[1]) {
						
						Q.translate(0f, -distance);
						didCollide = true;
						continue;
						
					}
					
				} else {
					
					if(xData[19] > targetData[TRY]) {
						
						Q.translate(0f, distance);
						didCollide = true;
						continue;
						
					}						
					
				}					
				
			}
					
		}
		
		//triangles ahead
		
		for(Colliders t : validTriangles) {
			
			boolean isColliding = false;
			float distance;
			xData = t.getData();						
			
			if(moveDirection == Direction.UP) {
				
				// treat as triangles

				if(t.isLowerLeftTriangle()) {
					
			    	float colliderArea = getLowerTriangleArea(xData);
			    	colliderArea = Math.round(colliderArea);

			    	float targetArea = 0f;
			    	//target's top right vertex replaces A
			    	targetArea += getTrianglePointArea(targetData[TRX] , targetData[TRY] ,  xData[18] , xData[19] , xData[0] , xData[1]);
			    	//targets top right vertex replaces B
			    	targetArea += getTrianglePointArea(xData[9] , xData[10] , targetData[TRX] , targetData[TRY] , xData[0] , xData[1]);
			    	//target's top right vertex replaces C
			    	targetArea += getTrianglePointArea(xData[9] , xData[10] , xData[18] , xData[19] , targetData[TRX] , targetData[TRY]);
			    	targetArea = Math.round(targetArea);

			    	isColliding = colliderArea == targetArea;

			    	if(isColliding && xData[0] > targetData[BRX]) {				    	
			    		
			    		didCollide = true;
			    		//slope of hypoteneus V1 is top left,  V2 is bottom right
			    		float triHypSlope = (xData[1] - xData[10]) / (xData[0] - xData[9]);//y2 - y1 / x2 - x1
			    		float mx = triHypSlope * xData[9]; //use slope and V1 to find intercept;
			    		float intercept = xData[10] - mx;
			    		//triangle hypoteneus formula = tryHypeSlope * x + intercept
			    		//plug in target's top right x to get the y value on the hyp line 
			    		//then move back the distance of top right y and distance
			    		float yValue = (targetData[TRX] * triHypSlope) + intercept; 
			    		distance = targetData[TRY] - yValue;
			    		Q.translate(0f, -distance);				    		
			    		
			    	} else if (targetData[TLY] > xData[1] && targetData[BRX] >= xData[0]) {				    		
			    		
			    		distance = targetData[TRY] - xData[1];
			    		Q.translate(0f, -distance);
			    		didCollide = true;				    		
			    		
			    	} else if(targetData[TRY] > xData[19]) {
			    	    
			    		didCollide = true;
			    		float triHypSlope = (xData[1] - xData[10]) / (xData[0] - xData[9]);
			    		float intercept = xData[10] - (triHypSlope * xData[9]);
			    		float yValue = (targetData[TRX] * triHypSlope) + intercept; 
			    		distance = targetData[TRY] - yValue;
			    		Q.translate(0f, -distance);		
			    		
			    	}
			    					    	
				} else if (t.isLowerRightTriangle()) {

			    	float colliderArea = Math.round(getLowerTriangleArea(xData));
			    	
			    	float targetArea = getTrianglePointArea(targetData[TLX] , targetData[TLY] , xData[9] , xData[10] , xData[18] , xData[19]);
			    	targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[TLX] , targetData[TLY] , xData[18] , xData[19]);
			    	targetArea += getTrianglePointArea(xData[27] , xData[28] , xData[9] , xData[10] , targetData[TLX] , targetData[TLY]);
			    	targetArea = Math.round(targetArea);
			    	isColliding = colliderArea == targetArea;	    	
			    	
			    	if(isColliding || targetData[TLY] > xData[10]) {
			    	    
				    	//V1 is bottom left , V2 is top right
				    	float triHypSlope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
				    	float intercept = xData[28] - (triHypSlope * xData[27]);
				    	float yValue = (targetData[TLX] * triHypSlope) + intercept;
				    	distance = targetData[TLY] - yValue;
				    	Q.translate(0f, -distance);
				    	didCollide = true;
				    						    	
			    	} else if (xData[27] >= targetData[TLX] && targetData[TLY] > xData[28]) {
			    		
			    		distance = targetData[TLY] - xData[28];
			    		Q.translate(0f, -distance);
			    		didCollide = true;
			    		
			    	}
					
				}
				
//				//treat as boxes
//				
//				else if(x.isUpperRightTriangle() ) {
//					
//					int XArea = (int)getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , xData[19] , xData[0] , xData[1]);
//					int areaTest = (int)getCoordianteRectangleArea(xData[27] , targetData[TRY] , xData[9] , xData[10] , xData[0] , xData[1]);
//					isColliding = XArea > areaTest ? true:false;
//					
//					if(isColliding) {
//						
//						didCollide = true;
//						distance = targetData[TRY] - xData[1];
//						E.translate(0f, -distance);							
//
//					} else { //check if E moved past this
//						
//						if(targetData[BLY] >= xData[10]) {
//							
//							didCollide = true;
//							distance = targetData[TLY] - xData[1];
//							E.translate(0f, -distance);							
//															
//						}
//						
//					}
//					
//				} else if (x.isUpperLeftTriangle()) {
//					
//					int XArea = (int)getCoordianteRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , xData[1]);
//					int areaTest = (int)getCoordianteRectangleArea(xData[27] , xData[28] , xData[18] , xData[19] , xData[0] , targetData[TRY]);
//					isColliding = XArea > areaTest ? true:false;
//					
//					if(isColliding) {
//						
//						didCollide = true;
//						distance = targetData[TRY] - xData[1];
//						E.translate(0f, -distance);							
//							
//					} else {  //check if E moved past this
//
//						if(targetData[BLY] >= xData[19]) {
//							
//							didCollide = true;
//							distance = targetData[TLY] - xData[1];
//							E.translate(0f, -distance);		
//							
//						}
//						
//					}
					
//				}
					
			} else if (moveDirection == Direction.DOWN) {
				
				//treat as triangles
				
				if(t.isUpperLeftTriangle()) {
			
					float colliderArea = getTriangleArea(xData);
			   		colliderArea = Math.round(colliderArea);

			   		float targetArea = 0;
			   		targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[BRX] , targetData[BRY] , xData[0] , xData[1]);
			   		targetArea += getTrianglePointArea(targetData[BRX] , targetData[BRY] , xData[18] , xData[19]  , xData[0] , xData[1]);
			   		targetArea += getTrianglePointArea(xData[27] , xData[28], xData[18] , xData[19] , targetData[BRX] , targetData[BRY]);
			   		targetArea = Math.round(targetArea);
			   		isColliding = colliderArea == targetArea;

			   		if(isColliding || xData[28] > targetData[BLY]) {

			   			didCollide = true;
			   			//let V1 = bottom left , V2 = top right
			   			float hypSlope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
			   			float intercept = xData[28] - (hypSlope * xData[27]);				   			
			   			float yValue = (targetData[BRX] * hypSlope) + intercept;
			   			distance = yValue - targetData[BRY];
			   			Q.translate(0f, distance);
			   			
			   		} else if (targetData[BRX] > xData[0] && xData[19] > targetData[BRY]) {
			   			
			   			distance = xData[19] - targetData[BRY];
			   			Q.translate(0f, distance);
			   			didCollide = true;
			   			
			   		}
					
				} else if (t.isUpperRightTriangle()) {
			
					float colliderArea = getTriangleArea(xData);
					colliderArea = Math.round(colliderArea);
			   		float targetArea = 0;
			   		targetArea += getTrianglePointArea(xData[27] , xData[28] , targetData[BLX] , targetData[BLY] , xData[0] , xData[1]);
			   		targetArea += getTrianglePointArea(targetData[BLX] , targetData[BLY] , xData[9] , xData[10]  , xData[0] , xData[1]);
			   		targetArea += getTrianglePointArea(xData[27] , xData[28] , xData[9] , xData[10] , targetData[BLX] , targetData[BLY]);
			   		targetArea = Math.round(targetArea);
			   		
			    	isColliding = colliderArea == targetArea;	
					
					if(isColliding || xData[28] > targetData[BLY]) {

						didCollide = true;
			   			//let V1 = bottom right , V2 = top left
			   			float hypSlope = (xData[10] - xData[1]) / (xData[9] - xData[0]);
			   			float intercept = xData[1] - (hypSlope * xData[0]);				   			
			   			float yValue = (targetData[BLX] * hypSlope) + intercept;
			   			distance = yValue - targetData[BLY];
			   			Q.translate(0f, distance);
			   			
			   		} else if (xData[9] >= targetData[BLX] && xData[10] > targetData[BLY]) {
			   			
			   			distance = xData[10] - targetData[BLY];
			   			Q.translate(0f, distance);
			   			didCollide = true;
			   			
			   		}
					
				}
			
				//treat as boxes
				
//				else if(x.isLowerLeftTriangle()) {
//					
//					int xArea = (int) getCoordianteRectangleArea(xData[9] , xData[10] , xData[18] , xData[19] , xData[0] , xData[1]);
//					int testArea = (int) getCoordianteRectangleArea(xData[9] , xData[10] , xData[18] , targetData[BRY] , xData[0] , xData[1]);
//					isColliding = xArea > testArea ? true:false;
//					
//					if(isColliding) {
//						
//						didCollide = true;
//						distance = xData[10] - targetData[BRY];
//						E.translate(0f, distance);							
//							
//					} else {  //check if E moved past this
//						
//						if(xData[1] > targetData[TRY]) {
//							
//							didCollide = true;
//							distance = xData[10] - targetData[BRY];
//							E.translate(0f, distance);
//							
//						}
//						
//					}
//					
//				} else if(x.isLowerRightTriangle()) {
//					
//					int xArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , xData[10] , xData[18] , xData[19]);
//					int testArea = (int) getCoordianteRectangleArea(xData[27] , xData[28] , xData[9] , targetData[BLY] , xData[18] , xData[19]);
//					isColliding = xArea > testArea ? true:false;
//					
//					if(isColliding) {
//						
//						didCollide = true;
//						distance = xData[10] - targetData[BRY];
//						E.translate(0f, distance);							
//							
//					} else {  //check if E moved past this
//						
//						if(xData[28] > targetData[TLY]) {
//							
//							distance = xData[10] - targetData[BLY];
//							E.translate(0f , distance);
//							didCollide = true;
//							
//						}
//						
//					}
//					
//				}
				
			}
			
		}
		
	
		return didCollide;
		
	}
	
	public boolean getPlayAnimations() {
		
		return playAnimations;
		
	}

	/**
	 * 
	 * @param E
	 * @param dir
	 * @param index
	 */
	public static final void animate(Entities E , Direction dir , int index) {
		
		if(E.type == CSType.ENTITY) E.animate(index ,  dir);
		else if (E.type == CSType.PROJECTILE) ((Projectiles)E).animate(dir);
		
	}
	
	public static final SpriteSets getActiveAnim(Entities E) {
		
		if(E.has(ECS.ANIMATIONS)) return ((EntityAnimations)E.components()[Entities.AOFF]).active();		
		return null;
		
	}
	
	public static final int activeAnim(Entities E) {
		
		if(E.has(ECS.ANIMATIONS)) return ((EntityAnimations)E.components()[Entities.AOFF]).activeIndex();
		else return -1;
		
	}
	
	public static final void setActiveAnim(Entities E , int index) {
		
		((EntityAnimations)E.components()[Entities.AOFF]).activate(index);
		
	}
	
	public Entities getEntityByLID(int LID) {
		
		cdNode<Entities> iter = list.get(0);
		for(int i = 0 ; i < list.size() ; i ++ , iter = iter.next) if(iter.val.LID() == LID) return iter.val;
		return null;
		
	}
		
	/**
	 * Gets the floor collider E is currently above as well as its distance.
	 * 
	 * @param E
	 * @return
	 */
	private Tuple2<Colliders , Float> getFloorCollider(Entities E) {
		
		if(!E.has(ECS.COLLISION_DETECTION)) return null;
		Object[] comps = E.components();
		float[] EData = comps[Entities.CDOFF] != null ? (float[]) comps[Entities.CDOFF] : E.getData();
		float[] xData;
		float distance = Float.MAX_VALUE;
		float currentDistance = Float.MAX_VALUE;

		CSLinked<Colliders> allColliders = ColliderLists.getComposite();
		cdNode<Colliders> iter = allColliders.get(0);
		Colliders closest = null;
		Colliders x;

		for(int i = 0 ; i < allColliders.size() ; i ++ , iter = iter.next) {//eliminate noncandidates, then calculate distance		
			
			x = iter.val;
			xData = x.getData();				

			if (x.isLowerLeftTriangle()) continue;				
			else if (x.isLowerRightTriangle()) continue;
			
			//if the object is left or right of the subject, ignore it
			if(!x.isTriangle() || x.isUpperLeftTriangle() || x.isUpperRightTriangle()) 
				if(EData[27] >= xData[0] || xData[27] >= EData[0]) continue;
			
			if(xData[28] >= EData[19]) continue;
			
			//now get the distance
			if(!x.isTriangle()) currentDistance = EData[1] - xData[19];
			
			else if(x.isUpperLeftTriangle()) {
				//V1 = bottom left , V2 = top right
				float slope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
				float intercept = xData[28] - (slope * xData[27]);
				
				currentDistance = EData[1] - ((slope * EData[0])  + intercept); 
				
			} else if (x.isUpperRightTriangle()) {
			
				//V1 = bottom right , V2 = top left
				float slope = (xData[10] - xData[1]) / (xData[9] - xData[0]);
				float intercept = xData[1] - (slope * xData[0]);
				
				currentDistance = EData[28] - ((slope * EData[27])  + intercept);
				
			}
			
			//this collider is closer than the previous one
			if(currentDistance < distance) {
				
				distance = currentDistance;
				closest = x;
				
			}
			
			if(distance == 0) return new Tuple2<>(closest , distance);
			
		}
	
		return new Tuple2<>(closest , distance);
			
	}
	
	/**
	 * Gets and returns this distance of E from the closest valid floor collider. 
	 * 
	 * @param E — entity whose distance from the ground is being queried
	 * @return float representation of distance
	 */
	public static final float getDistanceToFloor(Entities E) {
				
		if(E.has(ECS.COLLISION_DETECTION)) {
						
			var comps = E.components();
			
			float[] EData = comps[Entities.CDOFF] != null ? (float[]) comps[Entities.CDOFF] : E.getData();
			float[] xData;
			float distance = Float.MAX_VALUE;
			float currentDistance = Float.MAX_VALUE;
			
			CSLinked<Colliders> allColliders = ColliderLists.getComposite();
			cdNode<Colliders> iter = allColliders.get(0);
			Colliders x ;
			
			for(int i = 0 ; i < allColliders.size() ; i ++ , iter = iter.next) {//eliminate noncandidates, then calculate distance		
				
				x = iter.val;
				xData = x.getData();				

				if (x.isLowerLeftTriangle()) continue;				
				else if (x.isLowerRightTriangle()) continue;
				
				//if the object is left or right of the subject, ignore it
				if(!x.isTriangle() || x.isUpperLeftTriangle() || x.isUpperRightTriangle()) 
					if(EData[27] >= xData[0] || xData[27] >= EData[0]) continue;
				
				if(xData[28] >= EData[19]) continue;
				
				//now get the distance
				if(!x.isTriangle()) currentDistance = EData[1] - xData[19];
				
				else if(x.isUpperLeftTriangle()) {
					//V1 = bottom left , V2 = top right
					float slope = (xData[19] - xData[28]) / (xData[18] - xData[27]);
					float intercept = xData[28] - (slope * xData[27]);
					
					currentDistance = EData[1] - ((slope * EData[0])  + intercept); 
					
				} else if (x.isUpperRightTriangle()) {
				
					//V1 = bottom right , V2 = top left
					float slope = (xData[10] - xData[1]) / (xData[9] - xData[0]);
					float intercept = xData[1] - (slope * xData[0]);
					
					currentDistance = EData[28] - ((slope * EData[27])  + intercept);
					
				}
				
				distance = distance > currentDistance ? currentDistance : distance;
				if(distance == 0) return distance;
				
			}
		
			return distance;
			
		} else return -1;
		
	}
	
	/**
	 * Returns a direction representing what target is with respect to caller. If target is to the left of caller, left is retured, else
	 * right is returned.
	 * 
	 * @param caller — the entity to orient the check around
	 * @param target — an entity who is either right or left of caller
	 * @return — a direction enum, left or right
	 */
	public static Direction horizontally(Entities caller , Entities target) {
		
		float[] callerMid = caller.getMidpoint();
		float[] targetMid = target.getMidpoint();
		if(callerMid[0] > targetMid[0]) return Direction.LEFT;
		else if (callerMid[0] < targetMid[0]) return Direction.RIGHT;		
		return null;
		
	}
	
	/**
	 * 
	 * Returns a direction representing what target is with respect to caller. If target is below caller, down is returned, else up is returned.
	 * 
	 * @param caller — the entity to orient the check around
	 * @param target — an entity who is either above or below of caller
	 * @return — a direction enum, up or down
	 */
	public static Direction vertically(Entities caller , Entities target) {
		
		if(caller.getMidpoint()[1] > target.getMidpoint()[1]) return Direction.DOWN;
		else return Direction.UP;
		
	}
	
	public static final void toggleAutoOrient(Entities E) {
		
		if(E.has(ECS.DIRECTION)) {
			
			Object[] comps = E.components();
			comps[Entities.DOFF + 2] = toggle((boolean)comps[Entities.DOFF + 2]);
			
		}
		
	}
	
	public static final void setAutoOrient(Entities E , boolean state) {
		
		if(E.has(ECS.DIRECTION)) E.components()[Entities.DOFF + 2] = state;	
		
	}	
	
	/**
	 * Scans for entities within radius from target, and returns the closest one, along with it's distance across the x and y axis. 
	 * If no entity is found from the scan, the record contains null.
	 * 
	 * @param target — entity that is scanning for other entities.
	 * @param scanRadius — radius around this object's midpoint to check entities 
	 * @return record containing the closest entity, the horizontal distance between the entities and the vertical distance between the entities
	 */
	public EntityScanResult nearestEntity(Entities target , float scanRadius) {
		
		float[] center = target.getMidpoint();
		
		Entities closest = null;
		
		float smallestHorizontalDistance = Float.MAX_VALUE;
		float smallestVerticalDistance = Float.MAX_VALUE;
				
		float currentHorizontalDistance = 0f;
		float currentVerticalDistance = 0f;
		
		boolean wasSmallerHoriz = false;
		boolean wasSmallerVert = false;
		
		cdNode<Entities> iter = list.get(0);
		Entities x = iter.val;
		for(int i = 0 ; i < list.size() ; i ++ , iter = iter.next) {
		
			x = iter.val;
			if(x.freeze) continue; 
			if(x == target) continue;
			
			float [] xMid = x.getMidpoint();
			
			wasSmallerHoriz = false;
			wasSmallerVert = false;			
			
			currentHorizontalDistance = xMid[0] - center[0];
			if(Math.abs(currentHorizontalDistance) > scanRadius) continue;
			else if(currentHorizontalDistance < smallestHorizontalDistance) {
				
				wasSmallerHoriz = true;
				smallestHorizontalDistance = currentHorizontalDistance;
				
			}
				
			currentVerticalDistance = xMid[1] - center[1];
			if(Math.abs(currentVerticalDistance) > scanRadius) continue;
			else if(currentVerticalDistance < smallestVerticalDistance) {
				
				wasSmallerVert = true;
				smallestVerticalDistance = currentVerticalDistance;
				
			}
			
			if(wasSmallerHoriz && wasSmallerVert) closest = x;
			
		}
		
		return new EntityScanResult(closest , smallestHorizontalDistance , smallestVerticalDistance);
		
	}
		
	/**
	 * Scans entities and finds the entity closest to the caller within the float radius which has at least one of the vargs components
	 * passed.
	 * 
	 * @param caller — an entity scanning for others
	 * @param scanRadius — a maximum distance something is allowed to be in order to be tested
	 * @param comps — vargs components to test entities with
	 * @return a scan result representing the results of this scan.
	 */
	public EntityScanResult nearestEntityWithAny(Entities caller , float scanRadius , ECS... comps) {
		
		float[] callerMid = caller.getMidpoint();
		Entities closest = null;
		
		float smallestHorizontalDistance = Float.MAX_VALUE;
		float smallestVerticalDistance = Float.MAX_VALUE;
				
		float currentHorizontalDistance = 0f;
		float currentVerticalDistance = 0f;
		
		boolean wasSmallerHoriz = false;
		boolean wasSmallerVert = false;
		
		cdNode<Entities> iter = list.get(0);
		Entities x;
		float[] xMid;
		for(int i = 0 ; i < list.size() ; i ++ , iter = iter.next) {
			
			x = iter.val;
			
			if(x.freeze || x == caller || !x.hasAny(comps)) continue;
			
			
			xMid = x.getMidpoint();
			wasSmallerHoriz = false;
			wasSmallerVert = false;	
		
			//actually try now
			currentHorizontalDistance = xMid[0] - callerMid[0];
			if(Math.abs(currentHorizontalDistance) > scanRadius) continue;
			else if(currentHorizontalDistance < smallestHorizontalDistance) {
				
				wasSmallerHoriz = true;
				smallestHorizontalDistance = currentHorizontalDistance;
				
			}
			
			currentVerticalDistance = xMid[1] - callerMid[1];
			if(Math.abs(currentVerticalDistance) > scanRadius) continue ;
			else if (currentVerticalDistance < smallestVerticalDistance){
				
				wasSmallerVert = true;
				smallestVerticalDistance = currentVerticalDistance;
				
			}
			
			if(wasSmallerHoriz && wasSmallerVert) closest = x;
			
		}
		EntityScanResult res = new EntityScanResult (closest , currentHorizontalDistance , currentVerticalDistance);
		res.setMatchingComps(closest != null ? closest.matching(comps) : null);
		return res;
		
	}
	
	public EntityScanResult nearestEntityWithAll(Entities caller , float radius , ECS... comps){
		
		float[] callerMid = caller.getMidpoint();
		Entities closest = null;
		
		float smallestHorizontalDistance = Float.MAX_VALUE;
		float smallestVerticalDistance = Float.MAX_VALUE;
				
		float currentHorizontalDistance = 0f;
		float currentVerticalDistance = 0f;
		
		boolean wasSmallerHoriz = false;
		boolean wasSmallerVert = false;
		
		cdNode<Entities> iter = list.get(0);
		Entities x = iter.val;
		for(int i = 0 ; i < list.size() ; i ++ , iter = iter.next , x = iter.val) {
			
			if(x.freeze) continue;
			if(x == caller) continue;
			if(!x.has(comps)) continue;
			float[] xMid = x.getMidpoint();
			wasSmallerHoriz = false;
			wasSmallerVert = false;	
		
			//actually try now
			currentHorizontalDistance = xMid[0] - callerMid[0];
			if(Math.abs(currentHorizontalDistance) > radius) continue;
			else if(currentHorizontalDistance < smallestHorizontalDistance) {
				
				wasSmallerHoriz = true;
				smallestHorizontalDistance = currentHorizontalDistance;
				
			}
			
			currentVerticalDistance = xMid[1] - callerMid[1];
			if(Math.abs(currentVerticalDistance) > radius) continue ;
			else if (currentVerticalDistance < smallestVerticalDistance){
				
				wasSmallerVert = true;
				smallestVerticalDistance = currentVerticalDistance;
				
			}
			
			if(wasSmallerHoriz && wasSmallerVert) closest = x;
			
		}
		
		EntityScanResult res = new EntityScanResult (closest , currentHorizontalDistance , currentVerticalDistance);
		res.setMatchingComps(closest != null ? closest.matching(comps) : null);
		return res;
		
	}

	public record hitboxScan(
		
		boolean collided ,
		int callerHot , 
		int callerCold , 
		int callerActive ,
		int targetHot , 
		int targetCold ,
		int targetActive
				
	) {
		
		public String toString() {
			
			String str = "collided: " + collided;
			if(collided) {
				
				str += "\ncaller hot: " + callerHot + "\ncaller cold: " + callerCold + "\ncaller active: " + callerActive + 
					   "\ntarget hot: " + targetHot + "\ntargetCold: " + targetCold + "\ntarget active: " + targetActive;
								
				
			}
			
			return str;
			
		}
		
		public boolean isCallerHot() {
			
			return callerHot != -1;
			
		}

		public boolean isCallerCold() {
			
			return callerCold != -1;
			
		}

		public boolean isTargetHot() {
			
			return targetHot != -1;
			
		}

		public boolean isTargetCold() {
			
			return targetCold != -1;
			
		}
		
	}
	
	public static final void activateHitBox(Entities E , int index) {
		
		if(E.has(ECS.HITBOXES)) ((EntityHitBoxes)E.components()[ECS.HITBOXES.offset]).activate(index);
		
	}
	
	/**
	 * Iterates through each entity's hit boxes, returning the results of the scan in the form of a record; <br>
	 * boolean collided = true if two hitboxes are colliding <br>
	 * int callerHot will be positive if the caller's hitbox found to be colliding is hot, otherwise it's -1 <br>
	 * int callerCold will be positive if the caller's hitbox found to be colliding is cold, otherwise it's -1 <br>
	 * int targetHot will be positive if the target's hitbox found to be colliding is hot, otherwise it's -1 <br>
	 * int targetCold will be positive if the target's hitbox found to be colliding is cold, otherwise it's -1 <br>
	 * 
	 * @param caller — entity scanning for other hitboxes
	 * @param target — entity whose hitboxes are being queried 
	 * @return hitboxScan record containing info about the nature of the hitbox collision
	 */
	public hitboxScan checkHitBoxes(Entities caller , Entities target) {
		
		if(caller.has(ECS.HITBOXES) && target.has(ECS.HITBOXES)) {
			
			if(target.isFrozen()) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
			
			EntityHitBoxes callerBoxes = (EntityHitBoxes) caller.components()[Entities.HOFF];
			if(callerBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
						
			EntityHitBoxes targetBoxes = ((EntityHitBoxes)target.components()[Entities.HOFF]);
			if(targetBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
						
			float[][] callerActiveSet;
			float[][] targetActiveSet;
			
			callerActiveSet = callerBoxes.getActiveHitBoxes(caller , (Direction) caller.components()[Entities.DOFF]);			
			targetActiveSet = targetBoxes.getActiveHitBoxes(target , (Direction) target.components()[Entities.DOFF]);			

			final int 
			BY , TY ,
			LX , RX ;
			
			BY = QuadIndices.BY;
			TY = QuadIndices.TY;
			LX = QuadIndices.LX;
			RX = QuadIndices.RX;
			
			float callerHeight;
			float targetHeight;
			for(int i = 0 ; i < callerActiveSet.length ; i ++) {
				
				float[] callerBox = callerActiveSet[i];
				callerHeight = CSUtil.BigMixin.getArrayHeight(callerBox);
				
				for(int j = 0 ; j < targetActiveSet.length ; j++) {
					
					float[] targetBox = targetActiveSet[j];
										
					//start with vertical collision:					
					targetHeight = CSUtil.BigMixin.getArrayHeight(targetBox);
					
					//y distance is the distance betwen the bottom of the quad thats above and the top of the quad thats below
					float yDistance;
					if(callerBox[TY] > targetBox[TY]) yDistance = callerBox[BY] - targetBox[TY];
					else yDistance = targetBox[BY] - callerBox[TY];
					
					//collided if the distance between the objects calculated above is less than or equal to sum of the halves of the heights of the objects
					boolean collidedVertical = yDistance <= (callerHeight / 2) + (targetHeight / 2);
									
					boolean collidedHorizontal = false;
					//if caller is to the right of target and targets' right vertex is greater than callers left vertex
					if(callerBox[RX] > targetBox[RX]) if(targetBox[RX] > callerBox[LX]) collidedHorizontal = true;
					//if target is to the right of caller and caller's right vertex is greater than targets left vertex
					else if(targetBox[RX] > callerBox[RX]) if(callerBox[LX] > targetBox[RX]) collidedHorizontal = true;
					
					if(collidedVertical && collidedHorizontal) {
						
						var hb = new hitboxScan(true , callerBoxes.hot(i) , callerBoxes.cold(i) , callerBoxes.active() , targetBoxes.hot(j) , targetBoxes.cold(j) , targetBoxes.active());
						return hb;
						
					}
					
				}
				
			}

		}
		
		var hb = new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
		return hb;
		
	}

	/**
	 * Iterates through each entity's hit boxes, returning the results of the scan in the form of a record; <br>
	 * boolean collided = true if two hitboxes are colliding <br>
	 * int callerHot will be positive if the caller's hitbox found to be colliding is hot, otherwise it's -1 <br>
	 * int callerCold will be positive if the caller's hitbox found to be colliding is cold, otherwise it's -1 <br>
	 * int targetHot will be positive if the target's hitbox found to be colliding is hot, otherwise it's -1 <br>
	 * int targetCold will be positive if the target's hitbox found to be colliding is cold, otherwise it's -1 <br>
	 * 
	 * @param caller — entity scanning for other hitboxes
	 * @param target — entity whose hitboxes are being queried 
	 * @return hitboxScan record containing info about the nature of the hitbox collision
	 */
	public hitboxScan checkHitBoxes(Entities caller , EntityScanResult targetScan) {
		
		Entities target = targetScan.result;
		if(target == null) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
		

		
		if(target.freeze || caller.freeze) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1); 
		
		if(caller.has(ECS.HITBOXES) && target.has(ECS.HITBOXES)) {
			
			EntityHitBoxes callerBoxes = (EntityHitBoxes)caller.components()[Entities.HOFF];
			if(callerBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
			
			EntityHitBoxes targetBoxes = ((EntityHitBoxes)target.components()[Entities.HOFF]);
			if(targetBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
						
			float[][] callerActiveSet;
			if(caller.has(ECS.DIRECTION)) callerActiveSet = callerBoxes.getActiveHitBoxes(caller, (Direction) caller.components()[Entities.DOFF]);
			else callerActiveSet = callerBoxes.getActiveHitBoxes(caller, Direction.RIGHT);
			float[][] targetActiveSet;
			if(target.has(ECS.DIRECTION)) targetActiveSet = targetBoxes.getActiveHitBoxes(target, (Direction) target.components()[Entities.DOFF]);
			else targetActiveSet = targetBoxes.getActiveHitBoxes(target, Direction.RIGHT);
						
			for(int i = 0 ; i < callerActiveSet.length ; i ++) {
				
				float[] y = callerActiveSet[i];
				
				for(int j = 0 ; j < targetActiveSet.length ; j++) {
					
					float[] x = targetActiveSet[j];
					
					if((x[28] < y[19] && x[10] > y[28]) && (x[27] < y[18] && y[27] < x[0])){
											
						int targetArea = (int) getCoordinateRectangleArea(x[27] , x[28] , x[18] , x[19] , x[0] , x[1]);
						int callerArea = (int)getCoordinateRectangleArea(y[0] , y[1] , x[18] , x[19] , x[0] , x[1]);
						if(targetArea > callerArea) {
							
							var hb = new hitboxScan(true , callerBoxes.hot(i) , callerBoxes.cold(i) , callerBoxes.active() , targetBoxes.hot(j) , targetBoxes.cold(j) , targetBoxes.active());
							return hb;
							
						}
					
					}
					
				}
				
			}
				
		}
				
		var hb = new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
		return hb;
		
	
		
	}

	/**
	 * Iterates through each entity's hit boxes, returning the results of the scan in the form of a record; <br>
	 * boolean collided = true if two hitboxes are colliding <br>
	 * int callerHot will be positive if the caller's hitbox found to be colliding is hot, otherwise it's -1 <br>
	 * int callerCold will be positive if the caller's hitbox found to be colliding is cold, otherwise it's -1 <br>
	 * int targetHot will be positive if the target's hitbox found to be colliding is hot, otherwise it's -1 <br>
	 * int targetCold will be positive if the target's hitbox found to be colliding is cold, otherwise it's -1 <br>
	 * 
	 * @param caller — entity scanning for other hitboxes
	 * @param target — entity whose hitboxes are being queried 
	 * @return hitboxScan record containing info about the nature of the hitbox collision
	 */
	public hitboxScan checkHitBoxes(Entities caller , float radius) {
		
		if(caller.freeze || !caller.has(ECS.HITBOXES)) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1); 
		
		EntityHitBoxes callerBoxes = (EntityHitBoxes)caller.components()[Entities.HOFF];
		if(callerBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
		float[] callerMid = caller.getMidpoint();
		float[][] callerActiveSet = callerBoxes.getActiveHitBoxes(caller, (Direction) caller.components()[Entities.DOFF]);
		
		cdNode<Entities> iter = list.get(0);
		Entities E;
		for(int e = 0 ; e < list.size(); e ++ , iter = iter.next) {
			
			E = iter.val;
			float[] iterMid = E.getMidpoint();
			
			if(E.equals(caller) || !E.has(ECS.HITBOXES) || (iterMid[0] - callerMid[0]) > radius || iterMid[1] - callerMid[1] > radius) continue;
			
			EntityHitBoxes targetBoxes = ((EntityHitBoxes)E.components()[Entities.HOFF]);
			if(targetBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
			
			float[][] targetActiveSet = targetBoxes.getActiveHitBoxes(E, (Direction) E.components()[Entities.DOFF]);			
			
			for(int i = 0 ; i < callerActiveSet.length ; i ++) {
				
				float[] y = callerActiveSet[i];
				
				for(int j = 0 ; j < targetActiveSet.length ; j++) {
					
					float[] x = targetActiveSet[j];
					
					if((x[28] < y[19] && x[10] > y[28]) && (x[27] < y[18] && y[27] < x[0])){
						
						int targetArea = (int) getCoordinateRectangleArea(x[27] , x[28] , x[18] , x[19] , x[0] , x[1]);
						int callerArea = (int)getCoordinateRectangleArea(y[0] , y[1] , x[18] , x[19] , x[0] , x[1]);
						if(targetArea > callerArea) {
							
							var hb = new hitboxScan(true , callerBoxes.hot(i) , callerBoxes.cold(i) , callerBoxes.active() , targetBoxes.hot(j) , targetBoxes.cold(j) , targetBoxes.active());
							return hb;
							
						}
						
					}
					
				}
				
			}
						
		}
					
		var hb = new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
		return hb;
		
	}
	
	public void updateHitBoxes(Entities E) {
		
		if(E.has(ECS.HITBOXES))((EntityHitBoxes)E.components()[Entities.HOFF]).setupActive(E , (Direction)E.components()[Entities.DOFF]);
		
	}
	
	public void overrideEntityAnimation(Entities E , int animIndex , int frameIndex , Direction animDirection) {
		
		if(E.has(ECS.ANIMATIONS)) {
			
			((SpriteSets[]) E.components()[ECS.ANIMATIONS.offset])[animIndex].setCurrentSprite(frameIndex);
			E.animate(animIndex , animDirection);
			
		}
		
	}
	
	/**
	 * Searches for unowned items, and if one is colliding with E, E will attempt to acquire it.
	 * 
	 * @param E — an entity trying to pick up items who can also hold items
	 */
	public final void findItems(Entities E , UnownedItems unownedItems) {
		
		if(!E.has(ECS.INVENTORY)) return;
		
		final int 
		BY , TY ,
		LX , RX ;
		
		BY = QuadIndices.BY;
		TY = QuadIndices.TY;
		LX = QuadIndices.LX;
		RX = QuadIndices.RX;
		
		cdNode<Items> iter = unownedItems.iter();		
		float[] caller = E.has(ECS.COLLISION_DETECTION) ? (float[]) E.components()[Entities.CDOFF] : E.getData();
		for(int i = 0 ; i < unownedItems.size() ; i ++ , iter = iter.next) {
			
			float[] target = iter.val.getData();
			
			float yDistance;
			if(caller[TY] > target[TY]) yDistance = caller[BY] - target[TY];
			else yDistance = target[BY] - caller[TY];
			
			boolean collidedVertical = yDistance <= (E.getHeight() / 2) + (iter.val.getHeight() / 2);							
			boolean collidedHorizontal = false;
			if(caller[RX] > target[RX]) if(target[RX] > caller[LX]) collidedHorizontal = true;
			else if(target[RX] > caller[RX]) if(caller[LX] > target[RX]) collidedHorizontal = true;
			
			if(collidedVertical && collidedHorizontal) {

				Inventories inventory = (Inventories) E.components()[Entities.IOFF];
				inventory.acquire(iter.val);
				unownedItems.removeItem(iter);
				return;
				
			}
			
		}
		
	}
	
	public final void findItemsByFlag(Entities E , UnownedItems unownedItems , String... flags) {
		
		if(!E.has(ECS.INVENTORY)) return;

		final int 
		BY , TY ,
		LX , RX ;
		
		BY = QuadIndices.BY;
		TY = QuadIndices.TY;
		LX = QuadIndices.LX;
		RX = QuadIndices.RX;
		
		cdNode<Items> iter = unownedItems.iter();		
		float[] caller = E.has(ECS.COLLISION_DETECTION) ? (float[]) E.components()[Entities.CDOFF] : E.getData();
		for(int i = 0 ; i < unownedItems.size() ; i ++ , iter = iter.next) if(iter.val.componentData().hasAnyFlags(flags)) {
			
			float[] target = iter.val.getData();
			
			float yDistance;
			if(caller[TY] > target[TY]) yDistance = caller[BY] - target[TY];
			else yDistance = target[BY] - caller[TY];
			
			boolean collidedVertical = yDistance <= (E.getHeight() / 2) + (iter.val.getHeight() / 2);							
			boolean collidedHorizontal = false;
			if(caller[RX] > target[RX]) if(target[RX] > caller[LX]) collidedHorizontal = true;
			else if(target[RX] > caller[RX]) if(caller[LX] > target[RX]) collidedHorizontal = true;
			
			if(collidedVertical && collidedHorizontal) {

				Inventories inventory = (Inventories) E.components()[Entities.IOFF];
				inventory.acquire(iter.val);
				unownedItems.removeItem(iter);
				return;
				
			}
			
		}
		
	}
	
	public static final void dropItem() {
		
		//TODO
		
	}
	
	public static final void moveEntityToCollisionBound(Entities E , float additionalX , float additionalY) {
		
		if(E.has(ECS.COLLISION_DETECTION)) {
			
			float[] mid = E.getMidpoint();
			float[] boundMid = getArrayMidpoint(((float[])(E.components()[ECS.COLLISION_DETECTION.offset])));
			E.translate((mid[0] - boundMid[0]) + additionalX , (boundMid[1] - mid[1]) + additionalY);
			
		}
		
	}
	
	private int numberTicks = 0;
	public static int ticksLastSecond;
	
	public void startNumberTicks() {
		
		ticksLastSecond = numberTicks;
		numberTicks = 0; 
		
	}
	
	float framesPerTick;
	
	public float frameInterval() {
		
		return framesPerTick;
		
	}
	
	public int numberTicks() {
		
		return numberTicks;
		
	}

	public int ticksLastSecond() {
		
		return ticksLastSecond;
		
	}
	
	/**
	 * Update function for use in the editor. We need to know the current frame rate because we want to ensure physics occurs
	 * correctly even if the framerate is not 60 or some set-in-stone number.
	 * 
	 * We can divide {@code framesLastSecond} into 60 'ticks.' On frames of a certain interval, we will enter this method and run game logic.
	 * Thus this method gets called 60 times a second, while other procesing like rendering happens as fast as it can.
	 * <br><br>
	 *  
	 * @param start — a function to call before any entities are processed, but only on a frame they will be processed, 
	 * @param inbetween — a function to call between initial physics calls, scripts, and animations, but before finishing physics calls
	 * @param after — a function to call after all entities have been processed.
	 *  
	 */
	public void editorRunSystems(Executor start , Executor inbetween , Executor after) {
		
		if(Engine.secondPassed()) {
			
			startNumberTicks();
			framesPerTick = ((float)(Engine.framesLastSecond())  / 60f);//frames between a tick		
			
		}
		
		float addativeTick = (1f / framesPerTick) % 1; //if the currentFrame is a multiple of this, subtract one from the frameStep
		float frameStep = numberTicks * framesPerTick;				
		if(Engine.currentFrame() % addativeTick == 0) frameStep -= 1;
		
		if(numberTicks == 0 || ((frameStep - Engine.currentFrame() <= 1) && numberTicks < 70) ) {
			
			start.execute();
			Object[] comps;
			
			cdNode<Entities> iter = list.get(0);
			Entities x;			
			for(int i = 0 ; i < list.size() ; i ++) {
				
				x = iter.val;
				if(x.freeze) continue;
				iter = iter.next;
				comps = x.components();
				
				//initial entity physics calls
				if(x.has(ECS.VERTICAL_DISPLACEMENT)) setVerticalPosition(comps);
				if(x.has(ECS.HORIZONTAL_DISPLACEMENT)) setHorizontalPosition(comps);
				if(x.has(ECS.GRAVITY_CONSTANT)) gravity(comps);
				
				//game logic calls
				playerInterface(comps);
				if(x.has(ECS.SCRIPT)) script(comps);				
				if(x.has(ECS.ANIMATIONS)) animator(comps);				
				if(x.has(ECS.CAMERA_TRACK)) cameraTrack(comps);
			
			}
			
			//physics
			inbetween.execute();
			
			iter = list.get(0);
			for(int i = 0 ; i < list.size() ; i ++) {
			
				x = iter.val;
				iter = iter.next;
				comps = x.components();
				
				//concluding entitiy physics calls
				if(x.has(ECS.VERTICAL_DISPLACEMENT)) verticalDisplacement(comps);
				if(x.has(ECS.HORIZONTAL_DISPLACEMENT)) horizontalDisplacement(comps);	
				if(x.has(ECS.DIRECTION)) directionOrient(comps);
				
			}
			
			after.execute();
			numberTicks++;
			
		}		
			
	}

	public void gameRunSystems(Executor onStart , Executor inBetween , Executor onFinish) {
		
	}
	
	public Entities getEnclosedEntity(EntityScanResult result) {
		
		return result.result;
		
	}
	
	/**
	 * Stores the subject's x midpoint as an initial horiztonal position for calculations that need dispalcement
	 */
	private void setHorizontalPosition(Object[] comps) {
		
		if(((Entities)comps[0]).has(ECS.COLLISION_DETECTION) && comps[Entities.CDOFF] != null) 
			comps[Entities.HDOFF] = getArrayMidpoint((float[])comps[Entities.CDOFF])[0];
		else comps[Entities.HDOFF] = ((Entities)comps[0]).getMidpoint()[0];
		
	}
	
	/**
	 * Calculates displacement over one frame
	 */
	public static void horizontalDisplacement(Object[] comps) {
		
		if(((Entities)comps[0]).has(ECS.COLLISION_DETECTION) && comps[Entities.CDOFF] != null) 
			comps[Entities.HDOFF + 1] = getArrayMidpoint((float[])comps[Entities.CDOFF])[0] - (float)comps[Entities.HDOFF];
		else comps[Entities.HDOFF + 1] = ((Entities)comps[0]).getMidpoint()[0] - (float)comps[Entities.HDOFF];
		
		
	}
	
	/**
	 * Stores the subject's vertical position as a midpoint for calculations that need displacement
	 */
	private void setVerticalPosition(Object[] comps) {
		
		if(((Entities)comps[0]).has(ECS.COLLISION_DETECTION) && comps[Entities.CDOFF] != null) 
			comps[Entities.VDOFF] = getArrayMidpoint((float[])comps[Entities.CDOFF])[1];
		else comps[Entities.VDOFF] = ((Entities)comps[0]).getMidpoint()[1];
		
	}
	
	/**
	 * Calculates vertical displacement over one frame
	 */
	public static void verticalDisplacement(Object[] comps) {
		
		if(((Entities)comps[0]).has(ECS.COLLISION_DETECTION) && comps[Entities.CDOFF] != null) 
			comps[Entities.VDOFF + 1] = getArrayMidpoint((float[])comps[Entities.CDOFF])[1] - (float)comps[Entities.VDOFF];
		else 
			comps[Entities.VDOFF + 1] = ((Entities)comps[0]).getMidpoint()[1] - (float)comps[Entities.VDOFF];
		
	}
			
	/**
	 * The subject will move about the x axis the given speed according to the left and right move inputs. If collisions are enabled for
	 * the subject, collisions will be scanned as well.
	 * TODO : customizable controlls
	 */
	private void horizontalPlayerController(Object[] comps) {

		if((boolean) comps[Entities.HCOFF + 1] == false) return;//if the entity has control
		Entities E = ((Entities)comps[0]);
					
		if(E.has(ECS.COLLISION_DETECTION)) {
			
			if(GLFWWindow.isAPressed()) moveHorizChecked(E , -(float)comps[Entities.HCOFF]);
			if(GLFWWindow.isDPressed()) moveHorizChecked(E , (float)comps[Entities.HCOFF]);
							
		} else {
		
			if(GLFWWindow.isAPressed()) E.translate(-(float) comps[Entities.HCOFF], 0f);
			if(GLFWWindow.isDPressed()) E.translate((float)comps[Entities.HCOFF], 0f);
			
		}			
		
	}

	/**
	 * Applies a constant downward movement to its subject. The downward movement is given by the subject. If the subject
	 * has collisions, they are calculated. 
	 */
	private void gravity(Object[] comps) {
		
		Entities E =  (Entities)comps[0];
		int gravityIndex = Entities.GCOFF;
		int timerIndex = gravityIndex + 1;
		int maxTimerIndex = gravityIndex + 2;
		int velocityIndex = gravityIndex + 3;
		
		if(E.has(ECS.COLLISION_DETECTION , ECS.VERTICAL_DISPLACEMENT)) {
			
			float gravityTimer = (float)comps[timerIndex];
			boolean collided = moveVertChecked(E , (-(float)comps[gravityIndex] + gravityTimer / (float)comps[velocityIndex]));
			if(!(gravityTimer < -2 * (float)comps[maxTimerIndex])) comps[timerIndex] = gravityTimer -1;
			if(collided) comps[timerIndex] = 0f;
							
		} else if (E.has(ECS.COLLISION_DETECTION)) {
			
			moveVertChecked(E , -(float)comps[gravityIndex]);
							
		} else {
			
			E.translate(0f, -(float)comps[gravityIndex]);
							
		}
		
	}
	
	/**
	 * Allows the glfw input to control subjects vertically. They can jump and platform down. If the subject has collisions, they 
	 * are calculated.
	 * 
	 */
	private void verticalPlayerController(Object[] comps) {
		
		if((boolean) comps[Entities.VCOFF + 4] == false) return;//if the entity does not have control
		
		Entities E = (Entities) comps[0];
		
		if(E.has(ECS.COLLISION_DETECTION , ECS.GRAVITY_CONSTANT , ECS.VERTICAL_DISPLACEMENT)) {				
			
			//fall through a platform
			if(GLFWWindow.isSPressed() && GLFWWindow.isSpacePressed()) {
				
				Tuple2<Colliders , Float> floorData = getFloorCollider(E);
				if(floorData.getSecond() < 2.0f && floorData.getFirst().isPlatform()) {
					
					comps[Entities.VCOFF + 5] = floorData.getFirst();
					
				}				
				
			} else if(GLFWWindow.isSpacePressed() && Math.round((float)comps[Entities.VCOFF]) <= 0f) {//start the jump
				
				comps[Entities.VCOFF] = comps[Entities.VCOFF + 1];
				comps[Entities.VCOFF + 3] = true;
				
			}
			
			float jumpTimer = (float) comps[Entities.VCOFF];				
			float jumpHeight = (float) comps[Entities.GCOFF] + (jumpTimer / (float) comps[Entities.VCOFF + 2]);				
			boolean collided = moveVertChecked(E , jumpHeight);

			if(!collided && jumpTimer > (-2 * (float)comps[Entities.VCOFF + 1])){//jump Timer max

				comps[Entities.VCOFF] = (float) comps[Entities.VCOFF] - 1f;

			} else if (collided){
				//guess a number to find if it is the correct time offset for the time variable
				//a value if it divided by the velocity constant of the jump is the negation of the gravity constant.
				for(float i = 0f ; i > -(float)comps[Entities.VCOFF + 1] ; i--) 
					if(i / (float) comps[Entities.VCOFF + 2] == -(float) comps[Entities.GCOFF]) {

					comps[Entities.VCOFF] = i;
					break;

				}				

			}

			if((float)comps[Entities.VCOFF] < 0f) comps[Entities.VCOFF + 3] = false;

		} else if (E.has(ECS.COLLISION_DETECTION)) {

			if(GLFWWindow.isWPressed()) moveVertChecked(E , (float) comps[Entities.VCOFF + 2]);
			if(GLFWWindow.isSPressed()) moveVertChecked(E , -(float)comps[Entities.VCOFF + 2]);

		} else {

			if(GLFWWindow.isWPressed()) E.translate(0f , (float) comps[Entities.VCOFF + 2]);
			if(GLFWWindow.isSPressed()) E.translate(0f , -(float)comps[Entities.VCOFF + 2]);

		}
	
	}
		
	/**
	 * Subject will be controlled by the mouse and keyboard. 
	 */
	private void playerInterface(Object[] comps) {
		
		Entities E = (Entities)comps[0]; 
		
		if(E.has(ECS.HORIZONTAL_PLAYER_CONTROLLER)) horizontalPlayerController(comps);
		if(E.has(ECS.VERTICAL_PLAYER_CONTROLLER)) verticalPlayerController(comps);					
		
	}

	int numberScripts = 0;
	private void script(Object[] comps) {
		
		if(comps[ECS.SCRIPT.offset] != null && (boolean)comps[ECS.SCRIPT.offset + 1]) { 
			
			((EntityScripts)comps[Entities.SOFF]).exec();
			numberScripts ++;
			
		}
		
	}
	
	public void resetScriptCount() {
		
		numberScripts = 0;
		
	}
	
	public int numberScripts() {
		
		return numberScripts;
		
	}
	
	private void cameraTrack(Object[] comps) {
		
		Entities E = (Entities)comps[0];
		
		float[] mid;
		if(comps[ECS.COLLISION_DETECTION.offset] != null) mid = getArrayMidpoint((float[])comps[ECS.COLLISION_DETECTION.offset]);
		else mid = E.getMidpoint();
		
		float horizAdd = (float)comps[ECS.CAMERA_TRACK.offset];
		float vertAdd = (float)comps[ECS.CAMERA_TRACK.offset + 1];
		float zoom = (float)comps[ECS.CAMERA_TRACK.offset + 2];
		
		camera.lookAt(mid[0] + horizAdd , mid[1] + vertAdd);
		camera.setOrthoFactor(zoom);
		
	}

	private void animator(Object[] comps){
		
		if(playAnimations) {

			Entities E = (Entities)comps[0];
			if(E.has(ECS.DIRECTION)) animate(E , (Direction)comps[Entities.DOFF] , -1);
			else animate(E , Direction.RIGHT , -1);
			
		}			
		
	};
	
	private void directionOrient(Object[] comps) {
		
		if((boolean) comps[Entities.DOFF + 2]) {
			
			if(((Entities)comps[0]).has(ECS.HORIZONTAL_DISPLACEMENT)) {
				
				if((float)comps[Entities.HDOFF + 1] > 0) comps[Entities.DOFF] = Direction.RIGHT;
				else if ((float)comps[Entities.HDOFF + 1] < 0) comps[Entities.DOFF] = Direction.LEFT;
				
			}
			
			if(((Entities)comps[0]).has(ECS.VERTICAL_DISPLACEMENT)){
				
				if((float)comps[Entities.VDOFF + 1] > 0) comps[Entities.DOFF + 1] = Direction.UP;
				else if ((float)comps[Entities.VDOFF + 1] < 0) comps[Entities.DOFF + 1] = Direction.DOWN;
				
			}
			
		}			
		
	};
	
	public void clear() {
		
		list.clear();
		
	}
	
}
