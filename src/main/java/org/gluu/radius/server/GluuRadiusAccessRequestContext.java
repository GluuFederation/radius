package org.gluu.radius.server;


public interface GluuRadiusAccessRequestContext extends GluuRadiusRequestContext {
	
	public String getUsername();
	public String getPassword();
	public String getAuthProtocol();
}