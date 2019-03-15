# Super Gluu Radius Resource Owner Password Credentials Script
# Copyright (c) 2019 Gluu Inc.

import java

class SuperGluuRadiusROPasswordCredentials(ResourceOwnerPasswordCredentialsType):
    def __init__(self,currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis
    
    def init(self, configurationAttributes):
        print "Super Gluu RO PW Credentials Script"
        return True
    
    def destroy(self, configurationAttributes):
        print "Super Gluu RO PW Credentials Script destroy"
        return True

    def authenticate(self, context):
        # TODO Implement this
        return True