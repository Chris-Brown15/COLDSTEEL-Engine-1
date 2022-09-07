package Core;

/**
 * Represents a single abstract method called execute which takes no input and returns nothing. This is a safer idea than using Runnable, which is functionally
 * identical. 
 * 
 *
 */
public interface Executor {
	
	public abstract void execute();
	
}
