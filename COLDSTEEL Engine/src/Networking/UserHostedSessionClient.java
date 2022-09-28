package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import CSUtil.Timer;
import Core.Scene;
import Core.TemporalExecutor;
import Game.Core.GameRuntime;
import Game.Core.GameState;
import Game.Levels.Levels;
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

	/**
	 * list of key codes to query from GLFW and send to servers. 
	 * 
	 */	
	private final CopyOnWriteArrayList<NetworkedEntities> otherPlayers = new CopyOnWriteArrayList<>(); 
	private int[] networkedKeys;
	private MultiplayerChatUI chatUI;
	private Scene scene;
	private final DatagramSocket clientSocket;	
	private final Thread clientListeningThread; //thread responsible for recieving messages
	private volatile boolean connected = false;	
	private PlayerCharacter player;
	private Timer timeoutTimer = new Timer();
	private final Supplier<Levels> currentLevel;
	
	public UserHostedSessionClient(Scene scene , Supplier<Levels> getCurrentLevel , String connectionInfo) throws IOException {
		
		this.currentLevel = getCurrentLevel;
		this.scene = scene;
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

			Levels level = currentLevel.get();
			String levelAsString = level.macroLevel() + "/" + level.gameName() + ".CStf";
			float[] mid = player.playersEntity().getMidpoint();
			String messageString = player.playersEntity().name() + "|"  + mid[0] + "," + mid[1]  + "|" + levelAsString;	
						
			sendReliable(ByteArrayUtils.prepend(messageString.getBytes() , CHANGE_CONNECTION) , CHANGE_CONNECTION , 1200);
			
			/*
			 * Here, we start listening for a response from the server. If 30 seconds elapse and we recieve no message return to the main menu
			 * and TODO: display an error message. 
			 */				
			clientListeningThread.start();
			timeoutTimer.start();
			
			//block and wait for a connection message
			while(timeoutTimer.getElapsedTimeSecs() < 30 && !connected);
			
			if(timeoutTimer.getElapsedTimeSecs() >= 30) throw new SocketTimeoutException();//failed to connect
			else if (connected) { //connected
				
				System.out.println("Connected!");
				chatUI = new MultiplayerChatUI(player.playerCharacterName() , this);
				timeoutTimer.start();				
				
			}
			
		} catch(IOException e) {
			
			System.err.println("Failed to connect to server.\n");
			e.printStackTrace();
			clientListeningThread.interrupt();
			GameRuntime.setState(GameState.MAIN_MENU);
			
		}
		
	}
	
	private void handleFlags(DatagramPacket packet , int offset) {
		
		switch(packet.getData()[offset]) { 
		
			case CHANGE_CONNECTION -> { 

				connected = connected ? false:true;
				
				//we have to offload this code to the main thread because OpenGL is only available there. what this means is that for frame
				//0 of the connection we still won't know about other players and on frame 1 we will drop frames as we load them all.
				TemporalExecutor.onTrue(() -> true, () -> {

					//this string is laid out as entityname,posX,posY,macrolevel/level|entityname,posX,posY,macrolevel/level...
					String otherClientsNameAndLocation = new String(packet.getData() , offset + 1 , packet.getLength() - (offset + 1));
					String[] otherClientsSplit = otherClientsNameAndLocation.split("\\|") , namePositionAndLocation;
					for(String x : otherClientsSplit) {
						
						namePositionAndLocation = x.split(",");
						NetworkedEntities newNetworkedEntity = new NetworkedEntities(namePositionAndLocation[0] + ".CStf" , namePositionAndLocation[3]);
						newNetworkedEntity.moveTo(Float.parseFloat(namePositionAndLocation[1]) , Float.parseFloat(namePositionAndLocation[2]));
						otherPlayers.add(newNetworkedEntity);
						scene.entities().add(newNetworkedEntity);
						
					}
						
				});
				
			}
			
			case CLIENT_CONNECTED -> {
			
				//notifies that a new player has connected to the server
				//entity name, position, and location separated by "|"
				String newClient = new String(packet.getData() , offset  + 1 , packet.getLength() - (offset + 1));
				String[] split = newClient.split("\\|");
				NetworkedEntities newClientEntity = new NetworkedEntities(split[0] + ".CStf" , split[2]);
				String[] position = split[1].split(",");
				newClientEntity.moveTo(Float.parseFloat(position[0]) , Float.parseFloat(position[1]));
				otherPlayers.add(newClientEntity);
				scene.entities().add(newClientEntity);				
				
			}
			
			case CLIENT_DISCONNECTED -> {}
			case CHAT_MESSAGE -> chatUI.appendChatMessage(new String(packet.getData() , offset + 1 , packet.getLength() - (offset + 1)));//offset + 1 to avoid the flag byte
			case LOCATION_CHANGE -> {}
			case PLAYER_MANIFEST -> {}
			
			
		}
		
	}

	public void setNetworkedKeys(int...keycodes) { 
		
		this.networkedKeys = keycodes;
		
	}
	
	@Override public void update() {
		
		chatUI.layout();
//		if(timeoutTimer.getElapsedTimeSecs() > 30) System.err.println("TIMED OUT");

		try {
			
			ReliableDatagram.tickLiveDatagrams(clientSocket);
			
		} catch (IOException e) {
			
//			e.printStackTrace();
			
		}
		
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
				if (connected) TemporalExecutor.onTrue(() -> true, () -> handleFlags(received , 8));
				else handleFlags(received , 8);
				
			}
			
		} else {
			
			if(NetworkedInstance.isFlaggedMessage(received.getData() , 0)) { 
				
				if(connected) TemporalExecutor.onTrue(() -> true, () -> handleFlags(received , 0));
				else handleFlags(received , 0);
				 				
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
