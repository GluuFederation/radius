package org.gluu.radius.services;


public class GluuRadiusServiceException extends RuntimeException {
	
	public GluuRadiusServiceException(String msg) {
		super(msg);
	}

	public GluuRadiusServiceException(String msg,Throwable reason) {

		super(msg,reason);
	}
}