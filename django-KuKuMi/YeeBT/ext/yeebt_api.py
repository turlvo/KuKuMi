import logging
from yeelightbt import Lamp
from bluepy import btle
import time
from threading import Event
import threading

DEBUG = 1

class YeeBT_Light(threading.Thread):
    def __init__(self, addr, callback=None, interface=None):
        super(YeeBT_Light, self).__init__()
        if DEBUG:
            #btle.Debugging = True
            logging.basicConfig(level=logging.DEBUG)
        else:
            logging.basicConfig(level=logging.INFO)
        self.addr = addr
        self.callback = callback
        self.iface = interface
        self.runnable = True
        self.isConnected = False

        self.lamp = Lamp(self.addr, self.notification_cb, self.paired_cb,
                keep_connection=True, wait_after_call=0.2)

        #self.scan(5)
        #self.connect()
        #self.get_state()
        #self.on()

    def run(self):
        while self.runnable:
            try:
                print('connecting to sensor...')
                self.connect()

                while True:
                    time.sleep(1)
                    self.wait_notification(1)
            except Exception as e:
                print ('Error in main %s' % str(e))
                self.isConnected = False
            else:
                print('disconnect')

    def connect(self):
        try:
            result = self.lamp.connect()
            self.isConnected = True
            logging.debug ("Succeed to connect to  >> %s" % self.addr)
        except btle.BTLEException as ex:
            self.isConnected = False
            logging.error ("Failed to connect to %s!!!" % self.addr)

    def disconnect(self):
        try:
            result = self.lamp.disconnect()
            self.isConnected = False
            logging.debug ("disconnect >> %s" % result)
        except btle.BTLEException as ex:
            logging.error ("Failed to disconnect :  %s!!!" % self.addr)
    
    def on(self):
        try:
            result = self.lamp.turn_on()
            logging.debug ("on >> %s" % result)
        except btle.BTLEException as ex:
            logging.error ("Failed to on: %s!!!" % self.addr)

    def off(self):
        try:
            result = self.lamp.turn_off()
            logging.debug ("off >> %s" % result)
        except btle.BTLEException as ex:
            logging.error ("Failed to off:  %s!!!" % self.addr)

    def set_brightness(self, brightness):
        try:
            result = self.lamp.brightness(brightness)
            logging.debug ("set_brightness >> brightness: %d, result: %s" % (brightness, result))
        except btle.BTLEException as ex:
            logging.error ("Failed to set_brightness: brightness: %d, mac:  %s!!!" % (brightness, self.addr))

    def get_state(self):
        try:
            result = self.lamp.state()
            logging.debug ("getState >> %s" % result)
        except btle.BTLEException as ex:
            logging.error ("Failed to connect to %s!!!" % self.addr)


    def wait_notification(self, sec):
        self.lamp.wait(sec)

    def paired_cb(self, data):
        if data.pairing_status == "PairRequest":
            print ("Waiting for pairing, please push the button/change the brightness")
            time.sleep(5)
        elif data.pairing_status == "PairSuccess":
            print ("We are paired.")
        elif data.pairing_status == "PairFailed":
            print ("Pairing failed, exiting")
            #sys.exit(-1)
        logging.debug ("Got paired? %s" % data.pairing_status)

    def notification_cb(self, data):
        print ("Got notification: %s" % data)
        print ("mac: %s"  % (data.addr))
        print ("is_on: %s" % (data.is_on))
        print ("brightness: %s" % (data.mode))
    
    @staticmethod
    def scan(sec=5):
        """ Scans for available devices. """
        scan = btle.Scanner()
        print ("Scanning for %s seconds" % sec)
        
        try:
            devs = scan.scan(sec)
        except btle.BTLEException as ex:
            logging.error ("Unable to scan for devices, did you set-up permissions for bluepy-helper correctly? ex: %s" % ex)
            return

        print ("Devices found:")
        found_devices = []
        for dev in devs:
            localname = dev.getValueText(9)
            if not localname: continue
            if localname.startswith("XMCTD_"):
                print ("Bedlight lamp v1  %s (%s), rssi=%d" % (dev.addr, localname, dev.rssi))
            elif localname.startswith("yeelight_ms"):
                print ("Candela %s (%s), rssi=%d" % (dev.addr, localname, dev.rssi))
                found_devices.append({"MAC address": dev.addr, "name": localname, "RSSI": dev.rssi})
        return found_devices
    


if __name__ == '__main__':
    runner = YeeBT_Light('f8:24:41:c0:80:41')
    runner.demon = True
    runner.start()

