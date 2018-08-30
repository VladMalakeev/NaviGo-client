package ga.navigo.help;

/**
 * Created by ASUS 553 on 11.03.2018.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ga.navigo.InstallActivity;
import ga.navigo.MapsActivity;
import ga.navigo.SplashActivity;


public class LocationSingleton implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {
    private static Context mContext;
    private static LocationSingleton instance;
    private List<MyLocationListener> listeners;
    private GoogleApiClient mGoogleApiClient;
    private Location lastLocation;
    final static String LOG = "myLog";
    private long interval=10000;
    private long fastestInterval=5000;
    private int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;

    public static LocationSingleton getInstance(Context context)
    {
        mContext = context;
        if (instance == null)
        {
            instance = new LocationSingleton();
        }
        return instance;
    }
    private LocationSingleton()
    {
        listeners = new ArrayList<>();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                    .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

    }




    public void updateLocationRequest(LocationRequest locationrequest) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d(LOG,"Недостаточно разрешений");
            return;
        }
        interval = locationrequest.getInterval();
        fastestInterval = locationrequest.getFastestInterval();
        priority = locationrequest.getPriority();

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationrequest, this);
        Log.d(LOG,"Выполнен запрос на обновление параметров");
    }

    public void registerListener(MyLocationListener ... listeners)
    {
        this.listeners.addAll(Arrays.asList(listeners));
        Log.d(LOG,"Слушатели зарегистрированы");
    }
    public void unregisterListener(MyLocationListener ... listeners)
    {
        this.listeners.removeAll(Arrays.asList(listeners));
        Log.d(LOG,"Слушатели удалены");
    }

     public void disconnected(){
         mGoogleApiClient.disconnect();
     }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG,"Соединение установлено");
        if (ActivityCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(LOG,"Недостаточно разрешений");
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null)
        {
            MyLocationListener list = new MapsActivity();
           // MyLocationListener list = new InstallActivity();
            list.viewLocation(lastLocation);
        }
        else
        {
            Log.d(LOG, "Нет данных о последней локации");
        }

        LocationRequest mLastRequest = new LocationRequest();
        mLastRequest.setInterval(interval);
        mLastRequest.setFastestInterval(fastestInterval);
        mLastRequest.setPriority(priority);

        updateLocationRequest(mLastRequest);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (lastLocation != null)
        {
            MyLocationListener list = new MapsActivity();
           // MyLocationListener list = new InstallActivity();
            list.updateLocation(lastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG,"Соединение приостановлено");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG,"Соединение потеряно");
    }
}