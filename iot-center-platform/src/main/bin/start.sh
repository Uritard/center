#!/bin/sh
SH_DIR=$(cd `dirname $0`;pwd)
java_home=env|grep "JAVA_HOME";
latestjar=`ls $SH_DIR/.. -lt | grep '.jar'| awk '{print $9}' | sed -n '1p'`
echo 'start:'$latestjar
process_exists=`ps -ef|grep $latestjar|grep -v grep|awk '{print $2}'`
if [ -n "${process_exists}" ];then
	kill -9 ${process_exists}
fi

JAVA_OPTS="-server -Xms2048m -Xmx2048m -XX:MetaspaceSize=128m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:$SH_DIR/logs/verbose.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=200M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$SH_DIR/heap_%p_%t.dump"

if [ ! -n "$java_home" ]; then
        cd $SH_DIR/../;
        nohup java $JAVA_OPTS -jar $SH_DIR/../$latestjar > /dev/null 2>&1 &
	echo " application is starting......";
        cd -;
        exit;
else
	java -version;
        cd $SH_DIR/../;
	if [ $? -eq 0]; then
		nohup java $JAVA_OPTS -jar $SH_DIR/../$latestjar > /dev/null 2>&1 &
	else	
		echo "this system has no jdk";
                cd -;
		exit;
	fi
fi
