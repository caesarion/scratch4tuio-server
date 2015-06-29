package scratch4Tuio.server;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import TUIO.TuioBlob;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

public class TuioServerApp implements OSCListener {
	private int port = 3333;
	private OSCPortIn oscPort;
	private TuioTime currentTime;
	private SocketIOServer server;
	private boolean startedIOServer = false;
	private boolean connectedToOSC = false;

	public TuioServerApp() {

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

	@Override
	public void acceptMessage(Date time, OSCMessage message) {
		String separator = ",";
		String relayMessage = "#bundle" + separator + time.getTime()  + ".0"+ separator;
		Object[] args = message.getArguments();
		String command = (String) args[0];
		String address = message.getAddress();

		if (address.equals("/tuio/2Dobj")) {
			relayMessage += "/tuio/2Dobj" + separator;
			if (command.equals("set")) {
				relayMessage += "set" + separator;
				
				long s_id = ((Integer) args[1]).longValue();
				int c_id = ((Integer) args[2]).intValue();				
				
				relayMessage += s_id + separator;
				relayMessage += c_id + separator;
				for (int i = 3; i <= 10; i++) {
					relayMessage += ((Float)args[i]).floatValue() + separator;
				}
			} else if (command.equals("alive")) {
				relayMessage += "alive" + separator;
				for (int i = 1; i < args.length; i++) {
					// get the message content
					long s_id = ((Integer) args[i]).longValue();
					relayMessage += s_id + separator;
				}
			} else if (command.equals("fseq")) {

				long fseq = ((Integer) args[1]).longValue();
				relayMessage += "fseq" + separator + fseq;
			}
		} else if (address.equals("/tuio/2Dcur")) {
			relayMessage += "/tuio/2Dcur" + separator;
			if (command.equals("set")) {
				relayMessage += "set" + separator;
				long s_id = ((Integer)args[1]).longValue();
				relayMessage += s_id + separator;
				for (int i = 2; i <= 6; i++) {
					relayMessage += ((Float)args[i]).floatValue() + separator;
				}
			} else if (command.equals("alive")) {
				relayMessage += "alive" + separator;
				for (int i = 1; i < args.length; i++) {
					// get the message content
					long s_id = ((Integer) args[i]).longValue();
					relayMessage += s_id + separator;
				}

			} else if (command.equals("fseq")) {
				long fseq = ((Integer) args[1]).longValue();
				relayMessage += "fseq" + separator + fseq;
			}
		}
		for (SocketIOClient client : server.getAllClients()) {
			System.out.println("send Message: " + relayMessage);
			client.sendEvent("osc", relayMessage);
		}
	}

	/**
	 * The TuioServerApp starts listening to OSC messages on the configured UDP
	 * port
	 */
	public void connect() {
		TuioTime.initSession();
		currentTime = new TuioTime();
		currentTime.reset();
		try {
			oscPort = new OSCPortIn(port);
			oscPort.addListener("/tuio/2Dobj", this);
			oscPort.addListener("/tuio/2Dcur", this);
			oscPort.addListener("/tuio/2Dblb", this);
			oscPort.startListening();
			connectedToOSC = true;
		} catch (Exception e) {
			System.out.println("TuioServerApp: failed to connect to port "
					+ port);
			connectedToOSC = false;
		}
	}

	/**
	 * The TuioServerApp stops listening to OSC messages on the configured UDP
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
		connectedToOSC = false;
	}

	public static void main(String[] args) throws InterruptedException {
		TuioServerApp app = new TuioServerApp();
		app.startSocketIOServer();
		app.connect();
	}
}
