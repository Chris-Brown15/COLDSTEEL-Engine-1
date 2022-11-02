package Core;

import java.util.function.Consumer;

import CS.Engine;
import Core.Entities.EntityLists;
import Core.Statics.StaticLists;
import Core.TileSets.TileSets;
import Game.Items.UnownedItems;
import Physics.ColliderLists;
import Renderer.Camera;

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
	public final int index;
	
	public Scene(
		ObjectLists quads1 , TileSets tiles1 , StaticLists statics1 , 
		EntityLists entities ,
		UnownedItems items , 
		ObjectLists quads2 , TileSets tiles2 , StaticLists statics2 ,
		ColliderLists colliders) {
		
		this.quads1 = quads1;
		this.tiles1 = tiles1;
		this.statics1 = statics1;
		this.entities = entities;
		this.items = items;
		this.quads2 = quads2;
		this.tiles2 = tiles2;
		this.statics2 = statics2;
		this.colliders = colliders;
		
	}
	
	public Scene(Camera camera) {

		this.quads1 = new ObjectLists(1);
		this.tiles1 = new TileSets(2);
		this.statics1 = new StaticLists(3);
		this.entities = new EntityLists(4 , camera);
		this.items = new UnownedItems(5);
		this.quads2 = new ObjectLists(6);
		this.tiles2 = new TileSets(7);
		this.statics2 = new StaticLists(8);
		this.colliders = new ColliderLists();
		
	}
	
	{
		index = Engine.addScene(this);
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
	
}
