package org.gluu.radius.ldap;

import org.gluu.radius.config.LdapConfiguration;
import org.gluu.radius.config.GluuRadiusConfigurationProvider;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GluuRadiusLdapConnectionProviderTest {


	@Test
	public void unableToConnectToLdapHost() {

		try {
			LdapConfiguration config = getUnreachableHostLdapConfig();
			GluuRadiusLdapConnectionProvider provider = new GluuRadiusLdapConnectionProvider(config);
			fail("Expected GluuRadiusLdapException not thrown");
		}catch(GluuRadiusLdapException e) {
			assertEquals("Could not create secured ldap connection",e.getMessage());
		}
	}

	@Test
	public void validConnection () {

		try {
			LdapConfiguration config = getValidLdapConnection();
			GluuRadiusLdapConnectionProvider provider = new GluuRadiusLdapConnectionProvider(config);
		}catch(GluuRadiusLdapException e) {
			assertEquals("Could not create secured ldap connection",e.getMessage());
			fail("Unexpected GluuRadiusLdapException thrown. " +
				"Make sure the gluu instance is running and the test configuration files are valid");
		}
	}


	private LdapConfiguration getUnreachableHostLdapConfig() {

		LdapConfiguration config = new LdapConfiguration();
		config.setHostname("host.unknown");
		config.setPort(5555);
		config.setBindDn("");
		config.setPassword("");
		return config;
	}


	private LdapConfiguration getValidLdapConnection() {

		GluuRadiusConfigurationProvider cfgprovider = new GluuRadiusConfigurationProvider("valid-config.properties");
		return cfgprovider.getLdapConfiguration();
	}
}