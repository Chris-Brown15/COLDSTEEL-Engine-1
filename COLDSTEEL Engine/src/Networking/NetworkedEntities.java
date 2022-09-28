package Networking;

import Core.ECS;
import Core.Entities.Entities;

/**
 * This class represents a way to model another player's entity. 
 * There will be an instance of this class for each instance of {@code UserHostedSessionClient}.
 * 
 * This class needs to intercept requests from {@code playerEntity}'s script to the GLFWWindow about the state of peripherals and replace
 * the returned states with states received by the server.
 * 
 * <br> <br> <b>IMPORTANT:</b> constructors of this class should only be called in the main thread, because they will likely call OpenGL functions. 
 * 
 * @author Chris Brown
 *
 */
public class NetworkedEntities extends Entities {
	
	private String location;
	
	public NetworkedEntities(String entityNamePath , String macroLevelNameAndLevelName) {
		
		super(entityNamePath);
		location = macroLevelNameAndLevelName;
		System.out.println("NetworkedEntity created with entity: " + name);
		//remove components that clash with the host's entity's components
		removeComponents(ECS.HORIZONTAL_PLAYER_CONTROLLER , ECS.VERTICAL_PLAYER_CONTROLLER , ECS.CAMERA_TRACK);		
		
	}

	String location() {
		
		return location;
		
	}
	
}