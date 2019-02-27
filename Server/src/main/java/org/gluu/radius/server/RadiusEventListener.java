package org.gluu.radius.server;


public interface RadiusEventListener {
    public void onAccessRequest(AccessRequestContext context);
    public void onAccountingRequest(AccountingRequestContext context);
    public void onSharedSecretRequest(SharedSecretRequestContext context); 
}