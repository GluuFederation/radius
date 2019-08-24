package org.gluu.radius;

import java.util.List;

import org.gluu.radius.exception.ServerFactoryException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.model.AuthScope;
import org.gluu.radius.model.ServerConfiguration;
import org.gluu.radius.ServiceLocator;
import org.gluu.radius.KnownService;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.server.filter.SuperGluuAccessRequestFilter;
import org.gluu.radius.server.filter.SuperGluuAccessRequestFilterConfig;
import org.gluu.radius.server.matcher.IpAddressMatcher;
import org.gluu.radius.server.matcher.CidrSubnetMatcher;
import org.gluu.radius.server.RadiusServerAdapter;
import org.gluu.radius.server.RunConfiguration;
import org.gluu.radius.server.tinyradius.TinyRadiusServerAdapter;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.service.CryptoService;
import org.gluu.radius.service.RadiusClientService;
import org.gluu.radius.service.OpenIdConfigurationService;
import org.gluu.radius.service.ServerConfigService;

public class ServerFactory {

    private ServerFactory() {

    }

    public static final GluuRadiusServer createServer() {

        RadiusClientService rcService = ServiceLocator.getService(KnownService.RadiusClient);
        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        return new GluuRadiusServer(createRunConfiguration(),createServerAdapter(),rcService,bcService.getEncodeSalt());
    }

    private static final RadiusServerAdapter createServerAdapter() {

        return new TinyRadiusServerAdapter();
    }

    private static final RunConfiguration createRunConfiguration() {

        ServerConfiguration serverConfig = getServerConfiguration();
        RunConfiguration runConfig = RunConfiguration.fromServerConfiguration(serverConfig);
        addClientMatchers(runConfig);
        addAccessRequestFilters(runConfig);
        addAccountingRequestFilters(runConfig);
        return runConfig;
    }

    private static final void addClientMatchers(RunConfiguration runConfig) {

        runConfig.addClientMatcher(new IpAddressMatcher());
        runConfig.addClientMatcher(new CidrSubnetMatcher());
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

    private static final List<AuthScope> getAuthScopes(ServerConfiguration serverConfig) {

        try {
            ServerConfigService scService = ServiceLocator.getService(KnownService.ServerConfig);
            return scService.getScopes(serverConfig);
        }catch(ServiceException e) {
            throw new ServerFactoryException("Error getting server configuration",e);
        }
    }

    private static final SuperGluuAccessRequestFilterConfig getSuperGluuAccessRequestFilterConfig() {

        ServerConfiguration serverConfig = getServerConfiguration();
        List<AuthScope> scopes = getAuthScopes(serverConfig);
        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        OpenIdConfigurationService openIdConfigService = ServiceLocator.getService(KnownService.OpenIdConfig);
        CryptoService cryptoService = ServiceLocator.getService(KnownService.Crypto);
        return new SuperGluuAccessRequestFilterConfig(
            bcService,
            serverConfig,
            scopes,
            openIdConfigService,
            cryptoService
        );
    }
}