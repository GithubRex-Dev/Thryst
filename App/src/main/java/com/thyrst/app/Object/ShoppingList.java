package com.thyrst.app.Object;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Rex on 7/8/2017.
 */

@SuppressWarnings("serial")
public class ShoppingList {

    @SerializedName("sid")
    private String spListID;

    @SerializedName("sitm")
    private String spListItems;

    @SerializedName("sname")
    private String spListName;

    private String[] spListItem;

    public String getSpListName() {
        return spListName;
    }

    public void setSpListName(String spListName) {
        this.spListName = spListName;
    }

    public String getSpListID() {
        return spListID;
    }

    public void setSpListID(String spListID) {
        this.spListID = spListID;
    }

    public String getSpListItems() {
        return spListItems;
    }

    public void setSpListItems(String spListItems) {
        this.spListItems = spListItems;
    }

    public String[] getSpListItem() {
        return spListItems.split("\\|");
    }

    // Serialize a single object.
    public static String serializeToJson(ShoppingList mObj) {
        Gson gson = new Gson();
        String j = gson.toJson(mObj);
        return j;
    }

    // Deserialize to single object.
    public static ShoppingList deserializeFromJson(String jsonString) {
        Gson gson = new Gson();
        ShoppingList mObj = gson.fromJson(jsonString, ShoppingList.class);
        return mObj;
    }
}
