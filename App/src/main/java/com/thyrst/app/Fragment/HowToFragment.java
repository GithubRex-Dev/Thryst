package com.thyrst.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.thyrst.app.Adapter.HowToListAdapter;
import com.thyrst.app.Helper.FireBaseHelper;
import com.thyrst.app.Object.HowTo;
import com.thyrst.app.R;

import java.util.ArrayList;

/**
 * Created by Rex on 5/26/2017.
 */

public class HowToFragment extends Fragment implements AdapterView.OnItemClickListener{

    private FireBaseHelper mFireBaseHelper;

    private Toolbar howToListToolbar;
    private ListView mListView;
    private Context mContext;
    private ArrayList mHowToList;
    private HowToListAdapter mHowToListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = getActivity();
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_howto, container, false);
        howToListToolbar = (Toolbar) contentView.findViewById(R.id.how_to_list_toolbar);
        howToListToolbar.setTitle("How To");
        howToListToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ViewCompat.setElevation(howToListToolbar, 10);
        mListView = (ListView) contentView.findViewById(R.id.list_howto);
        mListView.setOnItemClickListener(this);
        ViewCompat.setNestedScrollingEnabled(mListView, true);

        // 从Firebase获取数据
        mFireBaseHelper = new FireBaseHelper(mContext,mListView,"H");
        mFireBaseHelper.refreshHowToList();

        return contentView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO
    }

}
