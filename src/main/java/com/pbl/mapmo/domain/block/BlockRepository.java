package com.pbl.mapmo.domain.block;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Integer> {
    Optional<Block> findByUserId(Integer userId);
}