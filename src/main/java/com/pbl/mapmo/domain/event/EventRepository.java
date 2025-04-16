package com.pbl.mapmo.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByBrandId(Integer brandId);
    List<Event> findByTitleContaining(String title);

    // 추가된 메서드
    List<Event> findByStatus(Event.EventStatus status);
    List<Event> findByEndDateBefore(LocalDate date);
    List<Event> findByStartDateAfter(LocalDate date);
    List<Event> findByEndDateBetween(LocalDate startDate, LocalDate endDate);

    // 날짜 필드가 null인 이벤트 찾기
    @Query("SELECT e FROM Event e WHERE e.startDate IS NULL AND e.endDate IS NULL")
    List<Event> findEventsWithoutDates();
}