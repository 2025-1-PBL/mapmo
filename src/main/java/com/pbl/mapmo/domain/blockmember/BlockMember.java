package com.pbl.mapmo.domain.blockmember;

import com.pbl.mapmo.domain.block.Block;
import com.pbl.mapmo.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "block_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BlockMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 차단 그룹 (Block)
    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false, referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_block_member_block"))
    private Block block; // 차단 그룹

    // 차단된 사용자 (member)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_block_member_user"))
    private User user; // 차단된 사용자
}
