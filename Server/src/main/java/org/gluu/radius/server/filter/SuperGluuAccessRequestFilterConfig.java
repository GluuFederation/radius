package org.gluu.radius.server.filter;

import org.gluu.radius.model.ServerConfiguration;
import org.gluu.radius.service.BootstrapConfigService;
import org.gluu.radius.util.EncDecUtil;
import org.xdi.oxauth.model.crypto.signature.SignatureAlgorithm;

public class SuperGluuAccessRequestFilterConfig {
    
    //status check timeout is in milliseconds
    //so we put a default of 10 seconds 
    private static final Long DEFAULT_STATUS_CHECK_TIMEOUT = 10000L;
    private final BootstrapConfigService bcService;
    private ServerConfiguration serverConfig;
    private Long statusCheckTimeout;
    
    public SuperGluuAccessRequestFilterConfig(final BootstrapConfigService bcService ,ServerConfiguration serverConfig) {

        this.bcService = bcService;
        this.serverConfig = serverConfig;
        this.statusCheckTimeout = DEFAULT_STATUS_CHECK_TIMEOUT;
    }

    public String getOpenidUsername() {

        return serverConfig.getOpenidUsername();
    }

    public String getOpenidPassword() {

        return EncDecUtil.decode(serverConfig.getOpenidPassword(),bcService.getEncodeSalt());
    }


    public Long getStatusCheckTimeout() {

        return this.statusCheckTimeout;
    }

    public String getInitialAuthAcrValues() {

        return serverConfig.getInitialAuthAcrValues();
    }

    public String getInitialAuthScopes() {

        return serverConfig.getInitialAuthScopes();
    }

    public String getFinalAuthAcrValues() {

        return serverConfig.getFinalAuthAcrValues();
    }

    public String getFinalAuthScopes() {

        return serverConfig.getFinalAuthScopes();
    }

    public String getJwtAuthKeyId() {

        return bcService.getJwtAuthKeyId();
    }

    public SignatureAlgorithm getJwtAuthSignAlgo() {

        return bcService.getJwtAuthSignAlgo();
    }
}