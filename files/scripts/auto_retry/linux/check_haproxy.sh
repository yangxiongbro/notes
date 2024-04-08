#!/bin/bash
if [ $(cat /opt/temp/haproxy/haproxy.pid | wc -l) -eq 0 ];then
    sudo service haproxy restart
fi
sleep 15
if [ $(cat /opt/temp/haproxy/haproxy.pid | wc -l) -eq 0 ];then
    sudo service keepalived stop
fi