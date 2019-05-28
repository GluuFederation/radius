package org.gluu.radius.service;


import java.util.List;

import org.apache.log4j.Logger;

import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.EntryPersistenceException;
import org.gluu.persist.model.SearchScope;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.model.ServerConfiguration;
import org.gluu.search.filter.Filter;


public class ServerConfigService {

    private static final Logger log = Logger.getLogger(ServerConfigService.class);

    private String configEntryDn;
    private PersistenceEntryManager persistenceEntryManager;

    public ServerConfigService(PersistenceEntryManager persistenceEntryManager,String configEntryDn) {

        this.persistenceEntryManager = persistenceEntryManager;
        this.configEntryDn = configEntryDn;
    }

    public ServerConfiguration getServerConfiguration() {

        try {
            Filter searchFilter = createServerConfigurationSearchFilter();
            String [] attributes = null;
            List<ServerConfiguration> serverConfigs = persistenceEntryManager.findEntries(configEntryDn,ServerConfiguration.class,
                searchFilter,SearchScope.BASE,attributes,0,1,1);
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
           String [] attributes = null;
           List<ServerConfiguration.AuthScope> foundScopes = persistenceEntryManager.findEntries(scopeDn,
           ServerConfiguration.AuthScope.class,null,SearchScope.BASE,attributes,0,1,1);
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