package Core.Entities;

import static CS.COLDSTEEL.assets;
import static CS.COLDSTEEL.data;
import static CSUtil.BigMixin.getArrayHeight;
import static CSUtil.BigMixin.getArrayMidpoint;
import static CSUtil.BigMixin.getArrayWidth;
import static CSUtil.BigMixin.getEntityFloatArray;
import static CSUtil.BigMixin.toLocalDirectory;
import static CSUtil.BigMixin.translateArray;
import static Renderer.Renderer.loadTexture;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;
import java.nio.file.Paths;
import java.util.BitSet;
import org.joml.Vector3f;
import AudioEngine.SoundEngine;
import AudioEngine.Sounds;
import CSUtil.CSTFParser;
import CSUtil.RefInt;
import CSUtil.DataStructures.CSArray;
import Core.CSType;
import Core.Direction;
import Core.ECS;
import Core.GameFiles;
import Core.HitBoxSets;
import Core.Quads;
import Core.SpriteSets;
import Game.Items.Inventories;
import Game.Items.Items;
import Renderer.Textures.ImageInfo;

public class Entities extends Quads implements GameFiles<Entities>{

	private static final int numberComponents = ECS.numberComps();
	
	public static final int HCOFF 		= ECS.HORIZONTAL_PLAYER_CONTROLLER.offset;
	public static final int CDOFF 		= ECS.COLLISION_DETECTION.offset;
	public static final int GCOFF 		= ECS.GRAVITY_CONSTANT.offset;
	public static final int HDOFF 		= ECS.HORIZONTAL_DISPLACEMENT.offset;
	public static final int VCOFF		= ECS.VERTICAL_PLAYER_CONTROLLER.offset;
	public static final int VDOFF 		= ECS.VERTICAL_DISPLACEMENT.offset;
	public static final int SOFF 		= ECS.SCRIPT.offset;
	public static final int AOFF 		= ECS.ANIMATIONS.offset;
	public static final int HOFF 		= ECS.HITBOXES.offset;
	public static final int RPGOFF 		= ECS.RPG_STATS.offset;
	public static final int CTOFF 		= ECS.CAMERA_TRACK.offset;
	public static final int DOFF 		= ECS.DIRECTION.offset;
	public static final int IOFF 		= ECS.INVENTORY.offset;
	public static final int FOFF 		= ECS.FLAGS.offset;
	public static final int AEOFF 		= ECS.AUDIO_EMIT.offset;
	
	private final Object[] comps = new Object[numberComponents + 1];
	private BitSet componentsBits = new BitSet(512); //used to know what components an entitiy has. 	
	protected String name;
	boolean freeze = false;
	private int LID = -1;
	private float[] defaultSprite = new float[6]; 
	
	{
		this.comps[0] = this;
	}
	
	public Entities(String name , int ID , ECS... components) {
		
		super(getEntityFloatArray() , ID , CSType.ENTITY);		
				
		this.name = name;
		addComponents(components);	
				
	}

	protected Entities(String name , int ID , CSType type , ECS... components) {
		
		super(getEntityFloatArray() , ID , type);		
				
		this.name = name;
		addComponents(components);	
				
	}
		
	public String toString() {
	
		return "Entity " + name + ", ID:  " + ID + " LID: " + LID;
		
	}
	
	public String name() {
		
		return name;
		
	}
	
	/**
	 * A name path is the name of the file + .CStf. It should refer to a file within the data / entities / folder
	 * 
	 * @return this name + .CStf
	 */
	public String namePath() {
		
		return name + ".CStf";
		
	}
	
	public Entities() {
		
		super(getEntityFloatArray() , -1 , CSType.ENTITY);
		comps[0] = this;
		
	}
	
	public Entities(String namePath) {
		
		super(getEntityFloatArray() , -1 , CSType.ENTITY);
		load(namePath);
		
	}
	
	protected void addComponents(ECS... component) {
		
		for(ECS x : component) {
			
			for(int i = x.offset , k = 0 ; k < x.data.length ; i++ , k++ ) this.comps[i] = x.data[k];
			componentsBits.set(x.index);
			
		}	
			
	}
	
	public void removeComponents(ECS...components) {
	
		for(ECS x : components) componentsBits.clear(x.index);
		
	}
	
	BitSet componentsBits() {
		
		return componentsBits;
		
	}

	public boolean has(ECS...queries) {
		
		for(ECS x : queries) if(!componentsBits.get(x.index)) return false;
		return true;
		
	}
	
	/**
	 * Returns true if this entity has any of the components provided. So long as this has one, true is returned. Otherwise, false is.
	 * 
	 * @param comps — vargs components to test 
	 * @return true if this entity has at least one of comps, otherwise false.
	 */
	public boolean hasAny(ECS...comps) {
		
		for(ECS x : comps) if(componentsBits.get(x.index)) return true;
		return false;
		
	}
	
	public Object[] components() {
		
		return comps;
		
	}

	public void moveTo(float x , float y) {
		
		float[] mid = getMidpoint();
		float deltaX = x - mid[0] , deltaY = y - mid[1];
		this.translate(deltaX , deltaY);
		
	}
	
	public float[] getMidpoint() {
		
		if(comps[CDOFF] != null) return CSUtil.BigMixin.getArrayMidpoint((float[]) comps[CDOFF]);
		else return super.getMidpoint();
		
	}
	
	public float xMid() {

		if(comps[CDOFF] != null) return CSUtil.BigMixin.arrayXMid((float[]) comps[CDOFF]);
		else return super.xMid();
		
	}

	public float yMid() {

		if(comps[CDOFF] != null) return CSUtil.BigMixin.arrayYMid((float[]) comps[CDOFF]);
		else return super.yMid();
		
	}
	
	public void translate(float horiz , float vert) {
		
		super.translate(horiz , vert);
		if(has(ECS.COLLISION_DETECTION)) if(comps[CDOFF] != null) comps[CDOFF] = translateArray((float[])comps[CDOFF] , horiz , vert);
						
	}
	
	int selectEntity(float cursorX , float cursorY) {
		
		return selectQuad(cursorX , cursorY);
		
	}
		
	void removeColor(Vector3f color) {
		
		removedColor = color;
		
	}
	
	protected void animate(int index , Direction targetDirection) {
		
		SpriteSets target;
		EntityAnimations anims = ((EntityAnimations)comps[AOFF]);
		
		if(!anims.animate()) return;
				
		//let the anim list's active animation be the one we play, or play a specific one if index is not -1
		if(index == -1) target = anims.active();
		else target = anims.get(index);
		
		if(target == null) return;		
		//if we are hungup, update variables and possibly end the hangup and possibly call onHangupEnd
		anims.handleHangup();
		
		if(target.defaultDirection != targetDirection) swapAndFlipSprite(target.swapSprite(true));
		else swapSprite(target.swapSprite(true));
		
		float[] current = target.getActiveSprite();
		//set a hitbox if possible //last element of the array is a hitbox to set as active
		if(has(ECS.HITBOXES) && current.length % 3 != 0) ((EntityHitBoxes)comps[HOFF]).activate((int) current[current.length - 1]);

	}
	
	ECS[] matching(ECS...comps) {
		
		ECS[] largeArr = new ECS[comps.length];
		int j = 0;
		for(int i = 0 ; i < comps.length ; i ++) if(componentsBits.get(comps[i].index)) {
			
			largeArr[j] = comps[i];
			j++;
			
		}
		
		ECS[] resizedArr = new ECS[j];
		System.arraycopy(largeArr, 0, resizedArr, 0, j);
		return resizedArr;
		
	}	
	
	public void freeze(boolean freeze) {
		
		this.freeze = freeze;
		
	}
	
	public boolean isFrozen() {
		
		return freeze;
		
	}
	
	/**
	 * LID, standing for level ID, is a number given to entities which equals their ID at the load of a level, but unlike ID, which changes 
	 * any time an entity is removed from the scene, LID stays the same and each entity will have a unique LID given by its addition into the scene.
	 * 
	 * For player entities, their LID will always be 0.
	 * 
	 * @return — this entity's Level ID
	 */
	public int LID() {
		
		return LID;
		
	}
	
	public void LID(int LID) {
		
		this.LID = LID;
		
	}
	
	public void setDefaultSprite() {
		
		float[] uvs = getUVs() , dims = dimensions();
		defaultSprite[0] = uvs[0];
		defaultSprite[1] = uvs[1];
		defaultSprite[2] = uvs[2];
		defaultSprite[3] = uvs[3];
		defaultSprite[4] = dims[0];
		defaultSprite[5] = dims[1];
		
	}
	
	public void resetToDefaultSprite() {
		
		swapSprite(defaultSprite);
		setWidth(defaultSprite[4]);
		setHeight(defaultSprite[5]);
		
	}

	/**
	 * Uses an existing BufferedWriter to write the data of a specified component to its file.
	 * 
	 * @param writer — the BufferedWriter to write the data
	 * @param comps — the caller's object array containing data for its components.
	 * @param target — what component to write
	 * @throws IOException — if an error occurs during the BufferedWriter's operation 
	 */
	public static final void writeComp(CSTFParser cstf , Object[] comps , ECS target) throws IOException {
		
		int off = target.offset;
		cstf.wlist(target.toString());
		
		switch(target) {		
		
			case HORIZONTAL_PLAYER_CONTROLLER:
								
				cstf.wlabelValue("speed" , (float)comps[off]);
							
			break;
			
			case COLLISION_DETECTION:
				
				float[] array = (float[])comps[off];
				
				if(array == null) cstf.wnullLabel("array"); 
				else {
					
					float[] arrayMid = getArrayMidpoint(array);
					float[] callerMid = ((Entities)comps[0]).getMidpoint();
					//array is laid out as width and height, x offset from owner, y offset from owner 
					cstf.wlabelValue("array", getArrayWidth(array) , getArrayHeight(array) , callerMid[0] - arrayMid[0] , callerMid[1] - arrayMid[1]);
					
				}
									
				cstf.wlabelValue("radius" , (float)comps[off + 1]);
				
			break;
				
			case GRAVITY_CONSTANT:
				
				cstf.wlabelValue("constant" , (float)comps[off]);
				cstf.wlabelValue("max additive" , (float)comps[off + 2]);
				cstf.wlabelValue("velocity" , (float)comps[off + 3]);
				
			break;
		
			case VERTICAL_PLAYER_CONTROLLER:
			
				cstf.wlabelValue("max jump time" , (float)comps[off + 1]);
				cstf.wlabelValue("jump velocity" , (float)comps[off + 2]);
								
			break;
			
			case SCRIPT:
			
				EntityScripts script = (EntityScripts)comps[off];
				if(script != null) cstf.wlabelValue("script" , script.scriptName());
				else cstf.wnullLabel("script");
								
			break;
			
			case ANIMATIONS:
				
				EntityAnimations anims = (EntityAnimations)comps[off];
				cstf.wlist("sprite sets", anims.size());
				for(int i = 0 ; i < anims.size() ; i ++) if(anims.get(i) != null) cstf.wvalue(anims.get(i).name());
				cstf.endList();				
				
			break;
			
			case HITBOXES:
				
				EntityHitBoxes boxes = (EntityHitBoxes)comps[off];
				cstf.wlabelValue("number boxes" , boxes.totalLength());
				
				cstf.wlist("hitboxes" , boxes.numberSets());
				for(HitBoxSets x : boxes) cstf.wvalue(x.name());
				cstf.endList();
				
			break;
			
			case RPG_STATS:
			
				EntityRPGStats stats = (EntityRPGStats)comps[off];
				RefInt number = new RefInt(0);
				stats.forEachStat((name , val) -> {
					
					if(val > 0) number.add();
					
				});
				
				cstf.wlist("stats" , number.get());
				
				stats.forValidStats((name , value) -> {
					
					if(value > 0) {
						
						try {
							
							cstf.wlabelValue(name , value);
							
						} catch(IOException e) {
						
							e.printStackTrace();
							
						}						
						
					}
					
				});
				
				cstf.endList();
												
				cstf.wlist("lsm" , 6);
				
				stats.forEachLSM((name , value) -> {

					try {
						
						cstf.wlabelValue(name, value);
						
					} catch (IOException e) {
						
						e.printStackTrace();
						
					}
									
				});
				
				cstf.endList();
				
				number.set(0);
				stats.forEachSkill((name , value) -> {
					
					if(value > 0) number.add();
					
				});
				
				cstf.wlist("skills" , number.get());
				stats.forValidSkills((name , value) -> {
					
					try {
						
						cstf.wlabelValue(name, value);
						
					} catch (IOException e) {
						
						e.printStackTrace();
						
					}
					
				});					
				
				cstf.endList();
								
			break;
			
			case CAMERA_TRACK:
				
				cstf.wlabelValue("horizontal offset" , (float)comps[off]);
				cstf.wlabelValue("vertical offset" , (float)comps[off + 1]);
				cstf.wlabelValue("zoom" , (float)comps[off + 2]);
				
				break;
				
			case DIRECTION:
				
				break;
				
			case HORIZONTAL_DISPLACEMENT:
								
				break;
				
			case INVENTORY:
				
				Inventories inv = (Inventories) comps[off];
				cstf.wlabelValue("weight limit" , inv.weightLimit());
				cstf.wlabelValue("x size" , inv.xDimension());
				cstf.wlabelValue("y size" , inv.yDimension());
				cstf.wlist("items" , inv.inventorySize() , () -> inv.getItems().forEachVal(tuple -> {
					
					try {
						
						cstf.wvalue(tuple.getFirst().name() + "," + tuple.getSecond()); 
						
					} catch (IOException e1) {
												
						e1.printStackTrace();
						
					}
					
				}));
				
				cstf.wlist("equipped" , inv.getEquipped().numberNonNullElements() , () -> inv.getEquipped().forEach(val -> {
					
					try {
						
						cstf.wvalue(val.name());
						
					} catch (IOException e1) {
						
						e1.printStackTrace();
						
					}
					
				}));
				
				break;
				
			case VERTICAL_DISPLACEMENT:
				
				break;
				
			case FLAGS:
				
				EntityFlags flags = ((EntityFlags)comps[off]);
				cstf.wlist("flags", flags.size(), () -> {
					
					flags.forEach((flag) -> {
						
						try {
							
							cstf.wvalue(flag);
							
						} catch (IOException e) {
							
							e.printStackTrace();
							
						}
						
					});
					
				});
								
				break;
				
			case AUDIO_EMIT:
				
				@SuppressWarnings("unchecked") CSArray<Sounds> sounds = (CSArray<Sounds>) comps[off];
				cstf.wlist("sounds" , sounds.size() , () -> sounds.forEach(sound -> {
					
					try {
			
						cstf.wvalue(sound.name());
						
					} catch (IOException e) {
						
						e.printStackTrace();
						
					}
					
				}));		
				
				break;
			
		}
		
		cstf.endList();
		
	}

	private void readComponentByName(CSTFParser cstf , String name) throws IOException, AssertionError {
		
		switch(name) {
			
			case "Horizontal Player Controller" -> {
				
				addComponents(ECS.HORIZONTAL_PLAYER_CONTROLLER);
				comps[HCOFF] = cstf.rfloatLabel("speed");
				
			}
			
			case "Vertical Player Controller" -> {
				
				addComponents(ECS.VERTICAL_PLAYER_CONTROLLER);
				comps[VCOFF + 1] = cstf.rfloatLabel("max jump time");
				comps[VCOFF + 2] = cstf.rfloatLabel("jump velocity");
				
			}
			
			case "Collision Detection" -> {
				
				addComponents(ECS.COLLISION_DETECTION);
				boolean isNull = cstf.rtestNull("array");
				if(isNull) {
					
					comps[CDOFF] = null;
					cstf.rname();
				
				} else {
					
					float[] array = new float[4];
					cstf.rlabel("array" , array);
					float[] collisionBound = CSUtil.BigMixin.getColliderFloatArray(); 
					collisionBound = CSUtil.BigMixin.changeColorTo(collisionBound , 0f , 0.75f , 0f);
					collisionBound = CSUtil.BigMixin.makeTranslucent(collisionBound , 0.25f);
					collisionBound = CSUtil.BigMixin.setArrayWidth(collisionBound , array[0]);
					collisionBound = CSUtil.BigMixin.setArrayHeight(collisionBound , array[1]);
					collisionBound = CSUtil.BigMixin.moveTo(this , collisionBound);
					collisionBound = CSUtil.BigMixin.translateArray(collisionBound , array[2] , -array[3]);
					comps[CDOFF] = collisionBound;
					
				}
				
				comps[CDOFF + 1] = cstf.rfloatLabel("radius");
				
			}
			
			case "Gravity Constant" -> {
				
				addComponents(ECS.GRAVITY_CONSTANT);
				comps[GCOFF] = cstf.rfloatLabel("constant");
				comps[GCOFF + 2] = cstf.rfloatLabel("max additive");
				comps[GCOFF + 3] = cstf.rfloatLabel("velocity");
				
			}
			
			case "Horizontal Displacement" -> addComponents(ECS.HORIZONTAL_DISPLACEMENT);
			case "Vertical Displacement" -> addComponents(ECS.VERTICAL_DISPLACEMENT);
			
			case "Animations" -> {
				
				addComponents(ECS.ANIMATIONS);
				
				int number = cstf.rlist("sprite sets");
				EntityAnimations anims = new EntityAnimations(number);
				comps[AOFF] = anims;
				
				for(int i = 0 ; i < number ; i ++) anims.add(new SpriteSets(cstf.rvalue() + ".CStf"));
				
				cstf.endList();
				
			}			
			
			case "Hitboxes" -> {
				
				addComponents(ECS.HITBOXES);
				
				EntityHitBoxes hitboxes = new EntityHitBoxes(cstf.rintLabel("number boxes"));
				comps[HOFF] = hitboxes;
				int number = cstf.rlist("hitboxes");
				
				for(int i = 0 ; i < number ; i ++) { 
				
					var set = new HitBoxSets(cstf.rvalue() + ".CStf");
					hitboxes.addSet(set);
										
				}
				
				cstf.endList();				
				
			}
			
			case "Rpg Stats" -> {
				
				addComponents(ECS.RPG_STATS);
				
				int number = cstf.rlist("stats");
				EntityRPGStats stats = new EntityRPGStats(this);
				comps[RPGOFF] = stats;
				int[] value = new int[1];
				String stat;
				for(int i = 0 ; i < number ; i ++) {
					
					stat = cstf.rlabel(value);
					stats.setCharacteristicForName(stat , value[0]);
					
				}
				
				cstf.endList();
				
				cstf.rlist("lsm");
				
				stats.currentLife(cstf.rfloatLabel("currentLife"));
				stats.maxLife(cstf.rfloatLabel("maxLife"));
				stats.currentStamina(cstf.rfloatLabel("currentStamina"));
				stats.maxStamina(cstf.rfloatLabel("maxStamina"));
				stats.currentMana(cstf.rfloatLabel("currentMana"));
				stats.maxMana(cstf.rfloatLabel("maxMana"));
				
				cstf.endList();
				
				number = cstf.rlist ("skills");
				for(int i = 0 ; i < number ; i ++) {
					
					stat = cstf.rlabel(value);
					stats.setSkillForName(stat, value[0]);
					
				}
				
				cstf.endList();
			
			}
			
			case "Camera Track" -> {
				
				addComponents(ECS.CAMERA_TRACK);
				comps[CTOFF] = cstf.rfloatLabel("horizontal offset");
				comps[CTOFF + 1] = cstf.rfloatLabel("vertical offset");
				comps[CTOFF + 2] = cstf.rfloatLabel("zoom");
								
			}
			
			case "Direction" -> {
				
				addComponents(ECS.DIRECTION);
				comps[DOFF] = Direction.RIGHT;
				comps[DOFF + 1] = Direction.UP;
				
			}
			
			case "Audio Emit" -> {
				
				addComponents(ECS.AUDIO_EMIT);
				
				int number = cstf.rlist("sounds");
				CSArray<Sounds> audio = new CSArray<Sounds>(number , 1);
				comps[AEOFF] = audio;
				
				for(int i = 0 ; i < number ; i ++) audio.add(SoundEngine.add(assets + "sounds/" + cstf.rvalue()));
				cstf.endList();
				
			}
			
			case "Inventory" -> {
				
				addComponents(ECS.INVENTORY);
				Inventories inventory = new Inventories(this);
				comps[IOFF] = inventory;
				inventory.weightLimit(cstf.rfloatLabel("weight limit"));
				inventory.xDimension(cstf.rintLabel("x size"));
				inventory.yDimension(cstf.rintLabel("y size"));

				int numberItems = cstf.rlist("items");
				for(int i = 0 ; i < numberItems ; i ++) {
				
					Items loaded = new Items(cstf.rvalue() + ".CStf");
					inventory.acquire(loaded);
					
				}
				
				cstf.endList();
				
				int equipped = cstf.rlist("equipped");
				for(int i = 0 ; i < equipped ; i ++) {
					
					Items newItem = new Items(cstf.rvalue() + ".CStf");					
					inventory.equip(newItem);
					
				}
				
				cstf.endList();
				
			}
			
			case "Flags" -> {
				
				addComponents(ECS.FLAGS);
				
				EntityFlags flags = new EntityFlags(10);
				comps[FOFF] = flags;
				int numberFlags = cstf.rlist("flags");
				
				String flagName;
				boolean[] state = new boolean[1];
				for(int i = 0 ; i < numberFlags ; i ++) {
					
					flagName = cstf.rlabel(state);
					flags.add(flagName);
					
				}
				
				cstf.endList();
				
			}
			
			case "Script" -> {
				
				addComponents(ECS.SCRIPT);
				
				boolean noFile = cstf.rtestNull("script");
				if(noFile) comps[SOFF] = null;
				else comps[SOFF] = new EntityScripts(this , cstf.rlabel("script"));
				
			}
			
		}
		
	}
	
	@Override public void delete() {
		
		GameFiles.delete(data + "entities/" + name + ".CStf");
		
	}

	@Override public void write(Object...additionalData) {

		try(BufferedWriter writer = newBufferedWriter(Paths.get(CS.COLDSTEEL.data + "entities/" + name + ".CStf") , Charset.forName("UTF-8"))){
			
			CSTFParser cstf = new CSTFParser(writer);
			
			cstf.wname(name);
			if(texture != null && texture.imageInfo != null)  cstf.wlabelValue("texture", toLocalDirectory(texture.imageInfo.path()));
			else cstf.wnullLabel("texture");
			cstf.wlabelValue("removed", removedColor.x , removedColor.y , removedColor.z);
			cstf.wlabelValue("filter" , filter.x , filter.y , filter.z);
			//if there is no default label ->
			if(defaultSprite[4] == 0 || defaultSprite[5] == 0) cstf.wnullLabel("default sprite");			
			else cstf.wlabelValue("default sprite" , defaultSprite);
			int numberComps = 0;
			for(ECS x : ECS.values()) if(has(x)) numberComps++;
			
			cstf.wlist("components", numberComps);						
			for(ECS x : ECS.values()) if(has(x) && x != ECS.SCRIPT) writeComp(cstf , comps , x);
			if(has(ECS.SCRIPT)) writeComp(cstf , comps , ECS.SCRIPT);
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}

	@Override public void load(String filepath) {
		
		try(BufferedReader reader = newBufferedReader(Paths.get(data + "entities/" + filepath))){
			
			CSTFParser cstf = new CSTFParser(reader);
			
			name = cstf.rname();
			String textureLocalDirectory = cstf.rlabel("texture");
			
			if(!textureLocalDirectory.equals("null")) loadTexture(texture , assets + textureLocalDirectory);			
			float[] array = new float[6];
			cstf.rlabel("removed" , array);
			removedColor.x = array[0]; removedColor.y = array[1]; removedColor.z = array[2];
			
			cstf.rlabel("filter" , array);
			filter.x = array[0]; filter.y = array[1]; filter.z = array[2];
			
			if(cstf.rtestNull("default sprite")) cstf.rname();
			else {
				
				cstf.rlabel("default sprite" , array);				
				defaultSprite = array;
				resetToDefaultSprite();
				
			} 			
			
			int numberComps = cstf.rlist("components");
			String componentName;
			
			for(int i = 0 ; i < numberComps ; i ++) {
				
				//read components			we know the top of this loop will always contain a list named the component whose data is in the list
				componentName = cstf.rlist();
				readComponentByName(cstf , componentName);
				cstf.endList();
				
			}
			
			cstf.endList();
			
			if(has(ECS.INVENTORY)) {
				
				Inventories inv = (Inventories)comps[IOFF];
				inv.getEquipped().forEach((x) -> {
					
					inv.equip(x);
					
				});
				
			}
			
		} catch (IOException e) {

			e.printStackTrace();
			System.exit(-1);

		}
		
	}

	@Override public void write(BufferedWriter writer , Object...additionalData) throws IOException {

		CSTFParser cstf = new CSTFParser(writer);
		
		cstf.wname(name);
		ImageInfo textureImageInfo = texture.imageInfo; 
		if(textureImageInfo != null)  cstf.wlabelValue("texture", toLocalDirectory(textureImageInfo.path()));
		else cstf.wnullLabel("texture");
		cstf.wlabelValue("removed", removedColor.x , removedColor.y , removedColor.z);
		cstf.wlabelValue("filter" , filter.x , filter.y , filter.z);
		//if there is no default label ->
		if(defaultSprite[4] == 0 || defaultSprite[5] == 0) cstf.wnullLabel("default sprite");			
		else cstf.wlabelValue("default sprite" , defaultSprite);
		int numberComps = 0;
		for(ECS x : ECS.values()) if(has(x)) numberComps++;
		
		cstf.wlist("components", numberComps);						
		for(ECS x : ECS.values()) if(has(x) && x != ECS.SCRIPT) writeComp(cstf , comps , x);
		if(has(ECS.SCRIPT)) writeComp(cstf , comps , ECS.SCRIPT);
		
	}

	@Override public void load(BufferedReader reader) {

		try {
			
			CSTFParser cstf = new CSTFParser(reader);
			
			name = cstf.rname();
			String textureLocalDirectory = cstf.rlabel("texture");
			
			if(!textureLocalDirectory.equals("null")) loadTexture(texture , assets + textureLocalDirectory);			
			float[] array = new float[6];
			cstf.rlabel("removed" , array);
			removedColor.x = array[0]; removedColor.y = array[1]; removedColor.z = array[2];
			
			cstf.rlabel("filter" , array);
			filter.x = array[0]; filter.y = array[1]; filter.z = array[2];
			
			if(cstf.rtestNull("default sprite")) cstf.rname();
			else {
				
				cstf.rlabel("default sprite" , array);				
				defaultSprite = array;
				resetToDefaultSprite();
				
			} 			
			
			int numberComps = cstf.rlist("components");
			String componentName;
			
			for(int i = 0 ; i < numberComps ; i ++) {
				
				//read components			we know the top of this loop will always contain a list named the component whose data is in the list
				componentName = cstf.rlist();
				readComponentByName(cstf , componentName);
				cstf.endList();
				
			}
			
			cstf.endList();
			
			if(has(ECS.INVENTORY)) {
				
				Inventories inv = (Inventories)comps[IOFF];
				inv.getEquipped().forEach((x) -> {
					
					inv.equip(x);
					
				});
				
			}
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}		
		
	}

}
