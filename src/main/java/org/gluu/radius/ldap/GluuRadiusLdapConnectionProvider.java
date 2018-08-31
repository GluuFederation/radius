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

import org.gluu.radius.config.LdapConfiguration;



public class GluuRadiusLdapConnectionProvider {

	private static final Integer DEFAULT_CONN_POOL_SIZE = 1;

	enum GluuRadiusConnectionType {
		UnboundConnection,
		BoundConnection
	}

	public  class GluuRadiusLdapConnection {

		private GluuRadiusConnectionType type;
		private LDAPConnection conn;
		private LDAPConnectionPool connpool;

		public GluuRadiusLdapConnection(GluuRadiusConnectionType type, LDAPConnection conn,
			LDAPConnectionPool connpool) {

			this.type = type;
			this.conn = conn;
			this.connpool = connpool;
		}


		public LDAPConnection getConnection() {

			return conn;
		}

		public void release() {

			connpool.releaseConnection(conn);
		}


		public void performBind(String bindDn,String password) {

			try {
				BindResult result = conn.bind(bindDn,password);
				GluuRadiusLdapConnectionProvider.this.validateBindResult(result);
			}catch(LDAPException e) {
				throw new GluuRadiusLdapException("LDAP Bind operation failed",e);
			}
		}
	}

	private LdapConfiguration config;
	private LDAPConnectionPool unboundcp;
	private LDAPConnectionPool boundcp;

	public GluuRadiusLdapConnectionProvider(LdapConfiguration config) {

		this.config = config;
		this.unboundcp = createConnectionPool(false);
		this.boundcp = createConnectionPool(true);
		
	}

	public GluuRadiusLdapConnection getUnboundLdapConnection() {

		try {
			LDAPConnection conn = unboundcp.getConnection();
			return new GluuRadiusLdapConnection(GluuRadiusConnectionType.UnboundConnection,
				conn,unboundcp);
		}catch(LDAPException e) {
			throw new GluuRadiusLdapException("Could not get unbound ldap connection",e);
		}
	}

	public GluuRadiusLdapConnection getBoundLdapConnection() {

		try {
			LDAPConnection conn = boundcp.getConnection();
			return new GluuRadiusLdapConnection(GluuRadiusConnectionType.BoundConnection,
				conn,boundcp);
		}catch(LDAPException e) {
			throw new GluuRadiusLdapException("Could not get bound ldap connection",e);
		}
	}

	

	private LDAPConnectionPool  createConnectionPool(boolean performbind) {

		try {

			Integer cpsize = DEFAULT_CONN_POOL_SIZE;
			
			if(performbind==true && config.getConnPoolConfig().getBoundCpSize() != null)
				cpsize = config.getConnPoolConfig().getBoundCpSize();

			if(performbind==false && config.getConnPoolConfig().getUnboundCpSize() != null)
				cpsize = config.getConnPoolConfig().getUnboundCpSize();

			LDAPConnection conn = null;
			if(config.getSslEnabled() == true) {
				conn = getSecuredLDAPConnection();
			}else {
				conn = getUnsecuredLDAPConnection();
			}

			if(performbind)
				validateBindResult(conn.bind(config.getBindDn(),config.getPassword()));

			return new LDAPConnectionPool(conn,cpsize);

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


	private void validateBindResult(BindResult result) {

		if(result.getResultCode() != ResultCode.SUCCESS)
			throw new GluuRadiusLdapException("Ldap bind operation failed. "+result.getDiagnosticMessage());
	}

}