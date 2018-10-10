package org.gluu.radius.services;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.gluu.radius.config.GluuRadiusBootstrapConfig;
import org.gluu.radius.services.impl.GluuRadiusBootstrapConfigServiceImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.hamcrest.core.IsInstanceOf.instanceOf;


public class GluuRadiusBootstrapConfigServiceTest {
	
	private static final String MISSING_CONFIG_FILENAME = "missing-config.properties";
	private static final String INVALID_CONFIG_FILENAME = "invalid-config.properties";
	private static final String VALID_CONFIG_FILENAME = "valid-config.properties";

	@Test
	public void nullConfigurationFile() {

		try {

			GluuRadiusBootstrapConfigService service = new GluuRadiusBootstrapConfigServiceImpl(null);
			fail("expected GluuRadiusConfigException not thrown");
		}catch(GluuRadiusServiceException e) {
			assertThat(e.getMessage(),is("Missing configuration filename"));
		}
	}

	@Test
	public void missingConfigurationFile() {

		try {
			GluuRadiusBootstrapConfigService service = new GluuRadiusBootstrapConfigServiceImpl(MISSING_CONFIG_FILENAME);
			fail("expected GluuRadiusConfigException not thrown");
		}catch(GluuRadiusServiceException e) {
			assertTrue(e.getCause() instanceof FileNotFoundException);
		}
	}


	@Test
	public void invalidConfigurationFile() {

		try {
			GluuRadiusBootstrapConfigService service = new GluuRadiusBootstrapConfigServiceImpl(INVALID_CONFIG_FILENAME);
		}catch(GluuRadiusServiceException e) {
			assertEquals("Missing salt file name",e.getMessage());
		}
	}


	@Test
	public void validConfigurationFile() {

		GluuRadiusBootstrapConfigService service = new GluuRadiusBootstrapConfigServiceImpl(VALID_CONFIG_FILENAME);
		GluuRadiusBootstrapConfig config  = service.getBootstrapConfiguration();

		Integer port = 1636;
		Integer cpsize = 10;
		assertEquals("localhost",config.getHostname());
		assertEquals(port,config.getPort());
		assertEquals("cn=directory manager",config.getBindDn());
		assertEquals("gluu",config.getPassword());
		assertEquals(true,config.getSslEnabled());

		assertEquals("/etc/certs/opendj.pkcs12",config.getTrustStoreFile());
		assertEquals("Agtyc0lLNwuC",config.getTrustStorePin());
		assertEquals("pkcs12",config.getTrustStoreFormat());
		assertEquals(true,config.getSslVerifyEnabled());
		assertEquals(cpsize,config.getConnPoolConfig().getConnPoolSize());
	}
}