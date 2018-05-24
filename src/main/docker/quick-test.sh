#!/bin/bash

docker_name=zerocopyapp

docker_tag=10am/zerocopyapp

docker rm -f $docker_name

docker rmi $docker_tag

docker build -t $docker_tag .

docker run \
  --name $docker_name \
  --hostname $docker_name \
  -e MONITOR_INTERVAL=5 \
  -e GRAPHITE_IP=192.168.11.182 \
  -p 8080:8080 \
  $docker_tag