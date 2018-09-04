package org.gluu.radius.services;

import org.gluu.radius.config.LdapConfiguration;


public interface GluuBootstrapConfigService {
	public LdapConfiguration getLdapConfiguration();
	public String getEncryptionKey();
}