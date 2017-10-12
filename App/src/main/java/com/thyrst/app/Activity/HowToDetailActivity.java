package com.thyrst.app.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.thyrst.app.Object.HowTo;
import com.thyrst.app.R;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Rex on 7/14/2017.
 */

public class HowToDetailActivity extends AppCompatActivity {
    public static final String HOWTO = "howtoObj";
    private Toolbar mToolBar;
    private HowTo mHowToObj;
    private VideoView videoView;
    private ImageButton videoPlayBtn;
    private MediaController mediaController;
    private Uri videoUrl;
    private ListView mInstructionsList;
    private ListView mIngredientsList;
    private Bundle arg = new Bundle();

    private void initView(){

        setContentView(R.layout.activity_howto_detail);
        mToolBar = (Toolbar) findViewById(R.id.howto_toolbar);
        mToolBar.setTitle(mHowToObj.getHtName());
        mToolBar.setTitleTextColor(getResources().getColor(R.color.white));


        videoView = (VideoView) findViewById(R.id.video_view_howto);
        videoPlayBtn = (ImageButton) findViewById(R.id.play_button_howto);
        videoUrl = Uri.parse(mHowToObj.getHtVideo());
        videoView.setVideoURI(videoUrl);
        videoView.seekTo(400);

        mInstructionsList = (ListView)findViewById(R.id.instructions_list);
        mIngredientsList = (ListView)findViewById(R.id.ingredients_list);
        setSupportActionBar(mToolBar);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // to create a Media Controller without rewind and forward button
        mediaController = new MediaController(getApplicationContext(),false);
        mediaController.setAnchorView(videoView);
        mediaController.setBackgroundColor(getResources().getColor(R.color.transparent));
        videoView.setMediaController(mediaController);
        mediaController.hide();

        // to hide the pause and play button on Media Controller
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                LinearLayout viewGroupLevel1 = (LinearLayout)  mediaController.getChildAt(0);
                LinearLayout viewGroupLevel2 = (LinearLayout) viewGroupLevel1.getChildAt(0);
                View view = viewGroupLevel2.getChildAt(2);
                view.setVisibility(View.GONE);
            }
        });
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(videoView.isPlaying()){
                    videoView.pause();
                    videoPlayBtn.setVisibility(View.VISIBLE);
                    mediaController.show(0);
                }
                return false;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                videoPlayBtn.setVisibility(View.VISIBLE);
                mediaController.hide();
                videoView.seekTo(400);
            }
        });

        videoPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoView.isPlaying()) {
                    //videoView.seekTo(position);
                    videoView.start();
                    // hide play button once video starts
                    videoPlayBtn.setVisibility(View.GONE);
                    mediaController.hide();
                }
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        String howtoString = intent.getStringExtra(HOWTO);
        mHowToObj = HowTo.deserializeFromJson(howtoString);
        arg.putString("howtoObj",howtoString);
        initView();
        new HowToDetailsAccess(mIngredientsList,mInstructionsList).execute(new HowTo[]{mHowToObj});

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class IngredientsContentAdapter extends BaseAdapter {
        String[] ingredients;

        public IngredientsContentAdapter(String[] ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public int getCount() {
            return ingredients.length;
        }

        @Override
        public Object getItem(int position) {
            return ingredients[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View contentView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.ingredients_item, null);
            TextView itemContent = (TextView) contentView.findViewById(R.id.ingredient_content);

            itemContent.setText(ingredients[position]);
            return contentView;
        }
    }

    private class InstructionsContentAdapter extends BaseAdapter {
        String[] steps;

        public InstructionsContentAdapter(String[] steps) {
            this.steps = steps;
        }

        @Override
        public int getCount() {
            return steps.length;
        }

        @Override
        public Object getItem(int position) {
            return steps[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View contentView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.direction_item, null);
            TextView itemNumber = (TextView) contentView.findViewById(R.id.item_number);
            TextView itemContent = (TextView) contentView.findViewById(R.id.item_content);

            itemContent.setText(steps[position]);
            itemNumber.setText(String.valueOf(position+1));
            return contentView;
        }
    }

    // 定义异步获取详细信息
    private class HowToDetailsAccess extends AsyncTask<HowTo,String,String[][]> {
        private ListView mIngredientsListView;
        private ListView mInstructionsListView;

        public HowToDetailsAccess(ListView mListView1, ListView mListView2) {
            this.mIngredientsListView = mListView1;
            this.mInstructionsListView = mListView2;
        }

        @Override
        protected String[][] doInBackground(HowTo... params) {
            String IngredientsLine = "";
            String InstructionsLine = "";
            String[][] result = new String[2][];

            try {
                URL txtUrl = new URL(params[0].getHtIngredients());
                InputStream inputStream = txtUrl.openStream();
                Scanner sc;
                if (inputStream != null) {
                    sc = new Scanner(inputStream); // also has a constructor which take in a charsetName
                    while(sc.hasNextLine()) {
                        IngredientsLine += sc.nextLine();
                    }
                }
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            result[0] = IngredientsLine.split("\\|");

            try {
                URL txtUrl = new URL(params[0].getHtDirections());
                InputStream inputStream = txtUrl.openStream();
                Scanner sc;
                if (inputStream != null) {
                    sc = new Scanner(inputStream); // also has a constructor which take in a charsetName
                    while(sc.hasNextLine()) {
                        InstructionsLine += sc.nextLine();
                    }
                }
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            result[1] = InstructionsLine.split("\\|");

            return result;
        }

        @Override
        protected void onPostExecute(String[][] mLines) {
            mIngredientsListView.setAdapter(new IngredientsContentAdapter(mLines[0]));
            mInstructionsListView.setAdapter(new InstructionsContentAdapter(mLines[1]));
        }
    }
}
