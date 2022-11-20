package Networking.UserHostedServer;

import java.net.InetAddress;
import Networking.NetworkedEntities;

import CSUtil.Timer;

public class UserConnection {

	public static final int computeHashCode(InetAddress addr , int port) { 
		
		return addr.hashCode() + port;
		
	}
	
	public final short index;
	public final int port;
	public final InetAddress address;
	public final Timer timer;
	public final NetworkedEntities entity;
	//this boolean means we are not expecting a packet from this client because they are doing something which could
	//cause their frame rate to drop such as loading a new level
	volatile boolean busy = true;
	
	public UserConnection(short index , int port , InetAddress address , NetworkedEntities entity) {
		
		this.index = index;
		this.port = port;
		this.address = address;
		this.entity = entity;
		this.timer = new Timer();
		
	}
	
	public int hashCode() {
		
		return computeHashCode(address , port);
		
	}
	
	public String toString() {
		
		return "Connection: " + address.toString() + ":" + port;
		
	}
	
}