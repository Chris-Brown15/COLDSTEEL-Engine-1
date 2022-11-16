package Game.Items;

import org.python.core.PyCode;
import org.python.core.PyObject;

import Audio.Sounds;
import CS.PythonScriptEngine;
import CSUtil.DataStructures.CSArray;
import Core.ECS;
import Core.Executor;
import Core.Scene;
import Core.SpriteSets;
import Core.TemporalExecutor;
import Core.Entities.Entities;
import Core.Entities.EntityAnimations;
import Core.Entities.EntityHitBoxes;

public class ItemUsable extends PythonScriptEngine{

	Executor onUse;
	Executor onStopUse;
	String useScriptNamePath;
	PyCode onUseCode;
	PyObject onStopUseCode;
	
	boolean using = false;
	
	private boolean initialized = false;
	
	ItemUsable(Scene owner , Items item , String scriptFile){

		super();
		python.set("I", item);
		python.set("lib", owner.itemScriptingInterface());
		
	}	
	
	ItemUsable(Scene owner , Items item){
		
		super();
		onUse = () -> System.err.println("No script selected for " + toString());
		python.set("I", item);
		python.set("lib", owner.itemScriptingInterface());
		
	}

	{
		
		python.set("initialized", initialized);
		python.exec(ItemScriptingInterface.ITEM_SCRIPTING_FACADE);
	}
	
	public void setVariable(String name , Object variable) {
		
		python.set(name , variable);
		
	}
	
	void onUse(String namePath) {

		useScriptNamePath = namePath != null ? namePath : useScriptNamePath;
		if(useScriptNamePath.equals("null")) {
			
			System.err.println("Invalid script path given for item use script.");
			return;
			
		}

		onUseCode = comp(useScriptNamePath);
		onStopUse = () -> python.exec("stopUse()");
		onUse = () -> {
		
			run(onUseCode);
			using = true;
			TemporalExecutor.onTicks(1, () -> using = false);
			TemporalExecutor.onTicks(2, () -> {
			
				if(!using) {
					
					try {
						
						onStopUse.execute();
						
					} catch(Exception e) {
						
						if(CS.COLDSTEEL.DEBUG_CHECKS) e.printStackTrace();
						
					}
				
				}				
				
			});
			
		};
		
	}
	
	void setOwner(ItemOwner owner) {

		python.set("owner" , owner);

		if(owner != null && owner instanceof Inventories) {
					
			if(((Inventories) owner).owner() != null) {
				
				Entities E = ((Inventories) owner).owner();
				Object[] comps;
				python.set("E" , E);
				python.set("E_components", comps = E.components());
				python.set("data", CS.COLDSTEEL.data);
				python.set("assets", CS.COLDSTEEL.assets);
				python.set("TRUE", true);
				python.set("FALSE", false);
								
				if(E.has(ECS.SCRIPT)) {
					
					python.set("HCOFF" , Entities.HCOFF);
					python.set("CDOFF" , Entities.CDOFF);
					python.set("GCOFF" , Entities.GCOFF);
					python.set("HDOFF" , Entities.HDOFF);
					python.set("VCOFF" , Entities.VCOFF);
					python.set("SOFF" , Entities.SOFF);
					python.set("AOFF" , Entities.AOFF);
					python.set("HOFF" , Entities.HOFF);
					python.set("RPGOFF" , Entities.RPGOFF);
					python.set("VDOFF" , Entities.VDOFF);
					python.set("CTOFF" , Entities.CTOFF);
					python.set("DOFF" , Entities.DOFF);
					python.set("IOFF" , Entities.IOFF);
					python.set("FOFF", Entities.FOFF);
					python.set("AEOFF", Entities.AEOFF);
					
					if(E.has(ECS.ANIMATIONS)) {
						
						SpriteSets[] anims;
						python.set("E_animations" , (anims = ((EntityAnimations)comps[Entities.AOFF]).anims()));
						python.set("E_animList", comps[Entities.AOFF]);
						
						for(int i = 0 ; i < anims.length ; i ++) if(anims[i] != null) python.set(((String) anims[i].name()).replaceAll(" ", ""), i);
						
					}
					
					if(E.has(ECS.HITBOXES)) python.set("E_hitboxList", (EntityHitBoxes)comps[Entities.HOFF]);
					if(E.has(ECS.AUDIO_EMIT)) {
						
						@SuppressWarnings("unchecked") CSArray<Sounds> sounds = (CSArray<Sounds>) comps[Entities.AEOFF];
						for(int i = 0 ; i < sounds.size() ; i ++) python.set("E" + sounds.get(i).name().replaceAll(" ", ""), i);
						
					}
					
				}
				
			}
			
		}
		
	}
	
	void recompile() {
		
		if(useScriptNamePath != null && !useScriptNamePath.equals("null")) onUseCode = comp(useScriptNamePath);
		
	}
	
	void setup(String scriptPath , ItemOwner owner) {

		useScriptNamePath = scriptPath != null ? scriptPath : useScriptNamePath;
		if(useScriptNamePath.equals("null")) {
			
			System.err.println("Invalid script path given for item use script, errrors may arise.");
			return;
			
		}
	
		setOwner(owner);
		
		onUseCode = comp(useScriptNamePath);
		onUse = () -> run(onUseCode);					
		
	}
	
}
