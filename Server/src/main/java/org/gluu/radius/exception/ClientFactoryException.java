package org.gluu.radius.exception;

public class ClientFactoryException extends GluuRadiusException {

    private static final long serialVersionUID = -1L;
    
    public ClientFactoryException(String msg) {
        super(msg);
    }
    
    public ClientFactoryException(Throwable cause) {
        super(cause);
    }

    public ClientFactoryException(String msg, Throwable cause) {
        super(msg,cause);
    }
}