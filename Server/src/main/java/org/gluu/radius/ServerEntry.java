package org.gluu.radius;

import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.gluu.oxauth.model.jwk.Algorithm;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.radius.exception.GenericPersistenceException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.exception.ServerException;
import org.gluu.radius.exception.ServerFactoryException;
import org.gluu.oxauth.model.registration.Client;
import org.gluu.radius.persist.PersistenceBackendType;
import org.gluu.radius.persist.PersistenceEntryManagerFactory;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.server.lifecycle.*;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.service.CryptoService;
import org.gluu.radius.service.OpenIdConfigurationService;
import org.gluu.radius.service.RadiusClientService;
import org.gluu.radius.service.ServerConfigService;


public class ServerEntry {

    private static final Integer DEFAULT_CERT_EXPIRY_TIME = 2;
    private static final List<Algorithm> authSignatureAlgorithms = Arrays.asList(
        Algorithm.RS256,
        Algorithm.RS384,
        Algorithm.RS512
    );
    private static final Logger log = Logger.getLogger(ServerEntry.class);
    private static PersistenceEntryManager persistenceEntryManager = null;
    private static GluuRadiusServer serverInstance = null;

    public static void main(String[] args) {

        printStartupMessage();
        if (args.length == 0) {
            log.error("Configuration file not specified on the command line.");
            System.exit(-1);
        }

        String appConfigFile = args[0];
        log.info("Initializing server");
        log.info("Application bootstrap configuration file: " + appConfigFile);

        log.info("Initializing security components");
        if(!initSecurity()) {
            log.error("Security components initialization failed");
            System.exit(-1);
        }
        log.info("Security components initialization successful");

        log.info("Registering bootstrap configuration service");
        if(!registerBootstrapConfigService(appConfigFile)) {
            log.error("Bootstrap configuration service registration failed");
            System.exit(-1);
        }
        log.info("Bootstrap configuration service registered");

        log.info("Initializing persistence layer");
        try {
            persistenceEntryManager = createPersistenceEntryManager();
            if(persistenceEntryManager == null) {
                log.error("Persistence layer initialization failed");
                System.exit(-1);
            }
            String clientdn = "inum=0008-86e0603c-2191-457c-b492-33ac9a9e7a30,ou=clients,o=gluu";
            Client myclient = persistenceEntryManager.find(Client.class,clientdn);
            log.info(String.format("Client name: %s",myclient.getClientName()));
            
        }catch(GenericPersistenceException e) {
            log.error("Persistence layer initialization failed",e);
            System.exit(-1);
        }
        log.info("Persistence layer initialized");

        log.info("Registering clients service");
        if(!registerRadiusClientService()) {
            log.error("Clients service registration failed");
            System.exit(-1);
        }
        log.info("Clients service registration complete");

        log.info("Registering server configuration service");
        if(!registerServerConfigService()) {
            log.error("Server configuration service registration failed");
            System.exit(-1);
        }
        log.info("Server configuration service registration complete");

        log.info("Registering OpenID configuration service");
        if(!registerOpenIdConfigurationService()) {
            log.error("OpenID configuration service registration failed");
            System.exit(-1);
        }
        log.info("OpenID configuration service registration complete");

        log.info("Registering cryptographic service");
        if(!registerCryptoService()) {
            log.error("Cryptographic service registration failed");
            System.exit(-1);
        }
        log.info("Cryptographic service registration complete");

        if(isListenEnabled()) {
            log.info("Starting radius server");
            if(!startServer()) {
                log.error("Radius server startup failed");
                System.exit(-1);
            }
            log.info("Radius server started");
        }

        log.info("Registering server shutdown hook");
        registerServerShutdownHook();
        log.info("Server shutdown hook registered");

        log.info("Initialization complete");
        
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

    private static final boolean isListenEnabled() {

        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        return bcService.isListenEnabled();
    }

    private static final boolean registerRadiusClientService() {

        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        RadiusClientService rcService = new RadiusClientService(persistenceEntryManager,bcService.getRadiusClientConfigDN());
        ServiceLocator.registerService(KnownService.RadiusClient,rcService);
        return true; 
    }

    private static final boolean registerServerConfigService() {

        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        ServerConfigService scService = new ServerConfigService(persistenceEntryManager,bcService.getRadiusConfigDN());
        ServiceLocator.registerService(KnownService.ServerConfig,scService);
        return true;
    }

    private static final boolean registerOpenIdConfigurationService() {

        boolean ret = false;
        try {
            BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
            ServerConfigService scService = ServiceLocator.getService(KnownService.ServerConfig);
            OpenIdConfigurationService openIdConfigService = new OpenIdConfigurationService(scService,
            persistenceEntryManager,bcService.getOpenidClientsDN());
            ServiceLocator.registerService(KnownService.OpenIdConfig,openIdConfigService);
            return true;
        }catch(ServiceException e) {
            log.error(e.getMessage(),e);
        }
        return ret;
    }

    private static final boolean registerCryptoService() {

        boolean ret = false;
        try {
            BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
            Integer expiry = DEFAULT_CERT_EXPIRY_TIME;
            CryptoService cryptoService = new CryptoService(bcService,authSignatureAlgorithms,expiry,0);
            ServiceLocator.registerService(KnownService.Crypto,cryptoService);
            ret = true;
        }catch(ServiceException e) {
            log.error(e.getMessage(),e);
        }catch(Exception e) {
            log.error(e.getMessage(),e);
        }
        return ret;
    }

    

    private static final PersistenceEntryManager createPersistenceEntryManager() {

        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);

        if (bcService.getPersistenceBackend() == PersistenceBackendType.PERSISTENCE_BACKEND_LDAP) {
            Properties props = bcService.getBackendConfiguration(PersistenceBackendType.PERSISTENCE_BACKEND_LDAP);
            return PersistenceEntryManagerFactory.createLdapPersistenceEntryManager(props);
        }else if(bcService.getPersistenceBackend() == PersistenceBackendType.PERSISTENCE_BACKEND_COUCHBASE) {
            Properties props = bcService.getBackendConfiguration(PersistenceBackendType.PERSISTENCE_BACKEND_COUCHBASE);
            return PersistenceEntryManagerFactory.createCouchbasePersistenceEntryManager(props);
        }else if(bcService.getPersistenceBackend() == PersistenceBackendType.PERSISTENCE_BACKEND_HYBRID) {
            Properties hybridprops = bcService.getBackendConfiguration(PersistenceBackendType.PERSISTENCE_BACKEND_HYBRID);
            Properties ldap_props = bcService.getBackendConfiguration(PersistenceBackendType.PERSISTENCE_BACKEND_LDAP);
            Properties couchbaseprops = bcService.getBackendConfiguration(PersistenceBackendType.PERSISTENCE_BACKEND_COUCHBASE);
            return PersistenceEntryManagerFactory.createHybridPersistenceEntryManager(hybridprops,ldap_props,couchbaseprops);
        }else
            log.error("unsupported persistence backend");

        return null;
    }


    private static final boolean startServer() {

        boolean ret = false;
        try {
            GluuRadiusServer server = ServerFactory.createServer();
            server.run();
            ret = true;
            serverInstance = server;
        }catch(ServerException e) {
            log.error("Error running radius server",e);
        }catch(ServerFactoryException e) {
            log.error("Error running radius server",e);
        }
        return ret;
    }


    private static final void registerServerShutdownHook() {

        Runner runner = new Runner(serverInstance);
        runner.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(runner));
    }
    
}