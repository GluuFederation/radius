package org.gluu.oxauth.client.supergluu;


import org.apache.log4j.Logger;

import org.gluu.oxauth.client.supergluu.impl.http.HttpContextFactory;
import org.gluu.oxauth.client.supergluu.impl.ICryptoProviderFactory;
import org.gluu.oxauth.client.supergluu.impl.IHttpClientFactory;
import org.gluu.oxauth.client.supergluu.impl.SessionStatusClient;
import org.gluu.oxauth.client.supergluu.impl.SessionStatusResponse;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.json.JSONObject;
import org.gluu.oxauth.client.TokenClient;
import org.gluu.oxauth.client.TokenRequest;
import org.gluu.oxauth.client.TokenResponse;
import org.gluu.oxauth.model.common.AuthenticationMethod;
import org.gluu.oxauth.model.common.GrantType;
import org.gluu.oxauth.model.crypto.AbstractCryptoProvider;
import org.gluu.oxauth.model.exception.InvalidJwtException;
import org.gluu.oxauth.model.jwt.Jwt;



public class SuperGluuAuthClient {

    private class AuthenticationContext {

        private String sessionId;
    
        public AuthenticationContext() {
            this.sessionId = "";
        }

        public String getSessionId() {

            return this.sessionId;
        }

        public void setSessionId(String sessionId) {

            this.sessionId = sessionId;
        }
    }

    private static final String ACR_VALUES_PARAM_NAME = "acr_values";
    private static final String STEP_PARAM_NAME  = "__step";
    private static final String PASSWORD_PARAM_NAME   = "__password";
    private static final String SESSION_ID_PARAM_NAME = "__session_id";
    private static final String REMOTE_IP_PARAM_NAME  = "__remote_ip";
    private static final String SESSION_ID_CLAIM_NAME = "__session_id";
    private static final String AUTH_SCHEME_PARAM_NAME = "__auth_scheme";
    private static final String INITIATE_AUTH_STEP_NAME = "initiate_auth";
    private static final String RESEND_NOTIFICATION_STEP_NAME = "resend_notification";
    private static final String VERIFY_AUTH_STEP_NAME = "verify_auth";

    private static final Logger log = Logger.getLogger(SuperGluuAuthClient.class);

    private AuthenticationContext authContext;
    private SuperGluuAuthClientConfig config;
    private AbstractCryptoProvider cryptoProvider;
    private TokenClient tokenClient;
    private SessionStatusClient sessionStatusClient;
    private JSONObject serverKeyset;
    private SuperGluuAuthScheme authScheme;

    public SuperGluuAuthClient(SuperGluuAuthClientConfig config, IHttpClientFactory httpClientFactory) {
        authContext = new AuthenticationContext();
        this.config = config;
        cryptoProvider = null;
        tokenClient = new TokenClient(config.getTokenEndpointUrl());
        tokenClient.setExecutor(createExecutor(httpClientFactory));
        sessionStatusClient = new SessionStatusClient(config.getSessionStatusUrl());
        sessionStatusClient.setExecutor(createExecutor(httpClientFactory,true));
        authScheme = SuperGluuAuthScheme.TWO_STEP;
    }

    public SuperGluuAuthClient(SuperGluuAuthClientConfig config, 
        IHttpClientFactory httpClientFactory, ICryptoProviderFactory cryptoProviderFactory,JSONObject serverKeyset) {
        
        authContext = new AuthenticationContext();
        this.config = config;
        this.cryptoProvider = cryptoProviderFactory.newCryptoProvider();
        this.serverKeyset = serverKeyset;
        tokenClient = new TokenClient(config.getTokenEndpointUrl());
        tokenClient.setExecutor(createExecutor(httpClientFactory));
        sessionStatusClient = new SessionStatusClient(config.getSessionStatusUrl());
        sessionStatusClient.setExecutor(createExecutor(httpClientFactory,true));
        authScheme = SuperGluuAuthScheme.TWO_STEP;
    }

    public void setCryptoProvider(AbstractCryptoProvider cryptoProvider) {

        this.cryptoProvider = cryptoProvider;
    }

    public void setAuthScheme(SuperGluuAuthScheme authScheme) {

        this.authScheme = authScheme;
    } 
    
    public Boolean initiateAuthentication(String username,String password) {

        return initiateAuthentication(username,password,null);
    }

    public Boolean initiateAuthentication(String username,String password,String userip) {

        TokenRequest request = createInitiateAuthTokenRequest(username,password,userip);
        tokenClient.setRequest(request);
        TokenResponse response = tokenClient.exec();
        if (response == null || (response != null && response.getStatus() != 200)) {
            if (response !=  null)
                log.debug("SuperGluu initial auth failed. Response: "+ response.getEntity());
            else
                log.debug("SuperGluu initial auth failed. No response");
            
            return false;
        }
        
        if(authScheme == SuperGluuAuthScheme.ONE_STEP)
            return true;
        
        String idtoken = response.getIdToken();
        if (idtoken == null || (idtoken != null && idtoken.isEmpty())) {
            log.debug("SuperGluu initial auth failed. No id_token returned. " + response.getEntity());
            return false;
        }
        return extractSessionIdFromIdToken(idtoken);
    }

    public Boolean resendPushNotification(String username, String password) {

        return resendPushNotification(username,password,null);
    }

    public Boolean resendPushNotification(String username, String password, String userip) {

        TokenRequest request = createResendNotificationTokenRequest(username,password,userip);
        tokenClient.setRequest(request);
        TokenResponse response = tokenClient.exec();
        if(response == null || (response != null && response.getStatus() != 200)) {
            if(response != null)
                log.debug("SuperGluu resend push notification failed. Response: " + response.getEntity());
            else
                log.debug("SuperGluu resend push notification failed. No response");
            
            return false;
        }
        
        return verifyIdTokenSignature(response.getIdToken());
    }

    public Boolean verifyAuthentication(String username,String password) {

        TokenRequest request = createVerifyAuthTokenRequest(username,password);
        tokenClient.setRequest(request);
        TokenResponse response = tokenClient.exec();
        if(response == null || (response != null && response.getStatus() != 200)) {
            if(response != null)
                log.debug("SuperGluu auth verify failed. Response: " + response.getEntity());
            else
                log.debug("SuperGluu auth verify failed. No response");
            
            return false;
        }

        return verifyIdTokenSignature(response.getIdToken());
    }

    public SuperGluuAuthStatus checkAuthenticationStatus() {
        
        sessionStatusClient.setSessionId(authContext.getSessionId());
        SessionStatusResponse response = sessionStatusClient.execGetStatus();
        if(response == null || (response != null && response.getStatus() != 200)) {
            if(response != null)
                log.debug("SuperGluu auth status check failed. Response: " + response.getEntity());
            else
                log.debug("SuperGluu auth status check failed. No response");
            
            return SuperGluuAuthStatus.UNAUTHENTICATED;
        }

        return (response.isAuthenticated()?SuperGluuAuthStatus.AUTHENTICATED:SuperGluuAuthStatus.UNAUTHENTICATED);
    }

    private final ApacheHttpClient43Engine createExecutor(IHttpClientFactory httpClientFactory) {

        return createExecutor(httpClientFactory,false);
    }

    private final ApacheHttpClient43Engine createExecutor(IHttpClientFactory httpClientFactory,boolean createContext) {

        if(createContext)
            return new ApacheHttpClient43Engine(httpClientFactory.newHttpClient(),new HttpContextFactory());
        else
            return new ApacheHttpClient43Engine(httpClientFactory.newHttpClient());
    }

    private final TokenRequest createInitiateAuthTokenRequest(String username,String password,String userip) {

        TokenRequest request = new TokenRequest(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS);
        
        if(config.hasScopes())
            request.setScope(config.getScopesAsString());
        
        if (config.hasAcrValue())
            request.addCustomParameter(ACR_VALUES_PARAM_NAME,config.getAcrValue());
        
        if (userip != null)
            request.addCustomParameter(REMOTE_IP_PARAM_NAME,userip);
        
        request.addCustomParameter(STEP_PARAM_NAME,INITIATE_AUTH_STEP_NAME);
        request.addCustomParameter(AUTH_SCHEME_PARAM_NAME, authScheme.schemeName());

        configureTokenRequestAuthentication(request,username,password);
        return request;
    }

    private final TokenRequest createResendNotificationTokenRequest(String username, String password, String userip) {

        TokenRequest request = new TokenRequest(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS);

        if(config.hasScopes())
            request.setScope(config.getScopesAsString());
        
        if(config.hasAcrValue())
            request.addCustomParameter(ACR_VALUES_PARAM_NAME,config.getAcrValue());
        
        if(authContext.getSessionId()!=null)
            request.addCustomParameter(SESSION_ID_PARAM_NAME,authContext.getSessionId());

        if(userip != null)
            request.addCustomParameter(REMOTE_IP_PARAM_NAME,userip);
        
        request.addCustomParameter(STEP_PARAM_NAME,RESEND_NOTIFICATION_STEP_NAME);

        configureTokenRequestAuthentication(request,username,password);
        return request;
    }

    private final TokenRequest createVerifyAuthTokenRequest(String username,String password) {

        TokenRequest request = new TokenRequest(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS);

        if(config.hasScopes())
            request.setScope(config.getScopesAsString());
        
        if(config.hasAcrValue())
            request.addCustomParameter(ACR_VALUES_PARAM_NAME,config.getAcrValue());
        
        if(authContext.getSessionId()!=null)
            request.addCustomParameter(SESSION_ID_PARAM_NAME,authContext.getSessionId());
        
        request.addCustomParameter(STEP_PARAM_NAME,VERIFY_AUTH_STEP_NAME);

        configureTokenRequestAuthentication(request,username,password);
        return request;
    }

    private final void configureTokenRequestAuthentication(TokenRequest request, String username, String password) {

        request.setAuthenticationMethod(config.getAuthenticationMethod());
        request.setUsername(username);
        request.setPassword("");
        if(password != null)
            request.addCustomParameter(PASSWORD_PARAM_NAME,password);
        else
            request.addCustomParameter(PASSWORD_PARAM_NAME,"");

        if(AuthenticationMethod.CLIENT_SECRET_BASIC == config.getAuthenticationMethod()) {
            request.setAuthUsername(config.getClientId());
            request.setAuthPassword(config.getClientSecret());
        }

        if(AuthenticationMethod.PRIVATE_KEY_JWT == config.getAuthenticationMethod()) {
            request.setKeyId(config.getKeyId());
            request.setCryptoProvider(cryptoProvider);
            request.setAlgorithm(config.getAlgorithm());
            request.setAudience(config.getAudience());
            request.setAuthUsername(config.getClientId());
        }
    }

    private boolean verifyJwtSignature(Jwt jwt) {
        try {
            String keyId = jwt.getHeader().getKeyId();
            return cryptoProvider.verifySignature(
                jwt.getSigningInput(),jwt.getEncodedSignature(), keyId,
                serverKeyset,null,jwt.getHeader().getSignatureAlgorithm());
        }catch(Exception e) {
            log.debug("JWT token signature verification failed",e);
            return false;
        }
    }

    private boolean verifyIdTokenSignature(String idToken) {

        try {
            Jwt jwt = Jwt.parse(idToken);
            return verifyJwtSignature(jwt);
        }catch(InvalidJwtException e) {
            log.debug("Id token validation failed.",e);
            return false;
        }
    }

    private boolean extractSessionIdFromIdToken(String idToken) {

        try {
            Jwt jwt = Jwt.parse(idToken);
            if(!verifyJwtSignature(jwt)) {
                log.debug("Jwt signature verification failed during session_id extraction");
                return false;
            }

            if(!jwt.getClaims().hasClaim(SESSION_ID_CLAIM_NAME)) {
                log.debug("No session_id claim found in JWT token");
                return false;
            }

            authContext.setSessionId(jwt.getClaims().getClaimAsString(SESSION_ID_CLAIM_NAME));
            return true;
        }catch(InvalidJwtException e) {
            log.debug("IdToken parse failed during sesion_id extraction.",e);
            return false;
        }
    }

}