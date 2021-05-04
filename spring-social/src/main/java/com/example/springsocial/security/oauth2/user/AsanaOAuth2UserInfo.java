package com.example.springsocial.security.oauth2.user;

import java.util.Map;

public class AsanaOAuth2UserInfo extends OAuth2UserInfo {

    public AsanaOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((String) attributes.get("gid"));
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        //TODO need to test and see if both required or not
        if (attributes.get("photo") instanceof Map) {
            return ((Map<String, String>) attributes.get("photo")).get("image_60x60");
        }
        return (String) attributes.get("photo");
    }
}
