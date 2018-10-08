package org.gluu.radius.services.impl.openid;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown=true)
public class OpenIdConfig  {
	
	private OpenIdError error;
	private String authorizationendpoint;
	private String tokenendpoint;

	public OpenIdConfig() {

		this.error = null;
		this.authorizationendpoint = null;
		this.tokenendpoint = null;
	}


	public boolean hasError() {

		return this.error != null;
	}


	public OpenIdError getError() {

		return this.error;
	}


	public OpenIdConfig setError(OpenIdError error) {

		this.error = error;
		return this;
	}


	public String getAuthorizationEndpoint() {

		return this.authorizationendpoint;
	}

	@JsonSetter("authorization_endpoint")
	public OpenIdConfig setAuthorizationEndpoint(String authorizationendpoint) {

		this.authorizationendpoint = authorizationendpoint;
		return this;
	}


	public String getTokenEndpoint() {

		return this.tokenendpoint;
	}


	@JsonSetter("token_endpoint")
	public OpenIdConfig setTokenEndpoint(String tokenendpoint) {

		this.tokenendpoint = tokenendpoint;
		return this;
	}


	public static OpenIdConfig fromJsonStream(InputStream stream) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(stream,OpenIdConfig.class);
	}

} 