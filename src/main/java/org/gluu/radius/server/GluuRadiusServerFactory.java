package org.gluu.radius.server;

import org.gluu.radius.auth.GluuRadiusOpenIdAccessRequestFilter;
import org.gluu.radius.cache.GluuRadiusCache;
import org.gluu.radius.cache.impl.SimpleGluuRadiusCache;
import org.gluu.radius.config.GluuRadiusCacheConfig;
import org.gluu.radius.config.GluuRadiusClientConfig;
import org.gluu.radius.config.GluuRadiusServerConfig;
import org.gluu.radius.server.impl.GluuRadiusCachedSharedSecretProvider;
import org.gluu.radius.server.impl.tinyradius.TinyRadiusServer;
import org.gluu.radius.services.GluuRadiusServiceLocator;
import org.gluu.radius.services.GluuRadiusKnownService;
import org.gluu.radius.services.GluuRadiusBootstrapConfigService;
import org.gluu.radius.services.GluuRadiusLdapService;
import org.gluu.radius.services.GluuRadiusServiceException;



public class GluuRadiusServerFactory {
	

	public static final GluuRadiusServer create() {

		return createTinyRadiusServer();
	}

	private static final GluuRadiusServer createTinyRadiusServer() {

		try {
			GluuRadiusLdapService ldapsvc = GluuRadiusServiceLocator.getService(GluuRadiusKnownService.LdapService);
			GluuRadiusBootstrapConfigService bcsvc = GluuRadiusServiceLocator.getService(GluuRadiusKnownService.BootstrapService);
			String key = bcsvc.getEncryptionKey();
			GluuRadiusServerConfig config = ldapsvc.getRadiusServerConfig();
			GluuRadiusServer server  = new TinyRadiusServer(config);
			server.setSharedSecretProvider(createSharedSecretProvider(key,ldapsvc));
			server.addAccessRequestFilter(new GluuRadiusOpenIdAccessRequestFilter());
			return server;
		}catch(GluuRadiusServiceException e) {
			throw new GluuRadiusServerException("Could not instantiate TinyRadius server.",e);
		}
	}


	private static final GluuRadiusSharedSecretProvider createSharedSecretProvider(String key,GluuRadiusLdapService ldapservice) {

		return new GluuRadiusCachedSharedSecretProvider(key,createCache(ldapservice));
	}

	private static final GluuRadiusCache<String,GluuRadiusClientConfig> createCache(GluuRadiusLdapService ldapservice) {

		try {
			GluuRadiusCacheConfig cacheconfig = ldapservice.getRadiusClientCacheConfig();
			return new SimpleGluuRadiusCache<String,GluuRadiusClientConfig>(cacheconfig);
		}catch(GluuRadiusServiceException e) {
			throw new GluuRadiusServerException("Could not create a credentials cache object",e);
		}
	}

}