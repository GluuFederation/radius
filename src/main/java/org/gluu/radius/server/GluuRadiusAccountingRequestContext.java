package org.gluu.radius.server;


public interface GluuRadiusAccountingRequestContext extends GluuRadiusRequestContext {
	public String getUsername();
	public GluuRadiusAccountingStatusType getAccountingStatusType();
}