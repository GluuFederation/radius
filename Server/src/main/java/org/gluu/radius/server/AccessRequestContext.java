package org.gluu.radius.server;


public interface AccessRequestContext extends RequestContext {

    public String getUsername();
    public String getPassword();
    public String getAuthProtocol();
    public AccessRequestContext setGranted(boolean granted);
}