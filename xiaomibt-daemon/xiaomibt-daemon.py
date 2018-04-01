from bluepy.btle import Scanner, DefaultDelegate, BTLEException


import sys, time
import queue as Queue
from daemon import Daemon
import os.path

from reporter import Reporter
from server import Server

CONST_CONFIG_IFACE_FILE = '/tmp/xiaomibt-daemon.iface'
CONST_CONFIG_SCAN_RESULT_FILE = '/tmp/xiaomibt-daemon.scanresult'
CONST_CONFIG_PID_FILE = '/tmp/xiaomibt-daemon.pid'
CONST_CONFIG_HUB_ADDRESS_FILE = '/tmp/xiaomibt-daemon.hubaddr'
CONST_CONFIG_THRESHOLD_FILE = '/tmp/xiaomibt-daemon.threshold'

class Global(object):
    scan_results = []

class XiaomiBTDaemon(Daemon):
    def __init__(self, *args, **kwargs):
        self.iface = 0
		
        self.found_devices = None		
        self.before_temperature = {}
        self.before_humidity = {}

        self.reporting_queue = Queue.Queue()
        self.data_reporter = Reporter(self.reporting_queue)
        self.data_reporter.setDaemon(True)

        self.bridge_server = Server(self.data_reporter, 39501)
        self.bridge_server.setDaemon(True)

        Daemon.__init__(self, *args, **kwargs)


    def run(self):
        print ("XiaomiBTDaemon started~~~")
        self.data_reporter.start()
        self.bridge_server.start()

        while True:
            if (os.path.isfile(CONST_CONFIG_PID_FILE) == False):
                self.data_reporter.stop()
                self.bridge_server.stop()
                print("release server, reporter")

            if (os.path.isfile(CONST_CONFIG_IFACE_FILE)):
                self.iface = int(open(CONST_CONFIG_IFACE_FILE, 'r').readline())
                print("read iface : %s" % (self.iface))

            try:
                self.scanner = Scanner(1).withDelegate(ScanDelegate(self.data_reporter))
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
                    f.write("{}\n".format(device))
                print("Writing to file : %s" % Global.scan_results)

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
    def __init__(self, reporter):
        self.data_reporter = reporter
        DefaultDelegate.__init__(self)

    def handleDiscovery(self, dev, isNewDev, isNewData):
        if isNewData:
            adv_data = dev.getValueText(0x16)
            if (None != adv_data and -1 != adv_data.find('95fe')):
                self.data_reporter.putData(adv_data)



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

