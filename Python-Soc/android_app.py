from flask import Flask 
import json
from flask_socketio import SocketIO, emit
import time

app = Flask(__name__)
socketio = SocketIO(app)
connected = False

def call_back():
    print("Data Was Sent to Android")

@socketio.on('sendData')
def sendDataToAndroid(msg):
	print('.....................................................................Sending Data To Android: ' + msg)
	# Sendind Data to the Client
	emit("dataFromServer", msg,callback=call_back)

# Client Connected
@socketio.on('connect')
def test_connect():
    connected = True
    print "\n"
    print "*********************Client Connected*********************"
    time.sleep(1)
    emit("dataFromServer", "HELLO CHAMPAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")


# Client Disconnected
@socketio.on('disconnect')
def test_connect():
    connected = True
    print
    print "*********************Client Disconnected*********************"
    print
    

@socketio.on('location')
def handleMessage(msg):
	# print('Message: ' + msg)
	# send(msg, broadcast=True)
	try:
		b = json.dumps(msg)
		a = json.loads(b)
		print a
    
	except Exception as e:
		print "Error "
		print str(e)
        

