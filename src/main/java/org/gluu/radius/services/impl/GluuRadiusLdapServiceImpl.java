package org.gluu.radius.services.impl;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchResultListener;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;

import java.io.IOException;
import java.util.List;

import org.gluu.radius.config.GluuRadiusCacheConfig;
import org.gluu.radius.config.GluuRadiusClientConfig;
import org.gluu.radius.config.GluuRadiusOpenIdConfig;
import org.gluu.radius.config.GluuRadiusServerConfig;

import org.gluu.radius.GluuRadiusException;
import org.gluu.radius.ldap.GluuRadiusLdapException;
import org.gluu.radius.ldap.GluuRadiusLdapConnectionProvider;
import org.gluu.radius.ldap.GluuRadiusLdapConnectionProvider.GluuRadiusLdapConnection;
import org.gluu.radius.services.GluuRadiusLdapService;
import org.gluu.radius.services.GluuRadiusServiceException;




public class GluuRadiusLdapServiceImpl implements GluuRadiusLdapService {
	

	private static final String UID_ATTRIBUTE_NAME = "uid";
	private static final String OBJECTCLASS_ATTRIBUTE_NAME = "objectClass";
	private static final String GLUUPERSON_OBJECTCLASS = "gluuPerson";
	private static final String GLUUCUSTOMPERSON_OBJECTCLASS = "gluuCustomPerson";
	private static final String ORGANIZATIONALUNIT_OBJECTCLASS = "organizationalUnit";
	private static final String TOP_OBJECTCLASS = "top";
	private static final String GLUU_BASE_DN = "o=gluu";
	private static final String APPLIANCES_BASE_DN = "ou=appliances,o=gluu";
	private static final String ORGANIZATION_UNIT_ATTRIBUTE_NAME = "ou";
	private static final String OXTRUST_ORGANIZATION_UNIT_ATTRIBUTE_VALUE = "oxtrust";
	private static final String CONFIGROOT_ORGANIZATION_UNIT_ATTRIBUTE_NAME = "configuration";
	private static final String OXTRUST_CONFIGURATION_OBJECTCLASS = "oxTrustConfiguration";
	private static final String OXTRUST_CONFIGURATION_ATTRIBUTE_NAME = "oxTrustConfApplication";
	private static final String OXRADIUS_CONFIGURATION_OBJECTCLASS = "oxRadiusConfiguration";
	private static final String OXAUTH_CLIENT_OBJECTCLASS = "oxAuthClient";
	private static final String OXAUTH_CLIENT_DISPLAYNAME_ATTRIBUTE_NAME = "displayName";
	private static final String OXAUTH_CLIENT_SECRET_ATTRIBUTE_NAME = "oxAuthClientSecret";
	private static final String OXAUTH_CLIENT_INUM_ATTRIBUTE_NAME = "inum";

	private static final String OXRADIUS_RDN = "ou=oxradius";
	private static final String OXRADIUS_ORGANIZATION_UNIT="oxradius";
	private static final String OXRADIUS_LISTEN_IP_ADDRESS_ATTRIBUTE_NAME = "oxRadiusListenIpAddress";
	private static final String OXRADIUS_ACCOUNTING_PORT_ATTRIBUTE_NAME = "oxRadiusAccountingPort";
	private static final String OXRADIUS_AUTHENTICATION_PORT_ATTRIBUTE_NAME = "oxRadiusAuthenticationPort";
	private static final String OXRADIUS_CLIENT_CONFIG_ATTRIBUTE_NAME = "oxRadiusClientConfig";
	private static final String OXRADIUS_CLIENT_DISPLAY_NAME_ATTRIBUTE_NAME = "oxRadiusOpenIdClientName";
	private static final String OXRADIUS_HTTP_CPOOL_SIZE_ATTRIBUTE_NAME = "oxRadiusHttpConnPoolSize";
	private static final String OXRADIUS_CLIENT_CACHE_DURATION_ATTRIBUTE_NAME = "oxRadiusClientCacheDuration";
	private static final String OXRADIUS_HTTP_SSLVERIFY_ATTRIBUTE_NAME = "oxRadiusHttpSslVerify";

	private static final String OXRADIUS_DEFAULT_LISTEN_IP_ADDRESS = "0.0.0.0"; // all interfaces 
	private static final String OXRADIUS_DEFAULT_ACCOUNTING_PORT = "1812";
	private static final String OXRADIUS_DEFAULT_AUTHENTICATION_PORT = "1813";
	private static final String OXRADIUS_DEFAULT_CLIENT_CONFIG="{\"clients\":[]}";
	private static final String OXRADIUS_DEFAULT_CLIENT_DISPLAY_NAME="oxRadius";
	private static final String OXRADIUS_DEFAULT_HTTP_CPOOL_SIZE="10";
	private static final String OXRADIUS_DEFAULT_CLIENT_CACHE_DURATION="-1"; // caching disabled
	private static final String OXRADIUS_DEFAULT_HTTPSSLVERIFY="true"; 

	private static final Integer CACHE_DURATION_CACHE_DISABLED =  -1;
	private static final Integer CACHE_DURATION_CACHE_NO_EXPIRY =  0; 

	private GluuRadiusLdapConnectionProvider connprovider;

	public GluuRadiusLdapServiceImpl(GluuRadiusLdapConnectionProvider connprovider) {

		this.connprovider = connprovider;
		oxRadiusConfigurationInit();
	}


	@Override
	public GluuRadiusServerConfig getRadiusServerConfig() {

		GluuRadiusLdapConnection conn = null;
		try {
			GluuRadiusServerConfig config = new GluuRadiusServerConfig();
			conn = connprovider.getConnection();
			String listenaddres = oxRadiusConfigAttributeAsString(OXRADIUS_LISTEN_IP_ADDRESS_ATTRIBUTE_NAME,conn);
			Integer authport = oxRadiusConfigAttributeAsInt(OXRADIUS_AUTHENTICATION_PORT_ATTRIBUTE_NAME,conn);
			Integer acctport = oxRadiusConfigAttributeAsInt(OXRADIUS_ACCOUNTING_PORT_ATTRIBUTE_NAME,conn);
			config.setListenAddress(listenaddres);
			config.setAuthPort(authport);
			config.setAcctPort(acctport);
			return config;
		}catch(GluuRadiusLdapException e) {
			throw new GluuRadiusServiceException("LDAP operation failed",e);
		}finally {
			if(conn!=null)
				conn.release();
		}
	}


	@Override
	public GluuRadiusOpenIdConfig getRadiusOpenIdConfig() {

		GluuRadiusLdapConnection conn = null;
		try {
			GluuRadiusOpenIdConfig config = new GluuRadiusOpenIdConfig();
			conn = connprovider.getConnection();
			String clientdisplayname = oxRadiusConfigAttributeAsString(OXRADIUS_CLIENT_DISPLAY_NAME_ATTRIBUTE_NAME,conn);
			GluuOxTrustConfig oxtrustconf = getOxTrustConfig(conn);
			config.setApplianceUrl(oxtrustconf.getApplianceUrl());
			getOpenIdConfigFromOxAuthConfig(clientdisplayname,config,conn);
			Integer cpsize = oxRadiusConfigAttributeAsInt(OXRADIUS_HTTP_CPOOL_SIZE_ATTRIBUTE_NAME,conn);
			if(cpsize != null)
				config.setCpSize(cpsize);
			Boolean sslverifyenabled = oxRadiusConfigAttributeAsBoolean(OXRADIUS_HTTP_SSLVERIFY_ATTRIBUTE_NAME,conn);
			if(sslverifyenabled != null)
				config.setSslVerifyEnabled(sslverifyenabled);
			return config;
		}catch(GluuRadiusLdapException e) {
			throw new GluuRadiusServiceException("LDAP operation failed",e);
		}finally {
			if(conn != null)
				conn.release();
		}
	}


	@Override
	public GluuRadiusClientConfig getRadiusClientConfig(String ipaddress) {

		if(ipaddress == null)
			throw new GluuRadiusServiceException("IP Address of client cannot be Null");

		return fetchClientConfigFromServer(ipaddress);
	}


	@Override
	public GluuRadiusCacheConfig getRadiusClientCacheConfig() {

		GluuRadiusLdapConnection conn = null;
		try {

			conn =connprovider.getConnection();
			Integer cacheduration = oxRadiusConfigAttributeAsInt(OXRADIUS_CLIENT_CACHE_DURATION_ATTRIBUTE_NAME,conn);

			if(cacheduration == null)
				cacheduration = CACHE_DURATION_CACHE_DISABLED;

			if(cacheduration == CACHE_DURATION_CACHE_DISABLED)
				return GluuRadiusCacheConfig.createCachingDisabledConfig();
			else if(cacheduration == CACHE_DURATION_CACHE_NO_EXPIRY)
				return GluuRadiusCacheConfig.createNoExpiryConfig();
			else
				return GluuRadiusCacheConfig.createNormalConfig(cacheduration);
		}catch(GluuRadiusLdapException e) {
			throw new GluuRadiusServiceException("LDAP operation failed",e);
		}finally {
			if(conn != null)
				conn.release();
		}
	}


	private void oxRadiusConfigurationInit() {

		GluuRadiusLdapConnection conn = null;
		try {
			conn  = connprovider.getConnection();
			SearchRequest request = buildoxRadiusConfigurationSearchRequest(
				OXRADIUS_LISTEN_IP_ADDRESS_ATTRIBUTE_NAME,
				OXRADIUS_AUTHENTICATION_PORT_ATTRIBUTE_NAME,
				OXRADIUS_ACCOUNTING_PORT_ATTRIBUTE_NAME,
				OXRADIUS_CLIENT_CONFIG_ATTRIBUTE_NAME,
				OXRADIUS_CLIENT_DISPLAY_NAME_ATTRIBUTE_NAME,
				OXRADIUS_HTTP_CPOOL_SIZE_ATTRIBUTE_NAME,
				OXRADIUS_CLIENT_CACHE_DURATION_ATTRIBUTE_NAME,
				OXRADIUS_HTTP_SSLVERIFY_ATTRIBUTE_NAME);
			SearchResultEntry entry = conn.getConnection().searchForEntry(request);
			if(entry == null)
				oxRadiusConfigurationCreate(conn);
		}catch(GluuRadiusLdapException e) {
			throw new GluuRadiusServiceException("LDAP Operation failed",e);
		}catch(LDAPSearchException e) {
			throw new GluuRadiusServiceException("LDAP Search Operation Failed",e);
		}catch(LDAPException e)  {
			throw new GluuRadiusServiceException("A generic LDAP operation failed",e);
		}finally {
			if(conn != null)
				conn.release();
		}
	}

	private void oxRadiusConfigurationCreate(GluuRadiusLdapConnection conn) throws LDAPException {

		SearchRequest request = buildConfigRootSearchRequest();
		SearchResultEntry entry = conn.getConnection().searchForEntry(request);
		if(entry == null)
			throw new GluuRadiusServiceException("Config root not found on LDAP server");
		String radiusconfigdn = OXRADIUS_RDN + "," + entry.getDN();
		Entry radiusconfig = new Entry(radiusconfigdn);

		radiusconfig.addAttribute(OBJECTCLASS_ATTRIBUTE_NAME,OXRADIUS_CONFIGURATION_OBJECTCLASS);
		radiusconfig.addAttribute(OBJECTCLASS_ATTRIBUTE_NAME,TOP_OBJECTCLASS);
		radiusconfig.addAttribute(ORGANIZATION_UNIT_ATTRIBUTE_NAME,OXRADIUS_ORGANIZATION_UNIT);

		radiusconfig.addAttribute(OXRADIUS_LISTEN_IP_ADDRESS_ATTRIBUTE_NAME,OXRADIUS_DEFAULT_LISTEN_IP_ADDRESS);
		radiusconfig.addAttribute(OXRADIUS_AUTHENTICATION_PORT_ATTRIBUTE_NAME,OXRADIUS_DEFAULT_AUTHENTICATION_PORT);
		radiusconfig.addAttribute(OXRADIUS_ACCOUNTING_PORT_ATTRIBUTE_NAME,OXRADIUS_DEFAULT_ACCOUNTING_PORT);
		radiusconfig.addAttribute(OXRADIUS_CLIENT_CONFIG_ATTRIBUTE_NAME,OXRADIUS_DEFAULT_CLIENT_CONFIG);
		radiusconfig.addAttribute(OXRADIUS_CLIENT_DISPLAY_NAME_ATTRIBUTE_NAME,OXRADIUS_DEFAULT_CLIENT_DISPLAY_NAME);
		radiusconfig.addAttribute(OXRADIUS_HTTP_CPOOL_SIZE_ATTRIBUTE_NAME,OXRADIUS_DEFAULT_HTTP_CPOOL_SIZE);
		radiusconfig.addAttribute(OXRADIUS_CLIENT_CACHE_DURATION_ATTRIBUTE_NAME,OXRADIUS_DEFAULT_CLIENT_CACHE_DURATION);
		radiusconfig.addAttribute(OXRADIUS_HTTP_SSLVERIFY_ATTRIBUTE_NAME,OXRADIUS_DEFAULT_HTTPSSLVERIFY);

		conn.getConnection().add(radiusconfig);
	}

	private GluuRadiusClientConfig fetchClientConfigFromServer(String ipaddress) {

		GluuRadiusLdapConnection conn = null;
		try {
			conn = connprovider.getConnection();
			String jsondata = oxRadiusConfigAttributeAsString(OXRADIUS_CLIENT_CONFIG_ATTRIBUTE_NAME,conn);
			if(jsondata == null)
				return null;
			GluuRadiusClientConfig clientconfig = GluuRadiusClientConfigList.fromJson(jsondata).getClientConfig(ipaddress);
			return clientconfig;
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

	private GluuOxTrustConfig getOxTrustConfig(GluuRadiusLdapConnection conn) {

		try {
			SearchRequest request = buildoxTrustConfigSearchRequest(OXTRUST_CONFIGURATION_ATTRIBUTE_NAME);
			SearchResultEntry entry = conn.getConnection().searchForEntry(request);
			if(entry == null)
				throw new GluuRadiusServiceException("oxTrust configuration could not be loaded");
			return GluuOxTrustConfig.fromJson(entry.getAttributeValue(OXTRUST_CONFIGURATION_ATTRIBUTE_NAME));
		}catch(LDAPSearchException e) {
			throw new GluuRadiusServiceException("LDAP Search operation failed",e);
		}catch(IOException e) {
			e.printStackTrace();
			throw new GluuRadiusServiceException("An I/O Error occured while loading the oxTrust configuration");
		}
	}

	private void getOpenIdConfigFromOxAuthConfig(String displayname,GluuRadiusOpenIdConfig config,GluuRadiusLdapConnection conn) {

		try {
			SearchRequest request = buildoxAuthClientSearchRequest(displayname,OXAUTH_CLIENT_INUM_ATTRIBUTE_NAME,
				OXAUTH_CLIENT_SECRET_ATTRIBUTE_NAME);
			SearchResultEntry entry = conn.getConnection().searchForEntry(request);
			if( entry == null)
				throw new GluuRadiusServiceException("oxAuthClient configuration not loaded");

			config.setClientId(entry.getAttributeValue(OXAUTH_CLIENT_INUM_ATTRIBUTE_NAME));
			config.setClientSecret(entry.getAttributeValue(OXAUTH_CLIENT_SECRET_ATTRIBUTE_NAME));
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

	private Boolean oxRadiusConfigAttributeAsBoolean(String attribute,GluuRadiusLdapConnection conn) {

		try {
			SearchRequest request = buildoxRadiusConfigurationSearchRequest(attribute);
			SearchResultEntry entry = conn.getConnection().searchForEntry(request);
			if(entry == null)
				throw new GluuRadiusServiceException("LDAP configuration probably missing");
			return entry.getAttributeValueAsBoolean(attribute);
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

	private SearchRequest buildoxTrustConfigSearchRequest(String ... attributes) {

		Filter filter = buildoxTrustConfigSearchFilter();
		return new SearchRequest(APPLIANCES_BASE_DN,SearchScope.SUB,filter,attributes);

	}

	private SearchRequest buildoxAuthClientSearchRequest(String displayname,String ... attributes) {

		Filter filter = buildoxAuthClientSearchFilter(displayname);
		return new SearchRequest(GLUU_BASE_DN,SearchScope.SUB,filter,attributes);
	}

	private SearchRequest buildConfigRootSearchRequest(String ... attributes) {

		Filter filter = buildConfigRootSearchFilter();
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

	private Filter buildoxTrustConfigSearchFilter() {

		Filter oxtrustobjfilter = Filter.createEqualityFilter(OBJECTCLASS_ATTRIBUTE_NAME,OXTRUST_CONFIGURATION_OBJECTCLASS);
		Filter oufilter = Filter.createEqualityFilter(ORGANIZATION_UNIT_ATTRIBUTE_NAME,OXTRUST_ORGANIZATION_UNIT_ATTRIBUTE_VALUE);
		return Filter.createANDFilter(oxtrustobjfilter,oufilter);
	}


	private Filter buildoxAuthClientSearchFilter(String displayname) {

		Filter oxauthclientobjfilter = Filter.createEqualityFilter(OBJECTCLASS_ATTRIBUTE_NAME,OXAUTH_CLIENT_OBJECTCLASS);
		Filter displaynamefilter = Filter.createEqualityFilter(OXAUTH_CLIENT_DISPLAYNAME_ATTRIBUTE_NAME,displayname);
		return Filter.createANDFilter(oxauthclientobjfilter,displaynamefilter);
	}

	private Filter buildConfigRootSearchFilter() {

		Filter oufilter = Filter.createEqualityFilter(ORGANIZATION_UNIT_ATTRIBUTE_NAME,CONFIGROOT_ORGANIZATION_UNIT_ATTRIBUTE_NAME);
		Filter ouobjclass = Filter.createEqualityFilter(OBJECTCLASS_ATTRIBUTE_NAME,ORGANIZATIONALUNIT_OBJECTCLASS);
		return Filter.createANDFilter(oufilter,ouobjclass);
	}
	
}