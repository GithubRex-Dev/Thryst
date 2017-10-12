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

import com.thyrst.app.Fragment.IngredientsFragment;
import com.thyrst.app.Helper.DatabaseHelper;
import com.thyrst.app.R;
import com.thyrst.app.View.CustomDialog;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by Rex on 7/31/2017.
 */

public class ShoppingListAddActivity extends AppCompatActivity {
    public static final String TITLE = "title";
    public LayoutInflater mInflater;
    private ArrayList checkBoxs = new ArrayList();
    private LinearLayout linearLayout;
    private FloatingActionButton mFabItem;
    private Timestamp mTimestamp ; // 时间戳 标记购物清单
    private Toolbar toolBar;
    private Context mContext;
    private String checkedItems="";
    private String title;

    // 定义异步更新收藏列表类
    private class ShoppingListAdd extends AsyncTask<String,String,Boolean> {

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

            // 插入该购物清单置于本地数据库
            try {
                dbHelper.addShoppingList(params[0],params[1],params[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dbHelper.close();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean mBoolean) {
            Snackbar snackbar = Snackbar
                    .make(linearLayout, "NEW SHOPPINGLIST ADDED", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.WHITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.DKGRAY);
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.RED);
            snackbar.show();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoppinglist_add);
        linearLayout = (LinearLayout)findViewById(R.id.item_checkbox_add);
        mContext = this;
        Intent intent = this.getIntent();
        title = intent.getStringExtra(TITLE);

        mInflater = this.getLayoutInflater();
        mFabItem = (FloatingActionButton) findViewById(R.id.fab_item_new);

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
                                dialogInterface.dismiss();
                                if (input.equals("")) {
                                    Toast.makeText(mContext.getApplicationContext(), "Please input Item" + input, Toast.LENGTH_SHORT).show();
                                }else {
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
            // 记录插入本地数据库
                if(!checkedItems.equals("")){
                    mTimestamp = new Timestamp(System.currentTimeMillis());
                    new ShoppingListAdd().execute(new String[]{mTimestamp.toString(),title,checkedItems});
                }

            onBackPressed();
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initToolBar(){
        toolBar = (Toolbar) findViewById(R.id.sp_add_toolbar);
        toolBar.setTitle(title);
        toolBar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolBar);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

}
