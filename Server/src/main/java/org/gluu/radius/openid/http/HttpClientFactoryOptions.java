package org.gluu.radius.openid.http;

public class HttpClientFactoryOptions {

    private static final Integer DEFAULT_MAX_TOTAL_CONNECTIONS = 100;
    private static final Integer DEFAULT_MAX_CONN_PER_ROUTE = 20;

    private boolean verifyHttpsHostname;
    private Integer maxTotalConnections;
    private Integer maxConnPerRoute;
    
    public HttpClientFactoryOptions() {

        this.verifyHttpsHostname = true;
        this.maxTotalConnections = DEFAULT_MAX_TOTAL_CONNECTIONS;
        this.maxConnPerRoute = DEFAULT_MAX_CONN_PER_ROUTE;
    }

    public boolean getVerifyHttpsHostname() {

        return this.verifyHttpsHostname;
    }

    public HttpClientFactoryOptions setVerifyHttpsHostname(boolean verifyHttpsHostname) {

        this.verifyHttpsHostname = verifyHttpsHostname;
        return this;
    }

    public Integer getMaxTotalConnections() {

        return this.maxTotalConnections;
    }

    public HttpClientFactoryOptions setMaxTotalConnection(Integer maxTotalConnections) {

        this.maxTotalConnections = maxTotalConnections;
        return this;
    }

    public Integer getMaxConnPerRoute() {

        return this.maxConnPerRoute;
    }

    public HttpClientFactoryOptions setMaxConnPerRoute(Integer maxConnPerRoute) {

        this.maxConnPerRoute = maxConnPerRoute;
        return this;
    }

    
}