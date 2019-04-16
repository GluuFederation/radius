# Super Gluu Radius Dynamic Scope 
# Copyright (c) 2019 Gluu Inc.

from org.xdi.model.custom.script.type.scope import DynamicScopeType
from org.xdi.oxauth.security import Identity
from org.xdi.service.cdi.util import CdiUtil

import java

class DynamicScope(DynamicScopeType):
    def __init__(self, currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis

    def init(self,configurationAttributes):
        print "SuperGluu Radius dynamic scope init"
        self.sessionIdClaimName = "session_id"
        if configurationAttributes.containsKey("session_id_claim_name"):
            self.sessionIdClaimName = configurationAttributes.get("session_id_claim_name").getValue2()
        
        print "SuperGluu Radius dynamic scope init complete"
        return True
    
    def destroy(self, configurationAttributes):
        print "SuperGluu Radius dynamic scope destruction"
        print "SuperGluu Radius dynamic scope destruction complete"
        return True
    
    def update(self, dynamicScopeContext, configurationAttributes):
        # Todo implement this
        print "SuperGluu Radius dynamic scope update"
        updated = False
        identity = CdiUtil.bean(Identity)
        if (identity is not None) and (identity.getSessionId() is not None):
            session_id = identity.getSessionId().getId()
            jsonWebResponse  = dynamicScopeContext.getJsonWebResponse()
            claims = jsonWebResponse.getClaims()
            claims.setClaim(self.sessionIdClaimName,session_id)
            updated = True
            print "Updated With Identity Session ID"
             
        return updated
    
    def getApiVersion(self):
        return 1
