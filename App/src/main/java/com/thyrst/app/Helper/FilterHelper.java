package com.thyrst.app.Helper;

import java.util.ArrayList;
import java.util.List;
import com.thyrst.app.Object.Recipe;

/**
 * Created by Rex on 6/12/2017.
 */

public class FilterHelper{

    public static ArrayList<Recipe> performFilter(ArrayList<Recipe> currentList, CharSequence constraint){
        if(constraint!=null && constraint.length() > 0){
            constraint = constraint.toString().toLowerCase();
            ArrayList<Recipe> foundlist = new ArrayList<>();
            for(Recipe mRecipe: currentList){
                String itemName = mRecipe.getRecipeName().toLowerCase();
                if(itemName.contains(constraint))
                    foundlist.add(mRecipe);
            }
            currentList = foundlist;
        }
        return currentList;

    }

    public static ArrayList<Recipe> performFilterByDropDown(ArrayList<Recipe> currentList, int type){
        if(type !=0){
            ArrayList<Recipe> foundlist = new ArrayList<>();
            for(Recipe mRecipe: currentList){
                int mRecipeType = mRecipe.getRecipeType();
                if(mRecipeType == type)
                    foundlist.add(mRecipe);
            }
            currentList = foundlist;
        }
        return currentList;
    }

}
