package org.gluu.radius.auth;

import org.apache.log4j.Logger;

import org.gluu.radius.server.GluuRadiusAccessRequestContext;
import org.gluu.radius.server.GluuRadiusAccessRequestFilter;
import org.gluu.radius.services.GluuRadiusKnownService;
import org.gluu.radius.services.GluuRadiusUserAuthService;
import org.gluu.radius.services.GluuRadiusServiceException;
import org.gluu.radius.services.GluuRadiusServiceLocator;


public class GluuRadiusOpenIdAccessRequestFilter implements GluuRadiusAccessRequestFilter {

	private static final Logger logger = Logger.getLogger(GluuRadiusOpenIdAccessRequestFilter.class);

	public GluuRadiusOpenIdAccessRequestFilter() {

	}


	@Override
	public boolean processAccessRequest(GluuRadiusAccessRequestContext context) {

		try {
			
			GluuRadiusUserAuthService userauthservice = getUserAuthService();
			if(userauthservice == null) {
				logger.debug("Could not obtain a GluuRadiusUserAuthService instance. Check service registration");
				return false;
			}

			String username = context.getUsername();
			String password = context.getPassword();
			logger.debug("Authenticating user " + username);
			GluuRadiusUserAuthResult authresult = userauthservice.authenticateUser(username,password);
			if(!authresult.isSuccess()) {
				logger.debug("Authentication failed for user " + username+". Error: " +authresult.getErrorDescription());
				return false;
			}
			logger.debug("User " + username + " authenticated successfully");
			
			return true;
		}catch(GluuRadiusServiceException e) {
			logger.debug("An exception occured when attempting to authenticate user",e);
		}
		return false;
	}


	private GluuRadiusUserAuthService getUserAuthService() {

		return GluuRadiusServiceLocator.getService(GluuRadiusKnownService.UserAuthService);
	}
}