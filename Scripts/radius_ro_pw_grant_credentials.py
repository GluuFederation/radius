# Super Gluu Radius Resource Owner Password Credentials Script
# Copyright (c) 2019 Gluu Inc.

from java.util import Date , HashMap
from org.gluu.oxnotify.client import NotifyClientFactory

from org.xdi.model.custom.script.type.owner import ResourceOwnerPasswordCredentialsType
from org.xdi.oxauth.model.common import SessionIdState
from org.xdi.oxauth.model.config import ConfigurationFactory , Constants
from org.xdi.oxauth.security import Identity
from org.xdi.oxauth.service import EncryptionService , UserService , AuthenticationService , SessionIdService
from org.xdi.oxauth.service.push.sns import PushPlatform, PushSnsService
from org.xdi.service.cdi.util import CdiUtil
from org.xdi.util import StringHelper
from supergluu import PushNotificationManager, NetworkApi, GeolocationData, SuperGluuRequestBuilder

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

        self.issuerId = CdiUtil.bean(ConfigurationFactory).getAppConfiguration().getIssuer()
        if configurationAttributes.containsKey("issuer_id"):
            self.issuerId = configurationAttributes.get("issuer_id").getValue2()
        
        self.pushNotificationManager = PushNotificationManager(notificationServiceMode,credentialsFile)
        self.networkApi = NetworkApi()

        return True
    
    def destroy(self, configurationAttributes):

        print "Super-Gluu-Radius. Destroy"
        self.pushNotificationManager = None
        print "Super-Gluu-Radius. Destroyed Successfully"

        return True
    
    def getApiVersion(self):
        return 1
    
    def authenticate(self, context):
        if context.getUser() == None:
            print "Super-Gluu-Radius. Notice: no user specified for ro password grant"
        
        if self.perform_preliminary_user_authentication(context) == False:
            print "Super-Gluu-Radius. User authentication state not validated"
            return False
        
        acr_values = context.getHttpRequest().getParameter("acr_values")
        if self.acr_contains(acr_values,self.initialAuthAcr):
            return self.initial_authenticate(context)
        elif self.acr_contains(acr_values,self.finalAuthAcr):
            return self.final_authenticate(context)
        else:
            context.setUser(None)
            print "Super-Gluu-Radius. No acr_values specified in request"
            return False
    
    def initial_authenticate(self, context):
        print "Super-Gluu-Radius initial_auth"
        sessionId = self.new_unauthenticated_session(context.getUser())
        # set session id in identity object
        # this will be used by our dynamic scope script
        identity = CdiUtil.bean(Identity)
        identity.setSessionId(sessionId)
        self.send_push_notification_to_user(sessionId,context)
        print "Super-Gluu-Radius initial_auth complete"
        return True
    
    def final_authenticate(self, context):
        print "Super-Gluu-Radius final_auth"
        return True
    
    def perform_preliminary_user_authentication(self, context):
        username = context.getHttpRequest().getParameter("username")
        if self.authWithoutPassword:
            userService = CdiUtil.bean(UserService)
            user = userService.getUser(username,"uid")
            if user == None:
                print "Super-Gluu-Radius. User '%s' not found" % username
                return False
            context.setUser(user)
            print "Super-Gluu-Radius. User '%s' authenticated without password" % username
            return True
        
        password = context.getHttpRequest().getParameter("password_ro")
        authService = CdiUtil.bean(AuthenticationService)
        if authService.authenticate(username, password) == False:
            print "Super-Gluu-Radius. Could not authenticate user '%s' " % username
            return False
        
        context.setUser(authService.getAuthenticatedUser())
        return True
    
    
    def acr_contains(self,acr_list,acr_value):
        if StringHelper.isEmpty(acr_list):
            return False
        space_delim = "\t\n\r\f"
        tokens = StringHelper.split(acr_list,space_delim,True,False)
        for acr_token in tokens:
            if StringHelper.equalsIgnoreCase(acr_value,acr_token):
                return True
        
        return False
    
    def get_client_id(self,context):
        return context.getHttpRequest().getParameter("client_id")
    
    def new_unauthenticated_session(self,user):
        sessionIdService = CdiUtil.bean(SessionIdService)
        authDate = Date()
        sid_attrs = HashMap()
        sid_attrs.put(Constants.AUTHENTICATED_USER,user.getUserId())
        sessionId = sessionIdService.generateUnauthenticatedSessionId(user.getDn(),authDate,SessionIdState.UNAUTHENTICATED,sid_attrs,True)
        return sessionId
    
    def send_push_notification_to_user(self, sessionId,context):
        remote_ip = context.getHttpRequest().getParameter("user_remote_ip")
        if remote_ip == None or (remote_ip != None and StringHelper.isEmpty(remote_ip)):
            remote_ip = self.networkApi.get_remote_ip_from_request(context.getHttpRequest())
        
        user = context.getUser()
        srbuilder = SuperGluuRequestBuilder()
        srbuilder.username = user.getUserId()
        srbuilder.app = self.applicationId
        srbuilder.issuer = self.issuerId
        srbuilder.state = sessionId.getId()
        srbuilder.requestLocation(self.networkApi.get_geolocation_data(remote_ip))
        srbuilder.req_ip = remote_ip 
        self.pushNotificationManager.sendPushNotification(user,self.applicationId,srbuilder.build())
