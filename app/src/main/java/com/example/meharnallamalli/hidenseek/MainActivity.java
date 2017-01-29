package com.example.meharnallamalli.hidenseek;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.app.ProgressDialog;



public class MainActivity extends Activity {

    public static final String KEY_NAME = "Name";
    public static final String KEY_EMAIL = "giantzfan2758@gmail.com";
    public static final String PREF = "MyPref";
    private static String TAG = "MainActivity";
    private SharedPreferences mPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mPref = getApplicationContext().getSharedPreferences(PREF, 0);





        findViewById(R.id.btnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.sendEmailBtn).setOnClickListener(new View.OnClickListener() {

                public void onClick(View v){

                    SendSmsEmailTask task = new SendSmsEmailTask();

                    task.execute("Test email. Want to see if properly connected to SMTP Port.");
                }

        });


        findViewById(R.id.pairButton).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v){

                Intent i = new Intent(MainActivity.this, RSSIActivity.class);
                startActivity(i);
            }

        });


        findViewById(R.id.LowV).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v){

                    int strong_vibration = 30; //vibrate with a full power for 30 secs
                    int interval = 1000;
                    int dot = 1; //one millisecond of vibration
                    int short_gap = 1; //one millisecond of break - could be more to weaken the vibration
                    long[] pattern = {
                            0,  // Start immediately
                            strong_vibration,
                            interval,
                            // 15 vibrations and 15 gaps = 30millis
                            dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap,
                    };


            }

        });

        findViewById(R.id.MedV).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v){

                int strong_vibration = 30; //vibrate with a full power for 30 secs
                int interval = 1000;
                int dot = 1; //one millisecond of vibration
                int short_gap = 1; //one millisecond of break - could be more to weaken the vibration
                long[] pattern = {
                        0,  // Start immediately
                        strong_vibration,
                        interval,
                        // 15 vibrations and 15 gaps = 30millis
                        dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, //yeah I know it doesn't look good, but it's just an example. you can write some code to generate such pattern.
                };
            }

        });

        findViewById(R.id.StrongV).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v){

                int strong_vibration = 30; //vibrate with a full power for 30 secs
                int interval = 1000;
                int dot = 1; //one millisecond of vibration
                int short_gap = 1; //one millisecond of break - could be more to weaken the vibration
                long[] pattern = {
                        0,  // Start immediately
                        strong_vibration,
                        interval,
                        // 15 vibrations and 15 gaps = 30millis
                        dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, //yeah I know it doesn't look good, but it's just an example. you can write some code to generate such pattern.
                };
            }

        });


    }

    public void onResume() {

        super.onResume();
    }

    public void onPause() {
        super.onPause();


    }

    public void onDestroy() {

        super.onDestroy();
    }





    private class SendSmsEmailTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            for(String param : params) {
                Utils.email(getApplicationContext(), mPref.getString(KEY_EMAIL, "giantzfan2758@gmail.com"), "Test email. Want to see if properly connected to SMTP Port.");
            }
            return null;
        }
    }
}



