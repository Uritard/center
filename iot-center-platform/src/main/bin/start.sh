#!/bin/sh
cd `dirname $0`
SH_DIR=${PWD%/*}
java_home=env|grep "JAVA_HOME";
latestjar=`ls $SH_DIR/ -lt | grep '.jar'| awk '{print $9}' | sed -n '1p'`
echo 'start:'$latestjar
process_exists=`ps -ef|grep $latestjar|grep -v grep|awk '{print $2}'`
if [ -n "${process_exists}" ];then
	kill -9 ${process_exists}
fi
process_ffmpeg_exists=`ps -ef|grep 'ffmpeg'|grep -v grep|awk '{print $2}'`
if [ -n "${process_ffmpeg_exists}" ];then
	kill -9 ${process_ffmpeg_exists}
fi

JAVA_OPTS="-server -Xms2048m -Xmx2048m -XX:MetaspaceSize=128m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:$SH_DIR/logs/verbose.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=200M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$SH_DIR/heap_%p_%t.dump"

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    cd $SH_DIR/;
    nohup "$JAVA_HOME/bin/java" $JAVA_OPTS -jar $SH_DIR/$latestjar > /dev/null 2>&1 &
    echo " application is starting by JAVA_HOME......";
    cd -;
else
    if command -v java &>/dev/null; then
        cd $SH_DIR/;
        nohup java $JAVA_OPTS -jar $SH_DIR/$latestjar > /dev/null 2>&1 &
        echo " application is starting by java command......";
        cd -;
    else
        echo "Neither JAVA_HOME nor java command is found in the system. Please ensure JDK is installed and properly configured.";
        exit 1
    fi
fi
exit 0