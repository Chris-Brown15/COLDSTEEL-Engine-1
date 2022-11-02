package Networking.UserHostedServer;

import static org.lwjgl.system.MemoryUtil.memCalloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static CSUtil.BigMixin.toBool;
import static Networking.Utils.NetworkingConstants.*;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_DOWN;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_label;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import CS.COLDSTEEL;
import CS.Engine;
import CS.UserInterface;
import CSUtil.RefInt;
import CSUtil.Timer;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.Tuple2;
import Core.Scene;
import Core.TemporalExecutor;
import Core.Entities.Entities;
import Game.Levels.Levels;
import Networking.MultiplayerChatUI;
import Networking.NetworkedEntities;
import Networking.NetworkedInstance;
import Networking.ReliableDatagram;
import Networking.Utils.ByteArrayUtils;
import Networking.Utils.PacketCoder;
import Physics.Kinematics;

/**
 * 
 * This class will be created when a player starts a User Hosted Session from the main menu. The player who hosts this server will connect to it
 * like any other player.
 * 
 * @author Chris Brown
 *
 */
public class UserHostedServer implements NetworkedInstance {

	private static final int DEFAULT_PORT = 32900;
	
	//holds data about all players in the level
	private final ConcurrentHashMap<Integer , UserConnection> connections = new ConcurrentHashMap<>(17);
	//holds references to levels that are in play, which means two or more players are in them
	private final ConcurrentHashMap<Tuple2<Levels , Scene> , CSLinked<UserConnection>> liveLevels = new ConcurrentHashMap<>(4);	
	private final Engine engine;
	private final ServerUI ui;
	private final MultiplayerChatUI chatUI;
	private short nextConnectionID = 0;
	private boolean running = false;
		
	private final Thread serverThread = new Thread(new Runnable() {
		
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
	
	private final Thread serverUpdateThread = new Thread(new Runnable() {
		
		@Override public void run() {
			
			while(true) { 
				
				instanceUpdate();
			
			}
			
		}
		
	});
	
	private final DatagramSocket serverSocket;
	
	{
		serverUpdateThread.setDaemon(true);
		serverThread.setDaemon(true);
	}
	
	public UserHostedServer(Engine engine) {
		
		this.engine = engine;
		
		ui = new ServerUI();
		chatUI = new MultiplayerChatUI("SERVER" , this);
		
		try {
			
			serverSocket = new DatagramSocket(DEFAULT_PORT , InetAddress.getLocalHost());
			serverThread.setName("Server Listening Thread");
			serverUpdateThread.setName("Server Perpetual Update Thread");
			
			serverThread.start();
			serverUpdateThread.start();
			running = true;
			System.out.println("User Hosted Server started.");
			
		} catch (SocketException|UnknownHostException e) {
			
			e.printStackTrace();
			throw new IllegalStateException("Failed to create server, terminating.");
			
		}
		
	}
	
	private void handleFlaggedPacket(DatagramPacket packet , int offset) {
		
		switch(packet.getData()[offset]) {
		
			case CHANGE_CONNECTION -> {
			
				int key;
				if(connections.containsKey(key = UserConnection.computeHashCode(packet.getAddress(), packet.getPort()))) {
					
					connections.remove(key);
					//disconnected

				} else {
										
					/*
					 * 
					 * Connect and sync the entrant to the server
					 * The entrant will either be in a level other players are in, or not.
					 * The packet we send back should contain all the needed info for the client. 
					 * Start with pairs of IDs and Entity names for each player on the server
					 * then a list of IDs and positions corresponding to players in the same level
					 * as entrant.
					 * 
					 */
					
					//connected
					System.out.println("Connecting new user");
					
					PacketCoder entrantCoder = new PacketCoder(packet.getData() , offset + 1);
					
					String entrantName = entrantCoder.rstring() + ".CStf";
					float[] entrantPos = entrantCoder.rposition();
					String entrantmacroLevelLevel = entrantCoder.rstring();
					
					short connectionID = nextConnectionID++;
					NetworkedEntities newClientNetworkedEntity = new NetworkedEntities(connectionID , new Entities(entrantName) , false);
					newClientNetworkedEntity.setNetworkControlled();
					UserConnection newClient = new UserConnection(
						connectionID , 
						packet.getPort() , 
						packet.getAddress() , 
						new Timer() , 
						newClientNetworkedEntity
					);
					
					newClient.entity.networked().moveTo(entrantPos);
					
					//this packet gets sent to people who are in the server but outside the level the entrant connected from
					PacketCoder toOthersOutside = new PacketCoder()
						.bflag(CLIENT_CONNECTED)
						.bconnectionID(connectionID)
						.bstring(entrantName)
					;
					
					//this coder will hold IDs and names of entities and then IDs and positions of players in the level the client is connecting to 
					PacketCoder toEntrant = new PacketCoder()
						.bflag(CHANGE_CONNECTION)					
						.bconnectionID(connectionID)
						.brepitition((short) connections.size() , (byte) 3)
					;
					
					//populate the coder for the entrant of ALL players ID and entity
					connections.forEach((hash , connection) -> {
						
						toEntrant
							.bconnectionID(connection.index)
							.bstring(connection.entity.networked().name())
						;
							
					});
					
					RefInt foundLevel = new RefInt(0);
					
					liveLevels.forEach((levelSceneTuple , players) -> {
						
						Levels level = levelSceneTuple.getFirst();
						//if this is true, the entrant is in a level that is already in play
						if(foundLevel.get() == 0 && (level.macroLevel() + "/" + level.gameName() + ".CStf").equals(entrantmacroLevelLevel)) { 
																			
							//this packet gets sent to people who are in the server and inside the level teh entrant connected from
							PacketCoder toOthersInside = new PacketCoder()
								.bflag(CLIENT_CONNECTED)
								.bconnectionID(connectionID)
								.bstring(entrantName)
								.bposition(entrantPos)
							;
							
							foundLevel.set(1);
							toEntrant.brepitition((short) players.size(), (byte)2);
							
							players.forEachVal(player -> {
								
								//add this player's data to the packet going to the entrant 
								toEntrant
									.bconnectionID(player.index)
									.bposition(player.entity.networked().getMidpoint())
								;
								
								//send the packet to players inside the level the entrant is in to each player
								try {
									
									sendReliable(toOthersInside.get() , 102894191 + player.index , 1000 , player.address , player.port);
									
								} catch (IOException e) {
									
									e.printStackTrace();
									
								}
								
							});
							
							//adds the new client to the level they said they were in
							players.add(newClient);
							levelSceneTuple.getSecond().entities().add(newClient.entity.networked());
							
						} else {
							
							players.forEachVal(player -> {

								//send the packet to players outside the level the entrant is in to each player not in entrants level
								try {
									
									sendReliable(toOthersOutside.get() , 102892191 + player.index , 1000 , player.address , player.port);
									
								} catch (IOException e) {
									
									e.printStackTrace();
									
								}
								
							});
							
						}
						
					});
					
					//if we did not find a level the client was in, we add a new level
					if(foundLevel.get() == 0)  {
						
						Levels newLiveLevel = new Levels((CharSequence)(COLDSTEEL.data + "macrolevels/" + entrantmacroLevelLevel));						
						Scene newLevelScene = new Scene(engine.getCamera());
						newLevelScene.entities().setServer(this);
//						//server simulates at 30 ticks per second
//						newLevelScene.entities().setTargetTicksPerSecond(30);
						newLiveLevel.deploy(newLevelScene);
						newLevelScene.entities().add(newClient.entity.networked());
						liveLevels.put(new Tuple2<Levels , Scene>(newLiveLevel , newLevelScene) , new CSLinked<UserConnection>(newClient));
					
						//update UI that we loaded a new level
						ui.refreshLevelPointers();
						
					}
					
					//key the entity
					connections.put(key, newClient);
					
					//finally respond to client
					try {
						
						sendReliable(toEntrant.get() , 1020103984 + connectionID , 1000 , packet.getAddress() , packet.getPort());
						
					} catch (IOException e) {
						
						e.printStackTrace();
						
					}
					
					System.out.println("New Player Connected as: " + entrantName);
						
				}
				
			}
			
			case CLIENT_CONNECTED -> {}
			case CLIENT_DISCONNECTED -> {}
			case CHAT_MESSAGE -> {
				
				PacketCoder coder = new PacketCoder(packet.getData() , offset + 1);
				String received = coder.rstring(); 
								
				byte[] chatData = ByteArrayUtils.compose(new byte[] {CHAT_MESSAGE} , received.getBytes());
				
				connections.forEach((hash , connection) -> {
					
					if(UserConnection.computeHashCode(packet.getAddress(), packet.getPort()) != hash) {
						
						try {
							
							sendReliable(chatData , 9349491 , 1500);
							
						} catch (IOException e) {
							
							e.printStackTrace();
							
						}
						
					}
					
				});
				
				chatUI.appendChatMessage(received);
				
			}
			
			case LOCATION_CHANGE -> {}
			
			case UPDATE -> {
				
				UserConnection sender = connections.get(UserConnection.computeHashCode(packet.getAddress(), packet.getPort()));
				PacketCoder coder = new PacketCoder(packet.getData() , offset + 1);
				
				/*
				 * Sets the server's view of the keystrokes sender made last 
				 */				
				if(coder.testFor(PacketCoder.CONTROL_KEY_STROKES)) {
					
					byte[] controls = coder.rControlStrokes();
					sender.entity.controlsState(controls);
					
				}
				
			}
			
			default -> throw new IllegalArgumentException("Invalid packet flag given: " + packet.getData()[offset]);
		
		}
		
	}

	public boolean running() {
		
		return running;
		
	}
	
	public String IP() {
		
		return serverSocket.getLocalAddress().getHostAddress();
		
	}
	
	public int port() {
		
		return serverSocket.getLocalPort();
		
	}
	
	public void broadCastReliable(byte[] data , int hash , int cooldownMillis) {
		
		connections.forEach((connectionHash , connection) -> {
			
			try {
				
				sendReliable(data , hash + connectionHash , cooldownMillis , connection.address , connection.port);
				
			} catch (IOException e) {

				e.printStackTrace();
				
			}
			
		});
		
	}
	
	public NetworkedEntities getNetworkedEntity(Entities E) {
		
		Set<Entry<Integer , UserConnection>> conns = connections.entrySet();
		for(var entry : conns) if(entry.getValue().entity.networked() == E) return entry.getValue().entity;		
		return null;
		
	}
	
	@Override public void instanceUpdate() {
		
		try {
			
			ReliableDatagram.tickLiveDatagrams(serverSocket);
			
			/*
			 * Unfortunately, this prevents someone from using this instance of the COLDSTEEL engine from hosting a server
			 * and client on that server at the same time because the global objects Kinematics and TemporalExecutor are needed
			 * for entities to work.
			 * TODO: something about this 
			 */
			liveLevels.forEach((levelSceneTuple , listOfConnections) -> {
				
				levelSceneTuple.getSecond().entities().editorRunSystems(
					() -> {} ,
					() -> {
		
						//serverside physics simulation
						Kinematics.process();
						TemporalExecutor.process();
										
					} , 
					//release server view of connection's keys
					() -> listOfConnections.forEachVal(connection -> connection.entity.unStrikeKeys())
				);
								
			});
				
		} catch (IOException e) {
				
			e.printStackTrace();
				
		}			
		
	}
	
	@Override public void send(byte[] data) throws IOException {

		
	}

	@Override public void sendReliable(byte[] data, int hash, int cooldownMillis) throws IOException {

		throw new UnsupportedOperationException("Servers must have a specified address to send a packet to.");
		
	}

	@Override public void sendReliable(byte[] data, int hash, int cooldownMillis, InetAddress addr, int port) throws IOException {

		ReliableDatagram.sendReliable(serverSocket, data, hash, cooldownMillis , addr , port);
		
	}

	@Override public void listen() throws IOException {
		
		DatagramPacket packet = new DatagramPacket(new byte[256] , 256);
		serverSocket.receive(packet);
		int offset = 0; 
		if(ReliableDatagram.isReliableDatagramPacket(packet)) {
			
			offset = 8;
			if(ReliableDatagram.isAcknowledgement(packet)) ReliableDatagram.acceptAcknowledgement(packet);
			else ReliableDatagram.acknowledge(serverSocket, packet);
			
		}
		
		if(isFlaggedMessage(packet.getData() , offset)) { 
			
			int off = offset;
			TemporalExecutor.onTrue(() -> true , () -> handleFlaggedPacket(packet , off));
			
		}
				
	}

	@Override public boolean host() {

		return false;
	}

	@Override public boolean client() {

		return false;
	}

	@Override public void toggleUI() {

		ui.toggleShow();
		chatUI.toggle();
		
	}

	@Override public void onLevelLoad(Levels newLevel, float[] initialPosition) throws IOException {

		
	}

	@Override public void shutDown() { 
	
		ui.shutDown();
		chatUI.shutDown();
		
	}

	private class ServerUI extends UserInterface {
		
		private static int options = NK_WINDOW_MOVABLE|NK_WINDOW_BORDER|NK_WINDOW_TITLE;
		
		public ServerUI() {
			
			super("Multiplayer Information", 1565 , 5 , 350 , 600 , options , options);
			
			layoutBody((frame) -> {

				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Server Address:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , serverSocket.getLocalAddress().toString() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Server Port:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + serverSocket.getLocalPort() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);					
				
				int symbol = toBool(liveLevelDropdown) ? NK_SYMBOL_TRIANGLE_DOWN : NK_SYMBOL_TRIANGLE_RIGHT;
				
				nk_layout_row_dynamic(context , 30 , 1);				
				if(nk_selectable_symbol_label(context , symbol , "Show Live Levels" , NK_TEXT_ALIGN_LEFT , liveLevelDropdown)) { 
					
					refreshLevelPointers();
					
				}
				
				if(toBool(liveLevelDropdown)) {
					
					RefInt iter = new RefInt(0);
					
					liveLevels.forEach((levelSceneTuple , listOfPlayers) -> {
						
						nk_layout_row_dynamic(context , 20 , 1);
						if(nk_checkbox_label(context , levelSceneTuple.getFirst().gameName() , levelPtrs[iter.get()])) {
							
							//unsets all others
							for(int i = 0 ; i < liveLevels.size() ; i ++) if(i != iter.get()) levelPtrs[i].put(0 , (byte) 0);
							
							engine.schedule(() -> {
								
								Engine.boundScene().entities().runSystems(false);
								Engine.bindScene(levelSceneTuple.getSecond());
								
							});
														
						}
						
						nk_layout_row_dynamic(context , 20 , 2);
						nk_text(context , "Ticks Last Second: " , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
						nk_text(context , "" + Engine.boundScene().entities().ticksLastSecond() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
						
						listOfPlayers.forEachVal(connection -> {
							
							nk_layout_row_begin(context , NK_STATIC , 30 , 2);								
							nk_layout_row_push(context , 20);
							nk_text_wrap(context , "");
							nk_layout_row_push(context , 150);
							nk_text(context , connection.index + ": " + connection.entity.networked().name() , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
							nk_layout_row_end(context);
							
						});
							
						iter.add();
						
					});
					
				}
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Number Connections" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + connections.size() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Average RTT (1 second)" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + ReliableDatagram.computeAverageRTT() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				var set = connections.entrySet();
				for(Entry<Integer , UserConnection> x : set) { 
					
					nk_layout_row_dynamic(context , 20 , 2);
					nk_text(context , "Index|IP Address:Port" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
					nk_text(context , x.getValue().index + " | " + x.getValue().toString() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				}
				
			});

		}

		//toggle this with f11
		private ByteBuffer liveLevelDropdown = ALLOCATOR.bytes((byte) 0);
		
		private ByteBuffer[] levelPtrs;
		
		public void refreshLevelPointers() {

			int active = -1;

			if(levelPtrs != null) for(int i = 0 ; i < levelPtrs.length ; i ++) { 

				if(levelPtrs[i].get(0) == 1) active = i;					
				memFree(levelPtrs[i]);
				
			}
			
			levelPtrs = new ByteBuffer[liveLevels.size()];
			for(int i = 0 ; i < liveLevels.size() ; i ++) levelPtrs[i] = memCalloc(1);
			if(active != -1) levelPtrs[active].put(0 , (byte) 1);
			
		}
		
		public void toggleShow() {
			
			show = show ? false : true;
			
		}
		
		public void shutDown() {
			
			show = false;
			if(levelPtrs != null) for(ByteBuffer b : levelPtrs) memFree(b);
			
		}
	
	}
	
}

