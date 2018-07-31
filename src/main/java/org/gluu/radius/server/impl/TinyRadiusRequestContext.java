package org.gluu.radius.server.impl;

import java.net.InetSocketAddress;

import org.gluu.radius.server.RequestContext;
import org.tinyradius.packet.RadiusPacket;


public class TinyRadiusRequestContext implements RequestContext {

	private  InetSocketAddress client; 
	protected RadiusPacket packet;

	public TinyRadiusRequestContext(RadiusPacket packet,InetSocketAddress client) {

		this.packet = packet;
	}


	public String getClientIpAddress() {

		return client.getAddress().getHostAddress();
	}


	public String getAttributeValue(String attributename) {

		return packet.getAttributeValue(attributename);
	}
} 