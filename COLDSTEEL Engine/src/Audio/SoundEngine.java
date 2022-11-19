package Audio;

import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.openal.AL10.alListenerf;
import static org.lwjgl.openal.AL10.alGetListenerf;

import java.util.function.Consumer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

import CSUtil.RefInt;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSStack;
import CSUtil.DataStructures.cdNode;
import Game.Levels.MacroLevels;

public abstract class SoundEngine {

	private static final Thread AUDIO_THREAD;

	private final static CSLinked<Sounds> sounds = new CSLinked<Sounds>();
	private static String deviceName;
	private static long ALDevice;	
	private static long ALContext;
	public static volatile boolean persist = true;
	
	static {
		
		AUDIO_THREAD = new Thread(() -> {
			
			initialize();
			while(persist);
			shutDown();
			
		});
		
	}	
	
	public static final void threadSpinup() {
		
		AUDIO_THREAD.start();
		
	}
	
	public static final void initialize() {

		deviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		ALDevice = alcOpenDevice(deviceName);
		ALContext = alcCreateContext(ALDevice , new int[] {0});
		alcMakeContextCurrent(ALContext);
		AL.createCapabilities(ALC.createCapabilities(ALDevice));
		
	}	
		
	/**
	 * Given a filepath or name of a sound file, this returns it's index.
	 * 
	 * @param filename — file path or file name
	 * @return — int representing the index of the sound in the current list of sounds.
	 */
	public static Sounds add(String filename) {
		
		Sounds newSound = new Sounds(filename);
		sounds.add(newSound);
		newSound.ID(sounds.size() - 1);
		return newSound;
				
	}
	
	public static void freeMacroLevel(MacroLevels freeThis) {
		
		CSStack<Sounds> macroLevelSounds = freeThis.loadedSounds();
		while(!(macroLevelSounds.empty())) sounds.removeVal(macroLevelSounds.pop()).val.shutDown();
		
	}
	
	public static void forEach(Consumer<Sounds> function) {
		
		sounds.forEachVal(function);
		
	}
	
	public static void remove(Sounds sound) {
		
		cdNode<Sounds> removedNode = sounds.removeVal(sound);
		for(RefInt i = removedNode.val.getID() ; i.get() < sounds.size() ; i.add(1) , removedNode = removedNode.next) removedNode.val.getID().dec();
		sound.shutDown();
		
	}

	public static void play(Sounds sound) {
		
		sound.play();
		
	}

	public static void pause(Sounds sound) {
		
		sound.pause();
	
		
	}

	public static void rewind(Sounds sound) {
		
		sound.rewind();
		
	}

	public static void resume(Sounds sound) {
		
		sound.resume();
		
	}

	public static void setState(Sounds sound , int option , float value) {
		
		sound.option(option, value);
		
	}

	public static boolean stopped(Sounds sound) {
		
		return sound.stopped();
		
	}
	
	public static boolean stopped(int index) {
		
		return sounds.getVal(index).stopped();
						
	}
	
	public static int size() {
		
		return sounds.size();
		
	}
	
	public static void shutDown() {

		alcDestroyContext(ALContext);
		alcCloseDevice(ALDevice);
	
		for(int i = 0 ; i < sounds.size() ; i ++) ((Sounds) sounds.getVal(i)).shutDown();
		
		System.out.println("Sound Engine Shut Down.");
		
	}
	
	public static void setGlobalVolume(float volume) {
		
		alListenerf(AL_GAIN , volume);
		
	}
	
	public static float getGlobalVolume() {
		
		return alGetListenerf(AL_GAIN);
		
	}
	
}
