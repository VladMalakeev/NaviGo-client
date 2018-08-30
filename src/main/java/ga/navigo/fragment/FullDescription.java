package ga.navigo.fragment;


import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ga.navigo.R;
import ga.navigo.help.DBHelper;
import ga.navigo.model.MarkerInfo;

/**
 * Created by ASUS 553 on 09.04.2018.
 */

public class FullDescription extends Fragment {
    final String LOG = "myLog";
    TextView markerName, description;
    ImageView imageView;
    String city, marker;
    DBHelper dbHelper;
    SQLiteDatabase db;
    int counter = 1;
    public static FullDescription newInstance( String city, String marker) {
        FullDescription fullDescription = new FullDescription();
        Bundle args = new Bundle();
        args.putString("city", city);
        args.putString("marker", marker);

        fullDescription.setArguments(args);
        return fullDescription;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        city = getArguments().getString("city", "");
        marker = getArguments().getString("marker", "");
        dbHelper = new DBHelper(getContext());
        db = dbHelper.getWritableDatabase();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.full_description_fragment, container, false);
        Log.d(LOG,"start fullDescription");
        MarkerInfo info = dbHelper.getMarkerInfo(db, city,marker);

        markerName = (TextView) view.findViewById(R.id.markerName);
        description =(TextView) view.findViewById(R.id.markerDescription);
        imageView = (ImageView) view.findViewById(R.id.markerView);

        markerName.setText(marker);
        description.setText(info.getDescription());
        nextImage();
        View.OnClickListener imageClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            nextImage();
            }
        };

        imageView.setOnClickListener(imageClick);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    public void nextImage(){
        if(counter==5){
            counter =0;
        }
        counter++;
        String url = "https://navigo.ga/photo/"+city+"/"+marker+"/"+counter+".jpg";
        Picasso.with(getContext())
                .load(url)
                .placeholder(R.drawable.ic_plaseholder)
                .error(R.drawable.ic_image_error)
                .into(imageView);
    }
}
