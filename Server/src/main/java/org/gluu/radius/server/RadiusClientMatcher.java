package org.gluu.radius.server;

import org.gluu.radius.model.RadiusClient;

public interface RadiusClientMatcher {
    public boolean match(String clientIpAddress,RadiusClient client);
}