package org.gluu.radius.server.impl;

import java.net.InetSocketAddress;

import org.gluu.radius.server.GluuRadiusServer;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusServer;

public class TinyRadiusServer extends RadiusServer {

	private GluuRadiusServer server;

	public TinyRadiusServer(GluuRadiusServer server) {

		this.server = server;
	}
		
	@Override
	public String getUserPassword(String username) {

		return null;
	}

	@Override
	public String getSharedSecret(InetSocketAddress client) {

		String clientip = client.getAddress().getHostAddress();
		return server.getSharedSecret(clientip);
	}


	@Override
	public RadiusPacket accessRequestReceived(AccessRequest request,InetSocketAddress client) {

		RadiusPacket response = null;
		TinyRadiusAccessRequestContext ctx = new TinyRadiusAccessRequestContext(request,client);
		if(server.processAccessRequest(ctx)) {
			response = new RadiusPacket(RadiusPacket.ACCESS_ACCEPT,request.getPacketIdentifier());
		}else {
			response = new RadiusPacket(RadiusPacket.ACCESS_REJECT,request.getPacketIdentifier());
		}
		copyProxyState(request,response);
		return response;
	}

	@Override
	public RadiusPacket accountingRequestReceived(AccountingRequest request,InetSocketAddress client) {

		RadiusPacket response = new RadiusPacket(RadiusPacket.ACCOUNTING_RESPONSE,request.getPacketIdentifier());
		TinyRadiusAccountingRequestContext ctx = new TinyRadiusAccountingRequestContext(request,client);
		server.processAccountingRequest(ctx);
		copyProxyState(request,response);
		return response;
	}
}