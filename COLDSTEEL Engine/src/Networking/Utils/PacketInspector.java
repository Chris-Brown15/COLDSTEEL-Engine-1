package Networking.Utils;

import java.net.DatagramPacket;
import static Networking.ReliableDatagram.isReliableDatagramPacket;
import static Networking.Utils.NetworkingConstants.*;


/**
 * Utility class for viewing the contents of a packet in engrish.
 * 
 * @author Chris Brown
 *
 */
public final class PacketInspector {

	private PacketInspector() {}
	
	public static boolean isReliable(final DatagramPacket packet) {
	
		return isReliableDatagramPacket(packet);
		
	}	
	
	public static String flag(final DatagramPacket packet) {
		
		return switch(DatagramPacketUtils.flag(packet)) {
		
			case CHANGE_CONNECTION -> "Change Connection Flag";
			case CLIENT_CONNECTED -> "Client Connected Flag";
			case CLIENT_DISCONNECTED -> "Client Disconnected Flag";
			case CHAT_MESSAGE -> "Chat Message Flag";			
			case LOCATION_CHANGE_IN -> "Location Change In Flag";
			case LOCATION_CHANGE_OUT -> "Location Change Out Flag";
			case LEVEL_LOAD_INFO -> "Level Load Info Flag";
			case UPDATE -> "Update Flag";
			case -1 -> "Acknowledgement Message";
			default -> throw new IllegalArgumentException(DatagramPacketUtils.flag(packet) + " is not a valid packet flag");
		
		};
		
	}
	
}