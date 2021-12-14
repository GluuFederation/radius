package org.gluu.oxauth.client.supergluu.impl.http;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.jaxrs.engines.HttpContextProvider;


public class HttpContextFactory  implements HttpContextProvider{

   
    public HttpContextFactory() {

    }

    private static final HttpContext newHttpContext() {

        HttpClientContext httpClientContext = new HttpClientContext();
        configureCookieStorage(httpClientContext);
        configureRequestConfig(httpClientContext);
        return httpClientContext;
    }

    private static final void configureCookieStorage(HttpClientContext httpClientContext) {

        Registry<CookieSpecProvider> r = RegistryBuilder.<CookieSpecProvider>create()
            .register(CookieSpecs.DEFAULT,new DefaultCookieSpecProvider())
            .register(CookieSpecs.STANDARD,new RFC6265CookieSpecProvider())
            .build();
        
        httpClientContext.setCookieSpecRegistry(r);
        httpClientContext.setCookieStore(new BasicCookieStore());
    }

    private static final void configureRequestConfig(HttpClientContext httpClientContext) {

        RequestConfig reqConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.STANDARD)
            .build();
        httpClientContext.setRequestConfig(reqConfig);
    }

    @Override
    public HttpContext getContext() {

        return HttpContextFactory.newHttpContext();
    }
}