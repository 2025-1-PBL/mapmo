package com.pbl.mapmo.domain.location;

import com.pbl.mapmo.domain.schedule.LocationBasedReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationBasedReminderService locationBasedReminderService;

    /**
     * 사용자의 현재 위치를 업데이트하고 근처 일정 알림을 확인합니다.
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateLocation(@RequestBody LocationUpdateRequest request,
                                            @RequestParam Integer userId) {
        try {
            // 기본 근접 반경 설정 (km)
            double proximityRadius = request.getProximityRadius() != null ?
                    request.getProximityRadius() : 0.5;

            locationBasedReminderService.checkProximityAndNotify(
                    userId,
                    request.getLatitude(),
                    request.getLongitude(),
                    proximityRadius
            );

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}