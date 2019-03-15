package org.gluu.radius.exception;

public class ServerFactoryException extends GluuRadiusException {

    private static final long serialVersionUID = -1L;
     
    public ServerFactoryException(String msg) {
        super(msg);
    }

    public ServerFactoryException(Throwable cause) {
        super(cause);
    }

    public ServerFactoryException(String msg,Throwable cause) {
        super(msg,cause);
    }
} 