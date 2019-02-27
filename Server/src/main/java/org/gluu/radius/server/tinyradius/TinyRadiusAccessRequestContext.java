package org.gluu.radius.server.tinyradius;

import java.net.InetSocketAddress;
import org.gluu.radius.exception.ServerException;
import org.gluu.radius.server.AccessRequestContext;
import org.tinyradius.packet.AccessRequest;

public class TinyRadiusAccessRequestContext extends TinyRadiusRequestContext implements AccessRequestContext {

    private boolean granted;

    public TinyRadiusAccessRequestContext(InetSocketAddress address,AccessRequest packet) {

        super(address,packet);
        granted = false;
    }

    @Override
    public String getUsername() {

        try {
            AccessRequest req = (AccessRequest) packet;
            return req.getUserName();
        }catch(RuntimeException e) {
            throw new ServerException("Error getting username from access request packet",e);
        }
    }

    @Override
    public String getPassword() {

        AccessRequest req = (AccessRequest) packet;
        return req.getUserPassword();
    }

    @Override
    public String getAuthProtocol() {

        AccessRequest req = (AccessRequest) packet;
        return req.getAuthProtocol();
    }

    @Override
    public AccessRequestContext setGranted(boolean granted) {

        this.granted = granted;
        return this;
    }

    public boolean isGranted() {

        return this.granted;
    }
}