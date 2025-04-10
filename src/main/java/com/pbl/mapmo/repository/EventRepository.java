package com.pbl.mapmo.repository;

import com.pbl.mapmo.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByBrandId(Integer brandId);
    List<Event> findByTitleContaining(String title);
    List<Event> findByContentContaining(String content);
    List<Event> findByTitleContainingOrContentContaining(String title, String content);
}