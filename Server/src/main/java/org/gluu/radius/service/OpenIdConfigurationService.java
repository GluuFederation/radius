package org.gluu.radius.service;

import java.io.IOException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.model.Client;
import org.gluu.radius.service.ServerConfigService;
import org.gluu.oxauth.client.OpenIdConfigurationClient;
import org.gluu.oxauth.client.OpenIdConfigurationResponse;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.EntryPersistenceException;

public class OpenIdConfigurationService {

    private String authorizationEndpoint;
    private String registrationEndpoint;
    private String tokenEndpoint;
    private String jwksUri;
    private final String openidClientsEntryDn;
    private PersistenceEntryManager persistenceEntryManager;

    public OpenIdConfigurationService(ServerConfigService serverConfigService,
        PersistenceEntryManager persistenceEntryManager, String openidClientsEntryDn) {

        this.persistenceEntryManager = persistenceEntryManager;
        this.openidClientsEntryDn = openidClientsEntryDn;
        loadOpenIdConfiguration(serverConfigService);
    }

    public String getAuthorizationEndpoint() {

        return this.authorizationEndpoint;
    }

    public String getRegistrationEndpoint() {

        return this.registrationEndpoint;
    }

    public String getTokenEndpoint() {

        return this.tokenEndpoint;
    }

    public String getJwksUri() {

        return this.jwksUri;
    }

    public Client loadOpenIdClient(String inum) {

        Client client = null;
        try {
            String dn = this.getDnForOpenIdClient(inum);
            client = persistenceEntryManager.find(Client.class,dn);
        }catch(EntryPersistenceException e) {
            throw new ServiceException("Failed to fetch openid client",e);
        }
        return client;
    }

    public void saveOpenIdClient(Client client) {

        try {
            persistenceEntryManager.merge(client);
        }catch(EntryPersistenceException e) {
            throw new ServiceException("Failed to save openid client",e);
        }
    }

    private final void loadOpenIdConfiguration(ServerConfigService serverConfigService) {

        try {
            String openIdBaseUrl = serverConfigService.getServerConfiguration().getOpenidBaseUrl();
            String openIdConfigurationUrl = openIdBaseUrl + "/.well-known/openid-configuration";
            OpenIdConfigurationClient client = new OpenIdConfigurationClient(openIdConfigurationUrl);
            OpenIdConfigurationResponse response = client.execOpenIdConfiguration();
            if (response != null && response.getStatus() == 200) {
                authorizationEndpoint = response.getAuthorizationEndpoint();
                registrationEndpoint  = response.getRegistrationEndpoint();
                tokenEndpoint = response.getTokenEndpoint();
                jwksUri = response.getJwksUri();
            }
            else
                throw new ServiceException("Could not load OpenIdConfiguration");
            
        }catch(IOException e) {
            throw new ServiceException("Could not load OpenIdConfiguration",e);
        }
    }
    

    private final String getDnForOpenIdClient(String inum) {

        return String.format("inum=%s,%s",inum,openidClientsEntryDn);
    }
}