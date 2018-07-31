package org.gluu.radius.server;

public interface AccountingRequestFilter {
	public boolean processAccountingRequest(AccountingRequestContext context);
}