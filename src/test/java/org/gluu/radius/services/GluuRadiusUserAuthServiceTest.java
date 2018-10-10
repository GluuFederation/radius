package org.gluu.radius.services;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gluu.radius.auth.GluuRadiusUserAuthResult;
import org.gluu.radius.config.GluuRadiusOpenIdConfig;
import org.gluu.radius.services.impl.GluuRadiusUserAuthServiceImpl;


public class GluuRadiusUserAuthServiceTest {
	
	private static final String DECRYPTION_KEY = "D5DEu3FF8LGYaxbNoXahfx7l";
	private static final String VALID_USER_USERNAME = "admin";
	private static final String VALID_USER_PASSWORD = "gluu";
	private static final String INVALID_USER_USERNAME = "foo";
	private static final String INVALID_USER_PASSWORD = "foo@bar";
	
	@Test
	public void validUserCredentials() {

		GluuRadiusOpenIdConfig config = getConfig();
		GluuRadiusUserAuthService authservice = new GluuRadiusUserAuthServiceImpl(DECRYPTION_KEY,config);
		GluuRadiusUserAuthResult result = authservice.authenticateUser(VALID_USER_USERNAME,VALID_USER_PASSWORD);
		assertTrue(result.isSuccess());
	}


	@Test
	public void invalidUserCredentials() {

		GluuRadiusOpenIdConfig config = getConfig();
		GluuRadiusUserAuthService authservice = new GluuRadiusUserAuthServiceImpl(DECRYPTION_KEY,config);
		GluuRadiusUserAuthResult result = authservice.authenticateUser(INVALID_USER_USERNAME,INVALID_USER_PASSWORD);
		assertTrue(!result.isSuccess());
	}

	private GluuRadiusOpenIdConfig getConfig() {

		GluuRadiusOpenIdConfig config = new GluuRadiusOpenIdConfig();
		config.setApplianceUrl("https://gluu.intranet.yems.group");
		config.setClientId("@!392F.C4B0.C2E8.191F!0001!28CF.5FED!0008!9015.F5F1.857B.21FE");
		config.setClientSecret("mHtydyUwOFo=");
		config.setSslVerifyEnabled(false);
		return config;
	}

}