package com.codse.ananta.smartx;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    private UsbService usbService;
    private MyHandler mHandler;
    double Latitude;
    double Longitude;
    LocationManager locationManager;
    public static double Y;
    private Socket mSocket;
    protected boolean serverRunning = false;
    private Button getIpButton;
    private EditText ipText;

    private TextView txtFromServer;
    private EditText txtBaudrate;
    private EditText txtSerial;
    private Button btnSerial;


    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);

        txtFromServer = (TextView) findViewById(R.id.txtDataFromServer);
        txtBaudrate = (EditText) findViewById(R.id.txtBaudrate);
        txtSerial = (EditText) findViewById(R.id.txtSerial);
        btnSerial = (Button) findViewById(R.id.btnSendSerial);
        txtSerial = (EditText) findViewById(R.id.txtSerial);

        getIpButton = (Button) findViewById(R.id.getIpBtn);
        ipText = (EditText) findViewById(R.id.editText);



        btnSerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(!txtSerial.getText().toString().equals("")){
                   String data = txtSerial.getText().toString();
                   if (usbService != null) { // if UsbService was correctly binded, Send data
                       usbService.changeBaudRate(Integer.parseInt(txtBaudrate.getText().toString()));
                       usbService.write(data.getBytes());
                   }
               }
            }
        });

        getIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(serverRunning == false){
                    serverRunning = true;
                    getIpButton.setText("Stop Client");
                    getIpButton.setBackgroundColor(getResources().getColor(R.color.materialGreen));
                    startPythonClient(ipText.getText().toString());
                }else{
                    serverRunning = false;
                    getIpButton.setText("Start Client");
                    getIpButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    stopPythonClient();
                }
            }
        });
    }

    // A callback for received data from serial
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback()
    {
        @Override
        public void onReceivedData(byte[] arg0)
        {
            // Code here
        }
    };

    protected void stopPythonClient(){
        mSocket.disconnect();
    }
    protected void startPythonClient(String _ip){
        {
            try {
                mSocket = IO.socket(_ip);
            } catch (URISyntaxException e) {}
        }
        mSocket.connect();
        mSocket.emit("message","Connected");
        checkPermission();

        mSocket.on("s", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Handel Serial
                        Log.e("DDDDDDDDDDDDDD",args.length+"");

                        String upcomingDataFromServer = args[0].toString();
                        Log.e("DDDDDDDDDDDDDD",upcomingDataFromServer+"<-");

                        if(!upcomingDataFromServer.equals("")){
                            if (usbService != null) { // if UsbService was correctly binded, Send data
                                usbService.write(upcomingDataFromServer.getBytes());
                            }
                        }
                    }
                });
            }
        });
        // Data From Server
        mSocket.on("dataFromServer", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.e("dataFromServer",args[1].toString());
                    }
                });
            }
        });



        final MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                Longitude = location.getLongitude();
                Latitude  = location.getLatitude();
                mSocket.emit("location","{\"longitude\":"+Longitude+", \"latitude\":" +Latitude+ "}");
                mSocket.emit("e","");
            }
        };

        final MyLocation myLocation = new MyLocation();


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myLocation.getLocation(getApplicationContext(), locationResult);
                    }
                });
            }
        }, 0, 1000);
    }

    void checkPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }



    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    Log.e("FROM ARDUINO", data);
                    mActivity.get().txtFromServer.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    Log.e("FROM ARDUINO",buffer);
                    mActivity.get().txtFromServer.append(buffer);
                    break;
            }
        }
    }
}