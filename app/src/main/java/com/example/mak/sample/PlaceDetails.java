package com.example.mak.sample;

import se.walkercrou.places.Hours;
import se.walkercrou.places.Price;
import se.walkercrou.places.Status;

/**
 * Created by mak on 12/3/15.
 */
public class PlaceDetails {
    private String placeId;
    private String gUrl;
    private Hours hours;
    private Status status;
    private Price price;
    private String website;
    private String phone;
    private String name;
    private double latitude;
    private double longitude;
    private double rating;
    //private double yelp_rating;

    @SuppressWarnings("unused")
    private PlaceDetails (){}

    PlaceDetails(String placeId,  String gurl, Hours hours, Status status, Price price, String website, String phone,
                         String name, double latitude, double longitude, double rating){
        this.placeId = placeId;
        this.gUrl = gurl;
        this.hours = hours;
        this.status = status;
        this.price = price;
        this.website = website;
        this.phone = phone;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
    }

    public String getPlaceId(){
        return placeId;
    }
    public String getgUrl(){
        return gUrl;
    }

    public Hours getHours(){
        return hours;
    }

    public Status getStatus(){
        return status;
    }

    public Price getPrice(){
        return price;
    }

    public String getWebsite(){
        return website;
    }

    public String getPhone(){
        return phone;
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

    public double getRating(){
        return rating;
    }

    @Override
    public String toString(){
        return "Too Many Place Details to Print";
    }


}
