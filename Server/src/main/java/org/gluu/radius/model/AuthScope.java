package org.gluu.radius.model;

import java.io.Serializable;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DN;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;

@DataEntry
@ObjectClass(value = "oxAuthCustomScope")
public class AuthScope implements Serializable {

    private static final long serialVersionUID = -1L;

    @DN
    private String dn;

    @AttributeName(name="oxId")
    private String id;

    @AttributeName(name="displayName")
    private String name;

    public AuthScope() {

    }

    public String getDn() {
        return this.dn;
    }

    public AuthScope setDn(String dn) {

        this.dn = dn;
        return this;
    }

    public String getId() {

        return this.id;
    }

    public AuthScope setId(String id) {

        this.id = id;
        return this;
    }

    public String getName() {

        return this.name;
    }

    public AuthScope setName(String name) {

        this.name = name;
        return this;
    }
}