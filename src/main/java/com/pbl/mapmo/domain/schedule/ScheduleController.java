package com.pbl.mapmo.domain.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "Schedule", description = "일정 관련 API")
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 사용자의 모든 일정 조회
     */
    @Operation(summary = "사용자 모든 일정 조회")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Schedule>> getAllSchedulesByUserId(@PathVariable Integer userId) {
        List<Schedule> schedules = scheduleService.getAllSchedulesByUserId(userId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * 일정 ID로 특정 일정 조회
     */
    @Operation(summary = "일정 상세 조회")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Integer scheduleId) {
        Optional<Schedule> schedule = scheduleService.getScheduleById(scheduleId);
        return schedule.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 새로운 일정 생성
     */
    @Operation(summary = "일정 생성")
    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody Schedule schedule,
                                                   @RequestParam Integer userId) {
        try {
            Schedule createdSchedule = scheduleService.createSchedule(schedule, userId);
            
            // User 객체 대신 필요한 정보만 반환
            Map<String, Object> response = new HashMap<>();
            response.put("id", createdSchedule.getId());
            response.put("title", createdSchedule.getTitle());
            response.put("content", createdSchedule.getContent());
            response.put("location", createdSchedule.getLocation());
            response.put("latitude", createdSchedule.getLatitude());
            response.put("longitude", createdSchedule.getLongitude());
            response.put("reminderTime", createdSchedule.getReminderTime());
            response.put("reminderEnabled", createdSchedule.getReminderEnabled());
            response.put("date", createdSchedule.getDate());
            response.put("isShared", createdSchedule.getIsShared());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "일정 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 기존 일정 수정
     */
    @Operation(summary = "일정 수정")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Integer scheduleId,
                                                   @RequestBody Schedule updatedSchedule,
                                                   @RequestParam Integer userId) {
        try {
            Schedule schedule = scheduleService.updateSchedule(scheduleId, updatedSchedule, userId);
            return ResponseEntity.ok(schedule);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 일정 삭제
     */
    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer scheduleId,
                                               @RequestParam Integer userId) {
        try {
            scheduleService.deleteSchedule(scheduleId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 일정 공유 상태 변경
     */
    @Operation(summary = "일정 공유 상태 변경")
    @PatchMapping("/{scheduleId}/share")
    public ResponseEntity<Schedule> updateScheduleShareStatus(@PathVariable Integer scheduleId,
                                                              @RequestBody Map<String, Boolean> shareStatus,
                                                              @RequestParam Integer userId) {
        try {
            Boolean isShared = shareStatus.get("isShared");
            if (isShared == null) {
                return ResponseEntity.badRequest().build();
            }

            Schedule schedule = scheduleService.updateScheduleShareStatus(scheduleId, isShared, userId);
            return ResponseEntity.ok(schedule);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 특정 위치 주변의 일정 검색
     */
    @Operation(summary = "주변 일정 검색")
    @GetMapping("/nearby")
    public ResponseEntity<List<Schedule>> findSchedulesNearby(@RequestParam Double latitude,
                                                              @RequestParam Double longitude,
                                                              @RequestParam Double radius) {
        List<Schedule> nearbySchedules = scheduleService.findSchedulesNearby(latitude, longitude, radius);
        return ResponseEntity.ok(nearbySchedules);
    }

    /**
     * 일정 알림 설정
     */
    @PatchMapping("/{scheduleId}/reminder")
    public ResponseEntity<Schedule> updateScheduleReminder(
            @PathVariable Integer scheduleId,
            @RequestBody Map<String, Object> reminderData,
            @RequestParam Integer userId) {

        Boolean enabled = (Boolean) reminderData.get("enabled");
        String reminderTimeStr = (String) reminderData.get("reminderTime");
        LocalDateTime reminderTime = null;

        if (reminderTimeStr != null) {
            reminderTime = LocalDateTime.parse(reminderTimeStr);
        }

        Schedule updatedSchedule = scheduleService.updateScheduleReminder(
                scheduleId, enabled, reminderTime, userId);

        return ResponseEntity.ok(updatedSchedule);
    }
}