/*
   Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleScan.cpp
   Ported to Arduino ESP32 by Evandro Copercini
 */

#define WIFI_SSID "WIFI SSID"
#define WIFI_PASSWORD "WIFI PASSWORD"
//#define POST_URL "KuKu Mi's Xiaomi BT daemon server IP" // ex) http://192.168.1.137:39501
#define POST_URL "192.168.1.137"  // For TCP Socket
#define POST_PORT 39501           // For TCP Socket
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

#include <HTTPClient.h>

#include "soc/soc.h"
#include "soc/rtc_cntl_reg.h"

WiFiMulti wifiMulti;
BLEScan *pBLEScan;

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

            //  For TCP Socket
            WiFiClient client;

            if (client.connect(POST_URL, POST_PORT)) {
                client.print(ss.str().c_str());
                Serial.println("Send success");
            }

/*

            Serial.println("Payload:");
            Serial.println(ss.str().c_str());
            // HTTP POST BLE list
            HTTPClient http;

            // configure traged server and url
            http.begin(POST_URL);

            // start connection and send HTTP header
            int httpCode = http.POST(ss.str().c_str());

            // httpCode will be negative on error
            if (httpCode > 0)
            {
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
*/
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
        break;
    case SYSTEM_EVENT_STA_DISCONNECTED:
        Serial.println("WiFi lost connection");
        wifiMulti.addAP(WIFI_SSID, WIFI_PASSWORD);
        break;
    }
}
void setup()
{
    WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0); //disable brownout detector

    Serial.begin(115200);
    Serial.println("ESP32 BLE Scanner");

    WiFi.onEvent(WiFiEvent);
    wifiMulti.addAP(WIFI_SSID, WIFI_PASSWORD);

    BLEDevice::init("");
    pBLEScan = BLEDevice::getScan(); //create new scan
    pBLEScan->setAdvertisedDeviceCallbacks(new MyAdvertisedDeviceCallbacks());
    pBLEScan->setActiveScan(true); //active scan uses more power, but get results faster
    pBLEScan->setInterval(0x50);
    pBLEScan->setWindow(0x30);
}

void loop() {
    // wait for WiFi connection
    if ((wifiMulti.run() == WL_CONNECTED)) {
        Serial.println("WiFi Connected");


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
