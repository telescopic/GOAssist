package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

public class LocationFinder extends Service implements LocationListener  {

    private Context mContext;
    boolean checkGPS=false;
    boolean checkNetwork=false;
    boolean cangetlocation=false;
    Location loc;
    double latitude;
    double longitude;
    double time;
    private static final long min_dist=10,min_time=1000*60*1;

    protected LocationManager locationManager;


    public LocationFinder(Context mContext) {
        this.mContext = mContext;
        getLocation();
    }

    @SuppressLint("MissingPermission")
    private Location getLocation() {
        try {


            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // get GPS status
            checkGPS = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // get network provider status
            checkNetwork = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetwork) {
                Toast.makeText(mContext, "No Service Provider is available", Toast.LENGTH_SHORT).show();
            } else {
                this.cangetlocation = true;
                Toast.makeText(getApplicationContext(),"cangetloc",Toast.LENGTH_LONG).show();
                // if GPS Enabled get lat/long using GPS Services
                if (checkGPS) {

//                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
////                        // TODO: Consider calling
////                        //    ActivityCompat#requestPermissions
                    Toast.makeText(getApplicationContext(), "need gps", Toast.LENGTH_SHORT).show();
////
////                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            min_time,
                            min_dist,this);
                    if (locationManager != null) {
                        loc = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Toast.makeText(getApplicationContext(),"getting last",Toast.LENGTH_LONG).show();
                        if (loc != null) {
                            latitude = loc.getLatitude();
                            longitude = loc.getLongitude();
                            Toast.makeText(getApplicationContext(),"lattt"+latitude,Toast.LENGTH_LONG).show();
                        }
                    }


                }




            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return loc;
    }

    public double getLongitude() {
        if (loc != null) {
            longitude = loc.getLongitude();
        }
        return longitude;
    }

    public double getLatitude() {
        if (loc != null) {
            latitude = loc.getLatitude();

        }
        return latitude;
    }

    public double getTime()
    {
        if(loc!=null)
            time= loc.getTime();
        return time;
    }

    public boolean canGetLocation() {
        return this.cangetlocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);


        alertDialog.setTitle("GPS is not Enabled!");

        alertDialog.setMessage("Do you want to turn on GPS?");


        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });


        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        alertDialog.show();
    }


    public void stopListener() {
        if (locationManager != null) {

//            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                return;
//            }
            locationManager.removeUpdates(LocationFinder.this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

