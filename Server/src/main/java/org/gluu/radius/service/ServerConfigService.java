package org.gluu.radius.service;

import java.util.ArrayList;
import java.util.List;


import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.EntryPersistenceException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.model.AuthScope;
import org.gluu.radius.model.ServerConfiguration;


public class ServerConfigService {

    
    private String configEntryDn;
    private PersistenceEntryManager persistenceEntryManager;

    public ServerConfigService(PersistenceEntryManager persistenceEntryManager,String configEntryDn) {

        this.persistenceEntryManager = persistenceEntryManager;
        this.configEntryDn = configEntryDn;
    }

    public ServerConfiguration getServerConfiguration() {

        try {
            return persistenceEntryManager.find(ServerConfiguration.class,configEntryDn);
        }catch(EntryPersistenceException e) {
            throw new ServiceException("Failed fetching server configuration",e);
        }
    }

    public List<AuthScope> getScopes(ServerConfiguration config) {

        List<AuthScope> ret = new ArrayList<AuthScope>();
        try {
            for(String scopeDn : config.getScopes()) {
                AuthScope scope = persistenceEntryManager.find(AuthScope.class,scopeDn);
                if(scope != null)
                    ret.add(scope);
            }
        }catch(EntryPersistenceException e) {
            throw new ServiceException("Failed fetch associated scopes for server configuration",e);
        }
        return ret;
    }


}