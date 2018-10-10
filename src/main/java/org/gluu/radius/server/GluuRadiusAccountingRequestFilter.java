package org.gluu.radius.server;

public interface GluuRadiusAccountingRequestFilter {
	public void processAccountingRequest(GluuRadiusAccountingRequestContext context);
}