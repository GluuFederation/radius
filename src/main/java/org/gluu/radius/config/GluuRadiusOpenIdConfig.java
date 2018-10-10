package org.gluu.radius.config;


public class GluuRadiusOpenIdConfig {
	
	private static final Integer DEFAULT_HTTP_CONNPOOL_SIZE = 10;
	private String applianceurl;
	private String clientid;
	private String clientsecret;
	private Integer cpsize;
	private Boolean sslverifyenabled;


	public GluuRadiusOpenIdConfig() {

		this.applianceurl = null;
		this.clientid = null;
		this.clientsecret = null;
		this.cpsize = DEFAULT_HTTP_CONNPOOL_SIZE;
		this.sslverifyenabled = true;
	}


	public String getApplianceUrl() {

		return this.applianceurl;
	}

	public GluuRadiusOpenIdConfig setApplianceUrl(String applianceurl) {

		this.applianceurl = applianceurl;
		return this;
	}


	public String getClientId() {

		return this.clientid;
	}


	public GluuRadiusOpenIdConfig setClientId(String clientid) {

		this.clientid = clientid;
		return this;
	}

	public String getClientSecret() {

		return this.clientsecret;
	}


	public GluuRadiusOpenIdConfig setClientSecret(String clientsecret) {

		this.clientsecret = clientsecret;
		return this;
	}


	public Integer getCpSize() {

		return this.cpsize;
	}

	public GluuRadiusOpenIdConfig setCpSize(Integer cpsize) {

		this.cpsize = cpsize;
		return this;
	}


	public Boolean getSslVerifyEnabled() {

		return this.sslverifyenabled;
	}

	public GluuRadiusOpenIdConfig setSslVerifyEnabled(Boolean sslverifyenabled) {

		this.sslverifyenabled = sslverifyenabled;
		return this;
	}
}