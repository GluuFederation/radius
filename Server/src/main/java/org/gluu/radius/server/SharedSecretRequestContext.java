package org.gluu.radius.server;

public interface SharedSecretRequestContext {
    public String getClientIpAddress();
    public void setSharedSecret(String sharedSecret);
}