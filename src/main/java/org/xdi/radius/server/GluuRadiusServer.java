package org.xdi.radius.server;

import java.net.InetSocketAddress;

import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusServer;

public class GluuRadiusServer {
	

	private class TinyRadiusServerWrapper extends RadiusServer {
		
		@Override
		public String getUserPassword(String username) {

			return null;
		}

		@Override
		public String getSharedSecret(InetSocketAddress client) {

			return null;
		}


		@Override
		public RadiusPacket accessRequestReceived(AccessRequest request,InetSocketAddress client) {

			//TODO implement this
			return null;
		}

		@Override
		public RadiusPacket accountingRequestReceived(AccountingRequest request,InetSocketAddress client) {

			//TODO implement this
			return null;
		}
	}

	
	private TinyRadiusServerWrapper trserver;



	
}