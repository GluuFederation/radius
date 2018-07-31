package org.gluu.radius.server;


public interface AccessRequestContext extends RequestContext {
	
	public String getPassword();
	public String getAuthProtocol();
}