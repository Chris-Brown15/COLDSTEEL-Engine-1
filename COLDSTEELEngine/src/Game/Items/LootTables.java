package Game.Items;

import org.joml.Random;

import CSUtil.RefInt;
import CSUtil.DataStructures.CSOHashMap;
import CSUtil.DataStructures.Tuple3;
import Core.Scene;
import Physics.ForceType;
import Physics.Kinematics;

/**
 * Loot tables provide a way to control what items an entity will drop on its death. Items are added into the loot table with an accompanying float
 * representing the items drop chance out of 100. 
 * 
 * @author Chris Brown
 *
 */
public class LootTables {

	/*
	 * Item names are added as values with their chance also being the hash value. 
	 * Hash codes are to be snapped to a multiple of ten, then divided by ten, then subtracted by one. The only exceptional case is percent 
	 * of 100, which returns 9.
	 * 
	 */
	CSOHashMap<Tuple3<String , Float , Integer>, Float> lootTable = new CSOHashMap<>(10 , percent -> {
		
		assert percent > 0 && percent <= 100.0f : "ERROR: Invalid drop chance: " + percent + " not a valid chance to drop.";
		
		float off = 10 - (percent % 10);
		float hash = ((percent + off) / 10) - 1;
		return (int) (hash != 10 ? hash : 9);
		
	});
		
	UnownedItems items;
	float worldX , worldY;
	
	public LootTables(Scene scene) {
		
		items = scene.items();
		
	}
	
	/**
	 * Adds an entry to this loot table.
	 * 
	 * @param itemName
	 * @param dropChance
	 * @param maxAmountToDrop
	 */
	public void addItem(String itemName , float dropChance , int maxAmountToDrop) {
		
		//we clamp the drop chance to a value between 0 and 10. for the loot table tuple because we use the mod of the proc to determine if we get it.
		float adjustedDropChance = dropChance % 10 == 0 ? 10 : dropChance % 10;
		lootTable.add(new Tuple3<>(itemName , adjustedDropChance , maxAmountToDrop) , dropChance);
				
	}
	
	public void moveTo(float x , float y) {
		
		this.worldX = x;
		this.worldY = y;
				
	}
	
	/**
	 * When called, the loot table is computed in its current state and procced items are added to unowned items.
	 * This table is processed in a repetitive way
	 * 
	 */
	public void computeLootTable() {
		
		//generate a random float between 0 and 100, then get the integer tens place which is the hash into the table
		//then, among items in that bucket, compare the remainder of the proc to the item drop chances, dropping those will a larger chance than
		//the remainder
		
		Random random = new Random(); 
		float rawProc = random.nextFloat() * 100f;
		int intProc = (int) rawProc;
		float floatingProc = rawProc % 10;
		RefInt adjustedProc = new RefInt((intProc - (intProc % 10)) / 10);
		if(adjustedProc.equals(10)) adjustedProc.dec();	
		
		lootTable.forEach((hash , list) -> {
			
			if(adjustedProc.get() <= hash) {
				
				list.forEachVal(tuple -> {
					
					if(tuple.getSecond() >= floatingProc) {
						
						if(tuple.getThird() > 1) {
						
							int randomAmount = random.nextInt(tuple.getThird()) + 1;
							for(int i = 0 ; i < randomAmount ; i ++) {

								Items newItem = new Items(tuple.getFirst() + ".CStf");
								newItem.moveTo(worldX, worldY);
								items.acquire(newItem);
								Kinematics.impulse(ForceType.LINEAR_GROW , 9999d , 0.0f , -1.0f , 0.0f , 0.025f, newItem);
								
							}
							
						} else {
							
							Items newItem = new Items(tuple.getFirst() + ".CStf");
							newItem.moveTo(worldX, worldY);
							items.acquire(newItem);
							Kinematics.impulse(ForceType.LINEAR_GROW , 9999d , 0.0f , -1.0f , 0.0f , 0.025f, newItem);
							
						}						
						
					}
					
				});
				
			}
			
		});		
		
	}

	/**
	 * 
	 * 
	 * 
	 */
	public void computeLootTable(float mod) {
		
		//generate a random float between 0 and 100, then get the integer tens place which is the hash into the table
		//then, among items in that bucket, compare the remainder of the proc to the item drop chances, dropping those will a larger chance than
		//the remainder
		
		Random random = new Random(); 
		float rawProc = random.nextFloat() * 100f;
		rawProc -= mod;
		int intProc = (int) rawProc;
		float floatingProc = rawProc % 10;
		RefInt adjustedProc = new RefInt((intProc - (intProc % 10)) / 10);
		if(adjustedProc.equals(10)) adjustedProc.dec();	
		
		lootTable.forEach((hash , list) -> {
			
			if(adjustedProc.get() <= hash) {
				
				list.forEachVal(tuple -> {
					
					if(tuple.getSecond() >= floatingProc) {
						
						if(tuple.getThird() > 1) {
						
							int randomAmount = random.nextInt(tuple.getThird()) + 1;
							for(int i = 0 ; i < randomAmount ; i ++) {

								Items newItem = new Items(tuple.getFirst() + ".CStf");
								newItem.moveTo(worldX, worldY);
								items.acquire(newItem);
								Kinematics.impulse(ForceType.LINEAR_GROW , 9999d , 0.0f , -1.0f , 0.0f , 0.025f, newItem);
								
							}
							
						} else {
							
							Items newItem = new Items(tuple.getFirst() + ".CStf");
							newItem.moveTo(worldX, worldY);
							items.acquire(newItem);
							Kinematics.impulse(ForceType.LINEAR_GROW , 9999d , 0.0f , -1.0f , 0.0f , 0.025f, newItem);
							
						}						
						
					}
					
				});
				
			}
			
		});		
		
	}
	
	public void print() {
		
		lootTable.forEach((hash , bucket) -> {
			
			System.out.println("Bucket at hash: " + hash + ":");
			bucket.forEachVal(tuple3 -> System.out.println("\t" + tuple3.getFirst() + ", odds: " + tuple3.getSecond()));
			
		});
		
	}
	
}
