package ga.navigo.retrofit;

import java.util.ArrayList;
import java.util.List;

import ga.navigo.model.CityInfo;
import ga.navigo.model.MarkerInfo;
import ga.navigo.model.MyGeocoder;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by asus on 12.03.2018.
 */

public interface ServerAPI {
    public String city = null;

    @GET("/data/city_data.php")
    Call<ArrayList<CityInfo>> getCityList();
    @GET("/data/marker_data.php")
    Call<List<MarkerInfo>> getMarkerList(@Query("city") String city);
    @GET("/data/geocoder.php")
    Call<MyGeocoder> geocoder(@Query("lat") Double lat, @Query("lng") Double lng);
}
