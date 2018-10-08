package org.gluu.radius.services;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gluu.radius.config.GluuRadiusBootstrapConfig;
import org.gluu.radius.config.GluuRadiusOpenIdConfig;
import org.gluu.radius.config.GluuRadiusServerConfig;
import org.gluu.radius.config.GluuRadiusClientConfig;
import org.gluu.radius.config.GluuRadiusCacheConfig;
import org.gluu.radius.ldap.GluuRadiusLdapConnectionProvider;
import org.gluu.radius.services.impl.GluuRadiusBootstrapConfigServiceImpl;
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

	private static final String OPENID_APPLIANCE_URL = "https://gluu.intranet.yems.group";
	private static final String OPENID_CLIENT_ID = "@!392F.C4B0.C2E8.191F!0001!28CF.5FED!0008!9015.F5F1.857B.21FE";
	private static final String OPENID_CLIENT_SECRET = "mHtydyUwOFo=";
	private static final Integer OPENID_HTTP_CONNPOOL_SIZE = 10;
	private static final Boolean OPENID_HTTP_SSL_VERIFY_ENABLED = false;

	private static final String RADIUS_CLIENT_IP_ADDRESS = "192.168.1.11";
	private static final String RADIUS_CLIENT_NAME = "test_client_2";
	private static final String RADIUS_CLIENT_SECRET = "FX7Wm87SNeCC8BmyWtOjcg==";

	private static final Integer RADIUS_CLIENT_CACHE_INTERVAL = 2000; // ms 

	private GluuRadiusLdapService ldapservice;


	public GluuRadiusLdapServiceTest() {

		GluuRadiusBootstrapConfigService configservice = new GluuRadiusBootstrapConfigServiceImpl(TEST_SERVER_CONFIG_FILE);
		GluuRadiusBootstrapConfig config = configservice.getBootstrapConfiguration();
		GluuRadiusLdapConnectionProvider connprovider = new GluuRadiusLdapConnectionProvider(config);
		String encryptionkey = configservice.getEncryptionKey();
		ldapservice = new GluuRadiusLdapServiceImpl(connprovider);
	}


	@Test
	public void validRadiusServerConfig() {

		GluuRadiusServerConfig config = ldapservice.getRadiusServerConfig();
		assertEquals(RADIUS_LISTEN_ADDRESS,config.getListenAddress());
		assertEquals(RADIUS_AUTH_PORT,config.getAuthPort());
		assertEquals(RADIUS_ACCT_PORT,config.getAcctPort());
	}


	@Test
	public void validRadiusOpenIdConfig() {

		GluuRadiusOpenIdConfig config = ldapservice.getRadiusOpenIdConfig();
		assertEquals(OPENID_APPLIANCE_URL,config.getApplianceUrl());
		assertEquals(OPENID_CLIENT_ID,config.getClientId());
		assertEquals(OPENID_CLIENT_SECRET,config.getClientSecret());
		assertEquals(OPENID_HTTP_CONNPOOL_SIZE,config.getCpSize());
		assertEquals(OPENID_HTTP_SSL_VERIFY_ENABLED,config.getSslVerifyEnabled());
	}


	@Test
	public void validClientConfig() {

		GluuRadiusClientConfig config = ldapservice.getRadiusClientConfig(RADIUS_CLIENT_IP_ADDRESS);
		assertEquals(RADIUS_CLIENT_NAME,config.getName());
		assertEquals(RADIUS_CLIENT_SECRET,config.getSecret());
		assertEquals(RADIUS_CLIENT_IP_ADDRESS,config.getIpAddress());
	}


	@Test
	public void validClientCacheConfig() {

		GluuRadiusCacheConfig config = ldapservice.getRadiusClientCacheConfig();
		assertEquals(RADIUS_CLIENT_CACHE_INTERVAL,config.getCacheInterval());
		assertTrue(config.isNormalPolicy());
	}

}