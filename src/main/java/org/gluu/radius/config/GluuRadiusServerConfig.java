package org.gluu.radius.config;


public class GluuRadiusServerConfig {
	
	private String listenaddress;
	private Integer authport;
	private Integer acctport;


	public GluuRadiusServerConfig() {

		this.listenaddress = null;
		this.authport = null;
		this.acctport = null;
	}


	public String getListenAddress()  {

		return this.listenaddress;
	}


	public GluuRadiusServerConfig setListenAddress(String listenaddress) {

		this.listenaddress = listenaddress;
		return this;
	}


	public Integer getAuthPort() {

		return this.authport;
	}

	public GluuRadiusServerConfig setAuthPort(Integer authport) {

		this.authport = authport;
		return this;
	}


	public Integer getAcctPort() {

		return this.acctport;
	}


	public GluuRadiusServerConfig setAcctPort(Integer acctport) {

		this.acctport = acctport;
		return this;
	}
}