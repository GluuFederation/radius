package org.gluu.radius.ldap;

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPInterface;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

import com.unboundid.util.ssl.HostNameSSLSocketVerifier;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllSSLSocketVerifier;
import com.unboundid.util.ssl.TrustStoreTrustManager;

import java.security.GeneralSecurityException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.gluu.radius.config.GluuRadiusBootstrapConfig;



public class GluuRadiusLdapConnectionProvider {

	private static final Integer DEFAULT_CONN_POOL_SIZE = 1;

	public  class GluuRadiusLdapConnection {

		private LDAPConnection conn;
		private LDAPConnectionPool connpool;

		public GluuRadiusLdapConnection(LDAPConnection conn, LDAPConnectionPool connpool) {

			this.conn = conn;
			this.connpool = connpool;
		}


		public LDAPConnection getConnection() {

			return conn;
		}

		public void release() {

			connpool.releaseConnection(conn);
		}

	}

	private GluuRadiusBootstrapConfig config;
	private LDAPConnectionPool connpool;

	public GluuRadiusLdapConnectionProvider(GluuRadiusBootstrapConfig config) {

		this.config = config;
		this.connpool = createConnectionPool();
		
	}


	public GluuRadiusLdapConnection getConnection() {

		try {
			LDAPConnection conn = connpool.getConnection();
			return new GluuRadiusLdapConnection(conn,connpool);
		}catch(LDAPException e) {
			throw new GluuRadiusLdapException("Could not get ldap connection",e);
		}
	}


	private LDAPConnectionPool  createConnectionPool() {

		try {

			Integer cpsize = config.getConnPoolConfig().getConnPoolSize();
		
			LDAPConnection conn = null;
			if(config.getSslEnabled() == true) {
				conn = getSecuredConnection();
			}else {
				conn = getUnsecuredConnection();
			}

			validateBindResult(conn.bind(config.getBindDn(),config.getPassword()));
			return new LDAPConnectionPool(conn,cpsize);

		}catch(LDAPException e) {
			throw new GluuRadiusLdapException("Could not create connection pool",e);
		}
	}

	private LDAPConnectionOptions getConnectionOptions() {

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

	private LDAPConnection getUnsecuredConnection() {

		try {

			LDAPConnectionOptions connopts = getConnectionOptions();
			LDAPConnection conn = new LDAPConnection(connopts,config.getHostname(),config.getPort());
			return conn;
		}catch(LDAPException e) {
			throw new GluuRadiusLdapException("Could not create unsecured ldap connection",e);
		}
	}

	private LDAPConnection getSecuredConnection() {

		try {
			LDAPConnectionOptions connopts = getConnectionOptions();
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


	private void validateBindResult(BindResult result) {

		if(result.getResultCode() != ResultCode.SUCCESS)
			throw new GluuRadiusLdapException("Ldap bind operation failed. "+result.getDiagnosticMessage());
	}

}