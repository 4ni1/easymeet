package com.example.mak.sample;

import se.walkercrou.places.Hours;
import se.walkercrou.places.Price;
import se.walkercrou.places.Status;

/**
 * Created by mak on 12/3/15.
 */
public class SimplePlace {
    private String placeId;
    private String name;
    private double latitude;
    private double longitude;
    //private double yelp_rating;

    @SuppressWarnings("unused")
    private SimplePlace (){}

    SimplePlace(String placeId, String name, double latitude, double longitude){
        this.placeId = placeId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlaceId(){
        return placeId;
    }

    public String getName(){
        return name;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    @Override
    public String toString(){
        return "ID : " + placeId + " Name : " + name + " Latitude: " + String.valueOf(latitude) +
                " Longitude: " + String.valueOf(longitude);
    }


}
