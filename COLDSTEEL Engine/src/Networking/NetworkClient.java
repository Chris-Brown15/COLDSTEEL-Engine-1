package Networking;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_DOWN;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_label;

import static CSUtil.BigMixin.toByte;
import static Networking.Utils.NetworkingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import CS.UserInterface;
import CSUtil.Timer;
import CSUtil.DataStructures.CSQueue;
import Core.Scene;
import Core.TemporalExecutor;
import Core.Entities.Entities;
import Game.Core.GameRuntime;
import Game.Core.GameState;
import Game.Levels.Levels;
import Game.Player.PlayerCharacter;
import Networking.Utils.PacketCoder;

/**
 * Representation of a user's client connection to another user's UserHostedSession server. This class facilitates communication between users.
 * <br>
 * The host of a session will not have an instance of this class. Users who join a UserHostedSession will create this class. For each user, 
 * there should only be one instance of this class. Hence, this class will be responsible for handling ReliableDatagram processing. 
 * 
 * @author Chris Brown
 *
 */
public class NetworkClient implements NetworkedInstance {

	/**
	 * list of key codes to query from GLFW and send to servers. 
	 * 
	 */	
	private final CopyOnWriteArrayList<NetworkedEntities> otherClients = new CopyOnWriteArrayList<>();
	private final Levels level;
	private ClientDebugUI debugUI = new ClientDebugUI();
	private MultiplayerChatUI chatUI;
	private GameRuntime gameRuntime;
	private Scene scene;
	private final DatagramSocket clientSocket;	
	private final Thread clientListeningThread; //thread responsible for recieving messages
	private volatile boolean connected = false;	
	private NetworkedEntities networkedPlayer;
	private Timer timeoutTimer = new Timer();
	private short connectionID = -1;
	
	public NetworkClient(GameRuntime gameRuntime , Levels currentLevel) throws IOException {
		
		this.level = currentLevel;
		this.gameRuntime = gameRuntime;
		this.scene = gameRuntime.gameScene();
				
		clientSocket = new DatagramSocket();
		
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
	public void connectAndStart(PlayerCharacter player , String connectionInfo) {

		networkedPlayer = new NetworkedEntities((short) -1 , player.playersEntity() , true);
		String[] split = connectionInfo.split(":");
		
		try {
			
			clientSocket.connect(InetAddress.getByName(split[0]) , Integer.parseInt(split[1]));
			
		} catch(IOException e) {
			
			e.printStackTrace();
			System.exit(-1);
			
		}
		
		try { 
			
			clientSocket.setSoTimeout(30000);//try to connect for 30 seconds, returning to the main menu otherwise.

			String levelAsString = level.macroLevel() + "/" + level.gameName() + ".CStf";
				
			try(PacketCoder coder = new PacketCoder()){
				
				coder
					.bflag(CHANGE_CONNECTION)
					.bstring(player.playersEntity().name())
					.bposition(player.playersEntity().getMidpoint())
					.bstring(levelAsString)
				;
				
				//message containing this client's name, position and level 
				sendReliable(coder.get() , CHANGE_CONNECTION , 1200 );
			
			}
			
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
				
				chatUI = new MultiplayerChatUI(player.playerCharacterName() , this);
				timeoutTimer.start();				
				scene.entityScriptingInterface().client(this);
				System.out.println("Successfully connected to server");

			}
			
		} catch(IOException e) {
			
			System.err.println("Failed to connect to server.\n");
			e.printStackTrace();
			clientListeningThread.interrupt();
			gameRuntime.setState(GameState.MAIN_MENU);
			
		}
		
	}
	
	private void handleFlags(DatagramPacket packet , int offset) {
		
		switch(packet.getData()[offset]) { 
		
			case CHANGE_CONNECTION -> { 
				
				connected = connected ? false:true;
				
				if(!connected) System.exit(-1); //TODO actual disconect code
				
				scene.entities().setNetworkInstance(this);
				//we have to offload this code to the main thread because OpenGL is only available there. what this means is that for frame
				//0 of the connection we still won't know about other players and on frame 1 we will drop frames as we load them all.
				TemporalExecutor.onTrue(() -> true, () -> {

					try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)) {
						
						connectionID = coder.rconnectionID();
						networkedPlayer.connectionIndex(connectionID);
						//contains all other players in the server's ID and entity name
						CSQueue<Object> othersData = coder.rRepititions(PacketCoder.CONNECTION_ID , PacketCoder.STRING);
						//dequeues other player's data and stores it 
						while(!othersData.empty()) {
							
							short othersID = (short) othersData.dequeue();
							String entName = (String) othersData.dequeue();
							Entities otherClient = new Entities(scene , entName + ".CStf");
							NetworkedEntities newNetworked = new NetworkedEntities(othersID , otherClient , false);
							otherClients.add(newNetworked);
							newNetworked.setNetworkControlled();						
							
						}
						
						//if the coder has more data it will be a repititon
						if(coder.testFor(PacketCoder.REPITITION)) {
							
							CSQueue<Object> othersInLevelData = coder.rRepititions(PacketCoder.CONNECTION_ID , PacketCoder.POSITION);
							while(!othersInLevelData.empty()) {
								
								short othersID = (short) othersInLevelData.dequeue();
								float[] othersPos = (float[]) othersInLevelData.dequeue();
								otherClients.forEach(otherClient -> {
									
									if(otherClient.connectionIndex() == othersID) {
										
										otherClient.networked().moveTo(othersPos[0] , othersPos[1]);
										scene.entities().add(otherClient.networked());
										
									}
									
								});
								
							}
							
						}
						
					}
										
				});
				
			}
			
			case CLIENT_CONNECTED -> {
			
				System.out.println("received client connected packet");

				/*
				 * This flag notates that a new client has joined the server. The new client may or may not be in this client's level,
				 * and the message will contain data on that if they are 
				 * 
				 */
				
				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)){
					
					short newClientsID = coder.rconnectionID();
					String newClientsName = coder.rstring();
					
					NetworkedEntities newClientsEntity = new NetworkedEntities(newClientsID , new Entities(scene , newClientsName) , false);
					newClientsEntity.setNetworkControlled();
					
					if(coder.testFor(PacketCoder.POSITION)) {
						
						float[] position = coder.rposition();
						newClientsEntity.networked().moveTo(position[0] , position[1]);
						scene.entities().add(newClientsEntity.networked());
						
					}
					
					otherClients.add(newClientsEntity);
					System.out.println("New client connected as: " + newClientsEntity.networked().name() + " with ID: " + newClientsEntity.connectionIndex());
					
				}
				
			}
			
			case CLIENT_DISCONNECTED -> {}
			case CHAT_MESSAGE -> {
			
				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)){
					
					String receivedMessage = coder.rstring();
					chatUI.appendChatMessage(receivedMessage);
					
				}
				
			}
			
			case LOCATION_CHANGE_OUT -> {
								
				//a player in this client's level has left it. Remove them from the scene in this case
				
				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)) {
					
					short ID = coder.rconnectionID();
					NetworkedEntities out;
					Iterator<NetworkedEntities> connections = managedConnections().iterator();
					
					while(connections.hasNext()) {								
						
						out = connections.next();					
						if(out.connectionIndex() == ID) {
							
							scene.entities().removeStraight(out.networked());
							break;
							
						}
						
					}
					
				}
				
			}

			case LOCATION_CHANGE_IN -> {
				
				//someone else changed level, either out of or into my level
				
				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)) {
					
					short ID = coder.rconnectionID();
					float[] pos = coder.rposition();
					NetworkedEntities e;
					
					Iterator<NetworkedEntities> connections = managedConnections().iterator();
					while(connections.hasNext()) {
						
						e = connections.next();
						if(e.connectionIndex() == ID) {
							
							scene.entities().add(e.networked());
							e.networked().moveTo(pos[0] , pos[1]);
							break;
							
						}
						
					}
					
				}				
				
			}
							
			case UPDATE -> {
				
				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)) {
				
					short senderID = coder.rconnectionID();
					byte[] controlStrokes = coder.rControlStrokes();
					
					Iterator<NetworkedEntities> iter = otherClients.iterator();
					NetworkedEntities e;
					
					while(iter.hasNext()) {
						
						e = iter.next();
						if(e.connectionIndex() == senderID) {
							
							e.controlsState(controlStrokes);
							break;
							
						}
						
					}
					
				}
					
			}

			case LEVEL_LOAD_INFO -> {
				
				try(PacketCoder decoder = new PacketCoder(packet.getData() , offset + 1)) {
					
					Iterator<NetworkedEntities> iter = otherClients.iterator();
					NetworkedEntities e;
					
					CSQueue<Object> reps = decoder.rRepititions(PacketCoder.CONNECTION_ID , PacketCoder.POSITION);
					while(!reps.empty()) {
						
						short ID = (short) reps.dequeue();
						float[] pos = (float[]) reps.dequeue();
						
						//find the entity we are looking for. worst case O(n) but we dont have to restart iterating every while loop iteration
						//and we are guaranteed the order of entities the server sends will be least to greatest
						while((e = iter.next()).connectionIndex() != ID);
													
						e.networked().moveTo(pos[0] , pos[1]);
						scene.entities().add(e.networked());
						
					}
					
				}
				
			}			
			
		}
		
	}
	
	@Override public void instanceUpdate() {
		
		//send unreliable message to server
		try (PacketCoder coder = new PacketCoder()) {

			coder
				.bflag(UPDATE)
				.bupdateSequence(networkedPlayer.updateSequence++)
				.bControlStrokes(networkedPlayer.controlStates())
			;
			
			clientSocket.send(new DatagramPacket(coder.get() , coder.position()));
		
		} catch (IOException e) {

			e.printStackTrace();

		}
		
		timeoutTimer.start();
			
		if(timeoutTimer.getElapsedTimeSecs() > 30) {
			
			System.err.println("TIMED OUT");
			timeoutTimer.start();
		
		}

		try {
			
			ReliableDatagram.tickLiveDatagrams(clientSocket);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public Iterable<NetworkedEntities> managedConnections() {
		
		return otherClients;
		
	}

	public void onLevelLoad(Levels level , float[] initialPosition) throws IOException {
		
		//tell the server we have left the old level and await a response
		try(PacketCoder coder = new PacketCoder()){
			
			coder
				.bflag(LOCATION_CHANGE_IN)
				.bstring(level.macroLevel() + "/" + level.gameName() + ".CStf")
				.bposition(initialPosition)
			;
			
			sendReliable(coder.get() , 190249113 , 1200);
			
			//block until we get a message TODO
			
			
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

	@Override public void listen() throws IOException {
		
		DatagramPacket received = new DatagramPacket(new byte[256] , 256);
		clientSocket.receive(received);
		
		if(ReliableDatagram.isReliableDatagramPacket(received)) {
		
			if(ReliableDatagram.isAcknowledgement(received)) ReliableDatagram.acceptAcknowledgement(received);
			else {
				
				ReliableDatagram.acknowledge(clientSocket, received);
				// Offload handling of flags to another thread if possible, but if we are not connected yet, handle them in this thread
				// because TemporalExecutor is not running at that time.
				if (connected) TemporalExecutor.onTrue(() -> true, () -> handleFlags(received , 8));
				else handleFlags(received , 8);
			
			}
						
		} else {
			
			if(connected) TemporalExecutor.onTrue(() -> true, () -> handleFlags(received , 0));
			else handleFlags(received , 0);
			
		}
				
	}

	@Override public boolean host() {
		
		return false;
		
	}

	@Override public boolean client() {
		
		return true;
		
	}

	@Override public void toggleUI() {
		
		chatUI.toggle();
		debugUI.toggle();
		
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
	
	public NetworkedEntities networkedEntity() {
		
		return networkedPlayer;
		
	}
	
	public Iterator<NetworkedEntities> otherClientsIterator() {
		
		return otherClients.iterator();
		
	}
	
	public NetworkedEntities getNetworkedByEntity(Entities E) {
		
		if(E == networkedPlayer.networked()) throw new IllegalArgumentException("Invalid parameter, gave this client's entity");
		Iterator<NetworkedEntities> iter = otherClients.iterator();
		NetworkedEntities element;
		while(iter.hasNext()) if((element = iter.next()).networked() == E) return element;
		throw new IllegalArgumentException("Entity " + E.name() + " not found among networked entities");
		
	}
	
	public void syncPeripheralsByControls(byte... controls) {
		
		networkedPlayer.syncPeripheralsByControls(controls);
		
	}
	
	private class ClientDebugUI extends UserInterface {
	
		private boolean playersDropDown = false;
		private boolean show = false;
		
		ClientDebugUI() {		
			
			super("Multiplayer Debug" , 1210 , 5 , 350 , 600 , NK_WINDOW_BORDER|NK_WINDOW_TITLE , NK_WINDOW_BORDER|NK_WINDOW_TITLE);
		
			layoutBody((frame) -> {

				int playersListDropDown = playersDropDown ? NK_SYMBOL_TRIANGLE_RIGHT : NK_SYMBOL_TRIANGLE_DOWN;
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Unique Connection ID:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + connectionID , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "RTT:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + ReliableDatagram.computeAverageRTT() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 1);
				if(nk_selectable_symbol_label(context , playersListDropDown , "Players" , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE , toByte(ALLOCATOR , playersDropDown))) 
					playersDropDown  = playersDropDown ? false : true;
				
				if(playersDropDown) for(NetworkedEntities x : otherClients) {
						
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , x.connectionIndex() + "|" + x.networked().name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_RIGHT);
						
				}
				
			});
			
		}
		
		void toggle() {
			
			show = show ? false : true;
			
		}
		
	}
	
}

