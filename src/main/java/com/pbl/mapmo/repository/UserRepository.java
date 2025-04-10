package com.pbl.mapmo.repository;

import com.pbl.mapmo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> { // User 엔티티의 PK 타입 명시 (Integer)
    Optional<User> findByEmail(String email); // 이메일로 사용자 조회. 이메일 없을수도 있으니 Optional
    List<User> findByIsDeletedFalse(); // 삭제되지 않은 사용자 조회
    List<User> findByNameContaining(String name); // 이름에 일부 문자열 포함된 사용자 조회
    boolean existsByEmail(String email); // 해당 이메일 db에 존재 여부 확인
}
