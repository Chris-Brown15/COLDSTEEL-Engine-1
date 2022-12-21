package Networking;

import static Networking.Utils.NetworkingConstants.*;

import java.util.function.Consumer;

import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;

import CS.Engine;
import Core.Entities.EntityScripts;

import CSUtil.Copy;
import CSUtil.DataStructures.CSCHashMap;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.LinkedRingBuffer;
import CSUtil.DataStructures.Tuple2;
import Core.Entities.ECS;
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

	private static final byte PREVIOUS_FRAMES_BUFFER_SIZE = 50;
	
	private volatile Entities networked;
	private short connectionIndex;
	public boolean isReady = false;
	
	//list of script variables that need to be saved when in sync
	private CSLinked<Tuple2<String , Copy>> syncedVariables = new CSLinked<>();
	//list of ECS component datas that need to be saved when in sync
	private CSLinked<ECS> syncedComponents = new CSLinked<ECS>();
	//list of deep-copied python variables that will be used if this goes out of sync
	private CSLinked<Tuple2<String , Object>> capturedVariables = new CSLinked<Tuple2<String , Object>>();
	//list of deep-copied ECS data that will be used if this goes out of sync
	private CSLinked<Tuple2<ECS , Object[]>> capturedComponents = new CSLinked<Tuple2<ECS , Object[]>>();
	
	//optionally, a particular callback can be given for when it comes time to resynchronize python variables 
	private CSCHashMap<Consumer<Object> , String > onResyncCallbacks = new CSCHashMap<Consumer<Object> , String >(17);
	
	//ring buffer of arrays of input states from the current frame and previous ones
	private LinkedRingBuffer<byte[]> previousInputs;
	
	//The values within this array represent IDs of controls.
	private volatile byte[] syncedControls = {};
	//used to synchronize the updates a client sends with the server
	private byte updateNumber = 1;
	
	//used to adapt synchronized variables that need to execute code when they get rewound
	private final ClassicPyObjectAdapter parameterAdapter = new ClassicPyObjectAdapter();
	
	/**
	 * 
	 * @param connectionIndex
	 * @param networked
	 * @param clientOwned — If true, the entity wrapped in {@code this} is owned by the client; 
	 * 						All NetworkedEntities created by the server should be false and only the player's NetworkedEntity should be true. 
	 */
	public NetworkedEntities(short connectionIndex , Entities networked , boolean clientOwned) {
		
		this.networked = networked;
		this.connectionIndex = connectionIndex;
		System.out.println("NetworkedEntity created with entity: " + networked.name());
		clientOwned(clientOwned);
		
	}

	public NetworkedEntities(short connectionIndex) {
		
		this.connectionIndex = connectionIndex;
		
	}
	
	private void clientOwned(boolean owned) {
		
		if(!owned) {
			
			Object[] comps = networked.components();
			comps[Entities.HCOFF + 2] = true;
			comps[Entities.VCOFF + 6] = true;
			networked.removeComponents(ECS.CAMERA_TRACK);
			
		}
		
	}

	public void runScript() {

		if(networked.has(ECS.SCRIPT)) ((EntityScripts)networked.components()[Entities.SOFF]).exec();
		
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

	public void networked(Entities entity , boolean clientOwned) {
		
		networked = entity;
		clientOwned(clientOwned);
		
	}
	
	/**
	 * Used to initially designate which controls will be synced between client and server. This is called for one of two reasons:
	 * <br><br>
	 * <b>1)</b> By a client who is setting their own entity's set of controls they will sync.
	 * <br>
	 * <b>2)</b> By a client or server who is setting another entity's RingBuffer of previous inputs which will be used for packet loss. 
	 * 
	 * @param controls — array of {@code CS.Controls}
	 */
	public void setControlsToSync(byte... controls) {
		
		syncedControls = new byte[controls.length];
		System.arraycopy(controls , 0 , syncedControls , 0 , controls.length);  
		initializePreviousInputBuffer(syncedControls.length);
		
	}
	
	/**
	 * Called by the server when it receives an update from a client about some current control state <b> OR </b> when the client
	 * receives an update from the server about some other player's entity.
	 * 
	 * @param controlsStates — current control states as specified by the packet the server or client received 
	 */
	public void updateControlStateView(byte[] controlsStates) {
		
		this.syncedControls = controlsStates;
		//i = 1 because index 0 == the tick number, a number between 1 and 60
		for(int i = 0 ; i < controlsStates.length ; i ++) {

			/*
			 * If the incoming byte says it was pressed (its eighth bit is set) but {@code this}'s view of the key
			 * is that it is not pressed (the eighth bit is unset), this will set the seventh and eighth bit.   
			 */
			if((syncedControls[i] & CONTROL_PRESSED_MASK) == 0 && (controlsStates[i] & CONTROL_PRESSED_MASK) != 0) { 
			
				syncedControls[i] |= CONTROL_PRESSED_MASK|CONTROL_STRUCK_MASK;
				
			} 
				
		}	

	}
	
	private void initializePreviousInputBuffer(int controlLength) {
		//this is the number of previous frames we will keep within the ring buffer. It's the closest multiple of controls.length that is less than
		//200 and closest to 200. That is then divided by the length of the array to give how many sets of inputs will be buffered.
		int res = (PREVIOUS_FRAMES_BUFFER_SIZE - (PREVIOUS_FRAMES_BUFFER_SIZE % controlLength)) / controlLength;
		previousInputs = new LinkedRingBuffer<>(res);
		for(int i = 0 ; i < previousInputs.capacity ; i ++) {
			
			byte[] array = new byte[controlLength];
			System.arraycopy(syncedControls, 0 , array , 0 , array.length);
			previousInputs.put(array);
			
		}
				
	}
	
	public void constructUpdatedViewOfControls() {

		boolean pressed = false;
		for(int i = 0 ; i < syncedControls.length ; i ++) {
			
			pressed = Engine.controlKeyPressed((byte)(syncedControls[i] & KEYCODE_MASK));
			if(pressed) syncedControls[i] |= CONTROL_PRESSED_MASK;
			
		}
		 
		byte[] array = new byte[syncedControls.length];
		System.arraycopy(syncedControls, 0, array, 0, syncedControls.length);
		previousInputs.put(array);
		
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

	public void unPressKeys() {
		
		for(int i = 0 ; i < syncedControls.length ; i ++) syncedControls[i] &= ~CONTROL_PRESSED_MASK|CONTROL_STRUCK_MASK;
		
	}
	
	public void sync(String name , Copy variable) {
		
		syncedVariables.add(new Tuple2<String , Copy>(name , variable));
		
	}

	public void sync(ECS component) {
		
		syncedComponents.add(component);
		
	}
	
	public void onResync(String variableName , Consumer<Object> callback) {
		
		onResyncCallbacks.put(callback , variableName);
		
	}

	public void onResync(String variableName , PyObject callback) {
		
		onResyncCallbacks.put(parameter -> callback.__call__(parameterAdapter.adapt(parameter)) , variableName);
		
	}
		
	public LinkedRingBuffer<byte[]> previousInputs() {
		
		return previousInputs;
		
	}

	public byte getUpdateNumber() {
		
		return updateNumber;
		
	}

	public void setUpdateNumber(byte update) {
		
		if(update == 61) update = 1;
		updateNumber = update;
		
	}
	
	public void incrementUpdateNumber() {
		
		updateNumber++;
		if(updateNumber == 61) updateNumber = 1;
		
	}
	
	/**
	 * Calculates the number of packets between {@code received} and {@code this.updateNumber}. The max value for updateNumber to be is 60, so some 
	 * integer overflows can cause issues with a simpler overflow.
	 * 
	 * @param received
	 */
	public byte getNumberLostPackets(byte received) {
	
		if(received > updateNumber) return (byte) (received - updateNumber);
		return (byte) (received - (60 - updateNumber));
		
	}
	
	public void captureSyncedVariables() {
		
		capturedVariables.clear();
		EntityScripts script = (EntityScripts)networked.components()[Entities.SOFF];
		//for each synced variable, call its copy callback and store the result in a tuple storing the object's name and current state 
		syncedVariables.forEachVal(tuple -> capturedVariables.add(new Tuple2<>(tuple.getFirst() , tuple.getSecond().copy(script.get(tuple.getFirst())))));
		//for each synced ecs, call its deep copy method and reset the current to this.
		syncedComponents.forEachVal(ecs -> capturedComponents.add(new Tuple2<ECS , Object[]>(ecs, ecs.deepCopy())));
		
	}
	
	public void rewindStateToCapture() {
		
		EntityScripts thisEntityScript = ((EntityScripts)networked.components()[Entities.SOFF]);		
		capturedVariables.forEachVal(tuple -> {
			
			Consumer<Object> onResyncCallback = onResyncCallbacks.get(tuple.getFirst());
			if(onResyncCallback != null) onResyncCallback.accept(tuple.getSecond());
			else thisEntityScript.set(tuple.getFirst(), tuple.getSecond());
			
		});

		Object[] components = networked.components();		
		capturedComponents.forEachVal(tuple -> System.arraycopy(tuple.getSecond() , 0 , components , tuple.getFirst().offset , tuple.getSecond().length));
		
	}
	
	public boolean initialized() {
		
		return networked != null;
		
	}
	
}