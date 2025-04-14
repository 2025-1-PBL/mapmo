package com.pbl.mapmo.service;

import com.pbl.mapmo.entity.*;
import com.pbl.mapmo.repository.ScheduleRepository;
import com.pbl.mapmo.repository.SharedScheduleMemberRepository;
import com.pbl.mapmo.repository.SharedScheduleRepository;
import com.pbl.mapmo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SharedScheduleService {

    private final SharedScheduleRepository sharedScheduleRepository;
    private final SharedScheduleMemberRepository sharedScheduleMemberRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Autowired
    public SharedScheduleService(
            SharedScheduleRepository sharedScheduleRepository,
            SharedScheduleMemberRepository sharedScheduleMemberRepository,
            ScheduleRepository scheduleRepository,
            UserRepository userRepository) {
        this.sharedScheduleRepository = sharedScheduleRepository;
        this.sharedScheduleMemberRepository = sharedScheduleMemberRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    /**
     * 일정을 공유합니다.
     *
     * @param scheduleId 공유할 일정 ID
     * @param userId 공유하는 사용자 ID
     * @return 생성된 공유 일정
     */
    @Transactional
    public SharedSchedule shareSchedule(Integer scheduleId, Integer userId) {
        // 일정 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));

        // 권한 확인 (자신의 일정만 공유 가능)
        if (!schedule.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 일정을 공유할 권한이 없습니다.");
        }

        // 이미 공유 중인지 확인
        if (Boolean.TRUE.equals(schedule.getIsShared())) {
            throw new RuntimeException("이미 공유 중인 일정입니다.");
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 일정 공유 상태 변경
        schedule.setIsShared(true);
        scheduleRepository.save(schedule);

        // 공유 일정 생성
        SharedSchedule sharedSchedule = new SharedSchedule();
        sharedSchedule.setSchedule(schedule);
        sharedSchedule.setUserMaster(user);

        return sharedScheduleRepository.save(sharedSchedule);
    }

    /**
     * 공유 일정에 멤버를 추가합니다.
     *
     * @param sharedScheduleId 공유 일정 ID
     * @param memberUserId 추가할 멤버 ID
     * @param masterId 공유 일정 소유자 ID (권한 확인용)
     * @return 생성된 공유 일정 멤버
     */
    @Transactional
    public SharedScheduleMember addMemberToSharedSchedule(
            Integer sharedScheduleId, Integer memberUserId, Integer masterId) {

        // 공유 일정 조회
        SharedSchedule sharedSchedule = sharedScheduleRepository.findById(sharedScheduleId)
                .orElseThrow(() -> new RuntimeException("공유 일정을 찾을 수 없습니다."));

        // 권한 확인 (공유 일정 소유자만 멤버 추가 가능)
        if (!sharedSchedule.getUserMaster().getId().equals(masterId)) {
            throw new RuntimeException("이 공유 일정에 멤버를 추가할 권한이 없습니다.");
        }

        // 추가할 멤버 조회
        User memberUser = userRepository.findById(memberUserId)
                .orElseThrow(() -> new RuntimeException("추가할 사용자를 찾을 수 없습니다."));

        // 이미 추가된 멤버인지 확인
        SharedScheduleMemberId memberId = new SharedScheduleMemberId(sharedScheduleId, memberUserId);
        if (sharedScheduleMemberRepository.existsById(memberId)) {
            throw new RuntimeException("이미 공유 일정에 추가된 멤버입니다.");
        }

        // 공유 일정 멤버 생성
        SharedScheduleMember member = new SharedScheduleMember();
        member.setId(memberId);
        member.setSharedSchedule(sharedSchedule);
        member.setUserMember(memberUser);

        return sharedScheduleMemberRepository.save(member);
    }

    /**
     * 공유 일정에서 멤버를 제거합니다.
     *
     * @param sharedScheduleId 공유 일정 ID
     * @param memberUserId 제거할 멤버 ID
     * @param requestUserId 요청한 사용자 ID (권한 확인용)
     */
    @Transactional
    public void removeMemberFromSharedSchedule(
            Integer sharedScheduleId, Integer memberUserId, Integer requestUserId) {

        // 공유 일정 조회
        SharedSchedule sharedSchedule = sharedScheduleRepository.findById(sharedScheduleId)
                .orElseThrow(() -> new RuntimeException("공유 일정을 찾을 수 없습니다."));

        // 권한 확인 (공유 일정 소유자 또는 자기 자신만 제거 가능)
        if (!sharedSchedule.getUserMaster().getId().equals(requestUserId) && !memberUserId.equals(requestUserId)) {
            throw new RuntimeException("이 공유 일정에서 멤버를 제거할 권한이 없습니다.");
        }

        // 공유 일정 멤버 제거
        SharedScheduleMemberId memberId = new SharedScheduleMemberId(sharedScheduleId, memberUserId);
        sharedScheduleMemberRepository.deleteById(memberId);
    }

    /**
     * 공유 일정을 취소합니다.
     *
     * @param sharedScheduleId 취소할 공유 일정 ID
     * @param userId 요청한 사용자 ID (권한 확인용)
     */
    @Transactional
    public void cancelSharedSchedule(Integer sharedScheduleId, Integer userId) {
        // 공유 일정 조회
        SharedSchedule sharedSchedule = sharedScheduleRepository.findById(sharedScheduleId)
                .orElseThrow(() -> new RuntimeException("공유 일정을 찾을 수 없습니다."));

        // 권한 확인 (공유 일정 소유자만 취소 가능)
        if (!sharedSchedule.getUserMaster().getId().equals(userId)) {
            throw new RuntimeException("이 공유 일정을 취소할 권한이 없습니다.");
        }

        // 일정 공유 상태 변경
        Schedule schedule = sharedSchedule.getSchedule();
        schedule.setIsShared(false);
        scheduleRepository.save(schedule);

        // 공유 일정의 모든 멤버 제거
        sharedScheduleMemberRepository.deleteBySharedScheduleId(sharedScheduleId);

        // 공유 일정 제거
        sharedScheduleRepository.delete(sharedSchedule);
    }

    /**
     * 사용자에게 공유된 모든 일정을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 공유된 일정 목록
     */
    public List<SharedSchedule> getSharedSchedulesForUser(Integer userId) {
        // 사용자가 멤버로 있는 공유 일정 조회
        List<SharedScheduleMember> memberSharedSchedules =
                sharedScheduleMemberRepository.findByUserMemberId(userId);

        // SharedScheduleMember에서 SharedSchedule로 변환
        return memberSharedSchedules.stream()
                .map(SharedScheduleMember::getSharedSchedule)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 소유한 모든 공유 일정을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 소유한 공유 일정 목록
     */
    public List<SharedSchedule> getOwnedSharedSchedules(Integer userId) {
        return sharedScheduleRepository.findByUserMasterId(userId);
    }

    /**
     * 특정 공유 일정의 모든 멤버를 조회합니다.
     *
     * @param sharedScheduleId 공유 일정 ID
     * @return 공유 일정의 멤버 목록
     */
    public List<User> getMembersOfSharedSchedule(Integer sharedScheduleId) {
        List<SharedScheduleMember> members =
                sharedScheduleMemberRepository.findBySharedScheduleId(sharedScheduleId);

        return members.stream()
                .map(SharedScheduleMember::getUserMember)
                .collect(Collectors.toList());
    }
}