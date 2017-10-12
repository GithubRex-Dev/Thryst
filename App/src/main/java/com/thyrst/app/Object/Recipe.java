package com.thyrst.app.Object;

/**
 * Created by Rex on 6/28/2017.
 */

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Rex on 6/8/2017.
 */

@SuppressWarnings("serial")
public class Recipe implements Serializable {

    @SerializedName("id")
    private String recipeID;

    @SerializedName("name")
    private String recipeName;

    @SerializedName("type")
    private int recipeType;

    @SerializedName("rank")
    private int recipeRank;

    @SerializedName("brief")
    private String recipeBrief;

    @SerializedName("cover")
    private String recipeCover;

    @SerializedName("video")
    private String recipeVideo;

    @SerializedName("steps")
    private String recipeDirection;

    @SerializedName("igdts")
    private String recipeIngredients;

    public String getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public int getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(int recipeType) {
        this.recipeType = recipeType;
    }

    public int getRecipeRank() {
        return recipeRank;
    }

    public void setRecipeRank(int recipeRank) {
        this.recipeRank = recipeRank;
    }

    public String getRecipeBrief() {
        return recipeBrief;
    }

    public void setRecipeBrief(String recipeBrief) {
        this.recipeBrief = recipeBrief;
    }

    public String getRecipeCover() {
        return recipeCover;
    }

    public void setRecipeCover(String recipeCover) {
        this.recipeCover = recipeCover;
    }

    public String getRecipeVideo() {
        return recipeVideo;
    }

    public void setRecipeVideo(String recipeVideo) {
        this.recipeVideo = recipeVideo;
    }


    public String getRecipeDirection() {
        return recipeDirection;
    }

    public void setRecipeDirection(String recipeDirection) {
        this.recipeDirection = recipeDirection;
    }

    public String getRecipeIngredients() {
        return recipeIngredients;
    }

    public void setRecipeIngredients(String recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    // Serialize a single object.
    public static String serializeToJson(Recipe recipeObj) {
        Gson gson = new Gson();
        String j = gson.toJson(recipeObj);
        return j;
    }

    // Deserialize to single object.
    public static Recipe deserializeFromJson(String jsonString) {
        Gson gson = new Gson();
        Recipe recipeObj = gson.fromJson(jsonString, Recipe.class);
        return recipeObj;
    }
}
