#!/bin/bash
set -e
export JAVA_HOME=/usr/java/jdk-25.0.1+8
mvn -version # 检查JDK版本是不是25
mvn test

