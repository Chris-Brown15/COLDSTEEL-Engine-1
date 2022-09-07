package Core.Entities;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.python.core.PyCode;
import org.python.core.PyObject;

import AudioEngine.Sounds;
import CS.Engine;
import CS.PythonScriptEngine;
import CSUtil.DataStructures.CSArray;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.cdNode;
import Core.SpriteSets;
import Core.ECS;
import Game.Items.Inventories;

/**
 * 
 * This class represents an entity's script. It lightly wraps data and functions needed for entities to be able to be scripted.  <br><br> 
 * This class is largely redundant on it's own. Scripts make much greater use of {@code EntityScriptingWrapper}, which provides 
 * all the needed functions to properly script an entity.
 * 
 * The size of this memory is given by {@code ENTITY_MEMORY_SIZE_KILOS} which defaults to 16. <br> 
 *  
 * @author Chris Brown
 *
 */
public class EntityScripts extends PythonScriptEngine{
		
	private PyCode code = null;
	private String scriptName;
	private boolean initialized = false;
	//if true, this script will stop running once its initialization is complete.
	boolean stopOnInitialization = false;
	
	/**
	 * Initializes this Entity's Script Interpreter. It will use the script name given to compile the script into PyCode, then 
	 * initialize it's memory. <br>
	 * After this it gives function aliases as python variables for this entity's script to use.
	 * 
	 * @param E — reference to the entity to which this object belongs
	 * @param scriptName — the name ONLY of the script to be used. This is essentially a final field.
	 */
	public EntityScripts(Entities E , String scriptName){
		
		try {
			
			code = python.compile(new FileReader(CS.COLDSTEEL.data + "scripts/" + scriptName));
			this.scriptName = scriptName;
						
			python.set("E" , E);
			
			python.set("data", CS.COLDSTEEL.data);
			python.set("assets", CS.COLDSTEEL.assets);
			
			Object[] comps = E.components();
			
			python.set("components", comps);
			python.set("initialized", initialized);
			python.set("stopOnInitialization", stopOnInitialization);
												
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
    		
    		python.set("lib" , Engine.ENTITY_SCRIPTING_INTERFACE);
    		
    		if(E.has(ECS.ANIMATIONS)) {
    			
    			SpriteSets[] anims;
    			python.set("animations" , (anims = ((EntityAnimations)comps[Entities.AOFF]).anims()));
    			python.set("animList", comps[Entities.AOFF]);
    			
    			for(int i = 0 ; i < anims.length ; i ++) if(anims[i] != null) python.set(((String) anims[i].name()).replaceAll(" ", ""), i);
    			
    		}
    		
    		if(E.has(ECS.INVENTORY)) python.set("inventory" , (Inventories)comps[Entities.IOFF]);    		
    		if(E.has(ECS.HITBOXES)) python.set("hitboxList", (EntityHitBoxes)comps[Entities.HOFF]);
    		if(E.has(ECS.AUDIO_EMIT)) {
    			
    			@SuppressWarnings("unchecked") CSArray<Sounds> sounds = (CSArray<Sounds>) comps[Entities.AEOFF];
    			for(int i = 0 ; i < sounds.size() ;  i ++) {
    				
    				String name = sounds.get(i).name().replaceAll(" ", "");
    				name = name.substring(0, name.length() - 4);
    				python.set(name , sounds.get(i));
    				
    			}
    			
    		}
    		
    		if(E.has(ECS.RPG_STATS)) python.set("rpgStats" , (EntityRPGStats)comps[Entities.RPGOFF]);
    		
			python.exec(EntityScriptingInterface.ENTITY_SCRIPTING_FACADE);    		
			
		} catch (Exception e) {
			
			System.err.println("Failed to load script: " + CS.COLDSTEEL.data + "scripts/" + scriptName);
			
			e.printStackTrace();
			
		}
	
	}
	
	public void recompile() {
		
		try {
			
			code = python.compile(new FileReader(CS.COLDSTEEL.data + "scripts/" + scriptName));
			call(EntityScriptingInterface.ENTITY_SCRIPTING_FACADE);
			System.out.println("Recompiled Script: " + scriptName);		
			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		
	}
	
	public String scriptName() {
		
		return scriptName;
		
	}
	
	public void exec() {
		
		if(CS.COLDSTEEL.DEBUG_CHECKS) {
			
			try {
				
				python.exec(code);
				
			} catch(Exception e) {
				
				e.printStackTrace();
				
			}
			
		} else {
			
			python.exec(code);
			
		}
		
		
	}
		
	public CSLinked<Tuple2<PyObject , PyObject>> getAllVariables() {
		
		python.exec("getAllVariables()");
		PyObject list = python.get("variableList");
		@SuppressWarnings("unchecked")
		CSLinked<Tuple2<PyObject , PyObject>> allMembers = (CSLinked<Tuple2<PyObject , PyObject>>)list.__tojava__(CSLinked.class);
		cdNode<Tuple2<PyObject , PyObject>> iter = allMembers.get(0);
		boolean bumpedIterator = false;
		for(int i = 0 ; i < allMembers.size() ; i ++) {
			
			try {
			
				if(iter.val.getSecond().getType() == Engine.PYFUNCTION_PYTYPE) {
					
					iter = allMembers.safeRemove(iter);
					bumpedIterator = true;
					
				} else {
					
					iter = iter.next;
					bumpedIterator = true;
					
				}
				
			} catch(ClassCastException|NullPointerException n) {} finally {
				
				if(!bumpedIterator) iter = iter.next;
				
				bumpedIterator = false;
				
			}			
			
		}
		
		return allMembers;
		
	}

	/**
	 * Returns a {@code CSLinked} list of {@code Tuple2}s, each holding a reference to a python member name and value belonging to
	 * the owning entity's python instance.
	 * <br> <br>
	 * The returned list is sorted alphabetically by the variable's names.
	 * 
	 * @return — CSLinkedList<Tuple2<PyObject , PyObject>> containing all members in the current python instance
	 */
	@SuppressWarnings("unchecked")
	public CSLinked<Tuple2<PyObject , PyObject>> getAllMembers(){
		
		python.exec("getAllVariables()");
		//This list PyObject is a global variable created when getAllVariables() is called which is a CSLinked list of Tuple2s which
		//are python variable names and values.
		PyObject list = python.get("variableList");
		CSLinked<Tuple2<PyObject , PyObject>> members = (CSLinked<Tuple2<PyObject , PyObject>>)list.__tojava__(CSLinked.class); 
		
		return members; 
				
	}
		public boolean initialized() {
		
		return initialized;
		
	}
	
}