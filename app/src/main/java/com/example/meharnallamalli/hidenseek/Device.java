package com.example.meharnallamalli.hidenseek;

/**
 * Created by vaibhav on 1/13/17.
 */

public class Device {

    String name;
    public int rssiStrength;

    String signalStrength;
    String address;
    boolean selected = false;


    Device(String name, int rssiStrength, String address) {
        this.name = name;
        this.rssiStrength = rssiStrength;
        this.address = address;
    }

    public void setRssiStrength(int rssiStrength) {
        this.rssiStrength = rssiStrength;
    }

    public void select() { selected = true; }

    public void deselect() { selected = false; }

    public boolean isSelected() { return selected; }
}
