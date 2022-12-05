package Networking.Utils;

import static Networking.Utils.NetworkingConstants.*;

import CSUtil.DataStructures.CSQueue;
import CSUtil.DataStructures.RingBuffer;

import java.nio.ByteBuffer;
import CS.Engine;

/**
 * This class is a minimal-state conviencience class for constructing arrays for packets given a particular layout.
 * You can provide data which is to be sent over the wire and call {@code get} to get the packet specified by the data given.
 * Packets constructed by this class can also be interpreted by it.  
 * 
 * Internally this class makes one static allocation of byte buffers and reuses them. 
 * 
 * @author Chris Brown
 *
 */
public final class PacketCoder implements AutoCloseable {

	private static final RingBuffer<ByteBuffer> BUFFER_POOL = new RingBuffer<>(10);	
	
	static {
		
		for(int i = 0 ; i < BUFFER_POOL.capacity() ; i ++) BUFFER_POOL.put(ByteBuffer.allocate(512));
		
	}	
	
	private static final void putBack(ByteBuffer resetBuffer) {
			
		BUFFER_POOL.put(resetBuffer);
		
	}
	
	private static final ByteBuffer nextBuffer() { 
		
		ByteBuffer next = BUFFER_POOL.get();
		return next;
		
	}	
	
	public static final byte
		POSITION = 1 ,
		CONNECTION_ID = 2 ,
		STRING = -107 ,
		REPITITION = 3,
		UPDATE_SEQUENCE = 4, 
		/*
		 * Control IDs will arrive at the server. these will consist of some of the first 6 bits set and possibly the 
		 * last bit.
		 * 
		 * if the seventh bit is set, this tells the server to stop reading as controls.
		 * 
		 */
		CONTROL_KEY_STROKES = 0b01000000

	;

	private ByteBuffer bytes;
	
	{
		bytes = nextBuffer();
	}
	
	public PacketCoder() {}
	
	public PacketCoder(byte[] source) {
		
		bytes.put(source);
		
	}
	
	public PacketCoder(byte[] source , int offset) {

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
	
	public PacketCoder bControlStrokes(byte tickNumber , byte[] controls) {
		
		//bytes in controls are control IDs a client is sending to the server.
		
		bytes.put(CONTROL_KEY_STROKES);
		bytes.put(tickNumber);
		boolean pressed = false;
		for(int i = 1 ; i < controls.length ; i ++) {
			
			pressed = Engine.controlKeyPressed((byte)(controls[i] & KEYCODE_MASK));
			if(pressed) bytes.put((byte) (controls[i] | CONTROL_PRESSED_MASK));
			else bytes.put((byte)controls[i]);
			
		}
		
		bytes.put(CONTROL_KEY_STROKES);
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
	
	public byte[] rControlStrokes() {
		
		boolean correct = bytes.get() == CONTROL_KEY_STROKES;
		if(!correct) except("buffer read control key strokes at invalid position");
		
		//this is an array of bytes so read byte by byte, constructing an array of shorts representing them
		//the first element is the tick number this was sent on, so it is not a control
		int startingPos = bytes.position() , endingPos = bytes.position();
		while(bytes.get(endingPos++) != CONTROL_KEY_STROKES);
		byte[] controls = new byte[1 + endingPos - 1 - startingPos];
		controls[0] = bytes.get();
		for(int i = 1 ; i < controls.length ; i ++) controls[i] = bytes.get();		
		bytes.position(endingPos + 1);
		return controls;
		
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

	@Override public void close() {

		bytes.rewind();
		putBack(bytes);
				
	}
	
}
