package Core.Entities;

import static CS.Engine.INTERNAL_ENGINE_PYTHON;

import org.joml.Random;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import CS.Engine;
import CS.GLFWWindow;
import CS.RuntimeState;
import Core.SpriteSets;
import Core.UIScript;
import Core.UIScriptingInterface;
import Core.Entities.EntityLists.hitboxScan;
import Core.ECS;
import Core.Scene;
import Core.Console;
import Core.Direction;
import Game.Core.DamageType;
import Game.Core.EntityHurtData;
import Game.Items.LootTables;
import Physics.MExpression;
import Renderer.ParticleEmitter;
import Renderer.Renderer;
/**
 * 
 * This class is known to entity scripting interpreter objects, and should serve as the main interface between entity scripts and the outside world
 * This is a singleton type object which holds references to everything, but exposes only what should be exposed to entities in the form of 
 * public methods. Those public methods can call nonpublic methods, but the scripts cannot call nonpublic methods directly. <br><br>
 * 
 * This class is entity agnostic and must therefore behave like a static class, which entails a worse API because calling scripts would have to 
 * pass their own entity into this classes functions. For this reason, a script will be called when an {@code EntityScriptInterpreter} is created
 * which defines functions which wrap methods of this class, passing in the owning entity's fields, making the API, hopefully, cleaner. <br><br>
 * 
 * However, scripts have access to this object (by necessity, so the script that wraps this class's methods can work), and thus can call them directly.
 * I could see this possibly boosting performance, but likely not enough to matter.
 * 
 * @author Chris Brown
 *
 */
public class EntityScriptingInterface {

	static final PyCode ENTITY_SCRIPTING_FACADE = INTERNAL_ENGINE_PYTHON().compile("CS_entityScriptingFunctions.py");
	
	private Scene scene;
	private GLFWWindow glfw;
	private PythonInterpreter internalInterpreter = new PythonInterpreter();
	private Random RNG = new Random();
	private Renderer renderer;
	private Console console;
	
	public EntityScriptingInterface(Renderer renderer , Scene scene , GLFWWindow window , Console console) {
	
		this.scene = scene;
		glfw = window;		
		this.renderer = renderer;
		this.console = console;
		
	}

	/**
	 * Adds a UI script object into a list of UI's to be rendered. This must be done in order to view a UI.
	 * 
	 * @param script a UIScript object
	 */
	public static void addUI(UIScript script) {
		
		UIScriptingInterface.addUI(script);
		
	}
	
	/**
	 * Removes a UIScript object from the UIScript list. After this call, the UI element will not longer be viewable or interactable.
	 * 
	 * @param script a UIScript
	 */
	public static void removeUI(UIScript script) {
		
		UIScriptingInterface.removeUI(script);
		
	}
	
	/**
	 * Moves E x amount. This will perform the double pass collision detection if E has the collision detection component and horizontal displacement component.
	 * 
	 * @param E — the calling entity
	 * @param x — a float horizontal distance
	 * @return true if it collided, else false (the return value does not reflect whether the object moved)
	 */
	public boolean moveHzntl(Entities E , float x) {
		
		return EntityLists.moveHorizChecked(E, x);
		
	}
	
	/**
	 * Moves E y amount. This will perform the double pass collision detection if E has the collision detection and vertical displacement components.
	 * 
	 * @param E — the calling entity
	 * @param y — a float representation of vertical distance in pixels
	 * @return true if a collision occurred, false otherwise.
	 */
	public boolean moveVrtcl(Entities E , float y) {
		
		return EntityLists.moveVertChecked(E, y);
		
	}
	
	/**
	 * Plays the animation at index of E's animation list, facing dir. <br><br> 
	 * This will call the {@code Quads} animate method, which does not check whether its possible to even play the animation. 
	 * Furthermore, an index out of bounds can be thrown if the index is invalid. <br><br> Be careful.
	 * 
	 * @param E — calling entity
	 * @param dir — direction enum, one of <br> {@code direction.UP} <br>{@code direction.LEFT} <br>{@code direction.RIGHT} or <br>{@code direction.DOWN}
	 * @param index — index into E's enityAnimations array of SpriteSets
	 */
	public void animate(Entities E , Direction dir , int index) {
	
		EntityLists.animate(E, dir, index);
		
	}
		
	/**
	 * Performs some collision scanning to find how distant this object is from the collider below it.
	 * 
	 * @param E — calling entity
	 * @return float representation of the distance between this entity and the floor below it, if any
	 */
	public float distanceToFloor(Entities E) {

		return EntityLists.getDistanceToFloor(E);		
		
	}
	
	/**
	 * Scans to find whether an item is available to be picked up. If an item is colliding with the entity and it is possible to pick up, it will be 
	 * added to the inventory.
	 * a
	 * @param E — calling entity
	 */
	public void findItems(Entities E) {
		
		scene.entities().findItems(E , scene.items());
		
	}
	
	public void findItemsByFlag(Entities E , String... flags) {
		
		scene.entities().findItemsByFlag(E , scene.items() , flags);
		
	}
	
	/**
	 * Toggles whether E can receive keyboard inputs for horizontal movement from GLFW. This does not preclude E from moving left and right, only 
	 * from receiving inputs to do so.
	 * 
	 * @param E — calling entity
	 */
	public void toggleHorizontalControl(Entities E) {
		
		EntityLists.toggleHorizontalControl(E);
		
	}
	
	/**
	 * Toggles whether E can receive GLFW inputs for vertical movement. E can still be moved up and down, but not by the keyboard.
	 * 	
	 * @param E — calling entity
	 */
	public void toggleVerticalControl(Entities E) {
		
		EntityLists.toggleVerticalControl(E);
		
	}
	
	/**
	 * Returns the SpriteSet object located at the active index of E's animation list.  
	 * 
	 * @param E — calling entity
	 * @return SpriteSet object reference to E's active SpriteSet.
	 */
	public SpriteSets activeAnimation(Entities E) {
		
		return EntityLists.getActiveAnim(E);
		
	}
	
	/**
	 * Returns the index marked as active in E's animation list object.
	 * 
	 * @param E — calling entity
	 * @return integer representing index of active SpriteSet
	 */
	public int activeAnimationIndex(Entities E) {
		
		return EntityLists.activeAnim(E);
		
	}
	
	/**
	 * Activates a SpriteSet specified by index. The EntityList will call animate on it, so {@code animate} does not need to be called inside the script.
	 * 
	 * @param E — calling entity
	 * @param index — index of animation to make active
	 */
	public void activateAnim(Entities E , int index) {
		
		EntityLists.setActiveAnim(E, index);
		
	}
		
	/**
	 * Sets whether underlying systems can perform auto orient on E. <br><br>
	 * Typically, this can be set to true so that underlying systems can orient the Entity based on its velocity. This allows it face the right direction
	 * based on how it's moving. In some cases though, its useful to stop auto orient. In this case, the entity will not orient itself automatically, 
	 * which will cause it to face one direction forever.
	 * 
	 * @param E — calling entity
	 * @param state — boolean representing whether this entity should auto orient itself
	 */
	public void setAutoOrient(Entities E , boolean state) {
		
		EntityLists.setAutoOrient(E, state);
		
	}
	
	/**
	 * Activates a hitbox belonging to E, located at index. This index represents the position in an array of hitboxsets which should be activated. Its
	 * therefore important to know what elements are where in E's EntityHitBoxes hitbox list.
	 * 
	 * @param E — calling entity
	 * @param index — index of hitbox to activate
	 */
	public void activateHitBox(Entities E , int index) {
		
		EntityLists.activateHitBox(E, index);
		
	}
	
	/**
	 * Used in the {@code entityScriptingFunctions} script to define a variable called glfw which is a reference to the window object, GLFWWindow.
	 * 
	 * @return — program's window, which includes user input
	 */
	public GLFWWindow getWindow() {
		
		return glfw;
		
	}
	
	/**
	 * Drops the specified item from E's inventory. <br><br>
	 * More specifically, removes the item from E's inventory if it exists, and begins to render it, after moving it in front of E.
	 * 
	 * @param E — calling entity
	 * @param drop — the item to be dropped
	 */
	public void dropItem() {
		
		EntityLists.dropItem();
		
	}

	/**
	 * Sets whether E can move itself horizontally with respect to the keyboard. 
	 * 
	 * @param E — calling entity
	 * @param state — true if keyboard inputs will move E left and right
	 */
	public void setHorizontalControl(Entities E , boolean state) {
		
		EntityLists.setHorizontalControl(E, state);
		
	}

	/**
	 * Sets whether E can move itself vertically. More specifically, if true, the vertical control system is processed for this entity
	 * 
	 * @param E — calling entity
	 * @param state — boolean representation of whether E can move vertically
	 */
	public void setVerticalControl(Entities E , boolean state) {
		
		EntityLists.setVerticalControl(E, state);
		
	}
		
	/**
	 * Returns the runtimeState of the program, as initially set by the passed argument
	 * 
	 * @return the Engine's state
	 */
	public RuntimeState getState() {
		
		return Engine.STATE;
		
	}
	
	/**
	 * Returns the editor console. This will return null if the program's runtimeState is not {@code runtimeState.Editor}
	 * 
	 * @return EditorConsole if possible, else null
	 */
	public Console getConsole() {
		
		return console;
		
	}
			
	/**
	 * Attempts to find an entity within radius pixels from caller, and if one is found, calls callback on it.
	 * Callback must take in one argument, that being the found entity. <br><br>
	 * This function is not very safe because it indirectly allows an entity to modify another in an albeit nonrobust way. 
	 * Higher level functions are preferential, but this is far more powerful.
	 * 
	 * @param caller — calling entity, the one trying to find other entities
	 * @param radius — the greatest distance an entity can be from caller to be counted
	 * @param callback — Python function to call, taking in the result of scan if one is present
	 * @return EntityScanResult, contains a package level reference to the entity found , and its x and y distance from the caller
	 */
	public EntityScanResult findEntityAnd(Entities caller , float radius , PyObject callback) {
		
		EntityScanResult  r = scene.entities().nearestEntity(caller, radius);
		if(r.result != null) {
			
			//I wish i could find a way to represent a java object as a PyObject which was cleaner than this, but the set method
			//sets a variable by the name of FEAE (the name of this method + E for entity) to the entity found by scanning, and uses
			//get method to get that variable. Get returns a PyObject, so we convert as we wanted to, in a roundabout way
			internalInterpreter.set("FEAE", r);
			callback.__call__(internalInterpreter.get("FEAE"));
			 	
		}
		
		return new EntityScanResult (r.result , r.xDistance , r.yDistance);
						
	}
	
	/**
	 * helper function that makes an entity face the direction specified. 
	 * 
	 * @param E — calling entity
	 * @param dir — direction for E to face
	 * @return direction E is now facing
	 */
	public Direction face(Entities E , Direction dir) {
		
		if(dir == Direction.RIGHT || dir == Direction.LEFT) E.components()[Entities.DOFF] = dir;
		else E.components()[Entities.DOFF + 1] = dir;
		return dir;
		
	}
	
	/**
	 * Makes E face the entity wrapped in scan.
	 * 
	 * @param E — callign entity
	 * @param scan — entity to face
	 * @param Direction E is now facing
	 */
	public Direction face(Entities E , EntityScanResult scan) {
		
		return face(E , EntityLists.horizontally(E, scan.result));
		
	}

	/**
	 * Returns true if E's current direction faces the entity wrapped in scanResult, else false.
	 * 
	 * @param E — a calling entity
	 * @param scanResult — an entity wrapped in a scan
	 * @return true if E is facing toward scanResult, else false
	 */
	public boolean facing(Entities E , EntityScanResult scanResult) {
		
		//E is facing scanRes if its horizontal direction is left and scanResult is to the left of it, or 
		//E is facing right and scanRes is to the right of it
		
		//E's current direction
		Direction dir = (Direction) E.components()[Entities.DOFF];
		float[] eMid = E.getMidpoint();
		float[] scanMid = scanResult.result.getMidpoint();
		
		if(dir == Direction.RIGHT) return scanMid[0] > eMid[0];
		else return eMid[0] > scanMid[0];		
		
	}
	
	/**
	 * Calls funtion on result field of entity. This function allows a script to modify an entity without having a reference to it and thus breaking
	 * encapsulation. However, for most purposes this function does break encapsulation, and should be avoided.
	 * 
	 * @param entity — EntityScanResult holding an entity to modify. Because the entity within EntityScanResult is not public, the {@code hasEntity()} 
	 * 				   method can be used to find whether this object has an entity at all. Note this function will not check for you.
	 * @param function — Python function to call, must take in only an EntityScanResult object as its parameter
	 */
	public void doTo(EntityScanResult entity , PyObject function) {
		
		if(entity.result != null) {
			
			internalInterpreter.set("DTE", entity.result);
			function.__call__(internalInterpreter.get("DTE"));
			
		}
		
	}	
	
	/**
	 * Performs a scan to find the nearest entity. This merely passes the result up into the script, doing no other operations on the result.
	 * <br><br>
	 * The EntityScanResult has four fields; Entities result , float xDistance , float yDistance , components[] matchingComps.
	 * The result field is hidden from scripts, but the other three are public and hence available. 
	 * 
	 * @param E — calling entity
	 * @param radius — the radius around this entity that an entity must be in to be considered
	 * @return EntityScanResult containing the results of the scan
	 */
	public EntityScanResult findEntity(Entities E , float radius) {
		
		return scene.entities().nearestEntity(E, radius);
		
	}
	
	/**
	 * Scans for other entities within radius that have at least one of comps.
	 * 
	 * @param E — entity scanning for others
	 * @param radius — maximum allowed distance an entity may be to scan it
	 * @param comps — array components entities will be tested with
	 * @return a scan result representing the result of the scan.
	 */
	public EntityScanResult findEntityWithAny(Entities E , float radius , ECS[] comps) {
		
		return scene.entities().nearestEntityWithAny(E, radius, comps);
				
	}
	
	/**
	 * Scans for other entities within radius that have all of comps.
	 * 
	 * @param E — entity scanning for others
	 * @param radius —maximum allowed distance an entity may be to scan it
	 * @param comps — array components entities will be tested with
	 * @return a scan result representing the result of the scan.
	 */
	public EntityScanResult findEntityWithAll(Entities E , float radius , ECS[] comps) {
		
		return scene.entities().nearestEntityWithAll(E, radius, comps);
				
	}	
	
	/**
	 * Scan's the hitboxes of E and scan.result to find if they are colliding, returning a hitboxScan record of that info.
	 * 
	 * @param E — calling entity
	 * @param scan — entity whose hitboxes are being queried
	 * @return hitboxScan instance representing info about the hitbox scan
	 */
	public hitboxScan scanHitBoxes(Entities E , EntityScanResult scan) {
		
		return scene.entities().checkHitBoxes(E, scan.result);
			
	}	
	
	/**
	 * Removes E from the scene permanently. This is distinct from deleting an entity, which deletes it's file out of the data/entities/ directory.
	 * In this case, the instance is removed.
	 * 
	 * @param E — calling entity
	 */
	public void remove(Entities E) {
		
		scene.entities().remove(E);
		
	}
	
	/**
	 * Returns a direction representing scan.result's orientation with respect to E.
	 * If scan is to the right of E, right is returned, else left is returned.
	 * 
	 * @param E — calling entity
	 * @param scan — other entity
	 * @return — direction indicating the horizontal orientation of scan with respect to E
	 */
	public Direction horizontally(Entities E , EntityScanResult scan) {
		
		return EntityLists.horizontally(E, scan.result);
		
	}
	
	/**
	 * Returns a direction representing E's orientation with respect to a generic EntityHurtData instance, most likely from a trigger.
	 * 
	 * @param E — an entity who was hurt
	 * @param data — EntityHurtData instance
	 * @return left if data is to the left of E, or right otherwise
	 */
	public Direction horizontally(Entities E , EntityHurtData data) {
		
		float[] EMid = E.getMidpoint();
		if(EMid[0] > data.x()) return Direction.LEFT;
		else if (EMid[0] < data.x()) return Direction.RIGHT;		
		return null;
		
	}
	
	/**
	 * Using JOML, generate a random int within the range of 0 to range -1.
	 * 
	 * @param range — a ceiling this generator can create
	 * @return a number between 0 and range -1.
	 */
	public int randomInt(int range) {
	
		return RNG.nextInt(range);
		
	}
	
	/**
	 * {@code callback} is called at the end of the current hangup in {@code animList}, with {@code input} as its input.
	 * 
	 * @param animList — an animation list whose {@code onHangupEnd} method will be invoked
	 * @param input — an input to {@code callback}
	 * @param callback — a python lambda or function to call
 	 */
	public void onHangupEnd(EntityAnimations animList , PyObject callback) {
		
		animList.onHangupEnd(() -> callback.__call__());
		
	}

	/**
	 * Gets the current displacement of E by calculating it's dispacement at the calling instant from it's position at the start of this tick 
	 * 
	 * @param E — calling entity
	 * @return — current speed, or more specifically immediate displacement
	 */
	public float currentSpeedX(Entities E) {
	
		Object[] comps;
		EntityLists.horizontalDisplacement(comps = E.components());
		return (float)comps[Entities.HDOFF + 1];
		
	}
	
	/**
	 * Queries GLFW on the most recent state of {@code key}
	 * 
	 * @param key — a glfw key enum
	 * @return — the most recent state of they key, one of {@code GLWF_KEY_PRESS} or {@code GLFW_KEY_RELEASE} 
	 */
	public int getKey(PyObject key) {
		
		return glfw.getKey((int)key.__tojava__(Integer.TYPE));
		
	}
	
	/**
	 * Creates and returns a particle emitter object. This function returns a particle emitter that is based on textures and animations.
	 * The particle emitter explicitly requires its update method to be called each frame its animations should update.
	 * 
	 * @param number — number of animated particles
	 * @param lifetime — lifetime in milliseconds for the kinematic impulse that acts on a particle
	 * @param xFunction — a math expression defining the horizontal movement of a particle;
	 * 					  this MExpression should either take 1 argument or none and if it takes one, 
	 * 					  that input will be the elapsed milliseconds since this emitter was started
	 * @param yFunction — a math expression defining the vertical movement of a particle;
	 * 					  this MExpression should either take 1 argument or none and if it takes one, 
	 * 					  that input will be the elapsed milliseconds since this emitter was started
	 * @param textureAbsPath — absolute path of the texture particles will display
	 * @param animAbsPath — absolute path of the animation particles will play 
	 * @param foreground — true if particles should be placed in the foreground, else they will be in the background
	 * @return — a newly created {@code ParticleEmitter} object
	 */
	public ParticleEmitter createParticleEmitter(int number , double lifetime , MExpression xFunction , MExpression yFunction , String textureAbsPath , String animAbsPath , boolean foreground) {
		
		return new ParticleEmitter(renderer, number , lifetime , xFunction , yFunction , textureAbsPath, animAbsPath , foreground);
		
	}
	
	/**
	 * Creates and returns a particle emitter object. This function returns a particle emitter that is based on nontextured pixels. These particles
	 * will be a solid color and it is not required to call an update function each frame for this particle emitter.
	 * 
	 * @param number — number of particles
	 * @param lifetime — lifetime in milliseconds for the kinematic impulse that acts on a particle
	 * @param xFunction — a math expression defining the horizontal movement of a particle;
	 * 					  this MExpression should either take 1 argument or none and if it takes one, 
	 * 					  that input will be the elapsed milliseconds since this emitter was started
	 * @param yFunction — a math expression defining the vertical movement of a particle;
	 * 					  this MExpression should either take 1 argument or none and if it takes one, 
	 * 					  that input will be the elapsed milliseconds since this emitter was started
	 * @param R — Red color value for particles 
	 * @param G — Green color value for particles
	 * @param B — Blue color value for particles
	 * @param width — Width of particles
	 * @param height — Height of particles
	 * @param foreground — true if particles should be placed in the foreground, else they will be in the background
	 * @return newly created {@code ParticleEmitter} object
	 */
	public ParticleEmitter createParticleEmitter(int number , double lifetime , MExpression xFunction , MExpression yFunction , float R , float G , float B , float width , float height , boolean foreground) {
		
		return new ParticleEmitter(renderer , number , lifetime , xFunction , yFunction , R , G , B , width , height , foreground); 
				
	}

	/**
	 * Creates and returns a particle emitter object. This function returns a particle emitter that is based on textures and animations.
	 * The particle emitter explicitly requires its update method to be called each frame its animations should update. 
	 * <br><br>
	 * The advantage of this version is that its animation is created and given externally, so it can be modified from the creator.
	 * 
	 * @param numberParticles — number of particles
	 * @param lifetimeMillis — lifetime in milliseconds for the kinematic impulse that acts on particles
	 * @param xFunction — a math expression defining the horizontal movement of a particle;
	 * 					  this MExpression should either take 1 argument or none and if it takes one, 
	 * 					  that input will be the elapsed milliseconds since this emitter was started
	 * @param yFunction — a math expression defining the vertical movement of a particle;
	 * 					  this MExpression should either take 1 argument or none and if it takes one, 
	 * 					  that input will be the elapsed milliseconds since this emitter was started
	 * @param textureAbsPath — absolute path of the texture particles will display
	 * @param animation — animation used for each particle
	 * @param foreground — true if particles should be placed in the foreground, else they will be in the background
	 * @return newly created {@code ParticleEmitter} object
	 */
	public ParticleEmitter createParticleEmitter(int numberParticles , double lifetimeMillis , MExpression xFunction , MExpression yFunction , String textureAbsPath , SpriteSets animation , boolean foreground) {
		
		return new ParticleEmitter(renderer , numberParticles , lifetimeMillis , xFunction , yFunction , textureAbsPath , animation , foreground);
		
	}
	
	/**
	 * Gets the active hitbox of the calling entity, throwing AssertionError if the caller does not have a hitboxes component. 
	 * 
	 * @param E — calling entity
	 * @return — index of active hitboxset
	 * @throws AssertionError if E does not have hitboxsets component
	 * 
	 */
	public int activeHitBoxIndex(Entities E) throws AssertionError{
		
		assert E.has(ECS.HITBOXES) : E.toString() + " does not have hitboxes component";		
		return ((EntityHitBoxes) E.components()[Entities.HOFF]).active();
		
	}
			
	public void hurtTarget(Entities attacker , EntityScanResult victim , DamageType damage) {
		
		float[] mid = attacker.getMidpoint();
		if(victim.result != null)
			((EntityRPGStats)victim.result.components()[Entities.RPGOFF]).hurt(new EntityHurtData(damage , mid[0] , mid[1]));
		
	}
	
	public LootTables newLootTable() {
		
		return new LootTables(scene);
		
	}
	
	public void shutDown() {
		
		internalInterpreter.cleanup();
		internalInterpreter.close();
		
	}
	
}
