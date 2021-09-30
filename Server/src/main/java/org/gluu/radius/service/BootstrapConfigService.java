package org.gluu.radius.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import java.util.Properties;

import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.util.EncDecUtil;
import org.apache.log4j.Logger;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.persist.model.PersistenceConfiguration;
import org.gluu.orm.util.properties.FileConfiguration;
import org.gluu.util.security.PropertiesDecrypter;
import org.gluu.util.security.StringEncrypter;
import org.gluu.util.security.StringEncrypter.EncryptionException;

public class BootstrapConfigService  {

    private enum AuthScheme {
        ONE_STEP_AUTH,
        TWO_STEP_AUTH
    }

    private enum BootstrapConfigKeys {
        ListenEnable("radius.listen.enable"),
        AuthScheme("radius.auth.scheme"),
        SaltFile("radius.config.saltfile"),
        PersistenceConfigFile("radius.persist.config"),
        PersistenceType("persistence.type"),
        JwtKeyStoreFile("radius.jwt.keyStoreFile"),
        JwtKeyStorePin("radius.jwt.keyStorePin"),
        JwtAuthKeyId("radius.jwt.auth.keyId"),
        JwtKeyGenInterval("radius.jwt.keygen.interval"),
        ConfigDN("oxradius_ConfigurationEntryDN"),
        OpenIdClientsDN("radius.openid_clients_DN"),
        ClientsDN("radius.clients_DN"),
        JwtAuthSignatureAlgorithm("radius.jwt.auth.signAlgorithm"),
        DefaultHybridStorage("storage.default");

        private String keyName;

        private BootstrapConfigKeys(String keyName) {

            this.keyName = keyName;
        }

        public String getKeyName() {

            return this.keyName;
        }
    }

    private static final String encodeSaltKey = "encodeSalt";
    private static final Logger LOG = Logger.getLogger(BootstrapConfigService.class);

    private boolean listenEnabled;
    private AuthScheme scheme;
    private String salt;
    private Properties persistenceConfig;
    private String persistenceConfigFile;
    private String jwtKeyStoreFile;
    private String jwtKeyStorePin;
    private String jwtAuthKeyId;
    private Long keygenInterval;
    private SignatureAlgorithm jwtAuthSignAlgo;
    private String configDN;
    private String clientsDN;
    private String openidClientsDN;

    public BootstrapConfigService(String appConfigFile) { 

        Properties oxRadiusConfig = loadPropertiesFromFile(appConfigFile);

        String listen = oxRadiusConfig.getProperty(BootstrapConfigKeys.ListenEnable.getKeyName());
        if(listen == null)
            throw new ServiceException("Server listening status not specified.");
        
        listen.trim();
        if(listen.equalsIgnoreCase("true"))
            listenEnabled = true;
        else if(listen.equalsIgnoreCase("false"))
            listenEnabled = false;
        else 
            throw new ServiceException("Invalid value for property radius.listen.enable.");
        
        String authscheme = oxRadiusConfig.getProperty(BootstrapConfigKeys.AuthScheme.getKeyName());
        if(authscheme == null)
            throw new ServiceException("Unspecified authentication scheme");
        authscheme = authscheme.trim();
        if(authscheme.equalsIgnoreCase("onestep")) {
            scheme = AuthScheme.ONE_STEP_AUTH;
        }else if(authscheme.equalsIgnoreCase("twostep")) {
            scheme = AuthScheme.TWO_STEP_AUTH;
        }else
            throw new ServiceException("Invalid/Unknown authscheme specified in configuration."); 

        String saltFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.SaltFile.getKeyName());
        if(saltFile == null)
            throw new ServiceException("Salt file not found");
        saltFile = saltFile.trim();
        this.salt = loadEncodeSalt(saltFile);
        persistenceConfigFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.PersistenceConfigFile.getKeyName());
        File persistFileObj = new File(persistenceConfigFile);
        if(!persistFileObj.exists())
            throw new ServiceException("Persistence configuration file not found");
        
        persistenceConfig = loadPropertiesFromFile(persistenceConfigFile);
        
        this.jwtKeyStorePin = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtKeyStorePin.getKeyName());
        this.jwtKeyStorePin = EncDecUtil.decode(this.jwtKeyStorePin, salt);
        this.jwtKeyStoreFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtKeyStoreFile.getKeyName());
        String signalgo = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtAuthSignatureAlgorithm.getKeyName());
        this.jwtAuthSignAlgo = SignatureAlgorithm.fromString(signalgo);
        this.jwtAuthKeyId = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtAuthKeyId.getKeyName());
        String krinterval = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtKeyGenInterval.getKeyName());
        try {
            keygenInterval = Long.parseLong(krinterval);
            if(keygenInterval < 0)
                throw new ServiceException("Keygen interval lesser than 0.");
        }catch(NumberFormatException e) {
            throw new ServiceException("Invalid value for keygen interval.");
        }

        this.configDN = persistenceConfig.getProperty(BootstrapConfigKeys.ConfigDN.getKeyName());
        if(this.configDN == null)
            throw new ServiceException("Server configuration base DN missing from configuration.");
        this.clientsDN = oxRadiusConfig.getProperty(BootstrapConfigKeys.ClientsDN.getKeyName());
        if(this.clientsDN == null)
            throw new ServiceException("Radius clients base DN missing from configuration.");
        this.openidClientsDN = oxRadiusConfig.getProperty(BootstrapConfigKeys.OpenIdClientsDN.getKeyName());
        if(this.openidClientsDN == null)
            throw new ServiceException("OpenID clients base DN missing from configuration.");
    }

    

    public final String getEncodeSalt() {

        return this.salt;
    }

    public final boolean isListenEnabled() {

        return this.listenEnabled;
    }

    public final boolean isOneStepAuth() {

        return this.scheme == AuthScheme.ONE_STEP_AUTH;
    }

    public final boolean isTwoStepAuth() {
        
        return this.scheme == AuthScheme.TWO_STEP_AUTH;
    }


    public final String getRadiusConfigDN() {

        return this.configDN;
    }

    public final String getRadiusClientConfigDN() {

        return this.clientsDN;
    }

    public final String getOpenidClientsDN() {

        return this.openidClientsDN;
    }

    public final String getJwtKeyStoreFile() {

        return this.jwtKeyStoreFile;
    }

    public final String getJwtKeyStorePin() {

        return this.jwtKeyStorePin;
    }

    public final String getJwtAuthKeyId() {

        return this.jwtAuthKeyId;
    }

    public final long getKeygenInterval() {

        return this.keygenInterval;
    }

    public final SignatureAlgorithm getJwtAuthSignAlgo() {

        return this.jwtAuthSignAlgo;
    }

    public String getPersistenceConfigFile() {

        return this.persistenceConfigFile;
    }

    public void setPersistenceConfigFile(String persistenceConfigFile) {

        this.persistenceConfigFile = persistenceConfigFile;
    }

    private String loadEncodeSalt(String saltFile) {

        Properties props = loadPropertiesFromFile(saltFile);
        return props.getProperty(encodeSaltKey);
    }

    private Properties loadPropertiesFromFile(String filename) {

        FileInputStream fileistream = null;
        try {
            fileistream = new FileInputStream(filename);
            Properties props = new Properties();
            props.load(fileistream);
            fileistream.close();
            return props;
        }catch(IOException e) {
            throw new ServiceException("Could not load properties from file "+filename,e);
        }catch(IllegalArgumentException e) {
            throw new ServiceException("Could not load properties from file "+filename,e);
        }finally {
            if(fileistream != null)
            try {
                fileistream.close();
            }catch(IOException e) {
                //ignore any exception thrown here
            }
        }
    }

    public Properties preparePersistenceProperties(PersistenceConfiguration persistenceConfiguration) {

        FileConfiguration persistenceConfig = persistenceConfiguration.getConfiguration();
        Properties connectionProperties = (Properties) persistenceConfig.getProperties();

        Properties decryptedConnectionProperties;
        try {
            decryptedConnectionProperties = PropertiesDecrypter.decryptAllProperties(StringEncrypter.defaultInstance(), connectionProperties, this.salt);
        }catch(EncryptionException ex) {
            throw new ServiceException("Failed to decript configuration properties", ex);
        }
        return decryptedConnectionProperties;

    }
}