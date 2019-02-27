package org.gluu.radius;

import java.util.Map;
import java.util.HashMap;

public class ServiceLocator {

    private static  ServiceLocator INSTANCE = null;

    private Map<String,Object> servicemap;

    private static final synchronized ServiceLocator getInstance() {

        if(INSTANCE == null)  {
            INSTANCE = new ServiceLocator();
        }

        return INSTANCE;
    }


    private ServiceLocator() {

        servicemap = new HashMap<String,Object>();
    }


    public static final synchronized void registerService(String serviceid,Object service) {

        getInstance().servicemap.put(serviceid,service);
    }

    public static final synchronized void registerService(KnownService knownservice,Object service) {

        getInstance().servicemap.put(knownservice.getServiceId(),service);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(String serviceid) {

        return (T) getInstance().servicemap.get(serviceid);
    } 

    @SuppressWarnings("unchecked")
    public static <T> T getService(KnownService knownservice) {

        return (T) getInstance().servicemap.get(knownservice.getServiceId());
    }
}