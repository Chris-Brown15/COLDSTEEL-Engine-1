package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import CSUtil.Timer;
import Core.TemporalExecutor;
import Game.Core.GameRuntime;
import Game.Core.GameState;
import Game.Player.PlayerCharacter;
import Networking.Utils.ByteArrayUtils;

/**
 * Representation of a user's client connection to another user's UserHostedSession server. This class facilitates communication between users.
 * <br>
 * The host of a session will not have an instance of this class. Users who join a UserHostedSession will create this class. For each user, 
 * there should only be one instance of this class. Hence, this class will be responsible for handling ReliableDatagram processing. 
 * 
 * @author Chris Brown
 *
 */
public class UserHostedSessionClient implements NetworkedInstance {

	private MultiplayerChatUI chatUI;
	private final DatagramSocket clientSocket;
	//thread responsible for recieving messages
	private final Thread clientListeningThread;
	//thread responsible for resending messages 
	private final Thread reliableTickingThread;
	private volatile boolean connected = false;	
	private PlayerCharacter player;
	private Timer timeoutTimer = new Timer();
	
	public UserHostedSessionClient(String connectionInfo) throws IOException {
	
		String[] split = connectionInfo.split(":");
				
		clientSocket = new DatagramSocket();
		clientSocket.connect(InetAddress.getByName(split[0]) , Integer.parseInt(split[1]));
		
		clientListeningThread = new Thread(new Runnable() {

			@Override public void run() {

				while (true) { 
					
					try {
						
						listen();
						
					} catch (IOException e) {
						
						e.printStackTrace();
						
					}
					
				}
								
			}
			
		});
		
		clientListeningThread.setDaemon(true);
		
		reliableTickingThread = new Thread(new Runnable() {

			@Override public void run() {

				while(true) {
					
					try {
						
						ReliableDatagram.tickLiveDatagrams(clientSocket);
						
					} catch (IOException e) {
						
						e.printStackTrace();
						
					}
					
				}
				
			}
			
		});		
		
		reliableTickingThread.setDaemon(true);
				
	}
	
	/**
	 * Attempts to connect to the address given at construction, returning the user to the main menu and calling {@code fadeInFunction} if a failure
	 * ocurrs. 
	 * 
	 * @param fadeInFunction — some code which should call the engine fade in method.
	 */
	public void connectAndStart(PlayerCharacter player) {

		this.player = player;
		
		try { 
			
			clientSocket.setSoTimeout(30000);//try to connect for 30 seconds, returning to the main menu otherwise.
			//establish connection here		
			ReliableDatagram.sendReliable(clientSocket , new byte [] {CHANGE_CONNECTION} , CHANGE_CONNECTION , 1200);
			
			/*
			 * Here, we start listening for a response from the server. If 30 seconds elapse and we recieve no message return to the main menu
			 * and TODO: display an error message. 
			 */			
			reliableTickingThread.start();			
			clientListeningThread.start();
			timeoutTimer.start();
			
			//block and wait for a connection message
			while(timeoutTimer.getElapsedTimeMillis() < 30000 && !connected) {}
			
			if(timeoutTimer.getElapsedTimeMillis() >= 30000) throw new SocketTimeoutException();//failed to connect
			else if (connected) { //connected
				
				System.out.println("connected!");
				//state gets set out of this method
				chatUI = new MultiplayerChatUI(player.playerCharacterName() , this);
				timeoutTimer.start();
				
			}
			
		} catch(IOException e) { 
			
			System.err.println("Failed to connect to server.\n");
			e.printStackTrace();
			reliableTickingThread.interrupt();
			clientListeningThread.interrupt();
			GameRuntime.setState(GameState.MAIN_MENU);
			
		}
		
	}
	
	private void handleFlags(byte[] data , int offset) {
		
		switch(data[offset]) { 
		
			case CHANGE_CONNECTION -> { 
				
				connected = connected ? false:true;
				System.out.println("changing connection state to: " + connected);
				
			}
			
			case CONNECTION_STATE_CHANGED -> {}
			case CHAT_MESSAGE -> chatUI.appendChatMessage(new String(ByteArrayUtils.fromOffset(data, offset + 1)));//offset + 1 to avoid the flag byte
		
		}
		
	}

	@Override public void update() {
		
		chatUI.layout();
		if(timeoutTimer.getElapsedTimeSecs() > 30) System.err.println("TIMED OUT");
		
	}
	
	@Override public void send(byte[] data) throws IOException {
	
		clientSocket.send(new DatagramPacket(data , data.length));
		
	}

	@Override public void sendReliable(byte[] data , int hash , int cooldownMillis) throws IOException {
	
		ReliableDatagram.sendReliable(clientSocket, data, hash, cooldownMillis);
		
	}
	
	@Override public void sendReliable(byte[] data , int hash , int cooldownMillis , InetAddress addr , int port) throws IOException {
		
		throw new UnsupportedOperationException("As of now, a client may not send a packet to any destination other than the server.");
		
	}

	@Override public DatagramPacket listen() throws IOException {
		
		DatagramPacket received = new DatagramPacket(new byte[256] , 256);
		clientSocket.receive(received);
		
		if(ReliableDatagram.isReliableDatagramPacket(received)) {
		
			if(ReliableDatagram.isAcknowledgement(received)) ReliableDatagram.acceptAcknowledgement(received);
			else ReliableDatagram.acknowledge(clientSocket, received);
			
			if(NetworkedInstance.isFlaggedMessage(received.getData() , 8)) { 
				// Offload handling of flags to another thread if possible, but if we are not connected yet, handle them in this thread
				// because TemporalExecutor is not running at that time.
				if (connected) TemporalExecutor.onTrue(() -> true, () -> handleFlags(received.getData() , 8));
				else handleFlags(received.getData() , 8);
				
			}
			
		} else {
			
			if(NetworkedInstance.isFlaggedMessage(received.getData() , 0)) { 
				
				if(connected) TemporalExecutor.onTrue(() -> true, () -> handleFlags(received.getData() , 0));
				else handleFlags(received.getData() , 0);
				 				
			}
			
		}
		
		return received;
		
	}

	@Override public boolean host() {
		
		return false;
		
	}

	@Override public boolean client() {
		
		return true;
		
	}

	@Override public void toggleUI() {
		
		chatUI.toggle();
		
	}
	
	@Override public void shutDown() {
		
		try {
			
			clientSocket.setSoTimeout(1);
			
		} catch (SocketException e) {

			e.printStackTrace();
			
		} finally { 
			
			if(chatUI != null) chatUI.shutDown();
			
		}
		
	}	
	
}
