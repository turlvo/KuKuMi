/*
   Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleScan.cpp
   Ported to Arduino ESP32 by Evandro Copercini
 */

#define WIFI_SSID ""       // "YOUR WIFI SSID"
#define WIFI_PASSWORD ""   // "YOUR WiFI AP PASSWORD"
#define MQTT_SERVER ""     // "YOUR_MQTT_BROKER_IP_ADDRESS"
#define MQTT_PORT 1883
#define SCAN_TIME  60 // seconds
#define SLEEP_TIME  0 // seconds

#include <Arduino.h>
#include <sstream>

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEAdvertisedDevice.h>

#include <WiFi.h>
#include <WiFiMulti.h>
#include <PubSubClient.h>

#include "soc/soc.h"
#include "soc/rtc_cntl_reg.h"
#include "esp_system.h"

#define SUPPORT_LCD
#ifdef SUPPORT_LCD
#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

// OLED Setting
#define OLED_RESET 4
Adafruit_SSD1306 display(OLED_RESET);

#if (SSD1306_LCDHEIGHT != 32)
#error("Height incorrect, please fix Adafruit_SSD1306.h!");
#endif // (SSD1306_LCDHEIGHT != 32)
#endif // SUPPORT_LCD

// WiFi & BT Instance
WiFiMulti wifiMulti;
WiFiClient espClient;
PubSubClient client(espClient);
BLEScan *pBLEScan;

const int loopTimeCtl = 0;
hw_timer_t *timer = NULL;

void IRAM_ATTR resetModule(){
    ets_printf("reboot\n");
    esp_restart_noos();
}

class MyAdvertisedDeviceCallbacks : public BLEAdvertisedDeviceCallbacks {
    void onResult(BLEAdvertisedDevice advertisedDevice)
    {
        if (advertisedDevice.haveName() && advertisedDevice.haveServiceData() && !advertisedDevice.getName().compare("MJ_HT_V1")) {
            std::string strServiceData = advertisedDevice.getServiceData();
            uint8_t cServiceData[100];
            char charServiceData[100];

            strServiceData.copy((char *)cServiceData, strServiceData.length(), 0);

            Serial.printf("\n\nAdvertised Device: %s\n", advertisedDevice.toString().c_str());

            for (int i=0;i<strServiceData.length();i++) {
                sprintf(&charServiceData[i*2], "%02x", cServiceData[i]);
            }

            std::stringstream ss;
            ss << "fe95" << charServiceData;
            
            Serial.print("Payload:");
            Serial.println(ss.str().c_str());

            char eventLog[256];
            unsigned long value, value2;
            char charValue[5] = {0,};
            switch (cServiceData[11]) {
                case 0x04:
                    sprintf(charValue, "%02X%02X", cServiceData[15], cServiceData[14]);
                    value = strtol(charValue, 0, 16);
                    Serial.printf("TEMPERATURE_EVENT: %s, %d\n", charValue, value);

#ifdef SUPPORT_LCD
                    sprintf(eventLog, "TEMPERATURE_EVENT:\n %.1f\n", (float)(value) / 10);                   
                    drawLog(eventLog);
#endif
                    break;
                case 0x06:
                    sprintf(charValue, "%02X%02X", cServiceData[15], cServiceData[14]);
                    value = strtol(charValue, 0, 16);                    
                    Serial.printf("HUMIDITY_EVENT: %s, %d\n", charValue, value);

#ifdef SUPPORT_LCD
                    sprintf(eventLog, "HUMIDITY_EVENT:\n %.1f\n", (float)(value) / 10);
                    drawLog(eventLog);
#endif
                    break;
                case 0x0A:
                    sprintf(charValue, "%02X", cServiceData[14]);
                    value = strtol(charValue, 0, 16);                    
                    Serial.printf("BATTERY_EVENT: %s, %d\n", charValue, value);

#ifdef SUPPORT_LCD
                    sprintf(eventLog, "BATTERY_EVENT:\n %.1f\n", (float)(value));
                    drawLog(eventLog);
#endif
                    break;
                case 0x0D:
                    sprintf(charValue, "%02X%02X", cServiceData[15], cServiceData[14]);
                    value = strtol(charValue, 0, 16);                    
                    Serial.printf("TEMPERATURE_EVENT: %s, %d\n", charValue, value);                    
                    sprintf(charValue, "%02X%02X", cServiceData[17], cServiceData[16]);
                    value2 = strtol(charValue, 0, 16);                    
                    Serial.printf("HUMIDITY_EVENT: %s, %d\n", charValue, value2);
                 
#ifdef SUPPORT_LCD
                    sprintf(eventLog, "TEMPERATURE_AND_HUMIDITY_EVENT:\n %.1f / %.1f\n", (float)(value) / 10, (float)(value2) / 10);
                    drawLog(eventLog);
#endif
                    break;
            }

            int send_retry = 5;
            client.setServer(MQTT_SERVER, MQTT_PORT);

            while (--send_retry >= 0) {
                if (client.connect("ESP32Client")) {
                    client.publish("esp/kukumi", ss.str().c_str());
                    Serial.println("Send success");
                    break;
                }

                if (send_retry == 0) {
                    resetModule();
                }

                Serial.println("Retry to send data...");
                delay(2000);
            }
        }
    }
};

void WiFiEvent(WiFiEvent_t event)
{
    Serial.printf("[WiFi-event] event: %d\n", event);

    switch(event) {
    case SYSTEM_EVENT_STA_GOT_IP:
        Serial.println("WiFi connected");
        Serial.println("IP address: ");
        Serial.println(WiFi.localIP());
        break;
    case SYSTEM_EVENT_SCAN_DONE:
        Serial.println("Scan Done");
        break;
    case SYSTEM_EVENT_STA_CONNECTED:
        Serial.println("STA_CONNECTED");
        
#ifdef SUPPORT_LCD
        drawLog("WiFi connected!");
#endif
        break;
    case SYSTEM_EVENT_STA_DISCONNECTED:
        Serial.println("WiFi lost connection");

#ifdef SUPPORT_LCD
        drawLog("WiFi lost connection");
#endif
        delay(1000);
        resetModule();
        break;
    }
}



void setup()
{
    WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0); //disable brownout detector

    Serial.begin(115200);
    Serial.println("KuKu Mi Bridge");

#ifdef SUPPORT_LCD
    display.begin(SSD1306_SWITCHCAPVCC, 0x3C);  // initialize with the I2C addr 0x3C (for the 128x32)
    // Clear the buffer.
    display.clearDisplay();
    display.setTextColor(WHITE);
#endif
 
    // BLE init and setting
    BLEDevice::init("");
    pBLEScan = BLEDevice::getScan(); //create new scan
    pBLEScan->setAdvertisedDeviceCallbacks(new MyAdvertisedDeviceCallbacks());
    pBLEScan->setActiveScan(true); //active scan uses more power, but get results faster
    pBLEScan->setInterval(0x50);
    pBLEScan->setWindow(0x30);
    
    // WiFi Setting and Connecting
    WiFi.onEvent(WiFiEvent);
    wifiMulti.addAP(WIFI_SSID, WIFI_PASSWORD);

    Serial.println();
    Serial.println();
#ifdef SUPPORT_LCD
    drawLog("WiFi connecting...");
#endif

    int wifi_retry = 10;
    while (--wifi_retry >= 0) {
        if (wifiMulti.run() == WL_CONNECTED) {
            break;
        }

        if (wifi_retry == 0) {
            resetModule();
        } 

        Serial.print(".");
        delay(3000);
    }

    // 30 minutes device reset scheduler
    timer = timerBegin(0, 80, true); //timer 0, div 80
    timerAttachInterrupt(timer, &resetModule, true);
    timerAlarmWrite(timer, 1800000000, false); //set time in us
    timerAlarmEnable(timer); //enable interrupt


}

#ifdef SUPPORT_LCD
void drawText(const char* text) {
  for (uint8_t i=0; i < strlen(text); i++) {
    display.write(text[i]);
  }    
}

void drawLog(const char* msg) {
  display.clearDisplay();
  display.setCursor(0,0);
  display.setTextSize(1);
  drawText(msg);

  display.display();
  delay(1);
}
#endif

void loop() {
    char printLog[256];
    Serial.printf("Start BLE scan for %d seconds...\n", SCAN_TIME);

#ifdef SUPPORT_LCD
    sprintf(printLog, "Start BLE scan for %d seconds...\n", SCAN_TIME);
    drawLog(printLog);
#endif
    
    BLEScanResults foundDevices = pBLEScan->start(SCAN_TIME);
    int count = foundDevices.getCount();
    printf("Found device count : %d\n", count);

#if SLEEP_TIME > 0
    esp_sleep_enable_timer_wakeup(SLEEP_TIME * 1000000); // translate second to micro second
    Serial.printf("Enter deep sleep for %d seconds...\n", (SLEEP_TIME));
    esp_deep_sleep_start();

#endif

    delay(2000);
}
