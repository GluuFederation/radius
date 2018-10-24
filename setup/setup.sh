#!/bin/bash

#########################################################
# Gluu Radius Setup script
# Copyright (c) Gluu Inc.
#########################################################

# directory where all our setup files reside
SETUP_DIR=$(cd `dirname $0` && pwd)
SETUP_CONFIG_FILE="config"
GLUU_RADIUS_DEPS_DIR=$SETUP_DIR/deps
GLUU_RADIUS_SVC_SCRIPT_FILE=gluu-radius
GLUU_RADIUS_SVC_DIR=/etc/init.d
GLUU_RADIUS_SVC_CONFIG_FILE=gluu-radius
GLUU_RADIUS_SVC_CONFIG_DIR=/etc/default

# used to determine if debugging is enabled (print dbg messages)
SETUP_DBG()
{
	if [ -z "$GLUU_RADIUS_SETUP_DBG" ]; then
		return 1
	else
		return 0
	fi
}

print_setup_banner()
{
	echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
	echo "+  Gluu Radius Setup                                   +"
	echo "+  Copyright (c) Gluu Inc.                             +"
	echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
	echo " "
}

#
# create group under which the service 
# will run 
# param $1 : group name 
#

create_group()
{
	groupadd -r "$1" > /dev/null 2>&1
}

#
# rollback group creation 
# param $1: group name
rollback_create_group()
{
	groupdel "$1" > /dev/null 2>&1
}


#
# create user under which the service
# will run
# param $1: username
# param $2: user principal group 
# param $3 (optional) : additional user groups in comma separated values (g1,g2,)
#
create_user()
{
	if [ -z "$3" ]; then
		useradd -g "$2" -m -r "$1" > /dev/null 2>&1
	else
		useradd -g "$2" -G "$3" -m -r "$1"  > /dev/null 2>&1
	fi
}

# 
# rollback user creation 
# param $1 username
#
rollback_create_user()
{
	userdel -r "$1" > /dev/null 2>&1
}

#
# directory creation helper
# param $1 : directory path
# param $2 : directory permission flag (chmod)
create_directory()
{
	mkdir "$1" > /dev/null 2>&1
	local RET="$?"
	if [ "$RET" -eq 0 ]; then
		chmod "$2" "$1" > /dev/null 2>&1
		RET="$?"
	fi
	return "$RET"
}

#
# directory creation rollback
#
rollback_create_directory()
{
	rm -r "$1" > /dev/null 2>&1
}

#
# setup installation directory 
# this includes creating the necessary 
# directories and setting up permissions
# 
setup_install_directory()
{
   create_directory "$GLUU_RADIUS_INSTALL_DIR" "755"
   local RET="$?"
   if [ "$RET" -ne 0 ]; then
   	 return "$RET"
   fi

   create_directory "$GLUU_RADIUS_INSTALL_DIR/$GLUU_RADIUS_LIB_DIR" "755"
   RET="$?"
   if [ "$RET" -ne 0 ]; then
   	rollback_create_directory "$GLUU_RADIUS_INSTALL_DIR"
   	return "$RET"
   fi

   create_directory "$GLUU_RADIUS_LOG_DIR" "755"
   RET="$?"
   if [ "$RET" -ne 0 ]; then
   	rollback_create_directory "$GLUU_RADIUS_INSTALL_DIR"
   	return "$RET"
   fi

   chown "$GLUU_RADIUS_USER:$GLUU_RADIUS_GROUP" "$GLUU_RADIUS_INSTALL_DIR"
   chown "$GLUU_RADIUS_USER:$GLUU_RADIUS_GROUP" "$GLUU_RADIUS_INSTALL_DIR/$GLUU_RADIUS_LIB_DIR"
   chown "$GLUU_RADIUS_USER:$GLUU_RADIUS_GROUP" "$GLUU_RADIUS_LOG_DIR"
}


#
# rollback setup of install directory 
#
rollback_setup_install_directory()
{
	rollback_create_directory "$GLUU_RADIUS_INSTALL_DIR/$GLUU_RADIUS_LIB_DIR"
	rollback_create_directory "$GLUU_RADIUS_INSTALL_DIR"
	rollback_create_directory "$GLUU_RADIUS_LOG_DIR"
}

#
# copy file into specified directory
# param $1 file 
# param $2 directory
# param $3 file user
# param $4 file group 
# param $5 permission flags

copy_file()
{
   cp -f "$1" "$2" > /dev/null 2>&1
   local RET="$?"
   if [ "$RET" -ne 0 ]; then
   	 return "$RET"
   fi

   local DEST_FILE_PATH="$2/$(basename ${1})"
   chown "$3:$4" "$DEST_FILE_PATH" > /dev/null 2>&1
   RET="$?"
   if [ "$RET" -ne 0 ]; then
   	 return "$RET"
   fi

   chmod "$5" "$DEST_FILE_PATH"  > /dev/null 2>&1
   RET="$?"
   return "$RET"
}

# rollback file copy operation
rollback_copy_file()
{
	rm -r -f "$1" > /dev/null 2>&1
}

rollback_all()
{
	rollback_create_user "$GLUU_RADIUS_USER"
	rollback_create_group "$GLUU_RADIUS_GROUP"
	rollback_setup_install_directory
	rollback_copy_file "$GLUU_RADIUS_SVC_DIR/$GLUU_RADIUS_SVC_SCRIPT_FILE"
	rollback_copy_file "$GLUU_RADIUS_SVC_CONFIG_DIR/$GLUU_RADIUS_SVC_CONFIG_FILE"
	rollback_copy_file "$GLUU_RADIUS_CONFIG_DIR/$GLUU_RADIUS_CONFIG_FILE"
	rollback_copy_file "$GLUU_RADIUS_CONFIG_DIR/$GLUU_RADIUS_LOG_CONFIG_FILE"
}




if [ "$EUID" -ne 0 ]; then
	echo "Fatal. Please run as root"
	exit 1
fi

if [ ! -f "$SETUP_DIR/$SETUP_CONFIG_FILE" ]; then
	echo "Fatal. Config file $SETUP_CONFIG_FILE not found."
	exit 1
fi

source "$SETUP_CONFIG_FILE"

SETUP_DBG && print_setup_banner

#
# configuration file entry checks 
#

if [ -z "$GLUU_RADIUS_GROUP" ]; then
	SETUP_DBG && echo "Fatal. GLUU_RADIUS_GROUP not found in config file"
	exit 1
fi

if [ -z "$GLUU_RADIUS_USER" ]; then
	SETUP_DBG && echo "Fatal. GLUU_RADIUS_USER not found in config file"
	exit 1
fi


if [ -z "$GLUU_RADIUS_INSTALL_DIR" ]; then
	SETUP_DBG && echo "Fatal. GLUU_RADIUS_INSTALL_DIR not found in config file"
	exit 1
fi

if [ -z "$GLUU_RADIUS_JAR_FILE" ]; then
	SETUP_DBG && echo "Fatal. GLUU_RADIUS_JAR_FILE not found in config file"
	exit 1
fi

if [ ! -f "$SETUP_DIR/$GLUU_RADIUS_JAR_FILE" ]; then
	SETUP_DBG && echo "Fatal. Gluu radius jar file not found"
	exit 1
fi

if [ ! -d "$GLUU_RADIUS_DEPS_DIR" ]; then
	SETUP_DBG && echo "Fatal. Gluu radius dependencies missing"
	exit 1
fi

# setting defaults where applicable

if [ -z "$GLUU_RADIUS_LIB_DIR" ]; then
	GLUU_RADIUS_LIB_DIR="libs"
fi

# optional 
# uncomment this if we want to check if the depenndencies directory
# is empty
#if [ ! "$(ls -A $GLUU_RADIUS_DEPS)" ]; then
#	SETUP_DBG && echo "Fatal. Gluu radius depenndencies missing"
#	exit 1
#fi

# create group

SETUP_DBG && echo -n "Creating group '$GLUU_RADIUS_GROUP' "
create_group "$GLUU_RADIUS_GROUP"
RET="$?"
if [ "$RET" -eq 0 ]; then
	SETUP_DBG && echo "[ok]"
else
	SETUP_DBG && echo "[error"]
	SETUP_DBG && echo "Fatal. Operation failed with code $RET."
	exit 1
fi

# create user 
SETUP_DBG && echo -n "Creating user '$GLUU_RADIUS_USER' "
create_user "$GLUU_RADIUS_USER" "$GLUU_RADIUS_GROUP" "$GLUU_RADIUS_ADDITIONAL_GROUPS"
RET="$?"
if [ "$RET" -eq 0 ]; then
	SETUP_DBG && echo "[ok]"
else
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET."
	rollback_create_group "$GLUU_RADIUS_GROUP"
	exit 1
fi

# create directories and copy files

#
# setup install directory
#
SETUP_DBG && echo " "
SETUP_DBG && echo -n "Initializing installation directory "
setup_install_directory
RET="$?"
if [ "$RET" -eq 0 ]; then
	SETUP_DBG && echo "[ok]"
else
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET."
	rollback_create_user  "$GLUU_RADIUS_USER"
	rollback_create_group "$GLUU_RADIUS_GROUP"
	exit 1
fi

# copy application jar 
SETUP_DBG && echo -n "Copying $GLUU_RADIUS_JAR_FILE "
copy_file "$SETUP_DIR/$GLUU_RADIUS_JAR_FILE" "$GLUU_RADIUS_INSTALL_DIR" \
          "$GLUU_RADIUS_USER" "$GLUU_RADIUS_GROUP" "644"
RET="$?"
if [ "$RET" -eq 0 ]; then
	SETUP_DBG && echo "[ok]"
else
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_create_user "$GLUU_RADIUS_USER"
	rollback_create_group "$GLUU_RADIUS_GROUP"
	rollback_setup_install_directory
	exit 1
fi

# copy application dependencies 
for JAR_DEP in $GLUU_RADIUS_DEPS_DIR/*.jar ; do
	if [ ! -f "$JAR_DEP" ]; then
		continue
	fi
	SETUP_DBG && echo -n "Copying $(basename ${JAR_DEP}) "
	copy_file "$JAR_DEP" "$GLUU_RADIUS_INSTALL_DIR/$GLUU_RADIUS_LIB_DIR" \
	          "$GLUU_RADIUS_USER" "$GLUU_RADIUS_GROUP" "644"
	RET="$?"
	if [ "$RET" -eq 0 ]; then
		SETUP_DBG && echo "[ok]"
	else
		SETUP_DBG && echo "[error]"
		SETUP_DBG && echo "Fatal. Operation failed with code $RET"
		rollback_create_user "$GLUU_RADIUS_USER"
		rollback_create_group "$GLUU_RADIUS_GROUP"
		rollback_setup_install_directory
		exit 1
	fi
done

# configuring service 
SETUP_DBG && echo -n "Configuring service "


# copy service init.d script
copy_file "$SETUP_DIR/initd/$GLUU_RADIUS_SVC_SCRIPT_FILE" "$GLUU_RADIUS_SVC_DIR" "root" "root" "755"
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi

# copy init.d script config file
copy_file "$SETUP_DIR/initd/default/$GLUU_RADIUS_SVC_CONFIG_FILE" \
          "$GLUU_RADIUS_SVC_CONFIG_DIR" "root" "root" "644"
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi

# copy main application config file
copy_file "$SETUP_DIR/conf/$GLUU_RADIUS_CONFIG_FILE" \
          "$GLUU_RADIUS_CONFIG_DIR" "$GLUU_RADIUS_USER" "$GLUU_RADIUS_GROUP" "644"
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi

# copy logging configuration file
copy_file "$SETUP_DIR/$GLUU_RADIUS_LOG_CONFIG_FILE" \
          "$GLUU_RADIUS_CONFIG_DIR" "$GLUU_RADIUS_USER" "$GLUU_RADIUS_GROUP" "644"
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatail. Operation failed with code $RET"
	rollback_all
	exit 1
fi

# swap nameplaces in various files with actual values 

# service configuration first
sed -i "s|<user>|$GLUU_RADIUS_USER|" \
       "$GLUU_RADIUS_SVC_CONFIG_DIR/$GLUU_RADIUS_SVC_CONFIG_FILE" > /dev/null 2>&1
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi

sed -i "s|<group>|$GLUU_RADIUS_GROUP|"  \
       "$GLUU_RADIUS_SVC_CONFIG_DIR/$GLUU_RADIUS_SVC_CONFIG_FILE" > /dev/null 2>&1
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi

sed -i "s|<install_dir>|$GLUU_RADIUS_INSTALL_DIR|" \
       "$GLUU_RADIUS_SVC_CONFIG_DIR/$GLUU_RADIUS_SVC_CONFIG_FILE" > /dev/null 2>&1
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi

sed -i "s|<lib_dir>|$GLUU_RADIUS_LIB_DIR|" \
       "$GLUU_RADIUS_SVC_CONFIG_DIR/$GLUU_RADIUS_SVC_CONFIG_FILE" > /dev/null 2>&1
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi

sed -i "s|<config_dir>|$GLUU_RADIUS_CONFIG_DIR|" \
       "$GLUU_RADIUS_SVC_CONFIG_DIR/$GLUU_RADIUS_SVC_CONFIG_FILE" > /dev/null 2>&1
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi

sed -i "s|<gluu_radius_jar>|$GLUU_RADIUS_JAR_FILE|" \
       "$GLUU_RADIUS_SVC_CONFIG_DIR/$GLUU_RADIUS_SVC_CONFIG_FILE" > /dev/null 2>&1
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi


# logging config file last 
sed -i "s|<log_dir>|$GLUU_RADIUS_LOG_DIR|" \
       "$GLUU_RADIUS_CONFIG_DIR/$GLUU_RADIUS_LOG_CONFIG_FILE" > /dev/null 2>&1
RET="$?"
if [ "$?" -ne 0 ]; then
	SETUP_DBG && echo "[error]"
	SETUP_DBG && echo "Fatal. Operation failed with code $RET"
	rollback_all
	exit 1
fi

SETUP_DBG && echo "[ok]"