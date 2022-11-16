package Core;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.python.core.PyCode;
import org.python.core.PyObject;

import CS.PythonScriptEngine;

/**
 * Objects of this class wrap and own a UI script, which they handle. Each instance of thisc class gets a Python Interpreter which it uses.
 * {@code run} must be called for every frame this UI elememt should be rendered
 * 
 * @author Chris Brown
 *
 */
public class UIScript extends PythonScriptEngine {

	private String scriptName;
	private PyCode code;
	private PyObject toggleImpl = null;
	public boolean initialized = false;
		
	/**
	 * Creates a new UIScript, taking in a UI script file, such as {@code ui_testUI.py} to point to a file to execute.  
	 * {@code run} must be called each frame to continue to render this UI. 
	 * 
	 * @param scriptName — name of a python script which will display this UI each frame.
	 */
	public UIScript(Scene owner , String scriptName) {
			
		this.scriptName = scriptName;
		try {
			
			python.set("lib", owner.uiScriptingInterface());
			python.set("initialized", initialized);
			python.set("assets", CS.COLDSTEEL.assets);
			python.set("data", CS.COLDSTEEL.data);
			python.set("FALSE", 0);
			python.set("TRUE", 1);
			python.set("UI", this);
			python.exec(UIScriptingInterface.UI_SCRIPTING_FACADE);
			
			code = python.compile(new FileReader(CS.COLDSTEEL.data + "scripts/" + scriptName));
									
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			throw new IllegalStateException();
			
		}
		
	}	
	
	/**
	 * Called to execute the code for the UI. Doesn't need to be called every frame, but at least once to initialize and start
	 * the UI.
	 * 
	 */
	public void run() {
		
		python.exec(code);
		
	}
	
	/**
	 * Sets a python variable name to o. This allows outside sources to give additional info to UI scripts. 
	 * From within this object's script, o can be referenced and manipulated.
	 * 
	 * @param name — what python should name its variable holding o 
	 * @param o — any object
	 */
	public void set(String name , Object o) {
		
		python.set(name, o);
		
	}
	
	/**
	 * Recompiles the code of script given at creation and sets its state to unitialized. Only helpful in some situations.
	 * This cannot undo the creation of NkRects, so often many restarts are required to fine tune rectangles
	 * TODO: nk_window_set_bounds() implementation in debug mode. keep track of rects in java and call this function on them to resize them on recompile
	 */
	public void recompile() {
		
		try {
			
			code = python.compile(new FileReader(CS.COLDSTEEL.data +  "/scripts/" + scriptName));
			initialized = false;
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * Called from a script to allow the user of {@code this} to toggle the UI.
	 * 
	 * @param toggleCode — some python code to execute
	 * 
	 */
	public void setToggleImpl(PyObject toggleCode) {
		
		this.toggleImpl = toggleCode;
		
	}
	
	/**
	 * Calls the current implementation of toggle.
	 */
	public void toggle() {
	
		if(toggleImpl != null) toggleImpl.__call__();
		
	}
	
	public PyObject get(String variableName) {
		
		PyObject gotten = python.get(variableName);		
		return gotten;
		
	}
	
}