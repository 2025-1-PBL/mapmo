package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.Block;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

public class BlockDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotEmpty(message = "차단할 사용자 ID를 하나 이상 포함해야 합니다")
        private List<@NotNull Integer> memberIds;
    }

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private UserDto.Response user;
        private List<BlockMemberDto.Response> blockMembers;

        public static Response of(Block block) {
            return Response.builder()
                    .id(block.getId())
                    .user(UserDto.Response.of(block.getUser()))
                    .blockMembers(block.getBlockMembers() != null ?
                            BlockMemberDto.Response.of(block.getBlockMembers()) : null)
                    .build();
        }
    }
}