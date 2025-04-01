package com.pbl.mapmo;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('남','여','기타')")
    private Gender sex;

    private String profilePic;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;

    public enum Gender {
        남, 여, 기타
    }

    @OneToMany(mappedBy = "user")
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "userMaster")
    private List<SharedSchedule> masterSchedules;

    @OneToMany(mappedBy = "userMember")
    private List<SharedScheduleMember> sharedSchedulesAsMember;
}

