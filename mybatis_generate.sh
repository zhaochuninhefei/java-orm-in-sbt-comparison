#!/bin/bash

export JAVA_HOME=/usr/java/jdk-25.0.1+8
mvn -version

mvn mybatis-generator:generate -e