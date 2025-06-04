package com.pbl.mapmo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {
    private String token;
    private String refreshToken;

    // 이전 생성자 유지 (하위 호환성)
    public TokenDto(String token) {
        this.token = token;
    }
}