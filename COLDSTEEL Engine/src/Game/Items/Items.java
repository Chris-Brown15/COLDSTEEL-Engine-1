package Game.Items;

import static CS.COLDSTEEL.data;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Random;

import CSUtil.CSTFParser;
import CSUtil.DataStructures.CSLinked;
import Core.CSType;
import Core.GameFiles;
import Core.HitBoxSets;
import Core.Quads;
import Core.Scene;
import Core.SpriteSets;
import Core.Entities.Entities;


/**
 * 
 * Class for game items. Items will use an entity component system-like architecture in which Item Components will deliver data to them which they will make 
 * make use of. Like Entities, Items will have a {@code has} method which will return whether an item has a given component, and Items will have a 
 * components array.
 * <br><br>
 * Now items have a few, very distinct states. Further, Items may or may not be owned, in use, on the ground, in an inventory, etc. They have a few
 * very distinct states that must be managed.
 * 
 * @author Chris Brown
 *
 */
public class Items extends Quads implements GameFiles<Items>{

	private ItemOwner owner;	
	String name;
	private BitSet components = new BitSet(ItemComponents.values().length);
	private ItemComponentData componentData = new ItemComponentData(this);
	
	//inventory dimensions
	int xSize = 1;
	int ySize = 1;
	
	//play this while on the ground
	SpriteSets iconAnim = null;
	
	//icon
	float[] iconSprite = new float[6];
	
	//item's weight in the inventory. If the sum of item weights >= the inventory's max weight, new items should not be acquired
	float weight = 0f;		

	//items fall force
	float initialFallForce = 0;
	
	//max number of items can be stacked at once
	int maxStackSize = 1;
	
	//84 109 142
		
	private final int hashCode;
	Scene owningScene;
	
	public Items(Scene owningScene , String name , int ID) {
		
		super(CSUtil.BigMixin.getItemFloatArray() , ID , CSType.ITEM);
		this.name = name;
		hashCode = name.hashCode();
		
	}
	
	public Items(Scene owningScene , String name , int ID , ItemComponents...components) {
		
		super(CSUtil.BigMixin.getItemFloatArray() , ID , CSType.ITEM);
		toggleComponents(components);
		this.name = name;
		hashCode = name.hashCode();
		
	}
		
	public Items(Scene owningScene , String namePath){
		
		super(CSUtil.BigMixin.getItemFloatArray() , -1 , CSType.ITEM);
		load(namePath);
		hashCode = namePath.substring(0, namePath.length() - 5).hashCode();
		
	}
	
	{
		snapToPixels = false;		
	}
	
	/**
	 * Returns {@code true} if {@code this} has all {@code components}, else {@code false}
	 * 
	 * @param components — vargs ItemComponents
	 * @return — true if this has all components, else false
	 */
	public boolean has(ItemComponents...components) {
		
		for(ItemComponents x : components) if(!this.components.get(x.index)) return false;
		return true;
		
	}
	
	/**
	 * For each component provided, if {@code this} has the component already, it is disabled, otherwise it is added and a new data object is created
	 * 
	 * @param components — components to use
	 */
	public void toggleComponents(ItemComponents...components) {
		
		for(ItemComponents x : components) {
			
			if(this.components.get(x.index)) this.components.clear(x.index);
			else {
				
				this.components.set(x.index);				
				componentData.set(x);
				
			}			
			
		}
		
	}
		
	public void maxStackSize(int stackSize) {
		
		this.maxStackSize = stackSize;
		
	}

	public int maxStackSize() {
		
		return maxStackSize;
		
	}
	
	public ItemComponentData componentData() {
		
		return componentData;
		
	}
	
	public void iconify() {
		
		swapSprite(iconSprite);
		setWidth(iconSprite[4]);
		setHeight(iconSprite[5]);
				
	}
	
	public void setIconAnimation(String iconAnimNamePath) {
	
		iconAnim = new SpriteSets(iconAnimNamePath);
		
	}
		
	public String name() {
		
		return name;
		
	}
	
	public void iconSprite(float[] iconSprite) {
		
		this.iconSprite = iconSprite;
		
	}
	
	public float[] iconSprite() {
		
		return iconSprite;
		
	}
	
	public void xSize(int xSize) {
		
		this.xSize = xSize;
		
	}

	public void ySize(int ySize) {
		
		this.ySize = ySize;
		
	}
	
	public void weight(float weight) {
		
		this.weight = weight;
		
	}
	
	public CSLinked<ItemComponents> getComponents() {
		
		CSLinked<ItemComponents> comps = new CSLinked<ItemComponents>();
		ItemComponents[] itemComps = ItemComponents.values();
		for(int i = 0 ; i < itemComps.length ; i ++) if(components.get(i)) comps.add(itemComps[i]);
		return comps;
		
	}
	
	public void use() {
		
		componentData.use();
		if(has(ItemComponents.CONSUMABLE) && componentData.consumable.percentageToConsume >= new Random().nextInt(100) + 1) owner.remove(this);
		
	}
	
	/**
	 * Changes this item's owner to the provided ItemOwner. Necessary for Item Component Data that relies on owners to have up to date
	 * info on the item.
	 * 
	 * @param owner — an instance of {@code ItemOwner}
	 */
	public void changeOwner(ItemOwner owner) {
		
		this.owner = owner;
		componentData.changeOwner(owner);
		
	}
	
	public Entities ownerAsEntity() {
		
		if(owner.getClass() == Inventories.class && ((Inventories)owner).owner != null) return ((Inventories)owner).owner;
		return null;
		
	}
	
	public void initialFallForce(float fallForce) {
		
		this.initialFallForce = fallForce;
		
	}

	public float initialFallForce() {
		
		return initialFallForce;
		
	}
	
	public int hashCode() {
	
		return hashCode;
		
	}	
	
	public boolean equals(Items other) {
		
		return this.hashCode == other.hashCode;
		
	}
	
	private void wItemComponent(CSTFParser cstf , ItemComponents component) throws IOException {
		
		cstf.wlist(component.toString());
		
		switch(component) {
		
			case CONSUMABLE -> {
			
				cstf.wlabelValue("consume chance" , componentData.chanceToConsume());
				
			}
			
			case EQUIPPABLE -> {
			
				cstf.wlabelValue("slot" , componentData.equipSlot());
				cstf.wlabelValue("on equip script" , componentData.onEquipScript());
				cstf.wlabelValue("on unequip script" , componentData.onUnequipScript());
				
			}
			
			case HITBOXABLE -> {
			
				cstf.wlabelValue("boxes" , componentData.HitBoxable().getNumberAvailableHitBoxes());
				var hitboxes = componentData.HitBoxable();						
				cstf.wlist("sets", hitboxes.numberSets());				
				for(int i = 0 ; i < hitboxes.numberSets() ; i ++) cstf.wvalue(hitboxes.get(i).name());				
				cstf.endList();
				
			}
			
			case MATERIALS -> {
				
				System.err.println("Trying to write item component Materials, not implemented yet");
				
			}
			
			case USABLE -> {
			
				cstf.wlabelValue("on use", componentData.useScript());
				
			}
			
			case FLAGS -> {
				
				cstf.wlist("flags", componentData.numberFlags());
				componentData.getFlagsMap().forEach((hash , list) -> {
					
					list.forEachVal(flagName -> {
						
						try {
							
							cstf.wvalue(flagName);
							
						} catch(IOException e) {
							
							e.printStackTrace();
							
						}
						
					});
					
				});
				
				cstf.endList();
				
				
			}
			
			default -> {}
		
		}
		
		cstf.endList();
		
	}
	
	private void rcomponent(CSTFParser cstf , String component) throws IOException {
		
		toggleComponents(ItemComponents.parse(component));
		
		switch(component) {
		
			case "Equippable" -> {
				
				componentData.equipSlot(cstf.rintLabel("slot"));
				componentData.onEquipScript(cstf.rlabel("on equip script"));
				componentData.onUnequipScript(cstf.rlabel("on unequip script"));
				
			}
			
			case "Usable" -> {
				
				componentData.onUse(cstf.rlabel("on use"));
				
			}
			
			case "Hitboxable" -> {
				
				var hitboxes = componentData.HitBoxable(); 
				hitboxes.setNumberAvailableHitBoxes(cstf.rintLabel("boxes"));
				int numberSets = cstf.rlist("sets");
				for(int i = 0 ; i < numberSets ; i ++) hitboxes.addSet(new HitBoxSets(cstf.rvalue() + ".CStf"));
				cstf.endList();
				
			} 
			
			case "Consumable" -> {
				
				componentData.chanceToConsume(cstf.rintLabel("consume chance"));
				
			}
			
			case "Materials" -> {
				
			}
			
			case "Flags" -> {
				
				int number = cstf.rlist("flags");
				for(int i = 0 ; i < number ; i ++) {
					
					componentData.addFlag(cstf.rvalue());
										
				}
				
				cstf.endList();
				
			}
		
		}
				
	}
	
	@Override public void delete() {

		try {
			
			Files.move(Paths.get(CS.COLDSTEEL.data + "items/" + name + ".CStf") , Paths.get(CS.COLDSTEEL.deleted + name + ".CStf"));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void write(Object...additionalData) {

		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(CS.COLDSTEEL.data + "items/" + name + ".CStf"))){
			
			CSTFParser cstf = new CSTFParser(writer);
			cstf.wname(name);		
			cstf.wlabelValue("texture" , CSUtil.BigMixin.toNamePath(texture.imageInfo.path()));
			cstf.wlabelValue("removed" , removedColor.x , removedColor.y , removedColor.z);
			cstf.wlabelValue("filter" , filter.x , filter.y , filter.z);
			cstf.wlabelValue("icon", iconSprite);
			cstf.wlabelValue("inventory slots", xSize , ySize);
			cstf.wlabelValue("weight" , weight);
			cstf.wlabelValue("stack limit" , maxStackSize);
			if(iconAnim != null) cstf.wlabelValue("icon anim", iconAnim.name());
			else cstf.wnullLabel("icon anim");
			
			int numberComponents = 0;
			for(ItemComponents x : ItemComponents.values()) if(has(x)) numberComponents++;
			cstf.wlist("components" , numberComponents);			
			for(ItemComponents x : ItemComponents.values()) if(has(x)) wItemComponent(cstf , x);
			
			cstf.endList();
						
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}

	@Override public void load(String filepath) {
		
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(data + "items/" + filepath))){
			
			CSTFParser cstf = new CSTFParser(reader);
			
			name = cstf.rname();
			String textureNamePath = cstf.rlabel("texture");			
			if(!textureNamePath.equals("null")) Renderer.Renderer.loadTexture(texture , CS.COLDSTEEL.assets + "items/" + textureNamePath);
			
			float[] array = new float[6];
			
			cstf.rlabel("removed" , array);
			removeColor(array[0] , array[1] , array[2]);
			
			cstf.rlabel("filter" , array);
			setFilter(array[0] , array[1] , array[2]);
			
			cstf.rlabel("icon" , array);			
			iconSprite[0] = array[0];
			iconSprite[1] = array[1];
			iconSprite[2] = array[2];
			iconSprite[3] = array[3];
			iconSprite[4] = array[4];
			iconSprite[5] = array[5];
			
			cstf.rlabel("inventory slots" , array);
			xSize = (int)array[0];
			ySize = (int)array[1];
			
			weight = cstf.rfloatLabel("weight");
			
			maxStackSize = cstf.rintLabel("stack limit");
			boolean noAnim = cstf.rtestNull("icon anim");
			if(noAnim) cstf.rname();
			else iconAnim = new SpriteSets(CSUtil.BigMixin.toNamePath(cstf.rlabel("icon anim")));
			
			int numberComponents = cstf.rlist("components");
			
			for(int i = 0 ; i < numberComponents ; i ++)  {
				
				rcomponent(cstf , cstf.rlist());
				cstf.endList();
			
			}
			
			cstf.endList();
			
			iconify();
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}

	@Override public void write(BufferedWriter writer , Object...additionalData) {}

	@Override public void load(BufferedReader reader) {}
	
}
