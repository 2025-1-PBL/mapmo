package com.pbl.mapmo.domain.friend;

import com.pbl.mapmo.domain.notification.NotificationService;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public FriendService(FriendRepository friendRepository, UserService userService,
                         NotificationService notificationService) { // 생성자 주입
        this.friendRepository = friendRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    /**
     * 친구 요청 보내기
     *
     * @param userId 요청을 보내는 사용자 ID
     * @param friendId 친구로 요청할 사용자 ID
     * @return 생성된 친구 요청
     */
    @Transactional
    public Friend sendFriendRequest(Integer userId, Integer friendId) {
        // 자기 자신에게 요청 방지
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("요청자를 찾을 수 없습니다."));
        User friend = userService.getUserById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청할 사용자를 찾을 수 없습니다."));

        // 이미 친구 요청이 존재하는지 확인
        Optional<Friend> existingRequest = friendRepository.findByUserAndFriendOrFriendAndUser(
                user, friend, friend, user);

        if (existingRequest.isPresent()) {
            throw new IllegalArgumentException("이미 친구 관계이거나 요청이 진행 중입니다.");
        }

        Friend friendRequest = Friend.builder()
                .user(user)
                .friend(friend)
                .status(Friend.FriendStatus.PENDING)
                .build();

        Friend savedFriendRequest = friendRepository.save(friendRequest);

        // 알림 보내기 (수정된 부분)
        notificationService.createFriendRequestNotification(
                friend,  // 요청을 받는 사용자 (receiver)
                user,    // 요청을 보낸 사용자 (sender)
                savedFriendRequest.getId().longValue() // Integer를 Long으로 변환
        );

        return friendRepository.save(friendRequest);
    }

    /**
     * 친구 요청 수락
     *
     * @param userId 수락하는 사용자 ID
     * @param requestId 친구 요청 ID
     * @return 수락된 친구 관계
     */
    @Transactional
    public Friend acceptFriendRequest(Integer userId, Integer requestId) {
        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        // 요청을 받은 사용자인지 확인
        if (!friendRequest.getFriend().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 요청을 수락할 권한이 없습니다.");
        }

        // 이미 처리된 요청인지 확인
        if (friendRequest.getStatus() != Friend.FriendStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }

        friendRequest.setStatus(Friend.FriendStatus.ACCEPTED);
        return friendRepository.save(friendRequest);
    }

    /**
     * 친구 요청 거절
     *
     * @param userId 거절하는 사용자 ID
     * @param requestId 친구 요청 ID
     * @return 거절된 친구 관계
     */
    @Transactional
    public Friend rejectFriendRequest(Integer userId, Integer requestId) {
        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        // 요청을 받은 사용자인지 확인
        if (!friendRequest.getFriend().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 요청을 거절할 권한이 없습니다.");
        }

        // 이미 처리된 요청인지 확인
        if (friendRequest.getStatus() != Friend.FriendStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }

        friendRequest.setStatus(Friend.FriendStatus.REJECTED);
        return friendRepository.save(friendRequest);
    }

    /**
     * 친구 삭제
     *
     * @param userId 삭제를 요청하는 사용자 ID
     * @param friendId 삭제할 친구 관계 ID
     */
    @Transactional
    public void deleteFriend(Integer userId, Integer friendId) {
        Friend friendship = friendRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));

        // 친구 관계의 당사자인지 확인
        if (!friendship.getUser().getId().equals(userId) && !friendship.getFriend().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 친구 관계를 삭제할 권한이 없습니다.");
        }

        friendRepository.delete(friendship);
    }

    /**
     * 사용자의 친구 목록 조회
     *
     * @param userId 조회할 사용자 ID
     * @return 친구 목록
     */
    public List<Friend> getFriendList(Integer userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return friendRepository.findByUserAndStatus(user, Friend.FriendStatus.ACCEPTED);
    }

    /**
     * 사용자가 받은 친구 요청 조회
     *
     * @param userId 조회할 사용자 ID
     * @return 받은 친구 요청 목록
     */
    public List<Friend> getReceivedFriendRequests(Integer userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return friendRepository.findByFriendAndStatus(user, Friend.FriendStatus.PENDING);
    }

    /**
     * 사용자가 보낸 친구 요청 조회
     *
     * @param userId 조회할 사용자 ID
     * @return 보낸 친구 요청 목록
     */
    public List<Friend> getSentFriendRequests(Integer userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return friendRepository.findByUserAndStatus(user, Friend.FriendStatus.PENDING);
    }
}