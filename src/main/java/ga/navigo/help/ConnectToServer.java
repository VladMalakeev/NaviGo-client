package ga.navigo.help;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ga.navigo.model.CityInfo;
import ga.navigo.model.MarkerInfo;
import ga.navigo.model.MyGeocoder;
import ga.navigo.retrofit.RetrofitManager;
import ga.navigo.retrofit.ServerAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



/**
 * Created by ASUS 553 on 31.03.2018.
 */

public class ConnectToServer {
    final static String LOG = "myLog";
    Context context;
    RetrofitManager manager;
    ServerAPI serverAPI;

    DBHelper dbHelper;
    SQLiteDatabase db;
    static public String cityName;

    public ConnectToServer(Context context) {
        this.context = context;
        manager = new RetrofitManager();
        serverAPI = manager.getServerAPI(context, "https://navigo.ga/");

    }

    public void writeMarkerInfo(String city) {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        cityName = city;
        serverAPI.getMarkerList(cityName).enqueue(new Callback<List<MarkerInfo>>() {
            @Override
            public void onResponse(Call<List<MarkerInfo>> call, Response<List<MarkerInfo>> response) {
                Log.d(LOG, response.toString());
                List<MarkerInfo> markers = response.body();
                dbHelper.dropTable(db,cityName+"Markers");
                dbHelper.createTableMarkers(db, cityName);
                for (MarkerInfo marker : markers) {
                    dbHelper.insertMarker(db, cityName, marker);
                }

            }

            @Override
            public void onFailure(Call<List<MarkerInfo>> call, Throwable t) {
                Log.e(LOG, t.getMessage());
            }
        });
    }

    public void getCity() {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        serverAPI.getCityList().enqueue(new Callback<ArrayList<CityInfo>>() {
            @Override
            public void onResponse(Call<ArrayList<CityInfo>> call, Response<ArrayList<CityInfo>> response) {
                Log.d(LOG, response.toString());
                ArrayList<CityInfo> cities = response.body();
                for (CityInfo city : cities) dbHelper.insertCity(db, city);
            }

            @Override
            public void onFailure(Call<ArrayList<CityInfo>> call, Throwable t) {
                Log.e(LOG, t.getMessage());
            }
        });
    }


}
