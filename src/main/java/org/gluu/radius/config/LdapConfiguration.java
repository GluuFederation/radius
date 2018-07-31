package org.gluu.radius.config;


public class LdapConfiguration {
	
	private String hostname;
	private String username;
	private String password;
	private Boolean sslenabled;

	public LdapConfiguration() {

		this.hostname = "127.0.0.1";
		this.sslenabled = true;
	}

	public String getHostname() {

		return this.hostname;
	}

	public LdapConfiguration setHostname(String hostname) {

		this.hostname = hostname;
		return this;
	}

	public String getUsername() {

		return this.username;
	}

	public LdapConfiguration setUsername(String username) {

		this.username = username;
		return this;
	}

	public LdapConfiguration setPassword(String password) {

		this.password = password;
		return this;
	}


	public LdapConfiguration setSslEnabled(Boolean sslenabled) {

		this.sslenabled = sslenabled;
		return this;
	}
}