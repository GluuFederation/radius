package org.gluu.oxauth.client.supergluu.impl;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;


import java.io.IOException;
import java.net.CookiePolicy;
import java.net.URL;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.log4j.Logger;

import org.gluu.oxauth.client.supergluu.impl.model.SessionState;
import org.gluu.oxauth.client.supergluu.impl.model.SessionCustomState;
import org.gluu.oxauth.client.supergluu.impl.SessionStatusRequest;
import org.gluu.oxauth.client.supergluu.impl.SessionStatusResponse;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.gluu.oxauth.client.BaseClient;


public class SessionStatusClient extends BaseClient<SessionStatusRequest,SessionStatusResponse> {
    
    private static final Logger log = Logger.getLogger(SessionStatusClient.class);
    private static final String mediaTypes = String.join(",",MediaType.TEXT_PLAIN,MediaType.APPLICATION_JSON);
    private static final String sessionIdCookieName = "session_id";
    private static final int DEFAULT_COOKIE_DURATION = 60 * 60; // ONE HOUR

    private String requestDomain;

    public SessionStatusClient(String url) {
        super(url);
        this.requestDomain = null;
        try {
            URL urlToDecode = new URL(url);
            this.requestDomain = urlToDecode.getHost();
        }catch(Exception e) {
            log.debug("Could not get domain from url. " + e.getMessage(),e);
            this.requestDomain = url;
        }
    }

    @Override
    public String getHttpMethod() {

        return HttpMethod.GET;
    }

    public void setSessionId(String sessionId) {
        
        NewCookie sessionIdCookie = new NewCookie(sessionIdCookieName,sessionId,"/",requestDomain,"",DEFAULT_COOKIE_DURATION,true);
        getCookies().add(sessionIdCookie);
    }

    public SessionStatusResponse execGetStatus() {
        initClientRequest();
        Builder clientRequest = webTarget.request();
        applyCookies(clientRequest);

        SessionStatusResponse response = null;
        try {
            response = execGetStatusImpl(clientRequest);
        }catch(Exception e) {
            log.error(e.getMessage(),e);
        }
        return response;
    }

    private final SessionStatusResponse execGetStatusImpl(Builder clientRequest) throws Exception {

        setRequest(new SessionStatusRequest());
        clientRequest.accept(mediaTypes);
        clientResponse = clientRequest.buildGet().invoke();
        setResponse(new SessionStatusResponse(clientResponse));

        return getResponse();
    }
}