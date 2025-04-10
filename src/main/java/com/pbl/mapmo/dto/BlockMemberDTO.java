package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.BlockMember;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

public class BlockMemberDTO {

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private UserDTO.Response user;

        public static Response of(BlockMember blockMember) {
            return Response.builder()
                    .id(blockMember.getId())
                    .user(UserDTO.Response.of(blockMember.getUser()))
                    .build();
        }

        public static List<Response> of(List<BlockMember> blockMembers) {
            return blockMembers.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}