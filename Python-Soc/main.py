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