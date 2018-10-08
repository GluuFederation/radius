package org.gluu.radius.config;


public class GluuRadiusBootstrapConfig {
	
	// connection pool configuration 
	public static class ConnPoolConfiguration {

		private Integer connpoolsize;

		public ConnPoolConfiguration() {

			this.connpoolsize = null;
		}

		public Integer getConnPoolSize() {

			return this.connpoolsize;
		}

		public ConnPoolConfiguration setConnPoolSize(Integer connpoolsize) {

			this.connpoolsize = connpoolsize;
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

	public GluuRadiusBootstrapConfig() {

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

	public GluuRadiusBootstrapConfig setHostname(String hostname) {

		this.hostname = hostname;
		return this;
	}

	public Integer getPort() {

		return this.port;
	}

	public GluuRadiusBootstrapConfig setPort(Integer port) {

		this.port = port;
		return this;
	}

	public String getBindDn() {

		return this.bindDn;
	}

	public GluuRadiusBootstrapConfig setBindDn(String bindDn) {

		this.bindDn = bindDn;
		return this;
	}

	public String getPassword() {

		return this.password;
	}

	public GluuRadiusBootstrapConfig setPassword(String password) {

		this.password = password;
		return this;
	}


	public String getTrustStoreFile() {

		return this.truststorefile;
	}

	public GluuRadiusBootstrapConfig setTrustStoreFile(String truststorefile) {

		this.truststorefile = truststorefile;
		return this;
	}

	public String getTrustStorePin() {

		return this.truststorepin;
	}

	public GluuRadiusBootstrapConfig setTrustStorePin(String truststorepin) {

		this.truststorepin = truststorepin;
		return this;
	}

	public String getTrustStoreFormat() {

		return this.truststoreformat;
	}

	public GluuRadiusBootstrapConfig setTrustStoreFormat(String truststoreformat) {

		this.truststoreformat = truststoreformat;
		return this;
	}

	public Boolean getSslEnabled() {

		return sslenabled;
	}

	public GluuRadiusBootstrapConfig setSslEnabled(Boolean sslenabled) {

		this.sslenabled = sslenabled;
		return this;
	}

	public Boolean getSslVerifyEnabled() {

		return this.sslverifyenabled;
	}

	public GluuRadiusBootstrapConfig setSslVerifyEnabled(Boolean sslverifyenabled) {

		this.sslverifyenabled = sslverifyenabled;
		return this;
	}

	public ConnPoolConfiguration getConnPoolConfig() {

		return this.cpconfig;
	}


	public String getRadiusConfigEntryDn() {

		return this.radiusconfigentrydn;
	}

	public GluuRadiusBootstrapConfig setRadiusConfigEntryDn(String radiusconfigdn) {

		this.radiusconfigentrydn = radiusconfigentrydn;
		return this;
	}


	public String getPeopleConfigEntryDn() {

		return this.peopleconfigentrydn;
	}

	public GluuRadiusBootstrapConfig setPeopleConfigEntryDn(String peopleconfigdn) {

		this.peopleconfigentrydn = peopleconfigentrydn;
		return this;
	}
}