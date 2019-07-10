package org.gluu.radius.service;

import java.io.IOException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.KnownService;
import org.gluu.radius.service.ServerConfigService;
import org.gluu.radius.ServiceLocator;
import org.gluu.oxauth.client.OpenIdConfigurationClient;
import org.gluu.oxauth.client.OpenIdConfigurationResponse;

public class OpenIdConfigurationService {

    private String authorizationEndpoint;
    private String registrationEndpoint;
    private String tokenEndpoint;
    private String jwksUri;

    public OpenIdConfigurationService(ServerConfigService serverConfigService) {

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

    private void loadOpenIdConfiguration(ServerConfigService serverConfigService) {

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
    
}