package org.gluu.radius.services.impl;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.List;



public class GluuRadiusClientConfig {


	public static class GluuRadiusClient {

		private String name;
		private String ipaddress;
		private String secret;

		public GluuRadiusClient() {

			this.name = null;
			this.ipaddress = null;
			this.secret = null;
		}


		public String getName() {

			return this.name;
		}

		@JsonSetter("name")
		public GluuRadiusClient setName(String name) {

			this.name = name;
			return this;
		}

		public String getIpAddress() {

			return this.ipaddress;
		}

		public boolean isIpAddress(String ipaddress) {

			return ipaddress!=null && this.ipaddress.equalsIgnoreCase(ipaddress);
		}

		@JsonSetter("ipaddress")
		public GluuRadiusClient setIpAddress(String ipaddress) {

			this.ipaddress = ipaddress;
			return this;
		}

		public String getSecret() {

			return this.secret;
		}

		@JsonSetter("secret")
		public GluuRadiusClient setSecret(String secret) {

			this.secret = secret;
			return this;
		}
	}

	private List<GluuRadiusClient> clients;


	public GluuRadiusClientConfig() {

		this.clients = null;
	}


	public List<GluuRadiusClient> getClients() {

		return this.clients;
	}

	@JsonSetter("clients")
	public GluuRadiusClientConfig setClients(List<GluuRadiusClient> clients) {

		this.clients = clients;
		return this;
	}

}