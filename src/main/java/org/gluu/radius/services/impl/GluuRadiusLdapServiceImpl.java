package org.gluu.radius.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchResultListener;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.LDAPSearchException;

import java.io.IOException;
import java.util.List;

import org.gluu.radius.GluuRadiusException;
import org.gluu.radius.ldap.GluuRadiusLdapException;
import org.gluu.radius.ldap.GluuRadiusLdapConnectionProvider;
import org.gluu.radius.ldap.GluuRadiusLdapConnectionProvider.GluuRadiusLdapConnection;
import org.gluu.radius.services.GluuRadiusLdapService;
import org.gluu.radius.services.GluuRadiusServiceException;
import org.gluu.radius.util.CryptoUtil;

import org.gluu.radius.services.impl.GluuRadiusClientConfig.GluuRadiusClient;



public class GluuRadiusLdapServiceImpl implements GluuRadiusLdapService {
	
	private static final String UID_ATTRIBUTE_NAME = "uid";
	private static final String OBJECTCLASS_ATTRIBUTE_NAME = "objectClass";
	private static final String GLUUPERSON_OBJECTCLASS = "gluuPerson";
	private static final String GLUUCUSTOMPERSON_OBJECTCLASS = "gluuCustomPerson";
	private static final String GLUU_BASE_DN = "o=gluu";
	private static final String APPLIANCES_BASE_DN = "ou=appliances,o=gluu";
	private static final String OXRADIUS_CONFIGURATION_OBJECTCLASS = "oxRadiusConfiguration";

	private static final String OXRADIUS_LISTEN_IP_ADDRESS_ATTRIBUTE_NAME = "oxRadiusListenIpAddress";
	private static final String OXRADIUS_ACCOUNTING_PORT_ATTRIBUTE_NAME = "oxRadiusAccountingPort";
	private static final String OXRADIUS_AUTHENTICATION_PORT_ATTRIBUTE_NAME = "oxRadiusAuthenticationPort";
	private static final String OXRADIUS_CLIENT_CONFIG_ATTRIBUTE_NAME = "oxRadiusClientConfig";

	private String decryptionkey;
	private GluuRadiusLdapConnectionProvider connprovider;


	public GluuRadiusLdapServiceImpl(String decryptionkey,GluuRadiusLdapConnectionProvider connprovider) {

		this.decryptionkey = decryptionkey;
		this.connprovider = connprovider;
	}

	@Override
	public boolean verifyUserCredentials(String username,String password) {

		GluuRadiusLdapConnection boundconn = null;
		GluuRadiusLdapConnection unboundconn = null;
		try {
			boundconn = connprovider.getBoundLdapConnection();
			String userDN = getUserDN(username,boundconn);
			unboundconn = connprovider.getUnboundLdapConnection();
			unboundconn.performBind(userDN,password);
			return true;
		}catch(GluuRadiusLdapException e) {
			throw new GluuRadiusServiceException("LDAP operation failed",e);
		}finally {
			if(boundconn!=null)
				boundconn.release();

			if(unboundconn!=null)
				unboundconn.release();
		}
	}

	@Override
	public String getRadiusListenAddress() {

		GluuRadiusLdapConnection conn = null; 
		try {
			conn = connprovider.getBoundLdapConnection();
			return oxRadiusConfigAttributeAsString(OXRADIUS_LISTEN_IP_ADDRESS_ATTRIBUTE_NAME,conn);
		}catch(GluuRadiusLdapException e) {
			throw new GluuRadiusServiceException("LDAP operation failed",e);
		}finally {
			if(conn!=null)
				conn.release();
		}
	}


	@Override
	public Integer getRadiusAuthenticationPort() {

		GluuRadiusLdapConnection conn = null;
		try {
			conn = connprovider.getBoundLdapConnection();
			return oxRadiusConfigAttributeAsInt(OXRADIUS_AUTHENTICATION_PORT_ATTRIBUTE_NAME,conn);
		}catch(GluuRadiusLdapException e) {
			throw new GluuRadiusServiceException("LDAP operation failed",e);
		}finally {
			if(conn!=null)
				conn.release();
		}
	}

	@Override
	public Integer getRadiusAccountingPort() {

		GluuRadiusLdapConnection conn = null;
		try {
			conn = connprovider.getBoundLdapConnection();
			return oxRadiusConfigAttributeAsInt(OXRADIUS_ACCOUNTING_PORT_ATTRIBUTE_NAME,conn);
		}catch(GluuRadiusLdapException e) {
			throw new GluuRadiusServiceException("LDAP operation failed",e);
		}finally {
			if(conn!=null)
				conn.release();
		}
	}


	@Override
	public String getClientSharedSecret(String clientipaddress) {

		GluuRadiusLdapConnection conn = null;
		try {
			conn = connprovider.getBoundLdapConnection();
			String clientconfig = oxRadiusConfigAttributeAsString(OXRADIUS_CLIENT_CONFIG_ATTRIBUTE_NAME,conn);
			return null;
		}catch(GluuRadiusLdapException e) {
			throw new GluuRadiusServiceException("LDAP operation failed",e);
		}finally {
			if(conn!=null)
				conn.release();
		}
	}


	private String getUserDN(String username,GluuRadiusLdapConnection conn) {

		try {
			SearchRequest request = buildUserSearchRequest(username);
			SearchResultEntry entry = conn.getConnection().searchForEntry(request);
			if(entry == null)
				throw new GluuRadiusServiceException("User '"+username+"' not found");
			return entry.getDN();
		}catch(LDAPSearchException e) {
			throw new GluuRadiusServiceException("LDAP Search operation failed",e);
		}
	}

	private String oxRadiusConfigAttributeAsString(String attribute,GluuRadiusLdapConnection conn) {

		try {
			SearchRequest request = buildoxRadiusConfigurationSearchRequest(attribute);
			SearchResultEntry entry = conn.getConnection().searchForEntry(request);
			if(entry == null)
				throw new GluuRadiusServiceException("LDAP configuration probably missing");
			return entry.getAttributeValue(attribute);	
		}catch(LDAPSearchException e) {
			throw new GluuRadiusServiceException("LDAP Search operation failed",e);
		}
	}


	private Integer oxRadiusConfigAttributeAsInt(String attribute,GluuRadiusLdapConnection conn)  {

		try {
			SearchRequest request = buildoxRadiusConfigurationSearchRequest(attribute);
			SearchResultEntry entry = conn.getConnection().searchForEntry(request);
			if(entry == null)
				throw new GluuRadiusServiceException("LDAP configuration probably missing");
			return entry.getAttributeValueAsInteger(attribute);
		}catch(LDAPSearchException e) {
			throw new GluuRadiusServiceException("LDAP Search operation failed",e);
		}
	}

	private SearchRequest buildUserSearchRequest(String username) {

		Filter filter = buildUserSearchFilter(username);
		return new SearchRequest(GLUU_BASE_DN,SearchScope.SUB,filter,UID_ATTRIBUTE_NAME);
	}

	private SearchRequest buildoxRadiusConfigurationSearchRequest(String ... attributes) {

		Filter filter = buildoxRadiusConfigurationSearchFilter();
		return new SearchRequest(APPLIANCES_BASE_DN,SearchScope.SUB,filter,attributes);
	}

	private Filter buildUserSearchFilter(String username) {

		Filter uidfilter = Filter.createEqualityFilter(UID_ATTRIBUTE_NAME,username);
		Filter personobjfilter = Filter.createEqualityFilter(OBJECTCLASS_ATTRIBUTE_NAME,GLUUPERSON_OBJECTCLASS);
		Filter custpersonobjfilter = Filter.createEqualityFilter(OBJECTCLASS_ATTRIBUTE_NAME,GLUUCUSTOMPERSON_OBJECTCLASS);
		return Filter.createANDFilter(uidfilter,personobjfilter,custpersonobjfilter);
	}

	private Filter buildoxRadiusConfigurationSearchFilter() {

		Filter oxrconfigfilter= Filter.createEqualityFilter(OBJECTCLASS_ATTRIBUTE_NAME,OXRADIUS_CONFIGURATION_OBJECTCLASS);
		return oxrconfigfilter;
	}

	private String getRadiusClientPassword(String ipaddress,String jsondata) {

		List<GluuRadiusClient> clients = radiusClientListFromJson(jsondata);
		for(GluuRadiusClient client : clients) {
			if(client.isIpAddress(ipaddress))
				return decryptClientSecret(client.getSecret());
		}
		return null;
	}

	private List<GluuRadiusClient> radiusClientListFromJson(String jsonstring) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			GluuRadiusClientConfig config = mapper.readValue(jsonstring,GluuRadiusClientConfig.class);
			return config.getClients();
		}catch(IOException e) {
			throw new GluuRadiusServiceException("An I/O error occured while getting the client list",e);
		}
	}

	private String decryptClientSecret(String encryptedsecret) {

		try {
			return CryptoUtil.decryptPassword(encryptedsecret,decryptionkey);
		}catch(GluuRadiusException e) {
			throw new GluuRadiusServiceException("Client secret decryption failed",e);
		}
	}
}