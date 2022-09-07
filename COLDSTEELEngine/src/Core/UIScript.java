package Core;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.python.core.PyCode;

import CS.Engine;
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
	public boolean initialized = false;
	public boolean show = false;
		
	/**
	 * Creates a new UIScript, taking in a UI script file, such as {@code ui_testUI.py} to point to a file to execute.  
	 * {@code run} must be called each frame to continue to render this UI. 
	 * 
	 * @param scriptName — name of a python script which will display this UI each frame.
	 */
	public UIScript(String scriptName) {
	
		this.scriptName = scriptName;
		try {
			
			python.set("lib", Engine.UI_SCRIPTING_INTERFACE);
			python.set("initialized", initialized);
			python.set("assets", CS.COLDSTEEL.assets);
			python.set("data", CS.COLDSTEEL.data);
			python.set("FALSE", 0);
			python.set("TRUE", 1);
			python.exec(UIScriptingInterface.UI_SCRIPTING_FACADE);

			code = python.compile(new FileReader(CS.COLDSTEEL.data + "scripts/" + scriptName));
			
			UIScriptingInterface.getPyUIs().add(this);
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
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
	
	public void show() {
		
		show = true;
		
	}
	
	public void hide() {
		
		show = false;
		
	}
	
	public void toggle() {
		
		show = show ? false : true;
		
	}
	
	public void run() {
		
	 	if(show) python.exec(code);		
		
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
	
	public String scriptName() {
	
		return scriptName;
		
	}	
	
	PyCode getCode() {
		
		return code;
		
	}
	
}