package org.gluu.radius.server.filter;

import java.util.ArrayList;
import java.util.List;

import org.gluu.radius.model.AuthScope;
import org.gluu.radius.model.ServerConfiguration;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.service.OpenIdConfigurationService;
import org.gluu.radius.util.EncDecUtil;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;

public class SuperGluuAccessRequestFilterConfig {
    
    //status check timeout is in milliseconds
    private static final Long DEFAULT_AUTHENTICATION_TIMEOUT = 30000L;
    private static final String SESSION_STATUS_URI = "/oxauth/restv1/session_status";
    private final BootstrapConfigService bcService;
    private final OpenIdConfigurationService openIdConfigService;
    private ServerConfiguration serverConfig;
    private List<AuthScope> scopes;
    
    public SuperGluuAccessRequestFilterConfig(final BootstrapConfigService bcService, ServerConfiguration serverConfig, 
        List<AuthScope> scopes,OpenIdConfigurationService openIdConfigService) {

        this.bcService = bcService;
        this.serverConfig = serverConfig;
        this.openIdConfigService = openIdConfigService;
        this.scopes = scopes;
    }

    public String getOpenidUsername() {

        return serverConfig.getOpenidUsername();
    }

    public String getOpenidPassword() {

        return EncDecUtil.decode(serverConfig.getOpenidPassword(),bcService.getEncodeSalt());
    }


    public Long getAuthenticationTimeout() {

        if (serverConfig.getAuthenticationTimeout() == 0)
            return DEFAULT_AUTHENTICATION_TIMEOUT;
        else
            return serverConfig.getAuthenticationTimeout().longValue();
    }

    public String getAcrValue() {

        return serverConfig.getAcrValue();
    }

    public List<String> getScopes() {
        
       List<String> ret = new ArrayList<String>();
       for(AuthScope scope: scopes) {
           if(scope.getName() != null)
                ret.add(scope.getName());
           else if(scope.getId() != null)
                ret.add(scope.getId());
       }
       return ret;
    }

    public String getJwtKeyStoreFile() {

        return bcService.getJwtKeyStoreFile();
    }

    public String getJwtKeyStorePin() {

        return EncDecUtil.decode(bcService.getJwtKeyStorePin(),bcService.getEncodeSalt());
    }

    public String getJwtAuthKeyId() {

        return bcService.getJwtAuthKeyId();
    }

    public SignatureAlgorithm getJwtAuthSignAlgo() {

        return bcService.getJwtAuthSignAlgo();
    }

    public String getTokenEndpointUrl() {

        return this.openIdConfigService.getTokenEndpoint();
    }

    public String getAuthorizationEndpointUrl() {

        return this.openIdConfigService.getAuthorizationEndpoint();
    }

    public String getRegistrationEndpointUrl() {

        return this.openIdConfigService.getRegistrationEndpoint();
    }

    public String getOpenIdBaseUrl() {

        return this.serverConfig.getOpenidBaseUrl();
    }

    public String getSessionStatusUrl() {

        return this.serverConfig.getOpenidBaseUrl() + SESSION_STATUS_URI;
    }
}