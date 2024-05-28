#!/bin/sh
/entrypoint.sh mysqld &

sleep 5s

cd `dirname $0`
SH_DIR=${PWD%/*}
java_home=/jdk-17.0.11/
latestjar=`ls $SH_DIR/ -lt | grep '.jar'| awk '{print $9}' | sed -n '1p'`
echo 'start:'$SH_DIR/$latestjar

JAVA_OPTS="-server -Xms768m -Xmx768m -XX:MetaspaceSize=64m"
cd $SH_DIR/;

if [ -n "$java_home" ]; then
  $java_home/bin/java $JAVA_OPTS -jar $SH_DIR/$latestjar
  echo " application is starting......";
    
  cd -;
  exit;
else
    java -version;
    if [ $? -eq 0 ]; then
      java $JAVA_OPTS -jar $SH_DIR/$latestjar
      echo " application is starting......";
    else    
      echo "this system has no jdk";
    fi
    cd -;
    exit;
fi
