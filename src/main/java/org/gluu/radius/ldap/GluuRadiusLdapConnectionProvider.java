package org.gluu.radius.ldap;

import com.unboundid.ldap.sdk.LDAPInterface;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;

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
	private LDAPConnectionPool unboundconnections;
	private LDAPConnectionPool boundconnections;

	public GluuRadiusLdapConnectionProvider(LdapConfiguration config) {

		this.config = config;
		this.unboundconnections = null;
		this.boundconnections = null;
		initUnboundConnPool();
		initBoundConnPool();
	}

	public GluuRadiusLdapConnection getUnboundLdapConnection() {

		return null;
	}

	public GluuRadiusLdapConnection getBoundLdapConnection() {

		return null;	
	}

	private void initUnboundConnPool() {

		// initialize the pool of unbounded connections
	}

	private void initBoundConnPool() {

		// initilialize the pool of bounded connections
	}

	private LDAPConnectionOptions getLdapConnectionOptions() {

		LDAPConnectionOptions opts = new LDAPConnectionOptions();
		
		return opts;
	}


}