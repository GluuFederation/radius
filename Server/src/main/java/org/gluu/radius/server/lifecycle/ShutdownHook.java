package org.gluu.radius.server.lifecycle;

import org.apache.log4j.Logger;

public class ShutdownHook extends Thread {

    private static final Logger log = Logger.getLogger(ShutdownHook.class);
    private Runner runner;

    public ShutdownHook(Runner runner) {

        this.runner = runner;
    }

    @Override
    public void run() {

        log.info("Stopping Super Gluu Radius");
        runner.stopRunning();
    }
} 