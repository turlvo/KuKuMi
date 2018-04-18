import subprocess
import signal
import sys
import os.path

CONST_CONFIG_IFACE_FILE = '/tmp/xiaomibt-daemon.iface'

def startup():
    print ("Startup...")
    p = subprocess.Popen(["python", "../xiaomibt-daemon/xiaomibt-daemon.py", "restart"])

def signal_handler(signal, frame):
        print('You pressed Ctrl+C!')
        p = subprocess.Popen(["python", "../xiaomibt-daemon/xiaomibt-daemon.py", "stop"])
        sys.exit(0)

if os.path.isfile(CONST_CONFIG_IFACE_FILE):
    startup()
signal.signal(signal.SIGINT, signal_handler)


