package org.gluu.radius.config;

import com.fasterxml.jackson.annotation.JsonSetter;

public class GluuRadiusClientConfig {

	private String name;
	private String ipaddress;
	private String secret;


	public GluuRadiusClientConfig() {

		this.name = null;
		this.ipaddress = null;
		this.secret = null;
	}

	public String getName() {

		return this.name;
	}

	@JsonSetter("name")
	public GluuRadiusClientConfig setName(String name) {

		this.name = name;
		return this;
	}


	public String getIpAddress() {

			return this.ipaddress;
	}

	@JsonSetter("ipaddress")
	public GluuRadiusClientConfig setIpAddress(String ipaddress) {

		this.ipaddress = ipaddress;
		return this;
	}


	public String getSecret() {

		return this.secret;
	}


	@JsonSetter("secret")
	public GluuRadiusClientConfig setSecret(String secret) {

		this.secret = secret;
		return this;
	}


	public boolean isIpAddress(String ipaddress) {

		return this.ipaddress!=null && this.ipaddress.equalsIgnoreCase(ipaddress);
	}
}