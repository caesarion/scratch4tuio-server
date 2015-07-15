package OSCDispatching;

import java.net.DatagramPacket;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

public class OSCSocketIODispatcher implements IOSCDispatcher {
	private int port = 3333;
	private OSCDatagramPort oscPort;
	private SocketIOServer server;
	private boolean startedIOServer = false;
	private boolean connectedToOSC;

	public OSCSocketIODispatcher() {
		// initialize server
		Configuration config = new Configuration();
		config.setHostname("localhost");
		config.setPort(5000);

		server = new SocketIOServer(config);
		server.addConnectListener(new ConnectListener() {

			@Override
			public void onConnect(SocketIOClient client) {				
				System.out.println("client connected");
			}
		});
		server.addDisconnectListener(new DisconnectListener() {

			@Override
			public void onDisconnect(SocketIOClient client) {
				System.out.println("client disconnected");
			}
		});
	}

	public void startSocketIOServer() {
		if (server != null && !startedIOServer) {
			server.start();
			startedIOServer = true;
			System.out.println("Started socket.io server");
		}
	}

	public void stopSocketIOServer() {
		if (server != null && startedIOServer) {
			server.stop();
			startedIOServer = false;
			System.out.println("Stopped socket.io server");
		}
	}

	public void dispatchOSCMessage(DatagramPacket packet) {
		for (SocketIOClient client : server.getAllClients()) {
			client.sendEvent("osc", packet);
		}
	}

	/**
	 * The OSCDispatcher starts listening to OSC messages on the configured UDP
	 * port
	 */
	public void connect() {
		try {
			oscPort = new OSCDatagramPort(port);
			oscPort.addIOSCDispatcher(this);
			oscPort.startListening();
			setConnectedToOSC(true);
		} catch (Exception e) {
			System.out.println("OSCDispatcher: failed to connect to port " + port);
			setConnectedToOSC(false);
		}
	}

	/**
	 * The OSCDispatcher stops listening to OSC messages on the configured UDP
	 * port
	 */
	public void disconnect() {
		oscPort.stopListening();
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		;
		oscPort.close();
		setConnectedToOSC(false);
	}

	public boolean isConnectedToOSC() {
		return connectedToOSC;
	}

	private void setConnectedToOSC(boolean connectedToOSC) {
		this.connectedToOSC = connectedToOSC;
	}

	public static void main(String[] args) throws InterruptedException {
		OSCSocketIODispatcher oscdispatcher = new OSCSocketIODispatcher();
		oscdispatcher.startSocketIOServer();
		oscdispatcher.connect();
	}
}
