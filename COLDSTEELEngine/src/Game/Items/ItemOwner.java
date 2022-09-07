package Game.Items;

/**
 * Interface for standardizing Item operations. This Interface should be implemented by anything that wants to deal with items.
 * 
 * 
 * @author Chris Brown
 *
 */
public interface ItemOwner {

	public void acquire(Items item);
	public boolean canAcquire(Items item);
	public Items remove(int index);
	public void remove(Items item);
	public void remove(String itemName);
	public void give(ItemOwner receiver , int itemIndex);
	public Items get(int index);
	
}
