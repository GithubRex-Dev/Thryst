package com.thyrst.app.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thyrst.app.Adapter.ShoppingListAdapter;
import com.thyrst.app.Fragment.IngredientsFragment;
import com.thyrst.app.Helper.DatabaseHelper;
import com.thyrst.app.Object.ShoppingList;
import com.thyrst.app.R;
import com.thyrst.app.View.CustomDialog;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Rex on 7/31/2017.
 */

public class ShoppingListEditActivity extends AppCompatActivity {

    public static final String SHOPPINGLIST = "shoppingListObj";
    public ShoppingList mShoppingList;
    public LayoutInflater mInflater;
    private ArrayList checkBoxs = new ArrayList();
    private LinearLayout linearLayout;
    private FloatingActionButton mFabItem;
    private Toolbar toolBar;
    private Context mContext;
    private String checkedItems="";

    // 定义异步更新收藏列表类
    private class ShoppingListUpdate extends AsyncTask<String,String,Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // 创建一个数据库连接
            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            try {
                dbHelper.connectDataBase();
            } catch (IOException e) {
                e.printStackTrace();
                dbHelper.close();
            }
            // 在本地数据库更新该购物清单
            try {
                dbHelper.updateShoppingList(params[0],params[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dbHelper.close();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean mBoolean) {
            Snackbar snackbar = Snackbar
                    .make(linearLayout, "SHOPPINGLIST UPDATED", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.WHITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.DKGRAY);
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.RED);
            snackbar.show();
        }

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoppinglist_edit);
        linearLayout = (LinearLayout)findViewById(R.id.item_checkbox_edit);
        mContext = this;
        Intent intent = this.getIntent();
        String shoppingListString = intent.getStringExtra(SHOPPINGLIST);
        mShoppingList = ShoppingList.deserializeFromJson(shoppingListString);

        String[] Items = mShoppingList.getSpListItem();
        mInflater = this.getLayoutInflater();
        mFabItem = (FloatingActionButton) findViewById(R.id.fab_item_add);

        mFabItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View customView =View.inflate(mContext,R.layout.input_text_dialog,null);
                final TextView mTextView = (TextView) customView.findViewById(R.id.title);
                final EditText mEditText = (EditText) customView.findViewById(R.id.edit_text);
                mTextView.setText("Add New Item");
                mEditText.setHint("Item");
                CustomDialog.Builder dialog=new CustomDialog.Builder(mContext);
                dialog.setContentView(customView)//设置自定义customView
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String input = mEditText.getText().toString();
                                if (input.equals("")) {
                                    Toast.makeText(mContext.getApplicationContext(), "Please input Item" + input, Toast.LENGTH_SHORT).show();
                                    dialogInterface.dismiss();
                                }else {
                                    dialogInterface.dismiss();
                                    CheckBox checkBox = (CheckBox) mInflater.inflate(
                                            R.layout.checkbox, null);
                                    checkBox.setText(input);
                                    checkBox.setChecked(true);
                                    checkBoxs.add(checkBox);
                                    linearLayout.addView(checkBox);
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

        initToolBar();
        initCheckList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            {
                int size = IngredientsFragment.findChildrenByClass(linearLayout, CheckBox.class).size();
                for (CheckBox checkBox : IngredientsFragment.findChildrenByClass(linearLayout, CheckBox.class)) {
                    size--;
                    if(checkBox.isChecked())
                        if(size == 0)
                            checkedItems += checkBox.getText().toString();
                        else
                            checkedItems += checkBox.getText().toString()+"|";
                };
            }

            if(!checkedItems.equals(""))
                new ShoppingListUpdate().execute(new String[]{mShoppingList.getSpListID(),checkedItems});
            else
                new ShoppingListRemove().execute(new ShoppingList[]{mShoppingList});

            onBackPressed();
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initToolBar(){
        toolBar = (Toolbar) findViewById(R.id.sp_edit_toolbar);
        toolBar.setTitle(mShoppingList.getSpListName());
        toolBar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolBar);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initCheckList(){
        String[] Items = mShoppingList.getSpListItem();
        for (int i = 0; i < Items.length; i++) {
            CheckBox checkBox = (CheckBox) mInflater.inflate(
                    R.layout.checkbox, null);
            checkBox.setText(Items[i]);
            checkBox.setChecked(true);
            checkBoxs.add(checkBox);
            linearLayout.addView(checkBox, i);
            if(!checkBox.isEnabled())
                checkBox.setTextColor(this.getResources().getColor(R.color.black));

        }
    }
}
