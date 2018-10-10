package org.gluu.radius.ldap;

import org.gluu.radius.config.GluuRadiusBootstrapConfig;
import org.gluu.radius.services.impl.GluuRadiusBootstrapConfigServiceImpl; // not optimal

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GluuRadiusLdapConnectionProviderTest {


	@Test
	public void unableToConnectToLdapHost() {

		try {
			GluuRadiusBootstrapConfig config = getUnreachableHostBootstrapConfig();
			GluuRadiusLdapConnectionProvider provider = new GluuRadiusLdapConnectionProvider(config);
			fail("Expected GluuRadiusLdapException not thrown");
		}catch(GluuRadiusLdapException e) {
			assertEquals("Could not create secured ldap connection",e.getMessage());
		}
	}

	@Test
	public void validConnection () {

		try {
			GluuRadiusBootstrapConfig config = getValidBootstrapConfig();
			GluuRadiusLdapConnectionProvider provider = new GluuRadiusLdapConnectionProvider(config);
		}catch(GluuRadiusLdapException e) {
			assertEquals("Could not create secured ldap connection",e.getMessage());
			fail("Unexpected GluuRadiusLdapException thrown. " +
				"Make sure the gluu instance is running and the test configuration files are valid");
		}
	}


	private GluuRadiusBootstrapConfig getUnreachableHostBootstrapConfig() {

		GluuRadiusBootstrapConfig config = new GluuRadiusBootstrapConfig();
		config.setHostname("host.unknown");
		config.setPort(5555);
		config.setBindDn("");
		config.setPassword("");
		return config;
	}


	private GluuRadiusBootstrapConfig getValidBootstrapConfig() {

		String configfile = "ldap-test-server.properties";
		// this isn't optimal
		// replace later with a factory or a service provider
		GluuRadiusBootstrapConfigServiceImpl cfgservice = new GluuRadiusBootstrapConfigServiceImpl(configfile);
		return cfgservice.getBootstrapConfiguration();
	}
}