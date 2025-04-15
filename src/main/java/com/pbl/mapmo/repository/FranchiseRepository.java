package com.pbl.mapmo.repository;

import com.pbl.mapmo.entity.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface FranchiseRepository extends JpaRepository<Franchise, Integer> {
    List<Franchise> findByBrandId(Integer brandId);
    List<Franchise> findByNameContaining(String name);
    List<Franchise> findByLocationContaining(String location);

    // 위치 기반 검색 메소드 (Haversine 공식을 사용한 네이티브 쿼리를 구현하거나,
    // 간단한 사각형 영역 검색 방식으로 구현할 수 있습니다)

    // Haversine 공식을 사용:
    @Query(value = "SELECT * FROM schedule s WHERE " +
            "ST_Distance_Sphere(point(s.longitude, s.latitude), point(:lng, :lat)) <= :distance * 1000",
            nativeQuery = true)
    List<Franchise> findNearbyFranchises(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("distance") Double distance);
}