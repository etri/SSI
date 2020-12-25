#!/bin/sh
echo "Install icon-did-0.8.6.jar to local"
mvn install:install-file \
  -Dfile=/opt/driver-did-icon/lib/did/icon-did/0.8.6/icon-did-0.8.6.jar \
  -DgroupId=did \
  -DartifactId=icon-did \
  -Dversion=0.8.6 \
  -Dpackaging=jar \
  -DgeneratePom=true

echo "Install package"
cd /opt/driver-did-icon
mvn clean install package -N -DskipTests

echo "Run driver"
/opt/driver-did-icon/docker/run-driver-did-icon.sh