package org.gluu.radius.server.tinyradius;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.gluu.radius.exception.ServerException;
import org.gluu.radius.server.RadiusEventListener;
import org.gluu.radius.server.RadiusEventListenerManager;
import org.gluu.radius.server.RadiusServerAdapter;
import org.gluu.radius.util.ThreadUtil;

import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusServer;

public class TinyRadiusServerAdapter implements RadiusServerAdapter {


    private class TinyRadiusServerImpl extends RadiusServer {

        private RadiusEventListenerManager eventListenerManager;

        public TinyRadiusServerImpl() {

            this.eventListenerManager = new RadiusEventListenerManager();
        }

        public RadiusEventListenerManager getEventListenerManager() {

            return this.eventListenerManager;
        }

        @Override
        public String getUserPassword(String username) {

            return null;
        }

        @Override
        public String getSharedSecret(InetSocketAddress client) {

            String clientIp = client.getAddress().getHostAddress();
            TinyRadiusSharedSecretRequestContext context = new TinyRadiusSharedSecretRequestContext(clientIp);
            eventListenerManager.sharedSecretRequestNotification(context);
            return context.getSharedSecret();
        }

        @Override
        public RadiusPacket accessRequestReceived(AccessRequest request,InetSocketAddress client) {

            RadiusPacket response = null;
            TinyRadiusAccessRequestContext context = new TinyRadiusAccessRequestContext(client,request);
            eventListenerManager.accessRequestNotification(context);
            if(context.isGranted())
                response = new RadiusPacket(RadiusPacket.ACCESS_ACCEPT,request.getPacketIdentifier());
            else
                response = new RadiusPacket(RadiusPacket.ACCESS_REJECT,request.getPacketIdentifier());
            copyProxyState(request,response);
            return response;
        }

        @Override
        public RadiusPacket accountingRequestReceived(AccountingRequest request,InetSocketAddress client) {

            RadiusPacket response = null;
            TinyRadiusAccountingRequestContext context = new TinyRadiusAccountingRequestContext(client,request);
            eventListenerManager.accountingRequestNotification(context);
            copyProxyState(request,response);
            return response;
        }

    }

    private static final String AUTH_THREAD_NAME = "Radius Auth Listener";
    private static final String ACCT_THREAD_NAME = "Radius Acct Listener";
    private TinyRadiusServerImpl serverImpl;
    
   
    public TinyRadiusServerAdapter() {

        this.serverImpl = new TinyRadiusServerImpl();
    }

    @Override
    public void configureServer(String listenInterface,Integer authPort,Integer acctPort) {

        try {
            serverImpl.setAuthPort(authPort);
            serverImpl.setAcctPort(acctPort);
            serverImpl.setListenAddress(InetAddress.getByName(listenInterface));
        }catch(UnknownHostException e) {
            throw new ServerException("Tinyradius server configuration failed",e);
        }
    }

    @Override
    public void registerRadiusEventListener(RadiusEventListener listener) {

        serverImpl.getEventListenerManager().addListener(listener);
    }

    @Override
    public void unregisterRadiusEventListener(RadiusEventListener listener) {

        serverImpl.getEventListenerManager().removeListener(listener);
    }

    @Override
    public void runServer() {

        
        serverImpl.start(true,true);
        //hack to check if tinyradius server is running 
        long sleeptime = 1000; // 1 second
        int checkcount = 5; // we will check five times to see if the server is up 

        boolean stillrunning = true;
        do {
            try {
                Thread.sleep(sleeptime);
            }catch(InterruptedException e) {
                serverImpl.stop();
                throw new ServerException("Tinyradius interrupted",e);
            }
            stillrunning  = areServerThreadsRunning();
        }while(-- checkcount > 0);

        if(!stillrunning) {
            serverImpl.stop();
            throw new ServerException("TinyRadius server stopped unexpectedly");
        }

    }

    @Override
    public void stopServer() {
        
        serverImpl.stop();
    }

    private boolean areServerThreadsRunning() {

        Thread acct_thread = ThreadUtil.findRunningThread(ACCT_THREAD_NAME);
        Thread auth_thread = ThreadUtil.findRunningThread(AUTH_THREAD_NAME);

        return acct_thread!=null && auth_thread!=null;
    }
}