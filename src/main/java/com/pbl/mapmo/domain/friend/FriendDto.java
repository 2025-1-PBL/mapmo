package com.pbl.mapmo.domain.friend;

import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FriendDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Integer friendId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Integer id;
        private UserDto.Response user;
        private UserDto.Response friend;
        private Friend.FriendStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response of(Friend friend) {
            return Response.builder()
                    .id(friend.getId())
                    .user(UserDto.Response.of(friend.getUser()))
                    .friend(UserDto.Response.of(friend.getFriend()))
                    .status(friend.getStatus())
                    .createdAt(friend.getCreatedAt())
                    .updatedAt(friend.getUpdatedAt())
                    .build();
        }

        public static List<Response> of(List<Friend> friends) {
            return friends.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FriendListResponse {
        private UserDto.Response friend;
        private Friend.FriendStatus status;

        public static FriendListResponse of(Friend friend, boolean isRequester) {
            return FriendListResponse.builder()
                    .friend(UserDto.Response.of(isRequester ? friend.getFriend() : friend.getUser()))
                    .status(friend.getStatus())
                    .build();
        }

        public static List<FriendListResponse> of(List<Friend> friends, boolean isRequester) {
            return friends.stream()
                    .map(friend -> of(friend, isRequester))
                    .collect(Collectors.toList());
        }
    }
}