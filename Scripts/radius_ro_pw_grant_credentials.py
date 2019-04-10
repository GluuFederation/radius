# Super Gluu Radius Resource Owner Password Credentials Script
# Copyright (c) 2019 Gluu Inc.

from org.gluu.oxnotify.client import NotifyClientFactory
from org.xdi.model.custom.script.type.owner import ResourceOwnerPasswordCredentialsType
from org.xdi.oxauth.service import EncryptionService , UserService
from org.xdi.oxauth.service.push.sns import PushPlatform, PushSnsService
from org.xdi.service.cdi.util import CdiUtil
from org.xdi.util import StringHelper
from supergluu import PushNotificationManager, GeolocationApi, GeolocationData

import java
import json
import sys

class ResourceOwnerPasswordCredentials(ResourceOwnerPasswordCredentialsType):
    def __init__(self,currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis
        self.initialAuthAcr = "super_gluu_start"
        self.finalAuthAcr   = "super_gluu_finish"
    
    def init(self, configurationAttributes):

        print "Super-Gluu-Radius RO PW Init"
        print "Key size count : %d " % configurationAttributes.keySet().size()
        if not configurationAttributes.containsKey("application_id"):
            print "Super-Gluu-Radius RO PW Init Failed. application_id property is required"
            return False
        
        if not configurationAttributes.containsKey("credentials_file"):
            print "Super-Gluu-Radius RO PW Init Failed. credentials_file is required"
            return False
        
        notificationServiceMode = None
        if configurationAttributes.containsKey("notification_service_mode"):
            notificationServiceMode = configurationAttributes.get("notification_service_mode").getValue2()
        
        self.applicationId = configurationAttributes.get("application_id").getValue2()
        credentialsFile = configurationAttributes.get("credentials_file").getValue2()

        if configurationAttributes.containsKey("push_notification_title"):
            self.pushNotificationManager.titleTemplate = configurationAttributes.get("push_notification_title").getValue2()
        
        if configurationAttributes.containsKey("push_notification_message"):
            self.pushNotificationManager.messageTemplate = configurationAttributes.get("push_notification_message").getValue2()
        
        self.authWithoutPassword = False
        if configurationAttributes.containsKey("auth_without_password"):
            auth_without_password = configurationAttributes.get("auth_without_password").getValue2()
            if StringHelper.equalsIgnoreCase(auth_without_password,"yes"):
                self.authWithoutPassword = True
        
        if configurationAttributes.containsKey("initial_auth_acr"):
            self.initialAuthAcr = configurationAttributes.get("initial_auth_acr").getValue2()
        
        if configurationAttributes.containsKey("final_auth_acr"):
            self.finalAuthAcr = configurationAttributes.get("final_auth_acr").getValue2()
        
        self.pushNotificationManager = PushNotificationManager(notificationServiceMode,credentialsFile)


        return True
    
    def destroy(self, configurationAttributes):

        print "Super-Gluu-Radius. Destroy"
        self.pushNotificationManager = None
        print "Super-Gluu-Radius. Destroyed Successfully"

        return True
    
    def getApiVersion(self):
        return 1
    
    def authenticate(self, context):
        return True
    
    def initial_authenticate(self, context):
        print "Super-Gluu-Radius. Initial Auth"
        pass
    
    def final_authenticate(self, context):
        print "Super-Gluu-Radius. Final Auth"
        pass
    

