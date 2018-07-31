package org.gluu.radius.server;


public class GluuRadiusException extends RuntimeException {
	

	public GluuRadiusException(String msg) {
		super(msg);
	}

	public GluuRadiusException(String msg,Throwable reason) {
		super(msg,reason);
	}
}