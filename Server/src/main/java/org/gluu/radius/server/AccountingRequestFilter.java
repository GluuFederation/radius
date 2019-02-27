package org.gluu.radius.server;

public interface AccountingRequestFilter {
    public void processAccountingRequest(AccountingRequestContext arContext);
}