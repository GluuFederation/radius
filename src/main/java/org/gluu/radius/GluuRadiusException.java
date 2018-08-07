package org.gluu.radius;


public class GluuRadiusException extends RuntimeException {
	
	public GluuRadiusException(String msg) {
		super(msg);
	}

	public GluuRadiusException(String msg, Throwable cause) {
		super(msg,cause);
	}
}