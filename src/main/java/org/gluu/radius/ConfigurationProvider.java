package org.gluu.radius.config;


public interface ConfigurationProvider {
	public LdapConfiguration getLdapConfiguration();
	public RadiusServerConfiguration getRadiusServerConfiguration();
}