@ECHO OFF

SET MEMORY=512m
SET MAIN=meka.gui.explorer.Explorer

java -Xmx%MEMORY% -cp ".\meka*.jar;.\lib\*" %MAIN%
