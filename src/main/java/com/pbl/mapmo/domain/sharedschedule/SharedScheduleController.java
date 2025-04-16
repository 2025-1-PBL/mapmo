package com.pbl.mapmo.domain.sharedschedule;

import com.pbl.mapmo.domain.sharedschedulemember.SharedScheduleMember;
import com.pbl.mapmo.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shared-schedules")
public class SharedScheduleController {

    private final SharedScheduleService sharedScheduleService;

    @Autowired
    public SharedScheduleController(SharedScheduleService sharedScheduleService) {
        this.sharedScheduleService = sharedScheduleService;
    }

    /**
     * 일정 공유하기
     */
    @PostMapping("/share")
    public ResponseEntity<SharedSchedule> shareSchedule(@RequestParam Integer scheduleId,
                                                        @RequestParam Integer userId) {
        try {
            SharedSchedule sharedSchedule = sharedScheduleService.shareSchedule(scheduleId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(sharedSchedule);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("이미 공유 중인 일정입니다")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 공유 일정에 멤버 추가
     */
    @PostMapping("/{sharedScheduleId}/members")
    public ResponseEntity<SharedScheduleMember> addMemberToSharedSchedule(
            @PathVariable Integer sharedScheduleId,
            @RequestParam Integer memberUserId,
            @RequestParam Integer masterId) {
        try {
            SharedScheduleMember member = sharedScheduleService.addMemberToSharedSchedule(
                    sharedScheduleId, memberUserId, masterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(member);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("이미 공유 일정에 추가된 멤버입니다")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 공유 일정에서 멤버 제거
     */
    @DeleteMapping("/{sharedScheduleId}/members/{memberUserId}")
    public ResponseEntity<Void> removeMemberFromSharedSchedule(
            @PathVariable Integer sharedScheduleId,
            @PathVariable Integer memberUserId,
            @RequestParam Integer requestUserId) {
        try {
            sharedScheduleService.removeMemberFromSharedSchedule(
                    sharedScheduleId, memberUserId, requestUserId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 공유 일정 취소
     */
    @DeleteMapping("/{sharedScheduleId}")
    public ResponseEntity<Void> cancelSharedSchedule(
            @PathVariable Integer sharedScheduleId,
            @RequestParam Integer userId) {
        try {
            sharedScheduleService.cancelSharedSchedule(sharedScheduleId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 사용자에게 공유된 모든 일정 조회
     */
    @GetMapping("/shared-with-me")
    public ResponseEntity<List<SharedSchedule>> getSharedSchedulesForUser(@RequestParam Integer userId) {
        List<SharedSchedule> sharedSchedules = sharedScheduleService.getSharedSchedulesForUser(userId);
        return ResponseEntity.ok(sharedSchedules);
    }

    /**
     * 사용자가 소유한 모든 공유 일정 조회
     */
    @GetMapping("/owned")
    public ResponseEntity<List<SharedSchedule>> getOwnedSharedSchedules(@RequestParam Integer userId) {
        List<SharedSchedule> ownedSchedules = sharedScheduleService.getOwnedSharedSchedules(userId);
        return ResponseEntity.ok(ownedSchedules);
    }

    /**
     * 특정 공유 일정의 모든 멤버 조회
     */
    @GetMapping("/{sharedScheduleId}/members")
    public ResponseEntity<List<User>> getMembersOfSharedSchedule(@PathVariable Integer sharedScheduleId) {
        List<User> members = sharedScheduleService.getMembersOfSharedSchedule(sharedScheduleId);
        return ResponseEntity.ok(members);
    }
}