package CSUtil;

import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSOHashMap;
import Core.Executor;

/**
 * This class allows for the creation of functions whose functionality can be expanded programatically.
 * 
 * @param callbackName
 * @param code
 */
public final class GlobalCallbacks {

	private static final CSOHashMap<GlobalCallbacks , String> callbacks = new CSOHashMap<GlobalCallbacks , String>(97);
	
	private final String callbackName;
	private final CSLinked<Executor> functions = new CSLinked<>();
	
	private GlobalCallbacks(String name) {
		
		this.callbackName = name;
		
	}
	
	public static GlobalCallbacks newCallback(String callbackName) {
		
		GlobalCallbacks newCallback = new GlobalCallbacks(callbackName);
		callbacks.add(newCallback , callbackName);
		return newCallback;
		
	}
	
	public static void addFunction(String callbackName , Executor code) {
		
		callbacks.bucket(callbackName).getValIfExists((callback) -> callback.callbackName.equals(callbackName)).addFunction(code);
		
	}
	
	public void invoke() {
	
		functions.forEachVal(Executor::execute);
		
	}	
	
	private void addFunction(Executor code) {
		
		functions.add(code);
		
	}
	
}
