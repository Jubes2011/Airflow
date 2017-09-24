package com.airstem.airflow.ayush.airflow.model.search;

import com.airstem.airflow.ayush.airflow.helpers.collection.CollectionConstant;
import com.airstem.airflow.ayush.airflow.model.collection.CollectionTrack;

import java.io.Serializable;

/**
 * Created by mcd-50 on 8/7/17.
 */

public class SearchRadio  implements Serializable  {
    private String mTitle;
    private String mMaxUser;
    private String[] mStreamUrl;
    private String[] mTags;
    private String mCountry;
    private String mColor;


    public SearchRadio(String mTitle, String mMaxUser, String[] mStreamUrl, String[] mTags, String mCountry, String mColor) {
        this.mTitle = mTitle;
        this.mMaxUser = mMaxUser;
        this.mStreamUrl = mStreamUrl;
        this.mTags = mTags;
        this.mCountry = mCountry;
        this.mColor = mColor;
    }


    public String getTitle() {
        return mTitle;
    }

    public String getUser() {
        return mMaxUser;
    }

    public String[] getStreamUrl() {
        return mStreamUrl;
    }

    public String[] getTags() {
        return mTags;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getColor() {
        int titleLength = this.mTitle.length();
        int colorArrayLength = CollectionConstant.COLOR_ARRAY.size();
        if(titleLength > 0){
            return CollectionConstant.COLOR_ARRAY.get(titleLength % colorArrayLength);
        }
        return CollectionConstant.COLOR_ARRAY.get(0);
    }
}
