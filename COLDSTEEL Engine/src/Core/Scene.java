package Core;

import java.util.function.Consumer;

import CS.Engine;
import CSUtil.DataStructures.CSLinked;
import Core.Entities.EntityLists;
import Core.Entities.EntityScriptingInterface;
import Core.Statics.StaticLists;
import Core.TileSets.TileSets;
import Game.Items.ItemScriptingInterface;
import Game.Items.UnownedItems;
import Game.Levels.TriggerScriptingInterface;
import Game.Projectiles.ProjectileScriptingInterface;
import Physics.ColliderLists;
import Physics.Kinematics;

public class Scene {

	private final ObjectLists quads1;
	private final TileSets tiles1;
	private final StaticLists statics1; 
	private final EntityLists entities;
	private final UnownedItems items; 
	private final ObjectLists quads2;
	private final TileSets tiles2;
	private final StaticLists statics2;
	private final ColliderLists colliders;
	private final Kinematics kinematics;
	private final EntityScriptingInterface entityScriptingInterface;
	private final UIScriptingInterface uiScriptingInterface;
	private final ItemScriptingInterface itemScriptingInterface;
	private final TriggerScriptingInterface triggerScriptingInterface;
	private final ProjectileScriptingInterface projectileScriptingInterface;
	private final ObjectLists finalObjects;
	private final CSLinked<float[]> finalArrays;
	
	public Scene(Engine engine) {

		this.quads1 = new ObjectLists(this , 1);
		this.tiles1 = new TileSets(this , 2);
		this.statics1 = new StaticLists(this , 3);
		this.entities = new EntityLists(this , 4 , engine.getCamera());
		this.items = new UnownedItems(this , 5);
		this.quads2 = new ObjectLists(this , 6);
		this.tiles2 = new TileSets(this , 7);
		this.statics2 = new StaticLists(this , 8);
		this.colliders = new ColliderLists(this);
		this.kinematics = new Kinematics(this);
		this.entityScriptingInterface = new EntityScriptingInterface(this , engine.getConsole());
		this.uiScriptingInterface = new UIScriptingInterface(engine);                
		this.itemScriptingInterface = new ItemScriptingInterface(this, engine.getConsole());            
		this.triggerScriptingInterface = new TriggerScriptingInterface(this);      
		this.projectileScriptingInterface = new ProjectileScriptingInterface(this);
		finalObjects = new ObjectLists(this , 10);
		finalArrays = new CSLinked<>();
				
	}
	
	public void clear() {
		
		quads1.clear();
		tiles1.clearInstances();
		statics1.clear();
		entities.clear();
		items.clear();
		quads2.clear();
		tiles2.clearInstances();
		statics2.clear();
		colliders.clear();
		
	}

	public void forEach(Consumer<AbstractGameObjectLists<? extends Quads>> function) {
		
		function.accept(quads1);
		function.accept(tiles1);
		function.accept(statics1);
		function.accept(entities);
		function.accept(items);
		function.accept(quads2);
		function.accept(tiles2);
		function.accept(statics2);
		function.accept(colliders);
		
	}

	public void forDefault(Consumer<AbstractGameObjectLists<? extends Quads>> function) {
		
		function.accept(quads1);
		function.accept(tiles1);
		function.accept(statics1);
		function.accept(entities);
		function.accept(items);
		function.accept(quads2);
		function.accept(tiles2);
		function.accept(statics2);
		function.accept(finalObjects);
		
	}
	
	public ObjectLists quads1() {
		
		return quads1;
		
	}

	public TileSets tiles1() {
		
		return tiles1;
		
	}
	
	public StaticLists statics1() { 
		
		return statics1;
		
	}
	
	public EntityLists entities() {
	
		return entities;
		
	}	
	
	public UnownedItems items() {
		
		return items;
		
	}

	public ObjectLists quads2() {
		
		return quads2;
		
	}

	public TileSets tiles2() {
		
		return tiles2;
		
	}
	
	public StaticLists statics2() { 
		
		return statics2;
		
	}
	
	public ColliderLists colliders() {
	
		return colliders;
		
	}	
	
	public Kinematics kinematics() {
		
		return kinematics;
		
	}
	
	public EntityScriptingInterface entityScriptingInterface() {
		
		return entityScriptingInterface;
		
	}

	public UIScriptingInterface uiScriptingInterface() {
		
		return uiScriptingInterface;
		
	}

	public ItemScriptingInterface itemScriptingInterface() {
		
		return itemScriptingInterface;
		
	}

	public TriggerScriptingInterface triggerScriptingInterface() {
		
		return triggerScriptingInterface;
		
	}

	public ProjectileScriptingInterface projectileScriptingInterface() {
		
		return projectileScriptingInterface;
		
	}
	
	public ObjectLists finalObjects() {
		
		return finalObjects;
		
	}

	public CSLinked<float[]> finalArrays() {
		
		return finalArrays;
		
	}
	
	public int numberObjects() {
		
		return quads1.size() + 
			   tiles1.numberInstances() + 
			   statics1.size() + 
			   entities.size() + 
			   items.size() + 
			   quads2.size() + 
			   tiles2.size() + 
			   statics2.size() + 
			   colliders.size();
		
	}
	
	public int hashCode() {
		
		return super.hashCode() + numberObjects();
		
	}
	
}
