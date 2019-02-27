package org.gluu.radius.server.tinyradius;

import org.gluu.radius.server.SharedSecretRequestContext;


public class TinyRadiusSharedSecretRequestContext implements SharedSecretRequestContext {

    private final String clientIpAddress;
    private String sharedSecret;

    public TinyRadiusSharedSecretRequestContext(String clientIpAddress) {

        this.clientIpAddress = clientIpAddress;
        this.sharedSecret = null;
    }

    @Override
    public String getClientIpAddress() {

        return this.clientIpAddress;
    }

    @Override
    public void setSharedSecret(String sharedSecret) {

        this.sharedSecret = sharedSecret;
    }

    public String getSharedSecret() {

        return sharedSecret;
    }
}