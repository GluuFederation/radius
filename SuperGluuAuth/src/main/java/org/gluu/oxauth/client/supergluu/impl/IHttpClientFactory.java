package org.gluu.oxauth.client.supergluu.impl;

import org.apache.http.impl.client.CloseableHttpClient;

public interface IHttpClientFactory {
    public CloseableHttpClient newHttpClient();
}