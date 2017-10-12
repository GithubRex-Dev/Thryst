package com.thyrst.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.thyrst.app.Activity.ShoppingListAddActivity;
import com.thyrst.app.Adapter.ShoppingListAdapter;
import com.thyrst.app.Helper.DatabaseHelper;
import com.thyrst.app.Object.ShoppingList;
import com.thyrst.app.R;
import com.thyrst.app.View.CustomDialog;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Rex on 5/26/2017.
 */

public class ShoppinglistFragment extends Fragment implements AdapterView.OnItemClickListener{

    private Toolbar shoppingListToolbar;
    private ArrayList<ShoppingList> mShoppinglists;
    private ShoppingListAdapter mShoppingListAdapter;
    private FloatingActionButton mFabAdd;
    private ListView mListView;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);
        shoppingListToolbar = (Toolbar) contentView.findViewById(R.id.shopping_list_toolbar);
        shoppingListToolbar.setTitle("Shopping List");
        shoppingListToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ViewCompat.setElevation(shoppingListToolbar, 10);
        mListView = (ListView) contentView.findViewById(R.id.shopping_list);
        mListView.setOnItemClickListener(this);
        ViewCompat.setNestedScrollingEnabled(mListView, true);
        mFabAdd = (FloatingActionButton) contentView.findViewById(R.id.fab_add);
        new ShoppingListTask().execute(new Integer[]{0});

        mFabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View customView =View.inflate(mContext,R.layout.input_text_dialog,null);
                final EditText mEditText = (EditText) customView.findViewById(R.id.edit_text);
                CustomDialog.Builder dialog=new CustomDialog.Builder(mContext);
                dialog.setTitle("Create New List")
                        .setContentView(customView)//设置自定义customView
                        .setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String input = mEditText.getText().toString();
                                dialogInterface.dismiss();
                                if (input.equals("")) {
                                    Toast.makeText(mContext, "Please input List Name" + input, Toast.LENGTH_LONG).show();
                                }else {
                                    Intent addIntent = new Intent(mContext, ShoppingListAddActivity.class);
                                    addIntent.putExtra("title", input);
                                    startActivity(addIntent);
                                }
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            }
        });

        return contentView;

    }

    @Override
    public void onResume() {
        super.onResume();
        new ShoppingListTask().execute(new Integer[]{0});
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO
    }

    private class ShoppingListTask extends AsyncTask<Integer, Integer, ArrayList<ShoppingList>> {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        @Override
        protected ArrayList<ShoppingList> doInBackground(Integer... params) {
            try {
                dbHelper.connectDataBase();
            } catch (IOException e) {
                e.printStackTrace();
                dbHelper.close();
            }
            return dbHelper.getShoppingList();
        }

        @Override
        protected void onPostExecute(ArrayList<ShoppingList> mList) {
            mShoppinglists = mList;
            mShoppingListAdapter = new ShoppingListAdapter(mContext, R.layout.shopping_item, mShoppinglists, mListView);
            mListView.setAdapter(mShoppingListAdapter);
            dbHelper.close();
        }
    }
}
