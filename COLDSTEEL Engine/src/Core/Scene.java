package Core;

import java.util.function.Consumer;

import Core.Entities.EntityLists;
import Core.Statics.StaticLists;
import Core.TileSets.TileSets;
import Game.Items.UnownedItems;
import Physics.ColliderLists;

public record Scene(		
	ObjectLists quads1 , 
	TileSets tiles1 , 
	StaticLists statics1 , 
	EntityLists entities ,
	UnownedItems items , 
	ObjectLists quads2 , 
	TileSets tiles2 ,
	StaticLists statics2 ,
	ColliderLists colliders 
	) {

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
