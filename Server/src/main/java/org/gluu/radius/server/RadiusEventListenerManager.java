package org.gluu.radius.server;

import java.util.List;
import java.util.ArrayList;


public class RadiusEventListenerManager {

    private List<RadiusEventListener> listeners;

    public RadiusEventListenerManager() {

        this.listeners = new ArrayList<RadiusEventListener>();
    }

    public RadiusEventListenerManager addListener(RadiusEventListener listener) {

        if(listener!=null && !listeners.contains(listener))
            listeners.add(listener);
        
        return this;
    }

    public RadiusEventListenerManager removeListener(RadiusEventListener listener) {

        if(listener != null)
            listeners.remove(listener);
        return this;
    }


    public RadiusEventListenerManager accessRequestNotification(AccessRequestContext context) {

        for(RadiusEventListener listener: listeners) {
            listener.onAccessRequest(context);
        }
        return this;
    }

    public RadiusEventListenerManager accountingRequestNotification(AccountingRequestContext context) {

        for(RadiusEventListener listener : listeners) {
            listener.onAccountingRequest(context);
        }
        return this;
    }

    public RadiusEventListenerManager sharedSecretRequestNotification(SharedSecretRequestContext context) {

        for(RadiusEventListener listener : listeners) {
            listener.onSharedSecretRequest(context);
        }
        return this;
    }
}