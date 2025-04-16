package com.pbl.mapmo.domain.blockmember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockMemberRepository extends JpaRepository<BlockMember, Integer> {
    List<BlockMember> findByBlockId(Integer blockId);
    List<BlockMember> findByUserId(Integer userId);

    Optional<BlockMember> findByBlockIdAndUserId(Integer blockId, Integer userId);

    boolean existsByBlockIdAndUserId(Integer blockId, Integer userId); // 차단 여부 확인
    void deleteByBlockIdAndUserId(Integer blockId, Integer userId);
}