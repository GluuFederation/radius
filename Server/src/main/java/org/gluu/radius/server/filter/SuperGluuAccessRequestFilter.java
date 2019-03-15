package org.gluu.radius.server.filter;

import org.apache.log4j.Logger;
import org.gluu.radius.exception.GluuRadiusException;
import org.gluu.radius.server.AccessRequestContext;
import org.gluu.radius.server.AccessRequestFilter;
import org.gluu.radius.openid.ClientFactory;
import org.gluu.radius.openid.TokenRequestBuilder;
import org.gluu.radius.openid.jwt.CryptoProviderFactory;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenRequest;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.model.crypto.AbstractCryptoProvider;

public class SuperGluuAccessRequestFilter implements AccessRequestFilter {

    private static final Logger log = Logger.getLogger(SuperGluuAccessRequestFilter.class);

    private SuperGluuAccessRequestFilterConfig config;
    public SuperGluuAccessRequestFilter(SuperGluuAccessRequestFilterConfig config) {

        this.config  = config;
    }


    @Override
    public boolean processAccessRequest(AccessRequestContext arContext) {

        try {
            if(!performInitialAuthentication(arContext))
                return false;
            return true;
        }catch(GluuRadiusException e) {
            log.info("auth failed for user "+arContext.getUsername(),e);
        }catch(Exception e) {
            log.info("auth failed for user "+arContext.getUsername(),e);
        }
        return false;
    }

    private boolean performInitialAuthentication(AccessRequestContext arContext) throws Exception {

        TokenClient client = ClientFactory.createTokenClient();
        TokenRequest request = resourceOwnerPasswordCredentialsGrantRequest(
            client,config.getInitialAuthScopes(),config.getInitialAuthAcrValues(), arContext);
        client.setRequest(request);
        TokenResponse response = client.exec();
        if(response == null || (response!=null && response.getStatus() != 200)) {
            String responsedata = null;
            if(response!=null)
                responsedata = response.getEntity();
            log.info("auth failed for user "+arContext.getUsername()+". Response data: "+responsedata);
            return false;
        }
        log.info("Response data : "+response.getEntity());
        return true;
    }

    private TokenRequest resourceOwnerPasswordCredentialsGrantRequest(
        TokenClient client,String scopes, String acrValues,AccessRequestContext arContext) throws Exception {

        AbstractCryptoProvider cryptoProvider = CryptoProviderFactory.newCryptoProvider();
        return TokenRequestBuilder.resourceOwnerPasswordCredentialsGrant(
            arContext.getUsername(),arContext.getPassword(),scopes,
            config.getOpenidUsername(),config.getOpenidPassword())
            .acrValues(acrValues)
            .enablePrivateKeyJwtAuth(config.getJwtAuthKeyId(), cryptoProvider,config.getJwtAuthSignAlgo(),client.getUrl())
            .build();
    }

}

