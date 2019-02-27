package org.gluu.radius.server;

public interface RadiusServerAdapter {
    public void configureServer(String listenInterface,Integer authPort,Integer acctPort);
    public void registerRadiusEventListener(RadiusEventListener listener);
    public void unregisterRadiusEventListener(RadiusEventListener listener);
    public void runServer();
    public void stopServer();
}