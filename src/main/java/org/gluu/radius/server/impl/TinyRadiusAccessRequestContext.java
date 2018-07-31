package org.gluu.radius.server.impl;

import java.net.InetSocketAddress;
import org.tinyradius.packet.AccessRequest;
import org.gluu.radius.server.AccessRequestContext;
import org.gluu.radius.server.GluuRadiusException;

public class TinyRadiusAccessRequestContext extends TinyRadiusRequestContext implements AccessRequestContext {
	

	public TinyRadiusAccessRequestContext(AccessRequest request,InetSocketAddress client) {
		super(request,client);
	}

	@Override
	public String getUsername() {

		try {
			AccessRequest req = (AccessRequest) packet;
			return req.getUserName();
		}catch(RuntimeException re)  {
			throw new GluuRadiusException("Error getting username in radius access request",re);
		}
	}

	@Override
	public String getPassword() {

		AccessRequest req = (AccessRequest) packet;
		return req.getUserPassword();
	}

	@Override
	public String getAuthProtocol() {

		AccessRequest req = (AccessRequest) packet;
		return req.getAuthProtocol();
	}
}