package com.pbl.mapmo.domain.friend;

import com.pbl.mapmo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Integer> {

    // 유저의 모든 친구 조회 (수락된 상태인 경우만)
    List<Friend> findByUserAndStatus(User user, Friend.FriendStatus status);

    // 받은 친구 요청 조회
    List<Friend> findByFriendAndStatus(User friend, Friend.FriendStatus status);

    // 특정 사용자와의 친구 관계 조회
    Optional<Friend> findByUserAndFriend(User user, User friend);

    // 친구 관계 조회 (양방향)
    Optional<Friend> findByUserAndFriendOrFriendAndUser(User user1, User friend1, User user2, User friend2);

    // 친구 관계 존재 여부 확인
    boolean existsByUserAndFriend(User user, User friend);
}