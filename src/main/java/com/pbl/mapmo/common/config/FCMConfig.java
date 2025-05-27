package com.pbl.mapmo.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * Firebase Cloud Messaging(FCM) 설정 클래스
 * FCM을 사용하여 푸시 알림을 보내기 위한 Firebase 앱 초기화를 담당합니다.
 */
@Configuration
public class FCMConfig {

    /**
     * Firebase 앱 인스턴스를 생성하고 반환하는 빈
     *
     * @return Firebase 앱 인스턴스
     * @throws IOException Firebase 서비스 계정 파일을 읽는 중 오류가 발생할 경우
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 이미 Firebase 앱이 초기화되어 있는지 확인
        if (FirebaseApp.getApps().isEmpty()) {
            // Firebase 앱이 초기화되어 있지 않은 경우, 새로 초기화
            FirebaseOptions options = FirebaseOptions.builder()
                    // 클래스패스에서 firebase-service-account.json 파일을 읽어 인증 정보 설정
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource("firebase-service-account.json").getInputStream()))
                    .build();
            return FirebaseApp.initializeApp(options);
        } else {
            // 이미 초기화된 Firebase 앱이 있는 경우, 기존 인스턴스 반환
            return FirebaseApp.getInstance();
        }
    }
}