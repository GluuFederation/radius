package org.gluu.radius.server;


public interface GluuRadiusRequestContext {
	
	public String getClientIpAddress();
	public String getAttributeValue(String name);
}