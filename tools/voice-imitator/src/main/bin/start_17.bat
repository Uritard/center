@echo off

set JAR_DIR=%~dp0

set "JAVA_OPTS=-server -Xms1024m -Xmx1024m -XX:MetaspaceSize=128m"
rem set "JAVA_OPTS=%JAVA_OPTS% -Xlog:gc*,gc+ref=debug,gc+age=trace,gc+heap=debug:file=%JAR_DIR%logs\verbose%p%t.log:tags,uptime,time:filecount=5,filesize=200m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%JAR_DIR%heap_%p.dump"

rem set _JAVACMD=D:\Soft\Java\jdk1.8.0_101\bin\java.exe
set _JAVACMD=java
rem 切换控制台输出 UTF-8 格式，避免中文乱码
chcp 65001

%_JAVACMD% %JAVA_OPTS% -jar -Dfile.encoding=utf-8 %JAR_DIR%../voice-imitator-1.0.0-SNAPSHOT.jar
pause