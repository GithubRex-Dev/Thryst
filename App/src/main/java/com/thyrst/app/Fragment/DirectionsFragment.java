package com.thyrst.app.Fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import com.thyrst.app.Object.Recipe;
import com.thyrst.app.R;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


/**
 * Created by Rex on 5/29/2017.
 */

public class DirectionsFragment extends Fragment{

    private ImageButton videoPlayBtn;
    private VideoView videoView;
    private MediaController mediaController;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private ListView content_list;
    private ScrollView scrollView;
    private Recipe mRecipeObj;
    private String[] mSteps;
    private Uri videoUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRecipeObj = Recipe.deserializeFromJson(this.getArguments().getString("recipeObj"));

        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_directions, container, false);
        videoView = (VideoView) contentView.findViewById(R.id.video_view);
        videoPlayBtn = (ImageButton) contentView.findViewById(R.id.play_button);
        videoUrl = Uri.parse(mRecipeObj.getRecipeVideo());
        videoView.setVideoURI(videoUrl);
        videoView.seekTo(400);

        // 将滚动条置顶
        scrollView = (ScrollView)contentView.findViewById(R.id.scroll_view);
        scrollView.smoothScrollTo(0, 0);
        titleTextView = (TextView) contentView.findViewById(R.id.tv_title);
        titleTextView.setText(mRecipeObj.getRecipeName());
        descriptionTextView = (TextView)contentView.findViewById(R.id.tv_description);
        descriptionTextView.setText(mRecipeObj.getRecipeBrief());

        content_list = (ListView)contentView.findViewById(R.id.content_list);

        // to create a Media Controller without rewind and forward button
        mediaController = new MediaController(getActivity(),false);
        mediaController.setAnchorView(videoView);
        mediaController.setBackgroundColor(getResources().getColor(R.color.transparent));
        videoView.setMediaController(mediaController);
        mediaController.hide();

        new RecipeDetailsAccess(content_list).execute(new Recipe[]{mRecipeObj});

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

        return contentView;
    }

    // pause the video when switching tabs
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //onResume
            if(null!=videoView) {
                if(videoPlayBtn.getVisibility()==View.GONE)
                    videoView.start();
            }
        } else {
            //onPause
            if(null!=videoView){
                videoView.pause();
                mediaController.hide();
            }
        }
    }

    private class ContentAdapter extends BaseAdapter {
        String[] steps;

        public ContentAdapter(String[] steps) {
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
            View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.direction_item, null);
            TextView itemNumber = (TextView) contentView.findViewById(R.id.item_number);
            TextView itemContent = (TextView) contentView.findViewById(R.id.item_content);

            itemContent.setText(steps[position]);
            itemNumber.setText(String.valueOf(position+1));
            return contentView;
        }
    }

    // 定义异步获取详细信息
    private class RecipeDetailsAccess extends AsyncTask<Recipe,String,String[]> {
        private ListView mListView;

        public RecipeDetailsAccess(ListView mListView) {
            this.mListView = mListView;
        }

        @Override
        protected String[] doInBackground(Recipe... params) {
            String line = "";
            try {
                    URL txtUrl = new URL(params[0].getRecipeDirection());
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
            mListView.setAdapter(new ContentAdapter(mLines));
        }
    }

}
