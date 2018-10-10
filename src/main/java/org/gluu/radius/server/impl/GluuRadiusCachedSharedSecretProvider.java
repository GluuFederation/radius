package org.gluu.radius.server.impl;


import org.apache.log4j.Logger;

import org.gluu.radius.cache.GluuRadiusCache;
import org.gluu.radius.config.GluuRadiusClientConfig;
import org.gluu.radius.server.GluuRadiusSharedSecretProvider;

import org.gluu.radius.config.GluuRadiusClientConfig;
import org.gluu.radius.services.GluuRadiusServiceLocator;
import org.gluu.radius.services.GluuRadiusKnownService;
import org.gluu.radius.services.GluuRadiusLdapService;
import org.gluu.radius.util.CryptoUtil;


public class GluuRadiusCachedSharedSecretProvider implements GluuRadiusSharedSecretProvider {
	
	private static final Logger logger = Logger.getLogger(GluuRadiusCachedSharedSecretProvider.class);

	private String decryptionkey;
	private GluuRadiusCache<String,GluuRadiusClientConfig> cache;

	public GluuRadiusCachedSharedSecretProvider(String decryptionkey,GluuRadiusCache<String,GluuRadiusClientConfig> cache) {

		this.decryptionkey = decryptionkey;
		this.cache = cache;
	}

	@Override
	public String getSharedSecret(String ipaddress) {

		GluuRadiusClientConfig clientconfig = cache.getCacheEntry(ipaddress);
		if(clientconfig != null)
			return decryptClientSecret(clientconfig.getSecret());
		GluuRadiusLdapService ldapservice = GluuRadiusServiceLocator.getService(GluuRadiusKnownService.LdapService);
		clientconfig = ldapservice.getRadiusClientConfig(ipaddress);
		if(clientconfig != null) {
			cache.setCacheEntry(ipaddress,clientconfig);
			return decryptClientSecret(clientconfig.getSecret());
		}
		return null;
	}

	private String decryptClientSecret(String clientsecret) {

		return CryptoUtil.decryptPassword(clientsecret,decryptionkey);
	}
}