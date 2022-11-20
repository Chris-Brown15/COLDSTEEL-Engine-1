package Game.Levels;

import static CS.COLDSTEEL.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.python.core.PyCode;

import CS.PythonScriptEngine;

public class TriggerScripts extends PythonScriptEngine {

	private final String scriptNamePath;
	PyCode code;
	boolean initialized = false;
	
	public TriggerScripts(Triggers owner , String scriptNamePath) {
		
		super();
		this.scriptNamePath = scriptNamePath;
		
		Path scriptAbsPath = Paths.get(data + "scripts/t_" + scriptNamePath);
		
		if(!Files.exists(scriptAbsPath)) {
			
			try {
				
				Files.createFile(scriptAbsPath);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}			
			
		}
		
		code = comp("t_" + scriptNamePath);
		python.set("initialized", initialized);
		python.set("T", owner);
		python.set("lib", owner.owningScene);
		python.set("assets", CS.COLDSTEEL.assets);
		python.set("data", CS.COLDSTEEL.data);
		python.set("mods", CS.COLDSTEEL.mods);
		python.set("deleted", CS.COLDSTEEL.deleted);
		run(TriggerScriptingInterface.TRIGGER_SCRIPTING_FACADE);
		
	}
	
	public void recomp() {
		
		code = comp(scriptNamePath);
		
	}
	
	void exec() {
		
		run(code);
		
	}
	
}
