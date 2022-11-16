package Game.Levels;

import static CSUtil.BigMixin.getCoordinateRectangleArea;
import static CSUtil.BigMixin.toNamePath;
import static Physics.MExpression.toNumber;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import CSUtil.CSTFParser;
import CSUtil.RefInt;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.Tuple3;
import CSUtil.DataStructures.cdNode;
import Core.Direction;
import Core.GameFiles;
import Core.Quads;
import Core.Scene;
import Core.Entities.Entities;
import Core.TileSets.TileSets;
import Core.TileSets.Tiles;
import Game.Player.PlayerCharacter;
/**
 * Playable world area containing entities, items, statics, etc. Players move between levels by traveling to areas that will load an adjacent level 
 * and move them to it when finished. Macro levels are asset managers that load and free textures that are needed and no longer needed.
 * 
 * Levels are saved as .CStf files under the archive of the macro level file. They thus cannot share a name with the macro level they are
 * associated with. 
 * 
 * 
 * @author Chris Brown
 *
 */
public class Levels implements GameFiles<Levels>{
		
	String gameName;
	String macroLevelName;
		
	//These lists represent value pairs of game objedts and their positions
	CSLinked<Tuple2<String , float[]>> entities = new CSLinked<>();
	CSLinked<Tuple2<String , float[]>> statics1 = new CSLinked<>();
	CSLinked<Tuple2<String , float[]>> statics2 = new CSLinked<>();
	CSLinked<Tuple2<String , float[]>> items = new CSLinked<>();	
	CSLinked<Tuple3<String , float[] , Integer>> backgroundTiles = new CSLinked<>();
	CSLinked<Tuple3<String , float[], Integer>> foregroundTiles = new CSLinked<>();
	String tileSet1 = null;
	String tileSet2 = null;
	//a quad is an untextured rectangle with a position, a width and height, a color for each vertex, top left, top right, bottom left, bottom right
	CSLinked<float[]> quads1 = new CSLinked<>();
	CSLinked<float[]> quads2 = new CSLinked<>();
	//these float arrays are laid out as: posX , posY , width , height , state (0 if box, 1 if upper right tri, 2 if upper left tri, 3 if lower right tri , 
	//4 if lower left tri, 5 if platform)
	CSLinked<float[]> colliders = new CSLinked<>();
	
	CSLinked<Triggers> triggers = new CSLinked<Triggers>();
	CSLinked<LevelLoadDoors> loadDoors = new CSLinked<>();
	Scene scene;
	
	public Levels(Scene scene , String name) {
		
		gameName = name;
		this.scene = scene;
		
	}
	
	/**
	 * Although typically a {@code GameFiles.load} method should only take in the name path of the file, but in this case we cannot do that 
	 * because the macro level is needed, so that has to be passed as part of the string.
	 * 
	 * @param filepath — a {@code CharSequence} filepath used to load a level.
	 */
	public Levels(Scene scene , CharSequence filepath) {
		
		this.scene = scene;
		load((String) filepath);		
		
	}

	/**
	 * Associates this level with some macrolevel
	 * 
	 * @param path
	 */
	public void associate(String path) {

		macroLevelName = toNamePath(path);
		
	}
	
	public String macroLevel() {
		
		return macroLevelName;
		
	}
	
	/**
	 * Runs all the scripts from this level and its triggers
	 * 
	 */
	public void runScripts() {
		
		cdNode<Triggers> iter = triggers.get(0);
		for(int i = 0 ; i < triggers.size() ; i ++ , iter = iter.next) iter.val.callScript();
		
	}
	
	private void deployTileSet(String tileSetName , boolean background , Scene deployingTo) {
	
		if(!tileSetName.equals("null")) {
			
			//if the tileset of this level is not the one already present in the scene, load this level's one into the scene
			if(!tileSetName.equals("Unnamed Tile Set.CStf") && !tileSetName.equals(deployingTo.tiles1().name())) deployingTo.tiles1().load(tileSetName + ".CStf");
			
			/*
			 * This array is used to map an instance of Tiles to its ID, which represents when it should render
			 * the index into the array also is its ID.
			 */
			Tiles[] tiles = background ? new Tiles[backgroundTiles.size()] : new Tiles[foregroundTiles.size()];
			//get the right list of tiles
			CSLinked<Tuple3<String , float[] , Integer>> sourceTiles = background ? backgroundTiles : foregroundTiles;
			
			cdNode<Tuple3<String , float[] , Integer>> iter = sourceTiles.get(0);
			for(int i = 0 ; i < sourceTiles.size() ; i ++ , iter = iter.next) {
				
				try {
										
					Tiles added = deployingTo.tiles1().copyInstanceTile(iter.val.getFirst());
					//this fixes issues regarding the first time something is animated it gets warped
					if(added.getAnimation() != null) added.animateFast();					
					added.moveTo(iter.val.getSecond());
					added.setID(iter.val.getThird());
					//this is the important line that ensures tiles get placed in the right order
					tiles[added.getID()] = added;
								
				} catch(NullPointerException e) {
					
					System.err.println("ERROR occurred loading tile: " + iter.val.getFirst() + ". Is this tile still available in Tile Set: " + tileSet1);
					
				}
							
			}
			
			for(int i = 0 ; i < tiles.length ; i ++) { 
				
				if(tiles[i] != null) {
					
					if(background) deployingTo.tiles1().addInstanceTile(tiles[i]);
					else if(tiles[i] != null) deployingTo.tiles2().addInstanceTile(tiles[i]);
					
				} else {
				
					if(background) System.err.println("Error: Possible level corruption. Background tile at index " + i + " null.");
					else System.err.println("Error: Possible level corruption. Foreground tile at index " + i + " null.");
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * This method will copy all entites, statics, and items into the scene.
	 */
	public void deploy(Scene scene) {
		
		scene.clear();
			
		entities.forEachVal(tuple -> scene.entities().loadEntity(tuple.getFirst() + ".CStf").moveTo(tuple.getSecond()));		
		statics1.forEachVal(tuple -> scene.statics1().loadStatic(tuple.getFirst() + ".CStf").moveTo(tuple.getSecond()));
		statics2.forEachVal(tuple -> scene.statics2().loadStatic(tuple.getFirst()).moveTo(tuple.getSecond()));
		items.forEachVal(tuple -> scene.items().load(tuple.getFirst() + ".CStf").moveTo(tuple.getSecond()));
		deployTileSet(tileSet1 , true , scene);
		deployTileSet(tileSet2 , false , scene);
	
		colliders.forEachVal(colliderArray -> scene.colliders().add(colliderArray));
		quads1.forEachVal(quad -> {
			
			Quads newQ = scene.quads1().add();
			newQ.setWidth(quad[2]);
			newQ.setHeight(quad[3]);
			newQ.moveTo(quad);
			newQ.quickChangeColor(2, quad[4], quad[5], quad[6]);
			newQ.quickChangeColor(3, quad[7], quad[8], quad[9]);
			newQ.quickChangeColor(0, quad[10], quad[11], quad[12]);
			newQ.quickChangeColor(1, quad[13], quad[14], quad[15]);
			
		});
		
		quads2.forEachVal(quad -> {
			
			Quads newQ = scene.quads2().add();
			newQ.setWidth(quad[2]);
			newQ.setHeight(quad[3]);
			newQ.moveTo(quad);
			newQ.quickChangeColor(2, quad[4], quad[5], quad[6]);
			newQ.quickChangeColor(3, quad[7], quad[8], quad[9]);
			newQ.quickChangeColor(0, quad[10], quad[11], quad[12]);
			newQ.quickChangeColor(1, quad[13], quad[14], quad[15]);
			
		});
		
	}
	
	/**
	 * Takes a snapshot of the current scene as present in the editor and <b>sets</b> this level to it.
	 * 
	 * @param editor — the editor whose scene is being snapshotted
	 */
	public void snapShotScene(Scene scene) {
		
		clear();
		
		scene.entities().forEach((E) -> entities.add(new Tuple2<>(E.name() , E.getMidpoint())));
		scene.statics1().forEach((S) -> statics1.add(new Tuple2<>(S.name() , S.getMidpoint())));
		scene.statics2().forEach((S) -> statics2.add(new Tuple2<>(S.name() , S.getMidpoint())));
		scene.items().forEach((I) -> items.add(new Tuple2<>(I.name() , I.getMidpoint())));
		tileSet1 = scene.tiles1().name();
		tileSet2 = scene.tiles2().name();
		//animated tiles need to be oriented along the y axis by their bottoms so when they animate their intented position is preserved.
		scene.tiles1().forEach(tile -> {
		
			backgroundTiles.add(new Tuple3<>(tile.name() , tile.getMidpoint() , tile.getID()));
			
		});
		
		scene.tiles2().forEach(tile -> {
			
			foregroundTiles.add(new Tuple3<>(tile.name() , tile.getMidpoint() , tile.getID()));
			
		});
		
		scene.colliders().forEach(collider -> {
			
			float[] mid = collider.getMidpoint() , dims = collider.dimensions();
			float state = 0;
			if(collider.isUpperRightTriangle()) state = 1;
			else if (collider.isUpperLeftTriangle()) state = 2;
			else if (collider.isLowerRightTriangle()) state = 3;
			else if (collider.isLowerLeftTriangle()) state = 4;
			else if (collider.isPlatform()) state = 5;
			colliders.add(new float[] {mid[0] , mid[1] , dims[0] , dims[1] , state});			
			
		});
		
		scene.quads1().forEach(quad -> {
			
			float[] topLeft = quad.getTopLeftVertexColor();
			float[] topRight = quad.getTopLeftVertexColor();
			float[] bottomLeft = quad.getTopLeftVertexColor();
			float[] bottomRight = quad.getTopLeftVertexColor();
			quads1.add(new float[] {quad.xMid() , quad.yMid() , quad.getWidth() , quad.getHeight() , 
				topLeft[0] , topLeft[1] , topLeft[2] , topRight[0] , topRight[1] , topRight[2] ,
				bottomLeft[0] , bottomLeft[1] , bottomLeft[2] , bottomRight[0] , bottomRight[1] , bottomRight[2]
			});
			
		});

		scene.quads2().forEach(quad -> {
			
			float[] topLeft = quad.getTopLeftVertexColor();
			float[] topRight = quad.getTopLeftVertexColor();
			float[] bottomLeft = quad.getTopLeftVertexColor();
			float[] bottomRight = quad.getTopLeftVertexColor();
			quads2.add(new float[] {quad.xMid() , quad.yMid() , quad.getWidth() , quad.getHeight() , 
				topLeft[0] , topLeft[1] , topLeft[2] , topRight[0] , topRight[1] , topRight[2] ,
				bottomLeft[0] , bottomLeft[1] , bottomLeft[2] , bottomRight[0] , bottomRight[1] , bottomRight[2]
			});
			
		});
		
		write(scene.tiles1() , scene.tiles2());
		
	}
	
	public void addTrigger(String name) {
		
		triggers.add(new Triggers(scene , name , triggers.size()));
		
	}
	
	public void forEachTrigger(Consumer<Triggers> function) {
		
		triggers.forEachVal(x -> function.accept(x));
		
	}
	
	public void forEachLoadDoor(Consumer<LevelLoadDoors> function) {
		
		loadDoors.forEachVal(function);
		
	}
	
	public boolean empty() {
		
		return entities.empty() && 
			   statics1.empty() && 
			   statics2.empty() && 
			   items.empty() && 
			   backgroundTiles.empty() && 
			   foregroundTiles.empty() && 
			   colliders.empty() && 
			   quads1.empty() && 
			   quads2.empty();
		
	}
	
	public void clear() {
		
		entities.clear();
		statics1.clear();
		statics2.clear();
		items.clear();
		backgroundTiles.clear();
		foregroundTiles.clear();
		colliders.clear();
		quads1.clear();
		quads2.clear();
		
	}
	
	public CSLinked<Triggers> triggers(){
		
		return triggers;
		
	}
	
	public void removeTrigger(Triggers removeThis) {
		
		triggers.removeVal(removeThis);
		
	}
	
	public Tuple2<String , float[]> getEntity(int index){
		
		return entities.getVal(index);
		
	}
	
	public void addLoadDoor(String loadDoorName) {
		
		loadDoors.add(new LevelLoadDoors(loadDoorName));
		
	}
	
	public int numberLoadDoors() {
		
		return loadDoors.size();
		
	}
	
	public LevelLoadDoors getLoadDoorByName(String name) {
		
		cdNode<LevelLoadDoors> iter = loadDoors.get(0);
		for(int i = 0 ; i < loadDoors.size() ; i ++ , iter = iter.next) if(iter.val.thisLoadDoorName.equals(name)) return iter.val;		
		return null;		
		
	}
	
	public cdNode<LevelLoadDoors> loadDoorsIter(){
		
		return loadDoors.get(0);
		
	}

	public String gameName() {
		
		return gameName;
		
	}
	
	/**
	 * Checks the collision bounds of the player's entity against all level load doors in this level. The player's previously used load door
	 *  will be skipped if it exists. Once the player collides with a load door, their previously used load door field will be set.
	 * 
	 * @param player — a PlayerCharacter 
	 * @return — the load door the player collided with, or null.
	 */
	public LevelLoadDoors playerCollidingWithLoadDoor(PlayerCharacter player) {
		
		Entities entity = player.playersEntity();
		
		//player's collision bound is what we always use for collision detection, not their quad. CDOFF is the array of the collision bound.
		float[] playerVData = (float[]) entity.components()[Entities.CDOFF];
		float[] doorVData;
		
		cdNode<LevelLoadDoors> iter = loadDoors.get(0);
		for(int i = 0 ; i  < loadDoors.size() ; i ++ , iter = iter.next) {
						
			doorVData = iter.val.conditionArea.getData();
			boolean isColliding = false;
			//start by checking horizontal collisions. if we did not collide here, we will check vertically
			if((Direction) entity.components()[Entities.DOFF] == Direction.RIGHT) {
				
				if(playerVData[28] < doorVData[19] && playerVData[10] > doorVData[28] && playerVData[27] < doorVData[18] ){
					
					int XArea = (int) getCoordinateRectangleArea(doorVData[27] , doorVData[28] , doorVData[18] , doorVData[19] , doorVData[0] , doorVData[1]);
					int targetArea = (int)getCoordinateRectangleArea(playerVData[0] , playerVData[1] , doorVData[18] , doorVData[19] , doorVData[0] , doorVData[1]);
					isColliding = XArea > targetArea;
					
				}
				
			} else {
								
				if(playerVData[28] < doorVData[19] && playerVData[10] > doorVData[28] && playerVData[27] < doorVData[18] ){

					int XArea = (int) getCoordinateRectangleArea(doorVData[27] , doorVData[28] , doorVData[18] , doorVData[19] , doorVData[0] , doorVData[1]);
					int targetArea = (int)getCoordinateRectangleArea(playerVData[27] , playerVData[28] , doorVData[18] , doorVData[19] , doorVData[0] , doorVData[1]);
					isColliding = XArea > targetArea;
				
				}

			}
			
			//here we check vertically
			if(!isColliding) {
				
				if(playerVData[28] < doorVData[19] && playerVData[10] > doorVData[28] && playerVData[27] < doorVData[18] ){

					int XArea = (int) getCoordinateRectangleArea(doorVData[27] , doorVData[28] , doorVData[18] , doorVData[19] , doorVData[0] , doorVData[1]);
					int targetArea = (int)getCoordinateRectangleArea(playerVData[0] , playerVData[1] , doorVData[18] , doorVData[19] , doorVData[0] , doorVData[1]);
					if(XArea > targetArea) return iter.val;
				
				}
				
			} else return iter.val;
			
		}
		
		return null;
	
	}
	
	public Quads selectLoadDoors(float cursorWorldX , float cursorWorldY) {
		
		Quads doorArea = null;
		cdNode<LevelLoadDoors> iter = loadDoors.get(0);
		for(int i = 0 ; i < loadDoors.size() ; i ++ , iter = iter.next) if(iter.val.selectConditionArea(cursorWorldX, cursorWorldY) != -1) {
			
			doorArea = iter.val.conditionArea;
			break;
			
		}
		
		return doorArea;
		
	}

	public Quads selectTriggerAreas(float cursorWorldX , float cursorWorldY) {
		
		Quads triggerArea = null;
		cdNode<Triggers> iter = triggers.get(0);
		for(int i = 0 ; i < triggers.size() ; i ++ , iter = iter.next) if((triggerArea = iter.val.selectQuads(cursorWorldX, cursorWorldY)) != null) break;
		
		return triggerArea;
		
	}
	
	public MacroLevels associatedMacroLevel() {
		
		return new MacroLevels((CharSequence)(macroLevelName + ".CStf"));
		
	}
	
	public String tileSet1() {
		
		return tileSet1;
		
	}

	public String tileSet2() {
		
		return tileSet2;
		
	}

	public CSLinked<Tuple2<String , float[]>> entities(){
		
		return entities;
		
	}

	public CSLinked<Tuple2<String , float[]>> items(){
		
		return items;
		
	}

	public CSLinked<Tuple2<String , float[]>> backgroundStatics(){
		
		return statics1;
		
	}

	public CSLinked<Tuple2<String , float[]>> foregroundStatics(){
		
		return statics2;
		
	}

	public CSLinked<Tuple3<String , float[] , Integer>> backgroundTiles(){
		
		return backgroundTiles;
		
	}

	public CSLinked<Tuple3<String , float[] , Integer>> foregroundTiles(){
		
		return foregroundTiles;
		
	}
	
	private void writeList(CSTFParser cstf , CSLinked<Tuple2<String , float[]>> list) throws IOException {
		
		cdNode<Tuple2<String , float[]>> iter = list.get(0);
		for(int i = 0 ; i < list.size() ; i ++ , iter = iter.next) cstf.wlabelValue(iter.val.getFirst(), iter.val.getSecond());
		
	}
	
	private void readList(String header , CSTFParser cstf , CSLinked<Tuple2<String , float[]>> list) throws IOException, AssertionError {
		
		int number = cstf.rlist(header);
		for(int i = 0 ; i < number ; i ++) {
			
			float[] pos = new float[2];
			list.add(new Tuple2<String , float[]>(cstf.rlabel(pos) , pos));
			
		}
		
		cstf.endList();
		
	}
	
	private void writeTileSet(String tileSetName , CSTFParser cstf , TileSets TS) throws IllegalStateException, IOException {

		if(!TS.uninitialized()) {
			
			RefInt numberValidSources = new RefInt(0);			
			TS.forOnlySources(source -> source.numberInstances() > 0 , source -> numberValidSources.add());
			
			cstf.wlist(TS.name() , numberValidSources.get());
			//for each source that has instances
			TS.forOnlySources(source -> source.numberInstances() > 0 , source -> {
				
				try {
					
					cstf.wlist(source.name() , source.numberInstances());
					source.forEachInstance(instance -> {
						
						try {
							
							cstf.wlabelValue("" + instance.getID() , instance.getMidpoint());
							
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						
						
					});
					
					cstf.endList();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
									
			});
			
			cstf.endList();
			
		} else cstf.wnullLabel(tileSetName);
		
	}		
	
	private String readTileSet(CSTFParser cstf , CSLinked<Tuple3<String , float[] , Integer>> tiles) throws IOException {
		
		String tileSetName;
		boolean noTileSet = cstf.rtestNull();
		if(!noTileSet) {
			
			//name of tile set and number instances
			Tuple2<String , Integer> nameAndSize = new Tuple2<>();
			cstf.rlist(nameAndSize);
			tileSetName = nameAndSize.getFirst();					
			
			//iterate over sources
			for(int i = 0 ; i < nameAndSize.getSecond() ; i ++) {
				
				Tuple2<String , Integer> sourceNameAndNumberInstances = new Tuple2<>();
				
				cstf.rlist(sourceNameAndNumberInstances);
				float[] position; 
				for(int j = 0 ; j < sourceNameAndNumberInstances.getSecond() ; j ++) {
					
					position = new float[2];					
					String label = cstf.rlabel(position);
					int ID = (int) toNumber(label);
					tiles.add(new Tuple3<>(sourceNameAndNumberInstances.getFirst() , position , ID));
					
				}
				
				cstf.endList();
				
			}
			
			cstf.endList();
			
		} else {
			
			tileSetName = "null";
			cstf.rname();//used to advance one line
		
		}
		
		return tileSetName;
		
	}
	
	public int hashCode() {
		
		return macroLevelName.concat(gameName).hashCode();				
		
	}
	
	@Override public void delete() {

		try {
			
			Files.move(Paths.get(CS.COLDSTEEL.data + "/macrolevels/" + macroLevelName + "/" + gameName),
					Paths.get(CS.COLDSTEEL.deleted + "/macrolevels/" + macroLevelName + "/" + gameName));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void write(Object...additionalData) {

		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(CS.COLDSTEEL.data + "/macrolevels/" + macroLevelName + "/" + gameName + ".CStf"))){
			
			CSTFParser cstf = new CSTFParser(writer);
			cstf.wname(gameName);
			cstf.wlabelValue("macro level" , macroLevelName);
			
			cstf.wlist("entities" , entities.size());			 
			writeList(cstf , entities);		
			cstf.endList();
			
			cstf.wlist("statics 1" , statics1.size());			
			writeList(cstf , statics1);	
			cstf.endList();
			
			cstf.wlist("statics 2" , statics2.size());
			writeList(cstf , statics2);
			cstf.endList();
			
			cstf.wlist("items" , items.size());
			writeList(cstf , items);			
			cstf.endList();
			
			TileSets TS = (TileSets) additionalData[0];
			writeTileSet("tile set 1" , cstf , TS);			
			TS = (TileSets)additionalData[1];
			writeTileSet("tile set 2" , cstf , TS);
			
			cstf.wlist("triggers" , triggers.size());

			triggers.forEachVal(trigger -> {
				
				try {
					
					cstf.wvalue(trigger.name());
										
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			});
			
			cstf.endList();
			
			cstf.wlist("load doors" , loadDoors.size());
			
			loadDoors.forEachVal(door -> {
				
				try {

					cstf.wlist(door.thisLoadDoorName());
					cstf.wlabelValue("specs" , door.conditionAreaSpecs());
					cstf.wlabelValue("linked level" , door.linkedLevel());
					cstf.wlabelValue("linked door" , door.linkedLoadDoorName());
					cstf.endList();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			});
			
			cstf.endList();
					
			cstf.wlist("colliders", colliders.size());
			cdNode<float[]> iter = colliders.get(0);				
			for(int i = 0 ; i < colliders.size() ; i ++ , iter = iter.next) cstf.wvalue(iter.val);
			cstf.endList();
			
			cstf.wlist("quads 1" , quads1.size());			
			iter = quads1.get(0);
			for(int i = 0 ; i < quads1.size() ; i ++ , iter = iter.next) cstf.wvalue(iter.val);			
			cstf.endList();			

			cstf.wlist("quads 2" , quads2.size());			
			iter = quads2.get(0);
			for(int i = 0 ; i < quads2.size() ; i ++ , iter = iter.next) cstf.wvalue(iter.val);			
			cstf.endList();			
			
		} catch (Exception e) {
			
			System.err.println("ERROR: failed to write " + gameName);
			e.printStackTrace();
			
		}
		
	}

	@Override public void load(String filepath) {

		try(BufferedReader reader = Files.newBufferedReader(Paths.get(filepath))){
			
			CSTFParser cstf = new CSTFParser(reader);
			
			gameName = cstf.rname();			
			macroLevelName = cstf.rlabel("macro level");
						
			readList("entities" , cstf , entities);
			readList("statics 1" , cstf , statics1);
			readList("statics 2" , cstf , statics1);
			readList("items" , cstf , items);

			tileSet1 = readTileSet(cstf , backgroundTiles);
			tileSet2 = readTileSet(cstf , foregroundTiles);
						
			int numberTriggers = cstf.rlist("triggers");
			for(int i = 0 ; i < numberTriggers ; i ++) triggers.add(new Triggers(scene , cstf.rlabel() , i));
			
			cstf.endList();
			
			int numberLoadDoors = cstf.rlist("load doors");
			for(int i = 0 ; i < numberLoadDoors ; i ++) {
				
				String loadDoorName = cstf.rlist();
				LevelLoadDoors newLoadDoor = new LevelLoadDoors(loadDoorName);
				loadDoors.add(newLoadDoor);
				float[] specs = new float[4];
				cstf.rlabel("specs" , specs);
				newLoadDoor.conditionArea.setWidth(specs[0]);
				newLoadDoor.conditionArea.setHeight(specs[1]);
				newLoadDoor.conditionArea.moveTo(specs[2] , specs[3]);
				String linkedLevel = cstf.rlabel("linked level");
				if(linkedLevel.equals("null")) {
					
					newLoadDoor.linkToLevel("null");
					newLoadDoor.linkToLoadDoor(cstf.rlabel("linked door"));
					
				} else {
					
					newLoadDoor.linkToLevel(linkedLevel);
					newLoadDoor.linkToLoadDoor(cstf.rlabel("linked door"));
					
				}
				
				cstf.endList();
				
			}
			
			cstf.endList();
						
			int number = cstf.rlist("colliders");
			for(int i = 0 ; i < number ; i ++) {
				
				float[] colliderData = new float[5];
				cstf.rvalue(colliderData);
				colliders.add(colliderData);
				
			}
			
			cstf.endList();
			
			number = cstf.rlist("quads 1");
			for(int i = 0 ; i < number ; i++) {
				
				float[] quad = new float[16];
				cstf.rvalue(quad);
				quads1.add(quad);
				
			}
			
			cstf.endList();

			number = cstf.rlist("quads 2");
			for(int i = 0 ; i < number ; i++) {
				
				float[] quad = new float[16];
				cstf.rvalue(quad);
				quads2.add(quad);
				
			}
			
			cstf.endList();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void write(BufferedWriter writer , Object...additionalData) {}

	@Override public void load(BufferedReader reader) {}
		
}