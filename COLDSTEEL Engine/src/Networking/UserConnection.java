package Networking;

import java.net.InetAddress;

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
	
	public UserConnection(short index , int port , InetAddress address , Timer timer , NetworkedEntities entity) {
		
		this.index = index;
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