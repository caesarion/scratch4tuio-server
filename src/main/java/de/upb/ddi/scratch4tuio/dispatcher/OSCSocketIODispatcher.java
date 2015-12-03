package de.upb.ddi.scratch4tuio.dispatcher;

import java.net.DatagramPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

public class OSCSocketIODispatcher implements IOSCDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OSCSocketIODispatcher.class);

	// the standard port is 3333
	private int oscPort;
	private OSCDatagramPort oscDatagramPort;
	private SocketIOServer server;
	private boolean startedIOServer = false;
	private boolean connectedToOSC;

	public OSCSocketIODispatcher(int oscPort) {
		// initialize server
		this.oscPort = oscPort;
		Configuration config = new Configuration();
		config.setHostname("localhost");
		config.setPort(5000);

		server = new SocketIOServer(config);
		server.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				log.info("New client connected");
                log.debug(client.toString());
			}
		});
		server.addDisconnectListener(new DisconnectListener() {
			@Override
			public void onDisconnect(SocketIOClient client) {
				log.info("A client disconnected");
                log.debug(client.toString());
			}
		});
	}

	public void startSocketIOServer() {
		if (server != null && !startedIOServer) {
			server.start();
			startedIOServer = true;
			log.debug("Started socket.io server on localhost:5000");
		}
	}

	public void stopSocketIOServer() {
		if (server != null && startedIOServer) {
			server.stop();
			startedIOServer = false;
			log.debug("Stopped socket.io server");
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
			oscDatagramPort = new OSCDatagramPort(oscPort);
			oscDatagramPort.addIOSCDispatcher(this);
			oscDatagramPort.startListening();
			setConnectedToOSC(true);
		} catch (Exception e) {
			log.error("OSCDispatcher: failed to connect to port " + oscPort);
			setConnectedToOSC(false);
		}
	}

	/**
	 * The OSCDispatcher stops listening to OSC messages on the configured UDP
	 * port
	 */
	public void disconnect() {
		oscDatagramPort.stopListening();
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}

		oscDatagramPort.close();
		setConnectedToOSC(false);
		log.info("Disconnected UDP connection to OSC-Server");
	}

	public boolean isConnectedToOSC() {
		return connectedToOSC;
	}

	private void setConnectedToOSC(boolean connectedToOSC) {
		this.connectedToOSC = connectedToOSC;
	}


}
