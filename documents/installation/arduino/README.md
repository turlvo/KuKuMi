# KuKuMi Arduino BLE Reporter 

It extends converage between KuKuMi Server and  Xiaomi BT Temperature/Humidity Sensor.

# Overview
## - Architecture
> <img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/Architecture1.png' width=800>

# Installation
> 1) Arduino IDE Install
>> Download and install the Arduino IDE
[Link](https://www.arduino.cc/en/Main/Software)

> 2) Install Arduino core for the ESP32
[Link](https://github.com/espressif/arduino-esp32)  

> 3) Change partition size
>> Because WiFi and BLE library‘s size are big, partition size must be increased.
>>> [ARDUINO_DIR]/hardware/espressif/esp32/boards.txt
>>> 
```
’?.upload.maximum_size’ : replace all 1310720 to 1672864
```

>>> [ARDUINO_DIR]/hardware/espressif/esp32/tools/partitions/default.csv

>>> Modify manually or overwrite by ‘KuKuMi/arduino-ble-brdge/default.csv’ file
>>> 
```
Name	Type	SubType	Offset	Size	Flags
nvs	data	nvs	0x9000	0x5000	
otadata	data	ota	0xe000	0x2000	
app0	app	ota_0	0x10000	0x190000	
app1	app	ota_1	0x200000	0x190000	
eeprom	data	0x99	0x390000	0x1000	
spiffs	data	spiffs	0x391000	0x6F000	
```

> 4) Install to Arduino
>> - Open Arduino IDE Application

>> - Select 'Tool -> Board -> 'ESP32 Dev Module'
>> <img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install_arduino0.jpg' width=600>

>> - Copy source code 'arduino-ble-bridge.ino' to Arduino IDE
[Link](https://github.com/turlvo/KuKuMi/blob/master/arduino-ble-bridge/arduino-ble-bridge.ino)

>> - Change 'WIFI_SSID' to connect to your AP's SSID

>> - Change 'WIFI_PASSWORD' to your AP's password

>> - Change 'POST_URL' to your KuKu Mi server's IP
>> <img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install_arduino1.jpg' width=600>

>> - Compile and Upload
>> <img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install_arduino2.jpg' width=600>


## - Packaging
* ESP32s board
* Container box
* Sponge
> <img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/package.png' width=800>
