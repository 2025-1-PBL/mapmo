package com.pbl.mapmo.domain.schedule;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.sharedschedule.SharedSchedule;
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

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "marker-color", length = 20)
    private String markerColor;

    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;

    @Builder.Default
    @Column(name = "reminder_enabled", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean reminderEnabled = false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Builder.Default
    @Column(name = "is_shared", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isShared = false;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "schedule_ibfk_1"))
    private User user;

    @JsonIgnore
    @OneToOne(mappedBy = "schedule")
    private SharedSchedule sharedSchedule; // 스케줄 하나는 하나의 공유스케줄만 가짐
}

