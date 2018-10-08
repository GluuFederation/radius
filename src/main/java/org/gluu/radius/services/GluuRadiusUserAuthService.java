package org.gluu.radius.services;

import org.gluu.radius.auth.GluuRadiusUserAuthResult;

public interface GluuRadiusUserAuthService {
	public GluuRadiusUserAuthResult authenticateUser(String username,String password);
}