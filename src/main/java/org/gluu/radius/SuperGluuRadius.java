package org.gluu.radius;

import org.apache.log4j.Logger;

import org.gluu.radius.config.GluuRadiusBootstrapConfig;
import org.gluu.radius.config.GluuRadiusOpenIdConfig;
import org.gluu.radius.ldap.GluuRadiusLdapConnectionProvider;
import org.gluu.radius.ldap.GluuRadiusLdapException;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.server.GluuRadiusServerException;
import org.gluu.radius.server.GluuRadiusServerFactory;
import org.gluu.radius.services.*;
import org.gluu.radius.services.impl.GluuRadiusBootstrapConfigServiceImpl;
import org.gluu.radius.services.impl.GluuRadiusLdapServiceImpl;
import org.gluu.radius.services.impl.GluuRadiusUserAuthServiceImpl;


public class SuperGluuRadius {

	private static final Logger logger = Logger.getLogger(SuperGluuRadius.class);
	private static final String WAIT_TIMEOUT_PROPERTY_NAME = "org.gluu.radius.wait_timeout";
	private static final String RETRYCOUNT_PROPERTY_NAME = "org.gluu.radius.retrycount";
	private static final long DEFAULT_WAIT_TIMEOUT = 10; // in seconds 
	private static final int  DEFAULT_RETRYCOUNT = 2; // retry twice
	private static GluuRadiusServer server = null; 

	public static void main(String [] args) {
		
		printApplicationHeader();
		if(args.length == 0) {
			logger.fatal("Configuration file not specified on command line. Exiting... ");
			System.exit(-1);
		}

		String configfile = args[0];
		logger.info("Boostrap configuration file : " + configfile);


		logger.info("Registering bootstrap configuration service ... ");
		if(!registerBootstrapConfigService(configfile)) {
			logger.fatal("An error occured while registering the service. Exiting... ");
			System.exit(-1);
		}

		int ldapretrycount = 0;
		boolean ldapregistered = false;
		logger.info("Registering LDAP service... ");
		do {
			boolean logerror = ldapretrycount + 1 >= getRetryCount();
			ldapregistered = registerLdapService(logerror);
			if(ldapregistered == true)
				break;
			if(ldapretrycount + 1 < getRetryCount())
				waitForTimeout(getWaitTimeout());
		}while(++ldapretrycount<getRetryCount());

		if(!ldapregistered) {
			logger.fatal("An error occured while registering the service. Exiting...");
			System.exit(-1);
		}

		

		logger.info("Registering user authentication service... ");
		if(!registerUserAuthService()) {
			logger.fatal("An error occured while registering the service. Exiting... ");
			System.exit(-1);
		}

		
		logger.info("Creating Radius Server... ");
		if(!createRadiusServer()) {
			logger.fatal("An error occured while creating the radius server. Exiting... ");
			System.exit(-1);
		}

		logger.info("Starting Radius Server... ");
		if(!runRadiusServer()) {
			logger.fatal("An error occured while running the radius server. Exiting... ");
			System.exit(-1);
		}
		
		GluuRadiusServerRunner runner = new GluuRadiusServerRunner(server);
		runner.start();
		Runtime.getRuntime().addShutdownHook(new GluuRadiusShutdownHook(runner));
		logger.info("Super Gluu Radius running.");

	}

	private static final void printApplicationHeader() {

		logger.info(" ");
		logger.info(" ");
		logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		logger.info("+ Super Gluu Radius                                            ");
		logger.info("+ Copyright (c) Gluu Inc.                                      ");
		logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}

	private static final boolean registerBootstrapConfigService(String configfile) {

		boolean ret = false;
		try {
			GluuRadiusBootstrapConfigService service = new GluuRadiusBootstrapConfigServiceImpl(configfile);
			GluuRadiusServiceLocator.registerService(GluuRadiusKnownService.BootstrapService,service);
			ret = true;
		}catch(GluuRadiusServiceException e) {
			logger.error("Bootstrap configuration service registration failed",e);
		}
		return ret;
	}


	private static final boolean registerLdapService(boolean logerror) {

		boolean ret = false;
		try {
			GluuRadiusBootstrapConfig config = getBootstrapConfig();
			GluuRadiusLdapConnectionProvider connprovider = new GluuRadiusLdapConnectionProvider(config);
			GluuRadiusLdapService svc  = new GluuRadiusLdapServiceImpl(connprovider);
			GluuRadiusServiceLocator.registerService(GluuRadiusKnownService.LdapService,svc);
			ret = true;
		}catch(GluuRadiusServiceException e) {
			if(logerror)
				logger.error("LDAP service registration failed",e);
		}catch(GluuRadiusLdapException e) {
			if(logerror)
				logger.error("LDAP service registration failed",e);
		}
		return ret;
	}

	private static final boolean registerUserAuthService() {

		boolean ret = false;
		try {
			String key = getEncryptionKey();
			GluuRadiusOpenIdConfig config = getOpenIdConfig();
			GluuRadiusUserAuthService svc = new GluuRadiusUserAuthServiceImpl(key,config);
			GluuRadiusServiceLocator.registerService(GluuRadiusKnownService.UserAuthService,svc);
			ret = true;
		}catch(GluuRadiusServiceException e) {
			logger.error("LDAP user auth service registration failed",e);
		}
		return ret;
	}


	private static final boolean createRadiusServer() {

		boolean ret = false;
		try {
			server = GluuRadiusServerFactory.create();
			ret = true;
		}catch(GluuRadiusServerException e) {
			logger.error("Could not create radius server",e);
		}

		return ret;
	}

	private static final boolean runRadiusServer() {

		boolean ret = false;
		if(server==null)
			return ret;

		try {
			server.run();
			ret = true;
		}catch(GluuRadiusServerException e) {
			logger.error("Could not start radius server",e);
		}
		return ret;
	}


	private static final GluuRadiusBootstrapConfig getBootstrapConfig() {

		GluuRadiusBootstrapConfigService svc = 
			GluuRadiusServiceLocator.getService(GluuRadiusKnownService.BootstrapService);
		return svc.getBootstrapConfiguration();
		
	}

	private static final String getEncryptionKey()  {

		GluuRadiusBootstrapConfigService svc = 
			GluuRadiusServiceLocator.getService(GluuRadiusKnownService.BootstrapService);
		return svc.getEncryptionKey();
	}

	private static final GluuRadiusOpenIdConfig getOpenIdConfig() {

		GluuRadiusLdapService ldapsvc = 
			GluuRadiusServiceLocator.getService(GluuRadiusKnownService.LdapService);
		return ldapsvc.getRadiusOpenIdConfig();
	}

	private static final void waitForTimeout(long timeout) {

		try {
			Thread.currentThread().sleep(timeout*1000);
		}catch(InterruptedException e) {
			logger.warn("Thread wait interrupted",e);
		}
	}

	private static final long getWaitTimeout() {

		long ret = DEFAULT_WAIT_TIMEOUT;
		try {
			String strvalue = getSystemProperty(WAIT_TIMEOUT_PROPERTY_NAME);
			if(strvalue!=null)
				ret = Long.parseLong(strvalue);
		}catch(NumberFormatException e) {
			logger.warn("Error getting wait timeout",e);
		}
		return ret;
	}

	private static final int getRetryCount() {

		int ret = DEFAULT_RETRYCOUNT;
		try {
			String strvalue = getSystemProperty(RETRYCOUNT_PROPERTY_NAME);
			if(strvalue!=null)
				ret = Integer.parseInt(strvalue);
		}catch(NumberFormatException e) {
			logger.warn("Error getting retry count",e);
		}

		return ret;
	}

	private static final String getSystemProperty(String name) {

		String ret =  null;
		try {
			ret = System.getProperty(name);
		}catch(SecurityException e) {
			logger.warn("Error getting system property "+name,e);
		}catch(NullPointerException e) {
			logger.warn("Error getting system property "+name,e);
		}catch(IllegalArgumentException e) {
			logger.warn("Error getting system property "+name,e);
		}
		return ret;
	}
}