package org.gluu.radius.exception;


public class GluuRadiusException extends RuntimeException {

    private static final long serialVersionUID = -1L;
    
    public GluuRadiusException(Throwable cause) {
        super(cause);
    }
    
    public GluuRadiusException(String msg) {
        super(msg);
    }

    public GluuRadiusException(String msg, Throwable cause) {
        super(msg,cause);
    }
}