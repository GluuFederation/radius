package org.gluu.radius.server;

import org.gluu.radius.GluuRadiusException;


public class GluuRadiusServerException extends RuntimeException {
	

	public GluuRadiusServerException(String msg) {
		super(msg);
	}

	public GluuRadiusServerException(String msg,Throwable reason) {
		super(msg,reason);
	}
}