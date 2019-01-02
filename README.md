# arduino-espota-plugin
Arduino plugin for uploading files to ESP8266 via OTA, even if your device is not connected to same lan
<br>
Tested with the following Arduino IDE versions: 1.8.8

## Installation
- Make sure you use one of the supported versions of Arduino IDE and have ESP8266 core installed.
- Download the tool archive from [releases page](https://github.com/SmartBlug/arduino-espota-plugin/releases/latest).
- Unpack the tool into `tools` directory (the path will look like `<sketchbook directory>/tools/ESP8266OTA/tool/esp8266OTA.jar)`.
- Restart Arduino IDE. 

## Usage
- Open a sketch (or create a new one and save it).
- Create `ESPOTA.h` file.
- Make sure your sketch support ArduinoOTA.
- Compile your sketch and upload once normally with serial.
- Reset your board
- Check the IP address of your board and update ESPOTA.h accordingly.
- Select *Tools > ESP8266 Remote OTA Upload* menu item. This should start uploading the files into ESP8266.
  When done, your board will reboot with the new code. Might take a few minutes for large file system sizes.

## Screenshot

![Screenshot](screenshot.png)

