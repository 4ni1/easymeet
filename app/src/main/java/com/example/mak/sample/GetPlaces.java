package com.example.mak.sample;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;
import se.walkercrou.places.TypeParam;
import se.walkercrou.places.Types;
import se.walkercrou.places.exception.GooglePlacesException;

/**
 * Created by mak on 12/2/15.
 */



public class GetPlaces extends AsyncTask <String, Void, List<Place>> {

    protected static final String TAG = "EasyMeet";
    private static final String FIREBASE_URL = "https://sweltering-inferno-3584.firebaseio.com/";
    private Firebase mFirebasePlaces;
    private Context placesContext;
    private GoogleMap mMap;

    public GetPlaces(Context context, Firebase mFirebaseMaps, GoogleMap mMap){
        this.placesContext = context;
        this.mFirebasePlaces = mFirebaseMaps;
        this.mMap = mMap;
    }

    Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
        @Override
        public void onAuthenticated(AuthData authData) {
            // Authenticated successfully with payload authData
            Log.d(TAG, "Firebase Successful Authentication : " + authData.toString());
        }
        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            // Authenticated failed with error firebaseError
            Log.d(TAG, "Firebase Unsuccessful Authentication : " + firebaseError.toString());
        }
    };

    @Override
    protected void onPreExecute(){
        mFirebasePlaces = new Firebase(FIREBASE_URL).child("places");
        mFirebasePlaces.authAnonymously(authResultHandler);
        mFirebasePlaces.removeValue();
    }

    @Override
    protected List<Place> doInBackground(String... params) {

        List<Place> places = new ArrayList<>();
        try {
            List<String> types = new ArrayList<String>(Arrays.asList(params[4]));
            GooglePlaces client = new GooglePlaces(params[0]);
            places = client.getNearbyPlaces(Double.valueOf(params[1]),
                    Double.valueOf(params[2]), Double.valueOf(params[3]),
                    Integer.valueOf(params[4]), Param.name("opennow").value(true),
                    TypeParam.name(GooglePlaces.STRING_TYPES).value(Arrays.asList(Types.TYPE_BAR,
                            Types.TYPE_RESTAURANT, Types.TYPE_FOOD)));
            for(Place place : places){
            SimplePlace firebasePlace = new SimplePlace(place.getPlaceId(), place.getName(),
                    place.getLatitude(), place.getLongitude());
                //Log.d(TAG, firebasePlace.toString());
                mFirebasePlaces.child(place.getPlaceId()).setValue(firebasePlace);
            }

        }
        catch (GooglePlacesException g){
            Log.d(TAG, "ERROR: " + g.getErrorMessage() + g.getStatusCode());
        }
        return places;
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
    }

    @Override
    protected void onPostExecute(List<Place> places) {
        if (!places.isEmpty()) {
            for (Place place : places) {
                LatLng latlng = new LatLng(place.getLatitude(), place.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(latlng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title(place.getName()));
            /*    PlaceDetails placeDetails = new PlaceDetails(place.getPlaceId(), place.getGoogleUrl(),
                        place.getHours(), place.getStatus(), place.getPrice(), place.getWebsite(),
                        place.getPhoneNumber(), place.getName(), place.getLatitude(), place.getLongitude(),
                        place.getRating());
                        */
            }
            Toast.makeText(placesContext, "Places Recieved!", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(placesContext, "Places List Empty", Toast.LENGTH_SHORT).show();
        }
}
