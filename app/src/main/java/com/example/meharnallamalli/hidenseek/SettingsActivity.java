package com.example.meharnallamalli.hidenseek;



import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.SeekBar;

import android.bluetooth.BluetoothAdapter;




public class SettingsActivity extends Activity {


    SharedPreferences mPref;
    EditText mEmailEdit;
    EditText mBLENameEdit;
    String newName;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPref = getApplicationContext().getSharedPreferences(MainActivity.PREF, 0);

        mBLENameEdit = ((EditText) findViewById(R.id.nameEdit));
        mBLENameEdit.setText(mPref.getString(MainActivity.KEY_NAME, ""));

        mEmailEdit = ((EditText) findViewById(R.id.emailEdit));
        mEmailEdit.setText(mPref.getString(MainActivity.KEY_EMAIL, ""));

        setLocalBluetoothName(newName);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                String x = "Start game after  \t" + progressChangedValue + " min.";
                ((TextView) findViewById(R.id.seekBarProgress)).setText(x);
            }


        });

        findViewById(R.id.btnSaveSettings).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mPref.edit();

                editor.putString(MainActivity.KEY_NAME, mBLENameEdit.getText().toString());
                editor.putString(MainActivity.KEY_EMAIL, mEmailEdit.getText().toString());
                editor.apply();
                finish();
            }
        });
    }

    public void setLocalBluetoothName(String newName) {

        newName = "";

        String name = mBluetoothAdapter.getName();
        mBluetoothAdapter.setName(newName);


    }

}