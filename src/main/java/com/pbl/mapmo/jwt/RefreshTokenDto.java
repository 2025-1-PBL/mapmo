package com.pbl.mapmo.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 리프레시 토큰 정보를 전달하기 위한 데이터 전송 객체(DTO)
 * 사용자명과 리프레시 토큰을 담아 클라이언트-서버 간 통신에 사용됩니다.
 */
@Getter  // 게터 메서드 자동 생성 (필드 값 조회)
@Setter  // 세터 메서드 자동 생성 (필드 값 설정)
@NoArgsConstructor  // 매개변수 없는 기본 생성자 생성
@AllArgsConstructor  // 모든 필드를 매개변수로 받는 생성자 생성
public class RefreshTokenDto {
    /**
     * 사용자 식별자(아이디 또는 이메일)
     */
    private String username;

    /**
     * JWT 리프레시 토큰 값
     * 액세스 토큰이 만료되었을 때 새로운 액세스 토큰을 발급받는 데 사용됩니다.
     */
    private String refreshToken;
}