#!/bin/sh

export fxpath="/usr/share/openjfx/lib/javafx.base.jar:/usr/share/openjfx/lib/javafx.graphics.jar:/usr/share/openjfx/lib/javafx.swing.jar:/usr/share/openjfx/lib/javafx.controls.jar:/usr/share/openjfx/lib/javafx.media.jar:/usr/share/openjfx/lib/javafx.web.jar:/usr/share/openjfx/lib/javafx.fxml.jar:"

echo $fxpath

javac -classpath $fxpath HelloWorld.java
