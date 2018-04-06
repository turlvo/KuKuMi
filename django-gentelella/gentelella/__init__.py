import subprocess
import signal
import sys

def startup():
    print ("Startup...")
    p = subprocess.Popen(["python", "../xiaomibt-daemon/xiaomibt-daemon.py", "restart"])

def signal_handler(signal, frame):
        print('You pressed Ctrl+C!')
        p = subprocess.Popen(["python", "../xiaomibt-daemon/xiaomibt-daemon.py", "stop"])
        sys.exit(0)

startup()
signal.signal(signal.SIGINT, signal_handler)


