package ga.navigo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ga.navigo.R;

/**
 * Created by ASUS 553 on 08.04.2018.
 */

public class InfoWindow extends Fragment {
    final String LOG = "myLog";
    TextView infotext;
    public ImageButton infoButton;
    ImageView infoImage;
    String city, marker;
    ItransferInfo itransferInfo;

    public static InfoWindow newInstance( String city, String marker) {
        InfoWindow infoFragment = new InfoWindow();
        Bundle args = new Bundle();
        args.putString("city", city);
        args.putString("marker", marker);

        infoFragment.setArguments(args);
        return infoFragment;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            itransferInfo = (ItransferInfo) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ItransferInfo");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        city = getArguments().getString("city", "");
        marker = getArguments().getString("marker", "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_window_fragment, container, false);
        Log.d(LOG,"start infoWindow");
        infotext = (TextView) view.findViewById(R.id.infoText);
        infoButton = (ImageButton) view.findViewById(R.id.infoButton);
        infoImage  = (ImageView) view.findViewById(R.id.infoImage);

        String url = "https://navigo.ga/photo/"+city+"/"+marker+"/1.jpg";
        infotext.setText(marker);
        Picasso.with(getContext())
                .load(url)
                .placeholder(R.drawable.ic_plaseholder)
                .error(R.drawable.ic_image_error)
                .into(infoImage);

        View.OnClickListener btnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG,"next");
                itransferInfo.getFragmentInfo(city,marker);
            }
        };
        infoButton.setOnClickListener(btnClick);

        return view;
    }

}
