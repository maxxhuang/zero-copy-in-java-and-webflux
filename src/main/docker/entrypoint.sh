#!/bin/bash

set -e 

source /set_variable.sh
source /echo_variable.sh
source /set_graphite_conf.sh

# start collectd as root
if [ -d /mnt/proc ]; then
  umount /proc
  mount -o bind /mnt/proc /proc
fi

echo $app_name > /appname

/usr/sbin/collectd -C /etc/collectd/collectd.conf

java -jar zerocopy.jar $@

#if [ -d /mnt/proc ]; then
#  umount /proc
#  mount -o bind /mnt/proc /proc
#fi

#if [ -z "$@" ]; then
#  exec /usr/sbin/collectd -C /etc/collectd/collectd.conf
#else
#  exec $@
#fi



