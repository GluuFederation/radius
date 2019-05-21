# Gluu Radius Server 

This is the repository containing for the Gluu Radius Server. This is not to be used in a high load production environment. 
See [Gluu Radiator](https://github.com/GluuFederation/GluuRadiator.git) for such use case.

## 1. Manual Installation Instructions (Ubuntu 16.04 LTS)
1. Clone the repository (`git clone https://github.com/GluuFederation/radius.git`) into a directory we'll call <gluu-radius-src>.
2. Run `mvn package` from the source directory. 
3. Chroot into your gluu-server installation with `sudo service gluu-server-<version> login`.
4. Create a new user `radius` and add him to the `gluu` group 
    ``` 
    useradd -s /bin/false radius
    adduser radius gluu
    ```
5. Create the directory `/opt/gluu/radius/` and change it's ownership to user `radius` and group `gluu`
    ``` 
    mkdir -p /opt/gluu/radius/
    chown -R radius:gluu /opt/gluu/radius
    ```
6. Log out from your gluu-server installation

7. Copy the jar files from `<gluu-radius-src>/Server/target` to `/opt/gluu-server-<version>/opt/gluu/radius`
    ```
        sudo cp -R <gluu-radius-src>/Server/target/*.jar /opt/gluu-server-<version>/opt/gluu/radius
    ```
8. Copy <gluu-radius-src>/install/initd/default/gluu-radius to /opt/gluu-server-<version>/etc/default/ 
    ```
    sudo cp  <gluu-radius-src>/install/initd/default/gluu-radius /opt/<gluu-server-version>/etc/default/
    ```
9. Copy <gluu-radius-src>/setup/initd/gluu-radius to /opt/gluu-server-<version>/etc/init.d/
    ```
    sudo cp <gluu-radius-src>/setup/initd/gluu-radius /opt/gluu-server-<version>/etc/init.d/gluu-radius

10. Create the directory `/opt/gluu-server-<version>/etc/gluu/conf/radius`
    ```
    sudo mkdir -p /opt/gluu-server-<version>/etc/gluu/conf/radius
    ```
11. Copy <gluu-radius-src>/setup/conf/gluu-radius.properties to /opt/gluu-server-<version>/etc/gluu/conf/radius
    ```
    sudo cp <gluu-radius-src>/setup/conf/gluu-radius.properties /opt/gluu-server-<version>/etc/gluu/conf/radius
    ```

12. Copy <gluu-radius-src>/setup/conf/gluu-radius-logging.properties to /opt/gluu-server-<version>/etc/gluu/conf/radius
    ```
    sudo cp <gluu-radius-src>/setup/conf/gluu-radius.properties /opt/gluu-server-<version>/etc/gluu/conf/radius
    ```


