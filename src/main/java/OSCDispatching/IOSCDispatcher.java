package OSCDispatching;

import java.net.DatagramPacket;

public interface IOSCDispatcher {

	public void dispatchOSCMessage(DatagramPacket packet);
}
