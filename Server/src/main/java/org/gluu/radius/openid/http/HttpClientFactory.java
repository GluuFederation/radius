package org.gluu.radius.openid.http;


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


public class HttpClientFactory {

    private static PoolingHttpClientConnectionManager connMgr;

    private HttpClientFactory() {

    }

    public static final CloseableHttpClient newHttpClient() {

        return HttpClients.custom().setConnectionManager(connMgr).build();
    }



    public static final void init(HttpClientFactoryOptions options) {

        HostnameVerifier hostnameVerifier = null;

        if(options.getVerifyHttpsHostname())
            hostnameVerifier = new DefaultHostnameVerifier();
        else
            hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        
        SSLContext sslCtx = createSSLContext();

        SSLConnectionSocketFactory sslSf = new SSLConnectionSocketFactory(sslCtx,hostnameVerifier);
        PlainConnectionSocketFactory plainSf = PlainConnectionSocketFactory.INSTANCE;

        Registry<ConnectionSocketFactory> connRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http",plainSf)
            .register("https",sslSf)
            .build();
        
        connMgr = new PoolingHttpClientConnectionManager(connRegistry);
        connMgr.setMaxTotal(options.getMaxTotalConnections());
        connMgr.setDefaultMaxPerRoute(options.getMaxConnPerRoute());
    }

    private static final SSLContext createSSLContext() {

        return SSLContexts.createDefault();
    } 
}