package Game.Levels;

import static CS.Engine.INTERNAL_ENGINE_PYTHON;

import java.util.function.Consumer;

import org.python.core.PyCode;

import AudioEngine.SoundEngine;
import AudioEngine.Sounds;
import CSUtil.DataStructures.cdNode;
import Core.Direction;
import Core.ECS;
import Core.Quads;
import Core.Scene;
import Core.Entities.Entities;
import Core.Entities.EntityRPGStats;
import Game.Core.DamageType;
import Game.Core.EntityHurtData;

/**
 * Exposes Engine functionality and variables to trigger scripts 
 * 
 * @author littl
 *
 */
public class TriggerScriptingInterface {
	
	static final PyCode TRIGGER_SCRIPTING_FACADE = INTERNAL_ENGINE_PYTHON().compile("CS_triggerScriptingFunctions.py");
	
	Levels currentLevel;	
	Scene scene;
	
	public TriggerScriptingInterface(Scene scene , Levels currentLevel){
		
		this.currentLevel = currentLevel;
		this.scene = scene;
		
	}
	
	public Entities getEntity(int LID) {
		
		cdNode<Entities> iter = scene.entities().iter();
		for(int i = 0 ; i < scene.entities().size() ; i ++ , iter = iter.next) if(iter.val.LID() == LID) return iter.val;
		return null;
		
	}
	
	public boolean insideConditionAreas(Entities E , Triggers caller) {
		
		return caller.insideConditionBounds(E);
		
	}

	public boolean insideEffectAreas(Entities E , Triggers caller) {
		
		return caller.insideEffectBounds(E);
		
	}

	public boolean insideConditionAreas(int LID , Triggers caller) {
		
		Entities query = scene.entities().getEntityByLID(LID);
		if(query != null) return caller.insideConditionBounds(query);
		return false;
		
	}

	public boolean insideEffectAreas(int LID , Triggers caller) {
		
		Entities query = scene.entities().getEntityByLID(LID);
		if(query != null) return caller.insideEffectBounds(query);
		return false;
		
	}
	
	public void killEntity(Entities E) {
		
		if(E.has(ECS.RPG_STATS)) return;
		
		var killDamage = DamageType.PHYSICAL;
		killDamage.setValue(Float.MAX_VALUE);
		((EntityRPGStats)E.components()[Entities.RPGOFF]).kill(new EntityHurtData(killDamage , 0 , 0));
				
	}

	public void killEntity(int index) {
		
		killEntity(scene.entities().get(index));
		
	}
	
	public boolean isEntityKilled(Entities E) {
			
		return E.has(ECS.RPG_STATS) && ((EntityRPGStats)E.components()[Entities.RPGOFF]).killed();
		
	}

	public boolean isEntityKilled(int index) {
		
		Entities E = scene.entities().get(index);
		return E.has(ECS.RPG_STATS) && ((EntityRPGStats)E.components()[Entities.RPGOFF]).killed();
		
	}
	
	public Direction entityCurrentHorizontalDirection(Entities E) {
		
		return (Direction)E.components()[Entities.DOFF];
		
	}

	public Direction entityCurrentVerticalDirection(Entities E) {
		
		return (Direction)E.components()[Entities.DOFF + 1];
		
	}
	
	public void removeTrigger(Triggers T) {
		
		currentLevel.removeTrigger(T);
		
	}

	public int numberEntities() {
		
		return scene.entities().size();
		
	}
	
	public void hurtEntity(Entities E , EntityHurtData data) {
		
		if(E.has(ECS.RPG_STATS)) {
			
			EntityRPGStats stats = (EntityRPGStats) E.components()[Entities.RPGOFF];
			stats.hurt(data);
			
		}
		
	}
	
	public Quads getConditionArea(Triggers caller , int index) {
		
		return caller.conditionAreas.get(index).val;
		
	}
	
	public Sounds loadSoundFile(String filepath) {
		
		return SoundEngine.add(filepath);
		
	}
		
	public Consumer<Levels> onLevelLoad = (newLevel) -> currentLevel = newLevel;
		
}