package com.pbl.mapmo.repository;

import com.pbl.mapmo.entity.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseRepository extends JpaRepository<Franchise, Integer> {
    List<Franchise> findByBrandId(Integer brandId);
    List<Franchise> findByNameContaining(String name);
    List<Franchise> findByLocationContaining(String location);
}