package org.gluu.radius.services;


public enum GluuRadiusKnownService {
	
	UserAuthService("UserAuth"),
	LdapService("Ldap"),
	BootstrapService("Boostrap");

	private final String id;

	private GluuRadiusKnownService(String id) {

		this.id = id;
	}


	public String getId() {

		return this.id;
	}
}