package Core.TileSets;

import static CS.COLDSTEEL.data;
import static CS.COLDSTEEL.deleted;
import static CSUtil.BigMixin.toLocalPath;
import static CSUtil.BigMixin.toByte;
import static CSUtil.BigMixin.toBool;
import static Renderer.Renderer.loadTexture;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Predicate;

import CSUtil.CSTFParser;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;
import Core.AbstractGameObjectLists;
import Core.CSType;
import Core.GameFiles;
import Core.Quads;
import Core.Scene;
import Core.SpriteSets;
import Renderer.Textures;

/**
 * 
 * Tile Sets represent environment objects grouped by a common sprite sheet from which they are derived.
 * 
 * @author Chris Brown
 *
 */
public class TileSets extends AbstractGameObjectLists<Tiles> implements GameFiles<TileSets>{

	//list is a list of instances, sources is a list of sources to copy from
	private CSLinked<Tiles> sources = new CSLinked<Tiles>();
	
	private Quads tileSheet;
	private Textures texture = new Textures();
	private String name = "Unnamed Tile Set";
	private float[] remove = new float[3];
	
	public TileSets(Scene owningScene , String name , int renderOrder , Textures texture) {
		
		super(owningScene , renderOrder , CSType.TILE);
		this.name = name;
		this.texture = texture;
		
	}
	
	public TileSets(Scene owningScene , int renderOrder) {
		
		super(owningScene , renderOrder , CSType.TILE);
		
	}
	
	public boolean uninitialized() {
		
		return name.equals("Unnamed Tile Set") && sources.size() == 0;
		
	}
		
	public void setTileSheet(Quads tileSheet) {
		
		this.tileSheet = tileSheet;
		
	}
		
	public Quads getTileSheet() {
		
		return tileSheet;
		
	}

	public Quads select(float cursorX , float cursorY) {
				
		cdNode<Tiles> iter = list.get(list.size() - 1);
		for(int i = list.size() - 1 ; i >= 0 ; i-- , iter = iter.prev) if(iter.val.selectStatic(cursorX, cursorY) != -1) return iter.val;		
		return null;
		
	}

	
	public void addSourceTile(float[] UVsAndDims) {
		
		Tiles added = new Tiles(UVsAndDims , texture , "Untitled" , list.size());
		added.isSource(true);
		added.removeTextureColor(remove[0] , remove[1], remove[2]);
		sources.add(added);
		
	}
	
	public void addSourceTile(Tiles newTile) {
		
		newTile.setID(sources.size());
		sources.add(newTile);
		
	}
	
	public Tiles copy(Tiles copyThis) {
		
		Tiles copied = new Tiles(copyThis.tileData() , texture , copyThis.name() , copyThis.getID());
		copied.isSource(false);
		copied.setID(list.size());
		list.add(copied);
		
		copied.hasCollider(owningScene , copyThis.hasCollider());
		copied.removeColor(remove[0] , remove[1], remove[2]);
		if(copyThis.getAnimation() != null) copied.animation = copyThis.animation.copy();
		if(copied.hasCollider()) {
			
			if(copyThis.isColliderLowerLeftTriangle()) copied.toggleLowerLeftTriangle();
			else if (copyThis.isColliderLowerRightTriangle()) copied.toggleLowerRightTriangle();
			else if (copyThis.isColliderUpperLeftTriangle()) copied.toggleUpperLeftTriangle();
			else if (copyThis.isColliderUpperRightTriangle()) copied.toggleUpperRightTriangle();
			else if (copyThis.isColliderPlatform()) copied.togglePlatform();
						
			copied.colliderWidth(copyThis.colliderWidth());
			copied.colliderHeight(copyThis.colliderHeight());
			copied.moveCollider(copyThis.offset[0] , copyThis.offset[1]);
			
		}
		
		copyThis.addInstance(copied);
		return copied;
		
	}

	public Tiles copyDontAdd(Tiles copyThis) {
		
		Tiles copied = new Tiles(copyThis.tileData() , texture , copyThis.name() , copyThis.getID());
		copied.isSource(false);
		copied.setID(list.size());
		
		copied.hasCollider(owningScene , copyThis.hasCollider());
		copied.removeColor(remove[0] , remove[1], remove[2]);
		if(copyThis.getAnimation() != null) copied.animation = copyThis.animation.copy();
		if(copied.hasCollider()) {
			
			if(copyThis.isColliderLowerLeftTriangle()) copied.toggleLowerLeftTriangle();
			else if (copyThis.isColliderLowerRightTriangle()) copied.toggleLowerRightTriangle();
			else if (copyThis.isColliderUpperLeftTriangle()) copied.toggleUpperLeftTriangle();
			else if (copyThis.isColliderUpperRightTriangle()) copied.toggleUpperRightTriangle();
			else if (copyThis.isColliderPlatform()) copied.togglePlatform();
						
			copied.colliderWidth(copyThis.colliderWidth());
			copied.colliderHeight(copyThis.colliderHeight());
			copied.moveCollider(copyThis.offset[0] , copyThis.offset[1]);
			
		}
		
		copyThis.addInstance(copied);
		return copied;
		
	}
	
	public void removeTile(Tiles removeThis) {
		
		cdNode<Tiles> iter;
		if((iter = sources.removeIfHas(removeThis)) != null) {
			
			for(int i = iter.val.getID() ; i < sources.size() ; i ++ , iter = iter.next) iter.val.decrementID();
			
		}
		
	}
	
	public void remove(float[] rgb) {
		
		remove = rgb;		
		if(tileSheet != null) tileSheet.removeColor(rgb[0] , rgb[1] , rgb[2]);		
		
	}
	
	public String name() {
		
		return name;
		
	}

	public void name(String name) {
		
		this.name = name;
		
	}
	
	public Textures texture() {
	
		return texture;
		
	}	
	
	public void texture(String textureAbsPath) {
		
		loadTexture(texture , textureAbsPath);
		if(tileSheet != null) tileSheet.setTexture(this.texture);
		
	}
	
	public void forEachInstance(Consumer<Tiles> function) {
		
		super.forEach(function);
			
	}

	public void forEachSource(Consumer<Tiles> function) {
		
		sources.forEachVal(function);
		
	}
	
	public void forOnlySources(Predicate<Tiles> test , Consumer<Tiles> function) {
		
		sources.forOnlyVals(test, function);
		
	}
	
	public void forOnlyInstancesReversed(Predicate<Tiles> test , Consumer<Tiles> function) {
		
		list.forOnlyValsReversed(test , function);
		
	}
	
	public void forEachInstanceReversed(Consumer<Tiles> function) {
		
		list.forEachVal(function);
		
	}
	
	public void removeInstance(Tiles remove) {
		
		cdNode<Tiles> next = list.removeVal(remove);
		remove.hasCollider(owningScene , false);
		for(int i = remove.getID() ; i < list.size() ; i ++ , next = next.next) next.val.decrementID();
				
	}

	public void removeInstanceStandAlone(Tiles remove) {
		
		list.removeVal(remove);
		
	}
	
	public void moveInstanceToFront(int index) {
		
		list.moveTo(index , list.size() - 1);
		cdNode<Tiles> iter = list.get(0);		
		for(int i = 0 ; i < list.size() ; i ++ , iter = iter.next) {
		
			System.out.println("changing tile ID: " + iter.val.getID() + " to: " + i);
			iter.val.setID(i);
			
		}		
		
	}
	
	public void moveInstanceForward(int index) {
		
		if(index == list.size() - 1) return;
		Object[] quads = super.moveForward(index);
		int q0ID = ((Quads)quads[0]).getID();
		((Quads)quads[0]).setID(((Quads)quads[1]).getID());
		((Quads)quads[1]).setID(q0ID);
		
	}
	
	public void moveInstanceToBack(int index) {
		
		list.moveTo(index, 0);
		cdNode<Tiles>iter = list.get(0);
		for(int i = 0 ; i < list.size() ; i ++ , iter = iter.next) iter.val.setID(i);
		
	}
	
	public void moveInstanceBackward(int index) {
		
		if(index == 0) return;
		Object[] quads = super.moveBackward(index);
		int q0ID = ((Quads)quads[0]).getID();
		((Quads)quads[0]).setID(((Quads)quads[1]).getID());
		((Quads)quads[1]).setID(q0ID);
		
	}
	
	public float[] removed() {
		
		return remove;
		
	}
	
	public cdNode<Tiles> safeRemove(cdNode<Tiles> remove){
		
		cdNode<Tiles> removed = list.safeRemove(remove);
		for(int i = remove.val.getID() ; i < list.size() ; i ++ , removed = removed.next) removed.val.decrementID();		
		return removed;
		
	}
	
	public cdNode<Tiles> safeRemoveSource(cdNode<Tiles> remove){
		
		cdNode<Tiles> next = sources.safeRemove(remove);
		for(int i = remove.val.getID() ; i < list.size() ; i ++ , next = next.next) next.val.decrementID();		
		return next;
		
	}
	
	public int numberSourceTiles() {
		
		return sources.size();
		
	}
	
	public void addInstanceTile(Tiles addThis) {
		
		list.add(addThis);
		
	}
	
	public Tiles addInstanceTile(int sourceID) {
		
		Tiles source = sources.get(sourceID).val;
		Tiles copy = copy(source);
		return copy;
		
	}

	public Tiles addInstanceTile(String sourceName) {
		
		Tiles source = sources.getValIfExists(tile -> tile.name().equals(sourceName));
		if(source != null) return copy(source);		
		return null;
		
	}
	
	/**
	 * Identical to {@code addInstanceTile}, but this returns the tile instead of adding it to the list of instances. Although it is still added to
	 * its source tile's list of instances.
	 * 
	 * @param sourceName — name of a source tile
	 * @return copied tile.
	 */
	public Tiles copyInstanceTile(String sourceName) {
				
		Tiles source = sources.getValIfExists(tile -> tile.name().equals(sourceName));
		Tiles instance = copyDontAdd(source);
		return instance;		
				
	}
	
	public cdNode<Tiles> sourcesIter(){
		
		return sources.get(0);
		
	}
	
	public int numberInstances() {
		
		return list.size();
		
	}
	
	public int getSourceID(Tiles instance) {
		
		cdNode<Tiles> iter = sources.get(0);				
		for(int i = 0 ; i < sources.size() ; i ++ , iter = iter.next) if(iter.val.name().equals(instance.name())) return i;		
		return -1;
		
	}

	public int getSourceID(String name) {
		
		cdNode<Tiles> iter = sources.get(0);				
		for(int i = 0 ; i < sources.size() ; i ++ , iter = iter.next) if(iter.val.name().equals(name)) return i;		
		return -1;
		
	}
	
	public void setInstances(CSLinked<Tiles> instances) {
		
		this.list = instances;
		
	}
	
	public CSLinked<Tiles> getInstances(){
	
		return list;
		
	}	
	
	public void forOnlyInstances(Predicate<Tiles> test , Consumer<Tiles> function) {
	
		list.forOnlyVals(test, function);
		
	}	
	
	/**
	 * Removes all instances from the instances list AND from source tile's instances 
	 * 
	 */
	public void clearInstances() {
		
		sources.forEachVal(Tiles::clearInstances);
		
		list.clear();
				
	}
	
	private void clearSources() {
		
		sources.forEachVal(Tiles::removeMarked);
		sources.clear();
		
	}
	
	public void clear() {
		
		clearInstances();		
		clearSources();
		
	}
		
	public void animateTiles() {
			
//		forEachSource(tile -> {
//			
//			if(tile.animation != null) tile.forEachInstance(instance -> instance.animate());
//			
//		});
				
		forEachInstance(tile -> {
			
			if(tile.animation != null) tile.animate();
				
		});
		
	}
	
	public boolean containsNull() {
		
		return list.containsNull();
		
	}
	
	@Override public void delete() {
		
		try {
			
			Files.move(Paths.get(data +  "tilesets/" + name + ".CStf"), Paths.get(deleted + name + ".CStf"));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void write(Object... additionalData) {

		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(data + "tilesets/" + name + ".CStf") , Charset.forName("UTF-8"))){
			
			CSTFParser cstf = new CSTFParser(writer);
			
			cstf.wname(name);
			cstf.wlabelValue("texture" , toLocalPath(texture.filepath()));
			cstf.wlabelValue("remove", remove);
			
			cstf.wlist("tiles" , sources.size());
			
			forEachSource(tile -> {
			
				try {
					
					cstf.wlist(tile.name());
					cstf.wlabelValue("specs" , tile.tileData());
					var filter = tile.getFilter();
					cstf.wlabelValue("filter" , new float[] {filter.x , filter.y , filter.z});
					if(tile.animation == null) cstf.wnullLabel("animation");
					else cstf.wlabelValue("animation", (String) tile.animation.name());
					cstf.wlabelValue("collider" , tile.hasCollider());
					
					if(tile.hasCollider() && tile.colliderAdditionalData()) {
						
						cstf.wlist("data");
						cstf.wlabel("type");
						var type = tile.triangleType();
						cstf.wvalue(new byte[] {toByte(type.getFirst()) , toByte(type.getSecond()) , toByte(type.getThird()) , toByte(type.getFourth()) , toByte(tile.isColliderPlatform())});
						cstf.wlabelValue("dimensions" , tile.getColliderDimensions());
						cstf.wlabelValue("offset", tile.offset);
						cstf.endList();
						
					} 
					
					cstf.endList();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			});
			
			cstf.endList();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void write(BufferedWriter writer, Object... additionalData) {}

	@Override public void load(String namePath) {
		
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(data + "tileSets/" + namePath))){
			
			CSTFParser parser = new CSTFParser(reader);
			
			name = parser.rname();
			loadTexture(texture , parser.rlabel("texture"));
			parser.rlabel("remove", remove);			
			int numberTiles = parser.rlist("tiles");
			for(int i = 0 ; i < numberTiles ; i ++) {
				
				String tileName = parser.rlist();
				float[] specs = new float[6];
				parser.rlabel("specs", specs);
				float[] filter = new float[3];
				parser.rlabel("filter" , filter);
				
				Tiles newTile = new Tiles(specs , texture , tileName , list.size());
				
				boolean noAnimation = parser.rtestNull("animation");
				if(!noAnimation) newTile.animation = new SpriteSets(parser.rlabel("animation") + ".CStf");
				else parser.rname();
				
				boolean hasCollider = parser.rbooleanLabel("collider");
				
				newTile.isSource(true);
				newTile.setID(i);
				
				newTile.hasCollider(owningScene , hasCollider);
				sources.add(newTile);
				
				boolean additional = parser.rtest("data");
				if(additional) {
					
					parser.rlist();
					byte[] triangleType = new byte[5];
					parser.rlabel("type" , triangleType);
					
					if(toBool(triangleType[0])) newTile.toggleUpperRightTriangle();
					else if(toBool(triangleType[1])) newTile.toggleUpperLeftTriangle();
					else if(toBool(triangleType[2])) newTile.toggleLowerRightTriangle();
					else if(toBool(triangleType[3])) newTile.toggleLowerLeftTriangle();
					else if(toBool(triangleType[4])) newTile.togglePlatform();					
					
					float[] dimensions = new float[2];
					parser.rlabel("dimensions" , dimensions);
					
					newTile.colliderWidth(dimensions[0]);
					newTile.colliderHeight(dimensions[1]);
					
					float[] offset = new float[2];
					parser.rlabel("offset" , offset);
					newTile.offset = offset;
					parser.endList();
					
				}
				
				parser.endList();
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void load(BufferedReader reader) throws IOException {}
	
}
