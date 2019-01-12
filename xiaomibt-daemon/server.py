from http.server import BaseHTTPRequestHandler, HTTPServer
import threading
import socketserver
import sys
import logging
import paho.mqtt.client

logging.basicConfig(
            format='%(asctime)s %(levelname)-8s %(message)s',
            level=logging.INFO,
            datefmt='%Y-%m-%d %H:%M:%S')

class Global(object):
    data_reporter = None

class Server(threading.Thread):
    def __init__(self, reporter, port):
        threading.Thread.__init__(self)
        self.data_reporter = reporter
        self.port = port

        self.client = paho.mqtt.client.Client()
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message

    def run(self):
        logging.info("Started TCP Server on port %d..." % self.port)
        self.client.connect(host='127.0.01', port=self.port, keepalive=60)
        self.client.loop_forever()

    def stop(self):
        if None != self.client:
            self.client.disconnect()

    def on_connect(self, client, userdata, flags, rc):
        logging.info('ESP connected (%s)' % client._client_id)
        client.subscribe(topic='esp/kukumi')

    def on_message(self, client, userdata, message):
        received = str(message.payload, encoding='utf-8')
        logging.info ("Received from bridge aduino: {0}".format(received))
        self.data_reporter.putData(received)


if __name__ == '__main__':
    server = Server(None, 1883)
    server.run()
