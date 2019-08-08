package org.gluu.oxauth.client.supergluu;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.gluu.oxauth.model.crypto.AbstractCryptoProvider;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.oxauth.model.common.AuthenticationMethod;

public class SuperGluuAuthClientConfig {

    private AuthenticationMethod authenticationMethod;
    private String clientId;
    private String clientSecret;
    private String tokenEndpointUrl;
    private String sessionStatusUrl;
    private String acrValue;
    private List<String> scopes;
    // JWT private key auth configuration
    private String keyId;
    private SignatureAlgorithm algorithm;
    private String audience;

    public SuperGluuAuthClientConfig() {
        this.authenticationMethod = AuthenticationMethod.CLIENT_SECRET_BASIC;
        this.clientId = null;
        this.clientSecret = null;
        this.tokenEndpointUrl = null;
        this.sessionStatusUrl = null;
        this.keyId = null;
        this.algorithm = null;
        this.audience = null;
        this.acrValue = null;
        this.scopes = new ArrayList<String>();
    }

    public SuperGluuAuthClientConfig(String clientId,String clientSecret) {
        this.authenticationMethod = AuthenticationMethod.CLIENT_SECRET_BASIC;
        this.clientId = clientId; 
        this.clientSecret = clientSecret;
        this.tokenEndpointUrl = null;
        this.sessionStatusUrl = null;
        this.keyId = null;
        this.algorithm = null;
        this.audience = null;
        this.acrValue = null;
        this.scopes =  new ArrayList<String>();
    }

    public SuperGluuAuthClientConfig(String keyId, SignatureAlgorithm algorithm, String audience) {
        
        this.authenticationMethod = AuthenticationMethod.PRIVATE_KEY_JWT;
        this.clientId = null;
        this.tokenEndpointUrl = null;
        this.sessionStatusUrl = null;
        this.keyId = keyId;
        this.algorithm = algorithm;
        this.audience = audience;
        this.acrValue = null;
        this.scopes = new ArrayList<String>();
    }

    public AuthenticationMethod getAuthenticationMethod() {

        return this.authenticationMethod;
    }

    public SuperGluuAuthClientConfig setAuthenticationMethod(AuthenticationMethod authenticationMethod) {

        this.authenticationMethod = authenticationMethod;
        return this;
    }

    public String getClientId() {

        return this.clientId;
    }

    public SuperGluuAuthClientConfig setClientId(String clientId) {

        this.clientId = clientId;
        return this;
    }


    public String getClientSecret() {

        return this.clientSecret;
    }

    public SuperGluuAuthClientConfig setClientSecret(String clientSecret) {

        this.clientSecret = clientSecret;
        return this;
    }

    public String getTokenEndpointUrl() {

        return this.tokenEndpointUrl;
    }

    public SuperGluuAuthClientConfig setTokenEndpointUrl(String tokenEndpointUrl) {

        this.tokenEndpointUrl = tokenEndpointUrl;
        return this;
    }

    public String getSessionStatusUrl() {

        return this.sessionStatusUrl;
    }

    public SuperGluuAuthClientConfig setSessionStatusUrl(String sessionStatusUrl) {

        this.sessionStatusUrl = sessionStatusUrl;
        return this;
    }

    public String getKeyId() {

        return this.keyId;
    }

    public SuperGluuAuthClientConfig setKeyId(String keyId) {

        this.keyId = keyId;
        return this;
    }

    public SignatureAlgorithm getAlgorithm() {

        return this.algorithm;
    }

    public SuperGluuAuthClientConfig setAlgorithm(SignatureAlgorithm algorithm) {

        this.algorithm = algorithm;
        return this;
    }

    public String getAudience() {

        return this.audience;
    }

    public SuperGluuAuthClientConfig setAudience(String audience) {

        this.audience = audience;
        return this;
    }

    public String getAcrValue() {

        return this.acrValue;
    }

    public boolean hasAcrValue() {

        return this.acrValue != null;
    }

    public SuperGluuAuthClientConfig setAcrValue(String acrValue) {

        this.acrValue = acrValue;
        return this;
    }

    public List<String> getScopes() {

        return this.scopes;
    }

    public boolean hasScopes() {

        return !this.scopes.isEmpty();
    }

    public String getScopesAsString() {

        String ret = "";
        for (String scope : this.scopes) {
            ret += " " + scope;
        }
        return ret.trim();
    }

    public SuperGluuAuthClientConfig addScope(String scope) {

        this.scopes.add(scope);
        return this;
    }


}