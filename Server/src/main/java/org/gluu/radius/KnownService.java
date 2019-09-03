package org.gluu.radius;

public enum KnownService {

    BootstrapConfig("BootstrapConfig"),
    RadiusClient("RadiusClient"),
    ServerConfig("ServerConfig"),
    OpenIdConfig("OpenIdConfig"),
    Crypto("Crypto");

    private final String serviceid;

    private KnownService(String serviceid) {

        this.serviceid = serviceid;
    }

    public String getServiceId() {

        return this.serviceid;
    }
}