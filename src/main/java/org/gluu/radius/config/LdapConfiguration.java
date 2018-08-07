package org.gluu.radius.config;


public class LdapConfiguration {
	
	private String hostname;
	private Integer port;
	private String  bindDn;
	private String password;
	private Boolean sslenabled;
	private String applianceInum;

	public LdapConfiguration() {

		this.hostname = null;
		this.port = null;
		this.bindDn = null;
		this.password = null;
		this.sslenabled = true;
		this.applianceInum = null;
	}

	public String getHostname() {

		return this.hostname;
	}

	public LdapConfiguration setHostname(String hostname) {

		this.hostname = hostname;
		return this;
	}

	public Integer getPort() {

		return this.port;
	}

	public LdapConfiguration setPort(Integer port) {

		this.port = port;
		return this;
	}

	public String getBindDn() {

		return this.bindDn;
	}

	public LdapConfiguration setBindDn(String bindDn) {

		this.bindDn = bindDn;
		return this;
	}

	public String getPassword() {

		return this.password;
	}

	public LdapConfiguration setPassword(String password) {

		this.password = password;
		return this;
	}


	public LdapConfiguration setSslEnabled(Boolean sslenabled) {

		this.sslenabled = sslenabled;
		return this;
	}


	public String getApplianceInum() {

		return this.applianceInum;
	}


	public LdapConfiguration setApplianceInum(String applianceInum) {

		this.applianceInum = applianceInum;
		return this;
	}
}