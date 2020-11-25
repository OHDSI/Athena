#!/bin/bash

if [ $# = 1 ]; then
  java -Dumls-apikey=$1 -jar cpt4.jar 4
else
  java -Dumls-user=$1 -Dumls-password=$2 -jar cpt4.jar 4
fi
