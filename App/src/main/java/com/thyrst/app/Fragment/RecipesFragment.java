package com.thyrst.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.thyrst.app.Helper.FireBaseHelper;
import com.thyrst.app.Object.DropdownItemObject;
import com.thyrst.app.R;
import com.thyrst.app.View.DropdownButton;
import com.thyrst.app.View.DropdownListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Rex on 5/26/2017.
 */

public class RecipesFragment extends Fragment implements AdapterView.OnItemClickListener,DropdownListView.Container{

    private FireBaseHelper mFireBaseHelper;

    private Toolbar recipeListToolbar;
    private MenuItem mMenuItem;
    private SearchView searchView;
    private DropdownButton chooseType;
    private DropdownListView dropdownType;
    private DropdownListView currentDropdownList;
    private ListView listContent;
    private View mask;
    private Context mContext;

    private Animation dropdown_in, dropdown_out, dropdown_mask_out;

    private List<DropdownItemObject> chooseTypeData = new ArrayList<>();//可选Recipe类型

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Inflate the layout for this fragment */
        View contentView = inflater.inflate(R.layout.fragment_recipes, container, false);
        mContext = getActivity();
        recipeListToolbar = (Toolbar) contentView.findViewById(R.id.recipe_list_toolbar);
        listContent = (ListView) contentView.findViewById(R.id.list_content);
        listContent.setOnItemClickListener(this);
        ViewCompat.setNestedScrollingEnabled(listContent, true);

        chooseType = (DropdownButton) contentView.findViewById(R.id.chooseType);
        dropdownType = (DropdownListView) contentView.findViewById(R.id.dropdownType);
        mask = contentView.findViewById(R.id.mask);

        // 自定义搜索视图
        ViewCompat.setElevation(recipeListToolbar, 10);
        recipeListToolbar.inflateMenu(R.menu.search_view);
        mMenuItem = recipeListToolbar.getMenu().findItem(R.id.menu_item_search);
        searchView = (SearchView) mMenuItem.getActionView();
        searchView.setQueryHint("Search Recipe");
        for (TextView textView : findChildrenByClass(searchView, TextView.class)) {
            textView.setTextColor(getResources().getColor(R.color.white));
            textView.setHintTextColor(getResources().getColor(R.color.yellow));
        }
        // 获取搜索栏文本清空按钮
        ImageView closeButton = (ImageView)searchView.findViewById(R.id.search_close_btn);

        // 读取下拉框动画效果
        dropdown_in = AnimationUtils.loadAnimation(mContext, R.anim.dropdown_in);
        dropdown_out = AnimationUtils.loadAnimation(mContext,R.anim.dropdown_out);
        dropdown_mask_out = AnimationUtils.loadAnimation(mContext,R.anim.dropdown_mask_out);

        // 从Firebase获取数据
        mFireBaseHelper = new FireBaseHelper(mContext,listContent,"R");
        mFireBaseHelper.refreshRecipesList("",readRecipeType(),'D');

        // 初始化下拉框
        initialzeDropdown();

        // 点击遮罩时触发以下事件
        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        // 滚动列表时触发以下事件
        listContent.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    InputMethodManager inputMethodManger = (InputMethodManager) getActivity().getSystemService(Activity
                            .INPUT_METHOD_SERVICE);
                    inputMethodManger.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        //打开和关闭搜索框 触发以下事件
        MenuItemCompat.setOnActionExpandListener(recipeListToolbar.getMenu().findItem(R.id.menu_item_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                hide();
                listContent.setAdapter(null);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setQuery("",false);
                searchView.clearFocus();
                mFireBaseHelper.refreshRecipesList("",readRecipeType(),'D');
                return true;
            }
        });

        // 根据输入文本进行实时检索
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                if(s.equals(""))
                    listContent.setAdapter(null);
                else {
                    mFireBaseHelper.refreshRecipesList(s,0,'S');
                }
                return false;
            }
        });

        // 点击搜索文本清空按钮 触发以下事件
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("recipe search", "Search close button clicked");
                //Find EditText view
                EditText et = (EditText) searchView.findViewById(R.id.search_src_text);
                //Clear the text from EditText view
                et.setText("");
                listContent.setAdapter(null);
            }
        });

        return contentView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO
    }

    // when selection of dropdown changed
    @Override
    public void onSelectionChanged(DropdownListView view) {
        mFireBaseHelper.refreshRecipesList("",view.current.id,'D');
        saveRecipeType(view.current.id);
    }

    //  下拉框初始化
    private void initialzeDropdown() {
        DropdownItemObject allRecipes = new DropdownItemObject("All Recipes", 0, "All Recipes");
        DropdownItemObject Smoothie = new DropdownItemObject("Smoothie", 1, "Smoothie");
        DropdownItemObject bubbleTea = new DropdownItemObject("Bubble Tea", 2, "Bubble Tea");

        resetDropdown();
        chooseTypeData.add(allRecipes);
        chooseTypeData.add(Smoothie);
        chooseTypeData.add(bubbleTea);

        dropdownType.bind(chooseTypeData, chooseType, (DropdownListView.Container) this, 0);
        dropdown_mask_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (currentDropdownList == null) {
                    resetDropdown();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        switch(readRecipeType()){
            case 0:
                dropdownType.current = allRecipes;
                break;
            case 1:
                dropdownType.current = Smoothie;
                break;
            case 2:
                dropdownType.current = bubbleTea;
                break;
        }

        dropdownType.flush();
        saveRecipeType(dropdownType.current.id);
    }

    //重置下拉框
    public void resetDropdown() {
        chooseTypeData.clear();
        chooseType.setChecked(false);

        dropdownType.setVisibility(View.GONE);
        mask.setVisibility(View.GONE);

        dropdownType.clearAnimation();
        mask.clearAnimation();
    }

    //展开下拉框
    public void show(DropdownListView view) {
        if (currentDropdownList != null) {
            currentDropdownList.clearAnimation();
            currentDropdownList.startAnimation(dropdown_out);
            currentDropdownList.setVisibility(View.GONE);
            currentDropdownList.button.setChecked(false);
        }
        currentDropdownList = view;
        mask.clearAnimation();
        mask.setVisibility(View.VISIBLE);
        currentDropdownList.clearAnimation();
        currentDropdownList.startAnimation(dropdown_in);
        currentDropdownList.setVisibility(View.VISIBLE);
        currentDropdownList.button.setChecked(true);
    }

    // 收起下拉框
    public void hide() {
        if (currentDropdownList != null) {
            currentDropdownList.clearAnimation();
            currentDropdownList.startAnimation(dropdown_out);
            currentDropdownList.button.setChecked(false);
            mask.clearAnimation();
            mask.startAnimation(dropdown_mask_out);
        }
        currentDropdownList = null;
    }

    public void saveRecipeType(int type) {
        SharedPreferences aSharedPreferenes = mContext.getSharedPreferences(
                "Type", Context.MODE_PRIVATE);
        SharedPreferences.Editor aSharedPreferenesEdit = aSharedPreferenes
                .edit();
        aSharedPreferenesEdit.remove("RecipeType");
        boolean success = aSharedPreferenesEdit.commit();
        if (success) {
            aSharedPreferenesEdit.putInt("RecipeType", type);
            aSharedPreferenesEdit.commit();
        }
    }

    public int readRecipeType() {
        SharedPreferences aSharedPreferenes = mContext.getSharedPreferences(
                "Type", Context.MODE_PRIVATE);
        return aSharedPreferenes.getInt("RecipeType", 0);
    }

    // 获得子视图
    public static <V extends View> Collection<V> findChildrenByClass(ViewGroup viewGroup, Class<V> clazz) {
        return gatherChildrenByClass(viewGroup, clazz, new ArrayList<V>());
    }

    // 遍历视图子节点
    private static <V extends View> Collection<V> gatherChildrenByClass(ViewGroup viewGroup, Class<V> clazz, Collection<V> childrenFound) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
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
