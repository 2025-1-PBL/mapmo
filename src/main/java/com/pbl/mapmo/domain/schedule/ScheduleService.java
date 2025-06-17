package com.pbl.mapmo.domain.schedule;

import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository, UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    /**
     * 사용자의 모든 일정을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 모든 일정 목록
     */
    public List<Schedule> getAllSchedulesByUserId(Integer userId) {
        return scheduleRepository.findByUserId(userId);
    }

    /**
     * 위치 정보가 있는 일정을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 위치 정보가 있는 일정 목록
     */
    public List<Schedule> getSchedulesWithLocation(Integer userId) {
        return scheduleRepository.findSchedulesWithLocationByUserId(userId);
    }

    /**
     * 일정 ID와 사용자 ID를 기반으로 일정을 조회합니다.
     * 사용자가 해당 일정에 접근할 권한이 없으면 예외를 발생시킵니다.
     *
     * @param scheduleId 일정 ID
     * @param userId 사용자 ID
     * @return 조회된 일정
     * @throws RuntimeException 일정이 존재하지 않거나 사용자에게 권한이 없는 경우
     */
    private Schedule getScheduleByIdAndUserId(Integer scheduleId, Integer userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: " + scheduleId));

        // 일정의 소유자가 요청한 사용자인지 확인
        if (!schedule.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 일정에 대한 권한이 없습니다.");
        }

        return schedule;
    }

    @Transactional
    public Schedule updateMarkerColor(Integer scheduleId, String color, Integer userId) {
        Schedule schedule = getScheduleByIdAndUserId(scheduleId, userId);
        schedule.setMarkerColor(color);
        return scheduleRepository.save(schedule);
    }

    /**
     * 특정 일정을 ID로 조회합니다.
     *
     * @param scheduleId 일정 ID
     * @return 조회된 일정
     */
    public Optional<Schedule> getScheduleById(Integer scheduleId) {
        return scheduleRepository.findById(scheduleId);
    }

    /**
     * 새로운 일정을 생성합니다.
     *
     * @param schedule 생성할 일정 정보
     * @param userId   사용자 ID
     * @return 생성된 일정
     */
    @Transactional
    public Schedule createSchedule(Schedule schedule, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        schedule.setUser(user);
        schedule.setDate(LocalDateTime.now());
        return scheduleRepository.save(schedule);
    }

    /**
     * 기존 일정을 수정합니다.
     *
     * @param scheduleId      수정할 일정 ID
     * @param updatedSchedule 수정된 일정 정보
     * @param userId          사용자 ID (권한 확인용)
     * @return 수정된 일정
     */
    @Transactional
    public Schedule updateSchedule(Integer scheduleId, Schedule updatedSchedule, Integer userId) {
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));

        // 권한 확인 (자신의 일정만 수정 가능)
        if (!existingSchedule.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 일정을 수정할 권한이 없습니다.");
        }

        // 필드 업데이트
        existingSchedule.setTitle(updatedSchedule.getTitle());
        existingSchedule.setContent(updatedSchedule.getContent());
        existingSchedule.setLocation(updatedSchedule.getLocation());
        existingSchedule.setLatitude(updatedSchedule.getLatitude());
        existingSchedule.setLongitude(updatedSchedule.getLongitude());

        return scheduleRepository.save(existingSchedule);
    }

    /**
     * 일정을 삭제합니다.
     *
     * @param scheduleId 삭제할 일정 ID
     * @param userId     사용자 ID (권한 확인용)
     */
    @Transactional
    public void deleteSchedule(Integer scheduleId, Integer userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));

        // 권한 확인 (자신의 일정만 삭제 가능)
        if (!schedule.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 일정을 삭제할 권한이 없습니다.");
        }

        scheduleRepository.delete(schedule);
    }

    /**
     * 일정 공유 상태를 변경합니다.
     *
     * @param scheduleId 일정 ID
     * @param isShared   공유 상태
     * @param userId     사용자 ID (권한 확인용)
     * @return 수정된 일정
     */
    @Transactional
    public Schedule updateScheduleShareStatus(Integer scheduleId, Boolean isShared, Integer userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));

        // 권한 확인 (자신의 일정만 공유 상태 변경 가능)
        if (!schedule.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 일정의 공유 상태를 변경할 권한이 없습니다.");
        }

        schedule.setIsShared(isShared);
        return scheduleRepository.save(schedule);
    }

    /**
     * 특정 위치 주변의 일정을 검색합니다.
     *
     * @param latitude  위도
     * @param longitude 경도
     * @param radius    반경 (km)
     * @return 주변 일정 목록
     */
    public List<Schedule> findSchedulesNearby(Double latitude, Double longitude, Double radius) {
        // Haversine 공식을 사용하는 레포지토리 메소드 호출
        return scheduleRepository.findNearbySchedules(latitude, longitude, radius);
    }

    /**
     * 일정 알림 설정을 업데이트합니다.
     *
     * @param scheduleId 일정 ID
     * @param enabled 알림 활성화 여부
     * @param reminderTime 알림 시간
     * @param userId 사용자 ID (권한 확인용)
     * @return 수정된 일정
     */
    @Transactional
    public Schedule updateScheduleReminder(Integer scheduleId, Boolean enabled,
                                           LocalDateTime reminderTime, Integer userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + scheduleId));

        // 권한 확인
        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalStateException("이 일정을 수정할 권한이 없습니다.");
        }

        // 알림 설정 업데이트
        schedule.setReminderEnabled(enabled);

        if (reminderTime != null) {
            schedule.setReminderTime(reminderTime);
        } else if (enabled) {
            // 기본적으로 일정 15분 전에 알림
            schedule.setReminderTime(schedule.getDate().minusMinutes(15));
        }

        return scheduleRepository.save(schedule);
    }
}