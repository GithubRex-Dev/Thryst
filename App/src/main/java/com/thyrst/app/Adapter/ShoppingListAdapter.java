package com.thyrst.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.thyrst.app.Activity.ShoppingListEditActivity;
import com.thyrst.app.Helper.DatabaseHelper;
import com.thyrst.app.Object.ShoppingList;
import com.thyrst.app.R;
import com.thyrst.app.View.CustomDialog;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Rex on 7/9/2017.
 */

public class ShoppingListAdapter extends ArrayAdapter<ShoppingList> {
    private Context mContext;
    private int layoutResourceId;
    private LinearLayout linearLayout;
    private Toolbar mToolbar;
    private ImageButton mImageButton;
    private ListView mListView;
    private ArrayList<ShoppingList> mShoppingLists;
    private ArrayList checkBoxs = new ArrayList();
    private ShoppingListAdapter mShoppingListAdapter;

    public ShoppingListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<ShoppingList> objects, @NonNull ListView listview) {
        super(context, resource, objects);
        mContext = context;
        mShoppingLists = objects;
        layoutResourceId = resource;
        mListView = listview;
    }

    @Nullable
    @Override
    public ShoppingList getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater mInflater = ((Activity) mContext).getLayoutInflater();
        convertView = mInflater.inflate(layoutResourceId, parent, false);

        final ShoppingList mShoppinglist = mShoppingLists.get(position);
        final String[] Items = mShoppinglist.getSpListItem();
        String listName = mShoppinglist.getSpListName();

        linearLayout = (LinearLayout)convertView.findViewById(R.id.item_checkbox);
        mToolbar = (Toolbar)convertView.findViewById(R.id.item_toolbar);
        mToolbar.setTitle(listName);
        mToolbar.setTitleTextColor(mContext.getResources().getColor(R.color.white));

        mImageButton = (ImageButton) convertView.findViewById(R.id.option_btn);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu mPopup = new PopupMenu(mContext, v);
                //Inflating the Popup using xml file
                mPopup.getMenuInflater()
                        .inflate(R.menu.popup_menu, mPopup.getMenu());

                //registering popup with OnMenuItemClickListener
                mPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.edit:
                                {
                                    Intent editIntent = new Intent(mContext, ShoppingListEditActivity.class);
                                    //Serialize a single Item Object.
                                    String jsonString = ShoppingList.serializeToJson(mShoppinglist);
                                    editIntent.putExtra("shoppingListObj", jsonString);
                                    mContext.startActivity(editIntent);
                                }
                                break;
                            case R.id.share:
                                {
                                    Intent  shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("image/*");
                                    String shareMsg = "";
                                    for (int i = 0; i < Items.length; i++) {
                                        shareMsg += Items[i]+"\n";
                                    };
                                    shareMsg += "Download Thyrst free:URL \n Enjoy your beverage!";
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT , mShoppinglist.getSpListName().toUpperCase());
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    mContext.startActivity(Intent.createChooser(shareIntent,"Share this shoppinglist to "));
                                }
                                break;
                            case R.id.delete:
                                {
                                    View customView =View.inflate(mContext,R.layout.delete_dialog,null);
                                    CustomDialog.Builder dialog=new CustomDialog.Builder(mContext);
                                    dialog.setContentView(customView)//设置自定义customView
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // 删除该项，并刷新列表
                                                    dialogInterface.dismiss();
                                                    itemRemover(position);
                                                    new ShoppingListRemove().execute(new ShoppingList[]{mShoppinglist});
                                                }
                                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).create().show();
                                }
                                break;
                        }
                        return true;
                    }
                });
                mPopup.show(); //showing popup menu
            }
        });

        for (int i = 0; i < Items.length; i++) {
            CheckBox checkBox = (CheckBox) mInflater.inflate(
                    R.layout.checkbox, null);
            checkBox.setText(Items[i]);
            checkBox.setChecked(true);
            checkBox.setEnabled(false);
            checkBoxs.add(checkBox);
            linearLayout.addView(checkBox, i);
            if(!checkBox.isEnabled())
                checkBox.setTextColor(mContext.getResources().getColor(R.color.black));

        }
        return convertView;
    }

    private void itemRemover(int position) {
        final ShoppingList mShoppingList = getItem(position);
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                mShoppingLists.remove(mShoppingList);
                mShoppingListAdapter = new ShoppingListAdapter(mContext, layoutResourceId, mShoppingLists, mListView);
                mListView.setAdapter(mShoppingListAdapter);
            }
        }, 400);
    }

    // 定义异步更新收藏列表类
    private class ShoppingListRemove extends AsyncTask<ShoppingList,String,Boolean> {
        @Override
        protected Boolean doInBackground(ShoppingList... params) {
            // 创建一个数据库连接
            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            try {
                dbHelper.connectDataBase();
            } catch (IOException e) {
                e.printStackTrace();
                dbHelper.close();
            }
            // 在本地数据库删除该购物清单
            try {
                dbHelper.removeShoppingList(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dbHelper.close();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean mBoolean) {
            // To Do
        }

    }
}
