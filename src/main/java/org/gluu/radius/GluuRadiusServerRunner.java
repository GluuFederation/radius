package org.gluu.radius;

import org.apache.log4j.Logger;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.server.GluuRadiusServerException;

public class GluuRadiusServerRunner extends Thread {

	private static final Logger logger = Logger.getLogger(GluuRadiusServerRunner.class);
	private static final long sleeptimeout = 2000; // 2 seconds 
	private GluuRadiusServer server;
	private boolean stop;

	public GluuRadiusServerRunner(GluuRadiusServer server) {

		this.server = server;
		this.stop = false;
	}


	@Override
	public void run() {

		try {
			while(stop == false) {
				Thread.currentThread().sleep(sleeptimeout);
			}
			server.shutdown();
		}catch(GluuRadiusServerException e) {
			logger.error("An error occured while shutting down the server",e);
		}catch(InterruptedException e) {
			// nothing to be done
		}
	}

	public void stopServer() {

		this.stop = true;
	}
}