package com.pbl.mapmo;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment") // 테이블 이름은 기존과 동일
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScheduleComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreatedDate
    @Column(columnDefinition = "DATETIME", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime lastModifiedDate;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "comment_ibfk_1"), nullable = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "schedule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "comment_ibfk_2"), nullable = true)
    private Schedule schedule;
}
