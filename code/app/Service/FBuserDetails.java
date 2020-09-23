package com.coinflash.app.Service;

import org.springframework.social.facebook.api.User;
public interface FBuserDetails {
    User getUserDetails(String accessToken, String appName, String[] fetchFields);
}
