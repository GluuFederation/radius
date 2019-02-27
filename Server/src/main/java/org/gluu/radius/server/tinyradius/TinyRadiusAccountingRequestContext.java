package org.gluu.radius.server.tinyradius;

import java.net.InetSocketAddress;
import org.gluu.radius.exception.ServerException;
import org.gluu.radius.server.AccountingStatus;
import org.gluu.radius.server.AccountingRequestContext;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.util.RadiusException;

public class TinyRadiusAccountingRequestContext extends TinyRadiusRequestContext implements AccountingRequestContext {

    public TinyRadiusAccountingRequestContext(InetSocketAddress client,AccountingRequest request) {
        super(client,request);
    }

    @Override
    public String getUsername() {

        try {
            AccountingRequest req = (AccountingRequest) packet;
            return req.getUserName();
        }catch(RuntimeException e) {
            throw new ServerException("Error getting username from accounting request packet",e);
        }catch(RadiusException e) {
            throw new ServerException("Error getting username from accounting request packet",e);
        }
    }

    @Override
    public AccountingStatus getAccountingStatus() {

        try {
            AccountingRequest req = (AccountingRequest) packet;
            AccountingStatus status = AccountingStatus.Unknown;
            switch(req.getAcctStatusType()) {
                default:
                    break;
                case AccountingRequest.ACCT_STATUS_TYPE_ACCOUNTING_OFF:
                    status = AccountingStatus.AccountingOff;
                    break;
                case AccountingRequest.ACCT_STATUS_TYPE_ACCOUNTING_ON:
                    status = AccountingStatus.AccountingOn;
                    break;
                case AccountingRequest.ACCT_STATUS_TYPE_INTERIM_UPDATE:
                    status = AccountingStatus.InterimUpdate;
                    break;
                case AccountingRequest.ACCT_STATUS_TYPE_START:
                    status = AccountingStatus.Start;
                    break;
                case AccountingRequest.ACCT_STATUS_TYPE_STOP:
                    status = AccountingStatus.Stop;
                    break;
            }
            return status;
        }catch(RadiusException e) {
            throw new ServerException("Error getting accounting status",e);
        }catch(RuntimeException e) {
            throw new ServerException("Error getting accounting status",e);
        }
    }
}