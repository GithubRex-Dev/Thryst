package com.thyrst.app.Helper;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thyrst.app.Adapter.HowToListAdapter;
import com.thyrst.app.Adapter.RecipesListAdapter;
import com.thyrst.app.Object.HowTo;
import com.thyrst.app.Object.Recipe;
import com.thyrst.app.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rex on 6/28/2017.
 */

public class FireBaseHelper {
    private Context mContext;
    private ListView mRecipesListView;
    private ListView mHowToListView;
    private DatabaseReference mDatabase;
    private RecipesListAdapter mRecipesListAdapter;
    private HowToListAdapter mHowToListAdapter;
    private ArrayList<Recipe> mRecipesList = new ArrayList<Recipe>();
    private ArrayList<HowTo> mHowToList = new ArrayList<HowTo>();
    private static boolean isPersistenceEnabled = false;

    public FireBaseHelper(Context mContext,ListView mListView,String flag) {
        this.mContext = mContext;
        if(flag.equals("R"))
            this.mRecipesListView = mListView;
        else
            this.mHowToListView = mListView;

        if (!isPersistenceEnabled) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isPersistenceEnabled = true;
        }
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        //this.mDatabase.keepSynced(true);
    }

    public void refreshRecipesList(final CharSequence mCharSequence, final int mRecipeType, final char mRefreshMode){
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals("recipes list")) {
                    getUpdatedRecipeList(dataSnapshot);
                    loadRecipesToUI(mCharSequence, mRecipeType, mRefreshMode);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals("recipes list")) {
                    getUpdatedRecipeList(dataSnapshot);
                    loadRecipesToUI(mCharSequence, mRecipeType, mRefreshMode);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUpdatedRecipeList(DataSnapshot dataSnapshot){
        mRecipesList.clear();
        for(DataSnapshot ds:dataSnapshot.getChildren()){
            Recipe mRecipe = new Recipe();
            mRecipe.setRecipeID(ds.getValue(Recipe.class).getRecipeID());
            mRecipe.setRecipeType(ds.getValue(Recipe.class).getRecipeType());
            mRecipe.setRecipeName(ds.getValue(Recipe.class).getRecipeName());
            mRecipe.setRecipeBrief(ds.getValue(Recipe.class).getRecipeBrief());
            mRecipe.setRecipeCover(ds.getValue(Recipe.class).getRecipeCover());
            mRecipe.setRecipeVideo(ds.getValue(Recipe.class).getRecipeVideo());
            mRecipe.setRecipeDirection(ds.getValue(Recipe.class).getRecipeDirection());
            mRecipe.setRecipeIngredients(ds.getValue(Recipe.class).getRecipeIngredients());
            mRecipe.setRecipeRank(ds.getValue(Recipe.class).getRecipeRank());
            mRecipesList.add(mRecipe);
        }
    }

    private void loadRecipesToUI(CharSequence mCharSequence,int mRecipeType, char mRefreshMode){
        ArrayList<Recipe> mList = new ArrayList<Recipe>();
        if(mRecipesListAdapter != null)
            mRecipesListAdapter = null;

        switch (mRefreshMode){
            case 'D' : // dropdown
                mList = FilterHelper.performFilterByDropDown(mRecipesList, mRecipeType);
                break;
            case 'S' : // searchview
                mList = FilterHelper.performFilter(mRecipesList,mCharSequence);
                break;
        }

        mRecipesListAdapter = new RecipesListAdapter(mContext, R.layout.recipe_item, mList);
        mRecipesListView.setAdapter(mRecipesListAdapter);
    }

    public void refreshHowToList(){
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals("htlist")) {
                    getUpdatedHowToList(dataSnapshot);
                    loadHowToToItemUI();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals("htlist")) {
                    getUpdatedHowToList(dataSnapshot);
                    loadHowToToItemUI();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUpdatedHowToList(DataSnapshot dataSnapshot){
        mHowToList.clear();
        for(DataSnapshot ds:dataSnapshot.getChildren()){
            HowTo mHowTo = new HowTo();
            mHowTo.setHtID(ds.getValue(HowTo.class).getHtID());
            mHowTo.setHtName(ds.getValue(HowTo.class).getHtName());
            mHowTo.setHtCover(ds.getValue(HowTo.class).getHtCover());
            mHowTo.setHtVideo(ds.getValue(HowTo.class).getHtVideo());
            mHowTo.setHtDirections(ds.getValue(HowTo.class).getHtDirections());
            mHowTo.setHtIngerdients(ds.getValue(HowTo.class).getHtIngredients());
            mHowToList.add(mHowTo);
        }
    }

    private void loadHowToToItemUI(){
        if(mHowToListAdapter != null)
            mHowToListAdapter = null;
        mHowToListAdapter = new HowToListAdapter(mContext, R.layout.howto_item, mHowToList);
        mHowToListView.setAdapter(mHowToListAdapter);
    }

    //to download File From Cloud Storage by Andy
     public static void downloadFileFromCloudStorage(){

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference fileRef = storageRef.child("videos/watermelon_drink.mp4");

        File rootPath = new File(Environment.getExternalStorageDirectory(), "watermelon_drink");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        File localFile = new File(rootPath,"watermelon_drink.mp4");

        fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                System.out.print("Success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                System.out.print("Error");
            }
        });
    }
}
