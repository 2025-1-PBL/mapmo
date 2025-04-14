package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.User;
import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

public class UserDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "이메일은 필수 입력값입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력값입니다")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        private String password;

        @NotBlank(message = "이름은 필수 입력값입니다")
        private String name;

        private String profilePic;
        private User.Gender sex;

        public User toEntity() {
            return User.builder()
                    .email(email)
                    .password(password)
                    .name(name)
                    .profilePic(profilePic)
                    .sex(sex)
                    .isDeleted(false)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private String email;
        private String name;
        private String profilePic;
        private User.Gender sex;

        public static Response of(User user) {
            return Response.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profilePic(user.getProfilePic())
                    .sex(user.getSex())
                    .build();
        }

        public static List<Response> of(List<User> users) {
            return users.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}