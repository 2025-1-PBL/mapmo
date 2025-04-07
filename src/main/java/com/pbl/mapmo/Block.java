package com.pbl.mapmo;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "block")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 차단한 사용자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_block_user"))
    private User user; // 차단한 사용자

    // 차단된 사용자 목록 (BlockMember와 연결)
    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlockMember> blockMembers;
}
