package org.gluu.radius.server.provider;

import org.apache.log4j.Logger;
import org.gluu.radius.model.RadiusClient;
import org.gluu.radius.exception.GluuRadiusException;
import org.gluu.radius.server.SharedSecretProvider;
import org.gluu.radius.service.RadiusClientService;
import org.gluu.radius.util.EncDecUtil;

public class RadiusClientSharedSecretProvider implements SharedSecretProvider {

    private static final Logger log = Logger.getLogger(RadiusClientSharedSecretProvider.class);

    private RadiusClientService rcService;
    private String encodeSalt;

    public RadiusClientSharedSecretProvider(RadiusClientService rcService,String encodeSalt) {

        this.rcService = rcService;
        this.encodeSalt = encodeSalt;
    }

    @Override
    public String getSharedSecret(String clientIpAddress) {

        String sharedsecret = null;
        try {
            RadiusClient radiusclient = rcService.getRadiusClient(clientIpAddress);
            if(radiusclient != null) {
                sharedsecret = EncDecUtil.decode(radiusclient.getSecret(),encodeSalt);
            }
        }catch(GluuRadiusException e) {
            log.error("getSharedSecret() failed for client with ip address<"+clientIpAddress+">",e);
        }
        return sharedsecret;
    }
}