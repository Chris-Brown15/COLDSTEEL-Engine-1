package Game.Levels;

import static CSUtil.BigMixin.areColliding;
import static CSUtil.BigMixin.getTriggerConditionFloatArray;

import java.util.Objects;

import Core.CSType;
import Core.Quads;
import Core.Entities.Entities;

/**
 * 
 * Level Load Doors are special triggers that will take the player to a new level when the collision area of the load door is touched.
 * Note that this class does not handle loading of levels, it only tells the engine that it's time to load a new level.
 * 
 * @author Chris Brown
 *
 */
public class LevelLoadDoors {

	Quads conditionArea = new Quads(getTriggerConditionFloatArray() , 0 , CSType.TRIGGER);
	
	{
		conditionArea.makeTranslucent(0.5f);
	}
	
	String thisLoadDoorName = "Unnamed Load Door";	
	String linkedLevel = "null";
	String linkedLoadDoorName = "null";
	
	public LevelLoadDoors(String loadDoorName) {
		
		this.thisLoadDoorName = loadDoorName;
		
	}
	
	public void linkToLevel(String levelName) {
		
		Objects.requireNonNull(levelName);
		this.linkedLevel = levelName;
				
	}
	
	public String linkedLevel() {
		
		return linkedLevel;
		
	}
	
	public float[] conditionAreaSpecs() {
		
		return conditionArea.specs();
		
	}
	
	public boolean withinConditionArea(Entities E) {
		
		return areColliding(E.getData() , conditionArea.getData());
		
	}
	
	public String thisLoadDoorName() {
		
		return thisLoadDoorName;
		
	}

	public void linkToLoadDoor(String name) {
		
		Objects.requireNonNull(name);
		
		this.linkedLoadDoorName = name;
		
	}
	
	public String linkedLoadDoorName() {
		
		return linkedLoadDoorName;
		
	}

	public void modConditionAreaWidth(float mod) {
		
		conditionArea.modWidthBi(mod);
		
	}

	public void modConditionAreaHeight(float mod) {
		
		conditionArea.modHeightUp(mod);
		
	}
	
	public Quads getConditionArea() {
		
		return conditionArea;
		
	}
	
	public boolean hasLinkedDoor() {
		
		return !linkedLoadDoorName.equals("null");
		
	}
	
	public int selectConditionArea(float cursorWorldX , float cursorWorldY) {
		
		return conditionArea.selectQuad(cursorWorldX, cursorWorldY);
		
	}
	
}