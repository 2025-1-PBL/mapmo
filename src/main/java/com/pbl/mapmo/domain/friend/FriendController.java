package com.pbl.mapmo.domain.friend;

import com.pbl.mapmo.common.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friend", description = "친구 관리 API")
@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    @Autowired
    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    /**
     * 친구 요청 보내기
     */
    @Operation(summary = "친구 요청 보내기")
    @PostMapping("/request")
    public ResponseEntity<FriendDto.Response> sendFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FriendDto.Request request) {
        Friend friend = friendService.sendFriendRequest(userDetails.getId(), request.getFriendId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FriendDto.Response.of(friend));
    }

    /**
     * 친구 요청 수락
     */
    @Operation(summary = "친구 요청 수락")
    @PutMapping("/accept/{requestId}")
    public ResponseEntity<FriendDto.Response> acceptFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer requestId) {
        Friend friend = friendService.acceptFriendRequest(userDetails.getId(), requestId);
        return ResponseEntity.ok(FriendDto.Response.of(friend));
    }

    /**
     * 친구 요청 거절
     */
    @Operation(summary = "친구 요청 거절")
    @PutMapping("/reject/{requestId}")
    public ResponseEntity<FriendDto.Response> rejectFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer requestId) {
        Friend friend = friendService.rejectFriendRequest(userDetails.getId(), requestId);
        return ResponseEntity.ok(FriendDto.Response.of(friend));
    }

    /**
     * 친구 삭제
     */
    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> deleteFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer friendId) {
        friendService.deleteFriend(userDetails.getId(), friendId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 나의 친구 목록 조회
     */
    @Operation(summary = "나의 친구 목록 조회")
    @GetMapping
    public ResponseEntity<List<FriendDto.FriendListResponse>> getFriendList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Friend> friends = friendService.getFriendList(userDetails.getId());
        return ResponseEntity.ok(FriendDto.FriendListResponse.of(friends, true));
    }

    /**
     * 받은 친구 요청 조회
     */
    @Operation(summary = "받은 친구 요청 조회")
    @GetMapping("/requests/received")
    public ResponseEntity<List<FriendDto.FriendListResponse>> getReceivedFriendRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Friend> receivedRequests = friendService.getReceivedFriendRequests(userDetails.getId());
        return ResponseEntity.ok(FriendDto.FriendListResponse.of(receivedRequests, false));
    }

    /**
     * 보낸 친구 요청 조회
     */
    @Operation(summary = "보낸 친구 요청 조회")
    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendDto.FriendListResponse>> getSentFriendRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Friend> sentRequests = friendService.getSentFriendRequests(userDetails.getId());
        return ResponseEntity.ok(FriendDto.FriendListResponse.of(sentRequests, true));
    }
}