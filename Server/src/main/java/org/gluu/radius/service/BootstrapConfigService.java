package org.gluu.radius.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.util.EncDecUtil;
import org.gluu.radius.persist.PersistenceBackendType;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;

public class BootstrapConfigService  {

    private enum BootstrapConfigKeys {
        SaltFile("radius.config.saltfile"),
        PersistenceConfigFile("radius.persist.config"),
        PersistenceBackend("radius.persist.backend"),
        JwtKeyStoreFile("radius.jwt.keyStoreFile"),
        JwtKeyStorePin("radius.jwt.keyStorePin"),
        JwtAuthKeyId("radius.jwt.auth.keyId"),
        ConfigDN("radius.config_DN"),
        ClientsDN("radius.clients_DN"),
        JwtAuthSignatureAlgorithm("radius.jwt.auth.signAlgorithm");

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
    private static final String trustStorePinKey = "ssl.trustStorePin";
    
    private String salt;
    private Properties persistenceConfig;
    private PersistenceBackendType persistenceBackend;
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
            throw new ServiceException("Salt file not found in radius configuration file");

        this.salt = loadEncodeSalt(saltFile);

        String backend = oxRadiusConfig.getProperty(BootstrapConfigKeys.PersistenceBackend.getKeyName());
        if (backend.equalsIgnoreCase("couchbase")) {
            this.persistenceBackend = PersistenceBackendType.PERSISTENCE_BACKEND_LDAP;
        }else if(backend.equalsIgnoreCase("ldap")) {
            this.persistenceBackend = PersistenceBackendType.PERSISTENCE_BACKEND_LDAP;
        }else
            throw new ServiceException("Unknown persistence backend specified");

        String persistenceConfigFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.PersistenceConfigFile.getKeyName()); 

        this.persistenceConfig = loadPropertiesFromFile(persistenceConfigFile);

        this.jwtKeyStoreFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtKeyStoreFile.getKeyName());
        this.jwtKeyStorePin  = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtKeyStorePin.getKeyName());

        this.jwtAuthKeyId = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtAuthKeyId.getKeyName());
        String signalgo = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtAuthSignatureAlgorithm.getKeyName());
        this.jwtAuthSignAlgo = SignatureAlgorithm.fromString(signalgo);

        this.configDN = oxRadiusConfig.getProperty(BootstrapConfigKeys.ConfigDN.getKeyName());
        this.clientsDN = oxRadiusConfig.getProperty(BootstrapConfigKeys.ClientsDN.getKeyName());
    }

    public final String getEncodeSalt() {

        return this.salt;
    }

    public PersistenceBackendType getPersistenceBackend() {

        return this.persistenceBackend;
    }

    public final Properties getPersistenceConnectionParams() {

        Properties props =  (Properties) persistenceConfig.clone();

        if(persistenceBackend == PersistenceBackendType.PERSISTENCE_BACKEND_LDAP) {
            props.setProperty(bindPasswordKey,
                EncDecUtil.decode(props.getProperty(bindPasswordKey),salt));
            
        }else if (persistenceBackend == PersistenceBackendType.PERSISTENCE_BACKEND_COUCHBASE) {
            props.setProperty(authPasswordKey,
                EncDecUtil.decode(props.getProperty(authPasswordKey),salt));
        }
        if(props.getProperty(trustStorePinKey)!=null && !props.getProperty(trustStorePinKey).isEmpty()) {
            props.setProperty(trustStorePinKey,
                EncDecUtil.decode(props.getProperty(trustStorePinKey),salt));
        }
        return props;
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