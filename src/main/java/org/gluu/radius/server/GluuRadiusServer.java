package org.gluu.radius.server;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.List;
import java.util.ArrayList;

import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusServer;

public class GluuRadiusServer {
	

	private class TinyRadiusServerWrapper extends RadiusServer {
		
		@Override
		public String getUserPassword(String username) {

			return null;
		}

		@Override
		public String getSharedSecret(InetSocketAddress client) {

			return null;
		}


		@Override
		public RadiusPacket accessRequestReceived(AccessRequest request,InetSocketAddress client) {

			//TODO implement this
			return null;
		}

		@Override
		public RadiusPacket accountingRequestReceived(AccountingRequest request,InetSocketAddress client) {

			//TODO implement this
			return null;
		}
	}

	
	private TinyRadiusServerWrapper tswrapper;
	private SharedSecretProvider ssprovider;
	private List<AccessRequestFilter> accessfilters;
	private List<AccountingRequestFilter> accountingfilters;

	public GluuRadiusServer() {

		tswrapper = new TinyRadiusServerWrapper();
		this.ssprovider = null;
		this.accessfilters = new ArrayList<AccessRequestFilter>();
		this.accountingfilters = new ArrayList<AccountingRequestFilter>();
	}


	public GluuRadiusServer setAccountingPort(Integer port) {

		try {
			tswrapper.setAcctPort(port);
		}catch(IllegalArgumentException ie) {
			throw new GluuRadiusException("Invalid accounting port value specified",ie);
		}
		return this;
	}

	public GluuRadiusServer setAuthenticationPort(Integer port) {

		try {
			tswrapper.setAuthPort(port);
		}catch(IllegalArgumentException ie) {
			throw new GluuRadiusException("Invalid authentication port value specified",ie);
		}
		return this;
	}


	public GluuRadiusServer setListenAddress(String hostname) {

		try {
			tswrapper.setListenAddress(InetAddress.getByName(hostname));
		}catch(UnknownHostException ue) {
			throw new GluuRadiusException("Hostname resolution failed for listen address",ue);
		}catch(SecurityException se) {
			throw new GluuRadiusException("Operation not allowed due to security restrictions",se);
		}
		return this;
	}


	public GluuRadiusServer setSharedSecretProvider(SharedSecretProvider ssprovider) {

		this.ssprovider = ssprovider;
		return this;
	}


	public GluuRadiusServer start() {
		// tinyradius does not provide any error reporting 
		// mechanism when this method fails , except through 
		// logging
		tswrapper.start(true,true);
		return this;
	}

	public GluuRadiusServer stop() {

		// tinyradius does not provide any error reporting 
		// mechanism when this method fails, except through 
		// logging 
		tswrapper. stop();
		return this;
	}

	
}