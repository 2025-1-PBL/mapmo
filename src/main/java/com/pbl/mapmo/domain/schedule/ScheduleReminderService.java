package com.pbl.mapmo.domain.schedule;

import com.pbl.mapmo.domain.notification.NotificationService;
import com.pbl.mapmo.domain.notification.NotificationType;
import com.pbl.mapmo.domain.sharedschedule.SharedSchedule;
import com.pbl.mapmo.domain.sharedschedule.SharedScheduleRepository;
import com.pbl.mapmo.domain.sharedschedulemember.SharedScheduleMember;
import com.pbl.mapmo.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 일정 알림 서비스
 *
 * 일정 시작 시간이 다가올 때 사용자에게 알림을 보내는 서비스를 담당합니다.
 * 개인 일정 및 공유 일정에 대한 알림을 처리합니다.
 */
@Service
@RequiredArgsConstructor // 필수 필드를 포함하는 생성자를 자동 생성
@Slf4j // 로깅을 위한 Lombok 어노테이션
public class ScheduleReminderService {

    /**
     * 일정 정보에 접근하기 위한 레포지토리
     */
    private final ScheduleRepository scheduleRepository;

    /**
     * 공유 일정 정보에 접근하기 위한 레포지토리
     */
    private final SharedScheduleRepository sharedScheduleRepository;

    /**
     * 알림 전송을 담당하는 서비스
     */
    private final NotificationService notificationService;

    /**
     * 1분마다 실행되어 다가오는 일정 알림을 확인합니다.
     *
     * cron 표현식 "0 * * * * *"는 매 분의 0초에 실행됨을 의미합니다.
     * 현재 시간부터 다음 1분 사이에 알림이 설정된 일정을 찾아 알림을 전송합니다.
     */
    @Scheduled(cron = "0 * * * * *") // 매 분마다 실행
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
    public void checkUpcomingScheduleReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMinute = now.plusMinutes(1);

        log.info("일정 알림 스케줄러 실행: {}", now);

        // 다음 1분 안에 알림이 필요한 일정 조회
        // reminderEnabled가 true이고 reminderTime이 현재 시간과 다음 1분 사이인 일정을 조회
        List<Schedule> upcomingSchedules = scheduleRepository.findByReminderEnabledTrueAndReminderTimeBetween(
                now.truncatedTo(ChronoUnit.MINUTES),
                nextMinute.truncatedTo(ChronoUnit.MINUTES)
        );

        log.info("알림이 필요한 일정 수: {}", upcomingSchedules.size());

        // 각 일정에 대해 알림 전송 처리
        for (Schedule schedule : upcomingSchedules) {
            // 일정 소유자에게 알림 전송
            sendScheduleReminderToUser(schedule.getUser(), schedule);

            // 공유된 일정이라면 공유 멤버들에게도 알림 전송
            if (schedule.getIsShared()) {
                SharedSchedule sharedSchedule = schedule.getSharedSchedule();
                if (sharedSchedule != null) {
                    for (SharedScheduleMember member : sharedSchedule.getSharedMembers()) {
                        // 소유자는 위에서 이미 알림을 보냈으므로 제외
                        if (!member.getUserMember().getId().equals(schedule.getUser().getId())) {
                            sendScheduleReminderToUser(member.getUserMember(), schedule);
                        }
                    }
                }
            }
        }
    }

    /**
     * 특정 사용자에게 일정 알림을 전송합니다.
     *
     * @param user 알림을 받을 사용자
     * @param schedule 알림의 대상이 되는 일정
     */
    private void sendScheduleReminderToUser(User user, Schedule schedule) {
        // FCM 토큰이 없는 사용자는 푸시 알림을 받을 수 없으므로 처리 중단
        if (user.getFcmToken() == null) {
            log.warn("사용자 {}의 FCM 토큰이 없습니다.", user.getId());
            return;
        }

        // 알림 제목과 메시지 구성
        String title = "일정 알림";
        String message = String.format("일정 '%s'가 곧 시작됩니다.", schedule.getTitle());

        // 알림에 포함할 추가 데이터 설정
        Map<String, String> data = new HashMap<>();
        data.put("type", "SCHEDULE_REMINDER");
        data.put("scheduleId", schedule.getId().toString());

        // 위치 정보가 있다면 추가
        if (schedule.getLocation() != null) {
            data.put("location", schedule.getLocation());
        }

        // 위도/경도 정보가 있다면 추가
        if (schedule.getLatitude() != null && schedule.getLongitude() != null) {
            data.put("latitude", schedule.getLatitude().toString());
            data.put("longitude", schedule.getLongitude().toString());
        }

        // 알림 서비스를 통해 푸시 알림 전송
        notificationService.sendPushNotification(
                user,
                title,
                message,
                NotificationType.SCHEDULE_REMINDER,
                Long.valueOf(schedule.getId()),
                data
        );
    }
}