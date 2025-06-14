package com.pbl.mapmo.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pbl.mapmo.domain.friend.Friend;
import com.pbl.mapmo.domain.schedule.Schedule;
import com.pbl.mapmo.domain.sharedschedule.SharedSchedule;
import com.pbl.mapmo.domain.sharedschedulemember.SharedScheduleMember;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.pbl.mapmo.domain.authority.Authority;
import java.util.ArrayList;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email; // 소셜 로그인과 로컬 회원이 같은 email로 가입 못하게 막을 수 있음

    @Column(nullable = true) // 명시적으로 NULL 허용
    private String password; // 소셜로그인의 경우 password null, DTO로 자체 가입자 구분

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String profilePic;  // 이미지 URL 저장

    @Column(name = "fcm_token")
    private String fcmToken; // fcm token

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender sex;

    public enum Gender {
        남, 여, 기타
    }

    @Builder.Default
    @Column(name = "is_deleted", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Schedule> schedules;

    @JsonIgnore
    @OneToMany(mappedBy = "userMaster")
    private List<SharedSchedule> masterSchedules;

    @JsonIgnore
    @OneToMany(mappedBy = "userMember")
    private List<SharedScheduleMember> sharedSchedulesAsMember;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Authority> authorities = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Friend> sentFriendRequests;

    @JsonIgnore
    @OneToMany(mappedBy = "friend")
    private List<Friend> receivedFriendRequests;
}

