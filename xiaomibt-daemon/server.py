from http.server import BaseHTTPRequestHandler, HTTPServer
import threading
import socketserver
import sys


class Global(object):
    data_reporter = None

class Server(threading.Thread):
    def __init__(self, reporter, port):
        threading.Thread.__init__(self)
        Global.data_reporter = reporter
        self.port = port
        self.server = None

    def run(self):
        #print("Started WebServer on port 39501...")
        #self.sever = HTTPServer(('', self.port), AduinoEventHandler)
        #self.sever.serve_forever()

        print("Started TCP Server on port 39501...")
        self.server = socketserver.TCPServer(('', self.port), MyTCPHandler)
        self.server.allow_reuse_address = True
        self.server.serve_forever()

    def stop(self):
        #self.sever.server_close()

        if None != self.server:
            self.server.shutdown()
            self.server.socket.close()

class AduinoEventHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        data = self.rfile.read(content_length).decode('utf-8')
        print("AduinoEventHandler>>", data)
        Global.data_reporter.putData(data)
        self.send_response(200)
        self.end_headers()

class MyTCPHandler(socketserver.BaseRequestHandler):
    def handle(self):
        print("Client connected: {0}".format(self.client_address[0]))
        sock = self.request
        rbuff = sock.recv(1024)
        received = str(rbuff, encoding='utf-8')
        print("Received : {0}".format(received))
        Global.data_reporter.putData(received)
        sock.close()
