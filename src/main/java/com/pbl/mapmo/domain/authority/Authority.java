package com.pbl.mapmo.domain.authority;

import com.pbl.mapmo.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "authority")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "authority_name", nullable = false)
    private String authorityName; // 예: "ROLE_USER", "ROLE_ADMIN"
}