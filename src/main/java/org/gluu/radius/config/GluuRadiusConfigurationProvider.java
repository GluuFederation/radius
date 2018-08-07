package org.gluu.radius.config;

import java.io.FileInputStream;
import java.util.Properties;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.gluu.radius.util.CryptoUtil;
import org.gluu.radius.util.PropertyUtil;


public class GluuRadiusConfigurationProvider implements ConfigurationProvider {

	private static final String LDAP_SERVER_HOSTNAME_KEY = "ldap.server.hostname";
	private static final String LDAP_SERVER_PORT_KEY = "ldap.server.port";
	private static final String LDAP_SERVER_BIND_DN_KEY =  "ldap.server.bindDn";
	private static final String LDAP_SERVER_PASSWORD_KEY = "ldap.server.password";
	private static final String LDAP_SERVER_USE_SSL_KEY = "ldap.server.usessl";
	private static final String LDAP_SERVER_APPLIANCE_INUM_KEY = "ldap.server.applianceInum";
	private Properties configprops;

	public GluuRadiusConfigurationProvider(String configfile) {

		FileInputStream instream = null;
		try {
			this.configprops = new Properties();
			instream = new FileInputStream(configfile);
			configprops.load(instream);
		}catch(IOException ioe) {
			throw new GluuRadiusConfigException("could not create config provider (I/O error)",ioe);
		}catch(SecurityException se) {
			throw new GluuRadiusConfigException("could not create config provider (security restrictions)",se);
		}catch(IllegalArgumentException iae) {
			throw new GluuRadiusConfigException("could not create config (malformed file config file)",iae); 
		}finally {
			closeStream(instream);
		}

	}

	@Override
	public LdapConfiguration getLdapConfiguration() {

		LdapConfiguration config = new LdapConfiguration();
		config.setHostname(PropertyUtil.getStringProperty(configprops,LDAP_SERVER_HOSTNAME_KEY));
		config.setPort(PropertyUtil.getIntProperty(configprops,LDAP_SERVER_PORT_KEY));
		config.setBindDn(PropertyUtil.getStringProperty(configprops,LDAP_SERVER_BIND_DN_KEY));
		String encpassword = PropertyUtil.getStringProperty(configprops,LDAP_SERVER_PASSWORD_KEY);
		config.setPassword(CryptoUtil.decryptLdapPassword(encpassword));
		config.setSslEnabled(PropertyUtil.getBooleanProperty(configprops,LDAP_SERVER_USE_SSL_KEY));
		config.setApplianceInum(PropertyUtil.getStringProperty(configprops,LDAP_SERVER_APPLIANCE_INUM_KEY));
		return config;
	}


	@Override
	public RadiusServerConfiguration getRadiusServerConfiguration() {

		return null;
	}


	private final void closeStream(FileInputStream instream) {

		try {
			if(instream!=null)
				instream.close();
		}catch(IOException ioe) {

		}
	}

}