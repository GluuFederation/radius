# Super Gluu Radius Resource Owner Password Credentials Script
# Copyright (c) 2019 Gluu Inc.

from org.gluu.oxnotify.client import NotifyClientFactory
from org.xdi.model.custom.script.type.owner import ResourceOwnerPasswordCredentialsType
from org.xdi.oxauth.service import EncryptionService
from org.xdi.oxauth.service.push.sns import PushPlatform, PushSnsService
from org.xdi.service.cdi.util import CdiUtil
from org.xdi.util import StringHelper

import java
import json

class ResourceOwnerPasswordCredentials(ResourceOwnerPasswordCredentialsType):
    def __init__(self,currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis
    
    def init(self, configurationAttributes):
        print "Super-Gluu-Radius RO PW Init"
        if not configurationAttributes.containsKey("app_id"):
            print "Super-Gluu-Radius RO PW Init Failed. app_id property is required"
            return False
        if not configurationAttributes.containsKey("credentials_file"):
            print "Super-Gluu-Radius RO PW Init Failed. credentials_file is required"
            return False
        
        self.appId = configurationAttributes.get("app_id").getValue2()
        credentialsFile = configurationAttributes.get("credentials_file").getValue2()
        notificationServiceMode = None
        if configurationAttributes.containsKey("notification_service_mode"):
            notificationServiceMode = configurationAttributes.get("notification_service_mode").getValue2()
        

        print "Super-Gluu-Radius RO PW Credentials Init Complete"
        return True
    
    def destroy(self, configurationAttributes):
        print "Super Gluu RO PW Credentials Script destroy"
        return True

    def authenticate(self, context):
        # TODO Implement this
        return True
    
    def initPushNotificationService(self, credentialsFile, notificationServiceMode):
        print "Super-Gluu-Radius. Init notification services(Native/SNS/Gluu)"
        self.pushSnsMode = False
        self.pushGluuMode = False
        if StringHelper.equalsIgnoreCase(notificationServiceMode,"sns"):
            return self.initSnsPushNotificationService(credentialsFile)
        elif StringHelper.equalsIgnoreCase(notificationServiceMode,"gluu"):
            return self.initGluuPushNotificationService(credentialsFile)
        else:
            return self.initNativePushNotificationService(credentialsFile)
    
    def initSnsPushNotificationService(self, credentialsFile):
        print "Super-Gluu-Radius. Init SNS notification service"
        self.pushSnsMode = True
        creds = self.loadPushNotificationCreds(credentialsFile)
        if creds == None:
            return False
        
        try:
            sns_creds = creds["sns"]
            android_creds = creds["android"]["sns"]
            ios_creds = creds["ios"]["sns"]
        except:
            print "Super-Gluu-Radius. Init SNS notification service failed. Invalid credentials file format"
            return False
        
        self.pushAndroidService = None
        self.pushAppleService = None
        if not (android_creds["enabled"] or ios_creds["enabled"]):
            print "Super-Gluu-Radius. Initialize SNS notification services. SNS disabled for all platforms"
        
        sns_access_key = sns_creds["access_key"]
        sns_secret_access_key = sns_creds["secret_access_key"]
        sns_region = sns_creds["region"]

        encryptionService = CdiUtil.bean(EncryptionService)

        try:
            sns_secret_access_key = encryptionService.decrypt(sns_secret_access_key)
        except:
            # Ignore exception. Password is not encrypted
            print "Super-Gluu-Radius. Initialize SNS notification services. Assuming 'secret_access_key' is not encrypted"
        
        pushSnsService  = CdiUtil.bean(PushSnsService)
        pushClient = pushSnsService.createSnsClient(sns_access_key, sns_secret_access_key, sns_region)

        if android_creds["enabled"]:
            self.pushAndroidService = pushClient
            self.pushAndroidPlatform = android_creds["platform_arn"]
            print "Super-Gluu-Radius. Init SNS notification services. Created Android notification service"
        
        if ios_creds["enabled"]:
            self.pushAppleService = pushClient
            self.pushApplePlatformArn = ios_creds["platform_arn"]
            self.pushAppleServiceProduction = ios_creds["production"]
            print "Super-Gluu-Radius. Init SNS notification services. Created iOS notification service"
        
        enabled = self.pushAndroidService != None or self.pushApplePlatformArn != None

        return enabled
    
    def initGluuPushNotificationService(self, credentialsFile):
        print "Super-Gluu-Radius. Init gluu notification services"

        self.pushGluuMode = True

        creds = self.loadPushNotificationCreds(credentialsFile)
        if creds == None:
            return False
        
        try:
            gluu_conf = creds["gluu"]
            android_creds = creds["android"]["gluu"]
            ios_creds = creds["ios"]["gluu"]
        except:
            print "Super-Gluu-Radius. Init gluu notification service. Invalid credentials file format"
            return False
        
        self.pushAndroidService = None
        self.pushAppleService = None
        if not (android_creds["enabled"] or ios_creds["enabled"]):
            print "Super-Gluu-Radius. Initialize Gluu notification service. Gluu disabled for all platforms"
            return False
        
        gluu_server_uri = gluu_conf["server_uri"]
        notifyClientFactory = NotifyClientFactory.instance()
        metaDataConfiguration = None
        try:
            metaDataConfiguration = notifyClientFactory.createMetaDataConfiguration(gluu_server_uri).getMetadataConfiguration()
        except:
            print "Super-Gluu-Radius. Init gluu notification service. Failed to load metadata. Exception: ",sys.exec_info()[1]
            return False
        
        gluuClient  = notifyClientFactory.createNotifyService(metaDataConfiguration)

    
    def loadPushNotificationCreds(self, credentialsFile):
        print "Super-Gluu-Radius. Loading push notification credentials"

        f = open(credentialsFile,'r')
        try:
            creds = json.loads(f.read())
        except:
            print "Super-Gluu-Radius. Loading push notification credentials failed from file :", credentialsFile
            return None
        finally:
            f.close()
        
        return creds