package org.gluu.radius.server.matcher;

import org.apache.log4j.Logger;
import org.gluu.radius.model.RadiusClient;
import org.gluu.radius.server.RadiusClientMatcher;
import org.gluu.radius.util.NetUtil;

public class IpAddressMatcher implements RadiusClientMatcher {

    private static final Logger log = Logger.getLogger(IpAddressMatcher.class);

    public IpAddressMatcher() {

    }

    @Override
    public boolean match(String clientIpAddress, RadiusClient client) {

        if(NetUtil.isValidIpAddress(clientIpAddress) == false)
            return false;
        
        if(clientIpAddress.compareTo(client.getIpAddress()) == 0 ) {
            
            return true;
        }

        return false;
    }

}