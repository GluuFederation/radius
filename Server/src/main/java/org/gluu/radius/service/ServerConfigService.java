package org.gluu.radius.service;

import com.unboundid.ldap.sdk.Filter;

import java.io.IOException;
import java.util.List;

import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.model.ServerConfiguration;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;

import org.xdi.ldap.model.SearchScope;

import org.apache.log4j.Logger;

public class ServerConfigService {

    private static final Logger log = Logger.getLogger(ServerConfigService.class);

    private String configEntryDn;
    private LdapEntryManager ldapEntryManager;

    public ServerConfigService(LdapEntryManager ldapEntryManager,String configEntryDn) {

        this.ldapEntryManager = ldapEntryManager;
        this.configEntryDn = configEntryDn;
    }

    public ServerConfiguration getServerConfiguration() {

        try {
            Filter searchFilter = createServerConfigurationSearchFilter();
            List<ServerConfiguration> serverConfigs = ldapEntryManager.findEntries(configEntryDn,ServerConfiguration.class,
                searchFilter,SearchScope.BASE);
            if(serverConfigs.size() == 0)
                return null;
            return serverConfigs.get(0);
    
        }catch(EntryPersistenceException e) {
            throw new ServiceException("Failed fetching server configuration",e);
        }
    }

    private final Filter createServerConfigurationSearchFilter() {

        return null;
    }

}