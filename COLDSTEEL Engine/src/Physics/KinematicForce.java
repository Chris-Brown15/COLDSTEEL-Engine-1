package Physics;

import CSUtil.Timer;
import Core.Direction;
import Core.Executor;
import Core.Tester;

/**
 * One method of moving objects. Kinematic is an API to move game objects with respect to time. 
 * A static function from the Kinematics class creates a KinematicForce, which is responsible for keeping track of the data for the impulse.
 * In general, outside classes shouldn't need to interface with this one.
 * 
 *  There are four {@code ForceTypes} to choose from. 
 *  
 *  <br>
 *  <ul>
 *  <li> {@code LINEAR} Forces will act on an object by the given initial amounts, stopping at the elapsing of its time. </li>
 *  <li> {@code LINEAR_DECAY} Forces will act on an object by an initial amount which is modified by a step per tick, eventually going to 0. </li>
 *  <li> {@code LINEAR_GROW} Forces will act on an object by an initial amount which is modified by a step per tick, eventually going to infinity. </li>
 *  <li> {@code EXPRESSION} Forces will act on an object by getting an amount from a given {@code MExpression}. If the MExpresion takes an input, 
 *  						it will be the elapsed time. </li>
 *  </ul>
 *   
 * @author Chris Brown
 *
 */
public class KinematicForce {

	//if null is passed as an MExpression, this expession is substituted for it. It will always return 0
	private static final MExpression nullExpression = new MExpression("x - x");
	
	//length of remaining milliseconds to run this KF
	private double timeMillis;	
	//number of pixels to move each time
	private float stepX;	
	private float stepY;	
	//type of impulse,  currently one of LINEAR or EXPONENTIAL, maybe add logarithmic in the future
	final ForceType type;
	
	private final Timer timer = new Timer();
	
	private float initialStepX;
	private float initialStepY;
	private boolean finished = false;
	
	private float [] values = new float[2];
	
	private Executor onFinish = null;
	private Tester stopTest = null;	
	
	boolean collidedX = false;
	boolean collidedY = false;

	private MExpression xFunction;
	private MExpression yFunction;
	
	void onFinish(Executor callback) {
				
		this.onFinish = callback;
		
	}
	
	void stopIf(Tester callback) {
		
		this.stopTest = callback;
		
	}
	
	KinematicForce(ForceType type , double timeMillis , float initialX , float initialY , float unsignedStepX , float unsignedStepY){
		
		this.type = type;
		this.timeMillis = timeMillis; 
		this.initialStepX = initialX;
		this.initialStepY = initialY;
				
		this.stepX = unsignedStepX;
		this.stepY = unsignedStepY;
		
		if(type == ForceType.LINEAR_DECAY) {
			
			assert stepX >= 0 && stepY >= 0 : "ERROR: invalid impulse step, must be unsigned.";
			
			//we want the speed of this force to converge on 0
			if(initialStepX < 0) this.stepX *= -1;	
			if(initialStepY < 0) this.stepY *= -1;
			
		} else if (type == ForceType.LINEAR_GROW) {
			
			assert stepX >= 0 && stepY >= 0 : "ERROR: invalid impulse step, must be unsigned.";
			
			Direction horizontalDirection = initialStepX > 0 ? Direction.RIGHT : Direction.LEFT;
			Direction verticalDirection = initialStepY > 0 ? Direction.UP:Direction.DOWN;
			
			if(horizontalDirection == Direction.LEFT) stepX *= -1;
			if(verticalDirection == Direction.DOWN) stepY *= -1;
			
		}
		
		values[0] = initialX;
		values[1] = initialY;
		
	}
	
	KinematicForce(double timeMillis , MExpression XFunction , MExpression YFunction){
		
		this.type = ForceType.EXPRESSION;
		this.timeMillis = timeMillis;
		xFunction = XFunction != null ? XFunction : nullExpression;
		yFunction = YFunction != null ? YFunction : nullExpression;
		
	}
	
	void timerStart() {
		
		if(!timer.started()) timer.start();		
		
	}
			
	/**
	 * 
	 * Gives the next step given the current time interval.
	 * With respect to growth and decay types changes in the step are based on the elapsed time since this object was constructed.
	 * 
	 * 
	 *  @return the appropriate step at the calling moment in time for this type of impulse. What is returned ar the values to transform an object by.
	 */
	public float[] update() {
						
		switch(type) {
			
			case LINEAR_DECAY:	
								
				values[0] -= stepX;
				values[1] -= stepY;
				
				if(((initialStepX > 0 && values[0] <= 0) || (initialStepX < 0 && values[0] >= 0)) || 
				   ((initialStepY > 0 && values[1] <= 0) || (initialStepY < 0 && values[1] >= 0))) finished = true;				
				
				break;
				
			case LINEAR_GROW: 
				
				values[0] = initialStepX += stepX;
				values[1] = initialStepY += stepY;
				
				break;		
				
			case LINEAR: /* nothing happens here because in this case, the values array is already set */ break;
							
			case EXPRESSION:
				
				if(xFunction.floatVariables() > 0) values[0] = xFunction.at((float) timer.getElapsedTimeMillis());
				else values[0] = xFunction.atFloats();
				if(yFunction.floatVariables() > 0) values[1] = yFunction.at((float) timer.getElapsedTimeMillis());
				else values[1] = yFunction.atFloats();
				
				break;
			
			
		}	
		
		if(values[0] > Kinematics.maxSpeed) values[0] = Kinematics.maxSpeed;
		else if (values[0] < Kinematics.minSpeed) values[0] = Kinematics.minSpeed;
		
		if(values[1] > Kinematics.maxSpeed) values[1] = Kinematics.maxSpeed;
		else if (values[1] < Kinematics.minSpeed) values[1] = Kinematics.minSpeed;

		return values;
		
	}
	
	/**
	 * Forces this force to stop.
	 */
	void finish() {
		
		finished = true;
		
	}
	
	/**
	 * Checks whether a KinematicForce is finished and should no longer be processed. A force is finished if its timer has elapsed, it cannot move it's
	 * object any more, or if it's stop test returned true, assuming it exists.
	 * <br><br>
	 * Furthermore, if this force has an {@code onFinish} callback, that will be invoked.
	 * 
	 * @return — true if this KinematicForce is finished and should no longer be processed.
	 */
	public boolean finished() {
		
		boolean fin = timer.getElapsedTimeMillis() >= timeMillis || finished;
		boolean stop = stopTest != null ? stopTest.test() : false;
		if((fin || stop) && onFinish != null) onFinish.execute();
		return fin || stop;
		
	}
	
	public double elapsed() {
		
		return timer.getElapsedTimeMillis();
		
	}
	
	public KinematicForce copy() {
		
		KinematicForce newForce = new KinematicForce(this.type , this.timeMillis , this.initialStepX , this.initialStepY , this.stepX , this.stepY);
		newForce.onFinish(this.onFinish);
		newForce.timer.reset();
		newForce.xFunction = this.xFunction;
		newForce.yFunction = this.yFunction;
		return newForce;
		
	}
	
}