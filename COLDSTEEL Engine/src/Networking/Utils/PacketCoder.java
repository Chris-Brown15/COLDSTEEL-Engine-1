package Networking.Utils;

import CSUtil.DataStructures.CSQueue;
import java.nio.ByteBuffer;

import CS.Engine;

/**
 * This class is a minimal-state conviencience class for constructing arrays for packets given a particular layout.
 * You can provide data which is to be sent over the wire and call {@code get} to get the packet specified by the data given.
 * Packets constructed by this class can also be interpreted by it.  
 * 
 * @author Chris Brown
 *
 */
public final class PacketCoder {

	public static final byte
		POSITION = 1 ,
		CONNECTION_ID = 2 ,
		STRING = -107 ,
		REPITITION = 3,
		/*
		 * These flags tightly pack keystroke data into as few bytes as possible. They are marked as negative
		 * to avoid collision with valid key codes.
		 */
		KEYBOARD_KEY_STROKES = -4,
		MOUSE_KEY_STROKES = -5,
		GAMEPAD_KEY_STROKES = -6
	;

	public static final float[] rposition(byte[] bytes , int startIndex) {

		if(bytes[startIndex] != POSITION) except("Static read position at invalid position.");
		return ByteArrayUtils.toFloats(startIndex, Float.BYTES * 2, bytes);		
		
	}
	
	public static final short rconnectionID(byte[] bytes , int startIndex) {
		
		if(bytes[startIndex] != CONNECTION_ID) except("Static read connection at invalid position");
		return ByteArrayUtils.toShorts(startIndex, Short.BYTES, bytes)[0];
		
	}

	public static final String rstring(byte[] bytes , int startIndex) {
		
		if(bytes[startIndex] != STRING) except("Static read string at invalid position");
		int pos = startIndex + 1, stringStart = startIndex + 1;
		while(bytes[pos++] != STRING) ;
		String newStr = new String(bytes , stringStart , pos - 1 - stringStart);
		return newStr;
		
	}
	
	public static final CSQueue<Object> rRepititions(byte[] bytes , int startIndex , byte...types) {
		
		if(bytes[startIndex] != REPITITION) except("Static read repititions at invalid position");
		int off = startIndex + 1;
		short reps = ByteArrayUtils.toShorts(off , Short.BYTES , bytes)[0]; off += 2;
		short numberItemsPerRep = bytes[off]; off += 1;
		if(numberItemsPerRep < 0) numberItemsPerRep *= -1; 
		
		CSQueue<Object> readQueue = new CSQueue<Object>();
		reps *= numberItemsPerRep;
		
		for(int i = 0 , j = 0; i < reps ; i ++) {
			
			switch(types[j]) {
			
				case POSITION ->  {
					
					readQueue.enqueue(rposition(bytes , off));
					off += 1 + (Float.BYTES * 2);
					
				}
				
				case CONNECTION_ID -> {
					
					readQueue.enqueue(rconnectionID(bytes , off));
					off += 1 + (Short.BYTES);
					
				}
				
				case STRING -> {
					
					readQueue.enqueue(rstring(bytes , off));
					off += 2 + (((String) readQueue.peek()).length());
					
				}
			
			}
			
			if(++j == types.length) j = 0;
			
		}
		
		return readQueue;
		
	}
	
	private ByteBuffer bytes;
	
	public PacketCoder() {
		
		bytes = ByteBuffer.allocate(512);

	}
	
	public PacketCoder(byte[] source) {
		
		bytes = ByteBuffer.wrap(source);
		
	}
	
	public PacketCoder(byte[] source , int offset) {

		bytes = ByteBuffer.allocate(512);
		byte[] bytesArray = bytes.array();
		System.arraycopy(source, offset, bytesArray, 0, source.length - offset);
		
	}
	
	public PacketCoder bflag(byte flag) {
		
		boolean correct = bytes.position() == 0 && NetworkingConstants.isFlag(flag);
		if(!correct) except("buffer write position invalid or invalid argument");
		bytes.put(flag);
		return this;
		
	}
	
	public PacketCoder bposition(float[] position) {
		
		bytes.put(POSITION).putFloat(position[0]).putFloat(position[1]);
		return this;
		
	}
	
	public PacketCoder bconnectionID(short ID) {
		
		bytes.put(CONNECTION_ID).putShort(ID);
		return this;
		
	}

	public PacketCoder bstring(String string) {
				
		bytes.put(STRING).put(string.getBytes()).put(STRING);
		return this;
		
	}
	
	public PacketCoder brepitition(short repititions , byte numberItemsPerRep) {
		
		bytes.put(REPITITION).putShort(repititions).put(numberItemsPerRep);
		return this;
		
	}
	
	public PacketCoder bkeyboardKeyStrokes(byte[] networkedKeys) {
		
		bytes.put(KEYBOARD_KEY_STROKES);
		
		/*
		 * Steps for each byte:
		 * 1) get the state of the glfw key mapped to this key
		 * 2) set the eighth bit if the glfw key is pressed
		 * 3) buffer that byte
		 * Do this for every key for keyboard, mouse, and gamepad
		 */
		byte thisCode;
		for(int i = 0 ; i < networkedKeys.length ; i ++) {
				
			thisCode = networkedKeys[i];
			bytes.put(Engine.cs_keyboardPressed(thisCode) ? thisCode |= NetworkingConstants.KEY_PRESSED_MASK : thisCode);
			
		}				
		
		bytes.put(KEYBOARD_KEY_STROKES);
		return this;
		
	}

	public PacketCoder bmouseKeyStrokes(byte[] networkedKeys) {
		
		bytes.put(MOUSE_KEY_STROKES);
		
		byte thisCode;
		for(int i = 0 ; i < networkedKeys.length ; i ++) {
				
			thisCode = networkedKeys[i];
			bytes.put(Engine.cs_mousePressed(thisCode) ? thisCode |= NetworkingConstants.KEY_PRESSED_MASK : thisCode);
			
		}				
		
		bytes.put(MOUSE_KEY_STROKES);
		return this;
		
	}

	public PacketCoder bgamepadKeyStrokes(byte[] networkedKeys) {
		
		bytes.put(GAMEPAD_KEY_STROKES);
		
		byte thisCode;
		for(int i = 0 ; i < networkedKeys.length ; i ++) {
				
			thisCode = networkedKeys[i];
			bytes.put(Engine.cs_keyboardPressed(thisCode) ? thisCode |= NetworkingConstants.KEY_PRESSED_MASK : thisCode);
			
		}				
		
		bytes.put(GAMEPAD_KEY_STROKES);
		return this;
		
	}
	
	public float[] rposition() {

		boolean correct = bytes.get() == POSITION;
		if(!correct) except("Buffer read position at invalid position");
		return new float[] {bytes.getFloat() , bytes.getFloat()};
		
	}
	
	public short rconnectionID() {
		
		boolean correct = bytes.get() == CONNECTION_ID; 
		if(!correct) except("Buffer read connection at invalid position");
		return bytes.getShort();
		
	}

	public String rstring() {
		
		boolean correct = bytes.get() == STRING;
		if(!correct) except("Buffer read string at invalid position");
		int pos = bytes.position() , stringStart = bytes.position();
		while(bytes.get(pos++) != STRING) ;
		String newStr = new String(bytes.array() , stringStart , pos - 1 - stringStart);
		bytes.position(pos);
		return newStr;
		
	}
	
	public CSQueue<Object> rRepititions(byte...types) {
		
		boolean correct = bytes.get() == REPITITION;
		if(!correct) except("buffer read repititions at invalid position");
		short reps = bytes.getShort();
		short numberItems = bytes.get();
		if(numberItems < 0) numberItems *= -1; 
		
		CSQueue<Object> readQueue = new CSQueue<Object>();
		reps *= numberItems;
		
		for(int i = 0 , j = 0; i < reps ; i ++) {
			
			switch(types[j]) {
			
				case POSITION -> readQueue.enqueue(rposition());
				case CONNECTION_ID -> readQueue.enqueue(rconnectionID());
				case STRING -> readQueue.enqueue(rstring());
			
			}
			
			if(++j == types.length) j = 0;
			
		}
		
		return readQueue;
		
	}
	
	public byte[] rkeyboardKeyStrokes() {
		
		boolean correct = bytes.get() == KEYBOARD_KEY_STROKES;
		if(!correct) except("buffer read keyboard key strokes at invalid position");
		
		int startingPos = bytes.position() , endingPos = bytes.position();
		//find the end
		while(bytes.get(endingPos++) != KEYBOARD_KEY_STROKES);
		//the length is the difference between the flag and the starting pos -1 because ending pos is the index of the flag which tells to stop
		byte[] keys = new byte[endingPos - 1 - startingPos];
		//copy bytes from coder to returned value
		for(int i = 0 ; i < keys.length ; i ++) keys[i] = bytes.get(startingPos++);
		bytes.position(endingPos);
		return keys;
		
	}

	public byte[] rmouseKeyStrokes() {
		
		boolean correct = bytes.get() == MOUSE_KEY_STROKES;
		if(!correct) except("buffer read mouse key strokes at invalid position");
		
		int startingPos = bytes.position() , endingPos = bytes.position();
		while(bytes.get(endingPos++) != MOUSE_KEY_STROKES);
		byte[] keys = new byte[endingPos - 1 - startingPos];
		for(int i = 0 ; i < keys.length ; i ++) keys[i] = bytes.get(startingPos++);
		bytes.position(endingPos);
		return keys;
		
	}

	public byte[] rgamepadKeyStrokes() {
		
		boolean correct = bytes.get() == GAMEPAD_KEY_STROKES;
		if(!correct) except("buffer read gamepad key strokes at invalid position");
		
		int startingPos = bytes.position() , endingPos = bytes.position();
		while(bytes.get(endingPos++) != GAMEPAD_KEY_STROKES);
		byte[] keys = new byte[endingPos - 1 - startingPos];
		for(int i = 0 ; i < keys.length ; i ++) keys[i] = bytes.get(startingPos++);
		bytes.position(endingPos);
		return keys;
		
	}
	
	public byte[] get() {
		
		byte[] packetData = new byte[bytes.position()];
		System.arraycopy(bytes.array(), 0, packetData, 0, packetData.length);
		return packetData;
		
	}
	
	public void rewind() {
		
		bytes.rewind();
		
	}
	
	public int position() {
		
		return bytes.position();
		
	}
	
	public boolean testFor(byte incomingType) {
		
		return bytes.get(bytes.position()) == incomingType;	
		
	}
	
	private static void except(String error) {
		
		throw new IllegalArgumentException("PACKET BUILDER ERROR: " + error);
		
	}
	
}
