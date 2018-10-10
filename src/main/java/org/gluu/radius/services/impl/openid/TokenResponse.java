package org.gluu.radius.services.impl.openid;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TokenResponse {
	
	private OpenIdError error;
	private String accesstoken;
	private String tokentype;
	private Integer expirytime;
	private String refreshtoken;
	private String scope;


	public TokenResponse() {

		this.error = null;
		this.accesstoken = null;
		this.tokentype = null;
		this.expirytime = null;
		this.refreshtoken = null;
		this.scope = null;
	}


	public boolean hasError() {

		return this.error!=null;
	}


	public OpenIdError getError() {

		return this.error;
	}

	public TokenResponse setError(OpenIdError error) {

		this.error = error;
		return this;
	}


	public String getAccessToken() {

		return this.accesstoken;
	}


	@JsonSetter("access_token")
	public TokenResponse setAccessToken(String accesstoken) {

		this.accesstoken = accesstoken;
		return this;
	}


	public String getTokenType() {

		return this.tokentype;
	}

	@JsonSetter("token_type")
	public TokenResponse setTokenType(String tokentype) {

		this.tokentype = tokentype;
		return this;
	}


	public Integer getExpiryTime() {

		return this.expirytime;
	}


	@JsonSetter("expires_in")
	public TokenResponse setExpiryTime(Integer expirytime) {

		this.expirytime = expirytime;
		return this;
	}


	public String getRefreshToken() {

		return this.refreshtoken;
	}


	@JsonSetter("refresh_token")
	public TokenResponse setRefreshToken(String refreshtoken) {

		this.refreshtoken = refreshtoken;
		return this;
	}


	public String getScope() {

		return this.scope;
	}


	@JsonSetter("scope")
	public TokenResponse setScope(String scope) {

		this.scope = scope;
		return this;
	}


	public static TokenResponse fromJsonStream(InputStream stream) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(stream,TokenResponse.class);
	}

}