package Networking.UserHostedServer;

import static Networking.Utils.DatagramPacketUtils.*;

import static CSUtil.BigMixin.TRY;
import static CSUtil.BigMixin.toBool;
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
import static org.lwjgl.nuklear.Nuklear.nk_slider_int;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import CS.COLDSTEEL;
import CS.Engine;
import CS.UserInterface;
import CSUtil.RefInt;
import CSUtil.Timer;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSQueue;
import CSUtil.DataStructures.CSStack;
import CSUtil.DataStructures.LinkedRingBuffer;
import CSUtil.DataStructures.RingBuffer;
import CSUtil.DataStructures.Tuple2;
import CSUtil.DataStructures.cdNode;
import CSUtil.Dialogs.Debug.NetworkedEntityPreviousFrameViewer;
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
import Networking.Utils.PacketLoser;

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
	
	private final Engine engine;
	private final ServerUI ui;
	private final MultiplayerChatUI chatUI;
	
	public final String serverName;
	
	//holds data about all players in the server
	private volatile ConcurrentHashMap<Integer , UserConnection> connections = new ConcurrentHashMap<>(17);
	//holds references to levels that have previously been loaded. If all players leave a a level, we keep it in memory and once
	//another player enters it, we redeploy the level
	private volatile ConcurrentHashMap<Tuple2<Levels , Scene> , CSLinked<UserConnection>> liveLevels = new ConcurrentHashMap<>(4);
	
	private volatile CSQueue<Tuple2<DatagramPacket , Integer>> packetsToHandle = new CSQueue<>();
	private volatile RingBuffer<Long> previousPacketChecksums = new RingBuffer<Long>(25);
	
	private PacketLoser packetLoser = new PacketLoser();
	private final NetworkedEntityPreviousFrameViewer previousFrameViewer = new NetworkedEntityPreviousFrameViewer(connections , 5 , 5);
	
	private volatile short nextConnectionID = 0;
	private volatile boolean running = false;	
	
	private final Thread serverListeningThread = new Thread(() -> {
			
		while(true) TRY(() -> listen());
				
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
	
	public UserHostedServer(Engine engine , String serverName) {
		
		this.engine = engine;
		this.serverName = serverName;
		
		createServerFile(serverName);
		
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
		
		log("Server Launched");
		
	}
	
	private void handleFlaggedPacket(DatagramPacket packet , int offset) {
		
		switch(packet.getData()[offset]) {
		
			case PRE_CONNECT -> {

				System.out.println("Preconnecting new user.");
				
				short connectionID = nextConnectionID++;
				UserConnection newClient = new UserConnection(connectionID , packet.getPort() , packet.getAddress());
				
				try(PacketCoder response = new PacketCoder()) {
					
					response
						.bflag(PRE_CONNECT)
						.bconnectionID(connectionID)						
					;
					
					TRY(() -> sendReliable(response.get() , 1308513902 , 1000 , newClient.address , newClient.port));
					
				}

				connections.put(UserConnection.computeHashCode(packet.getAddress() , packet.getPort()) , newClient);
				System.out.println("Finished preconnecting new user.");
				
			}
		
			case CHANGE_CONNECTION -> {
				
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
				//initialize resourcse
				try(PacketCoder entrantCoder = new PacketCoder(packet.getData() , offset + 1) ; 
					PacketCoder toOthersInside = new PacketCoder() ;
					PacketCoder toOthersOutside = new PacketCoder() ;
					PacketCoder toEntrant = new PacketCoder() ;
				) {
					
					UserConnection sender = connections.get(UserConnection.computeHashCode(packet.getAddress(), packet.getPort()));
					
					String sendersEntityName = entrantCoder.rstring() + ".CStf";
					float[] sendersPos = entrantCoder.rposition();
					String sendersLevel = entrantCoder.rstring();
					
					//first, determine if the sender's level is already present.
					Tuple2<Tuple2<Levels , Scene> , CSLinked<UserConnection>> operantLevelSceneAndPlayers = findLevelSceneAndPlayersByString(sendersLevel);
					NetworkedEntities sendersEntity = sender.entity;
					
					//toEntrant contains ids of players and their entity names. 
					//If a player is in the same level as the sender, their position is included too.
					toEntrant
						.bflag(CHANGE_CONNECTION)
						.brepitition((short) (connections.size() - 1) , (byte) 2);
					;
					
					connections.forEach((hash , conn) -> {
						
						if(conn == sender || conn.entity.networked() == null) return;
						
						toEntrant
							.bconnectionID(conn.index)
							.bstring(conn.entity.networked().name())
						;
						
					});

					//message to players inside the same level as the sender
					toOthersInside
						.bflag(CHANGE_CONNECTION)
						.bconnectionID(sender.index)
						.bstring(sendersEntityName)
						.bposition(sendersPos)
					;

					//message to players in other levels
					toOthersOutside
						.bflag(CLIENT_CONNECTED)
						.bconnectionID(sender.index)
						.bstring(sendersEntityName)
					;
					
					//adds the new player to a level that is already in play, or adds them a new level if no one was in the level the new player is in
					if(operantLevelSceneAndPlayers == null) operantLevelSceneAndPlayers = newLiveLevel(sendersLevel);
					
					//finally create the entity for the sender
					sendersEntity.networked(new Entities(operantLevelSceneAndPlayers.getFirst().getSecond() , sendersEntityName) , false);
					//add the sender to a level scene
					addConnectionToLiveLevel(sender , operantLevelSceneAndPlayers);	
					
					//iterate over the list of players in the same level as the sender
					if(operantLevelSceneAndPlayers.getSecond().size() > 1) {

						toEntrant.brepitition((short) (operantLevelSceneAndPlayers.getSecond().size() - 1) , (byte) 2);
						
						operantLevelSceneAndPlayers.getSecond().forEachVal(conn -> {
							
							//if the current iteration is a player other than the sender
							if(conn != sender) {
								
								//append the information of the iteration's user connection to the message that will eventually get sent to the sender
								toEntrant								
									.bconnectionID(conn.index)
									.bposition(conn.entity.networked().getMidpoint())
								;
								
								//send the message to the players inside the level
								TRY(() -> sendReliable(toOthersInside.get() , CLIENT_CONNECTED + conn.index , 1000 , conn.address , conn.port));
							
							}
							
						});				
											
					}
					
					//for all levels other than the one the sender is in, send the appropriate message
					forAllLiveLevelsBut(operantLevelSceneAndPlayers.getFirst() , entry -> entry.getValue().forEachVal((conn) -> TRY(() -> {
						
						sendReliable(toOthersOutside.get() , CLIENT_CONNECTED + conn.index , 1000 , conn.address , conn.port);
					
					})));
								
					//some initialization for the newly created entity. we need to run its script so some data structures specified in the script
					//which the server needs can be generated.
					sender.entity.networked().moveTo(sendersPos[0] , sendersPos[1]);
					sender.entity.runScript();					
					
					TRY(() -> sendReliable(toEntrant.get() , CHANGE_CONNECTION + sender.index , 1000 , packet.getAddress() , packet.getPort()));
					
					System.out.println("Finished connecting player: " + sendersEntityName);
								
				}
				
			}
			
			case CLIENT_CONNECTED -> {}
			case CLIENT_DISCONNECTED -> {}
			case CHAT_MESSAGE -> {
				
				PacketCoder coder = new PacketCoder(packet.getData() , offset + 1);
				String received = coder.rstring(); 
								
				byte[] chatData = ByteArrayUtils.compose(new byte[] {CHAT_MESSAGE} , received.getBytes());
				
				connections.forEach((hash , connection) -> {
					
					if(UserConnection.computeHashCode(packet.getAddress(), packet.getPort()) != hash) TRY(() -> sendReliable(chatData , 9349491 , 1500));
					
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
				
				Set<Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>>> levels = liveLevels.entrySet();
					
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
					
					var newLiveArea = newLiveLevel(levelEntered);
					sender.entity.networked().moveTo(posInNewLevel[0] , posInNewLevel[1]);
					addConnectionToLiveLevel(sender , newLiveArea);
					
				}	
								
			}
			
			case UPDATE -> {
				
				UserConnection sender = connections.get(UserConnection.computeHashCode(packet.getAddress(), packet.getPort()));
				
				Scene sendersScene = getSceneByUserConnection(sender);

				byte serversViewOfUpdateNumber = sender.entity.getUpdateNumber();
				LinkedRingBuffer<byte[]> previousInputs = sender.entity.previousInputs();
				
				sender.timer.start();
				sender.busy = false;
				byte sequence;
				
				try(PacketCoder coder = new PacketCoder(packet.getData() , offset + 1)) {
					
					sequence = coder.rControlStrokes(previousInputs);
					
					byte[] currentControls = previousInputs.getAndPut();
					sender.entity.updateControlStateView(currentControls);

					if(sequence != serversViewOfUpdateNumber) {
						
						int dropped = sender.entity.getNumberLostPackets(sequence);
						
						System.out.println("Packet loss detected, received: " + sequence + ", expecting: " + serversViewOfUpdateNumber);
						System.out.println("Dropped " + dropped + " packets.");

						//simulates the lost frames
						if(dropped < previousInputs.capacity) {
							
							CSStack<byte[]> previousInputsStack = new CSStack<byte[]>();
							int iter = 0;
							while(iter < dropped) {
								
								previousInputsStack.push(previousInputs.get());
								iter++;
								
							}
										
							sender.entity.rewindStateToCapture();
							
							while(!previousInputsStack.empty()) {
								
								sender.entity.updateControlStateView(previousInputsStack.pop());
								sendersScene.entities().internalRunInitialSystems(sender.entity.networked());
								
								//serverside physics simulation
								sendersScene.kinematics().process(sender.entity.networked());
								
								sendersScene.entities().internalRunFinalSystems(sender.entity.networked());
							
							}

						}	
							
					} 
										
					sender.entity.setUpdateNumber((byte) (++sequence));
					sender.entity.isReady = true;

				}
				
				/*
				 * Update other players by finding the level the sender of this update packet is in by:
				 * 		creating a packet coder for a packet to send to other players,
				 		creating a datagram packet to send to each player,
				 		send it for each client other than the one who sent it.
				 * 
				 */
				byte seq = sequence;
				liveLevels.forEach((levelSceneTuple , connectionsTherein) -> {
					
					if(levelSceneTuple.getSecond().entities().has(sender.entity.networked())) {

						try(PacketCoder recipientCoder = new PacketCoder()) {
							
							recipientCoder
								.bflag(UPDATE)
								.bconnectionID(sender.index)
								.bControlStrokes(seq , sender.entity.previousInputs())	
							;
							
							DatagramPacket updatePacket = new DatagramPacket(recipientCoder.get() , 0 , recipientCoder.position());
														
							connectionsTherein.forOnlyVals(conn -> conn.entity != sender.entity , (conn) -> TRY(() -> {
								
								updatePacket.setAddress(conn.address);
								updatePacket.setPort(conn.port);
								serverSocket.send(updatePacket);								
								
							}));
							
						}						
						
					}
					
				});
				
			}
			
		}
		
	}

	private Tuple2<Tuple2<Levels , Scene> , CSLinked<UserConnection>> newLiveLevel(String level) {
		
		//same procedure as when a player enters a level for the first time
		Scene newLevelScene = new Scene(engine);
		newLevelScene.entityScriptingInterface().server(this);
		Levels newLiveLevel = new Levels(newLevelScene , (CharSequence)(COLDSTEEL.data + "macrolevels/" + level));						
		newLevelScene.entities().setNetworkInstance(this);
		newLiveLevel.deploy(newLevelScene);
		Tuple2<Levels , Scene> newTuple = new Tuple2<>(newLiveLevel , newLevelScene);
		CSLinked<UserConnection> newListOfPlayers = new CSLinked<UserConnection>();
		liveLevels.put(newTuple, newListOfPlayers);		
		return new Tuple2<Tuple2<Levels , Scene> , CSLinked<UserConnection>>(newTuple , newListOfPlayers);
		
	}
	
	private Scene getSceneByUserConnection(UserConnection connection) {

		Set<Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>>> entries = liveLevels.entrySet();
		for(Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>> x : entries) if(x.getValue().has(connection)) return x.getKey().getSecond();
		return null;
		
	}	
	
	private Tuple2<Tuple2<Levels , Scene> , CSLinked<UserConnection>> findLevelSceneAndPlayersByString(String sendersLevel) {
		
		Set<Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>>> levels = liveLevels.entrySet();
		
		String longName;
		for(Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>> x : levels) {
			
			longName = x.getKey().getFirst().macroLevel() + "/" + x.getKey().getFirst().gameName() + ".CStf";			
			if(longName.equals(sendersLevel)) return new Tuple2<>(x.getKey() , x.getValue());
			
		}
		
		return null;
		
	}
	
	private void forAllLiveLevelsBut(Tuple2<Levels , Scene> ignoreThis , Consumer<Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>>> callback) {

		Set<Entry<Tuple2<Levels , Scene> , CSLinked<UserConnection>>> levels = liveLevels.entrySet();
		for(var x : levels) if(x.getKey() != ignoreThis) callback.accept(x);
		
		
	}	
	
	private void addConnectionToLiveLevel(UserConnection connection , Tuple2<Tuple2<Levels , Scene> , CSLinked<UserConnection>> liveLevel) {
		
		liveLevel.getFirst().getSecond().entities().add(connection.entity.networked());
		((EntityScripts)connection.entity.networked().components()[Entities.SOFF]).resetLib(liveLevel.getFirst().getSecond().entityScriptingInterface());
		liveLevel.getSecond().add(connection);		
		
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
	
	public void createServerFile(String serverName) {
		
		if(serverName == null) serverName = "null";
		
		File[] servers = new File(CS.COLDSTEEL.data + "servers/").listFiles();
		for(File x : servers) if(x.getName().equals(serverName)) return;
		String name = serverName;
		TRY(() -> {
			
			Files.createDirectory(Paths.get(CS.COLDSTEEL.data + "servers/" + name));
			Files.createDirectory(Paths.get(CS.COLDSTEEL.data + "servers/" + name + "/saves/"));
			
		});		
		
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
						
			liveLevels.forEach((levelSceneTuple , listOfConnections) -> {
							
				levelSceneTuple.getSecond().entities().entitySystems(entity -> {
					
						NetworkedEntities asNetworked = NetworkedInstance.getNetworkedEntityForEntity(this , entity);
						return asNetworked != null && asNetworked.isReady;
					
					} ,
					() -> {} , () -> {
		
						//serverside physics simulation
						levelSceneTuple.getSecond().kinematics().process();
						TemporalExecutor.process();
						
					} , 
					//release server view of connection's keys
					() -> {}
					
				);

				levelSceneTuple.getSecond().entities().forEach(entity -> {

					NetworkedEntities asNetworked = NetworkedInstance.getNetworkedEntityForEntity(this , entity);
					if(asNetworked != null) {
						
						asNetworked.isReady = false;
						asNetworked.captureSyncedVariables();
						
					}					
					
				});
				
			});
			
			connections.forEach((hash , conn) -> conn.entity.isReady = false);
				
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
		
		DatagramPacket packet = new DatagramPacket(new byte[512] , 512);
		serverSocket.receive(packet);
		
		if(packetLoser.receive(packet) == null) return;
		
		if(handleChecksum(checksum(packet) , previousPacketChecksums)) {
			
//			System.out.println("RECEIVED IDENTICAL PACKET: " + flagAsString(packet));
			return;
		
		}
		
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
			
		}}

	@Override public boolean host() {

		return false;
		
	}

	@Override public boolean client() {

		return false;
		
	}

	@Override public void toggleUI() {

		ui.toggleShow();
		chatUI.toggle();
		previousFrameViewer.toggle();
		
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
		private IntBuffer packetLossSlider = ALLOCATOR.ints(-1);
		private final DecimalFormat decimalFormatter = new DecimalFormat();
		
		public ServerUI() {
			
			super("Multiplayer Information", 1565 , 5 , 350 , 600 , options , options);

			decimalFormatter.setMaximumFractionDigits(1);
		
			layoutBody((frame) -> {

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
					
					liveLevels.forEach((levelSceneTuple , listOfPlayers) -> {
						
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
					
				nk_layout_row_dynamic(context , 20 , 2);
				nk_text(context , "Packet Loss Rate:" , NK_TEXT_ALIGN_LEFT);
				nk_text(context , "" + packetLoser.rate , NK_TEXT_ALIGN_RIGHT);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_slider_int(context , -1 , packetLossSlider , 100 , 1);
				
				packetLoser.rate = packetLossSlider.get(0);
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_label(context , "Print Previous Packet Checksums")) {
					
					previousPacketChecksums.forEach(System.out::println);
					
				}								
				
			});
			
		}

		public void toggleShow() {
			
			show = show ? false : true;
			
		}
			
	}
	
}

