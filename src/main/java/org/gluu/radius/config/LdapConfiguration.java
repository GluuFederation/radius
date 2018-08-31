package org.gluu.radius.config;


public class LdapConfiguration {
	
	// connection pool configuration 
	public static class ConnPoolConfiguration {

		private Integer unboundcpsize;
		private Integer boundcpsize;

		public ConnPoolConfiguration() {

			this.boundcpsize = null;
			this.unboundcpsize = null;
		}

		public Integer getUnboundCpSize() {

			return this.unboundcpsize;
		}

		public ConnPoolConfiguration setUnboundCpSize(Integer size) {

			this.unboundcpsize = size;
			return this;
		}

		public Integer getBoundCpSize() {

			return this.boundcpsize;
		}

		public ConnPoolConfiguration setBoundCpSize(Integer size) {

			this.boundcpsize = size;
			return this;
		}

	}

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
	private ConnPoolConfiguration cpconfig;

	// DNs so we don't have to harcode them 
	// within the app itself 
	private String radiusconfigentrydn;
	private String peopleconfigentrydn;

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
		this.cpconfig = new ConnPoolConfiguration();
		this.radiusconfigentrydn = null;
		this.peopleconfigentrydn = null;
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

	public ConnPoolConfiguration getConnPoolConfig() {

		return this.cpconfig;
	}


	public String getRadiusConfigEntryDn() {

		return this.radiusconfigentrydn;
	}

	public LdapConfiguration setRadiusConfigEntryDn(String radiusconfigdn) {

		this.radiusconfigentrydn = radiusconfigentrydn;
		return this;
	}


	public String getPeopleConfigEntryDn() {

		return this.peopleconfigentrydn;
	}

	public LdapConfiguration setPeopleConfigEntryDn(String peopleconfigdn) {

		this.peopleconfigentrydn = peopleconfigentrydn;
		return this;
	}
}