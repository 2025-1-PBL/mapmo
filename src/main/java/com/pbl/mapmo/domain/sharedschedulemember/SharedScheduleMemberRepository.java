package com.pbl.mapmo.domain.sharedschedulemember;

import com.pbl.mapmo.domain.sharedschedulememberid.SharedScheduleMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedScheduleMemberRepository extends JpaRepository<SharedScheduleMember, SharedScheduleMemberId> {
    List<SharedScheduleMember> findBySharedScheduleId(Integer sharedScheduleId);
    List<SharedScheduleMember> findByUserMemberId(Integer userMemberId);
    void deleteBySharedScheduleId(Integer sharedScheduleId);
}