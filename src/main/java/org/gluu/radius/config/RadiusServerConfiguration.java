package org.gluu.radius.config;


public class RadiusServerConfiguration {
	
	private static final String  DEFAULT_LISTEN_ADDRESS = "127.0.0.1";
	private static final Integer DEFAULT_AUTHENTICATION_PORT = 1812;
	private static final Integer DEFAULT_ACCOUNTING_PORT = 1813;

	private String  listenaddress;
	private Integer authenticationport;
	private Integer accountingport;

	public RadiusServerConfiguration() {

		this.listenaddress = DEFAULT_LISTEN_ADDRESS;
		this.authenticationport = DEFAULT_AUTHENTICATION_PORT;
		this.accountingport = DEFAULT_ACCOUNTING_PORT;
	}

	public RadiusServerConfiguration setListenAddress(String listenaddress)  {

		this.listenaddress = listenaddress;
		return this;
	}

	public RadiusServerConfiguration setAuthenticationPort(Integer authenticationport) {

		this.authenticationport = authenticationport;
		return this;
	}


	public RadiusServerConfiguration setAccountingPort(Integer accountingport) {

		this.accountingport = accountingport;
		return this;
	}
}