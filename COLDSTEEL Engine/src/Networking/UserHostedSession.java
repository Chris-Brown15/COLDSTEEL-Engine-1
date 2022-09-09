package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import CSUtil.DataStructures.CSOHashMap;

/**
 * Class representing when a user creates a multiplayer session that their machine will host. This class will be responsible for sending
 * packets to connected users and will syncing the game. The user who is hosting will have authority over the game state.
 * 
 * @author Chris Brown
 *
 */
public class UserHostedSession {

	private static final int DEFAULT_PORT = 32900;
	
	//daemon thread used by the host socket for listening.
	private Thread sessionListeningThread = new Thread(new Runnable() {

		@Override public void run() {

			while(true) { 
				
				try {
					
					serverListen();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
		}
		
	});
	
	//daemon thread used by the reliable datagram system for its ticking operation
	private Thread sessionReliableTick = new Thread(new Runnable() {

		@Override public void run() {

			while(true) {
				
				try {
					
					ReliableDatagram.tickLiveDatagrams(hostSocket);
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
		}
		
	});
	
	private record UserConnection(int port , InetAddress address) {}	
	private CSOHashMap<Integer , UserConnection> connections = new CSOHashMap<Integer , UserConnection>(16);
	private final DatagramSocket hostSocket;
	private boolean serverStarted = false;
	
	{
	
		sessionListeningThread.setDaemon(true);
		sessionReliableTick.setDaemon(true);
		
	}
	
	public UserHostedSession(int port) {
		
		try {
			
			hostSocket = new DatagramSocket(port , InetAddress.getLocalHost());
			
		} catch (SocketException|UnknownHostException e) {

			e.printStackTrace();
			throw new IllegalStateException("Failed to Start Server");	
			
		} 
		
	}
	
	public UserHostedSession() {

		try {
			
			hostSocket = new DatagramSocket(DEFAULT_PORT , InetAddress.getLocalHost());
			
		} catch (SocketException|UnknownHostException e) {

			e.printStackTrace();
			throw new IllegalStateException("Failed to Start Server");	
			
		} 
		
	}
	
	public void startServer() {
		
		sessionListeningThread.start();
		sessionReliableTick.start();
		serverStarted = true;
		
	}
	
	private void serverListen() throws IOException {
		
		DatagramPacket recipientPacket = new DatagramPacket(null , 0);
		hostSocket.receive(recipientPacket);
		
		if(ReliableDatagram.isReliableDatagramPacket(recipientPacket)) {
			
			
			
		} else {
			
			
			
		}
		
	}
	
}