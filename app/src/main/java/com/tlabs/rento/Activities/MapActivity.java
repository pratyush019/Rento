package com.tlabs.rento.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tlabs.rento.Helpers.CoordinateList;
import com.tlabs.rento.Helpers.GPSHelper;
import com.tlabs.rento.Helpers.Methods;
import com.tlabs.rento.R;

import java.util.ArrayList;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "MapActivity";
    private static final float DEFAULT_ZOOM = 15f;
    private GoogleMap mMap;
    Double latitude, longitude;
    ArrayList<? extends CoordinateList> gpsList;
    boolean fromHomeActivity;
    String SAvailable, SBrand;


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "MapActivity is ready here.");
        mMap = googleMap;


        if (Methods.hasGrantedLocationPermission(this)) {
            getCoordinates();
            mMap.setMyLocationEnabled(true);
        }


        //to set any custom features on the map.......
        //mMap.getUiSettings().
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        if (Methods.hasGrantedLocationPermission(this))
            initMap();
        latitude=getIntent().getDoubleExtra("lat",0.0);
        longitude=getIntent().getDoubleExtra("lon",0.0);
        fromHomeActivity=getIntent().getBooleanExtra("fromHomeActivity",false);
        gpsList =  getIntent().getParcelableArrayListExtra("listCoordinates");
        SAvailable=getIntent().getStringExtra("available");
        SBrand=getIntent().getStringExtra("brand");
    }
    private void getCoordinates() {
        GPSHelper gpsHelper = new GPSHelper(this);
        if (gpsHelper.canGetLocation()) {
            gpsHelper.getLocation();
            double lat=gpsHelper.getLatitude();
            if (lat!=0){
                moveCamera(new LatLng(gpsHelper.getLatitude(),gpsHelper.getLongitude()));
                if (latitude!=0){
                    createMarker(latitude,longitude,SBrand,SAvailable); // if from CycleDetails activity
                }
                if (fromHomeActivity) {               // if from HomeActivity
                    if (gpsList.size() != 0) {
                        for (int x = 0; x < gpsList.size(); x++) {
                            double curLat = gpsList.get(x).getmLatitude();
                            double curLong = gpsList.get(x).getmLongitude();
                            String brand=gpsList.get(x).getmBrand();
                            String available=gpsList.get(x).getmAvailable();
                            createMarker(curLat, curLong,brand,available);
                        }
                    }
                }
                mMap.setOnMarkerClickListener(this);
                // apply click listener on marker;


            }
            else getCoordinates();

        }
        else {
            Methods.displayLocationSettingsRequest(this,this);
        }
    }
    private void moveCamera(LatLng latLng){
        Log.d(TAG,"moveCamera moving camera to lat:"+latLng.latitude+" long:"+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapActivity.DEFAULT_ZOOM));
    }
    private void initMap() {
        Log.d(TAG,"Initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(MapActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==30){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                initMap();
            }
        }
    }

    protected void createMarker(double latitude, double longitude, String brand,String available) {

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(brand)
                .snippet(available));
        //  .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_directions_bike_24)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode==RESULT_OK) {
            Log.d("toggle call","switched");
            getCoordinates();
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}