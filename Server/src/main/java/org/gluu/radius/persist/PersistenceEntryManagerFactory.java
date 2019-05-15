package org.gluu.radius.persist;

import java.util.Properties;

import org.gluu.persist.couchbase.impl.CouchbaseEntryManager;
import org.gluu.persist.couchbase.impl.CouchbaseEntryManagerFactory;
import org.gluu.persist.exception.operation.ConfigurationException;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.ldap.impl.LdapEntryManagerFactory;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.radius.exception.GenericPersistenceException;


public class PersistenceEntryManagerFactory {

    public static final PersistenceEntryManager createLdapPersistenceEntryManager(Properties properties) {

        try {
            LdapEntryManagerFactory ldapEntryManagerFactory = new LdapEntryManagerFactory();
            return ldapEntryManagerFactory.createEntryManager(properties);
        }catch(ConfigurationException e) {
            throw new GenericPersistenceException(e.getMessage(),e);
        }
    }

    public static final PersistenceEntryManager createCouchbasePersistenceEntryManager(Properties properties) {
    
        try {
            CouchbaseEntryManagerFactory couchbaseEntryManagerFactory = new CouchbaseEntryManagerFactory();
            couchbaseEntryManagerFactory.create();
            return couchbaseEntryManagerFactory.createEntryManager(properties);
        }catch(ConfigurationException e) {
            throw new GenericPersistenceException(e.getMessage(),e);
        }
    }

}