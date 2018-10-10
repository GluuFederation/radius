package org.gluu.radius.services.impl.openid.handlers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.HttpResponse;


import org.gluu.radius.services.impl.openid.TokenResponse;
import org.gluu.radius.services.impl.openid.OpenIdError;
import org.gluu.radius.util.HttpUtil;

public class GluuRadiusTokenResponseHandler implements ResponseHandler<TokenResponse> {


	@Override
	public TokenResponse handleResponse(HttpResponse response) throws IOException,ClientProtocolException {

		if(HttpUtil.isHttpOk(response))
			return parseTokenResponse(response);
		else
			return parseOpenIdError(response);
	}


	private TokenResponse parseTokenResponse(HttpResponse response) throws IOException {

		return TokenResponse.fromJsonStream(HttpUtil.getResponseContent(response));
	}

	private TokenResponse parseOpenIdError(HttpResponse response) throws IOException {

		TokenResponse ret = new TokenResponse();
		OpenIdError error = OpenIdError.fromJsonStream(HttpUtil.getResponseContent(response));
		ret.setError(error);
		return ret;
	}
}