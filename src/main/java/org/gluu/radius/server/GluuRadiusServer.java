package org.gluu.radius.server;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import org.gluu.radius.GluuRadiusException;

public abstract class GluuRadiusServer {

	private static final Logger logger = Logger.getLogger(GluuRadiusServer.class);

	protected GluuRadiusSharedSecretProvider ssprovider;
	protected List<GluuRadiusAccessRequestFilter> accessrequestfilters;
	protected List<GluuRadiusAccountingRequestFilter> accountingrequestfilters;

	public GluuRadiusServer() {

		this.ssprovider = null;
		this.accessrequestfilters = new ArrayList<GluuRadiusAccessRequestFilter>();
		this.accountingrequestfilters = new ArrayList<GluuRadiusAccountingRequestFilter>();
	}

	public GluuRadiusServer addAccessRequestFilter(GluuRadiusAccessRequestFilter filter) {

		if(filter!=null)
			accessrequestfilters.add(filter);
		return this;
	}


	public GluuRadiusServer addAccountingRequestFilter(GluuRadiusAccountingRequestFilter filter) {

		if(filter!=null)
			accountingrequestfilters.add(filter);
		return this;
	}

	public GluuRadiusServer setSharedSecretProvider(GluuRadiusSharedSecretProvider ssprovider) {

		this.ssprovider = ssprovider;
		return this;
	}

	protected boolean onAccessRequest(GluuRadiusAccessRequestContext context) {

		boolean ret = false;
		try {
			for(GluuRadiusAccessRequestFilter arfilter : accessrequestfilters) {
				if(arfilter.processAccessRequest(context) == true) {
					ret =  true;
					break;
				}
			}
		}catch(GluuRadiusException e) {
			logger.warn("Exception caught when processing access request by filters",e);
		}
		return ret;
	}


	protected void onAccountingRequest(GluuRadiusAccountingRequestContext context) {

		try {
			for(GluuRadiusAccountingRequestFilter acfilter : accountingrequestfilters) {
				acfilter.processAccountingRequest(context);
			}
		}catch(GluuRadiusException e) {
			logger.warn("Exception caught when processing accounting request by filters",e);
		}
	}


	protected String getSharedSecret(String ipaddress) {

		String secret = null;
		try {
			secret = ssprovider.getSharedSecret(ipaddress);
		}catch(GluuRadiusException e) {
			logger.warn("Exception caught when getting client shared secret "+ipaddress,e);
		}
		return secret;
	}


	public abstract GluuRadiusServer run();
	public abstract GluuRadiusServer shutdown();
}
