package com.thyrst.app.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.thyrst.app.Adapter.ViewPagerAdapter;
import com.thyrst.app.Fragment.FavouritesFragment;
import com.thyrst.app.Fragment.HowToFragment;
import com.thyrst.app.Fragment.RecipesFragment;
import com.thyrst.app.Fragment.ShoppinglistFragment;
import com.thyrst.app.Helper.BottomNavigationViewHelper;
import com.thyrst.app.Helper.FireBaseHelper;
import com.thyrst.app.R;

/**
 * Created by Rex on 5/22/2017.
 */

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView; //
    private ViewPager viewPager;//ViewPager to contain fragments

    private MenuItem prevMenuItem;

    // Fragments
    private RecipesFragment recipesFragment;
    private HowToFragment howToFragment;
    private FavouritesFragment favouritesFragment;
    private ShoppinglistFragment shoppinglistFragment;


    @Override
    protected void onStart() {
        super.onStart();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.d("TAG", "Max memory is " + maxMemory + "KB");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        //Initializing the bottomNavigationView
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        // Making the bottom navigation switch in normal way
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        setupViewPager(viewPager);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_recipe:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.action_howto:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.action_favourites:
                                viewPager.setCurrentItem(2);
                                break;
                            case R.id.action_shoppinglist:
                                viewPager.setCurrentItem(3);
                                break;
                        }
                        return false;
                    }
                });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                Log.d("page", "onPageSelected: "+position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        recipesFragment = new RecipesFragment();
        howToFragment = new HowToFragment();
        favouritesFragment = new FavouritesFragment();
        shoppinglistFragment = new ShoppinglistFragment();
        adapter.addFragment(recipesFragment);
        adapter.addFragment(howToFragment);
        adapter.addFragment(favouritesFragment);
        adapter.addFragment(shoppinglistFragment);
        viewPager.setAdapter(adapter);
    }


}
