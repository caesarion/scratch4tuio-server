package de.upb.ddi.scratch4tuio.dispatcher;

import java.net.DatagramPacket;

public interface IOSCDispatcher {

	public void dispatchOSCMessage(DatagramPacket packet);
}
