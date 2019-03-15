package org.gluu.radius.openid;


import org.xdi.oxauth.client.TokenRequest;
import org.xdi.oxauth.model.common.AuthenticationMethod;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.oxauth.model.crypto.AbstractCryptoProvider;
import org.xdi.oxauth.model.crypto.signature.SignatureAlgorithm;

public class TokenRequestBuilder {

    private static final String ACR_VALUES_PARAM_NAME = "acr_values";
    private TokenRequest request;

    private TokenRequestBuilder(TokenRequest request) {

        this.request = request;
    }

    public static final TokenRequestBuilder resourceOwnerPasswordCredentialsGrant(
        String username,String password, String scope, 
        String clientId,String clientSecret ) {
        
        TokenRequest request = new TokenRequest(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS);
        request.setUsername(username);
        request.setPassword(password);
        request.setScope(scope);
        request.setAuthUsername(clientId);
        request.setAuthPassword(clientSecret);
        return new TokenRequestBuilder(request);
    }


    public TokenRequestBuilder acrValues(String acrValues) {

        if(acrValues!=null && request!=null)
            request.addCustomParameter(ACR_VALUES_PARAM_NAME,acrValues);
        return this;
    }

    public TokenRequestBuilder enablePrivateKeyJwtAuth(String keyId,AbstractCryptoProvider cryptoProvider, 
        SignatureAlgorithm algorithm,String audience)  {

        if(request!=null) {
            request.setAuthenticationMethod(AuthenticationMethod.PRIVATE_KEY_JWT);
            request.setKeyId(keyId);
            request.setCryptoProvider(cryptoProvider);
            request.setAlgorithm(algorithm);
            request.setAudience(audience);
        }
        return this;
    }

    public final TokenRequest build() {

        return this.request;
    }
}