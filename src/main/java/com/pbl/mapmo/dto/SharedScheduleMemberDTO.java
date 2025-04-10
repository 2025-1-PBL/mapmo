package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.SharedScheduleMember;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

public class SharedScheduleMemberDTO {

    @Getter
    @Builder
    public static class Response {
        private UserDTO.Response userMember;

        public static Response of(SharedScheduleMember member) {
            return Response.builder()
                    .userMember(UserDTO.Response.of(member.getUserMember()))
                    .build();
        }

        public static List<Response> of(List<SharedScheduleMember> members) {
            return members.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}