package org.gluu.radius.exception;


public class GenericPersistenceException extends GluuRadiusException {

    private static final long serialVersionUID = -1L;

    public GenericPersistenceException(String msg) {
        super(msg);
    }

    public GenericPersistenceException(Throwable cause) {
        super(cause);
    }

    public GenericPersistenceException(String msg,Throwable cause) {
        super(msg,cause);
    }
}