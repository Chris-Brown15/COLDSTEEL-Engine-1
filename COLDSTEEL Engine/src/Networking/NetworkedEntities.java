package Networking;

import java.util.BitSet;

import CS.Controls;
import CS.Controls.Control;
import Core.ECS;
import Core.Entities.Entities;
import Networking.Utils.NetworkingConstants;

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
	// these arrays represent which keys are synced between client and server
	private volatile byte[] syncedKeyboard , syncedMouse , syncedGamepad;
	/*
	 * these arrays represent the states of synced keys as last received from the outside world
	 * 
	 * If the owning instance of NetworkedEntities belongs to the server, these represent the last
	 * state of the keys from the client whom this networked entities represents.
	 * 
	 * If the owning instance NetworkedEntities belongs to a client and is NOT the instance of
	 * NetworkedEntities which represents that player's entity, these will represent the last 
	 * notified state of keys received from the server about some other client's input
	 * 
	 * Otherwise the owning instance of NetworkedEntities belongs to a client and this instance
	 * models their player, in which case these are unused.
	 * 
	 */
	
	private volatile byte[] keyboardState , mouseState , gamepadState;
	
	private volatile BitSet keyboardStrikeStates , mouseStrikeStates , gamepadStrikeStates;
		
	public NetworkedEntities(short connectionIndex , Entities networked , boolean clientOwned) {
		
		this.networked = networked;
		this.connectionIndex = connectionIndex;
		System.out.println("NetworkedEntity created with entity: " + networked.name());
		//remove components that clash with the host's entity's components
//		if(!clientOwned) networked.removeComponents(ECS.HORIZONTAL_PLAYER_CONTROLLER , ECS.VERTICAL_PLAYER_CONTROLLER , ECS.CAMERA_TRACK);		
		
	}
	
	public short connectionIndex() {
		
		return connectionIndex;
		
	}

	public void connectionIndex(short connectionID) {
		
		connectionIndex = connectionID;
		
	}
		
	public void syncPeripheralsByControls(Control... controls) { 
		
		System.out.println("Syncing controls for entity: " + networked.name() + " in networked entity");
		 		
		int keyboard = 0 , mouse = 0 , gamepad = 0;
		
		//find out sizes of arrays first
		for(Control x : controls) {
			
			if(x.peripheral() == Controls.KEYBOARD) keyboard++;
			else if(x.peripheral() == Controls.MOUSE) mouse++;
			else gamepad++;
			
		}
		
		syncedKeyboard = new byte[keyboard];
		syncedMouse = new byte[mouse];
		syncedGamepad = new byte[gamepad];
		
		for(int i = 0 , nextKB = 0 , nextMB = 0 , nextGP = 0 ; i < controls.length ; i ++) {

			if(controls[i].peripheral() == Controls.KEYBOARD) syncedKeyboard[nextKB++] = controls[i].key();
			else if(controls[i].peripheral() == Controls.MOUSE) syncedMouse[nextMB++] = controls[i].key();
			else syncedGamepad[nextGP++] = controls[i].key();
		 
		}
			
		keyboardState = new byte[keyboard];
		mouseState = new byte[mouse];
		gamepadState = new byte[gamepad];
		
		System.arraycopy(syncedKeyboard, 0, keyboardState, 0, keyboard);
		System.arraycopy(mouseState, 0, mouseState, 0, mouse);
		System.arraycopy(gamepadState, 0, gamepadState, 0, gamepad);
		
		keyboardStrikeStates = new BitSet(keyboard);
		mouseStrikeStates = new BitSet(mouse);
		gamepadStrikeStates = new BitSet(gamepad);
				
	}
	
	public void syncKeyboard(byte[] syncedCSKeys) {
		
		syncedKeyboard = syncedCSKeys;
		keyboardState = new byte[syncedKeyboard.length];
		System.arraycopy(syncedKeyboard, 0, keyboardState, 0, syncedKeyboard.length);
		keyboardStrikeStates = new BitSet(syncedKeyboard.length);
		
	}
	
	public void syncMouse(byte[] syncedCSMouseButtons) {
		
		syncedMouse = syncedCSMouseButtons;
		mouseState = new byte[mouseState.length];
		System.arraycopy(mouseState, 0, mouseState, 0, mouseState.length);
		mouseStrikeStates = new BitSet(mouseState.length);
		
	}
	
	public void syncGamepad(byte[] syncedCSGamepadButtons) {
		
		syncedGamepad = syncedCSGamepadButtons;
		gamepadState = new byte[syncedGamepad.length];
		System.arraycopy(gamepadState, 0, gamepadState, 0, syncedGamepad.length);
		gamepadStrikeStates = new BitSet(syncedGamepad.length);
		
	}
	
	public byte[] syncedKeyboard() {
		
		return syncedKeyboard;
		
	}

	public byte[] syncedMouse() {
		
		return syncedMouse;
		
	}

	public byte[] syncedGamepad() {
		
		return syncedGamepad;
		
	}

	public void keyboardState(byte[] keyboardState) {
		
		this.keyboardState = keyboardState;
		
	}
	
	public void mouseState(byte[] mouseState) {
		
		this.mouseState = mouseState;
		
	}
	
	public void gamepadState(byte[] gamepadState) {
		
		this.gamepadState = gamepadState;
		
	}
	
	public byte[] keyboardState() {
		
		return keyboardState;
		
	}
	
	public byte[] mouseState() {
		
		return mouseState;
		
	}
	
	public byte[] gamepadState() {
		
		return gamepadState;
		
	}
	
	public void releaseKeys() {
		
		for(int i = 0 ; i < keyboardState.length ; i ++) {
			
			keyboardState[i] &= ~NetworkingConstants.KEY_PRESSED_MASK;
			keyboardStrikeStates.clear(i);
			
		}
		
		for(int i = 0 ; i < mouseState.length ; i ++) {
			
			mouseState[i] &= ~NetworkingConstants.KEY_PRESSED_MASK;
			mouseStrikeStates.clear(i);
			
		}
		
		for(int i = 0 ; i < gamepadState.length ; i ++) {
			
			gamepadState[i] &= ~NetworkingConstants.KEY_PRESSED_MASK;
			gamepadStrikeStates.clear(i);
			
		}
		
	}
	
	public BitSet keyboardStrikeState() {
		
		return keyboardStrikeStates;
		
	}

	public BitSet mouseStrikeState() {
		
		return mouseStrikeStates;
		
	}

	public BitSet gamepadStrikeState() {
		
		return gamepadStrikeStates;
		
	}
	
	public Entities networked() {
		
		return networked;
		
	}
		
}