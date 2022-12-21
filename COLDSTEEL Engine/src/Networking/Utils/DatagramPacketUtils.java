package Networking.Utils;

import java.net.DatagramPacket;

import CSUtil.DataStructures.RingBuffer;

import static Networking.ReliableDatagram.*;
import static Networking.Utils.NetworkingConstants.*;
import static Networking.Utils.PacketCoder.*;

public final class DatagramPacketUtils {

	/**
	 * {@code checksum} is to be used to determine whether {@code packet} has already been received. Of course, in UDP, its possible to receive
	 * the same exact message multiple times. The bulk of a packet may be identical to previous ones so how to produce this checksum depends on some 
	 * other factors such as how this packet is flagged.
	 * 
	 * @param packet — a packet to get the checksum for
	 * @return — result of the computing of the checksum
	 */
	public static long checksum(final DatagramPacket packet) {
		
		byte flag = flag(packet);
		
		switch(flag) {
		
			case -1: return reliableHashFromPacket(packet);
		
			case CHAT_MESSAGE: case CHANGE_CONNECTION: case LOCATION_CHANGE_IN: case LOCATION_CHANGE_OUT:

				long checksum = 0;
				for(int i = 1 ; i < packet.getLength() ; i ++) checksum += packet.getData()[i];
				return (checksum + (flag << 10));
			
			case UPDATE:
				
				//this is because update packets are formatted differently if they are sent to the server or the client, which means we cannot
				//have a static place in the packet to look for the update sequence.
				for(int i = 0 ; i < packet.getLength() ; i ++) if(packet.getData()[i] == UPDATE_SEQUENCE) return packet.getData()[i + 1];
				throw new IllegalStateException("Failed to find update sequence from packet for checksum");
				
			case PRE_CONNECT:
				return flag;
				
		}
		
		throw new IllegalStateException("Failed to construct checksum for packet flagged as " + flag);
		
	}
	
	/**
	 * This is to help mitigating previously received packets. If the received packet has an identical checksum with some other previously packet's
	 * checksum, it is considered duplicate and ignored.
	 * 
	 * @param checksum — checksum of a packet
	 * @param previouslyReceivedPacketChecksums — RingBuffer of checksums to compare {@code checksum} against
	 * @return — true if the checksum was found, false otherwise
	 */
	public static boolean handleChecksum(long checksum , RingBuffer<Long> previouslyReceivedPacketChecksums) {
				
		boolean has = previouslyReceivedPacketChecksums.has(checksum);
		previouslyReceivedPacketChecksums.put(checksum);		
		return has;
		
	}
	
	/**
	 * Gets the flag of the packet from the packet. If this packet is an acknowledgement, the returned value is -1, else it is a packet flag.
	 * 
	 * @param packet — packet to get the flag of
	 * @return — which packet flag.
	 */
	public static byte flag(final DatagramPacket packet) {
		
		if(isAcknowledgement(packet)) return -1;
		if(isReliableDatagramPacket(packet)) return packet.getData()[8]; //offset because of the meta of a reliable datagram
		return packet.getData()[0];
		
	}

	public static String flagAsString(DatagramPacket packet) {
		
		if(isAcknowledgement(packet)) return "Ack";
		int off = isReliableDatagramPacket(packet) ? 8 : 0;
		return switch(packet.getData()[off]) {
		
			case CHANGE_CONNECTION -> "Change Connection";
			case CLIENT_CONNECTED -> "Client Connected";
			case CLIENT_DISCONNECTED -> "Client Disconnected";
			case CHAT_MESSAGE -> "Chat Message";
			case LOCATION_CHANGE_IN -> "Location Changed Into";
			case LOCATION_CHANGE_OUT -> "Location Changed Out Of";
			case LEVEL_LOAD_INFO -> "Level Load Info";
			case PRE_CONNECT -> "Preconnecting";
			case RESYNC -> "Resync";
			case UPDATE -> "Update";
			default -> throw new IllegalArgumentException(packet.getData()[off] +  " is not a valid packet flag.") ;
		
		};
		
	}
	
}
