package Networking.Utils;

import java.net.DatagramPacket;
import java.util.Random;

/**
 * This class is used to simulate packet loss. Set a packet loss rate and then pass all received packets to this. The rate will be calculated and the given
 * percent of packets will be discarded in an undefined order. 
 * 
 */
public class PacketLoser {

	//number between 0 and 100 of the number of packets to discard.
	public volatile int rate = -1;
	private Random r = new Random();
	
	public DatagramPacket receive(DatagramPacket possiblyLost) {
		
		int proc = r.nextInt(101);
		
		if(proc < rate) return null;
		return possiblyLost;
		
	}
	
}