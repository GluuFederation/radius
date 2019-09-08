package org.gluu.radius.server;

import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.gluu.radius.model.RadiusClient;
import org.gluu.radius.service.RadiusClientService;
import org.gluu.radius.util.EncDecUtil;

public final class GluuRadiusServer implements RadiusEventListener {

    public static class RadiusClientComparator implements Comparator<RadiusClient> {

        @Override
        public int compare(RadiusClient first, RadiusClient second) {

            if(first == null && second == null)
                return 0;

            if(first== null && second !=null)
                return -1;
            
            if(first !=null && second == null)
                return 1;
            
            if(first.getPriority() == null && second.getPriority() == null)
                return 0;
            
            if(first.getPriority() == null && second.getPriority() != null)
                return -1;
            
            if(first.getPriority() != null && second.getPriority() != null)
                return 1;
            
            if(first.getPriority() < second.getPriority())
                return -1;
            else if(first.getPriority() > second.getPriority())
                return 1;
            else
                return 0;
        }
    }

    private static final Logger log = Logger.getLogger(GluuRadiusServer.class);
    private RunConfiguration runConfig;
    private RadiusServerAdapter radiusServerAdapter;
    private RadiusClientService radiusClientService;
    private String salt;

    public GluuRadiusServer(RunConfiguration runConfig,RadiusServerAdapter radiusServerAdapter,
        RadiusClientService radiusClientService,String salt) {

        if(runConfig!=null)
            this.runConfig = runConfig;
        else
            this.runConfig = new RunConfiguration();
        
        this.radiusServerAdapter = radiusServerAdapter;
        this.radiusClientService = radiusClientService;
        this.salt = salt;
    }

    public final GluuRadiusServer run() {

        radiusServerAdapter.configureServer(runConfig.getListenInterface(),
            runConfig.getAuthListenPort(),
            runConfig.getAcctListenPort());
        
        radiusServerAdapter.registerRadiusEventListener(this);
        radiusServerAdapter.runServer();
        return this;
    }

    public final GluuRadiusServer stop() {

        radiusServerAdapter.stopServer();
        return this;
    }

    @Override
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

    @Override
    public void onAccountingRequest(AccountingRequestContext context) {

        for(AccountingRequestFilter filter: runConfig.getAccountingRequestFilters())
            filter.processAccountingRequest(context);
    }

    @Override
    public void onSharedSecretRequest(SharedSecretRequestContext context) {
       
        try {
            List<RadiusClient> clients = radiusClientService.getRadiusClients();
            clients.sort(new RadiusClientComparator());
            for(RadiusClient client : clients) {
                log.info(String.format("Client ip: %s",context.getClientIpAddress()));
                for(RadiusClientMatcher matcher : runConfig.getClientMatchers()) {
                    if(matcher.match(context.getClientIpAddress(),client)) {
                        String secret = EncDecUtil.decode(client.getSecret(),salt);
                        context.setSharedSecret(secret);
                        break;
                    }
                }
            }
        }catch(Exception e) {
            log.info("Shared secret request failed",e);
        }
    }

}

