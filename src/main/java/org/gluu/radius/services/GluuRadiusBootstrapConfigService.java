package org.gluu.radius.services;

import org.gluu.radius.config.GluuRadiusBootstrapConfig;


public interface GluuRadiusBootstrapConfigService {
	public GluuRadiusBootstrapConfig getBootstrapConfiguration();
	public String getEncryptionKey();
}