package org.gluu.radius;

import java.io.File;
import java.io.FileWriter;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.gluu.oxauth.model.jwk.Algorithm;
import org.gluu.oxauth.model.jwk.JSONWebKeySet;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.PersistenceEntryManagerFactory;
import org.gluu.persist.model.PersistenceConfiguration;
import org.gluu.radius.exception.GenericPersistenceException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.model.Client;
import org.gluu.radius.model.ServerConfiguration;
import org.gluu.radius.exception.ServerException;
import org.gluu.radius.exception.ServerFactoryException;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.server.lifecycle.*;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.service.CryptoService;
import org.gluu.radius.service.OpenIdConfigurationService;
import org.gluu.radius.service.RadiusClientService;
import org.gluu.radius.service.ServerConfigService;
import org.gluu.persist.service.StandalonePersistanceFactoryService;


public class ServerEntry {

    private static final Integer DEFAULT_CERT_EXPIRY_TIME = 365;
    private static final List<Algorithm> authSignatureAlgorithms = Arrays.asList(
        Algorithm.RS256,
        Algorithm.RS384,
        Algorithm.RS512
    );
    private static final Logger log = Logger.getLogger(ServerEntry.class);
    private static StandalonePersistanceFactoryService standalonePfService = null;
    private static PersistenceEntryManagerFactory persistenceEntryManagerFactory = null;
    private static PersistenceEntryManager persistenceEntryManager = null;
    private static GluuRadiusServer serverInstance = null;

    private static final String SERVER_OPT = "server";
    private static final String CRYPTOGEN_OPT = "cryptogen";
    private static final String CONFIG_FILE_OPT = "config_file";
    private static final String PRIVATE_KEY_OUT_OPT = "private_key_out";
    private static final String RADIATOR_CONFIG_OUT_OPT = "radiator_config_out";
    private static final String HELP_OPT = "help";
    private static final String PRIVATE_KEY_JWT_AUTH = "private_key_jwt";

    public static void main(String[] args) {
        
        final Options opts = createCliOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdline = null;

        try {
            cmdline = parser.parse(opts,args);
        }catch(ParseException e) {
            log.error("Failed to start application",e);
            e.printStackTrace();
            printHelpMsg(opts);
            System.exit(-1);
        }

        validateCmdlineOptions(cmdline, opts);

        if(cmdline.hasOption(HELP_OPT)) {
            printHelpMsg(opts);
            System.exit(0);
        }

        printStartupMessage();

        String bootstrapconfig = cmdline.getOptionValue(CONFIG_FILE_OPT);
        String mode = "server";
        if(cmdline.hasOption(CRYPTOGEN_OPT))
            mode = "cryptographic generation";
        log.info(String.format("Starting application in %s mode. Configuration file: %s",mode,bootstrapconfig));

        log.info("Initializing security components");
        if(!initSecurity()) {
            log.error("Security components initialization failed");
            System.exit(-1);
        }
        log.info("Security components initialization successful");

        log.info("Registering bootstrap configuration service");
        if(!registerBootstrapConfigService(bootstrapconfig)) {
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
        }catch(GenericPersistenceException e) {
            log.error("Persistence layer initialization failed",e);
            System.exit(-1);
        }catch(Exception ex) {
            log.error("Persistence layer initialization failed",ex);
            System.exit(-1);
        }
        log.info("Persistence layer initialization successful");

        log.info("Registering clients service");
        if(!registerRadiusClientService()) {
            log.error("Clients service registration failed");
            System.exit(-1);
        }
        log.info("Clients service registration successful");

        log.info("Registering server configuration service");
        if(!registerServerConfigService()) {
            log.error("Server configuration service registration failed");
            System.exit(-1);
        }
        log.info("Server configuration service registration failed");

        log.info("Registering OpenID configuration service");
        if(!registerOpenIdConfigurationService()) {
            log.error("OpenID configuration service registration failed");
            System.exit(-1);
        }
        log.info("OpenID configuration service registration successful");


        log.info("Registering cryptographic service");
        if(!registerCryptoService()) {
            log.error("Cryptographic service registration failed");
            System.exit(-1);
        }
        log.info("Cryptographic service registration successful");

        if(cmdline.hasOption(SERVER_OPT))
            runServer(cmdline);
        else if(cmdline.hasOption(CRYPTOGEN_OPT))
            runCryptoGenerator(cmdline);

    }


    private static final Options createCliOptions() {

        Options opts = new Options();
        opts.addOption(CONFIG_FILE_OPT,true,
            "Configuration file. Non-optional.");
        opts.addOption(SERVER_OPT,false,
            "Run Gluu Radius as a server. This option and -cryptogen are mutually exclusive.");
        opts.addOption(CRYPTOGEN_OPT,false,
            "Use Gluu Radius to (re-)generate cryptographic material used for authentication and token signing/verification. " +
            "This option and -server are mutually exclusive.");
        opts.addOption(PRIVATE_KEY_OUT_OPT,true,
            "Path to file where the private key used by GluuRadiator for authentication will be stored." + 
            "Mandatory if -cryptogen is specified.");
        opts.addOption(RADIATOR_CONFIG_OUT_OPT,true,
            "Path to file where the radiator configuration (auth module) will be stored. " +
            "Used only when -cryptogen is specified. Optional.");
        opts.addOption(HELP_OPT,false,
            "Prints this help message.");
        opts.getOption(CONFIG_FILE_OPT).setArgs(1);
        opts.getOption(PRIVATE_KEY_OUT_OPT).setArgs(1);

        return opts;
    }

    private static final void validateCmdlineOptions(CommandLine cmdline,Options opts) {

        if(!cmdline.hasOption(CONFIG_FILE_OPT)) {
            log.error("Use the -config_file option to specify a configuration file.");
            printHelpMsg(opts);
            System.exit(-1);
        }

        if(cmdline.hasOption(SERVER_OPT) && cmdline.hasOption(CRYPTOGEN_OPT)) {
            log.error("The options -server and -cryptogen cannot be specified simultaneously.");
            printHelpMsg(opts);
            System.exit(-1);
        }

        if(!cmdline.hasOption(SERVER_OPT) && !cmdline.hasOption(CRYPTOGEN_OPT)) {
            log.error("Please specify -server or -cryptogen as arguments.");
            printHelpMsg(opts);
            System.exit(-1);
        }

    }

    private static final void printHelpMsg(Options opts) {

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("gluu-radius -config_file file_path [-server] [-cryptogen -private_key_out path]",opts);
    }

    private static final void printStartupMessage() {

        log.info(" ");
        log.info(" ");
        log.info("+---------------------------------------------------------+");
        log.info("+ Gluu Radius Server                                      +");
        log.info("+ Copyright (c) Gluu Inc.                                 +");
        log.info("+---------------------------------------------------------+");
    }

    private static final void runServer(CommandLine cmdline) {

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

        log.info("Server initialization complete");
    }

    private static final void runCryptoGenerator(CommandLine cmdline) {
       log.info("Generating cryptographic material");
       try {
           CryptoService cryptoService = ServiceLocator.getService(KnownService.Crypto);
           ServerConfigService scService = ServiceLocator.getService(KnownService.ServerConfig);
           BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
           OpenIdConfigurationService openIdService = ServiceLocator.getService(KnownService.OpenIdConfig);
           JSONWebKeySet jwks = cryptoService.generateKeys();
           String clientInum = scService.getServerConfiguration().getOpenidUsername();
           Client client = openIdService.loadOpenIdClient(clientInum);
           client.setTokenEndpointAuthMethod(PRIVATE_KEY_JWT_AUTH);
           client.setTokenEndpointAuthSigningAlg(bcService.getJwtAuthSignAlgo().name());
           client.setJwks(jwks.toString());
           openIdService.saveOpenIdClient(client); 

           if(cmdline.hasOption(PRIVATE_KEY_OUT_OPT)) {
               File pkey_file = new File(cmdline.getOptionValue(PRIVATE_KEY_OUT_OPT));
               cryptoService.exportAuthPrivateKeyToPem(pkey_file);
               if(cmdline.hasOption(RADIATOR_CONFIG_OUT_OPT)) {
                   String radiatorconfig = generateRadiatorConfiguration(scService, bcService, cryptoService,pkey_file);
                   FileWriter fw = new FileWriter(cmdline.getOptionValue(RADIATOR_CONFIG_OUT_OPT));
                   fw.write(radiatorconfig);
                   fw.close();
               }
           }else {
               if(!cmdline.hasOption(RADIATOR_CONFIG_OUT_OPT)) {
                   log.warn("radiator configuration file specified without pem output file");
               }
               log.info("Skipping private key export and radiator configuration generation");
           }
       }catch(Exception e) {
           log.error("Cryptographic material generation failed",e);
           System.exit(-1);
       }
       log.info("Cryptographic material generation complete");
    }
 
    private static final String generateRadiatorConfiguration(ServerConfigService scService,BootstrapConfigService bcService,
        CryptoService cryptoService, File pkeyfile) {

        StringBuilder sb = new StringBuilder();
        ServerConfiguration serverConfig = scService.getServerConfiguration();
        sb.append("<AuthBy GLUU>\r\n");
        sb.append(String.format("    gluuServerUrl %s\r\n",serverConfig.getOpenidBaseUrl()));
        sb.append(String.format("    clientId %s\r\n",serverConfig.getOpenidUsername()));
        sb.append(String.format("    signaturePkeyPassword %s\r\n",bcService.getJwtKeyStorePin()));
        sb.append(String.format("    signaturePkey file:\"/%s\"\r\n",pkeyfile.getAbsolutePath()));
        sb.append(String.format("    signaturePkeyId %s\r\n",cryptoService.getAuthSigningKeyId()));
        sb.append(String.format("    signatureAlgorithm %s\r\n",bcService.getJwtAuthSignAlgo().name()));
        sb.append(String.format("    sslVerifyCert %s\r\n","yes"));
        sb.append(String.format("    authScheme %s","twostep\r\n"));
        sb.append("</AuthBy>\r\n");
        return sb.toString();
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
            cryptoService.exportAuthPrivateKeyToPem();
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
        standalonePfService = new StandalonePersistanceFactoryService();
        PersistenceConfiguration persistenceConfiguration = standalonePfService.loadPersistenceConfiguration(null);

        Properties connectionProperties = bcService.preparePersistenceProperties(persistenceConfiguration);
        persistenceEntryManagerFactory = standalonePfService.getPersistenceEntryManagerFactory(persistenceConfiguration);
        return persistenceEntryManagerFactory.createEntryManager(connectionProperties);
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