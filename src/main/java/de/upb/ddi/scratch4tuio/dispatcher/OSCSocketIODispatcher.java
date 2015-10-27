package de.upb.ddi.scratch4tuio.dispatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

public class OSCSocketIODispatcher implements IOSCDispatcher {

    public static final int DEFAULT_PORT = 3333;

	private static final Logger log = LoggerFactory.getLogger("OSCSocketIODispatcher");

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
				log.info("client connected");
			}
		});
		server.addDisconnectListener(new DisconnectListener() {

			@Override
			public void onDisconnect(SocketIOClient client) {
				log.info("client disconnected");
			}
		});
	}

	public void startSocketIOServer() {
		if (server != null && !startedIOServer) {
			server.start();
			startedIOServer = true;
			log.debug("Started socket.io server");
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
		;
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

	private static int parsePort(String... args) {
		if (!(args.length == 2 && (args[0].equals("-p") || args[0].equals("--port")))) {
			// log.error("Mailformed program arguments");
			return DEFAULT_PORT;
		}
		try {
			return Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			log.error("The port argument -p is not of type int!");
			return -1;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		log.info("Try Starting OSC Dispatcher");
		int oscPort = parsePort(args);
		if (oscPort < 0) {
			log.error("program arguments not valid. Shutdown server.");
			return;
		}
		OSCSocketIODispatcher oscdispatcher = new OSCSocketIODispatcher(oscPort);
		oscdispatcher.startSocketIOServer();
		oscdispatcher.connect();
		log.info("OSC Dispatcher Running..");
		try {
			while(true) {
				Thread.sleep(100);
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String userCommand = reader.readLine();
				if(userCommand.equals("restart")) {
					log.info("Restarting OSC Dispatcher...");
					oscdispatcher.disconnect();
					oscdispatcher.stopSocketIOServer();
					Thread.sleep(100);
					oscdispatcher = new OSCSocketIODispatcher(oscPort);
					oscdispatcher.startSocketIOServer();
					oscdispatcher.connect();
					log.info("OSC Dispatcher Restarted and Running..");
				}
			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			oscdispatcher.disconnect();
			oscdispatcher.stopSocketIOServer();
			e.printStackTrace();
		}
	}
}
