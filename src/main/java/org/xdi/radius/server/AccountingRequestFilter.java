package org.xdi.radius.server;

public interface AccountingRequestFilter {
	public Response processAccountingRequest(AccountingRequestContext context);
}