#!/usr/bin/env bash

ARDUINOPATH="$HOME/data/Logiciels/arduino/"

if [[ -z "$INSTALLDIR" ]]; then
    INSTALLDIR="$HOME/Documents/Arduino"
fi
echo "ARDUINOPATH: $ARDUINOPATH"
echo "INSTALLDIR:  $INSTALLDIR"

pde_path=`find $ARDUINOPATH -name pde.jar`
core_path=`find $ARDUINOPATH -name arduino-core.jar`
lib_path=`find $ARDUINOPATH -name commons-codec-1.7.jar`

if [[ -z "$core_path" || -z "$pde_path" ]]; then
    echo "Some java libraries have not been built yet (did you run ant build?)"
    return 1
fi
echo "pde_path: $pde_path"
echo "core_path: $core_path"
echo "lib_path: $lib_path"

set -e

mkdir -p bin
javac -target 1.8 -cp "$pde_path:$core_path:$lib_path" \
      -d bin src/ESP8266OTA.java

pushd bin
mkdir -p $INSTALLDIR/tools
rm -rf $INSTALLDIR/tools/ESP8266OTA
#rm $INSTALLDIR/tools/ESP8266OTA/tool/esp8266OTA.jar
mkdir -p $INSTALLDIR/tools/ESP8266OTA/tool
zip -r $INSTALLDIR/tools/ESP8266OTA/tool/esp8266OTA.jar *
popd

dist=$PWD/dist
rev=$(git describe --tags)
mkdir -p $dist
pushd $INSTALLDIR/tools
zip -r $dist/ESP8266OTA-$rev.zip ESP8266OTA/
popd
