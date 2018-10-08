package org.gluu.radius.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.protocol.HttpClientContext;

import org.gluu.radius.services.GluuRadiusServiceException;

import org.gluu.radius.services.impl.openid.OpenIdConfig;
import org.gluu.radius.services.impl.openid.OpenIdError;
import org.gluu.radius.services.impl.openid.TokenResponse;
import org.gluu.radius.services.impl.openid.handlers.GluuRadiusOpenIdConfigResponseHandler;
import org.gluu.radius.services.impl.openid.handlers.GluuRadiusTokenResponseHandler;

public class GluuRadiusOpenIdHttpClient {
	
	private PoolingHttpClientConnectionManager connmgr;
	private HttpClientContext context;
	private static final String MEDIA_TYPE_PLAIN = "text/plain";
	private static final String MEDIA_TYPE_JSON  = "application/json";
	private static final String mediatypes = String.join(",",MEDIA_TYPE_PLAIN,MEDIA_TYPE_JSON);
	private static final String PASSWORD_GRANT_TYPE = "password";
	private static final String GRANT_TYPE_PARAM_NAME = "grant_type";
	private static final String USERNAME_PARAM_NAME = "username";
	private static final String PASSWORD_PARAM_NAME = "password";
	private static final String SCOPE_PARAM_NAME = "scope";

	public GluuRadiusOpenIdHttpClient(String clientid,String clientsecret,PoolingHttpClientConnectionManager connmgr) {

		this.connmgr = connmgr;
		context = HttpClientContext.create();
		context.setCredentialsProvider(buildCredentialsProvider(clientid,clientsecret));
	}

	private <T>  T executeHttpGet(String url,ResponseHandler<T> handler) throws IOException,ClientProtocolException {

		CloseableHttpClient httpclient = getPooledHttpClient();
		HttpUriRequest request = RequestBuilder.get(url).setHeader(HttpHeaders.ACCEPT,mediatypes).build();
		return httpclient.execute(request,handler,context);
	}

	private <T> T executeHttpPost(String url,List<NameValuePair> postparams,ResponseHandler<T> handler) 
		throws IOException,ClientProtocolException {

		CloseableHttpClient httpclient = getPooledHttpClient();
		HttpUriRequest request = RequestBuilder.post(url)
				.setHeader(HttpHeaders.ACCEPT,mediatypes)
				.setEntity(new UrlEncodedFormEntity(postparams))
				.build();
		return httpclient.execute(request,handler,context);
	}

	public OpenIdConfig getOpenIdConfig(String applianceurl) {

		try {
			String url = applianceurl+"/.well-known/openid-configuration";
			OpenIdConfig config = executeHttpGet(url,new GluuRadiusOpenIdConfigResponseHandler());
			if(config.hasError())
				throw new GluuRadiusServiceException("Error while getting openid configuration.");
			return config;
		}catch(IOException e) {
			throw new GluuRadiusServiceException("I/O error while getting openid configuration",e);
		}
	}

	public TokenResponse getResourceOwnerPasswordCredentialsGrant(String url,String username,String password,String scope) {

		try {
			List<NameValuePair> postparams = new ArrayList<NameValuePair>();
			postparams.add(new BasicNameValuePair(GRANT_TYPE_PARAM_NAME,PASSWORD_GRANT_TYPE));
			postparams.add(new BasicNameValuePair(USERNAME_PARAM_NAME,username));
			postparams.add(new BasicNameValuePair(PASSWORD_PARAM_NAME,password));
			if(scope!=null)
				postparams.add(new BasicNameValuePair(SCOPE_PARAM_NAME,scope));
			return executeHttpPost(url,postparams,new GluuRadiusTokenResponseHandler());
		}catch(IOException e) {
			throw new GluuRadiusServiceException("I/O error while obtaining password credentials grant");
		}
	}

	private CredentialsProvider buildCredentialsProvider(String clientid,String clientsecret) {

		CredentialsProvider credsprovider = new BasicCredentialsProvider();
		credsprovider.setCredentials(AuthScope.ANY,
			new UsernamePasswordCredentials(clientid,clientsecret));
		return credsprovider;
	}

	private CloseableHttpClient getPooledHttpClient() {

		return HttpClients.custom().setConnectionManager(connmgr).build();
	}

}