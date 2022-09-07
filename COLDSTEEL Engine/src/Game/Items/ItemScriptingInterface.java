package Game.Items;

import org.joml.Math;
import org.python.core.PyCode;

import static CS.Engine.INTERNAL_ENGINE_PYTHON;
import CS.GLFWWindow;
import CSUtil.QuadIndices;
import CSUtil.DataStructures.cdNode;
import Core.Console;
import Core.Direction;
import Core.ECS;
import Core.SpriteSets;
import Core.Entities.Entities;
import Core.Entities.EntityHitBoxes;
import Core.Entities.EntityLists;
import Core.Entities.EntityRPGStats;
import Core.Entities.EntityLists.hitboxScan;
import Game.Core.DamageType;
import Game.Core.EntityHurtData;
import Game.Projectiles.Projectiles;
import Renderer.Renderer;

public class ItemScriptingInterface {
	
	static final PyCode ITEM_SCRIPTING_FACADE = INTERNAL_ENGINE_PYTHON().compile("CS_itemScriptingFunctions.py");;
	
	Console console;
	GLFWWindow window;
	Renderer renderer;
	EntityLists eList;
	
	public ItemScriptingInterface(Console console, GLFWWindow glfw , Renderer renderer , EntityLists eList) {
		
		this.console = console;
		window = glfw; 
		this.renderer = renderer;
		this.eList = eList;
		
	}
	
	public Console getConsole() {
				
		return console;
		
	}

	public GLFWWindow getGLFW() {
		
		return window;
		
	}

	public Renderer getRenderer() {
	
		return renderer; 
		
	}
	
	public void renderItemHitboxes(Items I) {
		
		Entities owner = I.ownerAsEntity();		
		if(I.has(ItemComponents.HITBOXABLE) && owner != null) {
			
			EntityHitBoxes hb = I.componentData().HitBoxable();
			float[][] arr = hb.getActiveHitBoxes(I , (Direction) owner.components() [Entities.DOFF]);
			for(float[] x : arr) renderer.addToRawData(x);
			
		}
		
	}
	
	public void stopRenderItemHitboxes(Items I) {
		
		Entities owner = I.ownerAsEntity();
		if(I.has(ItemComponents.HITBOXABLE) && owner != null) {
			
			EntityHitBoxes hb = I.componentData().HitBoxable();
			float[][] allBoxes = hb.getAllHitBoxes();
			for(float[] x : allBoxes) renderer.removeFromRawData(x);
			
		}
		
	}

	public void activateHitbox(Items I , int index) {
		
		if(I.has(ItemComponents.HITBOXABLE)) I.componentData().hitboxable.hitboxManager.activate(index);
		
	}
	
	public void activateHitboxByAnimation(Items I , SpriteSets anim) {
		
		if(I.has(ItemComponents.HITBOXABLE)) {
			
			float[] activeSprite = anim.getActiveSprite();
			if(activeSprite.length % 3 != 0) I.componentData().hitboxable.hitboxManager.activate((int)activeSprite[activeSprite.length - 1]);
			
		}
		
	}
	
	public EntityOptional findEntity(Items scanner , float scanRadius) {
		
		float[] center = scanner.getMidpoint();
		
		Entities closest = null;
		
		float smallestHorizontalDistance = Float.MAX_VALUE;
		float smallestVerticalDistance = Float.MAX_VALUE;
				
		float currentHorizontalDistance = 0f;
		float currentVerticalDistance = 0f;
		
		cdNode<Entities> iter = eList.iter();
		Entities x = iter.val;		
		float [] xMid;
		for(int i = 0 ; i < eList.size() ; i ++ , iter = iter.next) {
			
			x = iter.val;
			if(x.isFrozen()) continue;
			if(x == scanner.ownerAsEntity()) continue;
			
			xMid = x.getMidpoint();
			
			//out of range
			if(Math.abs(currentHorizontalDistance = center[0] - xMid[0]) > scanRadius || Math.abs(currentVerticalDistance = center[1] - xMid[1]) > scanRadius) continue;
			
			if(currentHorizontalDistance < smallestHorizontalDistance && currentVerticalDistance < smallestVerticalDistance) {
				
				smallestHorizontalDistance = currentHorizontalDistance;
				smallestVerticalDistance = currentVerticalDistance;
				closest = iter.val;
				
			}
						
		}
		
		return new EntityOptional(closest , smallestHorizontalDistance , smallestVerticalDistance);
		
	}
	
	public hitboxScan checkHitBoxes(Items I , EntityOptional entity) {
		
		if(I.has(ItemComponents.HITBOXABLE) && entity.hasEntity() && entity.has(ECS.HITBOXES)) {
			
			if(entity.optional.isFrozen()) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
			EntityHitBoxes callerBoxes = (EntityHitBoxes)I.componentData().HitBoxable();
			if(callerBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
			
			Object[] entityComps = entity.optional.components();
			
			EntityHitBoxes targetBoxes = ((EntityHitBoxes)entityComps[Entities.HOFF]);
			if(targetBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
						
			float[][] callerActiveSet;
			float[][] targetActiveSet;
			
			if(I.ownerAsEntity().has(ECS.DIRECTION)) callerActiveSet = callerBoxes.getActiveHitBoxes(I , (Direction) I.ownerAsEntity().components()[Entities.DOFF]);
			else callerActiveSet = callerBoxes.getActiveHitBoxes(I , null);
			if(entity.optional.has(ECS.DIRECTION)) targetActiveSet = targetBoxes.getActiveHitBoxes(entity.optional , (Direction) entityComps[Entities.DOFF]);
			else targetActiveSet = targetBoxes.getActiveHitBoxes(entity.optional , null);

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
				
				float[] caller = callerActiveSet[i];
				callerHeight = CSUtil.BigMixin.getArrayHeight(caller);
				
				for(int j = 0 ; j < targetActiveSet.length ; j++) {
					
					float[] target = targetActiveSet[j];
										
					//start with vertical collision:					
					targetHeight = CSUtil.BigMixin.getArrayHeight(target);
					
					//y distance is the distance betwen the bottom of the quad thats above and the top of the quad thats below
					float yDistance;
					if(caller[TY] > target[TY]) yDistance = caller[BY] - target[TY];
					else yDistance = target[BY] - caller[TY];
					
					//collided if the distance between the objects calculated above is less than or equal to sum of the halves of the heights of the objects
					boolean collidedVertical = yDistance <= (callerHeight / 2) + (targetHeight / 2);
									
					boolean collidedHorizontal = false;
					//if caller is to the right of target and targets' right vertex is greater than callers left vertex
					if(caller[RX] > target[RX]) if(target[RX] > caller[LX]) collidedHorizontal = true;
					//if target is to the right of caller and caller's right vertex is greater than targets left vertex
					else if(target[RX] > caller[RX]) if(caller[LX] > target[RX]) collidedHorizontal = true;
					
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

	public hitboxScan checkHitBoxes(Items I , Entities target) {
		
		if(I.has(ItemComponents.HITBOXABLE) && target.has(ECS.HITBOXES)) {
			
			if(target.isFrozen()) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
			EntityHitBoxes callerBoxes = (EntityHitBoxes)I.componentData().HitBoxable();
			if(callerBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
			
			Object[] entityComps = target.components();
			
			EntityHitBoxes targetBoxes = ((EntityHitBoxes)entityComps[Entities.HOFF]);
			if(targetBoxes.active() == -1) return new hitboxScan(false , -1 , -1 , -1 , -1 , -1 , -1);
						
			float[][] callerActiveSet;
			float[][] targetActiveSet;
			
			callerActiveSet = callerBoxes.getActiveHitBoxes(I , (Direction) I.ownerAsEntity().components()[Entities.DOFF]);
			targetActiveSet = targetBoxes.getActiveHitBoxes(target , (Direction) entityComps[Entities.DOFF]);

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
				
				float[] caller = callerActiveSet[i];
				callerHeight = CSUtil.BigMixin.getArrayHeight(caller);
				
				for(int j = 0 ; j < targetActiveSet.length ; j++) {
					
					float[] targetBox = targetActiveSet[j];
										
					//start with vertical collision:					
					targetHeight = CSUtil.BigMixin.getArrayHeight(targetBox);
					
					//y distance is the distance betwen the bottom of the quad thats above and the top of the quad thats below
					float yDistance;
					if(caller[TY] > targetBox[TY]) yDistance = caller[BY] - targetBox[TY];
					else yDistance = targetBox[BY] - caller[TY];
					
					//collided if the distance between the objects calculated above is less than or equal to sum of the halves of the heights of the objects
					boolean collidedVertical = yDistance <= (callerHeight / 2) + (targetHeight / 2);
									
					boolean collidedHorizontal = false;
					//if caller is to the right of target and targets' right vertex is greater than callers left vertex
					if(caller[RX] > targetBox[RX]) if(targetBox[RX] > caller[LX]) collidedHorizontal = true;
					//if target is to the right of caller and caller's right vertex is greater than targets left vertex
					else if(targetBox[RX] > caller[RX]) if(caller[LX] > targetBox[RX]) collidedHorizontal = true;
					
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

	public void checkHitBoxesAndHurt(Items I , DamageType... damage) {

		if(!I.has(ItemComponents.HITBOXABLE)) return;
		
		cdNode<Entities> iter = eList.iter();
		if(iter == null) return;
		
		EntityHitBoxes callerBoxes = (EntityHitBoxes)I.componentData().HitBoxable();
		if(callerBoxes.active() == -1) return;
		float[] mid = I.ownerAsEntity().getMidpoint();
		
		Entities current;
		Object[] currentComps;		
		
		float[][] callerActiveSet;
		callerActiveSet = callerBoxes.getActiveHitBoxes(I , (Direction) I.ownerAsEntity().components()[Entities.DOFF]);
		
		final int 
		BY = QuadIndices.BY, TY = QuadIndices.TY,
		LX = QuadIndices.LX, RX = QuadIndices.RX;
		
		float[][] targetActiveSet;
		EntityHitBoxes currentBoxes;
		EntityRPGStats stats;

		float[] callerCurrentBox;
		float callerBoxHeight;

		float[] targetCurrentBox;
		float targetBoxHeight;
		
		for(int e = 0 ; e < eList.size() ; e ++ , iter = iter.next) {
			
			current = iter.val;
			if(current == I.ownerAsEntity() || current.isFrozen() || !current.has(ECS.HITBOXES) || !current.has(ECS.RPG_STATS)) continue;
			
			currentComps = current.components();
			
			currentBoxes = ((EntityHitBoxes)currentComps[Entities.HOFF]);
			if(currentBoxes.active() == -1) continue;
			
			targetActiveSet = currentBoxes.getActiveHitBoxes(current , (Direction) currentComps[Entities.DOFF]);
			stats = (EntityRPGStats)currentComps[Entities.RPGOFF];
			
			for(int i = 0 ; i < callerActiveSet.length ; i ++) {
				
				callerCurrentBox = callerActiveSet[i];
				callerBoxHeight = CSUtil.BigMixin.getArrayHeight(callerCurrentBox);
				
				for(int j = 0 ; j < targetActiveSet.length ; j++) {
					
					targetCurrentBox = targetActiveSet[j];										
					targetBoxHeight = CSUtil.BigMixin.getArrayHeight(targetCurrentBox);
					
					float yDistance;	
					if(callerCurrentBox[TY] > targetCurrentBox[TY]) yDistance = callerCurrentBox[BY] - targetCurrentBox[TY];
					else yDistance = targetCurrentBox[BY] - callerCurrentBox[TY];
					
					boolean collidedVertical = yDistance <= (callerBoxHeight / 2) + (targetBoxHeight / 2);
									
					boolean collidedHorizontal = false;
					if(callerCurrentBox[RX] > targetCurrentBox[RX]) if(targetCurrentBox[RX] > callerCurrentBox[LX]) collidedHorizontal = true;
					else if(targetCurrentBox[RX] > callerCurrentBox[RX]) if(callerCurrentBox[LX] > targetCurrentBox[RX]) collidedHorizontal = true;
					
					if(collidedVertical && collidedHorizontal && callerBoxes.isHot(i) && currentBoxes.isCold(j)) {
						
						for(int h = 0 ; h < damage.length - 1 ; h ++) stats.hurtSansInvincibilityTime(new EntityHurtData(damage[h] , mid[0] , mid[1]));
						stats.hurt(new EntityHurtData(damage[damage.length - 1] , mid[0] , mid[1]));
						
					}
					
				}
				
			}
			
		}
		
	}
	
	public void hurt(Entities caller , EntityOptional scan, DamageType type) {

		float[] mid = caller.getMidpoint();
		((EntityRPGStats) scan.optional.components()[Entities.RPGOFF]).hurt(new EntityHurtData(type , mid[0] , mid[1]));
			
	}
	
	public void hurt(Entities caller , EntityOptional scan , DamageType...types) {
		
		float[] mid = caller.getMidpoint();
		EntityRPGStats scanStats = ((EntityRPGStats) scan.optional.components()[Entities.RPGOFF]);
		for(int i = 0 ; i < types.length - 1 ; i ++) scanStats.hurtSansInvincibilityTime(new EntityHurtData(types[i] , mid[0] , mid[1]));		
		scanStats.hurt(new EntityHurtData(types[types.length - 1] , mid[0] , mid[1]));
		
	}
	
	public void launchProjectile(Projectiles projectile) {
		
		eList.add(projectile);
		
	}
		
}