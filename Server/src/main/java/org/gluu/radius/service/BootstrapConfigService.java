package org.gluu.radius.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.util.EncDecUtil;
import org.gluu.radius.persist.PersistenceBackendType;
import org.apache.log4j.Logger;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;

public class BootstrapConfigService  {

    private enum BootstrapConfigKeys {
        SaltFile("radius.config.saltfile"),
        PersistenceConfigFile("radius.persist.config"),
        PersistenceType("persistence.type"),
        JwtKeyStoreFile("radius.jwt.keyStoreFile"),
        JwtKeyStorePin("radius.jwt.keyStorePin"),
        JwtAuthKeyId("radius.jwt.auth.keyId"),
        ConfigDN("oxradius_ConfigurationEntryDN"),
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
    private static final String bindPasswordKey = "bindPassword";
    private static final String authPasswordKey = "auth.userPassword";
    private static final String trustStorePinKey_Ldap = "ssl.trustStorePin";
    private static final String trustStorePinKey_Couchbase = "ssl.trustStore.pin";
    private static final Logger log = Logger.getLogger(BootstrapConfigService.class);

    private String salt;
    private Properties persistenceConfig;
    private PersistenceBackendType persistenceBackend;
    private PersistenceBackendType defaultHybridBackend;
    private Map<PersistenceBackendType,Properties> persistenceBackendConfig;
    private String jwtKeyStoreFile;
    private String jwtKeyStorePin;
    private String jwtAuthKeyId;
    private SignatureAlgorithm jwtAuthSignAlgo;
    private String configDN;
    private String clientsDN;

    public BootstrapConfigService(String appConfigFile) { 

        Properties oxRadiusConfig = loadPropertiesFromFile(appConfigFile);
        String saltFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.SaltFile.getKeyName());
        if(saltFile == null)
            throw new ServiceException("Salt file not found");
        this.salt = loadEncodeSalt(saltFile);
        this.persistenceBackendConfig = new HashMap<PersistenceBackendType,Properties>();
        String persistFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.PersistenceConfigFile.getKeyName());
        File persistFileObj = new File(persistFile);
        if(persistFileObj.exists() == false)
            throw new ServiceException("Persistence configuration file not found");
        String persistDir = persistFileObj.getParent();
        if(persistDir == null || (persistDir !=null && persistDir.isEmpty()))
            throw new ServiceException("Could not determine db backend type");
        
        persistenceConfig = loadPropertiesFromFile(persistFile);
        String backendtype = persistenceConfig.getProperty(BootstrapConfigKeys.PersistenceType.getKeyName());
        if(backendtype == null)
            throw new ServiceException("Backend type not found");
        log.debug("Persistence backend: " + backendtype);
        if(backendtype.equalsIgnoreCase("opendj") || backendtype.equalsIgnoreCase("ldap")) {
            loadLdapBackendConfiguration(persistDir);
            persistenceBackend = PersistenceBackendType.PERSISTENCE_BACKEND_LDAP;
        }else if(backendtype.equalsIgnoreCase("couchbase")) {
            loadLdapBackendConfiguration(persistDir);
            persistenceBackend = PersistenceBackendType.PERSISTENCE_BACKEND_COUCHBASE;
        }else if(backendtype.equalsIgnoreCase("hybrid")) {
            loadHybridBackendConfiguration(persistDir);
            persistenceBackend = PersistenceBackendType.PERSISTENCE_BACKEND_HYBRID;
        }else
            throw new ServiceException("Unknown persistence backend " + backendtype);

        this.jwtKeyStorePin = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtKeyStorePin.getKeyName());
        this.jwtKeyStorePin = EncDecUtil.decode(this.jwtKeyStorePin, salt);
        this.jwtKeyStoreFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtKeyStoreFile.getKeyName());
        String signalgo = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtAuthSignatureAlgorithm.getKeyName());
        this.jwtAuthSignAlgo = SignatureAlgorithm.fromString(signalgo);
        this.jwtAuthKeyId = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtAuthKeyId.getKeyName());

        this.configDN = persistenceConfig.getProperty(BootstrapConfigKeys.ConfigDN.getKeyName());
        if(this.configDN == null)
            throw new ServiceException("Server configuration base DN missing from configuration.");
        this.clientsDN = oxRadiusConfig.getProperty(BootstrapConfigKeys.ClientsDN.getKeyName());
        if(this.clientsDN == null)
            throw new ServiceException("Radius clients base DN missing from configuration.");
    }

    private void loadLdapBackendConfiguration(String persistDir) {

        String ldapConfigFile = String.format("%s/gluu-ldap.properties",persistDir);
        if(new File(ldapConfigFile).exists() == false)
            throw new ServiceException("Ldap configuration file not found");
        Properties props = loadPropertiesFromFile(ldapConfigFile);
        persistenceBackend = PersistenceBackendType.PERSISTENCE_BACKEND_LDAP;
        String bindPassword = props.getProperty(bindPasswordKey);
        String trustStorePin = props.getProperty(trustStorePinKey_Ldap);
        bindPassword = EncDecUtil.decode(bindPassword,salt);
        trustStorePin = EncDecUtil.decode(trustStorePin,salt);
        props.setProperty(bindPasswordKey,bindPassword);
        props.setProperty(trustStorePinKey_Ldap,trustStorePin);
        persistenceBackendConfig.put(PersistenceBackendType.PERSISTENCE_BACKEND_LDAP,props);
    }

    private void loadCouchbaseBackendConfiguration(String persistDir) {

        String couchbaseConfigFile = String.format("%s/gluu-couchbase.properties",persistDir);
        if(new File(couchbaseConfigFile).exists() == false)
            throw new ServiceException("Couchbase configuration file not found");
        Properties props = loadPropertiesFromFile(couchbaseConfigFile);
        String authPassword = props.getProperty(authPasswordKey);
        String trustStorePin = props.getProperty(trustStorePinKey_Couchbase);
        authPassword = EncDecUtil.decode(authPassword,salt);
        trustStorePin = EncDecUtil.decode(trustStorePin,salt);
        props.setProperty(authPasswordKey,authPassword);
        props.setProperty(trustStorePinKey_Couchbase,trustStorePin);
        persistenceBackendConfig.put(PersistenceBackendType.PERSISTENCE_BACKEND_COUCHBASE,props);
        
    }

    private final void loadHybridBackendConfiguration(String persistDir) {

        String hybridConfigFile = String.format("%s/gluu-hybrid.properties",persistDir);
        if(new File(hybridConfigFile).exists() == false)
            throw new ServiceException("Hybrid configuration file not found");
        
        Properties props = loadPropertiesFromFile(hybridConfigFile);
        String storage = props.getProperty(BootstrapConfigKeys.DefaultHybridStorage.getKeyName());
        if(storage == null)
            throw new ServiceException("No default backend specified in hybrid storage configuration.");
        if(storage.equalsIgnoreCase("opendj") || storage.equalsIgnoreCase("ldap"))
            defaultHybridBackend = PersistenceBackendType.PERSISTENCE_BACKEND_LDAP;
        else if(storage.equalsIgnoreCase("couchbase"))
            defaultHybridBackend = PersistenceBackendType.PERSISTENCE_BACKEND_COUCHBASE;
        else {
            throw new ServiceException("Unknown or unsupported default hybrid storage");
        }
        
        loadLdapBackendConfiguration(persistDir);
        loadCouchbaseBackendConfiguration(persistDir);
        persistenceBackendConfig.put(PersistenceBackendType.PERSISTENCE_BACKEND_HYBRID,props);
    }

    public final String getEncodeSalt() {

        return this.salt;
    }

    public PersistenceBackendType getPersistenceBackend() {

        return this.persistenceBackend;
    }

    public final Properties getBackendConfiguration(PersistenceBackendType backendType) {

        Properties ret = persistenceBackendConfig.get(backendType);
        if(ret != null)
            return (Properties) ret.clone();
        
        return null;
    }

    public final String getRadiusConfigDN() {

        return this.configDN;
    }

    public final String getRadiusClientConfigDN() {

        return this.clientsDN;
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

    public final SignatureAlgorithm getJwtAuthSignAlgo() {

        return this.jwtAuthSignAlgo;
    }

    public final PersistenceBackendType getDefaultHybridBackend() {
        return defaultHybridBackend;
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

}