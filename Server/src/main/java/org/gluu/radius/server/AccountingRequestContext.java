package org.gluu.radius.server;

public interface AccountingRequestContext extends RequestContext {
    public String getUsername();
    public AccountingStatus getAccountingStatus();
}