package org.gluu.radius.server.impl.tinyradius;

import java.net.InetSocketAddress;
import org.tinyradius.packet.AccessRequest;
import org.gluu.radius.server.GluuRadiusAccessRequestContext;
import org.gluu.radius.server.GluuRadiusServerException;

public class TinyRadiusAccessRequestContext extends TinyRadiusRequestContext implements GluuRadiusAccessRequestContext {
	

	public TinyRadiusAccessRequestContext(AccessRequest request,InetSocketAddress client) {
		super(request,client);
	}

	@Override
	public String getUsername() {

		try {
			AccessRequest req = (AccessRequest) packet;
			return req.getUserName();
		}catch(RuntimeException re)  {
			throw new GluuRadiusServerException("Error getting username in radius access request",re);
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