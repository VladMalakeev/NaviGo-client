package ga.navigo.help;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONObject;
import java.util.HashMap;

import ga.navigo.MapsActivity;
import ga.navigo.MyAdapter;
import ga.navigo.R;
import ga.navigo.fragment.IfragmentManager;

/**
 * Created by ASUS 553 on 12.03.2018.
 */

public class MapDownloader {
    final  String LOG = "myLog";
    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;
    ProgressBar progressBar;
    private OfflineManager offlineManager;
    private OfflineRegion offlineRegion;
    String regionName;
    Boolean existCity=false;
    IfragmentManager mapsFragment;
   public MyAdapter.ViewHolder newHolder;


    public MapDownloader(Context context){
        this.context = context;
        offlineManager = OfflineManager.getInstance(context);
    }

    /*
    загружаем город на устройство
     */
    public void downloadArea(String city){
       mapsFragment = new MapsActivity();
       mapsFragment.startProgress();
        Log.d(LOG,"извлекаем данные города - " +city);
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        HashMap<String, String> cityData = dbHelper.viewCityData(db,city);
        Double northLat = Double.valueOf(cityData.get("north_lat"));
        Double southLat = Double.valueOf(cityData.get("south_lat"));
        Double eastLon = Double.valueOf(cityData.get("east_lon"));
        Double westLon = Double.valueOf(cityData.get("west_lon"));
        int zoomMin = Integer.valueOf(cityData.get("min_zoom"));
        int zoomMax = Integer.valueOf(cityData.get("max_zoom"));
        regionName = cityData.get("city_name");

        Log.d(LOG,"задаем рамку");
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(northLat, eastLon)) // Northeast
                .include(new LatLng(southLat, westLon)) // Southwest
                .build();

        Log.d(LOG,"устанавливае параметры региона");
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                Style.LIGHT,
                latLngBounds,
                zoomMin,
                zoomMax,
                context.getResources().getDisplayMetrics().density);

        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("city", regionName);
            String json = jsonObject.toString();
            metadata = json.getBytes("UTF-8");
        } catch (Exception exception) {
            Log.d(LOG, "Ошибка метаданных: " + exception.getMessage());
            metadata = null;
        }

        //делем запрос на скчивание
        offlineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                Log.d(LOG, "Регион создан: " + regionName);
                MapDownloader.this.offlineRegion = offlineRegion;
                launchDownload();
            }

            @Override
            public void onError(String error) {
                Log.e(LOG, "Error: " + error);
            }
        });
    }

    private void  launchDownload(){
        // визуально отслеживаем загрузку
        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                // расчитываем процент загрузки и отображаем в статусбаре
                double percentage = status.getRequiredResourceCount() >= 0
                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) : 0.0;

                if (status.isComplete()) {
                    // загрузка успешна
                    mapsFragment.endProgress("success");
                    Toast.makeText(context, "success", Toast.LENGTH_LONG).show();
                } else if (status.isRequiredResourceCountPrecise()) {
                    mapsFragment.setPercentage((int) Math.round(percentage),status.getCompletedResourceSize());
                }
                // текущее состояние
                Log.d(LOG, String.format("%s/%s resources; %s bytes downloaded.",
                        String.valueOf(status.getCompletedResourceCount()),
                        String.valueOf(status.getRequiredResourceCount()),
                        String.valueOf(status.getCompletedResourceSize())));
            }

            @Override
            public void onError(OfflineRegionError error) {
                //ошибка
                Log.d(LOG, "onError reason: " + error.getReason());
                Log.d(LOG, "onError message: " + error.getMessage());
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                // превышение лимита на занрузку
                Log.d(LOG, "Превышен лимит на скачивание: " + limit);
            }
        });
        //меняем состояние региона на "загружен"
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }

    public void existCity(MyAdapter.ViewHolder holder, String city){
        newHolder = holder;
        regionName = city;
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                if (offlineRegions == null || offlineRegions.length == 0) {
                    Log.d(LOG,"ненайдено ни одного города");
                }
                else {
                    for (OfflineRegion offlineRegion : offlineRegions) {
                      if(regionName.equals(getRegionName(offlineRegion))){
                          Log.d(LOG,regionName+" = true" );
                          newHolder.download.setEnabled(false);
                          newHolder.clear.setEnabled(true);
                          newHolder.download.setBackgroundResource(R.drawable.ic_action_download);
                          newHolder.clear.setBackgroundResource(R.drawable.ic_action_delete);
                      }else {
                          Log.d(LOG,regionName+" = false" );

                      }
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(LOG, "listOfflineRegions return error: " + error);
            }
        });

    }

    private String getRegionName(OfflineRegion offlineRegion) {
        String offlineRegionName;

        try {
            byte[] metadata = offlineRegion.getMetadata();
            String json = new String(metadata, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            offlineRegionName = jsonObject.getString("city");
        } catch (Exception exception) {
            Log.e(LOG, "Failed to decode metadata: " + exception.getMessage());
            offlineRegionName = null;
        }
        Log.e(LOG, "region name = " + offlineRegionName);
        return offlineRegionName;
    }


    public void deleteArea(String city){
        regionName = city;
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                if (offlineRegions == null || offlineRegions.length == 0) {
                    Log.d(LOG,"ненайдено ни одного города");
                }
                else {
                    int number = 0;
                    for (OfflineRegion offlineRegion : offlineRegions) {
                        if(regionName.equals(getRegionName(offlineRegion))){
                            Log.d(LOG, "удаляем "+regionName+" id = "+number);
                            offlineRegions[number].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
                                @Override
                                public void onDelete() {
                                   Log.d(LOG,"город "+regionName+" удален");
                                  // progressBar.setVisibility(View.INVISIBLE);
                                   // progressBar.setIndeterminate(false);
                                    Toast.makeText(context, "city is deleted", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(String error) {
                                   // progressBar.setVisibility(View.INVISIBLE);
                                  //  progressBar.setIndeterminate(false);
                                    Log.e(LOG, "ошибка удаления города : " + error);
                                }
                            });
                        }else {
                            Log.d(LOG,regionName+" = false" );

                        }
                        number++;
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(LOG, "listOfflineRegions return error: " + error);
            }
        });
    }


}
