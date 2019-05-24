## Ubuntu Manual Install Instructions

These are the installation instructions for gluu radius 
server on Ubuntu operating systems.
We will assume you have the following:
  - A working installation of Ubuntu (must be supported by Gluu)
  - An installation of Gluu 4.0. For now , only Gluu 4.0 and above are supported but backports are in the works.
  Also, this has been tested only with ldap installed.
  - An installation of Java 
  - An installation of maven
  - An installation of git

### I - Building and copying required files. 
 This section assumes you are not chrooted into your running gluu-server instance (`sudo service gluu-server-4.0 login.

 1. Open a terminal and from a directory of your choic(e.g. your user home directory), clone the source code for gluu radius , then compile it.
 ```
 $ git clone https://github.com/GluuFederation/radius.git
 $ cd radius
 $ git checkout -b version_1.1 origin/version_1.1
 $ mvn package
 ```
2. Without leaving the radius source code directory from your terminal , create required directories 
and copy all the necessary files to your gluu server installation.
```
$ sudo mkdir -p /opt/gluu-server-4.0/opt/gluu/radius/logs/
$ sudo mkdir -p /opt/gluu-server-4.0/opt/gluu/radius/libs/
$ sudo mkdir -p /opt/gluu-server-4.0/etc/gluu/conf/radius/
$ sudo cp install/conf/radius/gluu-radius.properties /opt/gluu-server-4.0/etc/gluu/conf/radius/
$ sudo cp install/conf/radius/gluu-radius-logging.xml /opt/gluu-server-4.0/etc/gluu/conf/radius/
$ sudo cp install/jwks/gluu-radius.jks /opt/gluu-server-4.0/etc/certs/
$ sudo cp install/ldif/98-radius.ldif /opt/gluu-server-4.0/opt/gluu/radius/
$ sudo cp install/svc/default/gluu-radius /opt/gluu-server-4.0/etc/default/
$ sudo cp install/svc/gluu-radius /opt/gluu-server-4.0/etc/init.d/
$ sudo cp Scripts/gluu_common.py /opt/gluu-server-4.0/opt/gluu/python/libs
$ sudo cp Server/target/super-gluu-radius-server.jar /opt/gluu-server-4.0/opt/gluu/radius/
$ sudo cp Server/target/lib/*.jar /opt/gluu-server-4.0/opt/gluu/radius/
```

### II - oxTrust Configuration (Web Interface) 
  In this section we will configure our running instance of Gluu Server via oxTrust. 

1. Login to your Gluu Server Instance

2. Go to Configuration > JSON Configuration > oxAuth Configuration and make the following changes. 
    - Set `legacyIdTokenClaims` to `true`
    - Set `openidScopeBackwardsCompatibility` to `true`

3. Go to Configuration > Manage Custom Scripts > Dynamic Scopes and add a new dynamic scope script. 
    - Name the script `gluu_ro_session`.
    - From the project source code , copy-paste the file contents of `Scripts/gluu_ro_session.py` into the script textarea.
    - Make sure the dynamic scope script is enabled.
    - Save your changes.

4. Go to Configuration > Manage Custom Scripts > Resource Owner Password Credentials and add a new script 
    - Name the script `super_gluu_ro`.
    - Add the following custom properties 
        - `credentials_file` with value `/etc/certs/super_gluu_creds.json`
        - `notification_service_mode` with value `gluu`
        - `application_id` with value `https://<your_server_url>/identity/authcode.htm`
    - From the project source code, copy-paste the file contents of `Scripts/super_gluu_ro.py` into the script textarea 
    - Save your changes 

5. Go to OpenID Connect > Scopes and add a new scope 
    - Set the scope display name to `gluu_ro_session`
    - Set the scope type to `Dynamic`
    - Click on `Add Dynamic Scope Script` and select `gluu_ro_session`
    - Save your changes 

6. Go to OpenID Connect > Clients and add a new client 
    - Set the client secret to a strong password.
    - Set the client name to something convenient.
    - Add `https://<your_server_url>/` as a redirect url (it won't be used).
    - Add the following scopes:
        - openid 
        - gluu_ro_session
    - Set `Application Type` to `Native`
    - Set the authentication method for the Token endpoint to `private_key_jwt`
    - Add the following grant types:
        - `password`
    - Under the `Advanced Settings Tab` check the `Include Claims In Id Token` box 
    - Under the `Encryption/Signing Settings`:
        - Select `RS512` for the `JWS alg Algorithm for Authentication method to Token Endpoint`
        - From the project source code , copy-past the file contents of `install/jwks/gluu-radius.jwks` 
          into the `JWKS` textarea.
    - Save your changes. 

### III - LDAP Configuration 
  In this section we perform some ldap configuration.
1. Open a terminal and login to your gluu server container. Run the following command 
```
ldapmodify -p 1636 -Z -X -D "cn=directory manager" -w "<your_ldap_password>" -f /opt/gluu/radius/98-radius.ldif
```
2. Launch an ldap client such as JXPlorer and login to your ldap instance. 
- Create a new entry under `ou=configuration,o=gluu` with RDN `ou=oxradius`.
  The entry will inherit from a couple objectClasses:
    - `top`
    - `oxRadiusServerConfiguration`
    - `organizationalUnit`
  Then
    - set `oxRadiusAccountingPort`  to `1813` 
    - set `oxRadiusAuthenticationPort` to `1812`
    - set `oxRadiusAcrValue` to `super_gluu_ro`
    - set `oxRadiusAuthenticationTimeout` to `35000` (35 seconds)
    - set `oxRadiusAuthScope` to `inum=F0C4,ou=scopes,o=gluu` (the DN of the openid scope)
    - add another attribute `oxRadiusAuthScope` and set it to the DN of our previous `gluu_ro_session` scope
    - set `oxRadiusListenInterface` to `0.0.0.0` 
    - set `oxRadiusOpenIdBaseUrl` to the url of your gluu server instance.
    - set `oxRadiusOpenidUsername` to the inum of the OpenID client we created above.
    - set `oxRadiusOpenidPassword` to the password of the OpenID client we created above (two pass encoded).

- Create a new entry under `o=gluu` with RDN `ou=radius_clients`.
  The entry will inherit from a couple objectClasses:
    - `top`
    - `organizationalUnit`
- This step is optional (for now) but is how radius clients are added manually. It will eventually
  be done through the UI. 
  Create an entry under `ou=radius_clients,o=gluu` with RDN `inum=<random_unique_number>`.
  The entry will inherit from a couple objectClasses:
    - `top`
    - `oxRadiusClient`
  You can set the client's ip address(`oxRadiusClientIpAddress`) , name (`oxRadiusClientName`),
  and secret(`oxRadiusClientSecret`). The secret is generated by running the following command 
  from the container.
  ```
  # /opt/gluu/bin/encode.py <plain_text_secret>
  ```

### IV - Gluu Server Container Configuration
    In this section , we will perform configurations from within the container itself.
1. Open a terminal and login to your gluu server container. Run the following command
```
# useradd -s /bin/false radius
# adduser radius gluu
# chown -R radius:gluu /opt/gluu/radius/
# chown -R root:gluu /etc/gluu/conf/radius/
# chown root:gluu /opt/gluu/python/libs/gluu_common.py
# chown root:gluu /etc/certs/gluu-radius.jks
# udpate-rc.d gluu-radius defaults
```

2. Start the radius service 
Simply run
```
# service gluu-radius start
```
Logs can be found under /opt/gluu/radius/logs 


### V - Testing  
 It will be assumed that at least a radius client was added and credentials for one account 
 with at least one super-gluu device enrolled are provided. 
 Let's suppose for this example that the user's credentials are daenerys/dracarys@.
 Download and install [RadLogin4](https://www.iea-software.com/products/radiusnt/radlogin4.cfm)
 It's user interface is available on http://localhost:8020/ 
 Add a Radius server (with your server's ip and port) and then test using the user's credentials.




