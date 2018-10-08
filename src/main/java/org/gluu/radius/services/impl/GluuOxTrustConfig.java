package org.gluu.radius.services.impl;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GluuOxTrustConfig {
	
	private String applianceurl;

	public GluuOxTrustConfig() {

		this.applianceurl = null;
	}


	public String getApplianceUrl() {

		return this.applianceurl;
	}


	@JsonSetter("applianceUrl")
	public GluuOxTrustConfig setApplianceUrl(String applianceurl) {

		this.applianceurl = applianceurl;
		return this;
	}


	public static final GluuOxTrustConfig fromJson(String jsondata) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsondata,GluuOxTrustConfig.class);
	}
}