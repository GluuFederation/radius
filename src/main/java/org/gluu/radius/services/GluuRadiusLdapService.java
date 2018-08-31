package org.gluu.radius.services;


public interface GluuRadiusLdapService {
	public boolean verifyUserCredentials(String username,String password);
	public String  getRadiusListenAddress();
	public Integer getRadiusAuthenticationPort();
	public Integer getRadiusAccountingPort();
	public String  getClientSharedSecret(String ipaddress);
}