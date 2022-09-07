package CS;

import static CS.COLDSTEEL.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public abstract class PythonScriptEngine {
	
	static {
		
		System.out.println("Beginning Python initialization...");
		PythonInterpreter.initialize(System.getProperties(), System.getProperties() , null);
		
	}
	
	protected PythonInterpreter python;

	protected PythonScriptEngine(){
		
		python = new PythonInterpreter();
		python.set("javaClassPath", System.getProperty("user.dir") + File.separator + "bin" + File.separator);			
		
	}
	
	protected void run(PyCode execMe) {
		
		python.exec(execMe);
		
	}
	
	/**
	 * Returns PyCode compilation for a script provided. Note that {@code scriptFilePath} should be a namepath, meaning the name of the file with its
	 * extension
	 * 
	 * @param scriptFilePath
	 * @return
	 */
	protected PyCode comp(String scriptNamePath) {
		
		try {
			
			return python.compile(new FileReader(data + "scripts/" + scriptNamePath));			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			System.err.println(scriptNamePath);
			return null;
			
		}
		
	}
	
	/**
	 * Get a PyObject view of a variable with the given name.
	 * 
	 * @param variableName — String representation of the name of the variable to retrieve
	 * @return PyObject representation of the variable queried; <br>
	 * 		   to get it back to a java object, if appropriate, call {@code (CLASSTYPE)x.__toJava__(CLASSTYPE)}
	 */
	public PyObject get(String variableName) {
		
		return python.get(variableName);
		
	}
	
	/**
	 * Set or create a variable whose name is {@code variableName} that references {@code variable}.
	 * 
	 * @param variableName — String representation of a variable name
	 * @param variable — Java Object reference to the variable 
	 */
	public void set(String variableName , Object variable) {
		
		python.set(variableName, variable);
		
	}

	public void call(String pythonCode) {
		
		if(CS.COLDSTEEL.DEBUG_CHECKS) {
			
			try {
				
				python.exec(pythonCode);
				
			} catch(Exception e) {}
			
		} else python.exec(pythonCode);
				
	}

	public void call(PyCode pythonCode) {

		if(CS.COLDSTEEL.DEBUG_CHECKS) {
			
			try {
				
				python.exec(pythonCode);
				
			} catch(Exception e) {}
			
		} else python.exec(pythonCode);
		
	}
	

	protected void shutDown(){

		python.cleanup();
		python.close();

	}

}