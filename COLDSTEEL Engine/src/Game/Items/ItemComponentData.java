package Game.Items;

import org.python.core.PyCode;

import CS.PythonScriptEngine;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.CSOHashMap;
import CSUtil.DataStructures.Tuple2;
import Core.Direction;
import Core.Executor;
import Core.HitBoxSets;
import Core.Entities.Entities;
import Core.Entities.EntityHitBoxes;
import Core.ECS;

public class ItemComponentData {

	private final Items belongsTo;
	
	ItemComponentData(Items belongsTo , ItemOwner owner){
		
		usable.setOwner(owner);
		this.belongsTo = belongsTo;	
				
	}

	ItemComponentData(Items belongsTo){
		
		this.belongsTo = belongsTo;
	
	}
	
	void changeOwner(ItemOwner owner) {
		
		if (usable != null) usable.setOwner(owner);
		
	}
	
	class ItemEquipData extends PythonScriptEngine {

		int equipSlot;
		Executor onEquip;
		Executor onUnequip;		
		String onEquipScript;
		String onUnequipScript;
	
		ItemEquipData(){
			
			super();
			
		}
		
		void applyScripts() {
			
			if(onEquipScript != null && !onEquipScript.equals("null")) {
				
				PyCode onEquipCode = comp(onEquipScript);
				onEquip = () -> run(onEquipCode);
				
			}
			
			if(onUnequipScript != null && !onUnequipScript.equals("null")) {
				
				PyCode onUnequipCode = comp(onUnequipScript);
				onUnequip = () -> run(onUnequipCode);
				
			}			
			
		}
		
	}
		
	class ItemHitBoxes {
		
		EntityHitBoxes hitboxManager;		
		
		ItemHitBoxes() {
			
			hitboxManager = new EntityHitBoxes();
			
		}
		
	}
	
	class ItemMaterials {
		
		//float percentage the item is made up of it and and material percentage refers to 
		CSArray<Tuple2<Float , materials>> ratio = new CSArray<Tuple2<Float , materials>>(4 , 1);
		
		enum materials {
			IRON,
			STEEL,
			WOOD,
						
			;
						
		}
		
	}
	
	class ItemConsumable {
		
		int percentageToConsume;
		
		ItemConsumable(int chance){
			
			percentageToConsume = chance;
			
		}
		
	}
	
	class ItemFlags {
		
		CSOHashMap<String , String> flags;
		
		ItemFlags(){
			
			flags = new CSOHashMap<String , String>(25);
			
		}
		
		void addFlag(String flagName) {
			
			flagName = flagName.toUpperCase().replace(' ', '_');
			flags.add(flagName, flagName);
			
		}
		
		int numberFlags() {
			
			return flags.size();
			
		}
		
	}
	
	ItemEquipData equippable;
	ItemUsable usable;
	ItemHitBoxes hitboxable;
	ItemMaterials materials;
	ItemConsumable consumable;
	ItemFlags flags;
	
	public void setEquippable() {
	
		equippable = new ItemEquipData();
		
	}	
	
	public void setUsable(String scriptNamePath) {
		
		usable = new ItemUsable(belongsTo.owningScene , belongsTo , scriptNamePath);
		
	}
	
	public void setHitboxable() {
		
		 hitboxable = new ItemHitBoxes();
		
	}
	
	public void setMaterials() {
	
		materials = new ItemMaterials();
		
	}	
	
	public void setConsumable() {
		
		consumable = new ItemConsumable(100);
		
	}
	
	public void setFlags() {
		
		flags = new ItemFlags();
		
	}
	
	public void equipSlot(int slot) {
		
		equippable.equipSlot = slot;
		
	}
	
	public int equipSlot() {
		
		return equippable != null ? equippable.equipSlot : -1;
		
	}
	
	public String onEquipScript() {
		
		return equippable != null ? equippable.onEquipScript != null ? CSUtil.BigMixin.toNamePath(equippable.onEquipScript): "null" : "null";
		
	}

	public void onEquipScript(String onEquipScript) {
		
		if(onEquipScript.equals("null")) equippable.onEquipScript = null;
		else equippable.onEquipScript = onEquipScript; 
		
	}

	public String onUnequipScript() {
		
		return equippable != null ? equippable.onUnequipScript != null ? CSUtil.BigMixin.toNamePath(equippable.onUnequipScript) : "null" : "null"; 
		
	}

	public void onUnequipScript(String onUnequip) {
		
		equippable.onUnequipScript = onUnequip;
		
	}
	
	public void onUse(String namePath) {
		
		usable.onUse(namePath);
		
	}
	
	public String useScript() {
		
		return usable != null ? usable.useScriptNamePath != null ? usable.useScriptNamePath : "null" : "null";
		
	}
	
	public EntityHitBoxes HitBoxable(EntityHitBoxes hitboxManager) {
		
		hitboxable.hitboxManager = hitboxManager;
		return hitboxable.hitboxManager;
		
	}
	
	public EntityHitBoxes HitBoxable() {
		
		return hitboxable != null ? hitboxable.hitboxManager : null;
		
	}
	
	public void addHitBox(HitBoxSets addThis) {
		
		hitboxable.hitboxManager.addSet(addThis);
		
	}
	
	public void chanceToConsume(int chance) {
		
		consumable.percentageToConsume = chance;
		
	}

	public int chanceToConsume() {
		
		return consumable != null ? consumable.percentageToConsume : -1;
		
	}
	
	public float[][] getActiveHitBoxes() {
		
		Entities owner = belongsTo.ownerAsEntity();
		if(hitboxable != null && owner.has(ECS.DIRECTION)) {
			
			return hitboxable.hitboxManager.getActiveHitBoxes(owner, (Direction)owner.components()[Entities.DOFF]);
			
		}
		
		return null;
		
	}
	
	/*
	 
	  Methods on Item Components
	  
	 */

	void use() {
	
		if(usable != null) {
			
			usable.onUse.execute();
		
		} else if (CS.COLDSTEEL.DEBUG_CHECKS) throw new AssertionError(belongsTo.name + " attempted to be used but is not usable");
		
	}	
	
	void activateHitBox(int index) {
		//activate a hitbox if the item is 
		hitboxable.hitboxManager.activate(index);
		
	}
	
	public void recompileUseScript() {
		
		usable.recompile();
		
	}
	
	public void recompileOnEquipAndOnUnequipScripts() {
		
		equippable.applyScripts();
		
	}
	
	public void addFlag(String flagName) {
		
		assert flags != null : "ERROR: Item " + belongsTo.name + " does not have FLAGS Component.";
		flags.addFlag(flagName);
		
	}
	
	public int numberFlags() {
		
		assert flags != null : "ERROR: Item " + belongsTo.name + " does not have FLAGS Component.";
		return flags.numberFlags();
		
	}
	
	public CSOHashMap<String , String> getFlagsMap(){
		
		assert flags != null : "ERROR: Item " + belongsTo.name + " does not have FLAGS Component.";
		return flags.flags;
		
	}
	
	public boolean hasFlag(String flagName) {
		
		return belongsTo.has(ItemComponents.FLAGS) && flags.flags.has(flagName = flagName.toUpperCase().replace(' ', '_') , flagName);
				
	}
	
	public boolean hasAnyFlags(String...flags ) {
	
		if(!belongsTo.has(ItemComponents.FLAGS)) return false;
		
		for(String x : flags) {
			
			x = x.toUpperCase().replace(' ', '_');
			if(this.flags.flags.has(x , x)) return true;
			
		}
		
		return false;
		
	}
	
}