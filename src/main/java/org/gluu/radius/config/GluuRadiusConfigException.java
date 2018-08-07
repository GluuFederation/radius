package org.gluu.radius.config;

import org.gluu.radius.GluuRadiusException;

public class GluuRadiusConfigException extends GluuRadiusException {
	
	public GluuRadiusConfigException(String msg) {
		super(msg);
	}


	public GluuRadiusConfigException(String msg, Throwable cause) {
		super(msg,cause);
	}
}