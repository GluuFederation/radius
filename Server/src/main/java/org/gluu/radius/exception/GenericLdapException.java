package org.gluu.radius.exception;


public class GenericLdapException extends GluuRadiusException {

    private static final long serialVersionUID = -1L;

    public GenericLdapException(String msg) {
        super(msg);
    }

    public GenericLdapException(Throwable cause) {
        super(cause);
    }

    public GenericLdapException(String msg,Throwable cause) {
        super(msg,cause);
    }
}