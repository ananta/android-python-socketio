import android_app
import time

def main():
	print "main() called"
	android_app.socketio.start_background_task(run_android_app())
	
	android_app.sendDataToAndroid("CHAMPA")

def run_android_app():
	android_app.socketio.run(android_app.app, "0.0.0.0", 3000, debug=True)
	
if __name__ == '__main__':
	main()

# import android_app
# import eventlet
# eventlet.monkey_patch()
# import time
# from threading import Thread

# def sendDATA():
# 	time.sleep(5)
# 	print "TIME OVERRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"


# def main():
# 	print "main() called"
# 	# android_app.start_scheduler()
# 	android_app.socketio.start_background_task(run_android_app())



# def run_android_app():
# 	android_app.socketio.run(android_app.app, "0.0.0.0", 3000, debug=False)
# 	android_app.args"""

# f1  = Thread(target = main)
# f2 = Thread(target = sendDATA)
# if __name__ == '__main__':
# 	f1.start()
# 	f2.start()	
