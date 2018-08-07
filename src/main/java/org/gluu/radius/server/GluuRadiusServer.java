package org.gluu.radius.server;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.List;
import java.util.ArrayList;

import org.gluu.radius.server.AccessRequestContext;
import org.gluu.radius.server.AccountingRequestFilter;
import org.gluu.radius.server.impl.TinyRadiusServer;

public class GluuRadiusServer {
	
	private TinyRadiusServer serverimpl;
	private SharedSecretProvider ssprovider;
	private List<AccessRequestFilter> accessfilters;
	private List<AccountingRequestFilter> accountingfilters;

	public GluuRadiusServer() {

		serverimpl = new TinyRadiusServer(this);
		ssprovider = null;
		accessfilters = new ArrayList<AccessRequestFilter>();
		accountingfilters = new ArrayList<AccountingRequestFilter>();
	}


	public GluuRadiusServer setAccountingPort(Integer port) {

		try {
			serverimpl.setAcctPort(port);
		}catch(IllegalArgumentException ie) {
			throw new GluuRadiusServerException("Invalid accounting port value specified",ie);
		}
		return this;
	}

	public GluuRadiusServer setAuthenticationPort(Integer port) {

		try {
			serverimpl.setAuthPort(port);
		}catch(IllegalArgumentException ie) {
			throw new GluuRadiusServerException("Invalid authentication port value specified",ie);
		}
		return this;
	}


	public GluuRadiusServer setListenAddress(String hostname) {

		try {
			serverimpl.setListenAddress(InetAddress.getByName(hostname));
		}catch(UnknownHostException ue) {
			throw new GluuRadiusServerException("Hostname resolution failed for listen address",ue);
		}catch(SecurityException se) {
			throw new GluuRadiusServerException("Operation not allowed due to security restrictions",se);
		}
		return this;
	}


	public GluuRadiusServer setSharedSecretProvider(SharedSecretProvider ssprovider) {

		this.ssprovider = ssprovider;
		return this;
	}

	public String getSharedSecret(String ipaddress) {

		return ssprovider.getSharedSecret(ipaddress);
	}


	public GluuRadiusServer start() {

		serverimpl.start(true,true);
		return this;
	}

	public GluuRadiusServer stop() {

		serverimpl.stop();
		return this;
	}


	public GluuRadiusServer addAccessRequestFilter(AccessRequestFilter filter) {

		accessfilters.add(filter);
		return this;
	}

	public boolean processAccessRequest(AccessRequestContext context) {

		boolean ret = false;
		for(AccessRequestFilter filter : accessfilters) {
			if(filter.processAccessRequest(context) == true) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public void processAccountingRequest(AccountingRequestContext context) {

		for(AccountingRequestFilter filter : accountingfilters) {
			filter.processAccountingRequest(context);
		}
	}

	public GluuRadiusServer addAccountingRequestFilter(AccountingRequestFilter filter) {

		accountingfilters.add(filter);	
		return this;
	}
}