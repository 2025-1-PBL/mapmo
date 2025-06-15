package com.pbl.mapmo.domain.notification;

// 클라이언트에서 보내는 연결 요청 객체
public class NotificationConnectRequest {
    private String userId;
    
    // getter와 setter
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}