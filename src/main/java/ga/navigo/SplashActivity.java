package ga.navigo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import ga.navigo.help.ConnectToServer;
import ga.navigo.help.LocationSingleton;
import ga.navigo.help.MyLocationListener;
import ga.navigo.model.CityInfo;
import ga.navigo.help.DBHelper;
import ga.navigo.model.MyGeocoder;
import ga.navigo.retrofit.RetrofitManager;
import ga.navigo.retrofit.ServerAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity  {

    final static String LOG = "myLog";
    public static int dbVersion =1;    //версия бд, default = 1
    boolean online;                     //проверка сети

   static public  SharedPreferences prefCity,prefVersion;               //файл вкоторый будем писать инфорацию о данных (кэш)
   static public  SharedPreferences.Editor writeCity, writeVersion;    //право на редактирование файла
    public ConnectToServer server;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        showButton(false);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.myprogressBar) ;
        Button button = (Button) findViewById(R.id.again);
        server = new ConnectToServer(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG,"Have tried again");
                recreate();
            }
        });

        online = isOnline(this);        //состояние сети
        prefCity = getSharedPreferences("lastCity",MODE_PRIVATE);
        writeCity = prefCity.edit();

        prefVersion = getSharedPreferences("dataBase",MODE_PRIVATE);
        writeVersion = prefVersion.edit();

        String getVersion = prefVersion.getString("dataBase","");

        //если у нас нет интернета и это первый запуск
        if(online == false && getVersion == ""){
            Log.e(LOG,"Первый запуск без интернета!");
            Toast.makeText(this, R.string.network1,Toast.LENGTH_SHORT).show();
            Toast.makeText(this, R.string.network2,Toast.LENGTH_SHORT).show();
            showButton(true);
        }
        // если есть интернет и это первый запуск
        else if(online == true && getVersion == ""){
            Log.d(LOG,"Первый запуск, есть интернет");
            writeVersion.putString("dataBase", String.valueOf(dbVersion)).commit();
            installActivity();

        }
        //если нет интернета и запуск не первый
        else if(online == false && getVersion != ""){
            Log.d(LOG,"Запуск без интернета, берем данные с бд");
            dbVersion = Integer.valueOf(getVersion);
            nextActivity();
        }
        //если естбь интернет и запуск не первый
        else if(online == true && getVersion != ""){
            Log.d(LOG,"Интернет есть, запрашиваем список городов");
            dbVersion = Integer.valueOf(getVersion) +1; //увеличиваем версию на 1
            server.getCity();
            writeVersion.putString("dataBase", String.valueOf(dbVersion)).commit();
            nextActivity();
        }
    }


    //переход к карте
    public void nextActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    //переход к installActivity
    public void installActivity() {
        Intent intent = new Intent(this, InstallActivity.class);
        startActivity(intent);
        finish();
    }

    //переход к ErrorActivity
    public void errorActivity() {
        Intent intent = new Intent(this, ErrorActivity.class);
        startActivity(intent);
        finish();
    }


    //проверка интернета
    public static boolean isOnline(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        return false;
    }
    private void showButton(boolean show) {
        Button button = (Button) findViewById(R.id.again);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.myprogressBar);
        if (show) progressBar.setVisibility(View.GONE); else progressBar.setVisibility(View.VISIBLE);
        if (show) button.setVisibility(View.VISIBLE); else button.setVisibility(View.INVISIBLE);
    }


}
