package org.gluu.radius;


public interface ConfigurationProvider {
	public Integer getAccountingPort();
	public Integer getAuthenticationPort();
	public String  getSharedSecret(String ipaddress);
	public String  getListenAddress();
	public Boolean authenticationListeningEnabled();
	public Boolean accountingListeningEnabled();
}