package org.gluu.radius.services.impl;

import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.impl.client.BasicResponseHandler;

import org.gluu.radius.auth.GluuRadiusUserAuthResult;
import org.gluu.radius.config.GluuRadiusOpenIdConfig;
import org.gluu.radius.services.GluuRadiusUserAuthService;
import org.gluu.radius.services.GluuRadiusServiceException;
import org.gluu.radius.services.impl.GluuRadiusOpenIdHttpClient;
import org.gluu.radius.services.impl.openid.OpenIdConfig;
import org.gluu.radius.services.impl.openid.OpenIdError;
import org.gluu.radius.services.impl.openid.TokenResponse;
import org.gluu.radius.util.CryptoUtil;


public class GluuRadiusUserAuthServiceImpl implements GluuRadiusUserAuthService {
	
	private static final int HTTP_OK = 200;
	private String decryptionkey;
	private String applianceurl;
	private String tokenendpoint;
	private String clientid;
	private PoolingHttpClientConnectionManager connmanager;

	public GluuRadiusUserAuthServiceImpl(String decryptionkey,GluuRadiusOpenIdConfig config) {

		this.decryptionkey = CryptoUtil.decryptPassword(config.getClientSecret(),decryptionkey);
		this.applianceurl = config.getApplianceUrl();
		this.clientid = config.getClientId();
		this.connmanager = createHttpConnectionManager(config);
		this.tokenendpoint = null;
	}


	@Override
	public GluuRadiusUserAuthResult authenticateUser(String username,String password) {
		
		initTokenEndpoint();
		GluuRadiusOpenIdHttpClient httpclient = getHttpClient();
		TokenResponse tokenresp = httpclient.getResourceOwnerPasswordCredentialsGrant(
			tokenendpoint,username,password,null);
		if(tokenresp.hasError())
			return GluuRadiusUserAuthResult.buildErrorAuthResult(tokenresp.getError().getError(),
				tokenresp.getError().getDescription());
		else
			return GluuRadiusUserAuthResult.buildSuccessAuthResult();
	}

	private synchronized void initTokenEndpoint() {

		if(this.tokenendpoint != null)
			return;

		GluuRadiusOpenIdHttpClient httpclient = getHttpClient();
		OpenIdConfig config = httpclient.getOpenIdConfig(applianceurl);
		this.tokenendpoint = config.getTokenEndpoint();
	}

	private GluuRadiusOpenIdHttpClient getHttpClient() {

		return new GluuRadiusOpenIdHttpClient(clientid,decryptionkey,connmanager);
	}


	private PoolingHttpClientConnectionManager createHttpConnectionManager(GluuRadiusOpenIdConfig config) {

		Registry<ConnectionSocketFactory> registry = createConnectionSocketFactory(config);
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(config.getCpSize());
		cm.setDefaultMaxPerRoute(config.getCpSize());
		return cm;
	}

	private Registry<ConnectionSocketFactory> createConnectionSocketFactory(GluuRadiusOpenIdConfig config) {

		try {
			SSLContext sslctx = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
			if(config.getSslVerifyEnabled())
				sslctx = SSLContexts.createSystemDefault();
			else
				sslctx = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
			SSLConnectionSocketFactory sslsf = null;
			if(config.getSslVerifyEnabled())
				sslsf = new SSLConnectionSocketFactory(sslctx,new DefaultHostnameVerifier(null));
			else
				sslsf = new SSLConnectionSocketFactory(sslctx,new NoopHostnameVerifier());

			PlainConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
			return RegistryBuilder.<ConnectionSocketFactory>create()
						.register("http",plainsf)
						.register("https",sslsf)
						.build();
		}catch(NoSuchAlgorithmException e) {
			throw new GluuRadiusServiceException("SSL Initialization failed while creating socket factory",e);
		}catch(KeyStoreException e) {
			throw new GluuRadiusServiceException("SSL Initialization failed while creating socket factory",e);
		}catch(KeyManagementException e) {
			throw new GluuRadiusServiceException("SSL Initialization failed while creating socket factory",e);
		}
	}


	
}