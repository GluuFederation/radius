package org.gluu.radius.server;

public interface GluuRadiusAccessRequestFilter {
	public boolean processAccessRequest(GluuRadiusAccessRequestContext context);
}