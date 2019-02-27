package org.gluu.radius.exception;


public class ServerException extends GluuRadiusException {

    private static final long serialVersionUID = -1L;

    public ServerException(String msg) {
        super(msg);
    }

    public ServerException(Throwable cause) {
        super(cause);
    }

    public ServerException(String msg, Throwable cause) {
        super(msg,cause);
    }
}