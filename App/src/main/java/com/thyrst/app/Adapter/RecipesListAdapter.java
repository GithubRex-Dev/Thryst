package com.thyrst.app.Adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thyrst.app.Activity.RecipeDetailActivity;
import com.thyrst.app.Helper.DatabaseHelper;
import com.thyrst.app.Helper.FirebaseImageLoader;
import com.thyrst.app.Object.Recipe;
import com.thyrst.app.R;

import java.io.IOException;
import java.util.List;

/**
 * Created by Rex on 6/28/2017.
 */

public class RecipesListAdapter extends ArrayAdapter<Recipe> {

    private static class ViewHolder {
        private CardView mCardView;
        private ImageView mImageView;
        private TextView mTextView;
        private ImageButton mImageButton;
    }

    private Context mContext;
    private int mLayoutResourceId;
    private List<Recipe> mRecipeList;

    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    public RecipesListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Recipe> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResourceId = resource;
        mRecipeList = objects;
    }

    @Override
    public int getCount() {
        return mRecipeList.size();
    }

    @Override
    public Recipe getItem(int position) {
        return mRecipeList.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder itemViewHolder;
        final Recipe mRecipe = getItem(position);
        if (convertView == null) {
            // 获得列表项的布局
            LayoutInflater mInflater = ((Activity) mContext).getLayoutInflater();
            convertView = mInflater.inflate(mLayoutResourceId, parent, false);

            // 定义一个ViewHolder对象并对其中成员进行赋值
            itemViewHolder = new ViewHolder();
            itemViewHolder.mCardView = (CardView) convertView.findViewById(R.id.content_cdv);
            itemViewHolder.mImageButton = (ImageButton) convertView.findViewById(R.id.favourite_btn);
            itemViewHolder.mImageView = (ImageView) convertView.findViewById(R.id.cover_img);
            itemViewHolder.mTextView = (TextView) convertView.findViewById(R.id.name_txt);

            // 将该ViewHolder对象存储于该列表项视图相关的Tag里
            convertView.setTag(itemViewHolder);
        } else {
            // 该列表项视图非空时，从视图的Tag里获取ViewHolder对象
            itemViewHolder = (ViewHolder) convertView.getTag();
        }
        // 为该列表项异步加载封面图片
        new ImageLoader(itemViewHolder).execute(new Recipe[]{mRecipe});

        // 添加事件监听
        //点击收藏按钮触发以下事件
        itemViewHolder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FavListUpdate(itemViewHolder).execute(new Recipe[]{mRecipe});
            }
        });

        // 进入菜单详细页面
        itemViewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailIntent = new Intent(mContext, RecipeDetailActivity.class);
                //Serialize a single Item Object.
                String jsonString = Recipe.serializeToJson(mRecipeList.get(position));
                detailIntent.putExtra(RecipeDetailActivity.RECIPE, jsonString);
                mContext.startActivity(detailIntent);
            }
        });

        return convertView;
    }

    // 定义异步图片加载类
    private class ImageLoader extends AsyncTask<Recipe,String,Boolean> {

        private ViewHolder viewHolder;
        private Recipe mRecipe;

        public ImageLoader(ViewHolder viewHolder){
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

        private ViewHolder viewHolder;

        public FavListUpdate(ViewHolder viewHolder){
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
            animateFavButton(viewHolder,isFav);
        }

    }

    public void animateFavButton(final ViewHolder itemViewHolder,boolean isFav){
        if(isFav){
            AnimatorSet animatorSet = new AnimatorSet();

            ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(itemViewHolder.mImageButton, "rotation", 0f, 360f);
            rotationAnim.setDuration(300);
            rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

            ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(itemViewHolder.mImageButton, "scaleX", 0.2f, 1f);
            bounceAnimX.setDuration(300);
            bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

            ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(itemViewHolder.mImageButton, "scaleY", 0.2f, 1f);
            bounceAnimY.setDuration(300);
            bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);

            bounceAnimY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    itemViewHolder.mImageButton.setImageResource(R.drawable.ic_favorite_white_24dp);
                }
            });

            animatorSet.play(rotationAnim);
            animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    itemViewHolder.mImageButton.setImageResource(R.drawable.ic_favorite_white_24dp);
                }
            });
            animatorSet.start();
        }else{
            itemViewHolder.mImageButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }
    }
}
