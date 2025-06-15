package com.pbl.mapmo.domain.location;

import lombok.Data;

@Data
public class LocationUpdateRequest {
    private Double latitude;
    private Double longitude;
    private Double proximityRadius;  // 선택적 파라미터, km 단위
}