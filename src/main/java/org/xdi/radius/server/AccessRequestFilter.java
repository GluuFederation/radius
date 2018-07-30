package org.xdi.radius.server;

public interface AccessRequestFilter {
	public Response processAccessRequest(AccessRequestContext context);
}