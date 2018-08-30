package ga.navigo.help;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ga.navigo.model.CityInfo;
import ga.navigo.model.MarkerInfo;

import static ga.navigo.SplashActivity.dbVersion;

/**
 * Created by ASUS 553 on 11.03.2018.
 */

public class DBHelper extends SQLiteOpenHelper {
    final String LOG = "myLog";

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "navigo", null, dbVersion);
        Log.d(LOG,"версия "+dbVersion );
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // метод срабатывает при саом первом запуске бд
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG,"onCreate");
        createTableCityData(db);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //метод срабатывает при повторном запуске, нужен для проверки версий бд
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(oldVersion < newVersion){
            Log.d(LOG, "старая версия - " + oldVersion + ", новая - " + newVersion);
            dropTable(db,"cityData");
            createTableCityData(db);
        }
        else {
            Log.d(LOG, "база не поменялась");
        }
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
    создать таблицу с маркерами
    */
    public void createTableMarkers(SQLiteDatabase db, String city){
        Log.d(LOG, "создание таблицы " + city+"Markers");
        db.execSQL("create table IF NOT EXISTS "+ city + "Markers ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "type text,"
                + "latitude text,"
                + "longetude text,"
                + "description text,"
                + "image text" + ");");
        viewTables(db);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
    создать таблицу со списком городов
    */
    private void createTableCityData(SQLiteDatabase db){
        Log.d(LOG, "создание таблицы cityData");
        db.execSQL("create table IF NOT EXISTS cityData ("
                + "id integer primary key autoincrement,"
                + "city_name text,"
                + "start_lat text,"
                + "start_lon text,"
                + "north_lat text,"
                + "east_lon text,"
                + "south_lat text,"
                + "west_lon text,"
                + "min_zoom integer,"
                + "max_zoom integer" + ");");
        viewTables(db);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
    добавить даные в таблицу маркеров конкретного города
    */
    public void insertMarker(SQLiteDatabase db, String city, MarkerInfo marker){
        ContentValues value = new ContentValues();
        value.put("name", marker.getName());
        value.put("type", marker.getType());
        value.put("latitude", marker.getLatitude());
        value.put("longetude", marker.getLongetude());
        value.put("description", marker.getDescription());
        value.put("image", marker.getImage());
        long rowID = db.insert(city+"Markers", null, value);
        Log.d(LOG, "Marker строка добавлена, id = " + rowID);

    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /*
    заполнить таблицу данными о городах
    */
    public void insertCity(SQLiteDatabase db,CityInfo city){
        ContentValues dataValue = new ContentValues();
        dataValue.put("city_name", city.getName());
        dataValue.put("start_lat", city.getStartLat());
        dataValue.put("start_lon", city.getStartLon());
        dataValue.put("north_lat", city.getNorthLat());
        dataValue.put("east_lon",  city.getEastLon());
        dataValue.put("south_lat", city.getSouthLat());
        dataValue.put("west_lon",  city.getWestLon());
        dataValue.put("min_zoom",  city.getMinZoom());
        dataValue.put("max_zoom",  city.getMaxZoom());

        long rowID = db.insert("cityData", null, dataValue);
        Log.d(LOG, "Город добавлен, id = " + rowID);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public ArrayList<String> viewCityNames(SQLiteDatabase db){
        Cursor c = db.query("cityData",null, null, null, null, null, null);

        ArrayList<String> data = new ArrayList<String>();

        if (c.getCount()==0) Log.e(LOG,"Table cityData is empty");
        while (c.moveToNext()) {

            String cityName = c.getString(c.getColumnIndex("city_name"));
            data.add(cityName);
        }
        return data;
    }
    /*
    вывод параметров конкретного города
    */
    public HashMap<String, String> viewCityData(SQLiteDatabase db, String city){
        Log.d(LOG, "get cityData");
        Cursor c = db.query("cityData",null, "city_name = ?", new String[] {city}, null, null, null);

        if (c.moveToFirst()) {
            HashMap<String,String> data = new HashMap<>();
            data.put("city_name", c.getString(c.getColumnIndex("city_name")));
            data.put("start_lat", c.getString(c.getColumnIndex("start_lat")));
            data.put("start_lon", c.getString(c.getColumnIndex("start_lon")));
            data.put("north_lat", c.getString(c.getColumnIndex("north_lat")));
            data.put("east_lon", c.getString(c.getColumnIndex("east_lon")));
            data.put("south_lat", c.getString(c.getColumnIndex("south_lat")));
            data.put("west_lon", c.getString(c.getColumnIndex("west_lon")));
            data.put("min_zoom", c.getString(c.getColumnIndex("min_zoom")));
            data.put("max_zoom", c.getString(c.getColumnIndex("max_zoom")));
            return data;
        }
        else{
            return null;
        }
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
    метод вернет все маркеры по заданому городу со всеми настройками
     */
    public ArrayList<MarkerInfo> viewCityMarkers(SQLiteDatabase db, String city) {
        Log.d(LOG, "start method");
        Cursor c = db.query(city+"Markers", null, null, null, null, null, null);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            //создадим список для набора маркеров, каждый элеент списка содержит hashMap с параетрами каждого маркера
            ArrayList<MarkerInfo> mapData = new ArrayList<>();
            do {
                MarkerInfo markerValue = new MarkerInfo();
                markerValue.setName(c.getString(c.getColumnIndex("name")));
                markerValue.setType(c.getString(c.getColumnIndex("type")));
                markerValue.setLatitude(c.getString(c.getColumnIndex("latitude")));
                markerValue.setLongetude(c.getString(c.getColumnIndex("longetude")));
                markerValue.setDescription(c.getString(c.getColumnIndex("description")));
                markerValue.setImage(c.getString(c.getColumnIndex("image")));
                mapData.add(markerValue);
            }
            while (c.moveToNext());
            return mapData;
        }
        else {
             Log.d(LOG, "0 rows");
        }
        c.close();
        return null;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /*
    вывод информаци конкретного маркера
     */
    public MarkerInfo getMarkerInfo(SQLiteDatabase db, String city, String name){
        Cursor c = db.query(city+"Markers",null, "name = ?", new String[] {name}, null, null, null);
        if (c.moveToFirst()) {
            MarkerInfo data = new MarkerInfo();
            data.setName(c.getString(c.getColumnIndex("name")));
            data.setDescription(c.getString(c.getColumnIndex("description")));
            data.setImage(c.getString(c.getColumnIndex("image")));
            return data;
        }
        else{
            return null;
        }
    }



    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /*
    удаление ненужной таблицы
    */
    public void dropTable(SQLiteDatabase db, String name){
        db.execSQL("drop table IF EXISTS "+ name);

    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
    очистить таблицу но не удалить
     */
    public  void cleanTable(SQLiteDatabase db, String name){
        db.execSQL("delete from "+name);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /*
    просмотреть какие таблицы есть в бд
    */
    public  void viewTables(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                Log.d(LOG, c.getString(0));
                c.moveToNext();
            }
        }
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

}
