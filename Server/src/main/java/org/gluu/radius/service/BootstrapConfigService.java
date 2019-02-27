package org.gluu.radius.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.util.EncDecUtil;

public class BootstrapConfigService  {

    private enum BootstrapConfigKeys {
        SaltFile("radius.config.saltfile"),
        LdapConfigFile("radius.config.oxldap");

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

    public BootstrapConfigService(String appConfigFile) { 

        Properties oxRadiusConfig = loadPropertiesFromFile(appConfigFile);
        String saltFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.SaltFile.getKeyName());

        if(saltFile == null)
            throw new ServiceException("Salt file not found in radius configuration file");

        this.salt = loadEncodeSalt(saltFile);

        String ldapConfigFile = oxRadiusConfig.getProperty(BootstrapConfigKeys.LdapConfigFile.getKeyName());

        this.oxLdapConfig = loadPropertiesFromFile(ldapConfigFile);
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