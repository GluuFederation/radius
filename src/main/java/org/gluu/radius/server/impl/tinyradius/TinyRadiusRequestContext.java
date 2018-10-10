package org.gluu.radius.server.impl.tinyradius;

import java.net.InetSocketAddress;

import org.gluu.radius.server.GluuRadiusRequestContext;
import org.tinyradius.packet.RadiusPacket;


public class TinyRadiusRequestContext implements GluuRadiusRequestContext {

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