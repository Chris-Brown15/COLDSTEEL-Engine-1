package Game.Items;

import CSUtil.DataStructures.cdNode;
import Core.AbstractGameObjectLists;
import Core.CSType;
import Core.Quads;
import Core.Scene;

/**
 * 
 * List object responsible for rendering Items that are not owned by anyone. Items in the scene that are not owned by anyone will live here and 
 * this class will be responsible for placing items in the inventories of entities that pick them up
 * 
 * @author Chris Brown
 *
 */
public class UnownedItems extends AbstractGameObjectLists<Items> implements ItemOwner {

	public UnownedItems(Scene owningScene , int order) {

		super(owningScene , order , CSType.ITEM);

	}
	
	public void newItem(String name) {
		
		Items newItem = new Items(owningScene , name , list.size());
		list.add(newItem);
		
	}
	
	public Items load(String namePath) {
		
		Items loaded = new Items(owningScene , namePath);
		list.add(loaded);
		loaded.setID(list.size() - 1);
		return loaded;
		
		
	}
	
	public int size() {
		
		return list.size();
		
	}
	
	public Quads selectItems(float mouseX , float mouseY) {
		
		cdNode<Items> iter = list.get(0);
		for(int i = 0 ; i < list.size() ; i ++ , iter = iter.next) if(iter.val.selectQuad(mouseX , mouseY) != -1) return iter.val;		
		return null;
		
	}
		
	public cdNode<Items> iter(){
		
		return list.get(0);
		
	}
	
	public void removeItem(cdNode<Items> item) {
		
		list.safeRemove(item);
		
	}
	
	public void clear() {
		
		list.clear();
		
	}
	
	@Override public Items get(int index) {
		
		assert index < list.size() && index >= 0: "invalid index";
		return list.get(index).val;
		
	}
	
	@Override public void acquire(Items item) {

		list.add(item);
		item.iconify();
		
	}

	@Override public boolean canAcquire(Items item) {

		return true;
		
	}

	@Override public Items remove(int index) {

		Items removed = list.safeRemove(list.get(index)).val;
		return removed;
		
	}

	@Override public void remove(Items item) {

		list.removeVal(item);
		
	}

	@Override public void remove(String itemName) {

		int hashCode = itemName.hashCode();
		list.removeFirstIf((item) -> item.hashCode() == hashCode);
		
	}

	@Override public void give(ItemOwner receiver , int itemIndex) {
	
		Items item = remove(itemIndex);
		if(receiver.canAcquire(item)) receiver.acquire(item);
		else acquire(item);
		
	}

}
