package Networking;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import CSUtil.Timer;
import CSUtil.DataStructures.CSStack;
import Core.NKUI;
import Core.Scene;
import Core.TemporalExecutor;
import Game.Levels.Levels;
import Game.Player.PlayerCharacter;
import Networking.Utils.ByteArrayUtils;

/**
 * Class representing when a user creates a multiplayer session that their machine will host. This class will be responsible for sending
 * packets to connected users and will syncing the game. The user who is hosting will have authority over the game state.
 * 
 * @author Chris Brown
 *
 */
public class UserHostedSession implements NetworkedInstance {

	private static final int DEFAULT_PORT = 32900;

	private class UserConnection {

		static int computeHashCode(InetAddress addr , int port) { 
			
			return addr.hashCode() + port;
			
		}
		
		final int port;
		final InetAddress address;
		final Timer timer;
		final NetworkedEntities entity;		
		
		UserConnection(int port , InetAddress address , Timer timer , NetworkedEntities entity) {
			
			this.port = port;
			this.address = address;
			this.timer = timer;
			this.entity = entity;
			timer.start();
			
		}
		
		public int hashCode() {
			
			return computeHashCode(address , port);
			
		}
		
		public String toString() {
			
			return "Connection: " + address.toString() + ":" + port;
			
		}
		
	}	
	
	private PlayerCharacter player;
	private Scene scene;
	private final ConcurrentHashMap<Integer , UserConnection> liveConnections = new ConcurrentHashMap<>();
	private MultiplayerChatUI chatUI;
	private final UserHostedSessionUI UI = new UserHostedSessionUI();
	private final DatagramSocket hostSocket;
	private boolean serverStarted = false;	
	private Thread sessionListeningThread; //daemon thread used by the host socket for listening.
	private final Supplier<Levels> level;
	
	{
	
		sessionListeningThread = new Thread(new Runnable() {

			@Override public void run() {

				while(true) { 
						
					try {
							
						listen();
							
					} catch (IOException e) {
							
						e.printStackTrace();
							
					}
						
				}
					
			}
				
		});
		
		sessionListeningThread.setDaemon(true);
		
	}
		
	public UserHostedSession(Scene scene , Supplier<Levels> getCurrentLevel , int port) {
		
		this.scene = scene;
		this.level = getCurrentLevel;
		
		try {
			
			hostSocket = new DatagramSocket(port , InetAddress.getLocalHost());
			
		} catch (SocketException|UnknownHostException e) {

			e.printStackTrace();
			throw new IllegalStateException("Failed to Start Server");	
			
		} 
		
	}
	
	public UserHostedSession(Scene scene , Supplier<Levels> getCurrentLevel) {
		
		this.scene = scene;
		this.level = getCurrentLevel;
		
		try {
			
			hostSocket = new DatagramSocket(DEFAULT_PORT , InetAddress.getLocalHost());
			
		} catch (SocketException|UnknownHostException e) {

			e.printStackTrace();
			throw new IllegalStateException("Failed to Start Server");	
			
		} 
		
	}
	
	public void startServer(PlayerCharacter player) {
		
		this.player = player;
		
		sessionListeningThread.start();
		serverStarted = true;

		chatUI = new MultiplayerChatUI(player.playerCharacterName() , this);
		System.out.println("Successfully Started User Hosted Server.");
		
	}
	
	public void toggleUserHostedSessionUI() {
		
		UI.show = UI.show ? false : true;
		
	}

	/**
	 * Intended for sending some data to all clients connected to the server <b>unreliably</b>.
	 * 
	 * @param data — bytes in the subsequent datagram packet
	 */
	public void broadcast(byte[] data) {
		
		try { 

			Collection<UserConnection> connections = liveConnections.values();
			for(UserConnection x :  connections) hostSocket.send(new DatagramPacket(data , data.length , x.address , x.port));
			
		} catch(IOException e) { 
			
			e.printStackTrace();
			
		}		
				
	}
	
	/**
	 * Sends a reliable datagram to all connected clients. {@code hash} should be a starting hash. For each connection, the sent datagram's hash
	 * will be {@code hash} + i, where i is the current iteration.
	 * 
	 * @param data
	 * @param hash
	 * @param cooldownMillis
	 */
	public void broadcastReliable(byte[] data , int hash , int cooldownMillis) {
		
		try { 
			
			var connections = liveConnections.values();
			int i = 0;
			for(UserConnection x : connections) {
				
				ReliableDatagram.sendReliable(hostSocket, data, hash + i, cooldownMillis , x.address , x.port);
				i++;
				
			}
			
		} catch(IOException e) { 
			
			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * Handles unique packet flags as specified by {@code NetworkedInstance}. 
	 * 
	 * @param sender — Socket to use to send a response message if necessary
	 * @param packetData — data this method should use during its operations
	 * @param offset — offset into {@code packet}'s data to look for a potential flag 
	 */
	private void handleFlaggedMessage(DatagramPacket packet , int offset) { 
		
		switch(packet.getData()[offset]) { 
			
			case CHANGE_CONNECTION -> {
				
				// this message was sent from someone who was already connected, so disconnect that user
				if(liveConnections.contains(packet.getPort())) { 
					
					UserConnection disconnected = liveConnections.remove(packet.getPort()); 
					scene.entities().remove(disconnected.entity);
					
				} else { //connect that user 

					//parse the connecting users info (what entity they are and what macrolevel/level they are
					String clientPacketData = new String(packet.getData() , offset + 1, packet.getLength() - (offset + 1));
					String[] clientNamePositionAndLocation = clientPacketData.split("\\|");	clientNamePositionAndLocation[0] += ".CStf";			
					
					float[] midpoint = player.playersEntity().getMidpoint();
					//this string represents other players connected to the server, their positions, and their locations
					String otherPlayersEntityAndLocation = player.playersEntity().name() + "," + midpoint[0] + "," + midpoint[1] + "," + level.get().macroLevel() + "/" + level.get().gameName();
					//send this to players who are already connected
					byte[] messageToOtherPlayers = ByteArrayUtils.prepend(clientPacketData.getBytes() , CLIENT_CONNECTED);					
					
					try { 
						
						for(Entry<Integer , UserConnection> x : liveConnections.entrySet()) { 
							
							midpoint = x.getValue().entity.getMidpoint();							
							//append this entry's information to be sent to the new client
							otherPlayersEntityAndLocation += "|" + x.getValue().entity.name() + "," + midpoint[0] + "," + midpoint[1] + "," + x.getValue().entity.location();
							//here we update all already-connected clients that a new one has connected using flag NEW_CLIENT_CONNECTED
							sendReliable(messageToOtherPlayers , x.getKey() , 1500 , x.getValue().address , x.getValue().port);
							
						}

						//create a new instance of a user connection.
						NetworkedEntities newClientEntity = new NetworkedEntities(clientNamePositionAndLocation[0] , clientNamePositionAndLocation[2]);
						//here we pull the position the client sent out of its packet and move the created entity to that position.
						String[] connectingClientPosition = clientNamePositionAndLocation[1].split(",");								
						newClientEntity.moveTo(Float.parseFloat(connectingClientPosition[0]) , Float.parseFloat(connectingClientPosition[1]));
						//add the new entity to the scene and add this connection to our list of live connections
						scene.entities().add(newClientEntity);
						UserConnection newClient = new UserConnection(packet.getPort() , packet.getAddress() , new Timer() , newClientEntity);
						liveConnections.put(newClient.hashCode() , newClient);
						byte[] connectionMessage = ByteArrayUtils.prepend(otherPlayersEntityAndLocation.getBytes() , CHANGE_CONNECTION); 
						sendReliable(connectionMessage , CHANGE_CONNECTION + liveConnections.size() , 1500 , packet.getAddress() , packet.getPort());
						
					} catch(IOException e) {
						
						e.printStackTrace();
						
					}

				}
				
			}
			
			case CLIENT_DISCONNECTED -> {}
			case CLIENT_CONNECTED -> {}
			case CHAT_MESSAGE -> {
				
				System.out.println("received chat message");
				broadcastReliable(packet.getData() , offset , 1400);
				chatUI.appendChatMessage(new String(ByteArrayUtils.fromOffset(packet.getData(), offset + 1)));//offset + 1 to avoid the actual flag byte
				
			}
			
			case LOCATION_CHANGE -> {}
			case PLAYER_MANIFEST -> {}
		
		}
		
	}

	/**
	 * Removes any connections for which no message has been received in 30 seconds.
	 * 
	 */
	public void maintainConnections() {
		
		var connections = liveConnections.values();
		CSStack<Integer> timedOut = new CSStack<>();
		for(UserConnection x : connections) if(x.timer.getElapsedTimeMillis() >= 30000) timedOut.push(x.hashCode());
		while(!timedOut.empty()) liveConnections.remove(timedOut.pop());		
		
	}

	@Override public void update() {
		
		UI.layout();
		chatUI.layout();
//		maintainConnections();	
		try {
			
			ReliableDatagram.tickLiveDatagrams(hostSocket);
				
		} catch (IOException e) {
				
//			e.printStackTrace();
				
		}
			
		
	}
	
	@Override public void send(byte[] data) throws IOException {
		
		hostSocket.send(new DatagramPacket(data , data.length));
	
	}

	@Override public void sendReliable(byte[] data , int hash , int cooldownMillis) throws IOException {
		
		ReliableDatagram.sendReliable(hostSocket, data, hash, cooldownMillis);
		
	}

	@Override public void sendReliable(byte[] data , int hash , int cooldownMillis , InetAddress addr , int port) throws IOException {
		
		ReliableDatagram.sendReliable(hostSocket, data, hash, cooldownMillis , addr , port);
		
	}
	
	/**
	 * This happens in a dedicated thread. Any additional operations such has handling flags needs to be done elsewhere.
	 * 
	 */
	@Override public DatagramPacket listen() throws IOException {
		
		DatagramPacket received = new DatagramPacket(new byte[256] , 256);
		hostSocket.receive(received);
		
		UserConnection sender;
		if((sender = liveConnections.get(UserConnection.computeHashCode(received.getAddress() , received.getPort()))) != null) sender.timer.start();
		
		if(ReliableDatagram.isReliableDatagramPacket(received)) {
			
			if(ReliableDatagram.isAcknowledgement(received)) ReliableDatagram.acceptAcknowledgement(received);
			else ReliableDatagram.acknowledge(hostSocket, received);
			
			if(NetworkedInstance.isFlaggedMessage(received.getData() , 8)) { 
				
				// Offload handling of flags to another thread if possible, but if server is not started yet, handle them in this thread
				// because TemporalExecutor is not running at that time.
				if(serverStarted) TemporalExecutor.onTrue(() -> true, () -> handleFlaggedMessage(received , 8));
				else handleFlaggedMessage(received , 8);
				
			}
			
			return received;
			
		} else {
			
			if(NetworkedInstance.isFlaggedMessage(received.getData() , 0)) { 
				
				if(serverStarted) TemporalExecutor.onTrue(() -> true, () -> handleFlaggedMessage(received , 0));
				else handleFlaggedMessage(received , 8);
				
			}
			
			return received;
			
		}
		
	}
	
	@Override public boolean host() {
		
		return true;
		
	}

	@Override public boolean client() {
		
		return false;
		
	}
	
	@Override public void toggleUI() {
		
		UI.toggle();
		chatUI.toggle();
		
	}
	
	@Override public void shutDown() {
		
		try {
		
			sessionListeningThread.interrupt();
			hostSocket.setSoTimeout(1);
			
		} catch (SocketException e) {

			e.printStackTrace();

		} finally { 
			
			chatUI.shutDown();
			
		}
		
	}
	
	/**
	 * Representation of a Multiplayer UI. Shows connection info and stats.
	 */
	private class UserHostedSessionUI implements NKUI {
				
		//toggle this with f11
		boolean show = false;
		
		public void layout() {
			
			if(!show) return;
			
			try(MemoryStack stack = allocator.push()){
				
				NkRect rect = NkRect.malloc(stack).set(1210 , 5 , 350 , 600);
				if(nk_begin(context , "Multiplayer Information" , rect , NK_WINDOW_BORDER|NK_WINDOW_TITLE)) {
				
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Server Address:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					nk_text(context , hostSocket.getLocalAddress().toString() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);

					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Server Port:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					nk_text(context , "" + hostSocket.getLocalPort() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);					
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Number Connections" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					nk_text(context , "" + liveConnections.size() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);

					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Average RTT (1 second)" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					nk_text(context , "" + ReliableDatagram.computeAverageRTT() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
					
					var set = liveConnections.entrySet();
					for(Entry<Integer , UserConnection> x : set) { 
						
						nk_layout_row_dynamic(context , 20 , 2);
						nk_text(context , "IP Address:Port" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
						nk_text(context , x.getValue().toString() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
						
					}
					
				}
				
				nk_end(context);
				
			}
	
		}
		
		public void toggle() {
			
			show = show ? false : true;
			
		}
		
	}

}