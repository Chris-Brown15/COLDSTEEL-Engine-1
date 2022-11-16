package Game.Levels;

import Core.CSType;
import Core.Quads;
import Core.Scene;

import static CSUtil.BigMixin.areColliding;
import static CSUtil.BigMixin.getTriggerConditionFloatArray;
import static CSUtil.BigMixin.getTriggerEffectFloatArray;

import java.util.function.Consumer;

import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;

/**
 * This Class represents triggers in levels. Triggers are generic game affectors that have a collidable area and a script attached to them.
 *   
 * 
 * @author Chris Brown
 *
 */
public class Triggers{
	
	CSLinked<Quads> conditionAreas = new CSLinked<>();
	CSLinked<Quads> effectAreas = new CSLinked<>();
			
	private String name = "UNNAMED";
	private TriggerScripts script;
	Scene owningScene;
		
	public Triggers(Scene owningScene , String name , int ID) {
		
		this.name = name;		
		script = new TriggerScripts(this , name + ".py");
		
	}	
	
	public Quads selectQuads(float x , float y) {
		
		cdNode<Quads> iter = conditionAreas.get(0);
		for(int i = 0 ; i < conditionAreas.size() ; i ++ , iter = iter.next) if(iter.val.selectQuad(x, y) != -1) return iter.val;
		iter = effectAreas.get(0);
		for(int i = 0 ; i < effectAreas.size() ; i ++  , iter = iter.next) if(iter.val.selectQuad(x, y) != -1) return iter.val;
		return null;
		
	}
	
	void callScript() {
		
		script.exec();
		
	}
	
	public void name(String name) {
		
		this.name = name;
		
	}
	
	public Quads addConditionArea() {
		
		Quads newQ = new Quads(getTriggerConditionFloatArray() , conditionAreas.size() , CSType.TRIGGER);
		newQ.makeTranslucent(0.35f);
		conditionAreas.add(newQ);
		newQ.makeTranslucent(0.35f);		
		return newQ;
		
	}

	public Quads addEffectArea() {
		
		Quads newQ = new Quads(getTriggerEffectFloatArray() , effectAreas.size() , CSType.TRIGGER);
		newQ.makeTranslucent(0.35f);
		effectAreas.add(newQ);
		newQ.makeTranslucent(0.35f);
		return newQ;
		
	}
	
	public void remove(Quads x) {
		
		cdNode<Quads> removed = conditionAreas.removeIfHas(x);
		if(removed != null) for(int i = removed.val.getID() ; i < conditionAreas.size() ; i ++ , removed = removed.next) removed.val.decrementID();
		
		if(removed == null) removed = effectAreas.removeIfHas(x);
		if(removed != null) for(int i = removed.val.getID() ; i < effectAreas.size() ; i ++ , removed = removed.next) removed.val.decrementID();
		
	}
	
	public void forEachConditionArea(Consumer<Quads> function) {
		
		conditionAreas.forEachVal(function);
		
	}

	public void forEachEffectArea(Consumer<Quads> function) {
		
		effectAreas.forEachVal(function);
		
	}
			
	public boolean insideConditionBounds(Quads query) {
		
		float[] queryData = query.getData();
		cdNode<Quads> iter = conditionAreas.get(0);
		for(int i = 0 ; i < conditionAreas.size() ; i ++ , iter = iter.next) if(areColliding(queryData , iter.val.getData())) return true;
		return false;
		
	}
	
	public boolean insideEffectBounds(Quads query) {
	
		float[] queryData = query.getData();
		cdNode<Quads> iter = effectAreas.get(0);
		for(int i = 0 ; i < effectAreas.size() ; i ++ , iter = iter.next) if(areColliding(queryData , iter.val.getData())) return true;
		return false;
		
	}

	public String name() {
		
		return name;
		
	}
	
}