package com.example.mak.sample;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.content.IntentSender;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.walkercrou.places.Param;
import se.walkercrou.places.TypeParam;
import se.walkercrou.places.Types;

public class MapsActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener{

    // Firebase Related
    private static final String FIREBASE_URL = "https://sweltering-inferno-3584.firebaseio.com/";
    private Firebase mFirebaseMaps;
    private Firebase mFirebasePlaces;
    private ValueEventListener mFirebaseMapsListener;
    private ValueEventListener mFirebasePlacesListener;
    private static String UID = Build.SERIAL;

    private List<Marker> allMapMarkers = new ArrayList<>();
    private List<Marker> allPlaceMarkers = new ArrayList<>();

    protected GoogleMap mMap;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    protected static final String TAG = "EasyMeet";
    protected static final String GOOGLE_SERVER_API_KEY="AIzaSyA8bwa3ynMQHnJwJrHxKVKn4oz1P5uAkWk";
    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected Location location;
    private LatLngBounds.Builder latlngbounds;
    private LatLngBounds latLngBoundsUpdate;
    private LatLng centroid;

    private SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        buildGoogleApiClient();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        if (mLocationRequest != null){
            Log.d(TAG, "Location Request Created");
        }

        // Unique ID to differentiate the Users
        Log.d(TAG, UID);

        // Firebase Initialization
        Firebase.setAndroidContext(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                show_places();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void show_places(){
        GetPlaces places = new GetPlaces(getApplicationContext(), mFirebaseMaps, mMap);
        places.execute(GOOGLE_SERVER_API_KEY, String.valueOf(centroid.latitude),
                String.valueOf(centroid.longitude), String.valueOf(1000.0), String.valueOf(10));

    }

    private void display_places(){
        Intent myIntent = new Intent(getApplicationContext(), PlacesViewActivity.class);
        startActivity(myIntent);
    }

    private void remove_all_map_markers(){
        if (!allMapMarkers.isEmpty()){
            allMapMarkers.clear();
        }

    }

    private void remove_all_place_markers(){
        if (!allPlaceMarkers.isEmpty()){
            allPlaceMarkers.clear();
        }

    }

    @Override
    protected void onStart(){
        Log.d(TAG, "On Start");
        super.onStart();
        mFirebaseMaps = new Firebase(FIREBASE_URL).child("maps");
        mFirebaseMaps.authAnonymously(authMapsResultHandler);

        mFirebasePlaces = new Firebase(FIREBASE_URL).child("places");
        mFirebasePlaces.authAnonymously(authPlacesResultHandler);

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "on Resume");
        super.onResume();
        //mapFragment.getRetainInstance();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
        mFirebaseMapsListener = mFirebaseMaps.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                latlngbounds = new LatLngBounds.Builder();

                Log.d(TAG, "Firebase on Data Change : " + snapshot.getValue().toString());

                remove_all_map_markers(); // TODO : See if clearing and redrawing the map in main thread affects performance
                //mMap.clear();
                // Include The current user as part of the bounds
                latlngbounds.include(new LatLng(location.getLatitude(), location.getLongitude()));

                for (DataSnapshot entry : snapshot.getChildren()){
                    //Log.d(TAG, entry.getKey().toString());
                    if ( !UID.equals(entry.getKey())) {
                        //Log.d(TAG, "Updating Marker for : " + entry.getKey());
                        Coordinates latlng = entry.getValue(Coordinates.class);
                        LatLng userlatlng = new LatLng(latlng.getLatitude(), latlng.getLongitude());
                        latlngbounds.include(userlatlng);
                        Marker currentMarker;
                        currentMarker = mMap.addMarker(new MarkerOptions()
                                .position(userlatlng)
                                .title(entry.getKey()));

                        allMapMarkers.add(currentMarker);
                    }
                }

                latLngBoundsUpdate = latlngbounds.build();
                Log.d(TAG, latLngBoundsUpdate.toString());
                centroid = latLngBoundsUpdate.getCenter();
                Marker centroidMarker = mMap.addMarker(new MarkerOptions()
                        .position(centroid)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .title("Centroid"));
                allMapMarkers.add(centroidMarker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsUpdate, 130));

                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsUpdate, 130));
                        return true;
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d(TAG, "The read failed: " + firebaseError.getMessage());
            }
        });


        mFirebasePlacesListener = mFirebasePlaces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(TAG, "Firebase Places on Data Change");

                remove_all_place_markers();

                for (DataSnapshot entry : snapshot.getChildren()){
                    SimplePlace place = entry.getValue(SimplePlace.class);
                    LatLng placelatlng = new LatLng(place.getLatitude(), place.getLongitude());
                    Marker currentPlaceMarker = mMap.addMarker(new MarkerOptions()
                            .position(placelatlng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title(place.getName()));
                    allPlaceMarkers.add(currentPlaceMarker);
                }


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d(TAG, "The read failed: " + firebaseError.getMessage());
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        //mapFragment.setRetainInstance(true);
        Log.d(TAG, "on Pause");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        mFirebaseMaps.removeEventListener(mFirebaseMapsListener);
        mFirebasePlaces.removeEventListener(mFirebasePlacesListener);

    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.

            mapFragment = (SupportMapFragment) getSupportFragmentManager().
                    findFragmentById(R.id.map);
            //mapFragment.setRetainInstance(true);
            mMap = mapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                Log.d(TAG, "Map Obtained!");
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API) // Added Maps API
                .addApi(Places.GEO_DATA_API) // Added Places GEO DATA
                .addApi(Places.PLACE_DETECTION_API) // Added Place Detection API
                .build();
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, "In Handle New Location");
        Log.d(TAG, "Location : " + location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        Coordinates coords = new Coordinates(currentLatitude, currentLongitude);

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        Log.d(TAG, "Unique Device ID: " + UID);

        mFirebaseMaps.child(UID).setValue(coords);
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "On Connected Method");

        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "On Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }


    Firebase.AuthResultHandler authMapsResultHandler = new Firebase.AuthResultHandler() {
        @Override
        public void onAuthenticated(AuthData authData) {
            // Authenticated successfully with payload authData
            Log.d(TAG, "Firebase Maps Successful Authentication : " + authData.toString());
        }
        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            // Authenticated failed with error firebaseError
            Log.d(TAG, "Firebase Maps Unsuccessful Authentication : " + firebaseError.toString());
        }
    };

    Firebase.AuthResultHandler authPlacesResultHandler = new Firebase.AuthResultHandler() {
        @Override
        public void onAuthenticated(AuthData authData) {
            // Authenticated successfully with payload authData
            Log.d(TAG, "Firebase Places Successful Authentication : " + authData.toString());
        }
        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            // Authenticated failed with error firebaseError
            Log.d(TAG, "Firebase Places Unsuccessful Authentication : " + firebaseError.toString());
        }
    };



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "Saving Instance");
        //mapFragment.setRetainInstance(true);
        savedInstanceState.putParcelable("Location", location);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        Log.d(TAG, "Restoring values from bundle");
        if (savedInstanceState != null) {
            //mapFragment.getRetainInstance();
            if (savedInstanceState.keySet().contains("Location")) {
                location = savedInstanceState.getParcelable("Location");
            }
        }
    }
}
