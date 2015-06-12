#!/bin/bash

MEMORY=512m
MAIN=meka.gui.explorer.Explorer

java -Xmx$MEMORY -cp "./lib/*" $MAIN $1

