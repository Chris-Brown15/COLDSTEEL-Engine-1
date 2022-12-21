package Networking.Utils;

import CSUtil.DataStructures.CSQueue;
import CSUtil.DataStructures.LinkedRingBuffer;
import CSUtil.DataStructures.RingBuffer;

import java.nio.ByteBuffer;

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
		if(!correct) except("buffer write position invalid or invalid argument" , flag);
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
	
	public PacketCoder bControlStrokes(final byte updateNumber , LinkedRingBuffer<byte[]> controls) {
		
		bytes.put(UPDATE_SEQUENCE);
		bytes.put(updateNumber);
		
		controls.forEach(controlState -> {
			//bytes in controls are control IDs a client is sending to the server.
			bytes.put(CONTROL_KEY_STROKES);		
			bytes.put(controlState);
			bytes.put(CONTROL_KEY_STROKES);
			
		});
				
		return this;
		
	}
	
	public float[] rposition() {

		byte correct = bytes.get();
		if(correct != POSITION) except("Buffer read position at invalid position" , correct);
		return new float[] {bytes.getFloat() , bytes.getFloat()};
		
	}
	
	public short rconnectionID() {
		
		byte correct = bytes.get(); 
		if(correct != CONNECTION_ID) except("Buffer read connection at invalid position" , correct);
		return bytes.getShort();
		
	}

	public String rstring() {
		
		byte correct = bytes.get();
		if(correct != STRING) except("Buffer read string at invalid position" , correct);
		int pos = bytes.position() , stringStart = bytes.position();
		while(bytes.get(pos++) != STRING) ;
		String newStr = new String(bytes.array() , stringStart , pos - 1 - stringStart);
		bytes.position(pos);
		return newStr;
		
	}
	
	public CSQueue<Object> rRepititions(byte...types) {
		
		byte flag = bytes.get();
		if(flag != REPITITION) except("buffer read repititions at invalid position" , flag);
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
	
	public byte rControlStrokes(final LinkedRingBuffer<byte[]> controlStores) {
	
		byte flag = bytes.get();
		if(flag != UPDATE_SEQUENCE) except("buffer read update at invalid position" , flag);
		
		byte updateNumber = bytes.get();		
				
		for(int i = 0 ; i < controlStores.capacity ; i++) {
			
			flag = bytes.get();
			if(flag != CONTROL_KEY_STROKES) except("buffer read control key strokes at invalid position" , flag);
					
			byte[] thisInput = new byte[controlStores.getAndPut().length];
			int iter = 0;
			byte currentByte;
			while((currentByte = bytes.get()) != CONTROL_KEY_STROKES) thisInput[iter++] = currentByte;
			controlStores.put(thisInput);
			
		}
		
		return updateNumber;
		
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
		
	private static void except(String error , byte given) {
		
		throw new IllegalArgumentException("PACKET BUILDER ERROR: " + error + " given byte: " + given);
		
	}

	@Override public void close() {

		bytes.rewind();
		putBack(bytes);
				
	}
	
}