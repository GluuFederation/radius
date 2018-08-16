package org.gluu.radius.ldap;

import org.gluu.radius.GluuRadiusException;

public class GluuRadiusLdapException extends GluuRadiusException {
	
	public GluuRadiusLdapException(String msg) {
		
		super(msg);
	}

	public GluuRadiusLdapException(String msg, Throwable cause) {

		super(msg,cause);
	}
}