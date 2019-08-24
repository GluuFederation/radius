package org.gluu.radius.model;

import java.io.Serializable;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.DN;
import org.gluu.persist.annotation.ObjectClass;

@DataEntry
@ObjectClass(values={"oxAuthClient","top"})
public class OpenIdClient implements Serializable {

    private static final long serialVersionUID = -1L;

    @DN
    private String dn;

    @AttributeName(name="inum")
    private String inum;

    @AttributeName(name="oxAuthJwks")
    private String keyset;

    @AttributeName(name="oxAuthIdTokenSignedResponseAlg")
    private String idTokenSignedResponseAlgorithm;

    @AttributeName(name="oxAuthTokenEndpointAuthSigningAlg")
    private String tokenEndpointAuthSigningAlgorithm;

    @AttributeName(name="oxAuthTokenEndpointAuthMethod")
    private String tokenEndpointAuthMethod;

    public OpenIdClient() {

    }

    public String getDn() {

        return this.dn;
    }

    public void setDn(String dn) {

        this.dn = dn;
    }

    public String getInum() {

        return this.inum;
    }

    public void setInum(String inum) {

        this.inum = inum;
    }

    public String getKeyset() {

        return this.keyset;
    }

    public void setKeyset(String keyset) {

        this.keyset = keyset;
    }

    public String getIdTokenSignedResponseAlgorithm() {

        return this.idTokenSignedResponseAlgorithm;
    }

    public void setIdTokenSignedResponseAlgorithm(String idTokenSignedResponseAlgorithm) {

        this.idTokenSignedResponseAlgorithm = idTokenSignedResponseAlgorithm;
    }

    public String getTokenEndpointAuthSigningAlgorithm() {

        return this.tokenEndpointAuthSigningAlgorithm;
    }

    public void setTokenEndpointAuthSigningAlgorithm(String tokenEndpointAuthSigningAlgorithm) {

        this.tokenEndpointAuthSigningAlgorithm = tokenEndpointAuthSigningAlgorithm;
    }

    public void setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {

        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public String getTokenEndpointAuthMethod() {

        return this.tokenEndpointAuthMethod;
    }
}