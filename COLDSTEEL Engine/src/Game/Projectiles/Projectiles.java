package Game.Projectiles;

import static Game.Projectiles.ProjectileIndices.*;

import Core.CSType;
import Core.Direction;
import Core.ECS;
import Core.HitBoxSets;
import Core.Quads;
import Core.Scene;
import Core.SpriteSets;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;
import Core.Entities.EntityHitBoxes;
import Core.Entities.EntityScripts;
import Renderer.Textures;

public class Projectiles extends Entities {

	EntityAnimations anims;
	EntityScripts script;
	EntityHitBoxes hitboxes;	
	Scene scene;
	
	/**
	 * position array is a field of vertices representing the object's position. It is not drawn upon, but only used for game logic 
	 */
	float[] position = {
		
		-100 ,  100 , 	//top left
		 100 ,  100 , 	//top right
		-100 , -100 , 	//bottom left
		 100 , -100 , 	//bottom right
			
	};
	
	public Projectiles(Scene scene , Textures texture , SpriteSets animation , String scriptNamePath){
		
		super("Projectile Entity" , -2 , CSType.PROJECTILE , ECS.ANIMATIONS , ECS.SCRIPT , ECS.HITBOXES);
		
		this.scene = scene;
		
		vertexData = CSUtil.BigMixin.getProjectileVertexArray();
		setTexture(texture);
		
		components()[AOFF] = anims = new EntityAnimations(1);		
		components()[SOFF] = script = new EntityScripts(scene , this , scriptNamePath);
		components()[SOFF + 1] = true;
		components()[HOFF] = hitboxes = new EntityHitBoxes(5);
				
		animation(animation);
		
		setWidth(animation.getWidthOf(0));
		setHeight(animation.getHeightOf(0));
		
		script.set("pLib", scene.projectileScriptingInterface());
		script.call(ProjectileScriptingInterface.PROJECTILE_SCRIPTING_FACADE);
		script.set("P" , this);
		
		
	}

	public Projectiles toggleCollisions() {
		
		if(has(ECS.COLLISION_DETECTION)) removeComponents(ECS.COLLISION_DETECTION);
		else addComponents(ECS.COLLISION_DETECTION);		
		return this;
		
	}
	
	public boolean hasCollisions() {
		
		return has(ECS.COLLISION_DETECTION);
		
	}
	
	public Projectiles toggleDirection() {
				
		if(has(ECS.DIRECTION)) removeComponents(ECS.HORIZONTAL_DISPLACEMENT , ECS.VERTICAL_DISPLACEMENT , ECS.DIRECTION);
		else { 
			
			addComponents(ECS.HORIZONTAL_DISPLACEMENT , ECS.VERTICAL_DISPLACEMENT , ECS.DIRECTION);
			components()[HDOFF] = 0f;
			components()[HDOFF + 1] = 0f;
			components()[VDOFF] = 0f;
			components()[VDOFF + 1] = 0f;
			components()[DOFF + 2] = false; 
			
			
		}
		return this;
		
	}
	
	public boolean hasDirection() {
		
		return has(ECS.DIRECTION);
		
	}
	
	public Projectiles horizDirection(Direction horiz) {
		
		components()[DOFF] = horiz;
		return this;
		
	}
	
	public Projectiles vertDirection(Direction vert) {
		
		components()[DOFF + 1] = vert;
		return this;
		
	}
	
	public Direction horizDirection() {
		
		return (Direction)components()[DOFF];
		
	}

	public Direction vertDirection() {
		
		return (Direction)components()[DOFF + 1];
		
	}
	
	public void translate(float x, float y) {
		
		translation.translate(x , y , 0);
		position[TLX] += x; position[TLY] += y;		
		position[TRX] += x; position[TRY] += y;
		position[BLX] += x; position[BLY] += y;
		position[BRX] += x; position[BRY] += y;
		
	}

	public float[] getMidpoint() {

    	float widthwiseMidpoint = (position[BRX] - position[BLX]) / 2;
    	float heightwiseMidpoint = (position[TLY] - position[BLY]) /2;
    	float[] midpointCoords = {position[BLX] + widthwiseMidpoint , position[BLY] + heightwiseMidpoint};
    	return midpointCoords;

    }
	
	/**
	 * Rotates this object by {@code degrees} degrees.
	 * 
	 * @param degrees — number of degrees to rotate
	 * 
	 */
	public void rotate(float degrees) {
		
		super.rotate(degrees);
		
	}
	
	public int selectProjectile(float cursorX , float cursorY) {
	
		return cursorX >= position[TLX] && cursorX <= position[TRX] && cursorY >= position[BLY] && cursorY <= position[TLY] ? 1 : -1;
		
	}
	
	public void animation(SpriteSets anim) {
		
		anims.setAnimation(0, anim);
		
	}
	
	public void modWidthBi(float mod) {
		
		super.modWidthBi(mod);
		mod /= 2;
		position[BRX] += mod;
		position[TRX] += mod;
		position[BLX] -= mod;
		position[TLX] -= mod;
		
	}
	
	public void modHeightBi(float mod) {
		
		super.modHeightBi(mod);
		mod /= 2;
		position[TLY] += mod;
		position[TRY] += mod;
		position[BLY] -= mod;
		position[BRY] -= mod;
		
	}
	
	public void setWidth(float width) {
		
		float currentWidth = getWidth();
		float widthDifference = width - currentWidth;
		modWidthBi(widthDifference);
		
	}
	
	public void setHeight(float height) {
		
		float currentHeight = getHeight();
		float heightDifference = height - currentHeight;
		modHeightBi(heightDifference);
		
	}
	
	public void fitQuadToTexture() {
		
		if(isTextured()) {
			
			setWidth(texture.width());
			setHeight(texture.height());
			
		}
		
	}
	
	public void moveTo(float x , float y) {
		
		float[] mid = getMidpoint();
		translate(x - mid[0] , y - mid[1]);
		
	}
	
	public void moveTo(Quads other) {
		
		moveTo(other.getMidpoint());
		
	}
	
	public void moveTo(float[] pos) {
		
		moveTo(pos[0] , pos[1]);
		
	}
	
	public void animate(Direction direction) {
		
		if(!anims.animate() || anims.get(0) == null) return;
		
		SpriteSets anim = anims.get(0);
		anims.handleHangup();
		
		//animate
		float[] sprite;
		if(direction == anim.defaultDirection) {
			
			//swap sprite
			sprite = anim.swapSprite(); 
			swapUVs(sprite);
			modWidthBi(sprite[4] - getWidth());
			modHeightBi(sprite[5] - getHeight());
			
		} else {
			
			//swap and flip sprite
			sprite = anim.swapSprite();
			swapAndFlipUVs(sprite);
			modWidthBi(sprite[4] - getWidth());
			modHeightBi(sprite[5] - getHeight());
			
		}
		
		//activates hitbox
		if(has(ECS.HITBOXES) && sprite.length % 3 != 0) hitboxes.activate((int) sprite[sprite.length - 1]);
		
	}
	
	public final float[] getPosition() {
		
		return position;
		
	}
	
	public void hitbox(HitBoxSets hitbox) {
		
		hitboxes.addSet(hitbox);
		hitboxes.activate(0);
		
	}
	
	public SpriteSets getAnimation() {
	
		return anims.get(0);
		
	}	
	
	/**
	 * Call this projectile's onCollide function, specified within its script
	 * 
	 */
	public void onCollide() {
	
		if(CS.COLDSTEEL.DEBUG_CHECKS) {
			
			try {
				
				script.call("onCollide()");
				
			} catch(Exception e) {
				
				try {
					
					throw new AssertionError("Error occured trying to call onCollide");
					
				} catch(AssertionError a) {}
				
				e.printStackTrace();
				
			}
			
		} else script.call("onCollide()");	
		
	}	
	
	/**
	 * Returns a deep copied instance of {@code this}.
	 * 
	 */
	public Projectiles copy() {
	
		Projectiles newProjectile = new Projectiles(scene , texture , anims.get(0).copy() , script.scriptName());
		if(hitboxes.numberSets() > 0) newProjectile.hitbox(hitboxes.get(0).copy());
		if(has(ECS.COLLISION_DETECTION)) newProjectile.addComponents(ECS.COLLISION_DETECTION);
		if(has(ECS.DIRECTION)) newProjectile.toggleDirection();
		newProjectile.components()[DOFF] = components()[DOFF];
		newProjectile.components()[DOFF + 1] = components()[DOFF + 1];
		newProjectile.setFilter(filter.x, filter.y, filter.z);
		newProjectile.removeColor(removedColor.x , removedColor.y , removedColor.z);
		
		newProjectile.moveTo(this);
		
		return newProjectile;
		
	}	
		
}
