package org.gluu.radius.service;

import com.unboundid.ldap.sdk.Filter;

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
            ServerConfiguration serverConfig = serverConfigs.get(0);
            if(serverConfig.getScopesDn() == null)
                return serverConfig;
            
            for(String scopeDn : serverConfig.getScopesDn()) {
                ServerConfiguration.AuthScope authScope = getScope(scopeDn);
                if(authScope != null)
                    serverConfig.addScope(authScope);
            }
            return serverConfig;
    
        }catch(EntryPersistenceException e) {
            throw new ServiceException("Failed fetching server configuration",e);
        }
    }

    private final ServerConfiguration.AuthScope getScope(String scopeDn) {

        try {
           List<ServerConfiguration.AuthScope> foundScopes = ldapEntryManager.findEntries(scopeDn,
           ServerConfiguration.AuthScope.class,null,SearchScope.BASE);
           if (foundScopes.isEmpty())
                return null;
            else
                return foundScopes.get(0);
        }catch(EntryPersistenceException e) {
            throw new ServiceException("Failed fetching server configuration",e);
        }
    }

    private final Filter createServerConfigurationSearchFilter() {

        return null;
    }

}