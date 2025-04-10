package com.pbl.mapmo.repository;

import com.pbl.mapmo.entity.BlockMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockMemberRepository extends JpaRepository<BlockMember, Integer> {
    List<BlockMember> findByBlockId(Integer blockId);
    List<BlockMember> findByUserId(Integer userId);

    boolean existsByBlockIdAndUserId(Integer blockId, Integer userId); // 차단 여부 확인
    void deleteByBlockIdAndUserId(Integer blockId, Integer userId);
}