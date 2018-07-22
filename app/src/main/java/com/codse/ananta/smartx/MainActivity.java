package com.codse.ananta.smartx;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    double Latitude;
    double Longitude;
    LocationManager locationManager;
    public static double Y;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.100.52:3000");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        mSocket.connect();
        mSocket.emit("message","Connected");
        checkPermission();

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
                Log.e("DATA","YOOOO");
            }
        });

        final MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                Longitude = location.getLongitude();
                Latitude  = location.getLatitude();
                mSocket.emit("location","{\"longitude\":"+Longitude+", \"latitude\":" +Latitude+ "}");
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

    @Override
    protected void onStop() {
        super.onStop();
        mSocket.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSocket.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSocket.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    void checkPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }


}