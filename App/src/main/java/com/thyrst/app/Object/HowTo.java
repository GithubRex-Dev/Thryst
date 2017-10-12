package com.thyrst.app.Object;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Rex on 7/2/2017.
 */

public class HowTo {
    @SerializedName("id")
    private String htID;
    @SerializedName("name")
    private String htName;
    @SerializedName("cover")
    private String htCover;
    @SerializedName("video")
    private String htVideo;
    @SerializedName("steps")
    private String htDirections;
    @SerializedName("igdts")
    private String htIngredients;

    public String getHtID() {
        return htID;
    }

    public void setHtID(String htID) {
        this.htID = htID;
    }

    public String getHtName() {
        return htName;
    }

    public void setHtName(String htName) {
        this.htName = htName;
    }

    public String getHtCover() {
        return htCover;
    }

    public void setHtCover(String htCover) {
        this.htCover = htCover;
    }

    public String getHtVideo() {
        return htVideo;
    }

    public void setHtVideo(String htVideo) {
        this.htVideo = htVideo;
    }

    public String getHtDirections() {
        return htDirections;
    }

    public void setHtDirections(String htDirections) {
        this.htDirections = htDirections;
    }

    public String getHtIngredients() {
        return htIngredients;
    }

    public void setHtIngerdients(String htIngredients) {
        this.htIngredients = htIngredients;
    }

    // Serialize a single object.
    public static String serializeToJson(HowTo howtoObj) {
        Gson gson = new Gson();
        String j = gson.toJson(howtoObj);
        return j;
    }

    // Deserialize to single object.
    public static HowTo deserializeFromJson(String jsonString) {
        Gson gson = new Gson();
        HowTo howtoObj = gson.fromJson(jsonString, HowTo.class);
        return howtoObj;
    }
}
