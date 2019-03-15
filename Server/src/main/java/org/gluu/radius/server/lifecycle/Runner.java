package org.gluu.radius.server.lifecycle;

import org.apache.log4j.Logger;
import org.gluu.radius.exception.ServerException;
import org.gluu.radius.server.GluuRadiusServer;

public class Runner extends Thread {

    private static final Logger log = Logger.getLogger(Runner.class);
    private static final long sleeptimeout = 2000; // 2 seconds 
    private GluuRadiusServer server;
    private boolean stop;

    public Runner(GluuRadiusServer server) {

        this.server = server;
        this.stop = false;
    }

    @Override
    public void run() {

        try {
            while(!stop) {
                Thread.sleep(sleeptimeout);
            }
            if(server!=null)
                server.stop();
        }catch(InterruptedException e) {
            // nothing to be done
        }catch(ServerException e) {
            log.error("Error while shutting down the server",e);
        }
    }

    public void stopRunning() {
        this.stop = true;
    }
}