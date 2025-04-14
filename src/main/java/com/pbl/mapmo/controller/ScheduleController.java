package com.pbl.mapmo.controller;

import com.pbl.mapmo.entity.Schedule;
import com.pbl.mapmo.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Schedule>> getAllSchedulesByUserId(@PathVariable Integer userId) {
        List<Schedule> schedules = scheduleService.getAllSchedulesByUserId(userId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * 일정 ID로 특정 일정 조회
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Integer scheduleId) {
        Optional<Schedule> schedule = scheduleService.getScheduleById(scheduleId);
        return schedule.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 새로운 일정 생성
     */
    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody Schedule schedule,
                                                   @RequestParam Integer userId) {
        try {
            Schedule createdSchedule = scheduleService.createSchedule(schedule, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 기존 일정 수정
     */
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
    @GetMapping("/nearby")
    public ResponseEntity<List<Schedule>> findSchedulesNearby(@RequestParam Double latitude,
                                                              @RequestParam Double longitude,
                                                              @RequestParam Double radius) {
        List<Schedule> nearbySchedules = scheduleService.findSchedulesNearby(latitude, longitude, radius);
        return ResponseEntity.ok(nearbySchedules);
    }
}