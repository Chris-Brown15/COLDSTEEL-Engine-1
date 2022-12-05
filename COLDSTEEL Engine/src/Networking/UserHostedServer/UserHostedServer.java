package Networking.UserHostedServer;

import static CSUtil.BigMixin.TRY;
import static CSUtil.BigMixin.toBool;
import static CSUtil.CSLogger.LOGGING_ENABLED;
import static CSUtil.CSLogger.log;
import static Networking.Utils.NetworkingConstants.*;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_DOWN;
import static org.lwjgl.nuklear.Nuklear.NK_SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_CENTERED;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_RIGHT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_MOVABLE;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_label;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import CS.COLDSTEEL;
import CS.Engine;
import CS.UserInterface;
import CSUtil.RefInt;
import CSUtil.Timer;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSQueue;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.cdNode;
import Core.Scene;
import Core.TemporalExecutor;
import Core.Entities.Entities;
import Core.Entities.EntityScripts;
import Game.Levels.Levels;
import Networking.MultiplayerChatUI;
import Networking.NetworkedEntities;
import Networking.NetworkedInstance;
import Networking.ReliableDatagram;
import Networking.Utils.ByteArrayUtils;
import Networking.Utils.PacketCoder;

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
	
	//holds data about all players in the server
	private volatile ConcurrentHashMap<Integer , UserConnection> connections = new ConcurrentHashMap<>(17);
	//holds references to levels that have previously been loaded. If all players leave a a level, we keep it in memory and once
	//another player enters it, we redeploy the level
	private volatile ConcurrentHashMap<Tuple2<Levels , Scene> , CSLinked<UserConnection>> loadedLevels = new ConcurrentHashMap<>(4);
	private volatile CSQueue<Tuple2<DatagramPacket , Integer>> packetsToHandle = new CSQueue<>();
	private final Engine engine;
	private final ServerUI ui;
	private final MultiplayerChatUI chatUI;
	private volatile short nextConnectionID = 0;
	private volatile boolean running = false;
	
	private final Thread serverListeningThread = new Thread(new Runnable() {
		
		@Override public void run() {
			
			while(true) TRY(() -> listen());
			
		}
		
	});
	
	private final Thread serverUpdateThread = new Thread(new Runnable() {

		private Timer updateThreadTimer = new Timer();
		
		@Override public void run() {
			
			while(true) { 
				
				updateThreadTimer.start();
				instanceUpdate();
				while(16 - updateThreadTimer.getElapsedTimeMillis() > 0.0d); 
				
			}
			
		}
		
	});
	
	private final DatagramSocket serverSocket;
	
	{	
		serverUpdateThread.setDaemon(true);
		serverListeningThread.setDaemon(true);
	}	
	
	public UserHostedServer(Engine engine) {
		
		this.engine = engine;
		
		ui = new ServerUI();
		chatUI = new MultiplayerChatUI("SERVER" , this);
		
		try {
			
			serverSocket = new DatagramSocket(DEFAULT_PORT , InetAddress.getLocalHost());
			serverListeningThread.setName("Server Listening Thread");
			serverUpdateThread.setName("Server Perpetual Update Thread");
			
			serverListeningThread.start();
			serverUpdateThread.start();
			running = true;
			System.out.println("User Hosted Server started.");
			
		} catch (SocketException|UnknownHostException e) {
			
			e.printStackTrace();
			throw new IllegalStateException("Failed to create server, terminating.");
			
		}
		
		if(LOGGING_ENABLED) {
			
			log("Server Launched");
			
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
					System.out.println("Connecting new user");
					
					//initialize resourcse
					try(PacketCoder entrantCoder = new PacketCoder(packet.getData() , offset + 1) ; 
						PacketCoder toOthersOutside = new PacketCoder() ;  
						PacketCoder toEntrant = new PacketCoder()
					) {
						
						String entrantName = entrantCoder.rstring() + ".CStf";
						float[] entrantPos = entrantCoder.rposition();
						String entrantmacroLevelLevel = entrantCoder.rstring();
						short connectionID = nextConnectionID++;
						
						//this packet gets sent to people who are in the server but outside the level the entrant connected from
						toOthersOutside
							.bflag(CLIENT_CONNECTED)
							.bconnectionID(connectionID)
							.bstring(entrantName)
						;
						
						//this coder will hold IDs and names of entities and then IDs and positions of players in the level the client is connecting to 
						toEntrant
							.bflag(CHANGE_CONNECTION)					
							.bconnectionID(connectionID)
							.brepitition((short) connections.size() , (byte) 2)
						;
						
						//populate the coder for the entrant of ALL players ID and entity
						connections.forEach((hash , connection) -> {
							
							toEntrant
								.bconnectionID(connection.index)
								.bstring(connection.entity.networked().name())
							;
							
						});
						
						RefInt foundLevel = new RefInt(0);
						UserConnection newClient = null;
						
						Set<Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>>> entries = loadedLevels.entrySet();
						
						for(Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>> x : entries) {

							Levels level = x.getKey().getFirst();
							Scene scene = x.getKey().getSecond();
							CSLinked<UserConnection> players = x.getValue();
							//if this is true, the entrant is in a level that is already in play
							if(foundLevel.get() == 0 && (level.macroLevel() + "/" + level.gameName() + ".CStf").equals(entrantmacroLevelLevel)) { 
								
								foundLevel.set(1);
								
								try(PacketCoder toOthersInside = new PacketCoder()) {
									
									toOthersInside
										.bflag(CLIENT_CONNECTED)
										.bconnectionID(connectionID)
										.bstring(entrantName)
										.bposition(entrantPos)
									;
									
									//this packet gets sent to people who are in the server and inside the level teh entrant connected from
									toEntrant.brepitition((short) players.size(), (byte)2);
									
									players.forEachVal(player -> TRY(() -> {

										//add this player's data to the packet going to the entrant 
										toEntrant
											.bconnectionID(player.index)
											.bposition(player.entity.networked().getMidpoint())
										;
										
										//send the packet to players inside the level the entrant is in to each player
										sendReliable(toOthersInside.get() , 102894191 + player.index , 1000 , player.address , player.port);
									
									}));
									
								} 
								
								NetworkedEntities newClientNetworkedEntity = new NetworkedEntities(
									connectionID , 
									new Entities(scene , entrantName) , 
									false
								);
																
								newClientNetworkedEntity.setNetworkControlled();
								
								newClient = new UserConnection(
									connectionID , 
									packet.getPort() , 
									packet.getAddress() ,
									newClientNetworkedEntity
								);
								
								newClientNetworkedEntity.networked().moveTo(entrantPos[0] , entrantPos[1]);
								
								//adds the new client to the level they said they were in
								players.add(newClient);
								scene.entities().add(newClient.entity.networked());
								
							} else {
								
								players.forEachVal(player -> TRY(() ->  {
									//send the packet to players outside the level the entrant is in to each player not in entrants level
									sendReliable(toOthersOutside.get() , 102892191 + player.index , 1000 , player.address , player.port);

								}));
								
							}
													
						}
						
						//if we did not find a level the client was in, we add a new level
						if(foundLevel.get() == 0)  {

							Scene newLevelScene = new Scene(engine);
							Levels newLiveLevel = new Levels(newLevelScene , (CharSequence)(COLDSTEEL.data + "macrolevels/" + entrantmacroLevelLevel));						

							newLevelScene.entityScriptingInterface().server(this);
							
							NetworkedEntities newClientNetworkedEntity = new NetworkedEntities(
								connectionID , 
								new Entities(newLevelScene , entrantName) , 
								false
							);
														
							newClientNetworkedEntity.setNetworkControlled();
							
							newClient = new UserConnection(
								connectionID , 
								packet.getPort() , 
								packet.getAddress() ,
								newClientNetworkedEntity
							);
							
							newClientNetworkedEntity.networked().moveTo(entrantPos[0] , entrantPos[1]);
							
							newLevelScene.entities().setNetworkInstance(this);
							newLiveLevel.deploy(newLevelScene);
							newLevelScene.entities().add(newClient.entity.networked());
							loadedLevels.put(new Tuple2<Levels , Scene>(newLiveLevel , newLevelScene) , new CSLinked<UserConnection>(newClient));
							
						}
						
						//key the entity
						connections.put(key, newClient);
						//finally respond to client
						TRY(() -> sendReliable(toEntrant.get() , 1020103984 + connectionID , 1000 , packet.getAddress() , packet.getPort()));
						
						System.out.println("New Player Connected as: " + entrantName);
						
					}
					
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
						
						TRY(() -> sendReliable(chatData , 9349491 , 1500));
						
					}
					
				});
				
				chatUI.appendChatMessage(received);
				
			}
			
			case LOCATION_CHANGE_IN -> {
				
				//someone has left their old level and new one is encoded in this packet
				UserConnection sender = connections.get(UserConnection.computeHashCode(packet.getAddress(), packet.getPort()));
				sender.busy = true;
				
				boolean foundNewLevelYet = false;
				
				String levelEntered;
				float[] posInNewLevel;
				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)){
					
					levelEntered = coder.rstring();
					posInNewLevel = coder.rposition();
					
				}
				
				Set<Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>>> levels = loadedLevels.entrySet();
					
				boolean removedSenderFromOldLevel = false;
				
				/*
				 * iterate over all live levels with two goals
						1) find the level the sender is leaving
						2) find the level the sender is entering
				   
				 * if any particular iteration satisfies one of these, take that opportunity to notify players in the level appropriately
				 * 
				 * Once we find the level the sender was previously in, either:
				   		1) there is at least one other player in the level,
				   		2) the sender was the only player in the level
				   
				 * If there is at least one other person in the level, we need to remove the sender from the list of connections and
				 * notify other players that the sender has left the level.
 
				 * If the sender is the only person in the level, we will redeploy the level ie reset the scene

				 */				
				for(Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>> x : levels) {
					
					//iterator of connections per level-scene tuple
					cdNode<UserConnection> iter = x.getValue().get(0);
					boolean advanceIter = true;
					
					//break out of the outer for loop if we can
					if(removedSenderFromOldLevel && foundNewLevelYet) break;
					
					//iterate over connections
					for(int i = 0 ; i < x.getValue().size() ; i ++) { 
						
						advanceIter = true;
						//if this is true, the sender is in the current list of connections
						if(iter.val.equals(sender)) {
							
							//if there is more than one person in the live level 
							if(x.getValue().size() > 1) {
																	
								//setup packet to old players
								try (PacketCoder toPlayersInOldLevel = new PacketCoder()) {

									toPlayersInOldLevel
										.bflag(LOCATION_CHANGE_OUT)
										.bconnectionID(sender.index)
									;
									
									//notify other player in the level the sender was in that the sender left
									x.getValue().forEachVal(conn -> TRY(() -> {
										
										if(!conn.equals(sender)) {
											
											sendReliable(toPlayersInOldLevel.get() , 130921012 + conn.index , 1000 , conn.address , conn.port);
											
										}
										
									}));
									
								} 
								
								//remove the senders entity from the list of entities
								x.getKey().getSecond().entities().remove(sender.entity.networked());
								
							} 
							//the sender was the last one in the level, we'll refresh the scene here which removes the entity anyway
							else {

								x.getKey().getFirst().deploy(x.getKey().getSecond());
								
							}						
							
							//remove the sender from the list of connections in this level
							iter = x.getValue().safeRemove(iter);
							advanceIter = false;
							removedSenderFromOldLevel = true;
													
						} 
						
						if(advanceIter) iter = iter.next;
						
					}
					
					//if we have not found the level that the sender is entering yet
					if(!foundNewLevelYet) {
						
						String someLevelsExpandedName = x.getKey().getFirst().macroLevel() + "/" + x.getKey().getFirst().gameName() + ".CStf";
						//string comparison to determine if the sender is entering this level
						//if true, we are entering this level, so send the below packet to players already in that level let them know 
						if(someLevelsExpandedName.equals(levelEntered)) try(PacketCoder toPlayersInNewLevel = new PacketCoder()) {
							
							foundNewLevelYet = true;
							//to players in new level, we are sending ID of sender and their position
							toPlayersInNewLevel
								.bflag(LOCATION_CHANGE_IN)
								.bconnectionID(sender.index)
								.bposition(posInNewLevel);
							;
							
							//this packet only needs to be sent in the case there are already people in the level the sender is entering
							try(PacketCoder toSender = new PacketCoder()) {
							
								toSender
									.bflag(LEVEL_LOAD_INFO)
									//number of people in the server with each having two elements, ID and position
									.brepitition((short) x.getValue().size() , (byte)2)
								;
								
								x.getValue().forEachVal(conn -> TRY(() -> {
								
									//update sender repitition
									toSender
										.bconnectionID(conn.index)
										.bposition(conn.entity.networked().getMidpoint())
									;
									
									//update people already in the level
									sendReliable(toPlayersInNewLevel.get() , 123095810 + conn.index , 1000 , conn.address , conn.port);
									
								}));
								
								//finally update sender about whose in the level already
								TRY(() -> sendReliable(toSender.get() , 123095810 << 1 , 800 , sender.address , sender.port));
								
							}

							sender.entity.networked().moveTo(posInNewLevel[0] , posInNewLevel[1]);
							//finally add sender to list of connections and entity to scene 
							x.getKey().getSecond().entities().add(sender.entity.networked());
							x.getValue().add(sender);

							((EntityScripts)sender.entity.networked().components()[Entities.SOFF]).resetLib(
								x.getKey().getSecond().entityScriptingInterface()
							);
							
						}
						
					}
						
				}
				
				//if we did not find the level the player is entering, we need to make a new one for them
				if(!foundNewLevelYet) {
					
					//same procedure as when a player enters a level for the first time
					Scene newLevelScene = new Scene(engine);
					newLevelScene.entityScriptingInterface().server(this);
					Levels newLiveLevel = new Levels(newLevelScene , (CharSequence)(COLDSTEEL.data + "macrolevels/" + levelEntered));						
					newLevelScene.entities().setNetworkInstance(this);
					newLiveLevel.deploy(newLevelScene);
					sender.entity.networked().moveTo(posInNewLevel[0] , posInNewLevel[1]);
					newLevelScene.entities().add(sender.entity.networked());
					((EntityScripts)sender.entity.networked().components()[Entities.SOFF]).resetLib(newLevelScene.entityScriptingInterface());
					loadedLevels.put(new Tuple2<Levels , Scene>(newLiveLevel , newLevelScene) , new CSLinked<UserConnection>(sender));
				
				}	
								
			}
			
			case UPDATE -> {
				
				UserConnection sender = connections.get(UserConnection.computeHashCode(packet.getAddress(), packet.getPort()));				
				sender.timer.start();
				sender.busy = false;
				byte[] controls;
				byte sequence;
				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)) {
					
					controls = coder.rControlStrokes();
					sequence = controls[0];
					
				}
				
				if(sequence != sender.entity.updateSequence()) {
					
					System.out.println("Possible Packet Loss, packed named: " + sequence + ", current sequence, " + sender.entity.updateSequence());
					 					
				}
				
//				if(sequence != 0 && sequence < sender.entity.advanceUpdateSequence()) { 
//					
//					sender.entity.inSync = false;
//					
//				} else sender.entity.inSync = true;
				
				/*
				 * Sets the server's view of the keystrokes sender made last 
				 */		
				sender.entity.controlsState(controls);
				
				/*
				 * Update other players by finding the level the sender of this update packet is in by:
				 * 		creating a packet coder for a packet to send to other players,
				 		creating a datagram packet to send to each player,
				 		send it for each client other than the one who sent it.
				 * 
				 */
				loadedLevels.forEach((levelSceneTuple , connectionsTherein) -> {
					
					if(levelSceneTuple.getSecond().entities().has(sender.entity.networked())) {

						try(PacketCoder recipientCoder = new PacketCoder()) {
							
							byte packetName = sender.entity.updateSequence();
							sender.entity.advanceUpdateSequence();
							
							recipientCoder
								.bflag(UPDATE)
								.bconnectionID(sender.index)
								.bControlStrokes(packetName , controls)	
							;
							
							DatagramPacket updatePacket = new DatagramPacket(recipientCoder.get() , 0 , recipientCoder.position());
														
							connectionsTherein.forOnlyVals(conn -> conn.entity != sender.entity , (conn) -> TRY(() -> {
								
								updatePacket.setAddress(conn.address);
								updatePacket.setPort(conn.port);
								serverSocket.send(updatePacket);								
								
							}));
							
						}						
						
						return;
						
					}
					
				});
				
			}
			
			default -> {
				
				System.err.println("Invalid packet flag given: " + packet.getData()[offset]);
				System.err.println("Packet bytes: ");
				for(int i = 0 ; i < packet.getLength() ; i ++) System.err.println("" + packet.getData()[i]);
				
				throw new IllegalArgumentException();
				
			}
		
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
		
		connections.forEach((connectionHash , connection) -> TRY(() -> {
			
			sendReliable(data , hash + connectionHash , cooldownMillis , connection.address , connection.port);
			
		}));
		
	}
	
	public NetworkedEntities getNetworkedEntity(Entities E) {
		
		Set<Entry<Integer , UserConnection>> conns = connections.entrySet();
		for(var entry : conns) if(entry.getValue().entity.networked() == E) return entry.getValue().entity;		
		return null;
		
	}
	
	@Override public void instanceUpdate() {
		
		try {
			
			ReliableDatagram.tickLiveDatagrams(serverSocket);
			
			synchronized(packetsToHandle) {
				
				while(!packetsToHandle.empty()) {
					
					var packet = packetsToHandle.dequeue();
					handleFlaggedPacket(packet.getFirst() , packet.getSecond());
					
				}
				
			}			
			
			loadedLevels.forEach((levelSceneTuple , listOfConnections) -> {
				
				levelSceneTuple.getSecond().entities().entitySystems(
					() -> {
						
					} , () -> {
		
						//serverside physics simulation
						levelSceneTuple.getSecond().kinematics().process();
						TemporalExecutor.process();
										
					} , 
					//release server view of connection's keys
					() -> {
						
						listOfConnections.forEachVal(connection -> {
							
//							if(!connection.busy && connection.timer.getElapsedTimeMillis() > 17) { 
//								
//								System.err.print("LIKELY LOST PACKET FROM:" + connection.index);
//								System.err.println(" ELAPSED SINCE UPDATE: " + connection.timer.getElapsedTimeMillis());
//								
//							}
							connection.entity.unStrikeKeys();
							
						});
						
					}		
					
				);
								
			});
				
		} catch (IOException e) {
				
			e.printStackTrace();
				
		}			
		
	}
	
	@Override public Iterable<NetworkedEntities> managedConnections() {
				
		ArrayList<NetworkedEntities> entities = new ArrayList<>(connections.size());
		connections.forEach((hash , conn) -> entities.add(conn.entity));
		return entities;
		
	}
	
	@Override public void send(byte[] data) throws IOException {

		throw new UnsupportedOperationException("Servers must have a specified address to send a packet to.");
		
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
		boolean isAck = false;
		if(ReliableDatagram.isReliableDatagramPacket(packet)) {
			
			offset = 8;
			if(ReliableDatagram.isAcknowledgement(packet)) { 
				
				ReliableDatagram.acceptAcknowledgement(packet);
				isAck = true;
				
			} else ReliableDatagram.acknowledge(serverSocket, packet);
			
		}
		
		if(!isAck) {
			
			int offset2pointo = offset;			
			synchronized(packetsToHandle) {
				
				packetsToHandle.enqueue(new Tuple2<>(packet , offset2pointo));
				
			}
			
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

	@Override public void shutDown() { 
	
		chatUI.shutDown();
		
	}

	private class ServerUI extends UserInterface {
		
		private static int options = NK_WINDOW_MOVABLE|NK_WINDOW_BORDER|NK_WINDOW_TITLE;

		//toggle this with f11
		private ByteBuffer liveLevelDropdown = ALLOCATOR.bytes((byte) 0);
		
		private ByteBuffer freeze = ALLOCATOR.bytes((byte)0);
		private ByteBuffer renderDebug = ALLOCATOR.bytes((byte)0);
		private final DecimalFormat decimalFormatter = new DecimalFormat();
		
		private ReentrantLock ptrModLock = new ReentrantLock();
		
		public ServerUI() {
			
			super("Multiplayer Information", 1565 , 5 , 350 , 600 , options , options);

			decimalFormatter.setMaximumFractionDigits(1);
		
			layoutBody((frame) -> {

				ptrModLock.lock();
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Server Address:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , serverSocket.getLocalAddress().toString() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);
				
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Server Port:" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_MIDDLE);
				nk_text(context , "" + serverSocket.getLocalPort() , NK_TEXT_ALIGN_RIGHT|NK_TEXT_ALIGN_MIDDLE);					
				
				int symbol = toBool(liveLevelDropdown) ? NK_SYMBOL_TRIANGLE_DOWN : NK_SYMBOL_TRIANGLE_RIGHT;
				
				nk_layout_row_dynamic(context , 30 , 1);				
				if(nk_selectable_symbol_label(context , symbol , "Show Live Levels" , NK_TEXT_ALIGN_LEFT , liveLevelDropdown)) ;
				
				if(toBool(liveLevelDropdown)) {
					
					RefInt iter = new RefInt(0);
					
					loadedLevels.forEach((levelSceneTuple , listOfPlayers) -> {
						
						nk_layout_row_dynamic(context , 20 , 1);
						if(nk_button_label(context , "Show " + levelSceneTuple.getFirst().gameName())) {
							
							engine.schedule(() -> engine.setRenderScene(levelSceneTuple.getSecond()));
														
						}
						
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

				nk_layout_row_dynamic(context , 20 , 2);
				if(nk_checkbox_label(context , "Render Debug" , renderDebug)) engine.toggleRenderDebug(null);
				if(nk_checkbox_label(context , "Freeze Scene" , freeze)) {}

				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Performance" , NK_TEXT_ALIGN_CENTERED);
				
				nk_layout_row_dynamic(context , 20 , 3);
				nk_text(context , "FLS: " + Engine.framesLastSecond() , NK_TEXT_ALIGN_LEFT);
				nk_text(context , "IRLS: " + decimalFormatter.format(Engine.iterationRateLastSecond()) , NK_TEXT_ALIGN_LEFT);
					
				ptrModLock.unlock();
				
			});
			
			show = true;

		}

		public void toggleShow() {
			
			show = show ? false : true;
			
		}
			
	}
	
}

