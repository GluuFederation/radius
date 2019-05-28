package org.gluu.oxauth.client.supergluu.impl;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.log4j.Logger;

import org.gluu.oxauth.client.supergluu.impl.model.SessionState;
import org.gluu.oxauth.client.supergluu.impl.model.SessionCustomState;
import org.gluu.oxauth.client.supergluu.impl.SessionStatusRequest;
import org.gluu.oxauth.client.supergluu.impl.SessionStatusResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.gluu.oxauth.client.BaseClient;


public class SessionStatusClient extends BaseClient<SessionStatusRequest,SessionStatusResponse> {
    
    private static final Logger log = Logger.getLogger(SessionStatusClient.class);
    private static final String mediaTypes = String.join(",",MediaType.TEXT_PLAIN,MediaType.APPLICATION_JSON);
    private static final String sessionIdCookieName = "session_id";

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
        
        if(executor instanceof ApacheHttpClient4Executor) {
            ApacheHttpClient4Executor apacheExecutor = (ApacheHttpClient4Executor) executor;
            if (apacheExecutor.getHttpContext() instanceof HttpClientContext) {
                HttpClientContext httpContext = (HttpClientContext) apacheExecutor.getHttpContext();
                httpContext.getCookieStore().clear();
                BasicClientCookie sessionIdCookie = new BasicClientCookie(sessionIdCookieName,sessionId);
                sessionIdCookie.setPath("/");
                sessionIdCookie.setSecure(true);
                sessionIdCookie.setDomain(requestDomain);
                httpContext.getCookieStore().addCookie(sessionIdCookie);
            }
        }
    }

    public SessionStatusResponse execGetStatus() {
        initClientRequest();
        SessionStatusResponse response = null;
        try {
            response = execGetStatusImpl();
        }catch(Exception e) {
            log.error(e.getMessage(),e);
        }
        return response;
    }

    private final SessionStatusResponse execGetStatusImpl() throws Exception {

        setRequest(new SessionStatusRequest());
        clientRequest.accept(mediaTypes);
        clientRequest.setHttpMethod(getHttpMethod());
        clientResponse = clientRequest.get(String.class);
        setResponse(new SessionStatusResponse(clientResponse));

        return getResponse();
    }
}