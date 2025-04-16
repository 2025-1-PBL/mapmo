package com.pbl.mapmo.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByUserId(Integer userId); // 특정 유저의 모든 스케쥴 조회
    List<Schedule> findByUserIdAndDateBetween(Integer userId, LocalDateTime start, LocalDateTime end); // 특정 유저의 기간 내 스케쥴 조회
    List<Schedule> findByLocationContaining(String location); // 위치 기반으로 스케쥴 검색
    List<Schedule> findByIsSharedTrue(); // 공유된 스케쥴 검색
    List<Schedule> findByTitleContaining(String title); // 제목 기반으로 스케쥴 검색

    @Query(value = "SELECT * FROM schedule s WHERE " +
            "ST_Distance_Sphere(point(s.longitude, s.latitude), point(:lng, :lat)) <= :distance * 1000",
            nativeQuery = true)
    List<Schedule> findNearbySchedules(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("distance") Double distance);
}