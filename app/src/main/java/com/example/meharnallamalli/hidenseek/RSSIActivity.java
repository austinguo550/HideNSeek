package com.example.meharnallamalli.hidenseek;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.meharnallamalli.hidenseek.Utils.getLocationPermission;

/**
 * Created by meharnallamalli on 1/12/17.
 */

public class RSSIActivity extends Activity {

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;    //must be greater than 0
    private static final int REQUEST_COARSE_LOCATION = 1;

    TextView mLabel;
    Button mCancelButton;
    Button mDiscoverButton;
    Button mStartPartyButton;
    ProgressBar mProgressBar;
    RecyclerView mRecyclerView;
    RSSIListAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    private Context mContext;
    RSSIActivity thisActivity = this;

    ArrayList<Device> deviceList;
    ArrayList<Device> partyList;                                                                    //PARTY RESET

    private boolean canStartDiscovering = false;
    private boolean selectedParty = false;                                                          //PARTY RESET

    //for debug purposes
    private final static String TAG = RSSIActivity.class.getSimpleName();

    //private Runnable runnable;
    //private Handler handler;
    //private final int interval = 12000; // 12 Seconds

    // To sample RSSI trend
    HashMap<String, ArrayList<Integer>> rssiTrends = new HashMap<String, ArrayList<Integer>>();     //PARTY RESET
    //HashMap<String, Integer> mPartyMapSignalAvgs = new HashMap<String, Integer>();
    private boolean contScan = false;                                                               //PARTY RESET


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        deviceList = new ArrayList<>();
        partyList = new ArrayList<>();

        mLabel = (TextView) findViewById(R.id.label);
        mDiscoverButton = (Button) findViewById(R.id.discover_btn);
        mStartPartyButton = (Button) findViewById(R.id.start_party_btn);
        mCancelButton = (Button) findViewById(R.id.cancel_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);    //infinite spinning wheel
        mProgressBar.setVisibility(View.INVISIBLE);

        mRecyclerView = (RecyclerView) findViewById(R.id.rssi_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RSSIListAdapter(this, deviceList);
        mRecyclerView.setAdapter(mAdapter);

        mContext = getApplicationContext();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        // Turning on Bluetooth if it wasn't on
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Making the device discoverable
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, 1);


        canStartDiscovering = (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);


        // Starting discovery with button press
        mDiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canStartDiscovering)
                    startDiscovering();
                else {
                    getLocationPermission(thisActivity, REQUEST_COARSE_LOCATION);
                }
                canStartDiscovering = (ContextCompat.checkSelfPermission(thisActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            }
        });

        // Cancel party with button press
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    unregisterReceiver(mReceiver);
                    deviceList.clear();
                    mAdapter.notifyDataSetChanged();
                    resetParty();

            }
        });


        // Start party with button press
        mStartPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canStartDiscovering) {
                    if (selectedParty) {
                        resetParty();
                        Toast.makeText(mContext, "Stopping party", Toast.LENGTH_SHORT).show();
                        mStartPartyButton.setText("Start Party");
                    }else{
                        startParty();
                        Toast.makeText(mContext, "Starting party", Toast.LENGTH_SHORT).show();
                        mStartPartyButton.setText("Stop Party");
                    }


                }
                else {
                    getLocationPermission(thisActivity, REQUEST_COARSE_LOCATION);
                }
            }
        });
    }

    private void resetParty() {
        selectedParty = false;
        mProgressBar.setVisibility(View.INVISIBLE);
        deviceList.clear();
        partyList.clear();
        rssiTrends.clear();
        setCardList(deviceList);
        mLabel.setText("Device List: ");
        turnOffContinuousScan();
    }

    private void startParty() {
        selectedParty = true;
        Toast.makeText(mContext, "Scanning for party signals", Toast.LENGTH_SHORT).show();
        if (selectedParty) {
            Log.d(TAG, "////////////Sampling RSSI");
            HashMap<String, Integer> mPartyAvgSignals = sampleRSSI();
            Log.d(TAG, "////////////Done sampling");
            turnOnContinuousScan();
            mProgressBar.setVisibility(View.VISIBLE);
            partyList.clear();
            partyList = mAdapter.getSelectedDevices();
            setCardList(partyList);
            scanForRSSI();
            /*handler = new Handler();
            runnable = new Runnable(){
                public void run() {
                    scanForRSSI();
                    Log.d(TAG, "running scanForRSSI every 12 seconds");
                }
            };
            handler.postAtTime(runnable, System.currentTimeMillis()+interval);
            handler.postDelayed(runnable, interval);*/
        }
    }

    private HashMap<String, Integer> sampleRSSI() {
        for (int i = 0; i < 5; i++) {
            scanForRSSI();
        }
        HashMap<String, Integer> rssi = new HashMap<String, Integer>();
        for (String device: rssiTrends.keySet()) {
            int avgSignal = 0;
            for (int i = 0; i < 5; i++) {
                avgSignal += rssiTrends.get(device).get(i);
            }
            avgSignal /= 5;
            rssi.put(device, avgSignal);
        }
        return rssi;
    }

    private boolean turnOnContinuousScan() {
        contScan = true;
        return contScan;
    }

    private boolean turnOffContinuousScan() {
        contScan = false;
        return contScan;
    }

    private void scanForRSSI() {
        // Starting scan for RSSI of devices in party
        BluetoothDevice remoteDevice = null;
        if (!mBluetoothAdapter.startDiscovery()) {
            Toast.makeText(mContext, "ERROR: Can't scan for nearby devices. A requested permission may be disabled.", Toast.LENGTH_SHORT).show();
            selectedParty = false;
        }
        else {
            if (contScan) {
                //vibratePhones();  //TODO: Make the phones vibrate depending on intensity
            }

            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);
        }
    }


    public void startDiscovering() {
        // Starting discovery
        deviceList.clear();
        mLabel.setText("Device List:");
        setCardList(deviceList);
        Toast.makeText(RSSIActivity.this, "Starting discovery for remote devices...", Toast.LENGTH_SHORT).show();
        if (!mBluetoothAdapter.startDiscovery())
            Toast.makeText(mContext, "Could not find any devices to connect to. A requested permission may be disabled.", Toast.LENGTH_SHORT).show();
        else {
            if (selectedParty) {
                mLabel.setText("Your party:");
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(RSSIActivity.class.getSimpleName(), "---- onReceive method started ----");
            String action = intent.getAction();

            // Just discovering: haven't selected party
            if (!selectedParty) {
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //discovery starts, we can show progress dialog or perform other tasks
                    Toast.makeText(mContext, "Discovery thread started... Scanning for devices", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, " ---- Reached ACTION_DISCOVERY_STARTED ----");
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //discovery finishes, dismiss progress dialog

                    mAdapter.notifyDataSetChanged();

                    Toast.makeText(mContext, "Done scanning", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, " ---- Reached ACTION_DISCOVERY_FINISHED ----");

                    //mBluetoothAdapter.cancelDiscovery();
                    //printDiscoveryResults();
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    Log.d(TAG, " ---- Reached ACTION_FOUND ----");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    if (device.getName() != null) {
                        Toast.makeText(mContext, "Found device " + deviceName, Toast.LENGTH_SHORT).show();

                        int rssi = (int) intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                        //TODO: Make this a checklist where you can select party members
                        addDeviceToList(deviceList, deviceName, rssi, device.getAddress(), false);

                        Toast.makeText(mContext, "There are " + deviceList.size() + " device(s) in party.", Toast.LENGTH_SHORT).show();
                    }  else {
                        Toast.makeText(mContext, "Found unknown device, ignoring...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            // You've selected party, scanning for RSSI values
            else {
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //discovery starts, we can show progress dialog or perform other tasks
                    Log.d(TAG, " ---- Reached ACTION_DISCOVERY_STARTED ----");
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //discovery finishes, dismiss progress dialog
                    mAdapter.notifyDataSetChanged();
                    // You've already determined which Bluetooth devices are in your party
                    if (contScan) {
                        scanForRSSI();  // Won't be called if selectedParty = false, so if you click cancel recursive loop will stop
                    }
                    // If not in continuous scan mode, the only reason to be called while selectedParty is on is to sample RSSI and find average

                    //mBluetoothAdapter.cancelDiscovery();
                    //printDiscoveryResults();
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    Log.d(TAG, " ---- Reached ACTION_FOUND ----");
                    BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = newDevice.getName();
                    if (newDevice.getName() != null) {
                        Toast.makeText(mContext, "Found device " + deviceName, Toast.LENGTH_SHORT).show();

                        int rssi = (int) intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        addDeviceToList(partyList, deviceName, rssi, newDevice.getAddress(), true);
                        Toast.makeText(mContext, "There are " + partyList.size() + " device(s) in party.", Toast.LENGTH_SHORT).show();

                        // If not in continuous scan mode, the only reason to be called while selectedParty is on is to sample RSSI and find average
                        if (!contScan) {
                            if (!rssiTrends.containsKey(deviceName)) {
                                ArrayList<Integer> deviceSignalTrend = new ArrayList<Integer>();
                                deviceSignalTrend.add(rssi);
                                rssiTrends.put(deviceName, deviceSignalTrend);
                            } else {    // The device is already there, just append the new RSSI to the deviceSignalTrend
                                rssiTrends.get(deviceName).add(rssi);
                            }

                        }
                    } else {
                        Toast.makeText(mContext, "Found unknown device, ignoring...", Toast.LENGTH_SHORT).show();
                    }
                }
            }


        }
    };






    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    canStartDiscovering = true;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setCardList(ArrayList<Device> list) {
        mAdapter.setDeviceList(list, selectedParty);
        mAdapter.notifyDataSetChanged();
    }

    private void addDeviceToList(ArrayList<Device> list, String name, int rssi, String address, boolean updateFlag) {
        for (Device d : list) {
            if(d.address.equals(address)) {
                d.setRssiStrength(rssi);
                mAdapter.notifyDataSetChanged();
                return;
            }
        }
        if(!updateFlag)
            list.add(new Device(name, rssi, address));
        mAdapter.notifyDataSetChanged();
    }




    /*  TODO: Implement onPause and onResume to turn off discovery/etc when app is paused
    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                Length, long, locationListener);
    }
    */


}
