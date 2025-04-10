package com.pbl.mapmo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "sharedSchedule"})
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String title;

    private String content;

    @Column(length = 255)
    private String location;

    @Column(precision = 10, scale = 6)
    private Double latitude;

    @Column(precision = 10, scale = 6)
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Column(name = "is_shared", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isShared = false;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "schedule_ibfk_1"))
    private User user;

    @OneToOne(mappedBy = "schedule")
    private SharedSchedule sharedSchedule; // 스케줄 하나는 하나의 공유스케줄만 가짐
}

