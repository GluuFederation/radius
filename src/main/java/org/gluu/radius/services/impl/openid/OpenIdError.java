package org.gluu.radius.services.impl.openid;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown=true)
public class OpenIdError {
	
	private String error;
	private String description;

	public OpenIdError() {

		this.error = null;
		this.description = null;
	}


	public String getError() {

		return this.error;
	}

	@JsonSetter("error")
	public OpenIdError setError(String error) {

		this.error  = error;
		return this;
	}


	public String getDescription() {

		return this.description;
	}

	@JsonSetter("error_description")
	public OpenIdError setDescription(String description) {

		this.description = description;
		return this;
	}


	public static OpenIdError fromJsonStream(InputStream stream) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(stream,OpenIdError.class);
	}
}