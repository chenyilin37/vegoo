#!/bin/bash
set -e

#查看mysql服务的状态，方便调试，这条语句可以删除
echo `service mysql status`

echo '1.启动mysql....'

service mysql start

sleep 3

echo '2.开始创建....'
mysql < /mysql/schema.sql
echo '3.创建完毕....'

sleep 3
echo `service mysql status`

tail -f /dev/null