package ga.navigo;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;


import java.util.ArrayList;
import java.util.HashMap;

import ga.navigo.fragment.CityListFragment;
import ga.navigo.fragment.FullDescription;
import ga.navigo.fragment.IfragmentManager;
import ga.navigo.fragment.InfoWindow;
import ga.navigo.fragment.ItransferInfo;
import ga.navigo.help.DBHelper;
import ga.navigo.help.DrawerAdapter;
import ga.navigo.help.LocationSingleton;
import ga.navigo.help.MyLocationListener;
import ga.navigo.model.ItemModel;
import ga.navigo.model.MarkerInfo;

import static ga.navigo.SplashActivity.prefCity;



/**
 * Created by ASUS 553 on 11.03.2018.
 */

public class MapsActivity extends FragmentActivity implements ItransferInfo, IfragmentManager, MyLocationListener{

   public MapView mapView;
    private String[] mItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private Toolbar mToolbar;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    public static ProgressBar progressBar;
    public  static boolean isEndNotified;
   public static FragmentManager fragmentManager;

   public static DBHelper dbHelper;
   public static SQLiteDatabase db;

    public String viewCity;
   public static Fragment fragment;
    InfoWindow info;
    FullDescription fullDescription;
    CityListFragment cityListFragment;
    Button myLocation;
   public static LocationSingleton locationSingleton;
   public static MapboxMap mapboxMap;
   public static TextView downloadInfo;

    final static String LOG = "myLog";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.d(LOG,"start map activity");

        downloadInfo = (TextView) findViewById(R.id.downloadInfo);
        progressBar = (ProgressBar) findViewById(R.id.cityBar);
        progressBar.setVisibility(View.GONE);
        downloadInfo.setVisibility(View.GONE);
        fragmentManager = getSupportFragmentManager();
        dbHelper = new DBHelper(MapsActivity.this);
        db = dbHelper.getWritableDatabase();
        Mapbox.getInstance(getApplicationContext(), "pk.eyJ1IjoidmxhZDIwMjAiLCJhIjoiY2plaXJpZ3FkMzg2MzMzcGU1N2R6dnc4ayJ9.4MYEUWWdK6HYAFlnnDy7Jg");

        Log.d(LOG,"последний город = "+prefCity.getString("lastCity",""));
        viewCity = prefCity.getString("lastCity","");

        MapboxMapOptions options = new MapboxMapOptions()
                .styleUrl(Style.LIGHT)
                .camera(new CameraPosition.Builder()
                        .target(new LatLng(43.7383, 7.4094))
                        .zoom(22)
                        .build());

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                MapsActivity.this.mapboxMap = mapboxMap;
                Log.d(LOG, "map view ");
                setCamera(mapboxMap);
                 createMarkers(mapboxMap);

            }
        });


        mTitle = getTitle();
        mItemTitles = getResources().getStringArray(R.array.drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView) findViewById(R.id.left_drawer);

        setupToolbar();
        ItemModel[] dItems = fillDataModel();
        DrawerAdapter adapter = new DrawerAdapter(this, R.layout.item_row, dItems);
        mDrawerListView.setAdapter(adapter);
        mDrawerListView.setOnItemClickListener(new ItemClickListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setupDrawerToggle();

        myLocation = (Button) findViewById(R.id.location);
        final View.OnClickListener location = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG,"location");
                locationSingleton =  LocationSingleton.getInstance(MapsActivity.this);
            }
        };
        myLocation.setOnClickListener(location);

    }

    public void createMap(){
        String tempData = prefCity.getString("lastCity","");
        Log.d(LOG, "temp =" +tempData);
        if(viewCity!=tempData) {
            viewCity=tempData;
            Log.d(LOG, "map view ");
            setCamera(mapboxMap);
            createMarkers(mapboxMap);

        }
    }

    public void setCamera(MapboxMap mapboxMap){
        Log.d(LOG, "setCamera ");
        HashMap<String, String> cityData = dbHelper.viewCityData(db,viewCity);
        Double latitude = Double.valueOf(cityData.get("start_lat"));
        Double longetude = Double.valueOf(cityData.get("start_lon"));
        int zoom = Integer.valueOf(cityData.get("min_zoom"));

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(latitude, longetude))
                .zoom(zoom)
                .tilt(20)
                .build();
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    public void createMarkers(MapboxMap mapboxMap){
        Log.d(LOG, "viewCity = "+viewCity);
        try {
            ArrayList<MarkerInfo> markers = dbHelper.viewCityMarkers(db, viewCity);
            Log.d(LOG, "count = " + markers.size());
            for (int i = 0; i < markers.size(); i++) {

                Double lat = Double.valueOf(markers.get(i).getLatitude());
                Double lon = Double.valueOf(markers.get(i).getLongetude());
                String name = markers.get(i).getName();
                String type = markers.get(i).getType();
                Icon icon;
                switch (type){
                    case "park":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_park);
                    break;
                    case "rv_park":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_park);
                        break;
                    case "amusement_park":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_amusement);
                        break;
                    case "aquarium":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_aquarium);
                        break;
                    case "zoo":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_zoo);
                        break;
                    case "stadium":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_stadium);
                        break;
                    case "bowling_alley":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_bouling);
                        break;
                    case "movie_theater":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_cinema);
                        break;
                    case "museum":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_museum);
                        break;
                    case "art_gallery":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_gallery);
                        break;
                    case "church":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_church);
                        break;
                    case "synagogue":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_sinagogue);
                        break;
                    case "lodging":icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_hotel);
                        break;
                    default:icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.mipmap.ic_red);
                }

                 mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .title(name)
                        .snippet(type)
                        .icon(icon));
                MapboxMap.OnMarkerClickListener listener = new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                         info = new InfoWindow().newInstance(viewCity, marker.getTitle());
                        fragmentManager.beginTransaction().replace(R.id.mapLayout, info).commit();
                        return false;
                    }
                };
                mapboxMap.setOnMarkerClickListener(listener);

            }
            MapboxMap.OnMapClickListener mapListener = new MapboxMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng point) {
                    Log.d(LOG,"map click");
                    try {
                        fragmentManager.beginTransaction().hide(info).commit();
                    }
                    catch (Exception e)
                    {
                        Log.d(LOG,"error");
                    }
                }
            };
            mapboxMap.addOnMapClickListener(mapListener);

        }
        catch (Exception e){
            Log.d(LOG,"error");
        }
    }




    // формируем массив с данными для адаптера
    private ItemModel[] fillDataModel() {
        return new ItemModel[]{
                new ItemModel(R.drawable.map, "Map"),
                new ItemModel(R.drawable.manager, "City Manager"),
                new ItemModel(R.drawable.ic_settings, "Settings"),

        };
    }

    // по клику на элемент списка устанавливаем нужный фрагмент в контейнер
    private class ItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // на основании выбранного элемента меню
            // вызываем соответственный ему фрагмент
            switch (position) {
                case 0:
                    Log.d(LOG,"Выбран элемент 0");
                    fragmentManager.beginTransaction().hide(fragment = new CityListFragment()).commit();
                    createMap();

                    break;
                case 1:
                    Log.d(LOG,"Выбран элемент 1");
                    fragment = new CityListFragment();

                    break;
                default:
                    break;
            }
            if (fragment != null ){
                fragmentManager.beginTransaction().replace(R.id.mapLayout, fragment).addToBackStack("drawer").commit();
                mDrawerListView.setItemChecked(position, true);
                mDrawerListView.setSelection(position);
                setTitle(mItemTitles[position]);
                mDrawerLayout.closeDrawer(mDrawerListView);
            }
        }
    }

    @Override
    public void getFragmentInfo(String city, String marker){
        Log.d(LOG,"getFragmentInfo ");
        fullDescription = new FullDescription().newInstance(city,marker);
        fragmentManager.beginTransaction().replace(R.id.mapLayout, fullDescription).addToBackStack("descript").commit();

        mapView.onPause();
    }

    @Override
    public void closeFragment(){
        Log.d(LOG,"closeFragment");
       // fragmentManager.beginTransaction().disallowAddToBackStack();
       createMap();
    }

    @Override
    public void startProgress() {
        Log.d(LOG,"startBar");
        isEndNotified = false;
        progressBar.setIndeterminate(true);
       progressBar.setVisibility(View.VISIBLE);
        downloadInfo.setVisibility(View.VISIBLE);
    }
    @Override
    public void setPercentage(final int percentage, final long size) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
        downloadInfo.setText("downloaded: "+size/1000000+" mb");
    }
    @Override
    public void endProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) {
            return;
        }

        // Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
        downloadInfo.setVisibility(View.GONE);

        // Show a toast

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        // getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(mToolbar);
    }

    void setupDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        // Это необходимо для изменения иконки на основании текущего состояния
        mDrawerToggle.syncState();
    }
    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void viewLocation(Location location) {
    }

    @Override
    public void updateLocation(Location location) {
        Double latitude = location.getLatitude();
        Double longetude = location.getLongitude();
        Log.d(LOG, "обновление координат  " + latitude + "  " + longetude);
        Icon icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.drawable.ic_current_location);
        try {
            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longetude))
                    .title("location")
                    .snippet("text")
                    .icon(icon));

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longetude))
                    .zoom(16)
                    .tilt(20)
                    .build();
            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
            locationSingleton.disconnected();
        } catch (Exception e) {
            Log.d(LOG, "error location");
        }
    }
}
