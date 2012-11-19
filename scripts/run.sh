#!/bin/bash

MEMORY=512m
MAIN=meka.gui.explorer.Explorer

java -Xmx$MEMORY -cp "./meka-1.0.jar:./lib/*" $MAIN

