package org.gluu.radius.ldap;

import com.unboundid.ldap.sdk.LDAPInterface;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;

import com.unboundid.util.ssl.HostNameSSLSocketVerifier;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllSSLSocketVerifier;
import com.unboundid.util.ssl.TrustStoreTrustManager;

import java.security.GeneralSecurityException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.gluu.radius.config.LdapConfiguration;



public class GluuRadiusLdapConnectionProvider {

	private static final Integer DEFAULT_CONN_POOL_SIZE = 1;
	enum GluuRadiusConnectionType {
		UnboundConnection,
		BoundConnection
	}

	public  class GluuRadiusLdapConnection {

		private GluuRadiusConnectionType type;
		private LDAPInterface conn;

		public GluuRadiusLdapConnection(GluuRadiusConnectionType type, LDAPInterface conn) {

			this.type = type;
			this.conn = conn;
		}
	}

	private LdapConfiguration config;
	private LDAPConnectionPool unboundcp;
	private LDAPConnectionPool boundcp;

	public GluuRadiusLdapConnectionProvider(LdapConfiguration config) {

		this.config = config;
		this.unboundcp = createConnectionPool();
		this.boundcp = createConnectionPool();
		
	}

	public GluuRadiusLdapConnection getUnboundLdapConnection() {

		return null;
	}

	public GluuRadiusLdapConnection getBoundLdapConnection() {

		return null;	
	}

	

	private LDAPConnectionPool  createConnectionPool() {

		try {

			Integer connpoolsize = DEFAULT_CONN_POOL_SIZE;
			if(config.getConnPoolSize() != null)
				connpoolsize = config.getConnPoolSize();
			LDAPConnection conn = null;
			if(config.getSslEnabled() == true) {
				conn = getSecuredLDAPConnection();
			}else {
				conn = getUnsecuredLDAPConnection();
			}
			return new LDAPConnectionPool(conn,connpoolsize);

		}catch(LDAPException e) {
			throw new GluuRadiusLdapException("Could not create connection pool",e);
		}
	}

	private LDAPConnectionOptions getLdapConnectionOptions() {

		LDAPConnectionOptions opts = new LDAPConnectionOptions();

		opts.setBindWithDNRequiresPassword(true);
		opts.setFollowReferrals(false);

		if(config.getSslEnabled() && config.getSslVerifyEnabled()) {
			opts.setSSLSocketVerifier(new HostNameSSLSocketVerifier(true));
		}else if(config.getSslEnabled() && !config.getSslVerifyEnabled()) {
			opts.setSSLSocketVerifier(TrustAllSSLSocketVerifier.getInstance());
		}

		return opts;
	}

	private LDAPConnection getUnsecuredLDAPConnection() {

		try {

			LDAPConnectionOptions connopts = getLdapConnectionOptions();
			LDAPConnection conn = new LDAPConnection(connopts,config.getHostname(),config.getPort());
			return conn;
		}catch(LDAPException e) {
			throw new GluuRadiusLdapException("Could not create unsecured ldap connection",e);
		}
	}

	private LDAPConnection getSecuredLDAPConnection() {

		try {
			LDAPConnectionOptions connopts = getLdapConnectionOptions();
			LDAPConnection conn = new LDAPConnection(getSSLSocketFactory(),connopts,config.getHostname(),
				config.getPort());
			return conn;
		}catch(LDAPException e) {
			throw new GluuRadiusLdapException("Could not create secured ldap connection",e);
		}
	}

	private SocketFactory getSSLSocketFactory() {

		try {

			if(config.getTrustStoreFile()==null)
				return SSLSocketFactory.getDefault();

			String truststorefile = config.getTrustStoreFile();
			char [] truststorepin = null;
			if(config.getTrustStorePin() != null)
				truststorepin = config.getTrustStorePin().toCharArray();

			String truststoreformat = config.getTrustStoreFormat();

			TrustStoreTrustManager tmanager = new TrustStoreTrustManager(
				config.getTrustStoreFile(),
				truststorepin,truststoreformat,true);
			SSLUtil sslutil = new SSLUtil(tmanager);
			return sslutil.createSSLSocketFactory();

		}catch(GeneralSecurityException e) {
			throw new GluuRadiusLdapException("Could not initialize ssl");
		}
	}

}