package org.xdi.radius.server;


public interface AccountingRequestContext extends RequestContext {
	public AccountingStatusType getAccountingStatusType();
}