package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class CSSocket {
	
	DatagramSocket socket;
	InetAddress thisAddress;
	DatagramPacket sendingPacket = null;
	DatagramPacket receivingPacket = null;
	final int port;
		
	public CSSocket(int port){
		
		this.port = port;
		
		try {
			
			thisAddress = InetAddress.getLocalHost();
			socket = new DatagramSocket();
			
		} catch(IOException e) {}
				
	}
	
	public void connect(InetSocketAddress connectTo) throws SocketException {
		
		socket.connect(connectTo);	
		if(socket.isConnected()) { 
			
			System.out.println("Connected to " + connectTo.toString());
			sendingPacket = new DatagramPacket(new byte [100] , 0 , 100 , connectTo);
			
		} else System.out.println("Failed to connect to " + connectTo.toString());
		
	}
	
	public void send() {
		
		try {
			
			socket.send(sendingPacket);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public void receive() {
		
		try {
			
			socket.receive(receivingPacket);
			for(byte b : receivingPacket.getData()) System.out.println(b);
						
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
	public void shutDown() {
		
		socket.close();
		
	}
	
}
