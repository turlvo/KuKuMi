from celery import Celery
app = Celery('tasks', broker='amqp://kuku:kuku@localhost:5672//')

from bluepy.btle import Scanner, DefaultDelegate, BTLEException
import sys, time, os
import queue as Queue

from .reporter import Reporter
from .server import Server


@app.task
def add(x,y):
    return x+y

@app.task
def startXiaomiBT(hubaddr, iface, threshold):
    xiaomi_bt = XiaomiBT(hubaddr, iface, threshold)
    xiaomi_bt.start()

class XiaomiBT():
    def __init__(self, hub_addr=None, iface=0, threshold=0.1):
        self.iface = 0
        self.hub_addr = hub_addr
        self.threshold = threshold

        self.found_devices = None		
        self.before_temperature = {}
        self.before_humidity = {}

        self.reporting_queue = Queue.Queue()
        self.data_reporter = Reporter(self.reporting_queue, self.hub_addr, self.threshold)
        self.data_reporter.setDaemon(True)

        self.bridge_server = Server(self.data_reporter, 39501)
        self.bridge_server.setDaemon(True)


    def start(self):
        print ("XiaomiBT started~~~")
        self.data_reporter.start()
        self.bridge_server.start()

        while True:
    #        if (os.path.isfile(CONST_CONFIG_PID_FILE) == False):
    #            self.data_reporter.stop()
    #            self.bridge_server.stop()
    #            print("release server, reporter")

            try:
                self.scanner = Scanner(1).withDelegate(ScanDelegate(self.data_reporter))
                self.found_devices = self.scanner.scan(10.0)
            except BTLEException as error:
                print ('Error in main %s' % str(error))

            time.sleep(2)

class ScanDelegate(DefaultDelegate):
    def __init__(self, reporter):
        self.data_reporter = reporter
        DefaultDelegate.__init__(self)

    def handleDiscovery(self, dev, isNewDev, isNewData):
        if isNewData:
            adv_data = dev.getValueText(0x16)
            if (None != adv_data and -1 != adv_data.find('95fe')):
                self.data_reporter.putData(adv_data)
