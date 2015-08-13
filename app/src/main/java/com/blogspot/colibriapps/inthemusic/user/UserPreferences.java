package com.blogspot.colibriapps.inthemusic.user;

import com.vk.sdk.api.model.VKApiUser;

/**
 * Created by Dmitriy Gaiduk on 14.07.15.
 */
public class UserPreferences {
    private static final UserPreferences mUserPreferences = new UserPreferences();

    private boolean mShowCoverImage = true;

    private VKApiUser mVKApiUser;

    private UserPreferences(){

    }

    public static UserPreferences getInstance(){
        return  mUserPreferences;
    }

    public void reset(){
        mVKApiUser = null;
        mShowCoverImage = true;
    }

    public boolean isShowCoverImage(){
        return mShowCoverImage;
    }

    public void setShowCoverImage(boolean value){
         mShowCoverImage = value;
    }

    public void toggleShowCoverImage(){
        mShowCoverImage = !mShowCoverImage;
    }

    public VKApiUser getVKApiUser() {
        return mVKApiUser;
    }

    public void setVKApiUser(VKApiUser mVKApiUser) {
        this.mVKApiUser = mVKApiUser;
    }
}
