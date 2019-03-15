package org.gluu.radius.openid.http;

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

public class HttpContextFactory {

    private HttpContextFactory() {

    }

    public static final HttpContext newHttpContext() {

        HttpClientContext ctx = new HttpClientContext();
        configureCookieStorage(ctx);
        configureRequestConfig(ctx);
        return ctx;
    }

    private static final void configureCookieStorage(HttpClientContext ctx) {

        Registry<CookieSpecProvider> r = RegistryBuilder.<CookieSpecProvider>create()
            .register(CookieSpecs.DEFAULT,new DefaultCookieSpecProvider())
            .register(CookieSpecs.STANDARD,new RFC6265CookieSpecProvider())
            .build();
        
        ctx.setCookieSpecRegistry(r);
        ctx.setCookieStore(new BasicCookieStore());
    }

    private static final void configureRequestConfig(HttpClientContext ctx) {

        RequestConfig reqConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.STANDARD)
            .build();
        ctx.setRequestConfig(reqConfig);
    }

}