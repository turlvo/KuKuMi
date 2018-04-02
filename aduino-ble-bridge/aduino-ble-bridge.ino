/*
   Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleScan.cpp
   Ported to Arduino ESP32 by Evandro Copercini
 */

#define WIFI_SSID "WIFI SSID"
#define WIFI_PASSWORD "WIFI PASSWORD"
#define POST_URL "KuKu Mi's Xiaomi BT daemon server IP" // ex) http://192.168.1.137:39501
#define SCAN_TIME  15 // seconds
#define SLEEP_TIME  3 // seconds

#include <Arduino.h>
#include <sstream>

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEAdvertisedDevice.h>

#include <WiFi.h>
#include <WiFiMulti.h>

#include <HTTPClient.h>

#include "soc/soc.h"
#include "soc/rtc_cntl_reg.h"

WiFiMulti wifiMulti;

class MyAdvertisedDeviceCallbacks : public BLEAdvertisedDeviceCallbacks {
    void onResult(BLEAdvertisedDevice advertisedDevice)
    {
        if (advertisedDevice.haveName() && advertisedDevice.haveServiceData() && !advertisedDevice.getName().compare("MJ_HT_V1")) {
            std::string strServiceData = advertisedDevice.getServiceData();
            uint8_t cServiceData[100];            
            char charServiceData[100];
            
            strServiceData.copy((char *)cServiceData, strServiceData.length(), 0);
            
            Serial.printf("Advertised Device: %s\n", advertisedDevice.toString().c_str());
            Serial.println("Payload:");
            for (int i=0;i<strServiceData.length();i++) {
                sprintf(&charServiceData[i*2], "%02x", cServiceData[i]);
            }
            Serial.println("\n");
            Serial.printf("%s\n", charServiceData);

            switch (cServiceData[11]) {
                case 0x04:
                    Serial.printf("TEMPERATURE_EVENT: %02X %02X\n", cServiceData[15], cServiceData[14]);
                    break;
                case 0x06:
                    Serial.printf("HUMIDITY_EVENT: %02X %02X\n", cServiceData[15], cServiceData[14]);
                    break;
                case 0x0A:
                    Serial.printf("BATTERY_EVENT: %02X\n", cServiceData[14]);
                    break;
                case 0x0D:
                    Serial.printf("TEMPERATURE_AND_HUMIDITY_EVENT: %02X %02X /  %02X %02X\n", cServiceData[15], cServiceData[14], cServiceData[17], cServiceData[16]);
                    break;              
            }
			
            std::stringstream ss;
            ss << "fe95" << charServiceData;
   
            // HTTP POST BLE list
            HTTPClient http;
        
            Serial.println("Payload:");
            Serial.println(ss.str().c_str());
            Serial.println("[HTTP] begin...");    
    
            // configure traged server and url
            http.begin(POST_URL);
    
            // start connection and send HTTP header
            int httpCode = http.POST(ss.str().c_str());
    
            // httpCode will be negative on error
            if (httpCode > 0)
            {
                // HTTP header has been send and Server response header has been handled
    
                Serial.printf("[HTTP] GET... code: %d\n", httpCode);
    
    
                // file found at server
                if (httpCode == HTTP_CODE_OK)
                {
                    Serial.println(http.getString());
                }
            }
            else
            {
                Serial.printf("[HTTP] GET... failed, error: %s\n", http.errorToString(httpCode).c_str());
            }
    
            http.end();
         
        }
    }
};

void setup()
{
    WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0); //disable brownout detector

    Serial.begin(115200);
    Serial.println("ESP32 BLE Scanner");

    wifiMulti.addAP(WIFI_SSID, WIFI_PASSWORD);

    BLEDevice::init("");
}

void loop() {
    // wait for WiFi connection
    if ((wifiMulti.run() == WL_CONNECTED)) {
        Serial.println("WiFi Connected");

        // put your main code here, to run repeatedly:
        BLEScan *pBLEScan = BLEDevice::getScan(); //create new scan
        pBLEScan->setAdvertisedDeviceCallbacks(new MyAdvertisedDeviceCallbacks());
        pBLEScan->setActiveScan(false); //active scan uses more power, but get results faster
        pBLEScan->setInterval(240);
        pBLEScan->setWindow(240);

        Serial.printf("Start BLE scan for %d seconds...\n", SCAN_TIME);
        BLEScanResults foundDevices = pBLEScan->start(SCAN_TIME);
        //int count = foundDevices.getCount();


#if SLEEP_TIME > 0
        esp_sleep_enable_timer_wakeup(SLEEP_TIME * 1000000); // translate second to micro second
        Serial.printf("Enter deep sleep for %d seconds...\n", (SLEEP_TIME));
        esp_deep_sleep_start();

#endif
  
    }
    // wait WiFi connected
    delay(1000);
}

