package com.thyrst.app.Fragment;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thyrst.app.Helper.DatabaseHelper;
import com.thyrst.app.Object.Recipe;
import com.thyrst.app.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

/**
 * Created by Rex on 5/29/2017.
 */

public class IngredientsFragment extends Fragment implements View.OnClickListener {
    private LinearLayout linearLayout;
    private LinearLayout linearCheckbox;
    private Button addToListBtn;
    private Recipe mRecipeObj;
    private String checkedItems="";
    private Timestamp mTimestamp ; // 时间戳 标记购物清单
    View.OnClickListener mOnClickListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRecipeObj = Recipe.deserializeFromJson(this.getArguments().getString("recipeObj"));

        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_ingredients, container, false);
        linearLayout = (LinearLayout)contentView.findViewById(R.id.linear_frame);
        linearCheckbox = (LinearLayout)contentView.findViewById(R.id.linear_checkboxes);
        addToListBtn = (Button)contentView.findViewById(R.id.btn_add_list);

        new RecipeIngredientsAccess(linearCheckbox).execute(new Recipe[]{mRecipeObj});

        addToListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = findChildrenByClass(linearLayout, CheckBox.class).size();
                for (CheckBox checkBox : findChildrenByClass(linearLayout, CheckBox.class)) {
                    size--;
                    if(checkBox.isChecked())
                        if(size == 0)
                            checkedItems += checkBox.getText().toString();
                        else
                            checkedItems += checkBox.getText().toString()+"|";
                }
                // 记录插入本地数据库
                mTimestamp = new Timestamp(System.currentTimeMillis());
                new ShoppingListAdd().execute(new String[]{mTimestamp.toString(),mRecipeObj.getRecipeName(),checkedItems});
            }
        });

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 创建一个数据库连接
                DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                try {
                    dbHelper.connectDataBase();
                } catch (IOException e) {
                    e.printStackTrace();
                    dbHelper.close();
                }

                // 取消插入该购物清单
                try {
                    dbHelper.UndoAddingList(mTimestamp.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dbHelper.close();
            }
        };

        return contentView;

    }

    @Override
    public void onClick(View v) {
    }


    // 定义异步更新收藏列表类
    private class ShoppingListAdd extends AsyncTask<String,String,Boolean>{

        @Override
        protected Boolean doInBackground(String... params) {
            // 创建一个数据库连接
            DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
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
                    .make(linearCheckbox, "NEW SHOPPINGLIST ADDED", Snackbar.LENGTH_LONG)
                    .setAction("Undo", mOnClickListener);
            snackbar.setActionTextColor(Color.WHITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.DKGRAY);
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.RED);
            snackbar.show();
        }

    }

    // 定义异步获取详细信息
    private class RecipeIngredientsAccess extends AsyncTask<Recipe,String,String[]> {
        private LinearLayout linearLayout;
        private ArrayList checkBoxs = new ArrayList();

        public RecipeIngredientsAccess(LinearLayout linearLayout) {
            this.linearLayout = linearLayout;
        }
        @Override
        protected String[] doInBackground(Recipe... params) {
            String line = "";
            try {
                URL txtUrl = new URL(params[0].getRecipeIngredients());
                InputStream inputStream = txtUrl.openStream();
                Scanner sc;
                if (inputStream != null) {
                    sc = new Scanner(inputStream); // also has a constructor which take in a charsetName
                    while(sc.hasNextLine()) {
                        line += sc.nextLine();
                    }
                }
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return line.split("\\|");
        }

        @Override
        protected void onPostExecute(String[] mLines) {
            for (int i = 0; i < mLines.length; i++) {
                CheckBox checkBox = (CheckBox) getActivity().getLayoutInflater().inflate(
                        R.layout.checkbox, null);
                checkBox.setText(mLines[i]);
                checkBox.setChecked(true);
                checkBoxs.add(checkBox);
                linearLayout.addView(checkBox, i);
            }
        }
    }

    public static <V extends View> Collection<V> findChildrenByClass(ViewGroup viewGroup, Class<V> clazz) {

        return gatherChildrenByClass(viewGroup, clazz, new ArrayList<V>());
    }

    private static <V extends View> Collection<V> gatherChildrenByClass(ViewGroup viewGroup, Class<V> clazz, Collection<V> childrenFound) {

        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {
            final View child = viewGroup.getChildAt(i);
            if (clazz.isAssignableFrom(child.getClass())) {
                childrenFound.add((V)child);
            }
            if (child instanceof ViewGroup) {
                gatherChildrenByClass((ViewGroup) child, clazz, childrenFound);
            }
        }

        return childrenFound;
    }
}