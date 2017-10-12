package com.thyrst.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thyrst.app.Activity.HowToDetailActivity;
import com.thyrst.app.Activity.RecipeDetailActivity;
import com.thyrst.app.Helper.FirebaseImageLoader;
import com.thyrst.app.Object.HowTo;
import com.thyrst.app.Object.Recipe;
import com.thyrst.app.R;

import java.util.List;

/**
 * Created by Rex on 7/3/2017.
 */

public class HowToListAdapter extends ArrayAdapter<HowTo> {

    private static class ViewHolder {
        private CardView mCardView;
        private ImageView mImageView;
        private TextView mTextView;
    }

    private Context mContext;
    private int mLayoutResourceId;
    private List<HowTo> mHowToList;

    public HowToListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<HowTo> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResourceId = resource;
        mHowToList = objects;
    }

    @Override
    public int getCount() {
        return mHowToList.size();
    }

    @Nullable
    @Override
    public HowTo getItem(int position) {
        return mHowToList.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final HowToListAdapter.ViewHolder itemViewHolder;
        final HowTo mHowTo = getItem(position);
        if (convertView == null) {
            // 获得列表项的布局
            LayoutInflater mInflater = ((Activity) mContext).getLayoutInflater();
            convertView = mInflater.inflate(mLayoutResourceId, parent, false);

            // 定义一个ViewHolder对象并对其中成员进行赋值
            itemViewHolder = new HowToListAdapter.ViewHolder();
            itemViewHolder.mCardView = (CardView) convertView.findViewById(R.id.howto_cdv);
            itemViewHolder.mImageView = (ImageView) convertView.findViewById(R.id.howto_img);
            itemViewHolder.mTextView = (TextView) convertView.findViewById(R.id.howto_name);

            // 将该ViewHolder对象存储于该列表项视图相关的Tag里
            convertView.setTag(itemViewHolder);
        } else {
            // 该列表项视图非空时，从视图的Tag里获取ViewHolder对象
            itemViewHolder = (HowToListAdapter.ViewHolder) convertView.getTag();
        }
        // 为该列表项异步加载封面图片
        new HowToListAdapter.ImageLoader(itemViewHolder).execute(new HowTo[]{mHowTo});

        // 进入菜单详细页面
        itemViewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailIntent = new Intent(mContext, HowToDetailActivity.class);
                //Serialize a single Item Object.
                String jsonString = HowTo.serializeToJson(mHowToList.get(position));
                detailIntent.putExtra(HowToDetailActivity.HOWTO, jsonString);
                mContext.startActivity(detailIntent);
            }
        });

        return convertView;
    }

    // 定义异步图片加载类
    private class ImageLoader extends AsyncTask<HowTo,String,String> {

        private HowToListAdapter.ViewHolder viewHolder;
        private HowTo mHowTo;

        public ImageLoader(HowToListAdapter.ViewHolder viewHolder){
            this.viewHolder = viewHolder;
        }

        @Override
        protected String doInBackground(HowTo... params) {
            mHowTo = params[0];
            return mHowTo.getHtName();
        }

        @Override
        protected void onPostExecute(String name) {
            viewHolder.mTextView.setText(name);
            // 使用图片路径和名称定义一个FirebaseStorage实例的引用
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/"+mHowTo.getHtCover());
            // 使用Gilde加载图像
            Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(viewHolder.mImageView);
        }
    }
}
