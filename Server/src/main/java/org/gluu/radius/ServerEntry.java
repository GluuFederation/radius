package org.gluu.radius;

import java.security.Security;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.gluu.radius.exception.GenericLdapException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.exception.ServerException;
import org.gluu.radius.exception.ServerFactoryException;
import org.gluu.radius.ldap.LdapEntryManagerFactory;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.server.lifecycle.*;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.service.OpenIdConfigurationService;
import org.gluu.radius.service.RadiusClientService;
import org.gluu.radius.service.ServerConfigService;

import org.gluu.site.ldap.persistence.LdapEntryManager;



public class ServerEntry {

    private static final Logger log = Logger.getLogger(ServerEntry.class);
    private static LdapEntryManager ldapEntryManager = null;

    public static void main(String[] args) {

        printStartupMessage();
        if (args.length == 0) {
            log.error("Configuration file not specified on the command line. Exiting ... ");
            System.exit(-1);
        }

        String appConfigFile = args[0];
        log.info("Application bootstrap configuration file: " + appConfigFile);

        log.info("Initializing Security Components ... ");
        if (!initSecurity()) {
            log.error("Could not initialize security components");
            System.exit(-1);
        }

        
        log.info("Registering BootstrapConfigService ... ");
        if(!registerBootstrapConfigService(appConfigFile)) {
            log.error("BootstrapConfigService registration failed. Exiting ... ");
            System.exit(-1);
        }
        log.info("done");

        try {
            ldapEntryManager = createLdapEntryManager();
        }catch(GenericLdapException e) {
            log.error("LdapEntryManager creation failed. Exiting ... ",e);
            System.exit(-1);
        }

        log.info("Registering RadiusClientService ... ");
        if(!registerRadiusClientService()) {
            log.error("RadiusClientService registration failed. Exiting ... ");
            System.exit(-1);
        }
        log.info("done");

        log.info("Registering ServerConfigService ... ");
        if(!registerServerConfigService()) {
            log.error("ServerConfigService registration failed. Exiting ... ");
            System.exit(-1);
        }
        log.info("done");

        log.info("Registering OpenIdConfigurationService ...");
        if(!registerOpenIdConfigurationService()) {
            log.error("OPenIdConfigurationService registration failed. Exiting ... ");
            System.exit(-1);
        }
        log.info("done");

        log.info("Starting Radius Server ...");
        if(!startServer()) {
            log.error("Error Starting GluuRadiusServer. Exiting ... ");
            System.exit(-1);
        }
        log.info("done");
        

        
    }

    private static final void printStartupMessage() {

        log.info(" ");
        log.info(" ");
        log.info("+---------------------------------------------------------+");
        log.info("+ Gluu Radius Server                                      +");
        log.info("+ Copyright (c) Gluu Inc.                                 +");
        log.info("+---------------------------------------------------------+");
    }

    private static final boolean initSecurity() {

        Security.addProvider(new BouncyCastleProvider());
        return true;
    }

    private static final boolean  registerBootstrapConfigService(String appConfigFile) {

        boolean success = false;
        try {
            ServiceLocator.registerService(KnownService.BootstrapConfig,new BootstrapConfigService(appConfigFile));
            success = true;
        }catch(ServiceException e) {
            log.error(e.getMessage(),e);
        }
        return success;
    }

    private static final boolean registerRadiusClientService() {

        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        RadiusClientService rcService = new RadiusClientService(ldapEntryManager,bcService.getRadiusClientConfigDN());
        ServiceLocator.registerService(KnownService.RadiusClient,rcService);
        return true; 
    }

    private static final boolean registerServerConfigService() {

        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        ServerConfigService scService = new ServerConfigService(ldapEntryManager,bcService.getRadiusConfigDN());
        ServiceLocator.registerService(KnownService.ServerConfig,scService);
        return true;
    }

    private static final boolean registerOpenIdConfigurationService() {

        boolean ret = false;
        try {
            ServerConfigService scService = ServiceLocator.getService(KnownService.ServerConfig);
            OpenIdConfigurationService openIdConfigService = new OpenIdConfigurationService(scService);
            ServiceLocator.registerService(KnownService.OpenIdConfig,openIdConfigService);
            return true;
        }catch(ServiceException e) {
            log.error(e.getMessage(),e);
        }
        return ret;
    }

    private static final LdapEntryManager createLdapEntryManager() {

        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        return LdapEntryManagerFactory.createLdapEntryManager(bcService.getLdapConnectionParams());
    }


    private static final boolean startServer() {

        boolean ret = false;
        try {
            GluuRadiusServer serverInstance = ServerFactory.createServer();
            serverInstance.run();
            registerServerShutdownHook(serverInstance);
            ret = true;
        }catch(ServerException e) {
            log.error("Error running radius server",e);
        }catch(ServerFactoryException e) {
            log.error("Error running radius server",e);
        }
        return ret;
    }


    private static final void registerServerShutdownHook(GluuRadiusServer server) {

        Runner runner = new Runner(server);
        runner.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(runner));
    }
    
}