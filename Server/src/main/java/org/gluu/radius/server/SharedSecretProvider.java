package org.gluu.radius.server;


public interface SharedSecretProvider {
    public String getSharedSecret(String clientIpAddress);
}