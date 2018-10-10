package org.gluu.radius.services.impl.openid.handlers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.HttpResponse;

import org.gluu.radius.services.impl.openid.OpenIdConfig;
import org.gluu.radius.services.impl.openid.OpenIdError;
import org.gluu.radius.util.HttpUtil;

public class GluuRadiusOpenIdConfigResponseHandler implements ResponseHandler<OpenIdConfig> {

	public GluuRadiusOpenIdConfigResponseHandler() {

	}


	@Override
	public OpenIdConfig handleResponse(HttpResponse response) throws IOException,ClientProtocolException {

		if(HttpUtil.isHttpOk(response))
			return parseGluuOpenIdConfig(response);
		else {
			OpenIdError error = new OpenIdError();
			error.setError("");
			error.setDescription(HttpUtil.getResponseContentAsString(response));
			OpenIdConfig ret = new OpenIdConfig();
			ret.setError(error);
			return ret;
		}
	}


	private OpenIdConfig parseGluuOpenIdConfig(HttpResponse response) throws IOException {

		return OpenIdConfig.fromJsonStream(HttpUtil.getResponseContent(response));
	}
}