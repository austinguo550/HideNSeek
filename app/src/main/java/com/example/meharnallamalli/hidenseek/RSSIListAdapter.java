package com.example.meharnallamalli.hidenseek;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vaibhav on 1/13/17.
 */

public class RSSIListAdapter extends RecyclerView.Adapter<RSSIListAdapter.DeviceViewHolder> {

    private static final String TAG = "String";
    private List<Device> deviceList;
    private Context mContext;
    private boolean inParty = false;

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView deviceName;
        TextView rssiStrength;
        TextView vibes;

        DeviceViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view);
            deviceName = (TextView)itemView.findViewById(R.id.device_name);
            rssiStrength = (TextView)itemView.findViewById(R.id.rssi_strength);
            vibes = (TextView)itemView.findViewById(R.id.vibes);
        }
    }

    public RSSIListAdapter(Context c, List<Device> deviceList) {
        this.mContext = c;
        this.deviceList = deviceList;
    }

    public void setDeviceList(ArrayList<Device> l, boolean inParty) {

        deviceList = l;
        this.inParty = inParty;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rssi_card, parent, false);
        DeviceViewHolder pvh = new DeviceViewHolder(v);
        return pvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final DeviceViewHolder holder, final int position) {
        final Device device = deviceList.get(position);

        holder.deviceName.setText(device.name);
        int rssiOfDev = device.rssiStrength;
        holder.rssiStrength.setText(""+scaledRssi(rssiOfDev));//String.format("%d", scaledRssi(device)));
        //holder.vibes.setText(getPartySignals(scaledRssi(device)));
        holder.vibes.setText(getPartySignals(scaledRssi(rssiOfDev)));    // OLD CODE

        if(!inParty) {
            holder.cv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Device onClick at" + position);
                    if (!device.selected) {
                        holder.cv.setCardBackgroundColor(Color.GRAY);
                        device.select();
                    } else if (device.selected) {
                        holder.cv.setCardBackgroundColor(Color.WHITE);
                        device.deselect();
                        //unchecked.
                    }
                }


            });
        }else{
            holder.cv.setCardBackgroundColor(Color.WHITE);
            vibrate();
        }
    }


    private int scaledRssi(Device device){
        int scaled =0;
        if(device.rssiStrength >= -30){
            scaled = 100;
        }else if(device.rssiStrength <= -100){
            scaled = 0;
        }else {
            int span = (-30+100);
            double scaledPercent = 100 - ((-device.rssiStrength - 30)/span)*100;
            scaled = (int) scaledPercent;
            return scaled;
        }


        return scaled;
    }

    private int scaledRssi(int rssiValue){
        int scaled = 0;
        if(rssiValue >= -30){
            scaled = 100;
            return scaled;
        }else if(rssiValue <= -100){
            scaled = 0;
            return scaled;
        }else {
            int span = ((-1* 30)+100);
            double scaledPercent = 100-((0-rssiValue) - 30)/70.0*100;
            //Toast.makeText(mContext, "scaledRSSI is " + scaledPercent, Toast.LENGTH_SHORT).show();
            scaled = (int) scaledPercent;
            return scaled;
        }
    }


    private String getPartySignals (int rssiValue) { //(Device device) {


        String signalStrength = "";
        /* Old code
        if (device.rssiStrength >= -50) {
            signalStrength = "Strong vibes";
        } else if (device.rssiStrength < -50 && device.rssiStrength >= -80) {
            signalStrength = "Ok vibes";
        } else if (device.rssiStrength < -80 && device.rssiStrength >= -90) {
            signalStrength = "Low vibes";
        } else if (device.rssiStrength < -90) {
            signalStrength = "Lost signal";
        }*/

        //new </code>
        if (rssiValue >= 80) signalStrength = "Strong vibes";
        else if (rssiValue >= 60 && rssiValue < 80) signalStrength = "Good vibes";
        else if (rssiValue >= 30 && rssiValue < 60) signalStrength = "Vibes";
        else if (rssiValue >= 0 && rssiValue < 30) signalStrength = "Low vibes";
        else if (rssiValue < 0) signalStrength = "Lost signal";
        return signalStrength;
    }

    private String getPartySignals (Device device) { //(Device device) {


        String signalStrength = "";
        // Old code
        if (device.rssiStrength >= -50) {
            signalStrength = "Strong vibes";
        } else if (device.rssiStrength < -50 && device.rssiStrength >= -80) {
            signalStrength = "Ok vibes";
        } else if (device.rssiStrength < -80 && device.rssiStrength >= -90) {
            signalStrength = "Low vibes";
        } else if (device.rssiStrength < -90) {
            signalStrength = "Lost signal";
        }
        return signalStrength;
    }






    public void vibrate() {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        if(deviceList.size() > 0) {
            Device closestDevice = deviceList.get(0);
            for (Device device : deviceList) {
                if (device.rssiStrength > closestDevice.rssiStrength) {
                    closestDevice = device;
                }
            }
            if (closestDevice.rssiStrength >= -50) {
                v.vibrate(new long[]{0, 500, 110, 500, 110, 450, 110, 200, 110, 170, 40, 450, 110, 200, 110, 170, 40, 500}, -1);
            } else if (closestDevice.rssiStrength < -50 && closestDevice.rssiStrength >= -80){
                v.vibrate(4000);
            } else if (closestDevice.rssiStrength < -80 && closestDevice.rssiStrength >= -90) {
                v.vibrate(3000);
            } else if (closestDevice.rssiStrength < -90) {
                v.vibrate(2000);
            }
        }
    }

    public ArrayList<Device> getSelectedDevices() {
        ArrayList<Device> out = new ArrayList<>();
        for(Device d : deviceList) {
            if(d.isSelected())
                out.add(d);
        }
        return out;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
