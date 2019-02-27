package org.gluu.radius.ldap;

import com.unboundid.ldap.sdk.ResultCode;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.gluu.site.ldap.LDAPConnectionProvider;
import org.gluu.site.ldap.OperationsFacade;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.radius.exception.GenericLdapException;

public class LdapEntryManagerFactory {

    private static final Logger log = Logger.getLogger(LdapEntryManagerFactory.class);

    public static final LdapEntryManager createLdapEntryManager(Properties connProperties) {

        LDAPConnectionProvider connProvider = createConnectionProvider(connProperties);
        if(!ResultCode.SUCCESS.equals(connProvider.getCreationResultCode()))
            throw new GenericLdapException("LdapEntryManager creation failed. ResultCode "+connProvider.getCreationResultCode());
        //TODO check if the use of a bindConnectionProvider is mandatory
        //if it is , create a bindConnectionProvider and pass it as second parameter
        //to the operations face
        OperationsFacade opsFacade = new OperationsFacade(connProvider,null);
        
        return new LdapEntryManager(opsFacade);
    }


    private static final LDAPConnectionProvider createConnectionProvider(Properties connProperties) {

        return new LDAPConnectionProvider(connProperties);
    }

}