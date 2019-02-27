package org.gluu.radius.server.filter;

public class SuperGluuAccessRequestFilterConfig {
    
    //status check timeout is in milliseconds
    //so we put a default of 10 seconds 
    private static final Long DEFAULT_STATUS_CHECK_TIMEOUT = 10000L;
    private String openidUsername;
    private String openidPassword;
    private Long statusCheckTimeout;
    
    public SuperGluuAccessRequestFilterConfig() {

        this.openidUsername = null;
        this.openidPassword = null;
        this.statusCheckTimeout = DEFAULT_STATUS_CHECK_TIMEOUT;
    }


    public SuperGluuAccessRequestFilterConfig(String openidUsername,String openidPassword) {

        this.openidUsername = openidUsername;
        this.openidPassword = openidPassword;
    }

    public String getOpenidUsername() {

        return this.openidUsername;
    }


    public SuperGluuAccessRequestFilterConfig setOpenidUsername(String openidUsername) {

        this.openidUsername = openidUsername;
        return this;
    }

    public String getOpenidPassword() {

        return this.openidPassword;
    }

    public SuperGluuAccessRequestFilterConfig setOpenidPassword(String openidPassword) {

        this.openidPassword = openidPassword;
        return this;
    }

    public Long getStatusCheckTimeout() {

        return this.statusCheckTimeout;
    }

    public SuperGluuAccessRequestFilterConfig setStatusCheckTimeout(Long timeout) {

        this.statusCheckTimeout = timeout;
        return this;
    }


}