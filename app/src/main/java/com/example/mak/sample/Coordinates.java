package com.example.mak.sample;
/**
 * Created by mak on 11/30/15.
 */
public class Coordinates {
    private double latitude;
    private double longitude;

    @SuppressWarnings("unused")
    private Coordinates(){}

    Coordinates(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude(){return latitude;}

    public double getLongitude(){return longitude;}

    @Override
    public String toString(){ return "Latitude="+ String.valueOf(latitude) + "Longitude=" + String.valueOf(longitude);}
}
