package com.pbl.mapmo.domain.notification;

public class FcmTokenRequest {
    private String fcmToken;
    
    // 기본 생성자
    public FcmTokenRequest() {
    }
    
    // 게터와 세터
    public String getFcmToken() {
        return fcmToken;
    }
    
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}