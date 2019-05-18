# Gluu Radius Server 

This is the repository containing for the Gluu Radius Server. This is not to be used in a high load production environment. 
See [Gluu Radiator](https://github.com/GluuFederation/GluuRadiator.git) for such use case.

## 1. Manual Installation
- Clone the repository (`git clone https://github.com/GluuFederation/radius.git`).
- Run `mvn package` from the source directory. 
- Chroot into your gluu-server installation with `sudo service gluu-server-<version> login`.

