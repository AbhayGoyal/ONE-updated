#! /bin/sh
java -Xmx262144M -cp target:lib/ECLA.jar:lib/DTNConsoleConnection.jar core.DTNSim $*
