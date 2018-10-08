package org.gluu.radius.services;

import org.gluu.radius.config.GluuRadiusOpenIdConfig;
import org.gluu.radius.config.GluuRadiusServerConfig;
import org.gluu.radius.config.GluuRadiusClientConfig;
import org.gluu.radius.config.GluuRadiusCacheConfig;

public interface GluuRadiusLdapService {
	
	public GluuRadiusServerConfig getRadiusServerConfig();
	public GluuRadiusOpenIdConfig  getRadiusOpenIdConfig();
	public GluuRadiusClientConfig getRadiusClientConfig(String ipaddress);
	public GluuRadiusCacheConfig  getRadiusClientCacheConfig();
}