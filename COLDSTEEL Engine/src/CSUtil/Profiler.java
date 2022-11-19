package CSUtil;

import Core.Executor;

/**
 * Basic profiling utility. This static class can wrap functions and execute them while providing time to complete information and 
 * repeted executions.
 * 
 * @author Chris Brown
 *
 */
public class Profiler {

	private final Timer timer = new Timer();
	
	private double millis;
	private double nanos;
	private double seconds;
	private int iters = 0;
	
	public final void call(Executor pure) {
		
		timer.start();
		pure.execute();
		millis = timer.getElapsedTimeMillis();
		nanos = timer.getElapsedTimeNanos();
		seconds = timer.getElapsedTimeSecs();
		
	}
	
	public final void timerStart() {
		
		timer.start();
		
	}
	
	public final void itersPerSecond(Executor code) {
		
		if(timer.getElapsedTimeSecs() >= 1) {
			
			timer.start();
			System.out.println(iters);
			iters = 0; 
			
		}

		code.execute();
		iters++;
		
	}
	
	public void printMillis() {
		
		System.out.println("Millis: " + millis);
		
	}

	public void printNanos() {
		
		System.out.println("Nanos: " + nanos);
		
	}

	public void printSeconds() {
		
		System.out.println("Seconds: " + seconds);
		
	}
	
	public double millis() {
		
		return millis;		
		
	}

	public double nanos() {
		
		return nanos;		
		
	}

	public double seconds() {
		
		return seconds;		
		
	}
	
}
