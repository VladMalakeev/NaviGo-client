package ga.navigo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;

import java.util.ArrayList;

import ga.navigo.help.ConnectToServer;
import ga.navigo.model.CityInfo;
import ga.navigo.model.MyGeocoder;
import ga.navigo.help.DBHelper;
import ga.navigo.help.LocationSingleton;
import ga.navigo.help.MyLocationListener;
import ga.navigo.retrofit.RetrofitManager;
import ga.navigo.retrofit.ServerAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ga.navigo.SplashActivity.LOG;
import static ga.navigo.SplashActivity.dbVersion;
import static ga.navigo.SplashActivity.writeCity;

public class InstallActivity extends AppCompatActivity implements MyLocationListener {

    private Button btnNext;
    private ListView list;
    ArrayList<String>  cities;
    ArrayAdapter<String> adapter;

    static  private String cityName;
    private  LocationSingleton singleton;
    static ServerAPI serverAPI;
    RetrofitManager manager;
    DBHelper dbHelper;
    SQLiteDatabase db;
    static public Context context;
    ConnectToServer server;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        Log.d(LOG, "переход в Install activity");

        context = InstallActivity.this;

        dbHelper = new DBHelper(InstallActivity.this);
        db = dbHelper.getWritableDatabase();
        btnNext = (Button) findViewById(R.id.next);
        btnNext.setEnabled(false);
        list = (ListView) findViewById(R.id.installListView);

        server = new ConnectToServer(this);
        manager = new RetrofitManager();
        serverAPI = manager.getServerAPI(this,"https://navigo.ga/");


        //извлекаем города
        getCity();

        View.OnClickListener oclBtnYes = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG,"качаем мыркеры");
                server.writeMarkerInfo(cityName);
                writeCity.putString("lastCity", String.valueOf(cityName)).commit();
                nextActivity();
            }
        };
        btnNext.setOnClickListener(oclBtnYes);
    }


    public void nextActivity() {
        singleton.disconnected();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }


    public void createList(String name){
        Log.d(LOG,"createlist");
        cities = dbHelper.viewCityNames(db);
        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, cities);
       list.setAdapter(adapter);
       registerForContextMenu(list);

      //  cities.add(0,currenCity);
       // adapter.notifyDataSetChanged();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                btnNext.setEnabled(true);
                TextView clicked = (TextView) itemClicked;
                cityName = clicked.getText().toString();
                Log.d(LOG,"click = " + cityName);
            }
        });
    }


    public void geocoder(Double lat, Double lng){
        serverAPI.geocoder(lat, lng).enqueue(new Callback<MyGeocoder>() {
            @Override
            public void onResponse(Call<MyGeocoder> call, Response<MyGeocoder> response) {
                Log.d(LOG,response.toString());
                MyGeocoder result = response.body();
                String currenCity = result.getCity();
                Log.d(LOG,"your city - "+ currenCity);
            }

            @Override
            public void onFailure(Call<MyGeocoder> call, Throwable t) {
                Log.e(LOG,t.getMessage());
            }
        });
    }


    public void getCity(){
        serverAPI.getCityList().enqueue(new Callback<ArrayList<CityInfo>>() {
            @Override
            public void onResponse(Call<ArrayList<CityInfo>> call, Response<ArrayList<CityInfo>> response) {
                Log.d(LOG,response.toString());
                ArrayList<CityInfo> cities = response.body();
                for (CityInfo city:cities) dbHelper.insertCity(db,city);
                //запускаем синглтон
                singleton = LocationSingleton.getInstance(InstallActivity.this);
                createList("city");
            }

            @Override
            public void onFailure(Call<ArrayList<CityInfo>> call, Throwable t) {
                Log.e(LOG,t.getMessage());
            }
        });
    }

    @Override
    public void viewLocation(Location location) {
       Double latitude = location.getLatitude();
       Double longitude = location.getLongitude();
        geocoder(latitude, longitude);


    }

    @Override
    public void updateLocation(Location location) {
        Log.d(LOG, "обновление координат");
    }

}
