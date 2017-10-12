package com.thyrst.app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.thyrst.app.Adapter.ViewPagerAdapter;
import com.thyrst.app.Fragment.DirectionsFragment;
import com.thyrst.app.Fragment.IngredientsFragment;
import com.thyrst.app.R;
import com.thyrst.app.Object.Recipe;

import java.io.File;

/**
 * Created by Rex on 5/29/2017.
 */

public class RecipeDetailActivity extends AppCompatActivity{

    public static final String RECIPE = "recipeObj";

    private Bundle arg = new Bundle();
    private TabLayout tabMenu;
    private ViewPager viewContent;
    private Toolbar toolBar;
    private MenuItem prevMenuItem;
    private DirectionsFragment directionsFragment;
    private IngredientsFragment ingredientsFragment;
    private Recipe recipeObj;
    private String[] tabTitles = {"INSTRUCTIONS","INGREDIENTS"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Intent intent = this.getIntent();
        String recipeString = intent.getStringExtra(RECIPE);
        recipeObj = Recipe.deserializeFromJson(recipeString);
        arg.putString("recipeObj",recipeString);

        tabMenu = (TabLayout) findViewById(R.id.tab_menu);
        viewContent = (ViewPager) findViewById(R.id.vp_content);
        initToolBar();
        setupViewPager(viewContent);
        initTab();
    }

    private void initToolBar(){
        toolBar = (Toolbar) findViewById(R.id.recipe_toolbar);
        toolBar.setTitle(recipeObj.getRecipeName());
        toolBar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolBar);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initTab(){
        tabMenu.setTabMode(TabLayout.MODE_FIXED);
        TabLayout.Tab tabDirections = tabMenu.newTab();
        TabLayout.Tab tabIngredients = tabMenu.newTab();
        tabMenu.addTab(tabDirections);
        tabMenu.addTab(tabIngredients);
        tabMenu.setTabTextColors(ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.white));
        tabMenu.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.yellow));
        ViewCompat.setElevation(tabMenu, 8);
        tabMenu.setupWithViewPager(viewContent);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        directionsFragment = new DirectionsFragment();
        ingredientsFragment = new IngredientsFragment();
        directionsFragment.setArguments(arg);
        ingredientsFragment.setArguments(arg);
        adapter.addFragment(directionsFragment);
        adapter.addFragment(ingredientsFragment);
        adapter.setPageTittle(tabTitles);
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_item_share:
                Intent  shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                String shareMsg = recipeObj.getRecipeBrief()+"\n";
                shareMsg += "Download Thyrst free:URL \n Enjoy your beverage!";

                //final File photoFile = new File(getFilesDir(), itemObj.getItemCover()+".png");
                //shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile));
                shareIntent.putExtra(Intent.EXTRA_SUBJECT , recipeObj.getRecipeName().toUpperCase());
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent,"Share this recipe to "));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
