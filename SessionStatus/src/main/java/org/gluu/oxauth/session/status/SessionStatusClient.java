package org.gluu.oxauth.session.status;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.xdi.oxauth.client.BaseClient;
import org.gluu.oxauth.session.status.model.SessionState;
import org.gluu.oxauth.session.status.model.SessionCustomState;

public class SessionStatusClient extends BaseClient<SessionStatusRequest,SessionStatusResponse> {

    private static final Logger log = Logger.getLogger(SessionStatusClient.class);

    private static final String mediaTypes = String.join(",",MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON);

    public SessionStatusClient(String url) {
        super(url);
    }

    @Override
    public String getHttpMethod() {

        return HttpMethod.GET;
    }

    public SessionStatusResponse execGetStatus() {
        initClientRequest();
        SessionStatusResponse response = null;
        try {
            response = execGetStatusImpl();
        }catch(Exception e) {
            log.error(e.getMessage(),e);
        }finally {
            closeConnection();
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
