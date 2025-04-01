package com.pbl.mapmo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String title;

    private String content;

    private String location;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime date;

    @Column(name = "is_shared", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isShared = false;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "schedule_ibfk_1"))
    private User user;

    @OneToMany(mappedBy = "schedule")
    private List<SharedSchedule> sharedSchedules;
}

