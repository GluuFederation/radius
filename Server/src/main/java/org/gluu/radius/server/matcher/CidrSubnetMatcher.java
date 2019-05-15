package org.gluu.radius.server.matcher;

import org.apache.log4j.Logger;
import org.gluu.radius.model.RadiusClient;
import org.gluu.radius.server.RadiusClientMatcher;
import org.gluu.radius.util.NetUtil;

public class CidrSubnetMatcher implements RadiusClientMatcher {

    private static final Logger log = Logger.getLogger(CidrSubnetMatcher.class);

    public CidrSubnetMatcher() {

    }

    @Override
    public boolean match(String clientipAddress,RadiusClient client) {

        if(NetUtil.isValidSubnetCidrNotiation(client.getIpAddress()) == false)
            return false;
        
        if(NetUtil.ipAddressBelongsToSubnet(clientipAddress,client.getIpAddress())) {
            log.debug("Match found for client with ip " + clientipAddress);
            return true;
        }
        return false;
    }
    
}