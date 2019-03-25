
from com.google.android.gcm.server import Sender, Message
from com.notnoop.apns import APNS
from org.gluu.oxnotify.client import NotifyClientFactory
from org.xdi.oxauth.service import EncryptionService , UserService
from org.xdi.oxauth.service.fido.u2f import DeviceRegistrationService
from org.xdi.oxauth.service.push.sns import PushPlatform, PushSnsService
from org.xdi.service.cdi.util import CdiUtil
from org.xdi.util import StringHelper



import json
import sys

class PushNotificationContext:
    def __init__(self, appId, superGluuRequest):

        self.appId = appId
        self.superGluuRequest = superGluuRequest
        self.debugEnabled = False
        self.deviceRegistrationService = CdiUtil.bean(DeviceRegistrationService)
        self.pushSnsService = CdiUtil.bean(PushSnsService)
        self.user = None
        self.u2fDevice = None
        self.devicePlatform = None
        self.pushToken = None

class PushNotificationWrapper:
    def __init__(self, serviceMode, credentialsFile):
        self.pushSnsMode = None
        self.pushGluuMode = None
        self.pushNotificationsEnabled = False
        self.titleTemplate = "Super-Gluu"
        self.messageTemplate = "Super-Gluu login request to %s"
        creds = self.loadCredentials(credentialsFile)
        if creds == None:
            return None
        
        if StringHelper.equalsIgnoreCase(serviceMode,"sns"):
            self.initSnsPushNotifications(creds)
        elif StringHelper.equalsIgnoreCase(serviceMode,"gluu"):
            self.initGluuPushNotifications(creds)
        else:
            self.initNativePushNotifications(creds)
    
    def initSnsPushNotifications(self, creds):
        print "Super-Gluu PushNotificationWrapper. SNS push notifications init ..."
        self.pushSnsMode = True
        try:
            sns_creds = creds["sns"]
            android_creds = creds["android"]
            ios_creds = creds["ios"]["sns"]
        except:
            print "Super-Gluu PushNotificationWrapper. Invalid SNS credentials format"
            return None
        
        self.pushAndroidService = None
        self.pushAppleService = None
        if not (android_creds["enabled"] or ios_creds["enabled"]):
            print "Super-gluu PushNotificationWrapper. SNS disabled for all platforms"
            return None
        
        sns_access_key = sns_creds["access_key"]
        sns_secret_access_key = sns_creds["secret_access_key"]
        sns_region = sns_creds["region"]

        encryptionService = CdiUtil.bean(EncryptionService)

        try:
            sns_secret_access_key = encryptionService.decrypt(sns_secret_access_key)
        except:
            # Ignore exception. Password is not encrypted
            print "Super-Gluu PushNotificationWrapper. Assuming 'sns_access_key' is not encrypted"
        
        pushSnsService = CdiUtil.bean(PushSnsService)
        pushClient = pushSnsService.createSnsClient(sns_access_key,sns_secret_access_key,sns_region)
        
        if android_creds["enabled"]:
            self.pushAndroidService = pushClient
            self.pushAndroidPlatformArn = android_creds["platform_arn"]
            print "Super-Gluu PushNotificationWrapper. Created SNS android notification service"
        
        if ios_creds["enabled"]:
            self.pushAppleService = pushClient
            self.pushApplePlatformArn = ios_creds["platform_arn"]
            self.pushAppleServiceProduction = ios_creds["production"]
        

        self.pushNotificationsEnabled = self.pushAndroidService != None or self.pushAppleService != None
    
    
    def initGluuPushNotifications(self, creds):
        print "Super-Gluu PushNotificationWrapper. Gluu push notifications init ... "

        self.pushGluuMode = True

        try:
            gluu_conf = creds["gluu"]
            android_creds = creds["android"]["gluu"]
            ios_creds = creds["ios"]["gluu"]
        except:
            print "Super-Gluu PushNotificationWrapper. Invalid Gluu credentials format"
            return None
        
        self.pushAndroidService = None
        self.pushAppleService = None

        if not(android_creds["enabled"] or not ios_creds["enabled"]):
            print "Super-Gluu PushNotificationWrapper. Gluu disabled for all platforms"
            return None
        
        gluu_server_uri = gluu_conf["server_uri"]
        notifyClientFactory  = NotifyClientFactory.instance()
        metadataConfiguration = None
        try:
            metadataConfigurationService = notifyClientFactory.createMetadataConfigurationService(gluu_server_uri)
            metadataConfiguration = metadataConfigurationService.getMetadataConfiguration()
        except:
            exc_value = sys.exc_info()[1]
            print "Super-Gluu PushNotificationWrapper. Gluu push notifications init failed while loading metadata" , exc_value
            return None
        
        gluuClient = notifyClientFactory.createNotifyService(metadataConfiguration)
        encryptionService = CdiUtil.bean(EncryptionService)

        if android_creds["enabled"]:
            gluu_access_key = android_creds["access_key"]
            gluu_secret_access_key = android_creds["secret_access_key"]

            try:
                gluu_secret_access_key = encryptionService.decrypt(gluu_secret_access_key)
            except:
                # Ignore exception. Password is not encrypted
                print "Super-Gluu PushNotificationWrapper. Assuming 'gluu_secret_access_key' is not encrypted"
            
            self.pushAndroidService = gluuClient
            self.pushAndroidServiceAuth = notifyClientFactory.getAuthorization(gluu_access_key,gluu_secret_access_key)
            print "Super-Gluu PushNotificationWrapper. Created Gluu Android notification service"
        
        if ios_creds["enabled"]:
            gluu_access_key = ios_creds["access_key"]
            gluu_secret_access_key = ios_creds["secret_access_key"]

            try:
                gluu_secret_access_key = encryptionService.decrypt(gluu_secret_access_key)
            except:
                # Ignore exception. Password is not encrypted
                print "Super-Gluu PushNotificationWrapper. Assuming 'gluu_secret_access_key' is not encrypted"
            self.pushAppleService = gluuClient
            self.pushAppleServiceAuth = notifyClientFactory.getAuthorization(gluu_access_key,gluu_secret_access_key)
            print "Super-Gluu PushNotificationWrapper. Created Gluu iOS notification service"
        
        self.pushNotificationsEnabled = self.pushAndroidService != None or self.pushAppleService != None
    
    
    def initNativePushNotifications(self, creds):
        print "Super-Gluu PushNotificationWrapper. Native push notifications init ... "
        try:
            android_creds = creds["android"]["gcm"]
            ios_creds = creds["ios"]["apns"]
        except:
            print "Super-Gluu PushNotificationWrapper. Invalid credentials format"
            return None
        
        self.pushAndroidService = None
        self.pushAppleService = None

        if android_creds["enabled"]:
            self.pushAndroidService = Sender(android_creds["api_key"])
            print "Super-Gluu PushNotificationWrapper. Created native Android notification service"
        
        if ios_creds["enabled"]:
            p12_file_path = ios_creds["p12_file_path"]
            p12_password  = ios_creds["p12_password"]

            try:
                encryptionService = CdiUtil.bean(EncryptionService)
                p12_password = encryptionService.decrypt(p12_password)
            except:
                # Ignore exception. Password is not encrypted
                print "Super-Gluu PushNotificationWrapper. Assuming 'p12_password' is not encrypted"

            apnsServiceBuilder = APNS.newService().withCert(p12_file_path,p12_password)
            if ios_creds["production"]:
                self.pushAppleService = apnsServiceBuilder.withProductionDestination().build()
            else:
                self.pushAppleService = apnsServiceBuilder.withSandboxDestination().build()
            
            self.pushAppleServiceProduction = ios_creds["production"]
            print "Super-Gluu PushNotificationWrapper. Created native iOS notification service"
        
        self.pushNotificationsEnabled = self.pushAndroidService != None or self.pushAppleService != None

    
    def loadCredentials(self, credentialsFile):
        print "Super-Gluu PushNotificationWrapper. Loading credentials ... "
        f = open(credentialsFile,'r')
        try:
            creds = json.loads(f.read())
        except:
            exception_value = sys.exc_info()[1]
            print "Super-Gluu PushNotificationWrapper. Loading credentials failed.", exception_value 
            return None
        finally:
            f.close()
        
        return creds
    
    def sendPushNotification(self, user, app_id, super_gluu_request):
        try:
            self.sendPushNotificationImpl(user, app_id, super_gluu_request)
        except:
            exception_value = sys.exc_info()[1]
            print "Super-Gluu PushNotificationWrapper. Failed to send push notification :",exception_value
    
    def sendPushNotificationImpl(self, user, app_id, super_gluu_request):

        if not self.pushNotificationsEnabled:
            print "Super-Gluu PushNotificationWrapper. Push notifications are disabled"
            return None
        
        user_name = user.getUserId()
        print "Super-Gluu PushNotificationWrapper. Sending push notification to user '%s' devices" % user_name

        userService = CdiUtil.bean(UserService)
        deviceRegistrationService = CdiUtil.bean(DeviceRegistrationService)

        user_inum = userService.getUserInum(user_name)

        u2f_device_list = deviceRegistrationService.findUserDeviceRegistrations(user_inum, app_id, 
            "oxId","oxDeviceData","oxDeviceNotificationConf")
        
        send_ios = 0
        send_android = 0
        if u2f_device_list.size() > 0:
            for u2f_device in u2f_device_list:
                device_push_result = self.sendDevicePushNotification(user, app_id, u2f_device, super_gluu_request)
                send_ios += device_push_result["send_ios"]
                send_android += device_push_result["send_android"]
        
        msg = """ Super-Gluu PushNotificationWrapper. Send push notification. send_android: '%s', send_ios: '%s' """
        print msg % (send_android, send_ios)
                


        
                
    
    def sendDevicePushNotification(self, user, app_id, u2f_device, super_gluu_request):

        device_data = u2f_device.getDeviceData()
        if device_data == None:
            return {"send_android":0,"send_ios":0}
        
        platform = device_data.getPlatform()
        push_token = device_data.getPushToken()
        pushNotificationContext = PushNotificationContext(app_id,super_gluu_request)
        pushNotificationContext.debugEnabled = False
        pushNotificationContext.user = user
        pushNotificationContext.u2fDevice = u2f_device
        pushNotificationContext.devicePlatform = platform
        pushNotificationContext.pushToken = push_token
        send_ios = 0
        send_android = 0

        if StringHelper.equalsIgnoreCase(platform,"ios") and StringHelper.isNotEmpty(push_token):
            # Sending notification to iOS user's device
            if self.pushAppleService == None:
                print "Super-Gluu PushNotificationWrapper. Apple push notification service disabled"
            else:
                self.sendApplePushNotification(pushNotificationContext)
                send_ios = 1
        
        if StringHelper.equalsIgnoreCase(platform,"android") and StringHelper.isNotEmpty(push_token):
            # Sending notification to android user's device
            if self.pushAndroidService == None:
                print "Super-Gluu PushNotificationWrapper. Android push notification service disabled"
            else:
                self.sendAndroidPushNotification(pushNotificationContext)
                send_android = 1
            
        
        return {"send_android":send_android,"send_ios":send_ios}


                

    def sendApplePushNotification(self, pushNotificationContext):
       
        if self.pushSnsMode or self.pushGluuMode:
            if self.pushSnsMode:
                self.sendApplePushSnsNotification(pushNotificationContext)
            elif self.pushGluuMode:
                self.sendApplePushGluuNotification(pushNotificationContext)
        else:
            self.sendApplePushNativeNotification(pushNotificationContext)
    
    def sendAndroidPushNotification(self, pushNotificationContext):

        if self.pushSnsMode or self.pushGluuMode:
            if self.pushSnsMode:
                self.sendAndroidPushSnsNotification(pushNotificationContext)
            elif self.pushGluuMode:
                self.sendAndroidPushGluuNotification(pushNotificationContext)
        else:
            self.sendAndroidPushNativeNotification(pushNotificationContext)
    


    def sendApplePushSnsNotification(self, pushNotificationContext):

        debug = pushNotificationContext.debugEnabled
        
        targetEndpointArn = self.getTargetEndpointArn(pushNotificationContext)
        if targetEndpointArn == None:
            return None
        
        push_message = self.buildApplePushMessage(pushNotificationContext)
        apple_push_platform = PushPlatform.APNS
        if not self.pushAppleServiceProduction:
            apple_push_platform = PushPlatform.APNS_SANDBOX
        
        pushSnsService = pushNotificationContext.pushSnsService
        send_notification_result = pushSnsService.sendPushMessage(self.pushAppleService, apple_push_platform, targetEndpointArn, push_message, None)
        if debug:
            dbg_msg = """ Super-Gluu PushNotificatioNWrapper. Send iOS SNS push notification. 
                          message: '%s', send_notification_result: '%s'"""
            print dbg_msg % (push_message, send_notification_result)
    
    def sendAndroidPushSnsNotification(self, pushNotificationContext):

        targetEndpointArn = self.getTargetEndpointArn(pushNotificationContext)
        if targetEndpointArn == None:
            return None
        pushSnsService = pushNotificationContext.pushSnsService
        push_message = self.buildAndroidPushMessage(pushNotificationContext)
        send_notification_result = pushSnsService.sendPushMessage(self.pushAndroidService, PushPlatform.GCM, targetEndpointArn, push_message, None)
        if pushNotificationContext.debugEnabled:
            dbg_msg = """ Super-Gluu PushNotificatioNWrapper. Send Android SNS push notification.
                          message:'%s', send_notification_result: '%s'"""
            print dbg_msg % (push_message, send_notification_result)
        
    
    
    def sendApplePushGluuNotification(self, pushNotificationContext):
        
        targetEndpointArn = self.getTargetEndpointArn(pushNotificationContext)
        if targetEndpointArn == None:
            return None
        
        debug = pushNotificationContext.debugEnabled
        push_message = self.buildApplePushMessage(pushNotificationContext)
        send_notification_result = self.pushAppleService.sendNotification(self.pushAppleServiceAuth, targetEndpointArn, push_message)
        if debug:
            dbg_msg = """ Super-Gluu PushNotificatioNWrapper. Send iOS gluu push notification. 
                          message: '%s', send_notification_result: '%s'"""
            print dbg_msg % (push_message, send_notification_result)
    
    def sendAndroidPushGluuNotification(self, pushNotificationContext):
        
        targetEndpointArn = self.getTargetEndpointArn(pushNotificationContext)
        if targetEndpointArn == None:
            return None
        push_message = self.buildAndroidPushMessage(pushNotificationContext)
        send_notification_result = self.pushAndroidService.sendNotification(self.pushAndroidServiceAuth, targetEndpointArn, push_message)
        if pushNotificationContext.debugEnabled:
            dbg_msg = """ Super-Gluu PushNotificatioNWrapper. Send Android gluu push notification.
                          message: '%s', send_notification_result: '%s' """
            print dbg_msg % (push_message,send_notification_result)
        pass
    
    def sendApplePushNativeNotification(self, pushNotificationContext):

        title = self.titleTemplate
        message = self.messageTemplate % pushNotificationContext.appId
        push_token = pushNotificationContext.pushToken
        additional_fields = {"request": pushNotificationContext.superGluuRequest}
        debug = pushNotificationContext.debugEnabled
        msgBuilder = APNS.newPayload().alertBody(message).alertTitle(title).sound("default")
        msgBuilder.forNewsstand()
        msgBuilder.customFields(additional_fields)
        push_message = msgBuilder.build()
        send_notification_result = self.pushAppleService.push(push_token, push_message)
        if debug:
            dbg_msg = """ Super-Gluu PushNotificatioNWrapper. Send iOS native push notification. 
                          push_token:'%s', message: '%s', send_notification_result: '%s'"""
            print dbg_msg % (push_token, push_message, send_notification_result)
    

    def sendAndroidPushNativeNotification(self, pushNotificationContext):
        title = self.titleTemplate
        superGluuRequest = pushNotificationContext.superGluuRequest
        msgBuilder = Message.Builder().addData("message", superGluuRequest).addData("title",title).collapseKey("single").contentAvailable(True)
        push_message = msgBuilder.build()
        push_token = pushNotificationContext.pushToken
        send_notification_result = self.pushAndroidService.send(push_message, push_token, 3)
        if pushNotificationContext.debugEnabled:
            dbg_msg = """ Super-Gluu PushNotificatioNWrapper. Send iOS native push notification. 
                          push_token:'%s', message: '%s', send_notification_result: '%s'"""
            print dbg_msg % (push_token, push_message, send_notification_result)
        
            
    
    def buildApplePushMessage(self, pushNotificationContext):
        
        title = self.titleTemplate
        message = self.messageTemplate % pushNotificationContext.appId
        sns_push_request_dictionary = {
            "request": pushNotificationContext.superGluuRequest,
            "apns": {
                "badge": 0,
                "alert": {"body":message,"title":title},
                "category": "ACTIONABLE",
                "content-available": "1",
                "sound": "default"
            }
        }
        return json.dumps(sns_push_request_dictionary,separators=(',',':'))
    
    def buildAndroidPushMessage(self, pushNotificationContext):

        sns_push_request_dictionary = {
            "collapse_key": "single",
            "content_available": True,
            "time_to_live": 60,
            "data": {
                "message": pushNotificationContext.superGluuRequest,
                "title": self.titleTemplate
            }
        }
        return json.dumps(sns_push_request_dictionary,separators=(',',':'))
      
    def getTargetEndpointArn(self, pushNotificationContext):
        
        deviceRegistrationService = pushNotificationContext.deviceRegistrationService
        pushSnsService = pushNotificationContext.pushSnsService
        platform = pushNotificationContext.devicePlatform
        user = pushNotificationContext.user
        u2fDevice  = pushNotificationContext.u2fDevice
        targetEndpointArn = None

        # Return endpoint ARN if it is created already
        notificationConf = u2fDevice.getDeviceNotificationConf()
        if StringHelper.isNotEmpty(notificationConf):
            notificationConfJson = json.loads(notificationConf)
            targetEndpointArn = notificationConfJson['sns_endpoint_arn']
            if StringHelper.isNotEmpty(targetEndpointArn):
                print "Super-Gluu PushNotificationWrapper. Target endpoint ARN already created"
                return targetEndpointArn
        
        # Create endpoint ARN
        pushClient = None
        pushClientAuth = None
        platformApplicationArn = None
        if platform == PushPlatform.GCM:
            pushClient = self.pushAndroidService
            if self.pushSnsMode:
                platformApplicationArn = self.pushAndroidPlatformArn
            if self.pushGluuMode:
                pushClientAuth = self.pushAndroidServiceAuth
        elif platform == PushPlatform.APNS:
            pushClient = self.pushAppleService
            if self.pushSnsMode:
                platformApplicationArn = self.pushApplePlatformArn
            if self.pushGluuMode:
                pushClientAuth = self.pushAppleServiceAuth
        else:
            return None
        
        deviceData = u2fDevice.getDeviceData()
        pushToken  = deviceData.getPushToken()

        print "Super-Gluu PushNotificationWrapper. Attempting to create target endpoint ARN for user: %s" % user.getUserId()
        if self.pushSnsMode:
            targetEndpointArn = pushSnsService.createPlatformArn(pushClient,platformApplicationArn,pushToken,user)
        else:
            customUserData = pushSnsService.getCustomUserData(user)
            registerDeviceResponse = pushClient.registerDevice(pushClientAuth, pushToken, customUserData)
            if registerDeviceResponse != None and registerDeviceResponse.getStatusCode() == 200:
                targetEndpointArn = registerDeviceResponse.getEndpointArn()
        
        if StringHelper.isEmpty(targetEndpointArn):
            print "Super-Gluu PushNotificationWrapper. Failed to get endpoint ARN for user: '%s'" % user.getUserId()
            return None
        
        printmsg = "Super-Gluu PushNotificationWrapper. Create target endpoint ARN '%s' for user '%s'"
        print printmsg % (targetEndpointArn, user.getUserId())
        
        # Store created endpoint ARN in device entry
        userInum = user.getAttribute("inum")
        u2fDeviceUpdate = deviceRegistrationService.findUserDeviceRegistration(userInum, u2fDevice.getId())
        u2fDeviceUpdate.setDeviceNotificationConf('{"sns_endpoint_arn": "%s"}' % targetEndpointArn)
        deviceRegistrationService.updateDeviceRegistration(userInum,u2fDeviceUpdate)

        return targetEndpointArn

            

