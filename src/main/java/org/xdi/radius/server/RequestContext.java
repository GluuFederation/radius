package org.xdi.radius.server;


public interface RequestContext {
	
	public String getClientIpAddress();
	public String getUserName();
	public String getAttributeValue(String name);
}