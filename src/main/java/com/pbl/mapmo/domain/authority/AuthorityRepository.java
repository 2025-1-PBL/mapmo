package com.pbl.mapmo.domain.authority;

import com.pbl.mapmo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
    List<Authority> findByUser(User user);
}