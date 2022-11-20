package Networking;

import static CSUtil.CSLogger.*;

import static Networking.Utils.NetworkingConstants.*;

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
public class NetworkedEntities {

	private final Entities networked;
	private short connectionIndex;
	byte updateSequence = 0;
	public boolean inSync = true;

	/**
	 * The values within this array represent IDs of controls.
	 */
	private volatile byte[] syncedControls = {};

	public NetworkedEntities(short connectionIndex , Entities networked , boolean clientOwned) {
		
		this.networked = networked;
		this.connectionIndex = connectionIndex;
		System.out.println("NetworkedEntity created with entity: " + networked.name());
		if(!clientOwned) networked.removeComponents(ECS.CAMERA_TRACK);
		
	}
	
	public void setNetworkControlled() {
		
		networked.components()[Entities.HCOFF + 2] = true;
		networked.components()[Entities.VCOFF + 6] = true;
		
	}
	
	public short connectionIndex() {
		
		return connectionIndex;
		
	}

	public void connectionIndex(short connectionID) {
		
		connectionIndex = connectionID;
		
	}
	
	public Entities networked() {
		
		return networked;
		
	}

	public void syncPeripheralsByControls(byte... controls) {
		
		this.syncedControls = controls;
		
	}
	
	public void controlsState(byte[] controlsStates) {
		
		this.syncedControls = controlsStates;
		
		for(int i = 0 ; i < controlsStates.length ; i ++) {

			if(LOGGING_ENABLED) { 
				
				if((syncedControls[i] & CONTROL_PRESSED_MASK) != 0 && (controlsStates[i] & CONTROL_PRESSED_MASK) == 0) {
					
					log("key un pressed");
					
				}
				
			}					
			
			/*
			 * If the incoming byte says it was pressed (its eighth bit is set) but {@code this}'s view of the key
			 * is that it is not pressed (the eighth bit is unset), this will set the seventh and eighth bit. This  
			 */
			if((syncedControls[i] & CONTROL_PRESSED_MASK) == 0 && (controlsStates[i] & CONTROL_PRESSED_MASK) != 0) { 
			
				syncedControls[i] |= CONTROL_PRESSED_MASK|CONTROL_STRUCK_MASK;
				
			} 
				
		}		
		
	}
	
	public byte[] controlStates() {
	
		return syncedControls;
		
	}		
	
	public boolean pressed(byte controlID) {
		
		for(int i = 0 ; i < syncedControls.length ; i ++) if((syncedControls[i] & KEYCODE_MASK) == controlID) { 
			
			return (syncedControls[i] & CONTROL_PRESSED_MASK) != 0;
			
		}
		
		return false;
		
	}
	
	public boolean struck(byte controlID) {
		
		for(int i = 0 ; i < syncedControls.length ; i ++) if((syncedControls[i] & KEYCODE_MASK) == controlID) { 
			
			return (syncedControls[i] & CONTROL_STRUCK_MASK) != 0;
			
		}
		
		return false;
	}
	
	public void unStrikeKeys() {
		
		for(int i = 0 ; i < syncedControls.length ; i ++) syncedControls[i] &= ~CONTROL_STRUCK_MASK;
		
	}
	
	public int updateSequence() {
		
		return updateSequence;
		
	}
	
	public int advanceUpdateSequence() {
		
		return updateSequence++;
		
	}
	
}