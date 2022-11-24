package Physics;

import java.util.function.Supplier;

import org.python.core.PyObject;

import CS.COLDSTEEL;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.cdNode;
import Core.Executor;
import Core.Quads;
import Core.Scene;
import Core.Tester;
import Core.Entities.Entities;
import Game.Items.Items;

/**
 * An API for moving game objects with respect to time in linear and exponential growth and decay forms. Call {@code impulse()} passing in an
 * object and parameters for the impulse. This class handles the rest.
 * 
 * 
 * @author Chris Brown
 *
 */
public class Kinematics {

	public static float maxSpeed = 100f;
	public static float minSpeed = -100f;
	
	Scene owner;
	
	public Kinematics(Scene owner){
		
		this.owner = owner;
		
	}
	
	/**
	 * Holds a Queue of kinematic forces and an object they operate on
	 *
	 */
	private class Pair {
		
		Quads obj;
		CSLinked<KinematicForce> queue = new CSLinked<KinematicForce>();
		
		Tester killTest = null;
		
		Pair(Quads obj , KinematicForce force){
			
			this.obj = obj;
			queue.add(force);
								
		}
		
		void killIf(Tester test) {
			
			this.killTest = test;					
			
		}
		
		cdNode<KinematicForce> current() {
			
			return queue.get(0);
						
		}
		
		boolean empty() {
			
			return queue.size() == 0;
			
		}
		
		boolean shouldKill() {
			
			if(!(killTest == null)) return killTest.test();			
			return false;
			
		}
						
	}
			
	private CSLinked<Pair> forces = new CSLinked<>();	

	public float [] values = new float[2];	
	
	/**
	 * Creates a new managed impulse. This impulse will persist on the object until it's time has run out.
	 * 
	 * @param type — ForceType enum value for which type of force desired.
	 * @param timeMillis — number of milliseconds this impulse will last
	 * @param initialX — initial step horizontally, this is mainly used for grow and decay force types
	 * @param initialY — initial step vertically, this is mainly used for grow and decay force types
	 * @param stepX — step per tick horizontally this impulse will exert, may be modified if type dictates
	 * @param stepY — step per tick vertically this impulse will exert, may be modified if type dictates
	 * @param target — an entity who will be the subject of this  impulse
	 * 
	 */
	public void impulse(ForceType type , double timeMillis , float initialX , float initialY , float stepX , float stepY , Quads target) {
		
		forces.add(new Pair(target , new KinematicForce(type , timeMillis , initialX , initialY , stepX , stepY)));
		
	}
	
	/**
	 * Creates a new managed impulse. This impulse wil persist until on the object until it's time has elapsed. 
	 * <br><br>
	 * The created impulse will invoke it's XFunction and YFunction in order to get its update values. The KinematicForce's elapsed time
	 * in millis will be passed into each function as input. Other variables max not exist within the expressions.
	 * 
	 * @param timeMillis — lifetime in milliseconds of this force 
	 * @param XFunction — an MExpression whose output will be the applied target in the horizontal plane
	 * @param YFunction — an MExpression whose output will be the applied target in the vertical plane
	 * @param target — an object to subject to this impulse;
	 */
	public void impulse(double timeMillis , MExpression XFunction , MExpression YFunction , Quads target) {
	
		forces.add(new Pair(target , new KinematicForce(timeMillis , XFunction , YFunction)));
		
	}
	
	/**
	 * Adds a pre-created KinematicForce to the list of kinematic forces to process. After it's time is finished, it will be removed from the list.
	 * 
	 * @param impulse — an already made force to exert
	 * @param target — an object to receive impulse
	 */
	public void impulse(KinematicForce impulse , Quads target) {
	
		forces.add(new Pair(target , impulse));
		
	}
	
	/**
	 * Creates a new managed impulse which will only take effect after the most recently added impulse ends. This method must be called
	 * immediately after a call to {@code impulse} or {@code then}, so it can be placed into a queue of actions.
	 * <br><br>
	 * <b>Usage:</b> <br>{@code 
	 * 
		 Kinematics.impulse(...);
		 Kinematics.then(...);
		 Kinematics.then(...);
	 * 
	 * }
	 * 
	 * A queue is created with a call to {@code impulse} and calls to then add to the most recently created queue.
	 * 
	 * @param type — Force Type, one of LINEAR, LINEAR_DECAY, LINEAR_GROW, EXPONENTIAL_DECAY, EXPONENTIAL_GROW
	 * @param timeMillis — time in milliseconds this force will last, may not affect some force types
	 * @param initialX — starting horizontal step
	 * @param initialY — starting vertical step
	 * @param stepX — incremental horizontal step
	 * @param stepY — incremental vertical step
	 */
	public void then(ForceType type , double timeMillis , float initialX , float initialY , float stepX , float stepY) {
		
		forces.tail().val.queue.add(new KinematicForce(type , timeMillis , initialX , initialY , stepX , stepY));		
		
	}
	
	/**
	 * Creates a new managed impulse which will only take effect after the most recently added impulse ends. This method must be called 
	 * immediately after a call to {@code impulse} or {@code then}, after which it is placed ito a queue of actions.
	 * <br><br>
	 * This force's x and y output will be given by the outputs of XFunction and YFunction, which must take in only one variable, which will be
	 * the elapsed time in milliseconds of the force. 
	 * 
	 * @param timeMillis — lifetime in milliseconds of this force 
	 * @param XFunction — an MExpression whose output will be the applied target in the horizontal plane
	 * @param YFunction — an MExpression whose output will be the applied target in the vertical plane
 	 */
	public void then(double timeMillis , MExpression XFunction , MExpression YFunction) {
		
		forces.tail().val.queue.add(new KinematicForce(timeMillis , XFunction , YFunction));
		
	}
		
	/**
	 * Shorthand for calling {@code then(...)}, passing in the exact same values as the very last call. 
	 */
	public void thenRepeat() {
		
		CSLinked<KinematicForce> queue = forces.tail().val.queue;
		queue.add(queue.get(queue.size() -1).val.copy());
		
	}
	
	/**
	 * Adds a callback to take place on the completion of the most recently added kinematic force.
	 * 
	 * @param callback — a SAM taking no arguments and returning nothing to call on the finishing of this force
	 */
	public void onFinish(Executor callback) {
	
		forces.tail().val.queue.tail().val.onFinish(callback);		
		
	}

	/**
	 * Adds a callback to take place on the completion of the most recently added kinematic force.
	 * 
	 * @param callback — callabe PyObject to execute on the finishing of this force
	 */
	public void onFinish(PyObject callback) {
	
		forces.tail().val.queue.tail().val.onFinish(() -> callback.__call__());		
		
	}
		
	/**
	 * If test returns true, the KinematicForce queue of the most recently created pair is popped.
	 * 
	 * @param test — SAM function taking no input and returning a boolean
	 */
	public void stopIf(Tester test) {
		
		forces.tail().val.queue.tail().val.stopIf(test);
		
	}
	
	/**
	 * If test returns true, the KinematicForce queue of the most recently created pair is popped.
	 * 
	 * @param test — callable PyObject representing a test that if true, removes the current Force from the queue of forces
	 */
	public void stopIf(PyObject test) {
		
		forces.tail().val.queue.tail().val.stopIf(() -> (boolean)test.__call__().__tojava__(Boolean.TYPE));
		
	}
	
	/**
	 * If test returns true, the KinematicForce Quad pair most recently added is removed from the list of impulses.  
	 * 
	 * @param test — SAM taking no arguments and returning a boolean, that when true, removes the entire queue of forces associated with this call.
	 */
	public void killIf(Tester test) {
	
		forces.tail().val.killIf(test);
		
	}

	/**
	 * If test returns true, the KinematicForce Quad pair most recently added is removed from the list of impulses.  
	 * 
	 * @param test — callable PyObject taking no arguments and returning a boolean, that when true, removes the entire queue of forces associated with this 
					 kill condition
	 */
	public void killIf(PyObject test) {
	
		forces.tail().val.killIf(() -> (boolean) test.__call__().__tojava__(Boolean.TYPE));
		
	}
	
	/**
	 * 
	 * TODO: test this <br><br>
	 *
	 * Returns a {@code java.util.function.Supplier} which will return true if the object paired with the most recent force collided with something
	 * along the X Axis of movement.
	 * 
	 * @return {@code java.util.function.Supplier} which will return true if a collision occurred the last time the most recent force was updated.
	 */	
	public Supplier<Boolean> collidedX(){
		
		KinematicForce currentTailForce = forces.tail().val.current().val;
		return () -> currentTailForce.collidedX;
		
	}
	
	/**
	 * TODO: test this <br><br>
	 * 
	 * Returns a {@code java.util.function.Supplier} which will return true if the object paired with the most recent force collided with something
	 * along the Y Axis of movement.
	 * 
	 * @return {@code java.util.function.Supplier} which will return true if a collision occurred the last time the most recent force was updated.
	 */
	public Supplier<Boolean> collidedY(){

		KinematicForce currentTailForce = forces.tail().val.current().val;
		return () -> currentTailForce.collidedY;
	
	}
	
	/**
	 * Creates and returns a KinematicForce which is not managed. The caller must handle it manually.
	 * 
	 * @param type — ForceType enum value for which type of force desired.
	 * @param timeMillis — number of milliseconds this impulse will last
	 * @param initialX — initial step horizontally, this is mainly used for grow and decay force types
	 * @param initialY — initial step vertically, this is mainly used for grow and decay force types
	 * @param stepX — unsigned float horizontal step to increment, for grow and decay types 
	 * @param stepY — unsigned float vertical step to increment, for grow and decay types 
	 * @param target — a quad or instanceof a quad who will be the subject of this  impulse
	 * @return KinematicForce — force generated based on input params.
	 */
	public KinematicForce newImpulse(ForceType type , double timeMillis , float initialX , float initialY , float stepX , float stepY) {
		
		return new KinematicForce(type , timeMillis,  initialX , initialY , stepX , stepY);
		
	}
	
	/**
	 * Takes in a KinematicForce and an object to apply it to, and processes it. This method must be called every frame in order to work as intended. 
	 * <br><br>
	 * This method as well starts {@code force}'s timer if necessary. In general a copy of a model force should be kept around and copied when sent to
	 * this method.
	 * 
	 * @param force — a Kinematic Force 
	 * @param object — a Quad to act on
	 */
	public void process(KinematicForce force , Quads object) {
					
		force.timerStart();
		
		if(force.finished()) return;
		
		values = force.update();
					
		switch (object.type) {

			case PROJECTILE: case ENTITY: 
				
				Entities E = (Entities)object;
				force.collidedX = owner.entities().moveHorizChecked(E , values[0]);
				force.collidedY = owner.entities().moveVertChecked(E , values[1]);
				
				break;
				
			case ITEM:
			
				force.collidedX = owner.entities().moveHorizChecked((Items) object , values[0]);
				force.collidedY = owner.entities().moveVertChecked((Items) object , values[1]);
				
				break;				
			
			case GENERIC: case PARTICLE:
				
				object.translate(values[0] , values[1]);
				
				break;
							
			default:
				
				if(COLDSTEEL.DEBUG_CHECKS) assert false : "Invalid object for Kinematic Force: " + object.type.toString();
				
				break;
		
		}
				
	}

	public void process() {
		
		cdNode<Pair> iter = forces.get(0);
		Pair currentPair;
		KinematicForce currentForce;		
		
		for(int i = 0 ; i < forces.size() ; i ++ , iter = iter.next) {
			
			currentPair = iter.val;						//current pair			
			if(currentPair.shouldKill()) {
				
				iter = forces.safeRemove(iter);
				if(iter == null) break;
				continue;
				
			}
			
			currentForce = currentPair.current().val;	//current force
						
			currentForce.timerStart();
			
			if(currentForce.finished()) {
				
				currentPair.queue.safeRemove(currentPair.current());
				
				if(currentPair.empty()) {
					
					iter = forces.safeRemove(iter);
					if(iter == null) break;
					
				}
				
			}			
			
			values = currentForce.update();
						
			switch (currentPair.obj.type) {

				case PROJECTILE: case ENTITY: 
					
					Entities E = (Entities)currentPair.obj;
					owner.entities().moveHorizChecked(E , values[0]);
					owner.entities().moveVertChecked(E , values[1]);
					
					break;
					
				case ITEM:
				
					owner.entities().moveHorizChecked((Items) currentPair.obj , values[0]);
					owner.entities().moveVertChecked((Items) currentPair.obj , values[1]);
					
					break;				
								
				case GENERIC: case PARTICLE:
					
					currentPair.obj.translate(values[0] , values[1]);
					
					break;
				
				default:
					
					if(COLDSTEEL.DEBUG_CHECKS) assert false : "Invalid object for Kinematic Force: " + currentPair.obj.type.toString();
					
					break;
			
			}

		}
				
	}
		
	public int size() {
		
		return forces.size();
		
	}
	
}
