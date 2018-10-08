package org.gluu.radius.services.impl;

import java.io.FileInputStream;
import java.util.Properties;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.gluu.radius.config.GluuRadiusBootstrapConfig;
import org.gluu.radius.util.CryptoUtil;
import org.gluu.radius.util.PropertyUtil;
import org.gluu.radius.services.GluuRadiusBootstrapConfigService;
import org.gluu.radius.services.GluuRadiusServiceException;


public class GluuRadiusBootstrapConfigServiceImpl implements GluuRadiusBootstrapConfigService {

	
	private static final String RADIUS_CONFIG_SALT_KEY = "radius.config.salt";
	private static final String RADIUS_CONFIG_OXLDAP_KEY = "radius.config.oxldap";
	private static final String RADIUS_LDAP_CPOOL_SIZE_KEY = "radius.ldap.cpool.size";
	private static final String RADIUS_LDAP_VERIFYSSL_KEY = "radius.ldap.verifyssl";

	private static final String ENCODE_SALT_KEY = "encodeSalt";

	private static final String BIND_DN_KEY = "bindDN";
	private static final String BIND_PASSWORD_KEY = "bindPassword";
	private static final String SERVERS_KEY = "servers";
	private static final String USE_SSL_KEY = "useSSL";
	private static final String SSL_TRUSTSTORE_FILE_KEY  = "ssl.trustStoreFile";
	private static final String SSL_TRUSTSTORE_PIN_KEY   = "ssl.trustStorePin";
	private static final String SSL_TRUSTSTORE_FORMAT_KEY = "ssl.trustStoreFormat";

	private String salt; // encryption/decryption key
	private Properties primaryconfig;
	private Properties oxldapconfig;

	public GluuRadiusBootstrapConfigServiceImpl(String configfile) {

		if(configfile == null)
			throw new GluuRadiusServiceException("Missing configuration filename");

		primaryconfig = loadPropertiesFromFile(configfile);

		String saltfile = PropertyUtil.getStringProperty(primaryconfig,RADIUS_CONFIG_SALT_KEY);
		if(saltfile == null)
			throw new GluuRadiusServiceException("Missing salt file name");

		Properties saltconfig = loadPropertiesFromFile(saltfile);
		salt = PropertyUtil.getStringProperty(saltconfig,ENCODE_SALT_KEY);

		String oxldapconfigfile = PropertyUtil.getStringProperty(primaryconfig,RADIUS_CONFIG_OXLDAP_KEY);

		if(oxldapconfigfile == null)
			throw new GluuRadiusServiceException("Missing ox-ldap configuration file");

		oxldapconfig = loadPropertiesFromFile(oxldapconfigfile);
	}

	@Override
	public GluuRadiusBootstrapConfig getBootstrapConfiguration() {

		GluuRadiusBootstrapConfig config = new GluuRadiusBootstrapConfig();
		String servers = PropertyUtil.getStringProperty(oxldapconfig,SERVERS_KEY);
		if(servers == null)
			throw new GluuRadiusServiceException("Could not find servers entry in config");
		String [] serverparts = servers.split(":");
		if(serverparts.length!=2) 
			throw new GluuRadiusServiceException("Unable to parse and extract servers entry in config");

		config.setHostname(serverparts[0]);
		try {
			config.setPort(Integer.parseInt(serverparts[1]));
		}catch(NumberFormatException e) {
			throw new GluuRadiusServiceException("Unable to parse ldap server port in config");
		}

		config.setBindDn(PropertyUtil.getStringProperty(oxldapconfig,BIND_DN_KEY));
		String encpassword  = PropertyUtil.getStringProperty(oxldapconfig,BIND_PASSWORD_KEY);
		if(encpassword!=null)
			config.setPassword(CryptoUtil.decryptPassword(encpassword,salt));


		config.setTrustStoreFile(PropertyUtil.getStringProperty(oxldapconfig,SSL_TRUSTSTORE_FILE_KEY));
		config.setTrustStoreFormat(PropertyUtil.getStringProperty(oxldapconfig,SSL_TRUSTSTORE_FORMAT_KEY));
		String encpin = PropertyUtil.getStringProperty(oxldapconfig,SSL_TRUSTSTORE_PIN_KEY);
		if(encpin!=null)
			config.setTrustStorePin(CryptoUtil.decryptPassword(encpin,salt));

		config.setSslEnabled(PropertyUtil.getBooleanProperty(oxldapconfig,USE_SSL_KEY));
		config.setSslVerifyEnabled(PropertyUtil.getBooleanProperty(primaryconfig,RADIUS_LDAP_VERIFYSSL_KEY));

		// conn pool 
		Integer cpsize = PropertyUtil.getIntProperty(primaryconfig,RADIUS_LDAP_CPOOL_SIZE_KEY);
		config.getConnPoolConfig().setConnPoolSize(cpsize);


		return config;
	}

	@Override 
	public String getEncryptionKey() {

		return salt;
	}

	private Properties loadPropertiesFromFile(String filename) {

		FileInputStream instream = null;
		try {
			instream = new FileInputStream(filename);
			Properties props = new Properties();
			props.load(instream);
			return props;
		}catch(IOException e) {
			throw new GluuRadiusServiceException("I/O error loading config file " + filename,e);
		}catch(SecurityException e) {
			throw new GluuRadiusServiceException("Security exception loading config file " + filename,e);
		}catch(IllegalArgumentException e) {
			throw new GluuRadiusServiceException("Malformed config file " + filename,e);
		}finally {
			closeFileStream(instream);
		}
	}

	private final void closeFileStream(FileInputStream instream) {

		try {
			if(instream!=null)
				instream.close();
		}catch(IOException e) {

		}
	}

}