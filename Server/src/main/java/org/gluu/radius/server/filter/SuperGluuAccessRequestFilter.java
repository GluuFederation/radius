package org.gluu.radius.server.filter;

import org.apache.log4j.Logger;
import org.gluu.radius.exception.GluuRadiusException;
import org.gluu.radius.server.AccessRequestContext;
import org.gluu.radius.server.AccessRequestFilter;
import org.gluu.radius.service.CryptoService;
import org.json.JSONObject;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.oxauth.client.supergluu.SuperGluuAuthClient;
import org.gluu.oxauth.client.supergluu.SuperGluuAuthClientConfig;
import org.gluu.oxauth.client.supergluu.SuperGluuAuthScheme;
import org.gluu.oxauth.client.supergluu.SuperGluuAuthStatus;
import org.gluu.oxauth.client.supergluu.impl.ICryptoProviderFactory;
import org.gluu.oxauth.client.supergluu.impl.IHttpClientFactory;
import org.gluu.oxauth.client.supergluu.impl.http.PoolingConnectionHttpClientFactory;

public class SuperGluuAccessRequestFilter implements AccessRequestFilter {

    private static final Logger log = Logger.getLogger(SuperGluuAccessRequestFilter.class);
    private static final long statusCheckInterval = 50; // in ms
    private IHttpClientFactory httpClientFactory;
    private SuperGluuAccessRequestFilterConfig filterConfig;

    public SuperGluuAccessRequestFilter(SuperGluuAccessRequestFilterConfig filterConfig) {

        this.httpClientFactory = new PoolingConnectionHttpClientFactory();
        this.filterConfig = filterConfig;
    }


    @Override
    public boolean processAccessRequest(AccessRequestContext context) {

        boolean ret = false;
        CryptoService cryptoService = filterConfig.getCryptoService();
        try {
            cryptoService.beginReadOpts();
            if(filterConfig.isOneStepAuth())
                ret = performOneStepAuth(context);
            else if(filterConfig.isTwoStepAuth())
                ret = performTwoStepAuth(context);
            else {
                log.debug("Authentication scheme is neither one-step nor two-step");
            }
        }finally {
            cryptoService.endReadOpts();
        }
        return ret;
    }

    private final boolean performOneStepAuth(AccessRequestContext context) {

        try {
            SuperGluuAuthClient client = createAuthClient();
            client.setAuthScheme(SuperGluuAuthScheme.ONE_STEP);
            String ipaddress = context.getClientIpAddress();
            String username  = context.getUsername();
            String password  = context.getPassword();
            log.debug(String.format("Performing one-step authentication for user {%s}",username));
            Boolean initiateresult = client.initiateAuthentication(username, password,ipaddress);
            if(initiateresult == null || (initiateresult != null && initiateresult == false)) {
                log.debug(String.format("Authentication failed for user {%s}",username));
                return false;
            }
            log.debug(String.format("Authentication success for user {%s}",username));
            return true;
        }catch(GluuRadiusException e) {
            String username = context.getUsername();
            log.debug(String.format("Authentication failed for user {%s}",username),e);
        }catch(Exception e) {
            String username = context.getUsername();
            log.debug(String.format("Authentication failed for user {%s}",username),e);
        }
        return false;
    }

    private final boolean performTwoStepAuth(AccessRequestContext context) {

        try {
            SuperGluuAuthClient client = createAuthClient();
            client.setAuthScheme(SuperGluuAuthScheme.TWO_STEP);
            String ipaddress = context.getClientIpAddress();
            String username  = context.getUsername();
            String password  = context.getPassword();

            log.debug(String.format("Performing two-step authentication for user {%s}",username));
            long authstart = System.currentTimeMillis();
            Boolean initiateresult = client.initiateAuthentication(username, password,ipaddress);
            if(initiateresult == null || (initiateresult != null && initiateresult == false)) {
                log.debug(String.format("Authentication failed for user {%s}.",username));
                return false;
            }

            log.debug(String.format("User {%s} step one auth success. Checking step-two auth result",username));
            SuperGluuAuthStatus authstatus = SuperGluuAuthStatus.UNAUTHENTICATED;
            while((System.currentTimeMillis() - authstart) < filterConfig.getAuthenticationTimeout()) {
                authstatus = client.checkAuthenticationStatus();
                if(authstatus == SuperGluuAuthStatus.AUTHENTICATED)
                    break;
                try {
                    Thread.sleep(statusCheckInterval);
                }catch(InterruptedException e) {

                }
            }
            
            if(authstatus == SuperGluuAuthStatus.UNAUTHENTICATED) {
                log.debug(String.format("Authentication timeout for user {%s}",username));
                return false;
            }

            log.debug(String.format("Performing additional two-step verification for user {%s}",username));
            Boolean verifyauth = client.verifyAuthentication(username, password);
            if(verifyauth == null || (verifyauth != null && verifyauth == false)) {
                log.debug(String.format("Two-step additional verification failed for user {%s}",username));
                return false;
            }
            log.debug(String.format("Authentication success for user {%s}",username));
            return true;
        }catch(GluuRadiusException e) {
            String username = context.getUsername();
            log.debug(String.format("Authentication failed for user {%s}",username));
        }catch(Exception e) {
            String username = context.getUsername();
            log.debug(String.format("Authentication failed for user {%s}",username));
        }
        return false;
    }

    private final SuperGluuAuthClient createAuthClient() {

        SuperGluuAuthClientConfig config = createAuthClientConfig();
        ICryptoProviderFactory factory = filterConfig.getCryptoProviderFactory();
        JSONObject keyset = filterConfig.getServerKeyset();
        return new SuperGluuAuthClient(config,httpClientFactory,factory,keyset);
    }

    private final SuperGluuAuthClientConfig createAuthClientConfig() {

        String keyId = filterConfig.getJwtAuthKeyId();
        SignatureAlgorithm algorithm = filterConfig.getJwtAuthSignAlgo();
        String audience = filterConfig.getTokenEndpointUrl();
        SuperGluuAuthClientConfig config = new SuperGluuAuthClientConfig(keyId,algorithm,audience);
        config.setClientId(filterConfig.getOpenidUsername());
        config.setTokenEndpointUrl(filterConfig.getTokenEndpointUrl());
        config.setSessionStatusUrl(filterConfig.getSessionStatusUrl());
        config.setAcrValue(filterConfig.getAcrValue());
        for(String scope : filterConfig.getScopes())
            config.addScope(scope);
        
        return config;
    }

}

