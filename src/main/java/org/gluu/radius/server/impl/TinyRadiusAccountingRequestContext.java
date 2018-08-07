package org.gluu.radius.server.impl;

import java.net.InetSocketAddress;
import org.gluu.radius.server.AccountingStatusType;
import org.gluu.radius.server.AccountingRequestContext;
import org.gluu.radius.server.GluuRadiusServerException;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.util.RadiusException;

public class TinyRadiusAccountingRequestContext extends TinyRadiusRequestContext implements AccountingRequestContext {
	
	public TinyRadiusAccountingRequestContext(AccountingRequest request,InetSocketAddress client) {
		super(request,client);
	}

	@Override
	public String getUsername() {

		try {
			AccountingRequest req = (AccountingRequest) packet;
			return req.getUserName();
		}catch(RuntimeException re) {
			throw new GluuRadiusServerException("Could not get username on accounting request",re);
		}catch(RadiusException rae) {
			throw new GluuRadiusServerException("Could not get username on accounting request",rae);
		}
	}

	@Override
	public AccountingStatusType getAccountingStatusType() {

		try {

			AccountingRequest req = (AccountingRequest) packet;
			AccountingStatusType ret = AccountingStatusType.UNKNOWN_STATUS;

			switch(req.getAcctStatusType()) {
				default:
					break;
				case AccountingRequest.ACCT_STATUS_TYPE_ACCOUNTING_OFF:
					ret = AccountingStatusType.ACCOUNTING_OFF;
					break;
				case AccountingRequest.ACCT_STATUS_TYPE_ACCOUNTING_ON:
					ret = AccountingStatusType.ACCOUNTING_ON;
					break;
				case AccountingRequest.ACCT_STATUS_TYPE_INTERIM_UPDATE:
					ret = AccountingStatusType.INTERIM_UPDATE;
					break;
				case AccountingRequest.ACCT_STATUS_TYPE_START:
					ret = AccountingStatusType.START;
					break;
				case AccountingRequest.ACCT_STATUS_TYPE_STOP:
					ret = AccountingStatusType.STOP;
					break;
			}

			return ret;
		}catch(RadiusException rae) {
			throw new GluuRadiusServerException("Could not get accounting status type",rae);
		}catch(RuntimeException re) {
			throw new GluuRadiusServerException("Could not get accounting status type",re);
		}
	}
}