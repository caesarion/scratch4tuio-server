package de.upb.ddi.scratch4tuio.dispatcher;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class OSCDatagramPort implements Runnable {

	private DatagramSocket socket;
	@SuppressWarnings("unused")
	private int port;
	private List<IOSCDispatcher> dispatchers = new ArrayList<IOSCDispatcher>();
	private boolean isListening;

	public void addIOSCDispatcher(IOSCDispatcher dispatcher) {
		dispatchers.add(dispatcher);
	}
	
	public OSCDatagramPort(int port) throws SocketException {
		socket = new DatagramSocket(port);
		this.port = port;
	}

	public void run() {
		//maximum UDP packet size
		
		while (isListening) {
			try {
				byte[] buffer = new byte[65536];
				DatagramPacket packet = new DatagramPacket(buffer, 65536);
				packet.setLength(65536);
				socket.receive(packet);
				int length = packet.getLength();
				byte[] packetBytes = new byte[packet.getLength()];
				System.arraycopy(packet.getData(),0,packetBytes,0,length);
				packet.setData(packetBytes, packet.getOffset(), length);
				packet.setLength(length);				
				for(IOSCDispatcher dispatcher : dispatchers) {
					dispatcher.dispatchOSCMessage(packet);
				}
			} catch (java.net.SocketException e) {
				if (isListening) e.printStackTrace();
			} catch (IOException e) {
				if (isListening) e.printStackTrace();
			} 
		}
	}
	

	public void startListening() {
		isListening = true;
		Thread thread = new Thread(this);
		thread.start();
	}

	public void stopListening() {
		isListening = false;
	}	

	public boolean isListening() {
		return isListening;
	}	
	
	protected void finalize() throws Throwable {
		super.finalize();
		socket.close();
	}
	
	public void close() {
		socket.close();
	}	
	
}