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

		logger.info("Registering LDAP service... ");
		if(!registerLdapService()) {
			logger.fatal("An error occured while registering the service. Exiting... ");
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

		logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		logger.info("+ Super Gluu Radius                                            ");
		logger.info("+ Copyright (c) Gluu Inc.                                      ");
		logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}

	private static final boolean registerBootstrapConfigService(String configfile) {

		boolean ret = false;
		try {
			GluuRadiusBootstrapConfigService svc = new GluuRadiusBootstrapConfigServiceImpl(configfile);
			GluuRadiusServiceLocator.registerService(GluuRadiusKnownService.BootstrapService,ret);
			ret = true;
		}catch(GluuRadiusServiceException e) {
			logger.error("Bootstrap configuration service registration failed",e);
		}
		return ret;
	}


	private static final boolean registerLdapService() {

		boolean ret = false;
		try {
			GluuRadiusBootstrapConfig config = getBootstrapConfig();
			GluuRadiusLdapConnectionProvider connprovider = new GluuRadiusLdapConnectionProvider(config);
			GluuRadiusLdapService svc  = new GluuRadiusLdapServiceImpl(connprovider);
			GluuRadiusServiceLocator.registerService(GluuRadiusKnownService.LdapService,svc);
			ret = true;
		}catch(GluuRadiusServiceException e) {
			logger.error("LDAP service registration failed",e);
		}catch(GluuRadiusLdapException e) {
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
}