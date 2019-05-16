package org.gluu.radius.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

public class NetUtil {

    private static final Logger log = Logger.getLogger(NetUtil.class);
    private static final Short[][] cidrSubnets = new Short[][] {
        new Short [] {0,0,0,0},

        new Short [] {128,0,0,0}, 
        new Short [] {192,0,0,0},
        new Short [] {224,0,0,0},
        new Short [] {240,0,0,0},
        new Short [] {248,0,0,0},
        new Short [] {252,0,0,0},
        new Short [] {254,0,0,0},
        new Short [] {255,0,0,0},

        new Short [] {255,128,0,0},
        new Short [] {255,192,0,0},
        new Short [] {255,224,0,0},
        new Short [] {255,240,0,0},
        new Short [] {255,248,0,0},
        new Short [] {255,252,0,0},
        new Short [] {255,254,0,0},
        new Short [] {255,255,0,0},

        new Short [] {255,255,128,0},
        new Short [] {255,255,192,0},
        new Short [] {255,255,224,0},
        new Short [] {255,255,240,0},
        new Short [] {255,255,248,0},
        new Short [] {255,255,252,0},
        new Short [] {255,255,254,0},
        new Short [] {255,255,255,0},

        new Short [] {255,255,255,128},
        new Short [] {255,255,255,192},
        new Short [] {255,255,255,224},
        new Short [] {255,255,255,240},
        new Short [] {255,255,255,248},
        new Short [] {255,255,255,252},
        new Short [] {255,255,255,254},
        new Short [] {255,255,255,255}
    };
    private static Pattern ipPattern;
    private static Pattern subnetPattern;

    private static class IpNotation {
        
        public Short firstByte;
        public Short secondByte;
        public Short thirdByte;
        public Short fourthByte;

        public IpNotation(Matcher ipmatcher) {

            try {
                this.firstByte = Short.parseShort(ipmatcher.group(1));
                this.secondByte = Short.parseShort(ipmatcher.group(2));
                this.thirdByte = Short.parseShort(ipmatcher.group(3));
                this.fourthByte = Short.parseShort(ipmatcher.group(4));
            }catch(NumberFormatException e) {

            }
        }

        public IpNotation(Short subnet) {

            if(subnet >=0 && subnet <= 32) {
                this.firstByte = cidrSubnets[subnet][0];
                this.secondByte = cidrSubnets[subnet][1];
                this.thirdByte = cidrSubnets[subnet][2];
                this.fourthByte = cidrSubnets[subnet][3];
            }
        }

        public final boolean isValid() {

            return this.firstByte != null && this.secondByte != null 
                && this.thirdByte != null && this.fourthByte != null;
        }
    }

    private static class SubnetNotation {
        public IpNotation network;
        public IpNotation subnet;

        public SubnetNotation(Matcher matcher) {
            try {
                network = new IpNotation(matcher);
                subnet = new IpNotation(Short.parseShort(matcher.group(5)));
            }catch(NumberFormatException e) {

            }
        }

        public final boolean isValid() {
            return network !=null && network.isValid()
                && subnet != null && subnet.isValid();
        }

        public final boolean addressInRange(IpNotation ip) {

            if(ip == null || !isValid())
                return false;
            
            return Short.compare((short)(ip.firstByte & subnet.firstByte),network.firstByte) == 0
                && Short.compare((short)(ip.secondByte & subnet.secondByte),network.secondByte) == 0
                && Short.compare((short)(ip.thirdByte & subnet.thirdByte),network.thirdByte) == 0
                && Short.compare((short)(ip.fourthByte & subnet.fourthByte),network.fourthByte) == 0;
        }
    }

    static {
        try {
            ipPattern = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$");
            subnetPattern = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})/([0-9]{2})$");
        }catch(PatternSyntaxException e) {
            log.debug("Invalid regex pattern syntax detected",e);
        }
    }

    public static final boolean isValidIpAddress(String ipAddress) {

        if(ipAddress == null) 
            return false;
        
        return ipPattern.matcher(ipAddress).matches();
    }

    public static final boolean isValidSubnetCidrNotiation(String subnetNotation) {

        if(subnetNotation == null)
            return false;
        
        return subnetPattern.matcher(subnetNotation).matches();
    }

    public static final boolean ipAddressBelongsToSubnet(String ipAddress, String subnetNotation) {

        if(ipAddress == null || subnetNotation == null)
            return false;
        
        try {
            Matcher ipmatcher = ipPattern.matcher(ipAddress);
            if(!ipmatcher.matches()) {
                log.debug(String.format("{%s} is not a valid ip address",ipAddress));
                return false;
            }
            Matcher subnetmatcher = subnetPattern.matcher(subnetNotation);
            if(!subnetmatcher.matches()) {
                log.debug(String.format("{%s} is not valid is not a valid CIDR subnet notation",subnetNotation));
                return false;
            }
            IpNotation ip = new IpNotation(ipmatcher);
            SubnetNotation subnet = new SubnetNotation(subnetmatcher);
            return subnet.addressInRange(ip);
        }catch(NumberFormatException e) {
            log.debug("Could not parse ip address or subnet",e);
            return false;
        }
    }
}