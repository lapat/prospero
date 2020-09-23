package com.coinflash.app.Service;

import org.springframework.social.InvalidAuthorizationException;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;

public class FBUserDetailsImpl implements FBuserDetails {

    @Override
    public User getUserDetails(String accessToken, String appName, String[] fetchFields) {
        Facebook facebook = new FacebookTemplate(accessToken, appName);
        User profile = null;
        try {
            profile = facebook.fetchObject("me", User.class, fetchFields);
        } catch (InvalidAuthorizationException e) {
            e.printStackTrace();
        }
        return profile;
    }
}
