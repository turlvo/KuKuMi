from http.server import BaseHTTPRequestHandler, HTTPServer
import threading


class Global(object):
    data_reporter = None

class Server(threading.Thread):
    def __init__(self, reporter, port):
        threading.Thread.__init__(self)
        Global.data_reporter = reporter
        self.port = port

    def run(self):
        print("Started WebServer on port 39501...")
        self.sever = HTTPServer(('', self.port), AduinoEventHandler)
        self.sever.serve_forever()

    def stop(self):
        self.sever.server_close()

class AduinoEventHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        data = self.rfile.read(content_length).decode('utf-8')
        print("AduinoEventHandler>>", data)
        Global.data_reporter.putData(data)
        self.send_response(200)
        self.end_headers()