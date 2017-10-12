package com.thyrst.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.thyrst.app.Activity.MainActivity;
import com.thyrst.app.Adapter.FavListAdapter;
import com.thyrst.app.Helper.DatabaseHelper;
import com.thyrst.app.Object.Recipe;
import com.thyrst.app.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rex on 5/26/2017.
 */

public class FavouritesFragment extends Fragment implements AdapterView.OnItemClickListener{

    private Toolbar mFavToolbar;
    private ConstraintLayout mViewGroup;
    private TextView findMyFav;
    private ListView mListView;
    private Context mContext;
    private ArrayList mFavList;
    private FavListAdapter mFavListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_favourites, container, false);

        mFavToolbar = (Toolbar) contentView.findViewById(R.id.favourites_list_toolbar);
        mFavToolbar.setTitle("Favourites");
        mFavToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ViewCompat.setElevation(mFavToolbar, 10);
        mListView = (ListView) contentView.findViewById(R.id.list_favourites);
        mListView.setOnItemClickListener(this);
        ViewCompat.setNestedScrollingEnabled(mListView, true);

        mViewGroup = (ConstraintLayout)contentView.findViewById(R.id.view_group);

        findMyFav = (TextView)contentView.findViewById(R.id.bold_12sp);

        new FavListTask().execute(new Integer[]{0});

        // 待优化
        findMyFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(mContext, MainActivity.class);
                startActivity(homeIntent);
            }
        });

        return contentView;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO
    }

        private class FavListTask extends AsyncTask<Integer,Integer, ArrayList<Recipe>> {
            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            @Override
            protected ArrayList<Recipe> doInBackground(Integer... params) {
                try {
                    dbHelper.connectDataBase();
                } catch (IOException e) {
                    e.printStackTrace();
                    dbHelper.close();
                }
                return dbHelper.getFavList();
            }

            @Override
            protected void onPostExecute(ArrayList<Recipe> mRecipes) {
                mFavList = mRecipes;
                mFavListAdapter = new FavListAdapter(mContext, R.layout.recipe_item, mFavList, mViewGroup, mListView);

                if(mFavList.size()!=0)
                    mViewGroup.setVisibility(View.GONE);
                mListView.setAdapter(mFavListAdapter);

                dbHelper.close();
            }
        }
}
