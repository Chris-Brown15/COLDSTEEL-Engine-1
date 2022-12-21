package Networking;

import static Networking.Utils.DatagramPacketUtils.*;

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
import static org.lwjgl.nuklear.Nuklear.nk_slider_int;

import static CSUtil.BigMixin.toByte;
import static CSUtil.BigMixin.Try;
import static CSUtil.BigMixin.TRY;
import static Networking.Utils.NetworkingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import CS.UserInterface;
import CSUtil.Timer;
import CSUtil.DataStructures.CSQueue;
import CSUtil.DataStructures.CSStack;
import CSUtil.DataStructures.LinkedRingBuffer;
import CSUtil.DataStructures.RingBuffer;
import CSUtil.DataStructures.Tuple2;
import CSUtil.Dialogs.Debug.NetworkedEntityPreviousFrameViewer;
import Core.Scene;
import Core.Entities.Entities;
import Game.Core.GameRuntime;
import Game.Core.GameState;
import Game.Levels.Levels;
import Game.Player.PlayerCharacter;
import Networking.Utils.PacketCoder;
import Networking.Utils.PacketLoser;

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

	private volatile ClientState state = ClientState.DORMANT;
	
	private GameRuntime gameRuntime;
	
	private Levels level;
	private Scene scene;
	
	private NetworkedEntities networkedPlayer;
	private final CopyOnWriteArrayList<NetworkedEntities> otherClients = new CopyOnWriteArrayList<>();
	
	private ClientDebugUI debugUI;
	private MultiplayerChatUI chatUI;
	private NetworkedEntityPreviousFrameViewer thisNetworkedPreviousInputViewer;
	
	private final DatagramSocket clientSocket = new DatagramSocket();	
	private final Thread clientListeningThread; //thread responsible for recieving messages
	private final Thread clientPacketHandlingThread; //thread responsible for handling received messages
	private final CSQueue<Tuple2<DatagramPacket , Integer>> packetsToHandle = new CSQueue<>();
		
	private volatile boolean connected = false;
	private short connectionID = -1;
	
	private Timer timeoutTimer = new Timer();
	private PacketLoser packetLoser = new PacketLoser();
	private RingBuffer<Long> receivedPacketChecksums = new RingBuffer<Long>(25);

	public NetworkClient(GameRuntime gameRuntime , String connectionInfo) throws IOException {
		
		this.gameRuntime = gameRuntime;
		this.scene = gameRuntime.scene();
		
		String[] split = connectionInfo.split(":");

		clientSocket.setSoTimeout(30000); //try to connect for 30 seconds, returning to the main menu otherwise.

		try {
			
			clientSocket.connect(InetAddress.getByName(split[0]) , Integer.parseInt(split[1]));
			
		} catch(IOException e) {
			
			System.err.println("Failed to conenct to User Hosted Server.");
			e.printStackTrace();
						
		}
		
		clientListeningThread = new Thread(() -> {

			while(true) Try(() -> listen());
					
		});

		clientPacketHandlingThread = new Thread(() -> {
			
			while(true) handlePackets();
			
		});
		
		clientListeningThread.setDaemon(true);
		clientPacketHandlingThread.setDaemon(true);
		
		/*
		 * Here, we start listening for a response from the server. If 30 seconds elapse and we recieve no message return to the main menu
		 * and TODO: display an error message. 
		 */				
		clientListeningThread.start();
		clientPacketHandlingThread.start();
		timeoutTimer.start();
		
		try(PacketCoder preconnect = new PacketCoder()) {
			
			preconnect.bflag(PRE_CONNECT);

			System.out.println("Sending preconnect message.");
			
			TRY(() -> sendReliable(preconnect.get() , 184912801 , 1000));
			setState(ClientState.AWAITING_PRE_CONNECTION);
			
		}
		
	}
	
	/**
	 * Attempts to connect to the address given at construction, returning the user to the main menu and calling {@code fadeInFunction} if a failure
	 * ocurrs. 
	 * 
	 * @param fadeInFunction — some code which should call the engine fade in method.
	 */
	public void connectAndStart(PlayerCharacter player , Levels currentLevel) {

		System.out.println("completing connection");
		
		this.level = currentLevel;
		networkedPlayer = new NetworkedEntities((short) -1 , player.playersEntity() , true);
		thisNetworkedPreviousInputViewer = new NetworkedEntityPreviousFrameViewer(networkedPlayer , 1565 , 5);
		
		try { 
			
			String levelAsString = level.macroLevel() + "/" + level.gameName() + ".CStf";
				
			try(PacketCoder coder = new PacketCoder()){
				
				coder
					.bflag(CHANGE_CONNECTION)
					.bstring(player.playersEntity().name())
					.bposition(player.playersEntity().getMidpoint())
					.bstring(levelAsString)
				;
				
				//message containing this client's name, position and level 
				sendReliable(coder.get() , CHANGE_CONNECTION , 1200);
			
			}
			
			//block and wait for a connection message
			while(timeoutTimer.getElapsedTimeSecs() < 30 && getState() == ClientState.AWAITING_PRE_CONNECTION);
			
			if(timeoutTimer.getElapsedTimeSecs() >= 30) System.err.println("Failed to finish connecting to User Hosted Server.");
			else { //connected
				
				connected = true;
				chatUI = new MultiplayerChatUI(player.playerCharacterName() , this);
				debugUI = new ClientDebugUI();
				timeoutTimer.start();				
				scene.entityScriptingInterface().client(this);
				scene.entities().internalRunInitialSystems(networkedPlayer.networked());
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
		
			case PRE_CONNECT -> {
				
				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)) {
					
					connectionID = coder.rconnectionID();					
					setState(ClientState.AWAITING_CONNECTION);
					
				}
				
				System.out.println("pre connection successful");
				
			}
		
			case CHANGE_CONNECTION -> { 
				
				if(!connected) System.exit(-1); //TODO actual disconect code
				
				setState(ClientState.READY);
				
				scene.entities().setNetworkInstance(this);

				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)) {
					
					//contains all other players in the server's ID and entity name
					CSQueue<Object> othersData = coder.rRepititions(PacketCoder.CONNECTION_ID , PacketCoder.STRING);
					//dequeues other player's data and stores it 
					while(!othersData.empty()) {
						
						short othersID = (short) othersData.dequeue();
						String entName = (String) othersData.dequeue();
						Entities otherClient = new Entities(scene , entName + ".CStf");
						NetworkedEntities newNetworked = new NetworkedEntities(othersID , otherClient , true);
						otherClients.add(newNetworked);
						newNetworked.runScript();
						
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
					NetworkedEntities e = getNetworkedEntity(senderID);
					
					LinkedRingBuffer<byte[]> previousInputs = e.previousInputs();
					byte sequence = coder.rControlStrokes(previousInputs);
					byte clientsViewOfUpdateNumer = e.getUpdateNumber();
					
					if(sequence == e.getUpdateNumber()) {
						 
						e.updateControlStateView(e.previousInputs().get());
						e.incrementUpdateNumber();
						 
					} else {

						int dropped = e.getNumberLostPackets(sequence);
						
						System.out.println("Possible Packet Loss, packed named: " + sequence + ", current sequence, " + clientsViewOfUpdateNumer);
						System.out.println("Dropped " + dropped + " packets.");
						e.isReady = false;

						CSStack<byte[]> previousInputsStack = new CSStack<byte[]>();
						int iter = 0;
						while(iter < dropped) {
							
							previousInputsStack.push(previousInputs.get());
							iter++;
							
						}
						
						e.rewindStateToCapture();
							
						while(!previousInputsStack.empty()) {
							
							e.updateControlStateView(previousInputsStack.pop());
							scene.entities().internalRunInitialSystems(e.networked());
							
							scene.kinematics().process(e.networked());
							
							scene.entities().internalRunFinalSystems(e.networked());
						
						}

												
					}
					
					e.setUpdateNumber(++sequence);
					e.isReady = true;
					
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
	
	private NetworkedEntities getNetworkedEntity(short connectionID) {

		Iterator<NetworkedEntities> iter = otherClients.iterator();
		NetworkedEntities e;
		
		while(iter.hasNext()) {
			
			e = iter.next();
			if(e.connectionIndex() == connectionID) return e;
		
		}
		
		throw new IllegalArgumentException(connectionID + " is invalid as a connection ID for a networked client.");
			
	}

	public NetworkedEntities getNetworkedEntity(Entities networkedEntity) {

		Iterator<NetworkedEntities> iter = otherClients.iterator();
		NetworkedEntities e;
		
		while(iter.hasNext()) {
			
			e = iter.next();
			if(e.networked() == networkedEntity) return e;
		
		}
		
		throw new IllegalArgumentException(connectionID + " is invalid as a connection ID for a networked client.");
			
	}
	
	public void handlePackets() {

		synchronized(packetsToHandle) {
			
			while(!packetsToHandle.empty()) {
				
				var packet = packetsToHandle.dequeue();
				handleFlags(packet.getFirst() , packet.getSecond());
				
			}
			
		}			
			
	}
	
	@Override public void instanceUpdate() {
	
		//send unreliable message to server
		try (PacketCoder coder = new PacketCoder()) {

			networkedPlayer.constructUpdatedViewOfControls();
			
			coder
				.bflag(UPDATE)
				.bControlStrokes(networkedPlayer.getUpdateNumber() , networkedPlayer.previousInputs())
			;
					
			clientSocket.send(new DatagramPacket(coder.get() , coder.position()));
			networkedPlayer.incrementUpdateNumber();
			networkedPlayer.unPressKeys();
			 
 		} catch (IOException e) {

			e.printStackTrace();
 
		}
		
		timeoutTimer.start();
		
		if(timeoutTimer.getElapsedTimeSecs() > 30) {
			
			System.err.println("TIMED OUT");
			timeoutTimer.start();
		
		}

		TRY(() -> ReliableDatagram.tickLiveDatagrams(clientSocket));
				
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
						
		}
		
	}
	
	public void forEachNetworkedEntity(Consumer<NetworkedEntities> callback) {
		
		otherClients.forEach(callback);
		
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
		
		DatagramPacket received = new DatagramPacket(new byte[512] , 512);
		clientSocket.receive(received);
		
		if(packetLoser.receive(received) == null) return;
		
		//prevents duplicate packets
		if(handleChecksum(checksum(received) , receivedPacketChecksums)) return;
		
		boolean isAck = false;
		int off = 0;
		
		if(ReliableDatagram.isReliableDatagramPacket(received)) {
		
			off = 8;
			
			if(ReliableDatagram.isAcknowledgement(received)) {
				
				ReliableDatagram.acceptAcknowledgement(received);
				isAck = true;
				
			} else ReliableDatagram.acknowledge(clientSocket, received);
						
		}
		
		if(!isAck){
			
			synchronized(packetsToHandle) {
				
				packetsToHandle.enqueue(new Tuple2<>(received , off));
				
			}
			
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
		thisNetworkedPreviousInputViewer.toggle();
		
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
	
	public void syncPeripheralsByControls(byte... controls) {
		
		networkedPlayer.setControlsToSync(controls);
		
	}
	
	public ClientState getState() {
		
		return state;
		
	}

	public void setState(ClientState state) {
		
		this.state = state;
		
	}

	private class ClientDebugUI extends UserInterface {
	
		private boolean playersDropDown = false;
		private boolean show = false;
		private IntBuffer packetLossSlider = ALLOCATOR.ints(-1);
		
		ClientDebugUI() {		
			
			super("Multiplayer Debug" , 1240 , 5 , 350 , 600 , NK_WINDOW_BORDER|NK_WINDOW_TITLE , NK_WINDOW_BORDER|NK_WINDOW_TITLE);
			
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
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_slider_int(context , -1 , packetLossSlider , 100 , 1);
				
			});
			
		}
		
		void toggle() {
			
			show = show ? false : true;
			
		}
		
	}
	
}

