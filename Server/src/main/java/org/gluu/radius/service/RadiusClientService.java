package org.gluu.radius.service;

import java.util.List;

import com.unboundid.ldap.sdk.Filter;
import org.apache.log4j.Logger;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.model.RadiusClient;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.xdi.ldap.model.SearchScope;


public class RadiusClientService  {

    private static final Logger log = Logger.getLogger(RadiusClientService.class);
    
    private LdapEntryManager ldapEntryManager;
    private String configEntryDn;

    public RadiusClientService(LdapEntryManager ldapEntryManager,String configEntryDn) {

        this.ldapEntryManager = ldapEntryManager;
        this.configEntryDn = configEntryDn;
    }

    public RadiusClient getRadiusClient(String ipaddress) {

        try {
            Filter searchFilter = createRadiusClientSearchFilter(ipaddress);
            List<RadiusClient> clients = ldapEntryManager.findEntries(configEntryDn,RadiusClient.class,searchFilter,SearchScope.SUB);
            if(clients.size()==0)
                return null;
            return clients.get(0);
        }catch(EntryPersistenceException e) {
            throw new ServiceException(String.format("Failed fetching client with ip %s",ipaddress),e);
        }
    }

    private Filter createRadiusClientSearchFilter(String ipaddress) {

        Filter ipFilter  = Filter.createEqualityFilter("oxRadiusClientIpAddress",ipaddress);
        return ipFilter;
    } 
}