package org.gluu.oxauth.client.supergluu.impl.crypto;


import org.gluu.oxauth.client.supergluu.impl.ICryptoProviderFactory;
import org.gluu.oxauth.model.crypto.AbstractCryptoProvider;
import org.gluu.oxauth.model.crypto.OxAuthCryptoProvider;


public class SingletonOxAuthCryptoProviderFactory implements ICryptoProviderFactory {

    private OxAuthCryptoProvider cryptoProvider;

    public SingletonOxAuthCryptoProviderFactory(String keyFile,String pin) throws Exception {
        
        cryptoProvider = new OxAuthCryptoProvider(keyFile,pin,null);
    }

    @Override
    public AbstractCryptoProvider newCryptoProvider()  {

        return cryptoProvider;
    }
}