package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import CSUtil.Timer;

/**
 * Reliable Datagram Packet. When a packet is created, it is added to a list of DatagramPackets and will continue to send until an ACK is received.
 * For a recipient to know if a DatagramPacket is a reliable packet, the first 4 bytes should contain a special value notating as much.
 * 
 * @author Chris Brown
 *
 */
public final class ReliableDatagram {

	private static final int RELIABLE_DATAGRAM_KEY = 1701143909;//randomly selected
	private static final byte RELIABLE_DATAGRAM_KEY_BYTE = 101;

	//keeps track of elapsed seconds
	private static final Timer SECOND_TIMER = new Timer();
	
	private static final ConcurrentHashMap<Integer , ReliableDatagram> DATAGRAMS = new ConcurrentHashMap<>();
	private static final ByteBuffer HASH_CODE_CONVERTER = ByteBuffer.allocate(4);	

	/*
	 * Used to keep track of round trip time of packets for the last second. 
	 * If this value gets above some specific amount, ~300 milliseconds, we can enter a desperate mode 
	 * where we send packets less frequently.
	 * 
	 * We can use Java's LocalTime to keep track of when a reliable datagram was sent, and use that class to get a difference
	 * in times from when we create a reliable datagram and when we receive an ack for it, and this we can use to determine
	 * the round trip time.
	 * 
	 */
	private static double rawRTT = 0;
	private static int numberAcksReceivedLastSecond = 0;
		
	private final int cooldownMillis;	
	private final DatagramPacket packet;
	private final int hash;
	private final Timer timer = new Timer();
	private final double creationTime = System.currentTimeMillis();
	
	/**
	 * Constructs a datagram packet whose destination is undefined.
	 * 
	 * @param data
	 * @param hash
	 * @param cooldownMillis
	 */
	private ReliableDatagram(byte[] data , int hash , int cooldownMillis) {
		
		byte[] fixedData = new byte[data.length + 8];

		fixedData[0] = RELIABLE_DATAGRAM_KEY_BYTE;
		fixedData[1] = RELIABLE_DATAGRAM_KEY_BYTE;
		fixedData[2] = RELIABLE_DATAGRAM_KEY_BYTE;
		fixedData[3] = RELIABLE_DATAGRAM_KEY_BYTE;
		
		HASH_CODE_CONVERTER.putInt(0 , hash);
		
		fixedData[4] = HASH_CODE_CONVERTER.get(0);
		fixedData[5] = HASH_CODE_CONVERTER.get(1);
		fixedData[6] = HASH_CODE_CONVERTER.get(2);
		fixedData[7] = HASH_CODE_CONVERTER.get(3);
		
		System.arraycopy(data, 0, fixedData, 8, data.length);
		HASH_CODE_CONVERTER.clear();
		packet = new DatagramPacket(fixedData , fixedData.length);
		
		this.hash = hash;
		this.cooldownMillis = cooldownMillis;
		timer.start();
		
	}
	
	/**
	 * Constructs a datagram packet that will be sent to the specified address and port.
	 * 
	 * @param data
	 * @param hash
	 * @param cooldownMillis
	 * @param addr
	 * @param port
	 */
	private ReliableDatagram(byte[] data , int hash , int cooldownMillis , InetAddress addr , int port) {
		
		byte[] fixedData = new byte[data.length + 8];

		fixedData[0] = RELIABLE_DATAGRAM_KEY_BYTE;
		fixedData[1] = RELIABLE_DATAGRAM_KEY_BYTE;
		fixedData[2] = RELIABLE_DATAGRAM_KEY_BYTE;
		fixedData[3] = RELIABLE_DATAGRAM_KEY_BYTE;
			
		HASH_CODE_CONVERTER.putInt(0 , hash);
		
		fixedData[4] = HASH_CODE_CONVERTER.get(0);
		fixedData[5] = HASH_CODE_CONVERTER.get(1);
		fixedData[6] = HASH_CODE_CONVERTER.get(2);
		fixedData[7] = HASH_CODE_CONVERTER.get(3);
		
		System.arraycopy(data, 0, fixedData, 8, data.length);
		HASH_CODE_CONVERTER.clear();
		packet = new DatagramPacket(fixedData , fixedData.length , addr , port);
		
		this.hash = hash;
		this.cooldownMillis = cooldownMillis;
		timer.start();
		
	}
	
	/**
	 * XXX TEST ME
	 * 
	 * @param data
	 * @param offset
	 * @param hash
	 * @param cooldownMillis
	 * @param addr
	 * @param port
	 */
	private ReliableDatagram(byte[] data , int offset , int hash , int cooldownMillis , InetAddress addr , int port) {
		
		byte[] fixedData = new byte[(data.length - offset) + 8];

		fixedData[0] = RELIABLE_DATAGRAM_KEY_BYTE;
		fixedData[1] = RELIABLE_DATAGRAM_KEY_BYTE;
		fixedData[2] = RELIABLE_DATAGRAM_KEY_BYTE;
		fixedData[3] = RELIABLE_DATAGRAM_KEY_BYTE;		
		
		HASH_CODE_CONVERTER.putInt(0 , hash);
		
		fixedData[4] = HASH_CODE_CONVERTER.get(0);
		fixedData[5] = HASH_CODE_CONVERTER.get(1);
		fixedData[6] = HASH_CODE_CONVERTER.get(2);
		fixedData[7] = HASH_CODE_CONVERTER.get(3);
		
		System.arraycopy(data, 0, fixedData, 8, data.length - offset);
		HASH_CODE_CONVERTER.clear();
		packet = new DatagramPacket(fixedData , fixedData.length , addr , port);
		
		this.hash = hash;
		this.cooldownMillis = cooldownMillis;
		timer.start();
		
	}

	/**
	 * Constructs a reliable datagram packet out of an existing DatagramPacket, for use with hashing. 
	 * 
	 * @param source
	 */
	private ReliableDatagram(DatagramPacket source) {

		this.hash = reliableHashFromPacket(source);
		this.cooldownMillis = -1;
		this.packet = source;
		timer.start();
		
	}
	
	public static ReliableDatagram newReliableDatagram(byte[] data , int hash , int cooldownMillis) {
		
		ReliableDatagram reliable = new ReliableDatagram(data , hash , cooldownMillis);
		return reliable;
		
	}

	public static ReliableDatagram newReliableDatagram(byte[] data , int hash , int cooldownMillis , InetAddress addr , int port) {
		
		ReliableDatagram reliable = new ReliableDatagram(data , hash , cooldownMillis , addr , port);		
		return reliable;
		
	}

	public static ReliableDatagram newReliableDatagram(byte[] data , int offset , int hash , int cooldownMillis , InetAddress addr , int port) {
		
		ReliableDatagram reliable = new ReliableDatagram(data , offset , hash , cooldownMillis , addr , port);		
		return reliable;
		
	}
	
	/**
	 * Sends a reliable Datagram Packet.
	 * 
	 * @param sender � UDP Socket sender
	 * @param data � byte array of data
	 * @param hash � unique hash for this reliable datagram, its ID
	 * @param cooldownMillis � how many milliseconds should pass before resending this packet
	 * @throws IOException if {@code sender} throws an error on {@code send}.
	 */
	public static void sendReliable(DatagramSocket sender , byte[] data , int hash , int cooldownMillis) throws IOException {
		
		ReliableDatagram reliable = newReliableDatagram(data , hash , cooldownMillis);
		sender.send(reliable.packet);
		DATAGRAMS.put(reliable.hash , reliable);
		
	}
	
	public static void sendReliable(DatagramSocket sender , byte[] data , int hash , int cooldownMillis , InetAddress addr , int port) throws IOException {
		
		ReliableDatagram reliable = newReliableDatagram(data , hash , cooldownMillis , addr , port);
		sender.send(reliable.packet);
		DATAGRAMS.put(reliable.hash , reliable);
		
	}
	
	public static void sendReliable(DatagramSocket sender , byte[] data , int offset , int hash , int cooldownMillis , InetAddress addr , int port) throws IOException { 
		
		ReliableDatagram reliable = newReliableDatagram(data , offset , hash , cooldownMillis , addr , port);
		sender.send(reliable.packet);
		DATAGRAMS.put(reliable.hash , reliable);
		
	}
	
	public static final byte[] reliableHeaderFromPacket(DatagramPacket packet) {
		
		byte[] data = packet.getData();
		return new byte[] {data[0] , data[1] , data[2] , data[3] , data[4] , data[5] , data[6] , data[7]};
		
	}

	public static final int reliableHashFromPacket(DatagramPacket packet) {
		
		byte[] data = packet.getData();
		HASH_CODE_CONVERTER.put(0 , data[4]);
		HASH_CODE_CONVERTER.put(1 , data[5]);
		HASH_CODE_CONVERTER.put(2 , data[6]);
		HASH_CODE_CONVERTER.put(3 , data[7]);
		return HASH_CODE_CONVERTER.getInt(0);
		
	}
	
	public static final void acknowledge(DatagramSocket sender , DatagramPacket respondingTo) throws IOException {
			
		sender.send(new DatagramPacket(reliableHeaderFromPacket(respondingTo) , 8 , respondingTo.getAddress() , respondingTo.getPort()));
		
	}
	
	public static final boolean isAcknowledgement(DatagramPacket reliable) {
		
		//assuming reliable is a reliable, returns true if reliable's hash equals a hash of a previously sent 

		byte[] data = reliable.getData();
		HASH_CODE_CONVERTER.put(0 , data[4]);
		HASH_CODE_CONVERTER.put(1 , data[5]);
		HASH_CODE_CONVERTER.put(2 , data[6]);
		HASH_CODE_CONVERTER.put(3 , data[7]);
		int prospectiveHash = HASH_CODE_CONVERTER.getInt(0);
		return DATAGRAMS.containsKey(prospectiveHash);
	
	}
	
	/**
	 * Checks all live datagrams and resends any whose cooldown time has elapsed. Also will add any elements previously created into the 
	 * list of all live datagrams.
	 * 
	 * @param sender � DatagramSocket responsible for resending packets
	 * @throws IOException if {@code sender} throws an error during its {@code send} method.
	 */
	public synchronized static void tickLiveDatagrams(DatagramSocket sender) throws IOException {

		if(SECOND_TIMER.getElapsedTimeSecs() >= 1) onSecondUpdate();
		
		Set<Entry<Integer , ReliableDatagram>> live = DATAGRAMS.entrySet();
		ReliableDatagram value;
		for(Entry<Integer , ReliableDatagram> x : live) if((value = x.getValue()).timer.getElapsedTimeMillis() >= value.cooldownMillis) {
				
			sender.send(value.packet);
			value.timer.start();
				
		}			
		
	}
	
	public static boolean isReliableDatagramPacket(DatagramPacket packet) {

		if(packet.getLength() < 8) return false;
		byte[] data = packet.getData();
		HASH_CODE_CONVERTER.put(0 , data[0]);
		HASH_CODE_CONVERTER.put(1 , data[1]);
		HASH_CODE_CONVERTER.put(2 , data[2]);
		HASH_CODE_CONVERTER.put(3 , data[3]);		
		int key = HASH_CODE_CONVERTER.getInt(0);
		return key == RELIABLE_DATAGRAM_KEY;
		
	}
		
	public static void removeLiveDatagram(int hash) {
		
		DATAGRAMS.remove(hash);
		
	}
	
	public static int numberLiveDatagrams() {
		
		return DATAGRAMS.size();
		
	}

	public static final void acceptAcknowledgement(DatagramPacket ack) {

		ReliableDatagram acked = DATAGRAMS.remove(reliableHashFromPacket(ack));
		numberAcksReceivedLastSecond++;

		if(acked != null) { 

			double arrivalTime = System.currentTimeMillis();
			rawRTT += (arrivalTime - acked.creationTime);
			numberAcksReceivedLastSecond++;			
			
		}
		
	}

	public static final DatagramPacket extractPacketDataFromReliable(DatagramPacket reliable) { 
		
		//given a reliable datagram, return a DatagramPacket containing the data from the reliable sans the reliable-specific 
		//data at the beginning of the packet
		byte[] fixedData = new byte[reliable.getLength() - 8];
		System.arraycopy(reliable.getData() , 8, fixedData, 0, reliable.getLength() - 8);
		return new DatagramPacket(fixedData , fixedData.length);
		
	}

	private static void onSecondUpdate() {
		
		numberAcksReceivedLastSecond = 0;
		SECOND_TIMER.start();
		
	}

	/**
	 * Given a hash, tests if a reliable datagram with the matching hash is awaiting an acknowledgement.
	 * 
	 * @param hash � a hash for a reliable datagram
	 * @return true if a reliable datagram was sent with {@code hash} but has not yet been acknowledged.
	 */
	public static final boolean awaitingAcknowledgement(int hash) { 
		
		return DATAGRAMS.containsKey(hash);
		
	}
	
	public static final double computeAverageRTT() {
		
		return rawRTT / numberAcksReceivedLastSecond;
		
	}
	
	public DatagramPacket packet() {
		
		return packet;
		
	}
	
	public int hashCode() {
		
		return hash;
		
	}
	
}