package Game.Levels;

import static CS.COLDSTEEL.data;
import static CS.COLDSTEEL.deleted;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import Audio.Sounds;
import CSUtil.CSTFParser;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSStack;
import CSUtil.DataStructures.cdNode;
import Core.GameFiles;
import Renderer.Textures;

/**
 * Macro Levels keep track of allocated native resources during game play and free them when the player enters a level which is 
 * no longer associated with the current macro level. 
 * 
 *   
 * @author Chris Brown
 *
 */
public class MacroLevels implements GameFiles<MacroLevels> {
	
	private String name;

	private CSStack<Textures> loadedTextures = new CSStack<>();
	private CSStack<Sounds> loadedSounds = new CSStack<>();

	private CSLinked<String> OSTIntroSegments = new CSLinked<>();
	private CSLinked<String> OSTLoopSegments = new CSLinked<>();
	
	public MacroLevels(String name) {

		this.name = name;
		
	}
	
	public MacroLevels(CharSequence namePath) {
		
		load((String) namePath);
		
	}
	
	public CSStack<Textures> loadedTextures(){
		
		return loadedTextures;
		
	}

	public CSStack<Sounds> loadedSounds(){
		
		return loadedSounds;
		
	}
	
	
	/**
	 * Creates a macro level directory under data/macrolevels and creates a text file representing the macro level object.
	 * 
	 */
	public void initialize() {
		
		try {
			
			Files.createDirectory(Paths.get(data + "macrolevels/" + name + "/"));
			
			try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(data + "macrolevels/" + name + "/" + name + ".CStf") , Charset.forName("UTF-8"))){
				
			}
			
		} catch (IOException e1) {

			e1.printStackTrace();
			
		}
		
	}
	
	public void addOSTIntroSegment(String soundFileName) {
		
		OSTIntroSegments.add(soundFileName);
		
	}
	
	public void addOSTLoopSegment(String soundFileName) {
		
		OSTLoopSegments.add(soundFileName);
		
	}
	
	public int OSTIntroSegmentsSize() {
		
		return OSTIntroSegments.size();
		
	}
	
	public int OSTLoopSegmentsSize() {
		
		return OSTLoopSegments.size();
		
	}

	public cdNode<String> OSTIntroSegmentsIter(){
		
		return OSTIntroSegments.get(0);
		
	}
	
	public cdNode<String> OSTLoopSegmentsIter(){
		
		return OSTLoopSegments.get(0);
		
	}

	public cdNode<String> safeRemoveIntroSegment(cdNode<String> iter){
		
		return OSTIntroSegments.safeRemove(iter);
		
	}
	
	public cdNode<String> safeRemoveLoopSegment(cdNode<String> iter){
		
		return OSTLoopSegments.safeRemove(iter);
		
	}
		
	public void forEachIntroSegment(Consumer<String> function) {
		
		OSTIntroSegments.forEachVal(function);
		
	}
	
	public void forEachLoopSegment(Consumer<String> function) {
		
		OSTLoopSegments.forEachVal(function);
		
	}
	
	@Override public void delete() {
		
		File deletedArchive = new File(deleted + name + "/");		
		File macroLevel = new File(data + "macrolevels/" + name + "/");
		File[] levels = macroLevel.listFiles();
		for(File x : levels) GameFiles.delete(x , deletedArchive);
	
	}

	public boolean equals(MacroLevels other) {
		
		return name.equals(other.name);
		
	}
	
	public String name() {
		
		return name;
		
	}
	
	@Override public void write(Object...additionalData) {
		
		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(data + "macrolevels/" + name + "/" + name + ".CStf") , Charset.forName("UTF-8"))){
			
			CSTFParser cstf = new CSTFParser(writer);
			cstf.wname(name);
			
			cdNode<String> iter = OSTIntroSegments.get(0);
			cstf.wlist("OST intro segments", OSTIntroSegments.size());
			for(int i = 0 ; i < OSTIntroSegments.size() ; i ++ , iter = iter.next) cstf.wvalue(iter.val);			
			cstf.endList();
			
			iter = OSTLoopSegments.get(0);
			cstf.wlist("OST loop segments", OSTLoopSegments.size());
			for(int i = 0 ; i < OSTLoopSegments.size() ; i ++ , iter = iter.next) cstf.wvalue(iter.val);			
			cstf.endList();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override public void load(String namePath) {
		
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(data + "macrolevels/" + namePath.substring(0, namePath.length() - 5) + "/" + namePath))){
			
			CSTFParser cstf = new CSTFParser(reader);
			name = cstf.rname();
			int number = cstf.rlist("OST intro segments");
			for(int i = 0 ; i < number ; i ++) OSTIntroSegments.add(cstf.rvalue());
			cstf.endList();	
			
			number = cstf.rlist("OST loop segments");
			for(int i = 0 ; i < number ; i ++) OSTLoopSegments.add(cstf.rvalue());		
			cstf.endList();
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}

	@Override public void write(BufferedWriter writer , Object...additionalData) {}

	@Override public void load(BufferedReader reader) {}
	
}
