package ga.navigo.retrofit;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by asus on 12.03.2018.
 */

public class RetrofitManager {
    private Context context;
    private ServerAPI serverAPI;
    public ServerAPI getServerAPI(Context context,String url) {
        this.context = context;
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        serverAPI = retrofit.create(ServerAPI.class);
        return serverAPI;
    }


}
