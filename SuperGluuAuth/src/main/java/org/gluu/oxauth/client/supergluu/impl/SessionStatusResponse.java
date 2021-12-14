package org.gluu.oxauth.client.supergluu.impl;

import java.io.Serializable;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import org.gluu.oxauth.client.supergluu.impl.model.SessionState;
import org.gluu.oxauth.client.supergluu.impl.model.SessionCustomState;
import static org.gluu.oxauth.client.supergluu.impl.model.SessionStatusResponseParam.*;


import org.gluu.oxauth.client.BaseResponse;


public class SessionStatusResponse extends BaseResponse implements Serializable {

    private static final long serialVersionUID = -1L;
    private static final Logger log = Logger.getLogger(SessionStatusResponse.class);

    private SessionState state;
    private SessionCustomState customState;
    private Integer authTime;

    public SessionStatusResponse(Response clientResponse) {
        
        super(clientResponse);
        
        if(StringUtils.isNotBlank(entity)) {
            
            try {
                JSONObject jsonObj = new JSONObject(entity);

                if(jsonObj.has(STATE)) {
                    state = SessionState.fromString(jsonObj.getString(STATE));
                }

                if(jsonObj.has(CUSTOM_STATE)) {
                   
                    customState = SessionCustomState.fromString(jsonObj.getString(CUSTOM_STATE));
                }

                if(jsonObj.has(AUTH_TIME) && !jsonObj.isNull(AUTH_TIME)) {
                   
                    authTime = jsonObj.getInt(AUTH_TIME);
                }

            }catch(JSONException e) {
                log.error(e.getMessage(),e);
            }catch(Exception e) {
                log.error(e.getMessage(),e);
            }
        }
    }

    public SessionState getState() {

        return this.state;
    }

    public SessionStatusResponse setState(SessionState state) {

        this.state = state;
        return this;
    }

    public SessionCustomState getCustomState() {

        return this.customState;
    }

    public SessionStatusResponse setCustomState(SessionCustomState customState) {

        this.customState = customState;
        return this;
    }

    public Integer getAuthTime() {

        return this.authTime;
    }

    public SessionStatusResponse setAuthTime(Integer authTime) {

        this.authTime = authTime;
        return this;
    }

    public boolean isAuthenticated() {

        if(checkIfNotAuthenticated())
            return false;
        else if(checkIfAuthenticated())
            return true;
        else
            return false; // let's play safe here
    }

    private final boolean checkIfNotAuthenticated() {
        return 
            (state == SessionState.UNKNOWN) ||
            (state == SessionState.UNAUTHENTICATED && 
            (customState == SessionCustomState.DECLINED || customState == SessionCustomState.EXPIRED));
    }

    private final boolean checkIfAuthenticated() {

        return 
            (state == SessionState.AUTHENTICATED) ||
            (state == SessionState.UNAUTHENTICATED && customState == SessionCustomState.APPROVED );
    }

    @Override
    public String toString() {

        return "SessionStatusResponse{" +
            "state='"+state+"'"+
            "custom_state='"+customState+"'"+
            "auth_time='"+authTime+"'"+
            "}";
    }
    
}