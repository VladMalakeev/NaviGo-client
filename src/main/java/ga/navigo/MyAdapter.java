package ga.navigo;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ga.navigo.fragment.CityListFragment;
import ga.navigo.fragment.IfragmentManager;
import ga.navigo.help.ConnectToServer;
import ga.navigo.help.MapDownloader;

import static ga.navigo.MapsActivity.fragmentManager;
import static ga.navigo.SplashActivity.prefCity;
import static ga.navigo.SplashActivity.writeCity;

/**
 * Created by asus on 17.03.2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    final  String LOG = "myLog";
    private List<String> data;
    private LayoutInflater mInflater;
    private Context context;
    private MapDownloader mapDownloader;

    public MyAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.context =context;
        this.data = data;

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.city_element, parent, false);
        mapDownloader = new MapDownloader(context);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        String viewCity = prefCity.getString("lastCity","");
        holder.mTextView.setText(data.get(position));
        holder.mLinearLayout.setElevation(2);

        if(viewCity.equals(data.get(position))){
            holder.mTextView.setTextColor(Color.RED);
        }

        View.OnClickListener downloadListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG, "download = "+data.get(position));
                mapDownloader.downloadArea(data.get(position));
                holder.mTextView.setTextColor(Color.BLUE);
                try {
                    new ConnectToServer(context).writeMarkerInfo(data.get(position));
                }
                catch (Exception e){
                    Log.d(LOG,e.toString());
                }
                writeCity.putString("lastCity", data.get(position)).commit();
                IfragmentManager ifragmentManager = new MapsActivity();
                ifragmentManager.closeFragment();
            }
        };
        View.OnClickListener clearListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG, "clear = "+data.get(position));
                mapDownloader.deleteArea(data.get(position));
                holder.mTextView.setTextColor(Color.BLUE);
                writeCity.putString("lastCity", data.get(position)).commit();
                IfragmentManager ifragmentManager = new MapsActivity();
                ifragmentManager.closeFragment();
            }
        };

        View.OnClickListener changeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mTextView.setTextColor(Color.BLUE);
                try {
                    new ConnectToServer(context).writeMarkerInfo(data.get(position));
                }
                catch (Exception e){
                    Log.d(LOG,e.toString());
                }

                Log.d(LOG, "chenge on = "+data.get(position));
                writeCity.putString("lastCity", data.get(position)).commit();
               IfragmentManager ifragmentManager = new MapsActivity();
               ifragmentManager.closeFragment();
            }
        };
         mapDownloader.existCity(holder,data.get(position));

        holder.download.setOnClickListener(downloadListener);
        holder.clear.setOnClickListener(clearListener);
        holder.mTextView.setOnClickListener(changeListener);


    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
       public TextView mTextView;
       public Button download, clear;
       public LinearLayout mLinearLayout;
       public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.nameItem);
            download = (Button) itemView.findViewById(R.id.downloadItem);
            clear = (Button) itemView.findViewById(R.id.clearItem);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.city_list_layout);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    // convenience method for getting data at click position
    String getItem(int id) {
        return data.get(id);
    }
}