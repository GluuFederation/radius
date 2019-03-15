package org.gluu.radius.openid;

import java.io.IOException;

import org.gluu.oxauth.session.status.SessionStatusClient;
import org.gluu.radius.exception.ClientFactoryException;
import org.gluu.radius.exception.ServiceException;
import org.gluu.radius.KnownService;
import org.gluu.radius.openid.http.HttpClientFactory;
import org.gluu.radius.openid.http.HttpContextFactory;
import org.gluu.radius.service.ServerConfigService;
import org.gluu.radius.ServiceLocator;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.xdi.oxauth.client.AuthorizeClient;
import org.xdi.oxauth.client.BaseClient;
import org.xdi.oxauth.client.OpenIdConfigurationClient;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.TokenClient;

public class ClientFactory {
    
    private static final String OPENID_CONFIGURATION_URI = "/.well-known/openid-configuration";
    private static final String SESSION_STATUS_URI = "/oxauth/restv1/session_status";
    private static String openIdBaseUrl = null;
    private static OpenIdConfigurationResponse openidConfig = null;

    public static OpenIdConfigurationClient createOpenIdConfigurationClient() {

        try {
            String url = getOpenIdConfigurationUrl();
            OpenIdConfigurationClient client = new OpenIdConfigurationClient(url);
            client.setExecutor(createExecutor());
            return client;
        }catch(ServiceException e) {
            throw new ClientFactoryException(e.getMessage(),e);
        }
    }

    public static AuthorizeClient createAuthorizeClient() {

        AuthorizeClient client = new AuthorizeClient(getAuthorizationEndpoint());
        client.setExecutor(createExecutor());
        return client;
    }

    public static TokenClient createTokenClient() {

        TokenClient client = new TokenClient(getTokenEndpoint());
        client.setExecutor(createExecutor());
        return client;
    }

    public static SessionStatusClient createSessionStatusClient() {

        return createSessionStatusClient(null);
    }

    public static SessionStatusClient createSessionStatusClient(BaseClient<?,?> openidClient) {

        SessionStatusClient client = new SessionStatusClient(getSessionStatusUrl());
        if(openidClient == null)
            client.setExecutor(createExecutor());
        else
            client.setExecutor(createExecutor(openidClient));
        return client;
    }

    private static final String getOpenIdConfigurationUrl() {

        return getOpenIdBaseUrl()+OPENID_CONFIGURATION_URI;
    }

    private static final synchronized String getAuthorizationEndpoint() {

        if(openidConfig == null)
            loadOpenidConfig();
        return openidConfig.getAuthorizationEndpoint();
    }

    private static final synchronized String getTokenEndpoint() {
      
        if(openidConfig == null)
            loadOpenidConfig();
        return openidConfig.getTokenEndpoint();
    }

    private static final String getSessionStatusUrl() {

        return getOpenIdBaseUrl() + SESSION_STATUS_URI;
    }

    private static final ApacheHttpClient4Executor createExecutor() {

        return new ApacheHttpClient4Executor(
            HttpClientFactory.newHttpClient(),
            HttpContextFactory.newHttpContext()
        );
    }

    private static final ApacheHttpClient4Executor createExecutor(BaseClient<?,?> client) {

        ApacheHttpClient4Executor executor = (ApacheHttpClient4Executor) client.getExecutor();
        return new ApacheHttpClient4Executor (
            HttpClientFactory.newHttpClient(),
            executor.getHttpContext()
        );
    }

    private static final synchronized String getOpenIdBaseUrl() {

        if(openIdBaseUrl == null) {
            ServerConfigService scService = ServiceLocator.getService(KnownService.ServerConfig);
            openIdBaseUrl = scService.getServerConfiguration().getOpenidBaseUrl();
        }
        return openIdBaseUrl;
    }

    private static final void loadOpenidConfig() {

        try {
            OpenIdConfigurationClient client = createOpenIdConfigurationClient();
            openidConfig = client.execOpenIdConfiguration();

            if(openidConfig==null || (openidConfig!=null && openidConfig.getStatus()!=200)) {
                openidConfig = null;
                throw new ClientFactoryException("openid config load failed");
            }
        }catch(IOException e) {
            throw new ClientFactoryException("openid config load failed",e);
        }
    }
}