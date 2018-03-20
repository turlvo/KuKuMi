from bluepy.btle import Scanner, DefaultDelegate, BTLEException
import binascii
import struct
import sys, time
from daemon import Daemon
import os.path
import requests, json


CONST_ADVERTISEMENT_FIELD = ("UUID", "Flag", "ID", "Index", "MAC", "DataType", "Length", "Temperature", "Humidity")
CONST_CONFIG_HUB_ADDRESS_FILE = '/tmp/xiaomibt-daemon.hubaddr'
CONST_CONFIG_IFACE_FILE = '/tmp/xiaomibt-daemon.iface'
CONST_CONFIG_THRESHOLD_FILE = '/tmp/xiaomibt-daemon.threshold'
CONST_CONFIG_SCAN_RESULT_FILE = '/tmp/xiaomibt-daemon.scanresult'

class Global(object):
    before_temperature = {}
    before_humidity = {}

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
            
            with open(CONST_CONFIG_SCAN_RESULT_FILE, 'w') as f:
                for device in self.found_devices:
                    localname = device.getValueText(9)
                    if not localname: continue

                    if localname.startswith("MJ_HT_V1"):
                        #print("Device address: {0}, RSSI: {1}\n".format(device.addr, device.rssi))
                        data = {'mac': device.addr, 'rssi': device.rssi}
                        #json.dump(data, f)
                        f.write("{0}\n".format(data))

            time.sleep(2)

    def stop(self, *args, **kwargs):
        print ("XiaomiBTDaemon stopped!!!")
        Daemon.stop(self, *args, **kwargs)

    def getScanResult(self):
        scan_result = []
        if (os.path.isfile(CONST_CONFIG_SCAN_RESULT_FILE)):
            for device in open(CONST_CONFIG_SCAN_RESULT_FILE, 'r', encoding='utf-8'):
                scan_result.append((device))
        print (scan_result)
            

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
        if isNewData:
            adv_data = dev.getValueText(0x16)
            if None != adv_data and len(adv_data) == 40:
                #print ("Received new data from, %s, %s" % (dev.addr, adv_data))

                ba_adv_data = bytearray.fromhex(adv_data)
                st_adv_data = struct.unpack('<HHHB6sHBHH', ba_adv_data)
                dic_adv_data = dict(zip(CONST_ADVERTISEMENT_FIELD, st_adv_data))

                if None != self.hub_address:
                    #temp_before = self.before_temperature.get(dev.addr, 0.0)
                   # humi_before = self.before_humidity.get(dev.addr, 0.0)
                    temp_before = Global.before_temperature.get(dev.addr, 0.0)
                    humi_before = Global.before_humidity.get(dev.addr, 0.0)
                    temp_current = float(dic_adv_data["Temperature"]) / 10
                    humi_current = float(dic_adv_data["Humidity"]) / 10
                    #dic_adv_data["MAC"] = dev.addr
                    print ("\n\nDevice: %s\nBefore>> Temp: %f, Humi: %f / After>> Temp: %f, Humi: %F" % (dev.addr, temp_before, humi_before, temp_current, humi_current))
                    print ("Diff>> Temp: %f, Humi: %f" % ( abs(temp_current - temp_before), abs(humi_current - humi_before)))
                    if abs(temp_current - temp_before) > self.threshold or abs(humi_current - humi_before) > self.threshold :
                        print("Report~~~to : %s" % (self.hub_address))
                        header = {'Content-Type': 'application/json; charset=utf-8'}
                        #data = {'mac': dic_adv_data["MAC"], 'temperature': dic_adv_data["Temperature"], 'humidity': dic_adv_data["Humidity"]}
                        data = {'device': 'xiaomibt', 'mac': dev.addr, 'temperature': temp_current, 'humidity':humi_current}
                        url = 'http://' + self.hub_address + ':39500/xiaomibt/event'
                        requests.post(url, headers=header, data=json.dumps(data))
                    #self.before_temperature[dev.addr] = temp_current
                    #self.before_humidity[dev.addr] = humi_current
                    Global.before_temperature[dev.addr] = temp_current
                    Global.before_humidity[dev.addr] = humi_current
                else:
                    print ("hub address is None. So pass to report!!!")
            

if __name__ == "__main__":
    #dev = XiaomiBT(1, 0.1)
    daemon = XiaomiBTDaemon('/tmp/xiaomibt-daemon.pid')
    if len(sys.argv) == 2:
        if 'start' == sys.argv[1]:
            daemon.start()
        elif 'stop' == sys.argv[1]:
            daemon.stop()
        elif 'restart' == sys.argv[1]:
            daemon.restart()
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

