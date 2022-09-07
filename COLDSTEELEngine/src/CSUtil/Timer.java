package CSUtil;
//This class is based on a stack overflow thread. I would like to give credit to whoever posted it, but I cannot find them
//If found, I'd like to give credit
public class Timer {

	private double startTime;
	private double nanoStartTime;
	private boolean started = false;
	
	public boolean started() {
		
		return started;
		
	}
	
	public void reset() {
		
		started = false;
		startTime = 0;
				
	}
	
	public void start(){
		
		started = true;
		startTime = System.currentTimeMillis();
		nanoStartTime = System.nanoTime();

	}
	
	/**
	 * Gets the difference in nanoseconds between the last call to start and the call to this method.
	 *
	 * @return — System.nanoTime() - nano time at last call to start()
	 */
	public double getElapsedTimeNanos(){

		return System.nanoTime() - nanoStartTime;

	}

	/**
	 *
	 * Gets the proportion given by difference between System.nanoTime() and nano time set at last call to start(), divided by numberNanos
	 *
	 * @param numberNanos — number of nano seconds to count from
	 * @return proportion reflecting whether the numberNanos time has passed since the last call to start(). If this is 1, numberNanos nanoseconds
	 * has passed
	 */
	public double getElapsedTimeNanos(double numberNanos){

		return (System.nanoTime() - nanoStartTime) / numberNanos;

	}


	/**
	 * Gets difference in milliseconds between the call to this method and the call to start() in milliseconds
	 * @return Difference in System.currentTimeMillies() and millisecondTime at set at last call to start()
	 */
	public double getElapsedTimeMillis(){

		return (System.currentTimeMillis() - startTime);

	}

	/**
	 * Gets the proportion given by the difference between current milli time and milli time at start() call, divided by paramater millis
	 *
	 * @param millis — number of millis to measure against the elapsed time
	 * @return System.currentTimeMillis() - time set at last call to start() / millis
	 */
	public double getElapsedTimeMilliSecs(double millis){

		return (System.currentTimeMillis() - startTime) / millis;

	}

	/**
	 *
	 * Gets the amount of seconds that has passed since last call to start()
	 *
	 * @return — number of seconds as decimal since call to start()
	 */
	public double getElapsedTimeSecs(){

		return (System.currentTimeMillis() - startTime) / 1000d;

	}

	/**
	 *
	 *Gets proportion of amount of seconds that has passed based on seconds
	 *
	 * @param seconds — number of seconds to measure against.
	 * @return decimal representation of proportion of seconds passed since start() to seconds parameter
	 */
	public double getElapsedTimeSecs(double seconds){

		return (System.currentTimeMillis() - startTime) / (seconds * 1000d);

	}

	public double getStartTimeSecs() {
		
		return startTime * 1000;
		
	}

}
