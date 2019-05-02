package org.gluu.oxauth.client.supergluu.impl.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import org.gluu.oxauth.client.supergluu.impl.IHttpClientFactory;


public class PoolingConnectionHttpClientFactory implements IHttpClientFactory {

    private static final Integer DEFAULT_MAX_TOTAL_CONNECTIONS = 100;
    private static final Integer DEFAULT_MAX_CONN_PER_ROUTE = 20;
    private PoolingHttpClientConnectionManager httpConnManager;

    public PoolingConnectionHttpClientFactory() {
        init(DEFAULT_MAX_CONN_PER_ROUTE,DEFAULT_MAX_TOTAL_CONNECTIONS);
    }

    public PoolingConnectionHttpClientFactory(Integer maxConnectionsPerRoute,Integer maxTotalConnections) {
        init(maxConnectionsPerRoute,maxTotalConnections);
    }


    @Override
    public CloseableHttpClient newHttpClient() {

        return HttpClients.custom().setConnectionManager(httpConnManager).build();
    }

    private void init(Integer maxConnectionsPerRoute, Integer maxTotalConnections) {
        
        HostnameVerifier hostNameVerifier = new DefaultHostnameVerifier();

        SSLContext sslContext = SSLContexts.createDefault();
        SSLConnectionSocketFactory sslConnSocketFactory = new SSLConnectionSocketFactory(sslContext,hostNameVerifier);
        PlainConnectionSocketFactory plainSf = PlainConnectionSocketFactory.INSTANCE;

        Registry<ConnectionSocketFactory> connRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http",plainSf)
            .register("https",sslConnSocketFactory)
            .build();
        
        httpConnManager = new PoolingHttpClientConnectionManager(connRegistry);
        httpConnManager.setMaxTotal(maxTotalConnections);
        httpConnManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
    }
}