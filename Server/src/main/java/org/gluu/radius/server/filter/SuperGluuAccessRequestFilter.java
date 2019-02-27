package org.gluu.radius.server.filter;

import org.apache.log4j.Logger;
import org.gluu.oxauth.session.status.SessionStatusClient;
import org.gluu.oxauth.session.status.SessionStatusResponse;
import org.gluu.radius.exception.GluuRadiusException;
import org.gluu.radius.server.AccessRequestContext;
import org.gluu.radius.server.AccessRequestFilter;
import org.gluu.radius.openid.ClientFactory;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenResponse;

public class SuperGluuAccessRequestFilter implements AccessRequestFilter {

    private static final Logger log = Logger.getLogger(SuperGluuAccessRequestFilter.class);
    private static final String RO_PWD_GRANT_SCOPE = "openid";
    //wait interval between session status polls in milliseconds
    private static final long WAIT_INTERVAL = 200;

    private SuperGluuAccessRequestFilterConfig config;
    public SuperGluuAccessRequestFilter(SuperGluuAccessRequestFilterConfig config) {

        this.config  = config;
    }


    @Override
    public boolean processAccessRequest(AccessRequestContext arContext) {

        try {
            TokenClient tokenclient = ClientFactory.createTokenClient();
            TokenResponse tokenresponse = tokenclient.execResourceOwnerPasswordCredentialsGrant(
                arContext.getUsername(),
                arContext.getPassword(),
                RO_PWD_GRANT_SCOPE,
                config.getOpenidUsername(),
                config.getOpenidPassword()
            );

            if(tokenresponse==null ||(tokenresponse!=null && tokenresponse.getStatus()!=200)) {
                log.info("auth failed for user " + arContext.getUsername());
                log.debug("response_body= " + tokenresponse.getEntity());
                return false;
            }

            //check for session status
            SessionStatusClient sessionstatusclient = ClientFactory.createSessionStatusClient(tokenclient);
            long current_time = System.currentTimeMillis();
            do {
                SessionStatusResponse ssresponse = sessionstatusclient.execGetStatus();
                if(ssresponse.isAuthenticated())
                    return true;
                
                try {
                    Thread.sleep(WAIT_INTERVAL);
                }catch(InterruptedException e) {
                    break;
                }
            }while(System.currentTimeMillis() < current_time + config.getStatusCheckTimeout());

            return false;
        }catch(GluuRadiusException e) {
            log.info("auth failed for user "+arContext.getUsername(),e);
        }
        return false;
    }

}

