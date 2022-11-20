package Audio;

import static CSUtil.BigMixin.dr;
import static CSUtil.BigMixin.pointer;
import static CSUtil.BigMixin.toNamePath;
import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_STOPPED;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceRewind;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.stb.STBVorbis.nstb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memShortBufferSafe;
import static org.lwjgl.system.MemoryUtil.nmemAlloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;
import static org.lwjgl.system.libc.LibCStdlib.free;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import CSUtil.RefInt;

public class Sounds {
	
	private RefInt ID;
	
	private ShortBuffer data;
	private int channels = 0;
	private int sampleRate = 0;
	private int state = 0;
	private final int format;
	
	private int ALBufferPtr;
	private int ALSourcePtr;
	
	private boolean playing = false;
	
	private final String name;
	
	Sounds(String filepath) {

		long memory = nmemAlloc(8 * 1024 * 1024);
		MemoryStack stack = MemoryStack.ncreate(memory, 8 * 1024 * 1024);
		long pathPtr = pointer(filepath , stack);
		IntBuffer channelsPtr = stack.mallocInt(1) , sampleRatePtr = stack.mallocInt(1);
		PointerBuffer output = stack.pointers(0);
		int numberSamples = nstb_vorbis_decode_filename(pathPtr , memAddress(channelsPtr), memAddress(sampleRatePtr), memAddress(output));
		data = memShortBufferSafe(output.get(0) , numberSamples * channelsPtr.get(0));
		
		channels = dr(channelsPtr);
		sampleRate = dr(sampleRatePtr);
		if(channels == 1) format = AL_FORMAT_MONO16;
		else if(channels == 2) format = AL_FORMAT_STEREO16;
		else {
			
			format = -1;
			throw new AssertionError("ERROR: Sound file at " + filepath + " format is invalid");
			
		}
		
		ID = new RefInt(-1);
		name = toNamePath(filepath);
		ALBufferPtr = alGenBuffers();
		ALSourcePtr = alGenSources();
		alBufferData(ALBufferPtr , format , data , sampleRate);
		alSourcei(ALSourcePtr ,  AL_BUFFER , ALBufferPtr);
		free(data);
		nmemFree(memory);
			
	}
		
	void ID(int ID) {
		
		this.ID.set(ID);
		
	}
	
	public int ID() {
		
		return ID.get();
		
	}
	
	RefInt getID() {
		
		return ID;
		
	}
	
	public void setState() {
		
		try(MemoryStack stack = stackPush()){
			
			var statePtr = stack.mallocInt(1);
			alGetSourcei(ALSourcePtr , AL_SOURCE_STATE , statePtr);
			state = dr(statePtr);
			
		}
		
	}
	
	public boolean stopped() {		
		
		setState();
		return state == AL_STOPPED;
			
	}
	
	public void option(int param , float value) {
	
		alSourcef(ALSourcePtr , param , value);
		
	}	
	
	void play() {
				
		playing = true;		
		alSourcePlay(ALSourcePtr);
		
	}
	
	void pause() {
		
		playing = false;
		alSourcePause(ALSourcePtr);
		
	}
	
	void resume() {
		
		if(!playing) {
			
			playing = true;
			alSourcePlay(ALSourcePtr);
			
		}
		
	}
	
	void rewind() {
		
		playing = true;
		alSourceRewind(ALSourcePtr);
		
	}		

	public String name() {
		
		return name;
		
	}
	
	public String toString() {
		
		return name + ", ID " + ID.get();
		
	}
	
	void shutDown() {
		
		alDeleteBuffers(ALBufferPtr);
		alDeleteSources(ALSourcePtr);
						
	}
	
}