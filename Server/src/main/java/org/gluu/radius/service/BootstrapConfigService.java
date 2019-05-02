package org.gluu.radius.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.util.EncDecUtil;
import org.xdi.oxauth.model.crypto.signature.SignatureAlgorithm;

public class BootstrapConfigService  {

    private enum BootstrapConfigKeys {
        SaltFile("radius.config.saltfile"),
        LdapConfigFile("radius.config.oxldap"),
        JwtKeyStoreFile("radius.jwt.keyStoreFile"),
        JwtKeyStorePin("radius.jwt.keyStorePin"),
        JwtAuthKeyId("radius.jwt.auth.keyId"),
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
    private static final String trustStorePinKey = "ssl.trustStorePin";
    private static final String oxRadiusConfigEntryDnKey = "oxradius_ConfigurationEntryDN";
    private static final String oxRadiusClientConfigRdn = "ou=clients";

    private String salt;
    private Properties oxLdapConfig;
    private String jwtKeyStoreFile;
    private String jwtKeyStorePin;
    private String jwtAuthKeyId;
    private SignatureAlgorithm jwtAuthSignAlgo;

    public BootstrapConfigService(String appConfigFile) { 

        Properties oxRadiusConfig = loadPropertiesFromFile(appConfigFile);
        String saltFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.SaltFile.getKeyName());

        if(saltFile == null)
            throw new ServiceException("Salt file not found in radius configuration file");

        this.salt = loadEncodeSalt(saltFile);

        String ldapConfigFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.LdapConfigFile.getKeyName()); 

        this.oxLdapConfig = loadPropertiesFromFile(ldapConfigFile);

        this.jwtKeyStoreFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtKeyStoreFile.getKeyName());
        this.jwtKeyStorePin  = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtKeyStorePin.getKeyName());

        this.jwtAuthKeyId = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtAuthKeyId.getKeyName());
        String signalgo = oxRadiusConfig.getProperty(BootstrapConfigKeys.JwtAuthSignatureAlgorithm.getKeyName());
        this.jwtAuthSignAlgo = SignatureAlgorithm.fromString(signalgo);
    }

    public final String getEncodeSalt() {

        return this.salt;
    }

    public final Properties getLdapConnectionParams() {

        Properties props = (Properties) oxLdapConfig.clone();
        props.setProperty(bindPasswordKey,
            EncDecUtil.decode(props.getProperty(bindPasswordKey),salt));
        props.setProperty(trustStorePinKey,
            EncDecUtil.decode(props.getProperty(trustStorePinKey),salt));
        return props;
    }

    public final String getRadiusConfigDN() {

        return oxLdapConfig.getProperty(oxRadiusConfigEntryDnKey);
    }

    public final String getRadiusClientConfigDN() {

        return oxRadiusClientConfigRdn + "," + getRadiusConfigDN();
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