package Game.Items;

import java.util.function.Consumer;

import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;

import CS.COLDSTEEL;
import CSUtil.RefInt;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.cdNode;
import Core.Entities.Entities;

public class Inventories implements ItemOwner {

	final Entities owner;
	float weightLimit = 100f;
	float currentWeight = 0f;
	int xDimension = 12;
	int yDimension = 4;
	CSLinked<Tuple2<Items , RefInt>> inventory = new CSLinked<>();
	CSArray<Items> equipSlots = new CSArray<>(99);
	Consumer<Items> onAcquire;
		
	public Inventories(Entities owner) {
		
		this.owner = owner;
		
	}
	
	public void weightLimit(float limit) {
		
		this.weightLimit = limit;
		
	}
	
	public float weightLimit() {
		
		return weightLimit;
		
	}
	
	public void xDimension(int xDim) {
		
		this.xDimension = xDim;
				
	}
	
	public int xDimension() {
		
		return xDimension;
		
	}

	public void yDimension(int yDim) {
		
		this.yDimension = yDim;
				
	}
	
	public int yDimension() {
		
		return yDimension;
		
	}
	
	public CSLinked<Tuple2<Items , RefInt>> getItems(){
		
		return inventory;
		
	}

	public CSArray<Items> getEquipped(){
		
		return equipSlots;
		
	}
	
	public Items equipSlot(int index) {
		
		return equipSlots.get(index);
		
	}
	
	public boolean has(String itemName) {
		RefInt has = new RefInt(0);
		inventory.forEachVal((tuple) -> {
			
			if(tuple.getFirst().name.equals(itemName)) has.set(1);
			
		});
		
		return has.get() == 1;
		
	}

	public boolean has(Items item) {
		
		int hashCode = item.hashCode();
		cdNode<Tuple2<Items , RefInt>> iter = iter();
		for(int i = 0 ; i < inventory.size() ; i ++ , iter = iter.next) if(iter.val.getFirst().hashCode() == hashCode) return true;
		return false;		
		
	}
	
	public void equip(Items x) {
		
		if(x.has(ItemComponents.EQUIPPABLE)) {
			
			int slot = x.componentData().equipSlot();
			x.changeOwner(this);
			
			if(equipSlots.get(slot) == null) { 
				
				equipSlots.addAt(slot, x);
				
			} else if (equipSlots.get(slot) != null) {
				
				unequip(slot);
				equipSlots.addAt(slot , x);
				
			}
			
			cdNode<Tuple2<Items , RefInt>> iter = inventory.get(0);
			for(int i = 0 ; i < inventory.size() ; i ++) {
				
				if(iter.val.getFirst() == x) {
					
					if(iter.val.getSecond().greaterThan(1)) iter.val.getSecond().dec();
					else inventory.safeRemove(iter);					
					return;
					
				}
				
				iter = iter.next;
				
			}
			
		}
		
	}
	
	/**
	 * 
	 * Attempts to equip the item in the inventory at {@code index} into its slot.
	 * 
	 * @param index — the index of the item in the inventory
	 * 
	 */
	public Items equip(int index) {
		
		if(index < 0 || index > inventory.size() - 1) {
			
			if(COLDSTEEL.DEBUG_CHECKS) System.err.println("Entity " + owner.name() + " trying to equip item at invalid index: " + index);
			return null;
			
		}
		
		Items query = inventory.getVal(index).getFirst();
		equip(query);
		return query;
		
	}
	
	public cdNode<Tuple2<Items , RefInt>> iter (){
		
		return inventory.get(0);				
		
	}
	
	public int inventorySize() {
		
		return inventory.size();
				
	}
	
	public Items getEquippedItem(int slot) {
		
		return equipSlots.get(slot);
		
	}
	
	public Items unequip(int slot) {
		
		Items unequipped = equipSlots.remove(slot);
		acquire(unequipped);	
		return unequipped;
		
	}
	
	public Entities owner() {
		
		return owner;
		
	}
	
	public void onAcquire(Consumer<Items> callback) {
		
		this.onAcquire = callback;
		
	}

	public void onAcquire(PyObject callback) {
		
		this.onAcquire = (item) -> callback.__call__(new ClassicPyObjectAdapter().adapt(item));
		
	}
	
	public RefInt numberOfItem(String name) {
		
		cdNode<Tuple2<Items , RefInt>> item = inventory.getIfExists(tuple -> tuple.getFirst().name.equals(name));		
		if(item != null) return item.val.getSecond();
		else return new RefInt(0);
		
	}
	
	@Override public Items get(int index) {
		
		assert index < inventory.size() && index >= 0: "invalid index";
		return inventory.get(index).val.getFirst();
		
	}
	
	public Tuple2<Items , RefInt> getByName(String name) {
		
		cdNode<Tuple2<Items , RefInt>> iter = iter();
		for(int i = 0 ; i < inventory.size() ; i ++ , iter = iter.next) if(iter.val.getFirst().name.equals(name)) return iter.val;		
		return new Tuple2<Items , RefInt>(null , new RefInt(0));
		
	}
	
	@Override public void acquire(Items item) {
		
		cdNode<Tuple2<Items , RefInt>> iter = iter();
		for(int i = 0 ; i  < inventory.size() ; i ++) {
		
			if(iter.val.getFirst().equals(item)) {
				
				if(onAcquire != null) onAcquire.accept(item);
				iter.val.getSecond().add();
				currentWeight += item.weight;
				item.changeOwner(this);
				return;
				
			}
			
			iter = iter.next;
			
		}
		
		if(onAcquire != null) onAcquire.accept(item);
		inventory.add(new Tuple2<>(item , new RefInt(1)));
		currentWeight += item.weight;
		item.changeOwner(this);
		
	}

	@Override public boolean canAcquire(Items item) {
		
		if(currentWeight + item.weight <= weightLimit) return true;
		return false;
		
	}

	@Override public Items remove(int index) {
		
		cdNode<Tuple2<Items , RefInt>> removedNode = inventory.get(index);
		Items removed = removedNode.val.getFirst();
		currentWeight -= removed.weight;		
		removedNode.val.getSecond().dec();
		if(removedNode.val.getSecond().lessThanEqualTo(1)) inventory.safeRemove(removedNode);
		return removed;
		
	}

	@Override public void remove(Items item) {
		
		cdNode<Tuple2<Items , RefInt>> iter = iter();
		for(int i = 0 ; i < inventory.size() ; i ++) {
			
			if(iter.val.getFirst() == item) {
				
				currentWeight -= item.weight;		
				iter.val.getSecond().dec();
				if(iter.val.getSecond().lessThanEqualTo(1)) inventory.safeRemove(iter);
				return;
				
			}
			
			iter = iter.next;
			
		}
		
	}

	@Override public void remove(String itemName) {
		
		int itemNameHash = itemName.hashCode();
		cdNode<Tuple2<Items , RefInt>> iter = iter();
		for(int i = 0 ; i < inventory.size() ; i ++) {
			
			if(iter.val.getFirst().hashCode() == itemNameHash) {
				
				currentWeight -= iter.val.getFirst().weight;		
				iter.val.getSecond().dec();
				if(iter.val.getSecond().lessThanEqualTo(1)) inventory.safeRemove(iter);
				return;
				
			}
			
			iter = iter.next;
			
		}
		
	}
	
	@Override public void give(ItemOwner receiver, int itemIndex) {
		
		if(receiver.canAcquire(get(itemIndex))) receiver.acquire(remove(itemIndex));
				
	}
	
}