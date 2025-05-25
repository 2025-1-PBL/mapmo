package com.pbl.mapmo.oauth2;

import java.util.Map;

// Naver 사용자 정보 처리
public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response != null) {
            this.attributes = response;
        }
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
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
        return (String) attributes.get("profile_image");
    }
}
