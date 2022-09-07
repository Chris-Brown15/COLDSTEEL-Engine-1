package CS;

import org.python.core.PyCode;

public final class EnginePython extends PythonScriptEngine{
	
	EnginePython(){
		
		super();
		System.out.println("Python initialization complete.");		
		run(comp("CS_startupTextScript.py"));
		
	}
	
	public PyCode compile(String namePath) {
		
		return comp(namePath);
		
	}
	
}
