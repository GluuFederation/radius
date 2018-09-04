package org.gluu.radius.services;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.gluu.radius.config.LdapConfiguration;
import org.gluu.radius.ldap.GluuRadiusLdapConnectionProvider;
import org.gluu.radius.services.impl.GluuBootstrapConfigServiceImpl;
import org.gluu.radius.services.impl.GluuRadiusLdapServiceImpl;

public class GluuRadiusLdapServiceTest  {
	
	private static final String TEST_SERVER_CONFIG_FILE = "ldap-test-server.properties";
	private static final String VALID_USERNAME = "admin";
	private static final String VALID_PASSWORD = "gluu";
	private static final String INVALID_USERNAME = "invalid_username";
	private static final String INVALID_PASSWORD = "invalid_password";
	private static final String RADIUS_LISTEN_ADDRESS = "127.0.0.1";
	private static final Integer RADIUS_AUTH_PORT = 1234;
	private static final Integer RADIUS_ACCT_PORT = 1235;

	private static final String TEST_CLIENT_1_IP_ADDRESS =  "192.168.1.10";
	private static final String TEST_CLIENT_1_SECRET = "gluu";

	private static final String TEST_CLIENT_2_IP_ADDRESS = "192.168.1.11";
	private static final String TEST_CLIENT_2_SECRET = "Agtyc0lLNwuC";

	private GluuRadiusLdapService ldapservice;


	public GluuRadiusLdapServiceTest() {

		GluuBootstrapConfigService configservice = new GluuBootstrapConfigServiceImpl(TEST_SERVER_CONFIG_FILE);
		LdapConfiguration ldapconfig = configservice.getLdapConfiguration();
		GluuRadiusLdapConnectionProvider connprovider = new GluuRadiusLdapConnectionProvider(ldapconfig);
		String encryptionkey = configservice.getEncryptionKey();
		ldapservice = new GluuRadiusLdapServiceImpl(encryptionkey,connprovider);
	}

	@Test
	public void validUserCredentials() {

		if(ldapservice.verifyUserCredentials(VALID_USERNAME,VALID_PASSWORD) == false) {
			fail("User credentials validation failed");
		} 
	}

	@Test
	public void invalidUserCredentials() {

		try {
			if(ldapservice.verifyUserCredentials(INVALID_USERNAME,INVALID_PASSWORD) == true) {
				fail("Expected credential validation to fail. User authenticated");
			}
		}catch(GluuRadiusServiceException e) {
			
		}
	}


	@Test
	public void testGetRadiusListenAddress() {

		assertEquals(RADIUS_LISTEN_ADDRESS,ldapservice.getRadiusListenAddress());
	}

	@Test
	public void testGetRadiusAuthenticationPort() {

		assertEquals(RADIUS_AUTH_PORT,ldapservice.getRadiusAuthenticationPort());
	}


	@Test
	public void testGetRadiusAccountingPort() {

		assertEquals(RADIUS_ACCT_PORT,ldapservice.getRadiusAccountingPort());

	}


	@Test
	public void testGetClientSharedSecret() {

		assertEquals(TEST_CLIENT_1_SECRET,ldapservice.getClientSharedSecret(TEST_CLIENT_1_IP_ADDRESS));
		assertEquals(TEST_CLIENT_2_SECRET,ldapservice.getClientSharedSecret(TEST_CLIENT_2_IP_ADDRESS));
	}

}