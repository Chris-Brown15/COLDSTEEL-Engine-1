package Networking;

/**
 *
 * This interface is used by objects that need to be deep copied for networking purposes. For player-controlled entities, some variables can be synced.
 * What this means is that the state of those variables is saved each frame and if the player goes out of sync, we have a previous state to refer
 * them to, which means resetting their synced state to the most previously captured state. If players are in sync, we will capture their state.
 * Then we can reset them back to the most previous state once we hear from them again.
 * 
 * @author Chris Brown
 *
 * @param <T> — The type of the object to be saved, should be the type of the implementor
 */
public interface DeepCopy <T> {
	
	/**
	 * Should return a deep copy of the implementor's state at invocation,
	 * 
	 * @return a deep copied version of {@code this}.
	 */
	public T copy();
	
}