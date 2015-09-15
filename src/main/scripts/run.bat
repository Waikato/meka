@ECHO OFF

SET MEMORY=512m
SET MAIN=meka.gui.guichooser.GUIChooser

java -Xmx%MEMORY% -cp ".\lib\*" %MAIN% %1
