package org.gluu.oxauth.client.supergluu;

public enum SuperGluuAuthScheme {
    ONE_STEP("onestep"),
    TWO_STEP("twostep");

    private String name;

    private SuperGluuAuthScheme(String name) {
        this.name = name;
    }

    public String schemeName() {

        return this.name;
    }
}

