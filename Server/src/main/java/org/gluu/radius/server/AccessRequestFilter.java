package org.gluu.radius.server;

public interface AccessRequestFilter {
    public boolean processAccessRequest(AccessRequestContext arContext);
}