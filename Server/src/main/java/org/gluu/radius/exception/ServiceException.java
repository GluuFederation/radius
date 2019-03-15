package org.gluu.radius.exception;


public class ServiceException extends GluuRadiusException {

    private static final long serialVersionUID = -1L;

    public ServiceException(String msg) {
        super(msg);
    }
    
    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String msg,Throwable cause) {
        super(msg,cause);
    }

}