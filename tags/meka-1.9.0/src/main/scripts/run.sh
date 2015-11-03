#!/bin/bash

MEMORY=512m
MAIN=meka.gui.guichooser.GUIChooser

java -Xmx$MEMORY -cp "./lib/*" $MAIN $1

