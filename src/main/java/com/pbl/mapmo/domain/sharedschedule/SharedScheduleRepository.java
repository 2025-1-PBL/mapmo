package com.pbl.mapmo.domain.sharedschedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedScheduleRepository extends JpaRepository<SharedSchedule, Integer> {
    List<SharedSchedule> findByUserMasterId(Integer userMasterId);
    Optional<SharedSchedule> findByScheduleId(Integer scheduleId);
}