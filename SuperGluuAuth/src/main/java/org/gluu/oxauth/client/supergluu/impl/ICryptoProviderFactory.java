package org.gluu.oxauth.client.supergluu.impl;

import org.xdi.oxauth.model.crypto.AbstractCryptoProvider;

public interface ICryptoProviderFactory {
    public AbstractCryptoProvider newCryptoProvider();
}