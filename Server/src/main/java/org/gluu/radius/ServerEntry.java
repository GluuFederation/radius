package org.gluu.radius;

import java.io.IOException;
import java.security.Security;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.gluu.radius.exception.GenericLdapException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.exception.ServerException;
import org.gluu.radius.exception.ServerFactoryException;
import org.gluu.radius.ldap.LdapEntryManagerFactory;
import org.gluu.radius.openid.http.HttpClientFactory;
import org.gluu.radius.openid.http.HttpClientFactoryOptions;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.server.lifecycle.*;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.service.RadiusClientService;
import org.gluu.radius.service.ServerConfigService;

import org.gluu.site.ldap.persistence.LdapEntryManager;





public class ServerEntry {
    
    private static final Logger log = Logger.getLogger(ServerEntry.class);
    private static LdapEntryManager ldapEntryManager = null;

    public static void main(String [] args) {

        
        printStartupMessage();
        if(args.length==0) {
            log.error("Configuration file not specified on the command line. Exiting ... ");
            System.exit(-1);
        }

        String appConfigFile = args[0];
        log.info("Application bootstrap configuration file: "+appConfigFile);

        log.info("Initializing Security Components ... ");
        if(!initSecurity()) {
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
            log.error("RadiusClientService registration failed. Exiting ... ");
            System.exit(-1);
        }
        log.info("done");

        log.info("Initializing Http components ... ");
        if(!initHttpComponents()) {
            log.error("Http components init failed. Exiting ... ");
            System.exit(-1);
        }
        log.info("Done");

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

    private static final boolean initHttpComponents() {

        HttpClientFactoryOptions cfOptions = new HttpClientFactoryOptions();
        cfOptions.setVerifyHttpsHostname(false);
        boolean ret = false;
        try {
            HttpClientFactory.init(cfOptions);
            ret = true;
        }catch(Exception e) {
            log.error("Error initializing http client factory",e);
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