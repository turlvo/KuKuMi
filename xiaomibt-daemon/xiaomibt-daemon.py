from bluepy.btle import Scanner, DefaultDelegate, BTLEException
import binascii
import struct
import sys, time
from daemon import Daemon
import os.path
import requests, json


CONST_ADV_TEMPERATURE_FIELD = ("UUID", "Flag", "ID", "Index", "MAC", "DataType", "Length", "Temperature")
CONST_ADV_HUMIDITY_FIELD = ("UUID", "Flag", "ID", "Index", "MAC", "DataType", "Length", "Humidity")
CONST_ADV_BATTERY_FIELD = ("UUID", "Flag", "ID", "Index", "MAC", "DataType", "Length", "Battery")
CONST_ADV_TEMPERATURE_AND_HUMIDITY_FIELD = ("UUID", "Flag", "ID", "Index", "MAC", "DataType", "Length", "Temperature", "Humidity")
CONST_CONFIG_HUB_ADDRESS_FILE = '/tmp/xiaomibt-daemon.hubaddr'
CONST_CONFIG_IFACE_FILE = '/tmp/xiaomibt-daemon.iface'
CONST_CONFIG_THRESHOLD_FILE = '/tmp/xiaomibt-daemon.threshold'
CONST_CONFIG_SCAN_RESULT_FILE = '/tmp/xiaomibt-daemon.scanresult'
CONST_CONFIG_PID_FILE = '/tmp/xiaomibt-daemon.pid'

CONST_TEMPERATURE_EVENT = 4100
CONST_HUMIDITY_EVENT = 4102
CONST_BATTERY_EVENT = 4106
CONST_TEMPERATURE_AND_HUMIDITY_EVENT = 4109

class Global(object):
    before_temperature = {}
    before_humidity = {}
    scan_results = []

class XiaomiBTDaemon(Daemon):
    def __init__(self, *args, **kwargs):
        self.hub_address = None
        self.threshold = 0.5
        self.iface = 0
        self.found_devices = None

        Daemon.__init__(self, *args, **kwargs)


    def run(self):
        print ("XiaomiBTDaemon started~~~")
        while True:
            if (os.path.isfile(CONST_CONFIG_HUB_ADDRESS_FILE)):
                self.hub_address = open(CONST_CONFIG_HUB_ADDRESS_FILE, 'r').readline()
                print ("read hub address : %s" % (self.hub_address))

            if (os.path.isfile(CONST_CONFIG_IFACE_FILE)):
                self.iface = int(open(CONST_CONFIG_IFACE_FILE, 'r').readline())
                print ("read iface : %s" % (self.iface))
            
            if (os.path.isfile(CONST_CONFIG_THRESHOLD_FILE)):
                self.threshold = float(open(CONST_CONFIG_THRESHOLD_FILE, 'r').readline())
                print ("read threshold: %s" % (self.threshold))

            try:
                self.scanner = Scanner(1).withDelegate(ScanDelegate(self.hub_address, self.threshold))
                self.found_devices = self.scanner.scan(10.0)
            except BTLEException as error:
                print ('Error in main %s' % str(error))
            
            for device in self.found_devices:
                localname = device.getValueText(9)
                if not localname: continue

                if localname.startswith("MJ_HT_V1"):
                    #print("Device address: {0}, RSSI: {1}\n".format(device.addr, device.rssi))
                    #data = {'mac': device.addr, 'rssi': device.rssi}
                    if device.addr not in Global.scan_results:
                        Global.scan_results.append(device.addr)
                    #json.dump(data, f)
            with open(CONST_CONFIG_SCAN_RESULT_FILE, 'w') as f:
                for device in Global.scan_results:
                    print("Writing to file : %s" % device)
                    f.write("{}\n".format(device))

            time.sleep(2)

    def stop(self, *args, **kwargs):
        print ("XiaomiBTDaemon stopped!!!")
        Daemon.stop(self, *args, **kwargs)

    def getScanResult(self):
        scan_result = []
        if (os.path.isfile(CONST_CONFIG_SCAN_RESULT_FILE)):
            for device in open(CONST_CONFIG_SCAN_RESULT_FILE, 'r', encoding='utf-8'):
                scan_result.append(device.rstrip('\n'))
        print (scan_result)
    
    def getState(self):
        if (os.path.isfile(CONST_CONFIG_PID_FILE)):
            print("ON");
        else:
            print("OFF"); 

    def setHubAddress(self, address):
        print ("setHubAddress : %s" % (address))
        with open(CONST_CONFIG_HUB_ADDRESS_FILE, 'w') as f:
            f.write(address)

    def setIface(self, iface):
        print ("setIface : %s" % (iface))
        with open(CONST_CONFIG_IFACE_FILE, 'w') as f:
            f.write(iface)

    def setThreshold(self, threshold):
        print ("setThreshold : %s" % (threshold))
        with open(CONST_CONFIG_THRESHOLD_FILE, 'w') as f:
            f.write(threshold)

    def setAll(self, iface, address, threshold): 
        print ("setAll")
        self.setIface(iface)
        self.setHubAddress(address)
        self.setThreshold(threshold)
        


class ScanDelegate(DefaultDelegate):
    def __init__(self, hub_address, threshold):
        self.hub_address = hub_address
        self.threshold = threshold
        self.before_temperature = {}
        self.before_humidity = {}

        DefaultDelegate.__init__(self)

    def handleDiscovery(self, dev, isNewDev, isNewData):
        try:
            if isNewData and None != self.hub_address:
                adv_data = dev.getValueText(0x16)
                if None != adv_data and -1 != adv_data.find('95fe'):
                    #print ("\n\nReceived new data from, %s, %s" % (dev.addr, adv_data))
                    len_data = len(adv_data)
                    if len_data == 40:
                        ba_adv_data = bytearray.fromhex(adv_data)
                        st_adv_data = struct.unpack('<HHHB6sHBHH', ba_adv_data)
                        dic_adv_data = dict(zip(CONST_ADV_TEMPERATURE_AND_HUMIDITY_FIELD, st_adv_data))

                        temp_before = Global.before_temperature.get(dev.addr, 0.0)
                        humi_before = Global.before_humidity.get(dev.addr, 0.0)
                        temp_current = float(dic_adv_data["Temperature"]) / 10
                        humi_current = float(dic_adv_data["Humidity"]) / 10
                        #dic_adv_data["MAC"] = dev.addr
                        print ("Device: %s\nBefore>> Temp: %f, Humi: %f / After>> Temp: %f, Humi: %F" % (dev.addr, temp_before, humi_before, temp_current, humi_current))
                        #print ("Diff>> Temp: %f, Humi: %f" % ( abs(temp_current - temp_before), abs(humi_current - humi_before)))
                        if abs(temp_current - temp_before) >= self.threshold or abs(humi_current - humi_before) >= self.threshold :
                            self.report_value(CONST_TEMPERATURE_AND_HUMIDITY_EVENT, dev.addr, temp_current, humi_current)
                        Global.before_temperature[dev.addr] = temp_current
                        Global.before_humidity[dev.addr] = humi_current
                    elif len_data == 36:
                        ba_adv_data = bytearray.fromhex(adv_data)
                        st_adv_data = struct.unpack('<HHHB6sHBH', ba_adv_data)
                        dic_adv_data = {}
                        before = 0.0
                        current = 0.0
                        if int(st_adv_data[5]) == CONST_TEMPERATURE_EVENT:
                            dic_adv_data = dict(zip(CONST_ADV_TEMPERATURE_FIELD, st_adv_data))
                            before = Global.before_temperature.get(dev.addr, 0.0)
                            current = float(dic_adv_data["Temperature"]) / 10
                            print ("Device: %s\nBefore>> Temp: %f / After>> Temp: %f" % (dev.addr, before, current))
                            #print ("Diff>> Temp: %f" % (abs(current - before)))
                            if abs(current - before) >= self.threshold :
                                self.report_value(CONST_TEMPERATURE_EVENT, dev.addr, temp=current)
                            Global.before_temperature[dev.addr] = current
                        elif int(st_adv_data[5]) == CONST_HUMIDITY_EVENT:
                            dic_adv_data = dict(zip(CONST_ADV_HUMIDITY_FIELD, st_adv_data))
                            before = Global.before_humidity.get(dev.addr, 0.0)
                            current = float(dic_adv_data["Humidity"]) / 10
                            print ("Device: %s\nBefore>> Humi: %f / After>> Humi: %f" % (dev.addr, before, current))
                            #print ("Diff>> Humi: %f" % (abs(current - before)))
                            if abs(current - before) >= self.threshold :
                                self.report_value(CONST_HUMIDITY_EVENT, dev.addr, humi=current)
                            Global.before_humidity[dev.addr] = current
                    elif len_data == 34:
                        ba_adv_data = bytearray.fromhex(adv_data)
                        st_adv_data = struct.unpack('<HHHB6sHBB', ba_adv_data)
                        dic_adv_data = dict(zip(CONST_ADV_BATTERY_FIELD, st_adv_data))
                        self.report_value(CONST_BATTERY_EVENT, dev.addr, battery=dic_adv_data["Battery"])
        except IOError as error:
            print ('Error in main %s' % str(error))
   
    def report_value(self, report_type, mac, temp=0.0, humi=0.0, battery=0):
        header = {'Content-Type': 'application/json; charset=utf-8'}
        url = 'http://' + self.hub_address + ':39500/xiaomibt/event'

        if report_type == CONST_TEMPERATURE_EVENT:
            data = {'device': 'xiaomibt', 'report_type': 'temperatureChange', 'mac': mac, 'temperature': temp}
        elif report_type == CONST_HUMIDITY_EVENT:
            data = {'device': 'xiaomibt', 'report_type': 'humidityChange', 'mac': mac, 'humidity': humi}
        elif report_type == CONST_BATTERY_EVENT:
            data = {'device': 'xiaomibt', 'report_type': 'batteryChange', 'mac': mac, 'battery': battery}
        elif report_type == CONST_TEMPERATURE_AND_HUMIDITY_EVENT:
            data = {'device': 'xiaomibt', 'report_type': 'temperatureAndHumidityChange', 'mac': mac, 'temperature': temp, 'humidity': humi}
        print ("report_value >> %s" % (data))
        try:
            requests.post(url, headers=header, data=json.dumps(data))
        except requests.exceptions.ConnectionErrori as error:
            print ('Error in report_value %s' % str(error))

if __name__ == "__main__":
    #dev = XiaomiBT(1, 0.1)
    daemon = XiaomiBTDaemon(CONST_CONFIG_PID_FILE)
    if len(sys.argv) == 2:
        if 'start' == sys.argv[1]:
            daemon.start()
        elif 'stop' == sys.argv[1]:
            daemon.stop()
        elif 'restart' == sys.argv[1]:
            daemon.restart()
            print("XaomiBTDaemon Restart~~~")
        elif 'state' == sys.argv[1]:
            daemon.getState()
        elif 'scanresult' == sys.argv[1]:
            daemon.getScanResult()
        else:
            print ("Unknown command")
            sys.exit(2)
            sys.exit(0)
    elif len(sys.argv) == 3:
        if 'address' == sys.argv[1]:
            daemon.setHubAddress(sys.argv[2])
        elif 'iface' == sys.argv[1]:
            daemon.setIface(sys.argv[2])
        elif 'threshold' == sys.argv[1]:
            daemon.setThreshold(sys.argv[2])
        else:
            print ("Unknown command")
            sys.exit(2)
            sys.exit(0)
    elif len(sys.argv) == 5:
        if 'set' == sys.argv[1]:
            daemon.setAll(sys.argv[2], sys.argv[3], sys.argv[4])
        else:
            print ("Unknown command")
            sys.exit(2)
            sys.exit(0)
    else:
        print ("usage: %s start|stop|restart|address|iface|threshold|set|scanresult [value]" % sys.argv[0])
        print ("For example : %s address '192.168.1.3'" % sys.argv[0])
        print ("              %s iface 0" % sys.argv[0])
        print ("              %s threshold 0.3" % sys.argv[0])
        print ("              %s set 0 '192.168.1.3' 0.3" % sys.argv[0])
        sys.exit(2)

