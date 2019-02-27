package org.gluu.radius.server.tinyradius;

import java.net.InetSocketAddress;

import org.gluu.radius.server.RequestContext;
import org.tinyradius.packet.RadiusPacket;

public class TinyRadiusRequestContext implements RequestContext {

    private InetSocketAddress client;
    protected RadiusPacket packet;

    public TinyRadiusRequestContext(InetSocketAddress client,RadiusPacket packet) {

        this.client = client;
        this.packet = packet;
    }

    @Override
    public String getClientIpAddress() {

        return client.getAddress().getHostAddress();
    }

    @Override
    public String getAttributeValue(String attributename) {

        return packet.getAttributeValue(attributename);
    }

    
}