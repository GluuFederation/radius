package org.gluu.radius;

import org.apache.log4j.Logger;


public class GluuRadiusShutdownHook extends Thread {

	private static final Logger logger = Logger.getLogger(GluuRadiusShutdownHook.class);
	private GluuRadiusServerRunner runner;

	public GluuRadiusShutdownHook(GluuRadiusServerRunner runner) {

		this.runner = runner;
	}

	@Override
	public void run() {

		runner.stopServer();
	}
}