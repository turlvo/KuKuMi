import threading
import queue as Queue
import time
import struct
import requests, json
import os.path

CONST_CONFIG_HUB_ADDRESS_FILE = '/tmp/xiaomibt-daemon.hubaddr'
CONST_CONFIG_THRESHOLD_FILE = '/tmp/xiaomibt-daemon.threshold'

CONST_ADV_TEMPERATURE_FIELD = (
"UUID", "Flag", "ID", "Index", "MAC", "MAC", "MAC", "MAC", "MAC", "MAC", "DataType", "Length", "Temperature")
CONST_ADV_HUMIDITY_FIELD = (
"UUID", "Flag", "ID", "Index", "MAC", "MAC", "MAC", "MAC", "MAC", "MAC", "DataType", "Length", "Humidity")
CONST_ADV_BATTERY_FIELD = (
"UUID", "Flag", "ID", "Index", "MAC", "MAC", "MAC", "MAC", "MAC", "MAC", "DataType", "Length", "Battery")
CONST_ADV_TEMPERATURE_AND_HUMIDITY_FIELD = (
"UUID", "Flag", "ID", "Index", "MAC", "MAC", "MAC", "MAC", "MAC", "MAC", "DataType", "Length", "Temperature",
"Humidity")

CONST_TEMPERATURE_EVENT = 4100
CONST_HUMIDITY_EVENT = 4102
CONST_BATTERY_EVENT = 4106
CONST_TEMPERATURE_AND_HUMIDITY_EVENT = 4109


class Global(object):
    before_temperature = {}
    before_humidity = {}


class Reporter(threading.Thread):
    def __init__(self, queue, threshold=0.1):
        threading.Thread.__init__(self)

        self.hub_address = None
        self.threshold = threshold
        self.before_temperature = {}
        self.before_humidity = {}

        self.data_queue = queue
        self.data_queue_runnable = True

    def putData(self, data):
        self.data_queue.put_nowait(data)

    def run(self):
        while self.data_queue_runnable:
            if (os.path.isfile(CONST_CONFIG_HUB_ADDRESS_FILE)):
                self.hub_address = open(CONST_CONFIG_HUB_ADDRESS_FILE, 'r').readline()
                print("read hub address : %s" % (self.hub_address))

            if (os.path.isfile(CONST_CONFIG_THRESHOLD_FILE)):
                self.threshold = float(open(CONST_CONFIG_THRESHOLD_FILE, 'r').readline())
                print("read threshold: %s" % (self.threshold))

            data = self.data_queue.get()
            # print ("Reporter>> data : %s" % (data))
            self.parse_data(data)

    def stop(self):
        self.data_queue_runnable = False

    def parse_data(self, adv_data):
        try:
            if None != self.hub_address:
                # print ("\n\nReceived new data from, %s" % (adv_data))
                len_data = len(adv_data)
                if len_data == 40:
                    ba_adv_data = bytearray.fromhex(adv_data)
                    st_adv_data = struct.unpack('<HHHB6BHBHH', ba_adv_data)
                    dic_adv_data = dict(zip(CONST_ADV_TEMPERATURE_AND_HUMIDITY_FIELD, st_adv_data))
                    dic_adv_data['MAC'] = "%02x:%02x:%02x:%02x:%02x:%02x" % (st_adv_data[4:10][::-1])
                    # print(dic_adv_data['MAC'])
                    # print(dic_adv_data)

                    temp_before = Global.before_temperature.get(dic_adv_data['MAC'], 0.0)
                    humi_before = Global.before_humidity.get(dic_adv_data['MAC'], 0.0)
                    temp_current = float(dic_adv_data["Temperature"]) / 10
                    humi_current = float(dic_adv_data["Humidity"]) / 10

                    print("Device: %s\nBefore>> Temp: %f, Humi: %f / After>> Temp: %f, Humi: %F" % (
                        dic_adv_data['MAC'], temp_before, humi_before, temp_current, humi_current))
                    # print ("Diff>> Temp: %f, Humi: %f" % ( abs(temp_current - temp_before), abs(humi_current - humi_before)))
                    if abs(temp_current - temp_before) >= self.threshold or abs(
                                    humi_current - humi_before) >= self.threshold:
                        self.report_value(CONST_TEMPERATURE_AND_HUMIDITY_EVENT, dic_adv_data)
                    Global.before_temperature[dic_adv_data['MAC']] = temp_current
                    Global.before_humidity[dic_adv_data['MAC']] = humi_current
                elif len_data == 36:
                    ba_adv_data = bytearray.fromhex(adv_data)
                    st_adv_data = struct.unpack('<HHHB6BHBH', ba_adv_data)
                    dic_adv_data = {}
                    before = 0.0
                    current = 0.0
                    if int(st_adv_data[5]) == CONST_TEMPERATURE_EVENT:
                        dic_adv_data = dict(zip(CONST_ADV_TEMPERATURE_FIELD, st_adv_data))
                        dic_adv_data['MAC'] = "%02x:%02x:%02x:%02x:%02x:%02x" % (st_adv_data[4:10][::-1])
                        before = Global.before_temperature.get(dic_adv_data['MAC'], 0.0)
                        current = float(dic_adv_data["Temperature"]) / 10
                        print(
                            "Device: %s\nBefore>> Temp: %f / After>> Temp: %f" % (dic_adv_data['MAC'], before, current))
                        # print ("Diff>> Temp: %f" % (abs(current - before)))
                        if abs(current - before) >= self.threshold:
                            self.report_value(CONST_TEMPERATURE_EVENT, dic_adv_data)
                        Global.before_temperature[dic_adv_data['MAC']] = current
                    elif int(st_adv_data[5]) == CONST_HUMIDITY_EVENT:
                        dic_adv_data = dict(zip(CONST_ADV_HUMIDITY_FIELD, st_adv_data))
                        dic_adv_data['MAC'] = "%02x:%02x:%02x:%02x:%02x:%02x" % (st_adv_data[4:10][::-1])
                        before = Global.before_humidity.get(dic_adv_data['MAC'], 0.0)
                        current = float(dic_adv_data["Humidity"]) / 10
                        print(
                            "Device: %s\nBefore>> Humi: %f / After>> Humi: %f" % (dic_adv_data['MAC'], before, current))
                        # print ("Diff>> Humi: %f" % (abs(current - before)))
                        if abs(current - before) >= self.threshold:
                            self.report_value(CONST_HUMIDITY_EVENT, dic_adv_data['MAC'])
                        Global.before_humidity[dic_adv_data['MAC']] = current
                elif len_data == 34:
                    ba_adv_data = bytearray.fromhex(adv_data)
                    st_adv_data = struct.unpack('<HHHB6BHBB', ba_adv_data)
                    dic_adv_data = dict(zip(CONST_ADV_BATTERY_FIELD, st_adv_data))
                    dic_adv_data['MAC'] = "%02x:%02x:%02x:%02x:%02x:%02x" % (st_adv_data[4:10][::-1])
                    self.report_value(CONST_BATTERY_EVENT, dic_adv_data)
        except IOError as error:
            print('Error in main %s' % str(error))

    def report_value(self, report_type, dic_data):
        header = {'Content-Type': 'application/json; charset=utf-8'}
        url = 'http://' + self.hub_address + ':39500/xiaomibt/event'

        if report_type == CONST_TEMPERATURE_EVENT:
            data = {'device': 'xiaomibt', 'report_type': 'temperatureChange', 'mac': dic_data['MAC'],
                    'temperature': (dic_data['Temperature'] / 10)}
        elif report_type == CONST_HUMIDITY_EVENT:
            data = {'device': 'xiaomibt', 'report_type': 'humidityChange', 'mac': dic_data['MAC'],
                    'humidity': (dic_data['Humidity'] / 10)}
        elif report_type == CONST_BATTERY_EVENT:
            data = {'device': 'xiaomibt', 'report_type': 'batteryChange', 'mac': dic_data['MAC'],
                    'battery': dic_data['Battery']}
        elif report_type == CONST_TEMPERATURE_AND_HUMIDITY_EVENT:
            data = {'device': 'xiaomibt', 'report_type': 'temperatureAndHumidityChange', 'mac': dic_data['MAC'],
                    'temperature': (dic_data['Temperature']/10),
                    'humidity': (dic_data['Humidity']/10)}
        print("report_value >> %s" % (data))
        try:
            requests.post(url, headers=header, data=json.dumps(data))
        except requests.exceptions.ConnectionErrori as error:
            print('Error in report_value %s' % str(error))
