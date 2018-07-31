package org.gluu.radius.server;


public interface AccountingRequestContext extends RequestContext {
	public AccountingStatusType getAccountingStatusType();
}