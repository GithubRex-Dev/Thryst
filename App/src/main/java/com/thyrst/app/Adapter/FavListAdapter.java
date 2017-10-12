package com.thyrst.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thyrst.app.Helper.DatabaseHelper;
import com.thyrst.app.Helper.FirebaseImageLoader;
import com.thyrst.app.Object.Recipe;
import com.thyrst.app.R;

import java.io.IOException;
import java.util.List;

/**
 * Created by Rex on 6/30/2017.
 */

public class FavListAdapter extends ArrayAdapter<Recipe> {

    private static class ViewHolder {
        private CardView mCardView;
        private ImageView mImageView;
        private TextView mTextView;
        private ImageButton mImageButton;
    }

    private Context mContext;
    private int mLayoutResourceId;
    private List<Recipe> mFavList;
    private ConstraintLayout viewGroup;
    private ListView mListView;
    private FavListAdapter mFavListAdapter;

    public FavListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Recipe> objects, @NonNull ConstraintLayout viewGroup, @NonNull ListView mListView) {
        super(context, resource, objects);
        this.mContext = context;
        this.viewGroup = viewGroup;
        this.mListView = mListView;
        this.mLayoutResourceId = resource;
        this.mFavList = objects;
    }

    @Override
    public int getCount() {
        return mFavList.size();
    }

    @Override
    public Recipe getItem(int position) {
        return mFavList.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final FavListAdapter.ViewHolder itemViewHolder;
        final Recipe mRecipe = getItem(position);
        if (convertView == null) {
            // 获得列表项的布局
            LayoutInflater mInflater = ((Activity) mContext).getLayoutInflater();
            convertView = mInflater.inflate(mLayoutResourceId, parent, false);

            // 定义一个ViewHolder对象并对其中成员进行赋值
            itemViewHolder = new FavListAdapter.ViewHolder();
            itemViewHolder.mCardView = (CardView) convertView.findViewById(R.id.content_cdv);
            itemViewHolder.mImageButton = (ImageButton) convertView.findViewById(R.id.favourite_btn);
            itemViewHolder.mImageView = (ImageView) convertView.findViewById(R.id.cover_img);
            itemViewHolder.mTextView = (TextView) convertView.findViewById(R.id.name_txt);

            // 将该ViewHolder对象存储于该列表项视图相关的Tag里
            convertView.setTag(itemViewHolder);
        } else {
            // 该列表项视图非空时，从视图的Tag里获取ViewHolder对象
            itemViewHolder = (FavListAdapter.ViewHolder) convertView.getTag();
        }
        // 为该列表项异步加载封面图片
        new FavListAdapter.ImageLoader(itemViewHolder).execute(new Recipe[]{mRecipe});

        // 添加事件监听
        //点击收藏按钮触发以下事件
        itemViewHolder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemRemover(itemViewHolder,position);
                new FavListAdapter.FavListUpdate(itemViewHolder).execute(new Recipe[]{mRecipe});
            }
        });

        return convertView;
    }

    private void itemRemover(ViewHolder mViewHolder,int position) {
        final Animation animation = AnimationUtils.loadAnimation(mListView.getContext(), R.anim.splashfadeout);
        final Recipe mRecipe = getItem(position);
        mViewHolder.mImageView.startAnimation(animation);
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                //mFavList.remove(positon);
                mFavList.remove(mRecipe);
                mFavListAdapter = new FavListAdapter(mContext, mLayoutResourceId, mFavList, viewGroup, mListView);
                mListView.setAdapter(mFavListAdapter);
                //animation.cancel();
                if(mFavList.size()<1){
                    viewGroup.setVisibility(View.VISIBLE);
                }
            }
        }, 400);

        /*Recipe item = getItem(position);
        mFavList.remove(mRecipe);
        mFavListAdapter = new FavListAdapter(mContext, mLayoutResourceId, mFavList, viewGroup, mListView);
        mListView.setAdapter(mFavListAdapter);*/
    }

    // 定义异步图片加载类
    private class ImageLoader extends AsyncTask<Recipe,String,Boolean> {

        private FavListAdapter.ViewHolder viewHolder;
        private Recipe mRecipe;

        public ImageLoader(FavListAdapter.ViewHolder viewHolder){
            this.viewHolder = viewHolder;
        }
        @Override
        protected Boolean doInBackground(Recipe... params) {
            mRecipe = params[0];
            return DatabaseHelper.readRecipeFavouriteState(mContext,mRecipe.getRecipeID());
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            viewHolder.mTextView.setText(mRecipe.getRecipeName());
            // 使用图片路径和名称定义一个FirebaseStorage实例的引用
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/"+mRecipe.getRecipeCover());
            // 使用Gilde加载图像
            Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(viewHolder.mImageView);

            if (flag) {
                viewHolder.mImageButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            } else {
                viewHolder.mImageButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            }
        }
    }

    // 定义异步更新收藏列表类
    private class FavListUpdate extends AsyncTask<Recipe,String,Boolean>{

        private FavListAdapter.ViewHolder viewHolder;

        public FavListUpdate(FavListAdapter.ViewHolder viewHolder){
            this.viewHolder = viewHolder;
        }

        @Override
        protected Boolean doInBackground(Recipe... params) {

            String ID = params[0].getRecipeID();

            // 从SharePreference对象中获取按钮点击前的状态,并取反
            boolean isFav = !DatabaseHelper.readRecipeFavouriteState(mContext,ID);

            // 创建一个数据库连接
            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            try {
                dbHelper.connectDataBase();
            } catch (IOException e) {
                e.printStackTrace();
                dbHelper.close();
            }

            // 更新收藏列表
            try {
                dbHelper.updateFavList(params[0],isFav);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dbHelper.close();

            // 将当前对应的收藏状态保存于SharePreference对象中
            DatabaseHelper.saveRecipeFavouriteState(mContext,ID,isFav);
            return isFav;
        }

        @Override
        protected void onPostExecute(Boolean isFav) {
            if (isFav){
                this.viewHolder.mImageButton.setImageResource(R.drawable.ic_favorite_white_24dp); //收藏
            } else {
                this.viewHolder.mImageButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);// 取消
            }
        }

    }
}
