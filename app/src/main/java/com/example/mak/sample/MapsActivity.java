package com.example.mak.sample;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    // Firebase Related
    private static final String FIREBASE_URL = "https://sweltering-inferno-3584.firebaseio.com/";
    private Firebase mFirebaseMaps;
    private ValueEventListener mFirebaseListener;
    private static String UID = Build.SERIAL;

    protected GoogleMap mMap;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    protected static final String TAG = "EasyMeet";
    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected Location location;
    private LatLngBounds.Builder latlngbounds;


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
    protected void onStart(){
        Log.d(TAG, "On Start");
        super.onStart();
        mFirebaseMaps = new Firebase(FIREBASE_URL).child("maps");
        mFirebaseMaps.authAnonymously(authResultHandler);

    }
    @Override
    protected void onResume() {
        Log.d(TAG, "on Resume");
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
        mFirebaseListener = mFirebaseMaps.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                latlngbounds = new LatLngBounds.Builder();
                Log.d(TAG, "Firebase on Data Change : " + snapshot.getValue().toString());
                mMap.clear(); // TODO : See if clearing and redrawing the map in main thread affects performance
                for (DataSnapshot entry : snapshot.getChildren()){
                    //Log.d(TAG, entry.getKey().toString());
                    if ( !UID.equals(entry.getKey())) {
                        Log.d(TAG, "Updating Marker for : " + entry.getKey());
                        Coordinates latlng = entry.getValue(Coordinates.class);
                        LatLng userlatlng = new LatLng(latlng.getLatitude(), latlng.getLongitude());
                        latlngbounds.include(userlatlng);
                        mMap.addMarker(new MarkerOptions()
                                .position(userlatlng)
                                .title(entry.getKey()));
                    }
                }
                Log.d(TAG, latlngbounds.build().toString());
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latlngbounds.build(),70));
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
        Log.d(TAG, "on Pause");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        mFirebaseMaps.removeEventListener(mFirebaseListener);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "Saving Instance");
        savedInstanceState.putParcelable("Location", location);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        Log.d(TAG, "Restoring values from bundle");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("Location")) {
                location = savedInstanceState.getParcelable("Location");
            }
        }
    }
}
