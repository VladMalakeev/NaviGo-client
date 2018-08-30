package ga.navigo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import ga.navigo.MapsActivity;
import ga.navigo.MyAdapter;
import ga.navigo.R;
import ga.navigo.help.DBHelper;


/**
 * Created by ASUS 553 on 15.03.2018.
 */

public class CityListFragment extends Fragment  {
    final String LOG = "myLog";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FragmentManager fragmentManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.city_list_fragment, container, false);
        Log.d(LOG,"start cityListManager");

        DBHelper dbHelper = new DBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<String> cityNames = dbHelper.viewCityNames(db);
        for (String city:cityNames) Log.d(LOG,city+" viewed");

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.city_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new MyAdapter(getContext(), cityNames));
        IfragmentManager ifragmentManager = new MapsActivity();
        ifragmentManager.closeFragment();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


}
