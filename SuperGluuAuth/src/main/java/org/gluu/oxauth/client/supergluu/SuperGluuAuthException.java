package org.gluu.oxauth.client.supergluu;

public class SuperGluuAuthException extends RuntimeException {

    private static final long serialVersionUID = -1L;
    
    public SuperGluuAuthException(String msg) {
        super(msg);
    }

    public SuperGluuAuthException(Throwable cause) {
        super(cause);
    }

    public SuperGluuAuthException(String msg ,Throwable cause) {
        super(msg,cause);
    }
}