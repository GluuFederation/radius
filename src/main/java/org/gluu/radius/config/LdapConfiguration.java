package org.gluu.radius.config;


public class LdapConfiguration {
	
	private String hostname;
	private Integer port;
	private String  bindDn;
	private String password;
	private String truststorefile;
	private String truststorepin;
	private String truststoreformat;
	private Boolean sslenabled;
	private Boolean sslverifyenabled;

	// connection pool configuration 
	private Integer connpoolsize;

	public LdapConfiguration() {

		this.hostname = null;
		this.port = null;
		this.bindDn = null;
		this.password = null;
		this.truststorefile = null;
		this.truststorepin = null;
		this.truststoreformat = null;
		this.sslenabled = true;
		this.sslverifyenabled = true;
		this.connpoolsize = null;
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


	public String getTrustStoreFile() {

		return this.truststorefile;
	}

	public LdapConfiguration setTrustStoreFile(String truststorefile) {

		this.truststorefile = truststorefile;
		return this;
	}

	public String getTrustStorePin() {

		return this.truststorepin;
	}

	public LdapConfiguration setTrustStorePin(String truststorepin) {

		this.truststorepin = truststorepin;
		return this;
	}

	public String getTrustStoreFormat() {

		return this.truststoreformat;
	}

	public LdapConfiguration setTrustStoreFormat(String truststoreformat) {

		this.truststoreformat = truststoreformat;
		return this;
	}

	public Boolean getSslEnabled() {

		return sslenabled;
	}

	public LdapConfiguration setSslEnabled(Boolean sslenabled) {

		this.sslenabled = sslenabled;
		return this;
	}

	public Boolean getSslVerifyEnabled() {

		return this.sslverifyenabled;
	}

	public LdapConfiguration setSslVerifyEnabled(Boolean sslverifyenabled) {

		this.sslverifyenabled = sslverifyenabled;
		return this;
	}

	public Integer getConnPoolSize() {

		return this.connpoolsize;
	}

	public LdapConfiguration setConnPoolSize(Integer connpoolsize) {

		this.connpoolsize = connpoolsize;
		return this;
	}
}