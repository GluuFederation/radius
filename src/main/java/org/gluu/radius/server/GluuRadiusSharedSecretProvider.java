package org.gluu.radius.server;


public interface GluuRadiusSharedSecretProvider {
	public String getSharedSecret(String ipaddress);
}