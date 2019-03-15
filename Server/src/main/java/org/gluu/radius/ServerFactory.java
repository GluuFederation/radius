package org.gluu.radius;

import org.apache.log4j.Logger;

import org.gluu.radius.exception.ServerFactoryException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.model.ServerConfiguration;
import org.gluu.radius.ServiceLocator;
import org.gluu.radius.KnownService;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.server.filter.SuperGluuAccessRequestFilter;
import org.gluu.radius.server.filter.SuperGluuAccessRequestFilterConfig;
import org.gluu.radius.server.provider.RadiusClientSharedSecretProvider;
import org.gluu.radius.server.RadiusServerAdapter;
import org.gluu.radius.server.RunConfiguration;
import org.gluu.radius.server.tinyradius.TinyRadiusServerAdapter;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.service.ServerConfigService;
import org.gluu.radius.service.RadiusClientService;

public class ServerFactory {

    private static final Logger log = Logger.getLogger(ServerFactory.class);

    private ServerFactory() {

    }

    public static final GluuRadiusServer createServer() {

        return new GluuRadiusServer(createRunConfiguration(),createServerAdapter());
    }

    private static final RadiusServerAdapter createServerAdapter() {

        return new TinyRadiusServerAdapter();
    }

    private static final RunConfiguration createRunConfiguration() {

        ServerConfiguration serverConfig = getServerConfiguration();
        RunConfiguration runConfig = RunConfiguration.fromServerConfiguration(serverConfig);
        addSharedSecretProviders(runConfig);
        addAccessRequestFilters(runConfig);
        addAccountingRequestFilters(runConfig);
        return runConfig;
    }

    private static final void addSharedSecretProviders(RunConfiguration runConfig) {

        RadiusClientService rcService = ServiceLocator.getService(KnownService.RadiusClient);
        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);

        runConfig.addSharedSecretProvider(new RadiusClientSharedSecretProvider(
            rcService,bcService.getEncodeSalt()
        ));

    }

    public static final void addAccessRequestFilters(RunConfiguration runConfig) {
       
       runConfig.addAccessRequestFilter(new SuperGluuAccessRequestFilter(
           getSuperGluuAccessRequestFilterConfig()
       ));
    }

    public static final void addAccountingRequestFilters(RunConfiguration runConfig) {
        //TODO implement this
    }

    private static final ServerConfiguration getServerConfiguration() {

        try {
            ServerConfigService scService  = ServiceLocator.getService(KnownService.ServerConfig);
            return scService.getServerConfiguration();
        }catch(ServiceException e) {
            throw new ServerFactoryException("Error getting server configuration",e);
        }
    }

    private static final SuperGluuAccessRequestFilterConfig getSuperGluuAccessRequestFilterConfig() {

        ServerConfiguration serverConfig = getServerConfiguration();
        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        return new SuperGluuAccessRequestFilterConfig(
            bcService,
            serverConfig
        );
    }
}