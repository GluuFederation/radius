package org.gluu.oxauth.client.supergluu.impl;

import org.gluu.oxauth.model.crypto.AbstractCryptoProvider;

public interface ICryptoProviderFactory {
    public AbstractCryptoProvider newCryptoProvider();
}