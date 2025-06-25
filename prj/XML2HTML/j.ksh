#!/bin/sh

export jdompath="./jdom/jdom-2.0.6-contrib.jar:./jdom/jdom-2.0.6-junit.jar:./jdom/jdom-2.0.6.jar:./jdom/jdom-2.0.6-sources.jar:./jdom/jdom-2.0.6-javadoc.jar"

echo $jdompath

javac -classpath $jdompath XML2HTML.java
