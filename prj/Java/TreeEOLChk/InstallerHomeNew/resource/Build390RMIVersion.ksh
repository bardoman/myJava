#!/bin/ksh
export JAVA_HOME=
export ENV=
export LIBPATH=
export JAVA_COMPILER=NONE
# If you installed the Build/390 client without the jre, Update the #JREPATH setting below
# to point to the correct version of the jre on your system
JREPATH="./jre"

$JREPATH/bin/java -mx60m -classpath $JREPATH/lib/rt.jar:./Build390.jar: Build390.MBClient version

