package Core;

import java.util.function.Consumer;

import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;

import CSUtil.RefInt;
import CSUtil.Timer;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSOHashMap;
import CSUtil.DataStructures.cdNode;

/**
 * This class will execute code it's given after a given time has elapsed. Its API is modeled after {@code Kinematics}, and it shares that class's design
 * idea. Given a SAM interface and a time from now, This class will execute that function upon the elapsing of the given time.
 * 
 * 
 * @author Chris Brown
 *
 */
public abstract class TemporalExecutor {
	
	private static final CSOHashMap<cooldown , Long> cooldowns = new CSOHashMap<cooldown , Long>(15);
	private static final CSLinked<event> events = new CSLinked<event>();
	private static final CSLinked<continuous> continuousEvents = new CSLinked<continuous>();
	private static final CSLinked<delayedEvent> delayedEvents = new CSLinked<>();
	private static final CSLinked<timeBasedEvent> timeBasedEvents = new CSLinked<>();
	//if any event lasts this long, force it out.
	public static final int EVENTS_TIMEOUT = 99999999;
	
	/**
	 * Executes some code at the elapse of either a span of milliseconds or a number of game ticks.
	 * 
	 * @author Chris Brown
	 *
	 */
	private static class event {
		
		private Timer timer = new Timer();
		private final double waitTime;
		private final int waitTicks;
		private int currentTick = 0;
		private final Executor code;
	
		event(double waitTime , Executor code){
			
			this.waitTime = waitTime;
		this.code = code;
			waitTicks = -1;
			timer.start();
			
		}
		
		event(int waitTicks , Executor code){
			
			this.waitTicks = waitTicks;
			this.code = code;
			waitTime = -1;
			
		}
		
		boolean check() {
			
			if(timer.started()) return timer.getElapsedTimeMillis() >= waitTime;
			return ++currentTick >= waitTicks;
			
		}
		
		void exec() {
			
			code.execute();
			
		}
		
	}
	
	/**
	 * Executes some code up to and until the elapse of some milliseconds or a number of game ticks.
	 * 
	 * @author Chris Brown
	 *
	 */
	private static class continuous {
		
		private Timer timer = new Timer();
		private final double continueTime;
		private final int continueTicks;
		private int currentTick = 0;		
		private final Executor code;		
		private final Tester test;
		
		continuous(double continueFor , Executor code){
			
			continueTime = continueFor;
			this.code = code;
			continueTicks = -1;
			timer.start();			
			test = null;
			
		}
	
		continuous(int continueFor , Executor code){
			
			continueTicks = continueFor;			
			continueTime = -1;
			this.code = code;
			test = null;
			
		}
		
		continuous(Tester test , Executor code){
			
			this.code = code;
			this.test = test;
			
			continueTicks = -1;
			continueTime = -1;
						
		}
	
		boolean check() {
			
			if(test != null) return test.test();				
			else if(timer.started()) return continueTime > timer.getElapsedTimeMillis();
			return continueTicks > ++currentTick;
			
		}
		
		void exec() {
			
			code.execute();
			
		}
				
	}
	
	private static class cooldown {
		
		private Timer timer = new Timer();
		private final double cooldownTime;
		private final int cooldownTicks;
		private int currentTick = 0;
		final long uniqueID;
		private final Tester tester;
		
		cooldown(double timeMillis , long uniqueID){
			
			this.cooldownTime = timeMillis;			
			this.uniqueID = uniqueID;			
			cooldownTicks = -1;
			timer.start();
			tester = null;
			
		}
		
		cooldown(int ticks , long uniqueID){
			
			cooldownTicks = ticks;
			this.uniqueID = uniqueID;
			this.cooldownTime = -1;
			tester = null;
			
		}
		
		cooldown(Tester tester , long uniqueID){
			
			this.tester = tester;
			this.uniqueID = uniqueID;
			cooldownTime = -1;
			cooldownTicks = -1;
			
		}
		
		boolean check() {
			
			if(tester != null) return !tester.test();
			if(timer.started()) return timer.getElapsedTimeMillis() >= cooldownTime;
			return ++currentTick == cooldownTicks;
			
		}
		
	}
	
	/**
	 * Wraper for a function that will be called once a predicate returns true, and once the function is called, the {@code delayedEvent}
	 *  is removed.
	 */
	private static class delayedEvent {
		
		final Tester test;
		final Executor code;

		private int timeoutCounter = 0;
		
		delayedEvent(final Tester test , final Executor code) {
			
			this.test = test;
			this.code = code;
			
		}
		
		boolean ready() {
			
			timeoutCounter ++;
			return test.test() || timeoutCounter > EVENTS_TIMEOUT;
			
		}
		
		void exec() {
			
			code.execute();
			
		}
		
	}
	
	private static class timeBasedEvent{
		
		private final double numberMillis;
		private final Consumer<Double> timeEvent;
		private final Timer timer = new Timer();
		
		timeBasedEvent(double numberMillis , Consumer<Double> timeEvent) {
			
			this.numberMillis = numberMillis;
			this.timeEvent = timeEvent;
			timer.start();
			
		}
		
		boolean process() {
		
			double elapsedMillis;
			if((elapsedMillis = timer.getElapsedTimeMillis()) >= numberMillis) return true;
			else {
				
				timeEvent.accept(elapsedMillis);
				return false;
				
			}
			
		}		
		
	}
	
	/**
	 * Given a function taking in a double as input, this method adds a time based event to the TemporalExecutor queue. From the invocation of this
	 * method, a timer is started whose elapsed milliseconds are passed into {@code code}, and upon the elapsing of {@code numberMillis}, this 
	 * time based event is removed from the queue.
	 * 
	 * @param numberMillis — number of milliseconds this event will exist for
	 * @param code — {@code java.util.Consumer} of {@code Double} which will be executed for {@code numberMillis} milliseconds, taking the elapsed time
	 * 				 as its input
	 */
	public static final synchronized void withElapseOf(double numberMillis , Consumer<Double> code) {
		
		timeBasedEvents.add(new timeBasedEvent(numberMillis , code));
		
	}
	
	/**
	 * Given a python function taking in a double as input, this method adds a time based event to the TemporalExecutor queue. From the invocation of this
	 * method, a timer is started whose elapsed milliseconds are passed into {@code code}, and upon the elapsing of {@code numberMillis}, this 
	 * time based event is removed from the queue.
	 * 
	 * @param numberMillis — number of milliseconds this event will exist for
	 * @param code — {@code PyObject} taking {@code Double} as input which will be executed for {@code numberMillis} milliseconds
	 */
	public static final synchronized void withElapseOf(double numberMillis , PyObject code) {
		
		timeBasedEvents.add(new timeBasedEvent(numberMillis , x -> code.__call__(new ClassicPyObjectAdapter().adapt(x))));
		
	}
	
	/**
	 * Adds a {@code Executor} whose SAM will be executed upon the elapsing of {@code millis}. An executor is an interface whose SAM takes no parameters
	 * and returns nothing. It is essentially a Runnable, but it could not be passed to a {@code Thread}
	 * 
	 * @param millis — number of milliseconds to wait until executing the given code
	 * @param code — code to execute upon the elapsing of this timer.
	 */
	public static final synchronized void onElapseOf(double millis , Executor code) {
		
		events.add(new event(millis , code));
	}

	/**
	 * Adds a PyObject who will be called upon the elapsing of {@code millis}. Intended for use in scripts
	 * 
	 * @param millis — number of milliseconds to wait until executing the given code
	 * @param code — code to execute upon the elapsing of this timer.
	 */
	public static final synchronized void onElapseOf(double millis , PyObject code) {
		
		events.add(new event(millis , () -> code.__call__()));
	}
	
	/**
	 * At the completion of {@code ticks}, {@code code} is executed, taking no parameters.
	 * 
	 * @param ticks — number of ticks to wait until executing this code
	 * @param code — code to execute on elapse of specified ticks
	 */
	public static final synchronized void onTicks(int ticks , Executor code) {
		
		events.add(new event(ticks , code));
		
	}
	
	/**
	 * At the completion of {@code ticks}, {@code code} is executed.
	 * 
	 * @param ticks — number of ticks to wait until executing this code
	 * @param code — code to execute on elapse of specified ticks
	 */
	public static final synchronized void onTicks(int ticks , PyObject code) {
		
		events.add(new event(ticks ,() ->  code.__call__()));
		
	}
	
	/**
	 * Executes {@code code} for {@code millis} milliseconds
	 * 
	 * @param millis — number of milliseconds to execute code
	 * @param code — SAM taking no parameters
	 */
	public static final synchronized void forMillis(double millis , Executor code) {
		
		continuousEvents.add(new continuous(millis , code));
		
	}

	/**
	 * Executes {@code code} for {@code millis} milliseconds
	 * 
	 * @param millis — number of milliseconds to execute code
	 * @param code — PyObject taking no parameters
	 */
	public static final synchronized void forMillis(double millis , PyObject code) {
		
		continuousEvents.add(new continuous(millis , () -> code.__call__()));
		
	}
	
	/**
	 * Executes {@code code} for {@code ticks} ticks.
	 * 
	 * @param ticks — number of ticks to execute code
	 * @param code  — SAM taking no input to execute
	 */
	public static final synchronized void forTicks(int ticks , Executor code) {
		
		continuousEvents.add(new continuous(ticks , code));
		
	}

	/**
	 * Executes {@code code} for {@code ticks} ticks.
	 * 
	 * @param ticks — number of ticks to execute code
	 * @param code  — callable PyObject taking no input to execute
	 */
	public static final synchronized void forTicks(int ticks , PyObject code) {
		
		continuousEvents.add(new continuous(ticks , () -> code.__call__()));
		
	}
		
	/**
	 * Executes {@code code} while {@code test} returns true.
	 * 
	 * @param test — Tester SAM returning a boolean and taking no input
	 * @param code —  Executor SAM to execute once
	 */
	public static final synchronized void whileTrue(Tester test , Executor code) {
		
		continuousEvents.add(new continuous(test , code));
		
	}
	
	/**
	 * Executes {@code code} while {@code test} returns true.
	 * 
	 * @param test — Tester SAM returning a boolean and taking no input
	 * @param code — PyObject representing a callable function taking no input to be called once
	 */
	public static final synchronized void whileTrue(PyObject test , PyObject code) {
		
		continuousEvents.add(new continuous(() -> (boolean)test.__call__().__tojava__(Boolean.TYPE) , () -> code.__call__()));
		
	}
	
	/**
	 * Adds a cooldown if no previous cooldown exists. The cooldown will last so long as tester returns true.
	 * 
	 * @param test — a Tester SAM, which takes no arguments and returns a boolean
	 * @param unique — a unique long identifying this cooldown, a UUL
	 */
	public static final synchronized void coolDown(Tester test , long unique) {

		RefInt has = new RefInt(0);
		cooldowns.bucket(unique).forEachVal(cooldown -> {

			if(cooldown.uniqueID == unique) {
				
				has.set(1);
				return;
			
			}			
		
		});
		
		if(has.get() == 0) cooldowns.add(new cooldown(test , unique) , unique);
		
	}

	/**
	 * Adds a cooldown if no previous cooldown exists, the cooldown will last so long as test, a callable PyObject, returns true.
	 * 
	 * @param test — a callable PyObject which takes no arguments and returns a boolean
	 * @param unique — a unique long identifying this cooldown, a UUL
	 */ 
	public static final synchronized void coolDown(PyObject test , long unique) {

		RefInt has = new RefInt(0);
		cooldowns.bucket(unique).forEachVal(cooldown -> {

			if(cooldown.uniqueID == unique) {
				
				has.set(1);
				return;
			
			}			
		
		});
		
		if(has.get() == 0) cooldowns.add(new cooldown(() -> (boolean)test.__call__().__tojava__(Boolean.TYPE) , unique) , unique);
		
	}
	
	/**
	 * Adds a cooldown if no extant cooldown has {@code unique} already. It will persist for {@code milliseconds} milliseconds.
	 * 
	 * @param milliseconds — milliseconds for this cooldown
	 * @param unique — a unique long identifying this cooldown, a UUL
	 */
	public static final synchronized void coolDown(double milliseconds , long unique) {
		
		RefInt has = new RefInt(0);
		cooldowns.bucket(unique).forEachVal(cooldown -> {

			if(cooldown.uniqueID == unique) {
				
				has.set(1);
				return;
			
			}			
		
		});
		
		if(has.get() == 0) cooldowns.add(new cooldown(milliseconds , unique) , unique);
		
	}

	/**
	 * Adds a cooldown if no extant cooldown has {@code unique} already. It will persist for {@code ticks} ticks.
	 * 
	 * @param ticks — number of ticks this cooldown will last for
	 * @param unique — a unique long identifying this cooldown, a UUL
	 */
	public static final synchronized void coolDown(int ticks, long unique) {

		RefInt has = new RefInt(0);
		cooldowns.bucket(unique).forEachVal(cooldown -> {

			if(cooldown.uniqueID == unique) {
				
				has.set(1);
				return;
			
			}			
		
		});
		
		if(has.get() == 0) cooldowns.add(new cooldown(ticks , unique) , unique);
		
	}
	
	/**
	 * Returns whether an extant whose {@code uniqueID} matches {@code unique}. If true, a cooldown is present.
	 * 
	 * @param unique — a UUL identifying a cooldown
	 * @return true if a cooldown exists whose {@code uniqueID} matches {@code unique}, else false.
	 */
	public static final synchronized boolean coolingDown(long unique) {

		RefInt has = new RefInt(0);
		cooldowns.bucket(unique).forEachVal(cooldown -> {

			if(cooldown.uniqueID == unique) {
				
				has.set(1);
				return;
			
			}			
		
		});
		
		return has.get() == 1;
		
	}
	
	/**
	 * Adds a new delayed event to the respective list. This delayed event wil not execute until {@code test} returns true,
	 * and once it does, this delayed event is removed from the list.
	 * 
	 * @param test — a {@code java.util.function.Predicate} who, on returning true, will trigger the delayed event
	 * @param testInput — some input to {@code test}
	 * @param code — a {@code java.util.function.Consumer} that will be called exactly once when {@code test} returns true
	 * @param codeInput — some input to {@code code}
	 */
	public static final synchronized void onTrue(Tester test , Executor code) {
		
		delayedEvents.add(new delayedEvent(test , code));
		
	}

	/**
	 * Adds a new delayed event to the respective list. This delayed event wil not execute until {@code test} returns true,
	 * and once it does, this delayed event is removed from the list.
	 * 
	 * @param test — a PyObject who, on returning true, will trigger the delayed event
	 * @param code — a {@code Executor} that will be called exactly once when {@code test} returns true
	 */
	public static final synchronized void onTrue(PyObject test , PyObject code) {
		
		delayedEvents.add(new delayedEvent(() -> (boolean)test.__call__().__tojava__(Boolean.class) , () -> code.__call__()));
		
	}
	
	/**
	 * 
	 * Handles all events created with this API.
	 * 
	 */
	public static final synchronized void process() {
		
		handleElapsed(0 , events.get(0));
		handleContinuous(0 , continuousEvents.get(0));
		handleCooldowns();
		handleDelayedEvents(0 , delayedEvents.get(0));
		handleTimeBasedEvents(0 , timeBasedEvents.get(0));
		
	}
	
	private static final void handleElapsed(int counter , cdNode<event> iter) {
		
		if(!(counter >= events.size())) {
						
			if(iter.val.check()) {
				
				iter.val.exec();				
				iter = events.safeRemove(iter);
				
			} else iter = iter.next;
			
			handleElapsed(++counter , iter);
			
		}
		
	}
	
	private static final void handleContinuous(int counter , cdNode<continuous> iter) {
		
		if(!(counter >= continuousEvents.size())) {
						
			if(iter.val.check()) {
				
				iter.val.exec();
				handleContinuous(++counter , iter.next);
				
			} else {

				iter = continuousEvents.safeRemove(iter);
				handleContinuous(++counter , iter);
				
			}
			
		} 
		
	}
	
	private static final void handleCooldowns() {
	
		cooldowns.forEach((index , cooldownList) -> {
			
			cdNode<cooldown> iter = cooldownList.get(0);
			for(int i = 0 ; i < cooldownList.size() ; i ++) {
				
				if(iter.val.check()) iter = cooldownList.safeRemove(iter);
				else iter = iter.next;
				
			}
			
		});
		
	}
	
	private static final void handleDelayedEvents(int counter , cdNode<delayedEvent> iter) {
		
		if(!(counter >= delayedEvents.size())) {
			
			if(iter.val.ready()) {
				
				iter.val.exec();
				iter = delayedEvents.safeRemove(iter);
			
			} else iter = iter.next;
			
			handleDelayedEvents(++counter , iter);
			
		}
		
	}
	
	private static final void handleTimeBasedEvents(int counter , cdNode<timeBasedEvent> iter) {
		
		if(!(counter >= timeBasedEvents.size())) {
			
			if(iter.val.process()) iter = timeBasedEvents.safeRemove(iter);
			else iter = iter.next;
			handleTimeBasedEvents(++counter , iter);
			
		}
		
	}
	
	public static final int eventsSize() {
		
		return events.size();
		
	}
	
	public static final int continuousEventsSize() {
		
		return continuousEvents.size();
		
	}
	
	public static final int cooldownsSize() {
		
		return cooldowns.size();
				
	}

	public static final int delayedEventSize() {
		
		return delayedEvents.size();
		
	}
	
	public static int totalSize() {

		return cooldownsSize() + continuousEventsSize() + eventsSize() + delayedEvents.size();
		
	}
	
}
