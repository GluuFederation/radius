package org.gluu.radius.openid.jwt;

import org.gluu.radius.KnownService;
import org.gluu.radius.ServiceLocator;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.util.EncDecUtil;
import org.xdi.oxauth.model.crypto.AbstractCryptoProvider;
import org.xdi.oxauth.model.crypto.OxAuthCryptoProvider;

public class CryptoProviderFactory {

    public static final AbstractCryptoProvider newCryptoProvider() throws Exception {

        BootstrapConfigService bcService = ServiceLocator.getService(KnownService.BootstrapConfig);
        String pin = EncDecUtil.decode(bcService.getKeyStorePin(),bcService.getEncodeSalt());
        return new OxAuthCryptoProvider(bcService.getKeyStoreFile(),pin,null);
    }
}