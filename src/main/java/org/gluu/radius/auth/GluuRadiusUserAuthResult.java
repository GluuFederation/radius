package org.gluu.radius.auth;



public class GluuRadiusUserAuthResult {
	
	private boolean success;
	private String error;
	private String errordescription;

	public GluuRadiusUserAuthResult(boolean success,String error,String errordescription) {

		this.success  =  success;
		this.error = error;
		this.errordescription = errordescription;
	}


	public boolean isSuccess() {

		return this.success;
	}


	public String getError() {

		return this.error;
	}

	public String getErrorDescription() {

		return this.errordescription;
	}

	public static final GluuRadiusUserAuthResult buildSuccessAuthResult() {

		return new GluuRadiusUserAuthResult(true,null,null);
	}

	public static final GluuRadiusUserAuthResult buildErrorAuthResult(String error,String errordescription) {

		return new GluuRadiusUserAuthResult(false,error,errordescription);
	}
}