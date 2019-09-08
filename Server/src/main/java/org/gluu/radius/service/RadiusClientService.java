package org.gluu.radius.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.model.RadiusClient;
import org.gluu.persist.exception.EntryPersistenceException;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.search.filter.Filter;


public class RadiusClientService  {

    private static final Logger log = Logger.getLogger(RadiusClientService.class);
    
    private PersistenceEntryManager persistenceEntryManager;
    private String configEntryDn;

    public RadiusClientService(PersistenceEntryManager persistenceEntryManager,String configEntryDn) {

        this.persistenceEntryManager = persistenceEntryManager;
        this.configEntryDn = configEntryDn;
    }

    public RadiusClient getRadiusClient(String ipaddress) {

        try {
            Filter searchFilter = searchByIpAddressFilter(ipaddress);
            List<RadiusClient> clients = persistenceEntryManager.findEntries(configEntryDn,RadiusClient.class,searchFilter);
            if(clients.size()==0)
                return null;
            return clients.get(0);
        }catch(EntryPersistenceException e) {
            throw new ServiceException(String.format("Failed fetching client with ip %s",ipaddress),e);
        }
    }

    public List<RadiusClient> getRadiusClients() {

        try {
            Filter searchFilter = Filter.createPresenceFilter("oxRadiusClientIpAddress");
            return persistenceEntryManager.findEntries(configEntryDn,RadiusClient.class,searchFilter);
        }catch(EntryPersistenceException e) {
            throw new ServiceException("Failed fetching clients",e);
        }
    }

    private Filter searchByIpAddressFilter(String ipaddress) {

       Filter ipFilter  = Filter.createEqualityFilter("oxRadiusClientIpAddress",ipaddress);
       return ipFilter;
    }
}