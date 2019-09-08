package org.gluu.radius.server.lifecycle;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.gluu.oxauth.client.JwkClient;
import org.gluu.oxauth.client.JwkResponse;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.oxauth.model.jwk.JSONWebKeySet;
import org.gluu.radius.KnownService;
import org.gluu.radius.model.Client;
import org.gluu.radius.ServiceLocator;
import org.gluu.radius.exception.ServerException;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.service.CryptoService;
import org.gluu.radius.service.OpenIdConfigurationService;
import org.gluu.radius.service.ServerConfigService;
import org.json.JSONObject;

public class Runner extends Thread {

    private static final Logger log = Logger.getLogger(Runner.class);
    private static final long sleeptimeout = 2000; // 2 seconds 
    private static final String PRIVATE_KEY_JWT_AUTH = "private_key_jwt";
    private static final long DEFAULT_JWKS_DOWNLOAD_INTERVAL = 5; // in minutes
    private static final String HEALTH_STATUS_FILE = "/tmp/gluu-radius-health";
    private GluuRadiusServer server;
    private boolean stop;
    private long keygenLastRun;
    private long jwksDownloadLastRun;
    private JSONWebKeySet currentKeyset;
    private boolean forceJwksDownload;

    public Runner(GluuRadiusServer server) {

        this.server = server;
        this.stop = false;
        this.keygenLastRun = System.currentTimeMillis();
        this.jwksDownloadLastRun = System.currentTimeMillis();
        this.forceJwksDownload = true;
    }

    @Override
    public void run() {

        try {
            if(!createHealthStatusFile()) {
                log.warn("Could not create the health status check file");
            }
            while(!stop) {
                log.debug("Performing background operations");
                performBackgroundOperations();
                log.debug("Background operations complete");
                Thread.sleep(sleeptimeout);
            }
            if(server!=null)
                server.stop();
        }catch(InterruptedException e) {
            // nothing to be done
        }catch(ServerException e) {
            log.error("Error while shutting down the server",e);
        }
    }

    public void stopRunning() {
        this.stop = true;
    }

    private final void performBackgroundOperations() {

        CryptoService cryptoService = ServiceLocator.getService(KnownService.Crypto);
        try {
            cryptoService.beginWriteOpts();
            BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
            long durationsincelastrun = System.currentTimeMillis() - keygenLastRun;
            long updateinterval = bcService.getKeygenInterval() * 86400 * 1000; 
            if(bcService.getKeygenInterval() != 0 && (durationsincelastrun >= updateinterval)) {
                currentKeyset = generateKeys(cryptoService);
                cryptoService.exportAuthPrivateKeyToPem();
                keygenLastRun = System.currentTimeMillis();
            }

            if(currentKeyset != null) {

                ServerConfigService scService = ServiceLocator.getService(KnownService.ServerConfig);
                String inum = scService.getServerConfiguration().getOpenidUsername();
                SignatureAlgorithm authsignalgorithm = bcService.getJwtAuthSignAlgo();
                saveOpenIdClientConfig(inum,currentKeyset,authsignalgorithm);
                currentKeyset = null; // once it's saved , no need saving it again
            }

            long durationsincelastdownload = System.currentTimeMillis() - jwksDownloadLastRun;
            long downloadinterval = DEFAULT_JWKS_DOWNLOAD_INTERVAL * 60 * 1000;
            if(forceJwksDownload == true || (durationsincelastdownload >= downloadinterval)) {
                if(downloadJwksServerKeys() == true) {
                    jwksDownloadLastRun = System.currentTimeMillis();  
                }
                forceJwksDownload = false;
            }

        }catch(Exception e) {
            log.error("Error while performing background operations",e);
        }finally {
            cryptoService.endWriteOpts();
        }
    }

    private final JSONWebKeySet generateKeys(CryptoService cryptoService) throws Exception {

        JSONWebKeySet keyset = null;
        keyset = cryptoService.generateKeys();
        return keyset;
    }

    private final void saveOpenIdClientConfig(String inum,JSONWebKeySet keyset,SignatureAlgorithm authSignatureAlgorithm) {

        OpenIdConfigurationService openidService = ServiceLocator.getService(KnownService.OpenIdConfig);
        Client client = openidService.loadOpenIdClient(inum);
        client.setJwks(keyset.toString());
        client.setTokenEndpointAuthMethod(PRIVATE_KEY_JWT_AUTH);
        client.setTokenEndpointAuthSigningAlg(authSignatureAlgorithm.name());
        openidService.saveOpenIdClient(client);
    }

    private final boolean downloadJwksServerKeys() {

        OpenIdConfigurationService openidService = ServiceLocator.getService(KnownService.OpenIdConfig);
        JwkClient jwkClient = new JwkClient(openidService.getJwksUri());
        JwkResponse response = jwkClient.exec();
        if(response == null || (response != null && response.getStatus() != 200)) {
            log.error("JWKS download failed");
            return false;
        }else {
            JSONObject jwks = response.getJwks().toJSONObject();
            CryptoService cryptoService = ServiceLocator.getService(KnownService.Crypto);
            cryptoService.setServerKeyset(jwks);
            return true;
        }
    }

    private final boolean createHealthStatusFile() {

        try {
            File hsf = new File(HEALTH_STATUS_FILE);
            if(hsf.exists())
                hsf.delete();
            return hsf.createNewFile();
        }catch(IOException e) {
            log.error("Error creating health status file",e);
            return false;
        }
    }
}