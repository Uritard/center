#!/bin/sh
cd `dirname $0`
SH_DIR=${PWD%/*}
java_home=env|grep "JAVA_HOME";
latestjar=`ls $SH_DIR/ -lt | grep '.jar'| awk '{print $9}' | sed -n '1p'`
stopjar=`echo $latestjar |cut -d"-" -f1,2,3`
echo 'stop:'$stopjar
process_exists=`ps -ef|grep -E ${stopjar}'\S+\.jar'|grep -v grep|awk '{print $2}'`
echo 'process:'$process_exists
if [ -n "${process_exists}" ];then
	kill -9 ${process_exists}
fi
echo "------application stop sucess---------"

echo 'stop:ffmpeg'
process_ffmpeg_exists=`ps -ef|grep 'ffmpeg'|grep -v grep|awk '{print $2}'`
if [ -n "${process_ffmpeg_exists}" ];then
	kill -9 ${process_ffmpeg_exists}
fi
echo "------ffmpeg stop sucess---------"
