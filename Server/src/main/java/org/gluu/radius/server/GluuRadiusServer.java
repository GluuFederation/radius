package org.gluu.radius.server;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public final class GluuRadiusServer implements RadiusEventListener {

    private static final Logger log = Logger.getLogger(GluuRadiusServer.class);
    private RunConfiguration runConfig;
    private RadiusServerAdapter rsAdapter;

    public GluuRadiusServer(RunConfiguration runConfig,RadiusServerAdapter rsAdapter) {

        if(runConfig!=null)
            this.runConfig = runConfig;
        else
            this.runConfig = new RunConfiguration();
        
        this.rsAdapter = rsAdapter;
    }

    public final GluuRadiusServer run() {

        rsAdapter.configureServer(runConfig.getListenInterface(),
            runConfig.getAuthListenPort(),
            runConfig.getAcctListenPort());
        
        rsAdapter.registerRadiusEventListener(this);
        rsAdapter.runServer();
        return this;
    }

    public final GluuRadiusServer stop() {

        rsAdapter.stopServer();
        return this;
    }

    public void onAccessRequest(AccessRequestContext context) {

        boolean granted = false;
        for(AccessRequestFilter filter: runConfig.getAccessRequestFilters()) {
            if(filter.processAccessRequest(context)==true) {
                granted = true;
                break;
            }
        }

        context.setGranted(granted);
    }

    public void onAccountingRequest(AccountingRequestContext context) {

        for(AccountingRequestFilter filter: runConfig.getAccountingRequestFilters())
            filter.processAccountingRequest(context);
    }

    public void onSharedSecretRequest(SharedSecretRequestContext context) {

        for(SharedSecretProvider ssProvider : runConfig.getSharedSecretProviders()) {
            String sharedSecret = ssProvider.getSharedSecret(context.getClientIpAddress());
            if(sharedSecret!=null) {
                context.setSharedSecret(sharedSecret);
                break;
            }
        }
    }

}

