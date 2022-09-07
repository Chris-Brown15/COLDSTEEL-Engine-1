package Renderer;

import static CSUtil.QuadIndices.BLX;
import static CSUtil.QuadIndices.BLY;
import static CSUtil.QuadIndices.BRX;
import static CSUtil.QuadIndices.BRY;
import static CSUtil.QuadIndices.TLX;
import static CSUtil.QuadIndices.TLY;
import static CSUtil.QuadIndices.TRX;
import static CSUtil.QuadIndices.TRY;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import Core.Quads;
import Core.Scene;

public class Camera {

	Matrix4f projectionMatrix , viewMatrix;
	public Vector2f cameraPosition;
	public Matrix4f inverseProjectionMatrix = new Matrix4f();
	public Matrix4f inverseViewMatrix = new Matrix4f();
	private float orthoFactor = 32.0f; // how zoomed in we are
	private float leftFactor = 0.0f;
	private float bottomFactor = 0.0f;
	private boolean collisions = false;
	
	public Camera (Vector2f position) {

		cameraPosition = position;
		projectionMatrix = new Matrix4f();
		viewMatrix = new Matrix4f();
		adjustProjectionMatrix();

	}

	public void adjustProjectionMatrix() {

		projectionMatrix.identity();
		projectionMatrix.ortho(leftFactor , orthoFactor * 40.0f , bottomFactor , (orthoFactor * 21.0f) , 0.0f , 100.0f);
		projectionMatrix.invert(inverseProjectionMatrix);

	}

	public float[] getPosition() {
		
		return new float[] {cameraPosition.x , cameraPosition.y};
		
	}
	
	public float getOrthoFactor() {
		
		return orthoFactor;
		
	}
	
	public Matrix4f getViewMatrix() {

		Vector3f cameraFront = new Vector3f(0.0f , 0.0f, -1.0f);
		Vector3f cameraUp = new Vector3f(0.0f , 1.0f , 0.0f);
		viewMatrix.identity();
		viewMatrix = viewMatrix.lookAt(new Vector3f(cameraPosition.x , cameraPosition.y , 50.0f),
								       cameraFront.add(cameraPosition.x , cameraPosition.y , 0.0f),
									   cameraUp);
		viewMatrix.invert(inverseViewMatrix);
		return viewMatrix;

	}
	
	public Matrix4f getViewMatrix(float parallaxOffsetX , float parallaxOffsetY) {

		Vector3f cameraFront = new Vector3f(0.0f , 0.0f, -1.0f);
		Vector3f cameraUp = new Vector3f(0.0f , 1.0f , 0.0f);
		viewMatrix.identity();
		viewMatrix = viewMatrix.lookAt(new Vector3f(cameraPosition.x  * parallaxOffsetX , cameraPosition.y * parallaxOffsetY , 50.0f),
								       cameraFront.add(cameraPosition.x  * parallaxOffsetX , cameraPosition.y * parallaxOffsetY, 0.0f),
									   cameraUp);
		viewMatrix.invert(inverseViewMatrix);
		return viewMatrix;

	}

	public Matrix4f getInverseViewMatrix(float parallaxOffsetX , float parallaxOffsetY){

		Vector3f cameraFront = new Vector3f(0.0f , 0.0f, -1.0f);
		Vector3f cameraUp = new Vector3f(0.0f , 1.0f , 0.0f);
		viewMatrix.identity();
		viewMatrix.lookAt(new Vector3f(cameraPosition.x  * parallaxOffsetX , cameraPosition.y * parallaxOffsetY , 50.0f),
			       cameraFront.add(cameraPosition.x  * parallaxOffsetX , cameraPosition.y * parallaxOffsetY, 0.0f),
				   cameraUp).invert(inverseViewMatrix);
		return inverseViewMatrix;
	}

	public Matrix4f getProjectionMatrix (){

		return projectionMatrix;

	}

	private boolean onScreen(Vector4f vertex , Matrix4f cameraView) {
		
		vertex.mul(cameraView);
		return (vertex.x > -1 && vertex.x < 1) && (vertex.y > -1 && vertex.y < 1);
		
	}
	
	public boolean onScreen(Vector4f vertex) {

		Matrix4f cameraView = new Matrix4f();
		cameraView = projectionMatrix.mul(viewMatrix , cameraView);
		return onScreen(vertex , cameraView);
		
	}
	
	//returns true if this quad is onscren
	public boolean onScreen(Quads q) {
		
		float[] data = q.getData();
		
		Vector4f topLeft = new Vector4f(data[TLX] , data[TLY] , 0 , 1);
		Vector4f topRight = new Vector4f(data[TRX] , data[TRY] , 0 , 1);
		Vector4f bottomLeft = new Vector4f(data[BLX] , data[BLY] , 0 , 1);
		Vector4f bottomRight = new Vector4f(data[BRX] , data[BRY] , 0 , 1);
		
		Matrix4f cameraView = new Matrix4f();
		cameraView = projectionMatrix.mul(viewMatrix , cameraView);
		
		return onScreen(topLeft , cameraView) || onScreen(topRight , cameraView) || onScreen(bottomLeft , cameraView) || onScreen(bottomRight , cameraView);
		
	}

	//returns true if this quad is onscren
	private boolean onScreen(Quads q , Matrix4f cameraView) {
		
		float[] data = q.getData();
		
		Vector4f topLeft = new Vector4f(data[TLX] , data[TLY] , 0 , 1);
		Vector4f topRight = new Vector4f(data[TRX] , data[TRY] , 0 , 1);
		Vector4f bottomLeft = new Vector4f(data[BLX] , data[BLY] , 0 , 1);
		Vector4f bottomRight = new Vector4f(data[BRX] , data[BRY] , 0 , 1);
		
		return onScreen(topLeft , cameraView) || onScreen(topRight , cameraView) || onScreen(bottomLeft , cameraView) || onScreen(bottomRight , cameraView);
		
	}
	
	public void scaleCamera(float scalespeed) {

		orthoFactor += scalespeed;
		adjustProjectionMatrix();

	}

	public void moveCamera(float xmovespeed , float ymovespeed) {

		cameraPosition.x += xmovespeed;
		cameraPosition.y += ymovespeed;

	}

	public void moveCamera(Scene scene , float x , float y) {
		
		if(collisions) {
			
			Matrix4f cameraView = new Matrix4f();
			projectionMatrix.mul(viewMatrix , cameraView);			
			moveCamera(x , y);
			
			//detect if a collider is on screen, and if so, respond (how)
			scene.colliders().forEach(collider -> {
				
				if(onScreen(collider , cameraView)) {
					
					moveCamera(-(x * 2) , -(y * 2));
					return;
					
				}
				
			});
			
		} else moveCamera(x , y);
		
	}
		
	public void lookAt(float x , float y) {
		
		cameraPosition.x = x;
		cameraPosition.y = y;	
		
	}
	
	public void setOrthoFactor(float ortho) {
		
		orthoFactor = ortho;
		adjustProjectionMatrix();
		
	}
	
	public boolean collisions() {
		
		return collisions;
		
	}
	
	public void toggleCollisions() {
		
		collisions = collisions ? false:true;
		
	}
	
}
