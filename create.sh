#!/bin/#!/bin/sh
Directory=`pwd`
JAVA_HOME=${Directory}"/../java"
echo $JAVA_HOME
${JAVA_HOME}/Contents/Home/bin/java -jar ${Directory}/OnLine.jar create
