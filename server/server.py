import tornado.httpserver
import tornado.websocket
import tornado.ioloop
import tornado.web
import socket
import thread

class IRCSocket (object):
    def __init__(self, wsHandler):
        self.ws=wsHandler
        self.connected=False
        
    def connect(self,host,port):
        try:
            self.conn=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.conn.connect((host,int(port)))
            self.connected=True
            print 'Connected!!!'
        except socket.error as msg:
            print 'ERROR OPENING IRC SOCKET'
            self.close()

    def listen(self):
        self.thread=thread.start_new_thread(self.loop,())
        
    def loop(self):
        while self.connected:
            try:
                data=self.conn.recv(512)
                if not self.connect: 
                    return
                if not data: #Disconnected
                    print 'NO MORE DATA'
                    self.close()
                self.ws.write_message(data)
            except socket.error as e:
                print 'LOOP SOCKET ERROR: '.format(e.errno, e.strerror)
                self.close()

    def send(self,data):
        try:
            if (self.connected):
                print 'Repassing message: '+data
                self.conn.send(data)
        except socket.error as e:
            print 'SEND SOCKET ERROR: '.format(e.errno, e.strerror)
            self.close()
            
    def close(self):
        if self.connected:
            self.conn.close()
        self.connected=False
        try:
            self.ws.close()
        except Exception:
            pass

class WSHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        print 'new connection'
        self.ircSocket = IRCSocket(self)
        self.write_message("Hello World")
      
    def on_message(self, message):
        print 'message received %s' % message
        if message.startswith("cake"):
            splitedMsg = message.split(' ')
            if len(splitedMsg) == 4 and splitedMsg[1]=='connect':
                print 'trying to connect'
                self.ircSocket.connect(splitedMsg[2], int(splitedMsg[3]))
                self.ircSocket.listen()
                
        else:
            self.ircSocket.send(message)
                
    def on_close(self):
        self.ircSocket.close()
        print 'connection closed'
 
 
application = tornado.web.Application([
    (r'/ws', WSHandler),
])
 
 
if __name__ == "__main__":
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(9999)
    print 'Starting server...'
    try:
        tornado.ioloop.IOLoop.instance().start()
    except KeyboardInterrupt:
        print ' detected, closing server'
        http_server.stop()
        pass
