package com.pbl.mapmo.repository;

import com.pbl.mapmo.entity.ScheduleComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleCommentRepository extends JpaRepository<ScheduleComment, Integer> {
    List<ScheduleComment> findByScheduleId(Integer scheduleId);
    List<ScheduleComment> findByUserId(Integer userId);
    void deleteByScheduleId(Integer scheduleId);
}