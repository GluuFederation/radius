package org.gluu.radius.persist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.gluu.persist.couchbase.impl.CouchbaseEntryManagerFactory;
import org.gluu.persist.exception.operation.ConfigurationException;
import org.gluu.persist.hybrid.impl.HybridEntryManager;
import org.gluu.persist.hybrid.impl.HybridPersistenceOperationService;
import org.gluu.persist.ldap.impl.LdapEntryManagerFactory;
import org.gluu.persist.operation.PersistenceOperationService;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.radius.exception.GenericPersistenceException;


public class PersistenceEntryManagerFactory {

    public static final PersistenceEntryManager createLdapPersistenceEntryManager(Properties properties) {

        try {
            LdapEntryManagerFactory ldapEntryManagerFactory = new LdapEntryManagerFactory();
            Properties connProps = createConnectionProperties(properties,ldapEntryManagerFactory.getPersistenceType());
            PersistenceEntryManager ret = ldapEntryManagerFactory.createEntryManager(connProps);
            if(ret == null)
                throw new GenericPersistenceException("Could not create persistence entry manager");
            return ret;
        }catch(ConfigurationException e) {
            throw new GenericPersistenceException(e.getMessage(),e);
        }
    }

    public static final PersistenceEntryManager createCouchbasePersistenceEntryManager(Properties properties) {
    
        try {
            CouchbaseEntryManagerFactory couchbaseEntryManagerFactory = new CouchbaseEntryManagerFactory();
            couchbaseEntryManagerFactory.create();
            Properties connProps = createConnectionProperties(properties,couchbaseEntryManagerFactory.getPersistenceType());
            PersistenceEntryManager ret = couchbaseEntryManagerFactory.createEntryManager(connProps);
            if(ret == null)
                throw new GenericPersistenceException("Could not create persistence entry manager");
            
            return ret;
        }catch(ConfigurationException e) {
            throw new GenericPersistenceException(e.getMessage(),e);
        }
    }

    public static final PersistenceEntryManager createHybridPersistenceEntryManager(Properties hybridprops,
        Properties ldap_props, Properties couchbaseprops) {
        try {
            PersistenceEntryManager ldapEntryManager = createLdapPersistenceEntryManager(ldap_props);
            PersistenceEntryManager couchbaseEntryManager = createCouchbasePersistenceEntryManager(couchbaseprops);
            HashMap<String,PersistenceEntryManager> managers = new HashMap<String,PersistenceEntryManager>();
            managers.put(LdapEntryManagerFactory.PERSISTENCE_TYPE,ldapEntryManager);
            managers.put(CouchbaseEntryManagerFactory.PERSISTENCE_TYPE,couchbaseEntryManager);
            List<PersistenceOperationService> persistenceOperationServices = new ArrayList<PersistenceOperationService>();
            persistenceOperationServices.add(ldapEntryManager.getOperationService());
            persistenceOperationServices.add(couchbaseEntryManager.getOperationService());
            HybridPersistenceOperationService opservice = new HybridPersistenceOperationService(persistenceOperationServices);
            return new HybridEntryManager(hybridprops,managers,opservice);
        }catch(ConfigurationException e) {
            throw new GenericPersistenceException(e.getMessage(),e);
        }
    }

    private static final Properties createConnectionProperties(Properties properties,String connPrefix) {

        Properties connProps = new Properties();
        for(String propname : properties.stringPropertyNames()) {
            connProps.setProperty(connPrefix+"."+propname,properties.getProperty(propname));
        }
        return connProps;
    }
}