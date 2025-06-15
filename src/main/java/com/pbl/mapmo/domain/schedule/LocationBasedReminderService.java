package com.pbl.mapmo.domain.schedule;

import com.pbl.mapmo.domain.notification.NotificationService;
import com.pbl.mapmo.domain.notification.NotificationType;
import com.pbl.mapmo.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationBasedReminderService {

    private final ScheduleRepository scheduleRepository;
    private final NotificationService notificationService;
    
    /**
     * 사용자 위치를 기반으로 근처 일정을 확인하고 알림을 전송합니다.
     * 
     * @param userId 사용자 ID
     * @param currentLatitude 현재 위도
     * @param currentLongitude 현재 경도
     * @param proximityRadius 근접 반경(km)
     */
    @Transactional(readOnly = true)
    public void checkProximityAndNotify(Integer userId, Double currentLatitude,
                                        Double currentLongitude, Double proximityRadius) {
        log.info("위치 기반 알림 확인: 사용자 ID {}, 위치: {}, {}", userId, currentLatitude, currentLongitude);

        // 사용자의 모든 일정 중 위치 정보가 있는 일정만 가져옴
        List<Schedule> schedulesWithLocation = scheduleRepository.findSchedulesWithLocationByUserId(userId);

        // 현재 시간 가져오기
        LocalDateTime now = LocalDateTime.now();

        for (Schedule schedule : schedulesWithLocation) {
            if (schedule.getLatitude() != null && schedule.getLongitude() != null) {
                // 현재 위치와 일정 위치 간의 거리 계산
                double distance = calculateDistance(
                        currentLatitude, currentLongitude,
                        schedule.getLatitude(), schedule.getLongitude()
                );

                // 설정된 반경 이내이고 현재 시간이 일정 시간과 일치하는 경우에만 알림 전송
                if (distance <= proximityRadius && isTimeMatch(now, schedule.getDate())) {
                    sendProximityNotification(schedule);
                }
            }
        }
    }

    /**
     * 현재 시간이 일정 시간과 일치하는지 확인합니다.
     *
     * @param now 현재 시간
     * @param scheduleDateTime 일정 시간
     * @return 시간이 일치하면 true, 그렇지 않으면 false
     */
    private boolean isTimeMatch(LocalDateTime now, LocalDateTime scheduleDateTime) {
        // 같은 날짜인지 확인 (연, 월, 일이 모두 같은지)
        boolean isSameDate = now.getYear() == scheduleDateTime.getYear() &&
                now.getMonthValue() == scheduleDateTime.getMonthValue() &&
                now.getDayOfMonth() == scheduleDateTime.getDayOfMonth();

        // 현재 시간이 일정 시간 전후 1시간 이내인지 확인
        // (시간의 정확도를 위해 1시간의 여유를 둠)
        boolean isTimeNear = Math.abs(now.getHour() - scheduleDateTime.getHour()) <= 1;

        return isSameDate && isTimeNear;
    }

    /**
     * 근처 일정에 대한 알림을 전송합니다.
     * 
     * @param schedule 알림 대상 일정
     */
    private void sendProximityNotification(Schedule schedule) {
        User user = schedule.getUser();
        String title = "근처 일정 알림";
        String message = String.format("'%s' 일정 장소에 근접했습니다.", schedule.getTitle());

        Map<String, String> data = new HashMap<>();
        data.put("scheduleId", schedule.getId().toString());
        data.put("scheduleTitle", schedule.getTitle());
        if (schedule.getLocation() != null) {
            data.put("location", schedule.getLocation());
        }
        data.put("scheduleDate", schedule.getDate().toString());

        notificationService.sendPushNotification(
                user, title, message,
                NotificationType.LOCATION_PROXIMITY,
                schedule.getId().longValue(), data
        );

        log.info("위치 기반 알림 전송: 사용자 ID {}, 일정 ID {}, 제목: {}, 날짜: {}",
                user.getId(), schedule.getId(), schedule.getTitle(), schedule.getDate());
    }

    /**
     * 두 지점 간의 거리를 계산합니다 (Haversine 공식 사용).
     * 
     * @return 거리(km)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 지구 반경 (km)
        final int R = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}