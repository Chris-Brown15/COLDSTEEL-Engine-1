package Core.Entities;

import static java.util.Objects.requireNonNull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;

import Core.TemporalExecutor;
import Game.Core.EntityHurtData;

/**
 * RPG stats component object for entities. The stats are based on other games as well as my own conceptualizations.
 * 
 * @author Chris Brown
 */
public class EntityRPGStats {
	
	public static final int NUMBER_STATS = 21;
	public static final int NUMBER_SKILLS = 13;
	
	//CHARACTERISTICS
	
	/*
	 
	 PHYSIQUE stats:
	 
	 */
	
	int 
	strength 					= -1 , 
	endurance 					= -1 , 
	dexterity 					= -1 , 
	agility 					= -1 , 
	recovery 					= -1 , 
	resistence 					= -1 , 
	balance 					= -1 ;
	
	/*
	 
	INTELLECT stats:

	 */
	
	int 
	sharpness 					= -1 , 
	morals 						= -1 , 
	charisma 					= -1 , 
	perception 					= -1 , 
	composure 					= -1 , 
	oratory 					= -1 , 
	emotion						= -1 , 
	extraSensoryPerception 		= -1 , 
	luck						= -1 ;
	
	/*
	 
	 ATTUNEMENT stats:
	 
	 */
	
	int 
	vessel 						= -1 , 
	physicalDurability			= -1 , 
	mentalDurability 			= -1 , 
	flow 						= -1 , 
	overload 					= -1 ;

	//LSM:
	
	float 
	currentLife 				= -1 , 
	maxLife 					= -1 , 
	currentStamina 				= -1 , 
	maxStamina 					= -1 , 
	currentMana 				= -1 , 
	maxMana 					= -1 ;
	
	//SKILLS:
	
	int 
	swords 						= -1 , 
	blunts 						= -1 , 
	polearms 					= -1 , 
	bows 						= -1 , 
	crossbows					= -1 , 
	staves 						= -1 , 
	wands 						= -1 , 
	pyromancy 					= -1 , 
	cryomancy 					= -1 , 
	electromancy 				= -1 , 
	heavyArmor 					= -1 , 
	lightArmor 					= -1 , 
	mediumArmor 				= -1 ;
	
	//MISC
	
	double invincibilityTimeMillis = 500;
	private Supplier<Long> invincibilityCooldownUL;	
	private Supplier<Integer> levelIDOffset;
	Consumer<EntityHurtData> onKill;
	Consumer<EntityHurtData> onHurt;
	Entities entity;
	
	public EntityRPGStats(Entities owner) {
		
		levelIDOffset = () -> owner.LID();
		invincibilityCooldownUL = () -> 100000444440000l + levelIDOffset.get();
		entity = owner;
		
	}	
	
	public void hurt(EntityHurtData hurtData) {
	
		if(!TemporalExecutor.coolingDown(invincibilityCooldownUL.get())) {
			
			TemporalExecutor.coolDown(invincibilityTimeMillis, invincibilityCooldownUL.get());			
			currentLife -= hurtData.damageType().value;
			if(killed() && onKill != null) onKill.accept(hurtData);
			else if(onHurt != null) onHurt.accept(hurtData);			
			
		}
		
	}
	
	/**
	 * Hurts the owner of this RPG stats class without causing it to become invincible.
	 * 
	 * @param hurtData — EntityHurtData record
	 */
	public void hurtSansInvincibilityTime(EntityHurtData hurtData) {
		
		currentLife -= hurtData.damageType().value;
		if(killed() && onKill != null) onKill.accept(hurtData);
		else if(onHurt != null) onHurt.accept(hurtData);
		
	}
	
	public void kill(EntityHurtData caller) {
		
		if(onKill != null) onKill.accept(caller);
		
	}
	
	public boolean killed() {
		
		return currentLife < 0;
		
	}
	
	public void heal(float amount) {
		
		if(currentLife + amount <= maxLife) currentLife += amount;
		else currentLife = maxLife;
				
	}
		
	public void onKill(Consumer<EntityHurtData> function) {
		
		this.onKill = (hurtData) -> function.accept(hurtData);		
		
	}
	
	public void onKill(PyObject function) {
		
		this.onKill = (hurtData) -> {
			
			function.__call__(new ClassicPyObjectAdapter().adapt(hurtData));
			
		};
		
	}

	public void onHurt(Consumer<EntityHurtData> function) {
		
		this.onHurt = (hurtData) -> {
			
			function.accept(hurtData);
			
		};		
		
	}
	
	public void onHurt(PyObject function) {
	
		this.onHurt = (hurtData) -> {
			
			function.__call__(new ClassicPyObjectAdapter().adapt(hurtData));
			
		};
		
	}	
	
	/**
	 * Given a name of a characteristic variable, sets it accordingly. 
	 * @param variableName — name of a characteristic, either capitalized or not, but with no spaces, underscores,slashes, etc.
	 * @param value — integer to set this stat to.
	 * @throws IllegalArgumentException if {@code variableName} is not a name of a characteristic.
	 */
	public void setCharacteristicForName(String variableName , int value) {
   
		switch(variableName) {
		
			case "Strength":  case "strength":				
				strength = value;
				break;
				
			case "Endurance": case "endurance": 
				endurance = value;
				break;
				
			case "Dexterity": case "dexterity":
				dexterity = value;
				break;
				
			case "Agility": case "agility":
				agility = value;
				break;
				
			case "Recovery": case "recovery": 
				recovery = value;
				break;
				
			case "Resistence": case "resistence":
				resistence = value;
				break;
				
			case "Balance": case "balance":
				balance = value;
				break;

			case "Sharpness":  case "sharpness":				
				sharpness = value;
				break;
				
			case "Morals": case "morals": 
				morals = value;
				break;
				
			case "Charisma": case "charisma":
				charisma = value;
				break;
				
			case "Perception": case "perception":
				perception = value;
				break;
				
			case "Composure": case "composure": 
				composure = value;
				break;
				
			case "Oratory": case "oratory":
				oratory = value;
				break;
				
			case "Emotion": case "emotion":
				emotion = value;
				break;
				
			case "ExtraSensoryPerception": case "extraSensoryPerception":
				extraSensoryPerception = value;
				break;
				
			case "Luck": case "luck":
				luck = value;
				break;
				
			case "Vessel": case "vessel": 
				vessel = value;
				break;
				
			case "PhysicalDurability": case "physicalDurability":
				physicalDurability = value;
				break;
				
			case "MentalDurability": case "mentalDurability":
				mentalDurability = value;
				break;
				
			case "Flow": case "flow":
				flow = value;
				break;
				
			case "Overload": case "overload":
				overload = value;
				break;
		
			default:
				throw new IllegalArgumentException("Error: " + variableName + " is not a valid name of a characteristic stat.");
				
		}
			
	}
	
	/**
	 * Returns an integer value of a characteristic given the stat's name.
	 * 
	 * @param variableName — String representation of some variable
	 * @return int value of that stat.
	 * @throws IllegalArgumentException if {@code variableName} is not the name of any variable.
	 */
	public int getCharacteristicForName(final String variableName) throws IllegalArgumentException{
		   
		switch(variableName) {
		
			case "Strength": case "strength": return strength;				
			case "Endurance": case "endurance":  return endurance;				
			case "Dexterity": case "dexterity": return dexterity;				
			case "Agility": case "agility": return agility;				
			case "Recovery": case "recovery": return recovery;				
			case "Resistence": case "resistence": return resistence;				
			case "Balance": case "balance": return balance;
			case "Sharpness":  case "sharpness": return sharpness;				
			case "Morals": case "morals":  return morals;				
			case "Charisma": case "charisma": return charisma;				
			case "Perception": case "perception": return perception;				
			case "Composure": case "composure":  return composure;				
			case "Oratory": case "oratory": return oratory;				
			case "Emotion": case "emotion": return emotion;				
			case "ExtraSensoryPerception": case "extraSensoryPerception": return extraSensoryPerception;				
			case "Luck": case "luck": return luck;				
			case "Vessel": case "vessel": return vessel;				
			case "PhysicalDurability": case "physicalDurability": return physicalDurability;				
			case "MentalDurability": case "mentalDurability": return mentalDurability;				
			case "Flow": case "flow": return flow;				
			case "Overload": case "overload": return overload;		
			default: throw new IllegalArgumentException("Error: " + variableName + " is not a valid name of a characteristic stat.");
				
		}
			
	}
	/**
	 * Sets a LSM (life, stamina, mana) stat by its given name. 
	 * @param variableName — name of a LSM stat, either capitalized or not, but with no spaces, underscores,slashes, etc.
	 * @param value — float to set this stat to.
	 * @throws IllegalArgumentException if {@code variableName} is not a name of a characteristic. 
	 * 
	 */
	public void setLSMForName(String variableName , float value) {

		switch(variableName) {
		
			case "CurrentLife": case "currentLife":
				currentLife = value;
				break;

			case "MaxLife": case "maxLife":
				maxLife = value;
				break;

			case "CurrentStamina": case "currentStamina":
				currentStamina = value;
				break;

			case "MaxStamina": case "maxStamina":
				maxStamina = value;
				break;

			case "CurrentMana": case "currentMana":
				currentMana = value;
				break;

			case "MaxMana": case "maxMana":
				maxMana = value;
				break;
		
			default:				
				throw new IllegalArgumentException("Error: " + variableName + " is not a valid name of a characteristic stat.");
				
		}
		
	}
	
	/**
	 * Returns a float value of a LSM stat given the stat's name.
	 * 
	 * @param variableName — String representation of some variable
	 * @return float value of that stat.
	 * @throws IllegalArgumentException if {@code variableName} is not the name of any variable.
	 */
	public float getLSMForName(final String variableName) throws IllegalArgumentException{

		switch(variableName) {
		
			case "CurrentLife": case "currentLife": return currentLife;
			case "MaxLife": case "maxLife": return maxLife;
			case "CurrentStamina": case "currentStamina": return currentStamina;
			case "MaxStamina": case "maxStamina": return maxStamina;
			case "CurrentMana": case "currentMana": return currentMana;
			case "MaxMana": case "maxMana": return maxMana;
			default: throw new IllegalArgumentException("Error: " + variableName + " is not a valid name of a characteristic stat.");
				
		}
		
	}
	
	/**
	 * Sets a skill stat by the given name.
	 * 
	 * @param name — name of a skill stat, either capitalized or not, but with no spaces, underscores,slashes, etc.
	 * @param value — integer to set this stat to.
	 * @throws IllegalArgumentException if {@code variableName} is not a name of a skill.
	 *  
	 */
	public void setSkillForName(String name , int value) {
			
		switch(name) {
		
			case "Swords": case "swords":
				swords = value;
				break;

			case "Blunts": case "blunts":
				blunts = value;
				break;

			case "Polearms": case "polearms":
				polearms = value;
				break;

			case "Bows": case "bows":
				bows = value;
				break;

			case "Crossbows": case "crossbows":
				crossbows = value;
				break;

			case "Staves": case "staves":
				staves = value;
				break;

			case "Wands": case "wands":
				wands = value;
				break;

			case "Pyromancy": case "pyromancy":
				pyromancy = value;
				break;

			case "Cryomancy": case "cryomancy":
				cryomancy = value;
				break;

			case "Electromancy": case "electromancy":
				electromancy = value;
				break;

			case "HeavyArmor": case "heavyArmor":
				heavyArmor = value;
				break;

			case "LightArmor": case "lightArmor":
				lightArmor = value;
				break;

			case "MediumArmor": case "mediumArmor":
				mediumArmor = value;
				break;

			default:				
				throw new IllegalArgumentException("Error: " + name + " is not a valid name of a characteristic stat.");
				
		}
		
	}

	/**
	 * Returns an integer value of a skill stat given the stat's name.
	 * 
	 * @param variableName — String representation of some variable
	 * @return int  value of that stat.
	 * @throws IllegalArgumentException if {@code variableName} is not the name of any variable.
	 */
	public int getSkillForName(String name) throws IllegalArgumentException{
		
		switch(name) {
		
			case "Swords": case "swords": return swords;
			case "Blunts": case "blunts": return blunts;
			case "Polearms": case "polearms": return polearms;
			case "Bows": case "bows": return bows;
			case "Crossbows": case "crossbows": return crossbows;
			case "Staves": case "staves": return staves;
			case "Wands": case "wands": return wands;
			case "Pyromancy": case "pyromancy": return pyromancy;
			case "Cryomancy": case "cryomancy": return cryomancy;
			case "Electromancy": case "electromancy": return electromancy;
			case "HeavyArmor": case "heavyArmor": return heavyArmor;
			case "LightArmor": case "lightArmor": return lightArmor;
			case "MediumArmor": case "mediumArmor": return mediumArmor;
			default: throw new IllegalArgumentException("Error: " + name + " is not a valid name of a characteristic stat.");
				
		}
		
	}
	
	/**
	 * For a stat to be valid, it must be greater than 0.
	 * 
	 * @param function — executed on each physique characteristic, taking its value as input
	 */
	public void forValidPhysique(Consumer<Integer> function) {
		
		if(strength > 0) function.accept(strength);
		if(endurance > 0) function.accept(endurance);
		if(dexterity > 0) function.accept(dexterity);
		if(agility > 0) function.accept(agility);
		if(recovery > 0) function.accept(recovery);
		if(resistence > 0) function.accept(resistence);
		if(balance > 0) function.accept(balance);
		
	}

	/**
	 * For a stat to be valid, it must be greater than 0.
	 * 
	 * @param function — executed on each physique characteristic, taking its value as input
	 */
	public void forValidPhysique(BiConsumer<String , Integer> function) {
		
		if(strength > 0) function.accept("strength" , strength);
		if(endurance > 0) function.accept("endurance" , endurance);
		if(dexterity > 0) function.accept("dexterity" , dexterity);
		if(agility > 0) function.accept("agility" , agility);
		if(recovery > 0) function.accept("recovery" , recovery);
		if(resistence > 0) function.accept("resistence" , resistence);
		if(balance > 0) function.accept("balance" , balance);
		
	}
		
	/**
	 * @param function — executed on each physique characteristic, taking its value as input
	 */
	public void forEachPhysique(Consumer<Integer> function) {
		
		function.accept(strength);
		function.accept(endurance);
		function.accept(dexterity);
		function.accept(agility);
		function.accept(recovery);
		function.accept(resistence);
		function.accept(balance);
		
	}
	
	/**
	 * @param function — executed on each physique characteristic, taking each stat's name and value as input
	 */
	public void forEachPhysique(BiConsumer<String , Integer> function) {
		
		function.accept("strength" , strength);
		function.accept("endurance" , endurance);
		function.accept("dexterity" , dexterity);
		function.accept("agility" , agility);
		function.accept("recovery" , recovery);
		function.accept("resistence" , resistence);
		function.accept("balance" , balance);
		
	}

	/**
	 * @see {@code forValidPhysique} 
	 */
	public void forValidIntellect(Consumer<Integer> function) {

		if(sharpness > 0) function.accept(sharpness);
		if(morals > 0) function.accept(morals);
		if(charisma > 0) function.accept(charisma);
		if(perception > 0) function.accept(perception);
		if(composure > 0) function.accept(composure);
		if(oratory > 0) function.accept(oratory);
		if(emotion > 0) function.accept(emotion);
		if(extraSensoryPerception > 0) function.accept(extraSensoryPerception);
		if(luck > 0) function.accept(luck);
		
	}

	/**
	 * @see {@code forValidPhysique} 
	 */
	public void forValidIntellect(BiConsumer<String , Integer> function) {

		if(sharpness > 0) function.accept("sharpness" , sharpness);
		if(morals > 0) function.accept("morals" , morals);
		if(charisma > 0) function.accept("charisma" , charisma);
		if(perception > 0) function.accept("perception" , perception);
		if(composure > 0) function.accept("composure" , composure);
		if(oratory > 0) function.accept("oratory" , oratory);
		if(emotion > 0) function.accept("emotion" , emotion);
		if(extraSensoryPerception > 0) function.accept("extraSensoryPerception" , extraSensoryPerception);
		if(luck > 0) function.accept("luck" , luck);
		
	}
	
	/**
	 * @see {@code forEachPhysique}
	 */
	public void forEachIntellect(Consumer<Integer> function) {

		function.accept(sharpness);
		function.accept(morals);
		function.accept(charisma);
		function.accept(perception);
		function.accept(composure);
		function.accept(oratory);
		function.accept(emotion);
		function.accept(extraSensoryPerception);
		function.accept(luck);
		
	}

	/**
	 * @see {@code forEachPhysique}
	 */
	public void forEachIntellect(BiConsumer<String , Integer> function) {

		function.accept("sharpness" , sharpness);
		function.accept("morals" , morals);
		function.accept("charisma" , charisma);
		function.accept("perception" , perception);
		function.accept("composure" , composure);
		function.accept("oratory" , oratory);
		function.accept("emotion" , emotion);
		function.accept("extraSensoryPerception" , extraSensoryPerception);
		function.accept("luck" , luck);
		
	}

	/**
	 * @see {@code forValidPhysique}
	 */
	public void forValidAttunement(Consumer<Integer> function) {

		if(vessel > 0) function.accept(vessel);
		if(physicalDurability > 0) function.accept(physicalDurability);
		if(mentalDurability > 0) function.accept(mentalDurability);
		if(flow > 0) function.accept(flow);
		if(overload > 0) function.accept(overload);
	}

	/**
	 * @see {@code forValidPhysique}
	 */
	public void forValidAttunement(BiConsumer<String , Integer> function) {

		if(vessel > 0) function.accept("vessel" , vessel);
		if(physicalDurability > 0) function.accept("physicalDurability" , physicalDurability);
		if(mentalDurability > 0) function.accept("mentalDurability" , mentalDurability);
		if(flow > 0) function.accept("flow" , flow);
		if(overload > 0) function.accept("overload" , overload);
	}
	
	/**
	 * @see {@code forEachPhysique}
	 */
	public void forEachAttunement(Consumer<Integer> function) {

		function.accept(vessel);
		function.accept(physicalDurability);
		function.accept(mentalDurability);
		function.accept(flow);
		function.accept(overload);
	}

	/**
	 * @see {@code forEachPhysique}
	 */
	public void forEachAttunement(BiConsumer<String , Integer> function) {

		function.accept("vessel" , vessel);
		function.accept("physicalDurability" , physicalDurability);
		function.accept("mentalDurability" , mentalDurability);
		function.accept("flow" , flow);
		function.accept("overload" , overload);
	}

	/**
	 * Calls {@code forValid...} on physique, intellect and attunement functions
	 * 
	 * @param function — function taking a stat's value as input
	 */
	public void forValidStats(Consumer<Integer> function) {
		
		forValidPhysique(function);
		forValidIntellect(function);
		forValidAttunement(function);
		
	}

	/**
	 * Calls {@code forValid...} on physique, intellect and attunement stats.
	 * 
	 * @param function — function taking a stat's name and value as input
	 */
	public void forValidStats(BiConsumer<String , Integer> function) {
		
		forValidPhysique(function);
		forValidIntellect(function);
		forValidAttunement(function);
		
	}
	
	/**
	 * Calls {@code forEach...} on physique, intellect, and attunement stats.
	 * 
	 * @param function — function taking a stat's value as input
	 */
	public void forEachStat(Consumer<Integer> function) {
		
		forEachPhysique(function);
		forEachIntellect(function);
		forEachAttunement(function);
		
	}

	/**
	 * Calls {@code forEach...} on physique, intellect, and attunement stats.
	 * 
	 * @param function — function taking a stat's name and value as input
	 */
	public void forEachStat(BiConsumer<String , Integer> function) {
		
		forEachPhysique(function);
		forEachIntellect(function);
		forEachAttunement(function);
		
	}

	/**
	 * Calls {@code function} for each LSM (life, stamina, mana) stat. 
	 * 
	 * @param function — function taking a stat's name and value as input 
	 */
	public void forEachLSM(BiConsumer<String , Float> function) {
		
		function.accept("currentLife", currentLife);
		function.accept("maxLife", maxLife);
		function.accept("currentStamina", currentStamina);
		function.accept("maxStamina", maxStamina);
		function.accept("currentMana", currentMana);
		function.accept("maxMana", maxMana);		
		
	}
	
	/**
	 * Calls {@code function} for each skill stat.
	 * 
	 * @param function — function taking a stat's name and value as input
	 */
	public void forEachSkill(BiConsumer<String , Integer> function) {
		
		function.accept("swords", swords);
		function.accept("blunts", blunts);
		function.accept("polearms", polearms);
		function.accept("bows", bows);
		function.accept("crossbows", crossbows);
		function.accept("staves", staves);
		function.accept("wands", wands);
		function.accept("pyromancy", pyromancy);
		function.accept("cryomancy", cryomancy);
		function.accept("electromancy", electromancy);
		function.accept("heavyArmor", heavyArmor);
		function.accept("lightArmor", lightArmor);
		function.accept("mediumArmor", mediumArmor);
		
	}	

	/**
	 * Calls {@code function} for each skill greater than 0.
	 * 
	 * @param function — functino taking a stat's name and value as input
	 */
	public void forValidSkills(BiConsumer<String , Integer> function) {
		
		if(swords > 0) function.accept("swords", swords);
		if(blunts > 0) function.accept("blunts", blunts);
		if(polearms > 0) function.accept("polearms", polearms);
		if(bows > 0) function.accept("bows", bows);
		if(crossbows > 0) function.accept("crossbows", crossbows);
		if(staves > 0) function.accept("staves", staves);
		if(wands > 0) function.accept("wands", wands);
		if(pyromancy > 0) function.accept("pyromancy", pyromancy);
		if(cryomancy > 0) function.accept("cryomancy", cryomancy);
		if(electromancy > 0) function.accept("electromancy", electromancy);
		if(heavyArmor > 0) function.accept("heavyArmor", heavyArmor);
		if(lightArmor > 0) function.accept("lightArmor", lightArmor);
		if(mediumArmor > 0) function.accept("mediumArmor", mediumArmor);
		
	}	
	
	/*
	 
	 GETTERS AND SETTERS
	 
	 */
	
	public int strength() {
		return strength;
	}

	public void strength(int strength) {
		this.strength = strength;
	}

	public int endurance() {
		return endurance;
	}

	public void endurance(int endurance) {
		this.endurance = endurance;
	}

	public int dexterity() {
		return dexterity;
	}

	public void dexterity(int dexterity) {
		this.dexterity = dexterity;
	}

	public int agility() {
		return agility;
	}

	public void agility(int agility) {
		this.agility = agility;
	}

	public int recovery() {
		return recovery;
	}

	public void recovery(int recovery) {
		this.recovery = recovery;
	}

	public int resistence() {
		return resistence;
	}

	public void resistence(int resistence) {
		this.resistence = resistence;
	}

	public int balance() {
		return balance;
	}

	public void balance(int balance) {
		this.balance = balance;
	}

	public int sharpness() {
		return sharpness;
	}

	public void sharpness(int sharpness) {
		this.sharpness = sharpness;
	}

	public int morals() {
		return morals;
	}

	public void morals(int morals) {
		this.morals = morals;
	}

	public int charisma() {
		return charisma;
	}

	public void charisma(int charisma) {
		this.charisma = charisma;
	}

	public int perception() {
		return perception;
	}

	public void perception(int perception) {
		this.perception = perception;
	}

	public int composure() {
		return composure;
	}

	public void composure(int composure) {
		this.composure = composure;
	}

	public int oratory() {
		return oratory;
	}

	public void oratory(int oratory) {
		this.oratory = oratory;
	}

	public int emotion() {
		return emotion;
	}

	public void emotion(int emotion) {
		this.emotion = emotion;
	}

	public int extraSensoryPerception() {
		return extraSensoryPerception;
	}

	public void extraSensoryPerception(int extraSensoryPerception) {
		this.extraSensoryPerception = extraSensoryPerception;
	}

	public int luck() {
		return luck;
	}

	public void luck(int luck) {
		this.luck = luck;
	}

	public int vessel() {
		return vessel;
	}

	public void vessel(int vessel) {
		this.vessel = vessel;
	}

	public int physicalDurability() {
		return physicalDurability;
	}

	public void physicalDurability(int physicalDurability) {
		this.physicalDurability = physicalDurability;
	}

	public int mentalDurability() {
		return mentalDurability;
	}

	public void mentalDurability(int mentalDurability) {
		this.mentalDurability = mentalDurability;
	}

	public int flow() {
		return flow;
	}

	public void flow(int flow) {
		this.flow = flow;
	}

	public int overload() {
		return overload;
	}

	public void overload(int overload) {
		this.overload = overload;
	}

	public float currentLife() {
		return currentLife;
	}

	public void currentLife(float currentLife) {
		this.currentLife = currentLife;
	}

	public float maxLife() {
		return maxLife;
	}

	public void maxLife(float maxLife) {
		this.maxLife = maxLife;
	}

	public float currentStamina() {
		return currentStamina;
	}

	public void currentStamina(float currentStamina) {
		this.currentStamina = currentStamina;
	}

	public float maxStamina() {
		return maxStamina;
	}

	public void maxStamina(float maxStamina) {
		this.maxStamina = maxStamina;
	}

	public float currentMana() {
		return currentMana;
	}

	public void currentMana(float currentMana) {
		this.currentMana = currentMana;
	}

	public float maxMana() {
		return maxMana;
	}

	public void maxMana(float maxMana) {
		this.maxMana = maxMana;
	}

	public int swords() {
		return swords;
	}

	public void swords(int swords) {
		this.swords = swords;
	}

	public int blunts() {
		return blunts;
	}

	public void blunts(int blunts) {
		this.blunts = blunts;
	}

	public int polearms() {
		return polearms;
	}

	public void polearms(int polearms) {
		this.polearms = polearms;
	}

	public int bows() {
		return bows;
	}

	public void bows(int bows) {
		this.bows = bows;
	}

	public int crossbows() {
		return crossbows;
	}

	public void crossbows(int crossbows) {
		this.crossbows = crossbows;
	}

	public int staves() {
		return staves;
	}

	public void staves(int staves) {
		this.staves = staves;
	}

	public int wands() {
		return wands;
	}

	public void wands(int wands) {
		this.wands = wands;
	}

	public int pyromancy() {
		return pyromancy;
	}

	public void pyromancy(int pyromancy) {
		this.pyromancy = pyromancy;
	}

	public int cryomancy() {
		return cryomancy;
	}

	public void cryomancy(int cryomancy) {
		this.cryomancy = cryomancy;
	}

	public int electromancy() {
		return electromancy;
	}

	public void electromancy(int electromancy) {
		this.electromancy = electromancy;
	}

	public int heavyArmor() {
		return heavyArmor;
	}

	public void heavyArmor(int heavyArmor) {
		this.heavyArmor = heavyArmor;
	}

	public int lightArmor() {
		return lightArmor;
	}

	public void lightArmor(int lightArmor) {
		this.lightArmor = lightArmor;
	}

	public int mediumArmor() {
		return mediumArmor;
	}

	public void mediumArmor(int mediumArmor) {
		this.mediumArmor = mediumArmor;
	}

	/**
	 * Copies the primitive values ONLY of this object into {@code other}
	 * 
	 * @param other — some EntityRPGStats object
	 * @throws NullPointerException if {@code other} is null.
	 */
	public void copy(EntityRPGStats other) throws NullPointerException{
		
		requireNonNull(other);
		forEachStat((name , value) -> other.setCharacteristicForName(name, value));
		forEachLSM((name , value) -> other.setLSMForName(name, value));
		forEachSkill((name , value) -> other.setSkillForName(name, value));
		other.invincibilityTimeMillis = this.invincibilityTimeMillis; 
		
	}

}
